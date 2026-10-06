package com.moonsolstudios.kavvoro.engine

import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.collision.shapes.EdgeShape
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.callbacks.ContactImpulse
import org.jbox2d.callbacks.ContactListener
import org.jbox2d.collision.Manifold
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.dynamics.World
import org.jbox2d.dynamics.contacts.Contact
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

class PhysicsEngine {
    private var world = World(Vec2(0f, 9.8f))
    private var ballBody: Body? = null
    private var level: LevelSpec? = null
    private var riftAnchor: Point2? = null
    private var riftStrength = 0f
    private var ballPower = BallPower.NONE
    private var prismShieldAvailable = false
    private var powerTriggeredThisStep = false
    private var impactStrengthThisStep = 0f
    private var portalTriggeredThisStep = false
    private var portalCooldown = 0f
    private val scratchForce = Vec2()
    private val scratchImpulse = Vec2()
    private val scratchTransform = Vec2()
    private val scratchVelocity = Vec2()

    fun reset(spec: LevelSpec, power: BallPower = BallPower.NONE) {
        level = spec
        ballPower = power
        prismShieldAvailable = power == BallPower.PRISM_SHIELD
        powerTriggeredThisStep = false
        portalTriggeredThisStep = false
        portalCooldown = 0f
        riftAnchor = null
        riftStrength = 0f
        world = World(Vec2(0f, spec.gravity))
        configureContactAudio()
        createBounds(spec.stageHeight)
        createBlocks(spec)
        createBall(spec)
    }

    fun setRiftControl(anchor: Point2?, strength: Float) {
        riftAnchor = anchor
        riftStrength = if (anchor == null) 0f else strength.coerceIn(0f, 1f)
    }

    fun step(dt: Float, elapsedSeconds: Float): PhysicsFrame {
        val spec = level ?: return PhysicsFrame(Point2(0f, 0f), 0f, 0f, PhysicsOutcome.LOST)
        val body = ballBody ?: return PhysicsFrame(spec.start, 0f, 0f, PhysicsOutcome.LOST)

        powerTriggeredThisStep = false
        portalTriggeredThisStep = false
        impactStrengthThisStep = 0f
        portalCooldown = max(0f, portalCooldown - dt)
        val pulseIntensity = max(
            applyPlayerRift(spec, body),
            max(
                applyPulseForces(spec, body, elapsedSeconds),
                applyRiftWind(spec, body, elapsedSeconds)
            )
        )
        world.step(min(dt, 1f / 30f), 8, 3)
        val portalIntensity = resolvePortal(spec, body)
        limitBallSpeed(spec, body)

        val position = body.position
        val velocity = body.linearVelocity
        val ball = Point2(position.x, position.y)
        val speed = sqrt(velocity.x * velocity.x + velocity.y * velocity.y)
        val outcome = resolveOutcome(spec, body, ball, elapsedSeconds)
        return PhysicsFrame(
            ball = ball,
            speed = speed,
            pulseIntensity = max(pulseIntensity, portalIntensity),
            outcome = outcome,
            riftAnchor = riftAnchor?.takeIf { riftStrength > 0f },
            riftStrength = riftStrength,
            elapsedSeconds = elapsedSeconds,
            powerTriggered = powerTriggeredThisStep,
            impactStrength = impactStrengthThisStep,
            portalTriggered = portalTriggeredThisStep
        )
    }

    private fun configureContactAudio() {
        world.setContactListener(object : ContactListener {
            override fun beginContact(contact: Contact) = Unit

            override fun endContact(contact: Contact) = Unit

            override fun preSolve(contact: Contact, oldManifold: Manifold) = Unit

            override fun postSolve(contact: Contact, impulse: ContactImpulse) {
                val ball = ballBody ?: return
                val touchesBall = contact.fixtureA.body === ball || contact.fixtureB.body === ball
                if (!touchesBall || impulse.count <= 0) return
                var strongestImpulse = 0f
                for (index in 0 until impulse.count) {
                    strongestImpulse = max(strongestImpulse, impulse.normalImpulses[index])
                }
                val velocityChange = strongestImpulse / max(ball.mass, 0.05f)
                impactStrengthThisStep = max(impactStrengthThisStep, velocityChange)
            }
        })
    }

    fun currentBall(): Point2 {
        val body = ballBody ?: return level?.start ?: Point2(0f, 0f)
        return Point2(body.position.x, body.position.y)
    }

    private fun createBounds(stageHeight: Float) {
        createEdge(Point2(STAGE_WALL_INSET, -1f), Point2(STAGE_WALL_INSET, stageHeight + 1.2f), friction = 0.45f)
        createEdge(Point2(STAGE_WIDTH - STAGE_WALL_INSET, -1f), Point2(STAGE_WIDTH - STAGE_WALL_INSET, stageHeight + 1.2f), friction = 0.45f)
        createEdge(Point2(0f, 0f), Point2(STAGE_WIDTH, 0f), friction = 0.45f)
    }

    private fun createBlocks(spec: LevelSpec) {
        for (block in spec.blocks) {
            val bodyDef = BodyDef().apply {
                type = BodyType.STATIC
                position.set(0f, 0f)
            }
            val body = world.createBody(bodyDef)
            val shape = PolygonShape().apply {
                setAsBox(
                    block.width * 0.5f,
                    block.height * 0.5f,
                    Vec2(block.center.x, block.center.y),
                    block.angleRadians
                )
            }
            val fixture = FixtureDef().apply {
                this.shape = shape
                friction = 0.62f
                restitution = 0.26f
            }
            body.createFixture(fixture)
        }
    }

    private fun createBall(spec: LevelSpec) {
        val bodyDef = BodyDef().apply {
            type = BodyType.DYNAMIC
            position.set(spec.start.x, spec.start.y)
            linearDamping = 0.018f
            angularDamping = 0.035f
            bullet = true
        }
        val body = world.createBody(bodyDef)
        val circle = CircleShape().apply {
            m_radius = BALL_RADIUS
        }
        val fixture = FixtureDef().apply {
            shape = circle
            density = 0.43f
            friction = 0.58f
            restitution = when (ballPower) {
                BallPower.CHROME_RICOCHET -> 0.74f
                BallPower.MINOR_RICOCHET -> 0.52f
                else -> 0.38f
            }
        }
        body.createFixture(fixture)
        val dx = spec.goal.x - spec.start.x
        val dy = spec.goal.y - spec.start.y
        val distance = sqrt(dx * dx + dy * dy).coerceAtLeast(0.01f)
        body.linearVelocity = Vec2(
            dx / distance * 1.5f,
            max(0.33f, dy / distance * 0.55f)
        )
        ballBody = body
    }

    private fun createEdge(a: Point2, b: Point2, friction: Float) {
        val bodyDef = BodyDef().apply {
            type = BodyType.STATIC
        }
        val body = world.createBody(bodyDef)
        val edge = EdgeShape().apply {
            set(Vec2(a.x, a.y), Vec2(b.x, b.y))
        }
        val fixture = FixtureDef().apply {
            shape = edge
            this.friction = friction
            restitution = 0.14f
        }
        body.createFixture(fixture)
    }

    private fun applyPulseForces(spec: LevelSpec, body: Body, elapsed: Float): Float {
        var strongest = 0f
        val position = body.position
        val storm = spec.hasCurse(CurseType.PULSE_STORM)
        val fever = when {
            !storm -> 1f
            riftAnchor != null -> 0.82f
            else -> 1.48f
        }
        for (zone in spec.pulseZones) {
            val dx = position.x - zone.center.x
            val dy = position.y - zone.center.y
            val distSq = dx * dx + dy * dy
            val radiusSq = zone.radius * zone.radius
            if (distSq >= radiusSq || distSq <= 0.0001f) continue

            val dist = sqrt(distSq)
            val falloff = 1f - dist / zone.radius
            val wave = 0.72f + 0.28f * sin(elapsed * 3.4f + zone.phase)
            val nx = dx / dist
            val ny = dy / dist
            val pulseBoost = if (storm) 2.15f else 1.72f
            val radial = zone.radialForce * falloff * wave * fever * pulseBoost
            val swirl = zone.swirlForce * falloff * wave * fever * pulseBoost
            scratchForce.set(
                nx * radial + -ny * swirl,
                ny * radial + nx * swirl
            )
            body.applyForceToCenter(scratchForce)
            if (falloff > 0.48f) {
                val kick = 0.09f * falloff * wave * if (storm) 1.35f else 1f
                scratchImpulse.set(scratchForce.x * kick, scratchForce.y * kick)
                body.applyLinearImpulse(scratchImpulse, body.worldCenter)
            }
            strongest = max(strongest, min(1f, falloff * wave * 1.18f))
        }
        return strongest
    }

    private fun resolvePortal(spec: LevelSpec, body: Body): Float {
        if (portalCooldown > 0f || spec.portals.isEmpty()) return 0f
        val posX = body.position.x
        val posY = body.position.y
        for (portal in spec.portals) {
            val entryDx = posX - portal.entry.x
            val entryDy = posY - portal.entry.y
            val maxDist = portal.radius + BALL_RADIUS * 0.72f
            if (entryDx * entryDx + entryDy * entryDy > maxDist * maxDist) continue
            val velocity = body.linearVelocity
            val currentSpeed = sqrt(velocity.x * velocity.x + velocity.y * velocity.y)
            val goalDx = spec.goal.x - portal.exit.x
            val goalDy = spec.goal.y - portal.exit.y
            val goalDistance = sqrt(goalDx * goalDx + goalDy * goalDy).coerceAtLeast(0.01f)
            val speed = max(currentSpeed * 1.08f, 4.2f)
            val vx = velocity.x * 0.42f + goalDx / goalDistance * speed * 0.84f
            val vy = velocity.y * 0.42f + goalDy / goalDistance * speed * 0.84f
            scratchTransform.set(portal.exit.x, portal.exit.y)
            body.setTransform(scratchTransform, body.angle)
            scratchVelocity.set(vx, vy)
            body.linearVelocity = scratchVelocity
            portalCooldown = 0.58f
            portalTriggeredThisStep = true
            return 1f
        }
        return 0f
    }

    private fun applyPlayerRift(spec: LevelSpec, body: Body): Float {
        val anchor = riftAnchor
        if (anchor == null || riftStrength <= 0f) {
            body.linearDamping = 0.018f
            return 0f
        }

        val dx = anchor.x - body.position.x
        val dy = anchor.y - body.position.y
        val distanceSq = dx * dx + dy * dy
        if (distanceSq < 0.0001f) return riftStrength
        val distance = sqrt(distanceSq)
        val nx = dx / distance
        val ny = dy / distance
        val curseMultiplier = when {
            spec.hasCurse(CurseType.POWER_HOLD) -> 1.34f
            spec.hasCurse(CurseType.OVERHEAT) -> 1.18f
            spec.hasCurse(CurseType.HEAVY_CORE) -> 1.12f
            else -> 1f
        }
        val powerMultiplier = when (ballPower) {
            BallPower.PLASMA_SURGE -> 1.24f
            BallPower.MINOR_SURGE -> 1.1f
            else -> 1f
        }
        val force = (4.8f + min(distance, 4.6f) * 3.7f) * riftStrength * curseMultiplier * powerMultiplier
        scratchForce.set(nx * force, ny * force)
        body.applyForceToCenter(scratchForce)
        body.linearDamping = when {
            spec.hasCurse(CurseType.FOCUS_FIELD) -> 0.96f
            spec.hasCurse(CurseType.MOON_GLIDE) -> 0.2f
            spec.hasCurse(CurseType.POWER_HOLD) -> 0.35f
            else -> 0.5f
        }
        return (riftStrength * (0.45f + min(distance / 4f, 0.55f))).coerceIn(0f, 1f)
    }

    private fun applyRiftWind(spec: LevelSpec, body: Body, elapsed: Float): Float {
        if (!spec.hasCurse(CurseType.RIFT_WIND)) return 0f
        val windScale = riftWindScale(spec.index)
        val directionWave = sin(elapsed * 1.45f + (spec.seed % 13L) * 0.31f)
        val gustWave = sin(elapsed * 4.2f + spec.index * 0.73f) * 0.35f
        val holdGuard = if (riftAnchor != null) (0.18f + windScale * 0.16f).coerceIn(0.2f, 0.38f) else 1f
        val forceX = ((directionWave * 5.8f) + (gustWave * 2.8f)) * holdGuard
        val lift = -0.42f * abs(directionWave) * holdGuard
        scratchForce.set(forceX * windScale, lift * windScale)
        body.applyForceToCenter(scratchForce)
        return (abs(directionWave) * holdGuard * windScale).coerceIn(0f, 1f)
    }

    private fun riftWindScale(levelIndex: Int): Float {
        return when {
            levelIndex <= 10 -> 0.32f
            levelIndex <= 18 -> 0.42f
            levelIndex <= 28 -> 0.56f
            levelIndex <= 42 -> 0.7f
            levelIndex <= 65 -> 0.84f
            levelIndex <= 100 -> 0.96f
            else -> (1f + (levelIndex - 100) * 0.0015f).coerceAtMost(1.16f)
        }
    }

    private fun limitBallSpeed(spec: LevelSpec, body: Body) {
        val baseLimit = when {
            spec.index <= 5 -> 7.55f
            spec.index <= 10 -> 8.25f
            else -> 9.15f
        }
        val limit = when {
            spec.hasCurse(CurseType.FOCUS_FIELD) && riftAnchor != null -> 6.1f
            spec.hasCurse(CurseType.MOON_GLIDE) -> min(baseLimit, 7.9f)
            spec.hasCurse(CurseType.POWER_HOLD) -> min(baseLimit + 0.4f, 9.35f)
            spec.hasCurse(CurseType.OVERHEAT) -> min(baseLimit + 0.35f, 9.35f)
            ballPower == BallPower.CHROME_RICOCHET -> min(baseLimit + 0.75f, 9.8f)
            ballPower == BallPower.MINOR_RICOCHET -> min(baseLimit + 0.3f, 9.45f)
            else -> baseLimit
        }
        val velocity = body.linearVelocity
        val speedSq = velocity.x * velocity.x + velocity.y * velocity.y
        if (speedSq <= limit * limit) return
        val scale = limit / sqrt(speedSq)
        scratchVelocity.set(velocity.x * scale, velocity.y * scale)
        body.linearVelocity = scratchVelocity
    }

    private fun resolveOutcome(spec: LevelSpec, body: Body, ball: Point2, elapsed: Float): PhysicsOutcome {
        if (ball.distanceTo(spec.goal) <= spec.goalRadius + BALL_RADIUS * 0.85f) {
            return PhysicsOutcome.WON
        }

        val ballHitRadius = when (ballPower) {
            BallPower.VOID_PHASE -> BALL_RADIUS * 0.24f
            BallPower.MINOR_PHASE -> BALL_RADIUS * 0.54f
            else -> BALL_RADIUS * 0.82f
        }
        val hazards = spec.hazards
        for (i in 0 until hazards.size) {
            val hazard = hazards[i]
            val hx = hazard.positionXAt(elapsed)
            val hy = hazard.positionYAt(elapsed)
            val dx = ball.x - hx
            val dy = ball.y - hy
            val hitLimit = hazard.radius + ballHitRadius
            if (dx * dx + dy * dy <= hitLimit * hitLimit) {
                if (prismShieldAvailable) {
                    repelFromHazard(body, hx, hy, hazard.radius)
                    prismShieldAvailable = false
                    powerTriggeredThisStep = true
                    return PhysicsOutcome.RUNNING
                }
                return PhysicsOutcome.LOST
            }
        }

        if (ball.y > spec.stageHeight + 0.95f || ball.x < -0.7f || ball.x > STAGE_WIDTH + 0.7f) {
            return PhysicsOutcome.LOST
        }

        if (elapsed > spec.timeLimitSeconds) {
            return PhysicsOutcome.LOST
        }

        return PhysicsOutcome.RUNNING
    }

    private fun repelFromHazard(body: Body, hazardX: Float, hazardY: Float, hazardRadius: Float) {
        var dx = body.position.x - hazardX
        var dy = body.position.y - hazardY
        var distance = sqrt(dx * dx + dy * dy)
        if (distance < 0.001f) {
            dx = 0f
            dy = -1f
            distance = 1f
        }
        val nx = dx / distance
        val ny = dy / distance
        val safeDistance = hazardRadius + BALL_RADIUS + 0.12f
        scratchTransform.set(hazardX + nx * safeDistance, hazardY + ny * safeDistance)
        body.setTransform(scratchTransform, body.angle)
        scratchVelocity.set(nx * 5.2f, ny * 5.2f - 0.6f)
        body.linearVelocity = scratchVelocity
    }

    companion object {
        const val BALL_RADIUS = 0.35f
    }
}
