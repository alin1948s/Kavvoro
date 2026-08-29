package com.moonsolstudios.kavvoro.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ================================================================
// DESIGN TOKENS
// ================================================================

private object TopActionStyle {
    val CardBackground = Color(0xFF070C14)
    val CardBorder = Color(0xFF35404D)
    val IconColor = Color(0xFFF2F4F7)
    val CardSize = 56.dp
    val CornerRadius = 14.dp
    val BorderWidth = 1.dp
    val SettingsIconSize = 29.dp
    val SoundIconSize = 30.dp
}

// ================================================================
// BASE / REUSABLE CARD
// ================================================================

@Composable
fun TopActionCard(
    modifier: Modifier = Modifier,
    cardSize: Dp = TopActionStyle.CardSize,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(TopActionStyle.CornerRadius)

    Box(
        modifier = modifier
            .size(cardSize)
            .background(
                color = TopActionStyle.CardBackground,
                shape = shape
            )
            .border(
                width = TopActionStyle.BorderWidth,
                color = TopActionStyle.CardBorder,
                shape = shape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

// ================================================================
// SETTINGS BUTTON
// ================================================================

@Composable
fun SettingsButton(
    modifier: Modifier = Modifier,
    buttonSize: Dp = TopActionStyle.CardSize,
    iconSize: Dp = TopActionStyle.SettingsIconSize,
    onClick: () -> Unit = {}
) {
    TopActionCard(
        modifier = modifier,
        cardSize = buttonSize,
        onClick = onClick
    ) {
        SettingsIcon(iconSize = iconSize)
    }
}

// ================================================================
// SETTINGS ICON
// ================================================================

@Composable
private fun SettingsIcon(
    iconSize: Dp
) {
    Canvas(modifier = Modifier.size(iconSize)) {
        val w = size.width
        val h = size.height

        val cx = w / 2f
        val cy = h / 2f

        val outerRadius = size.minDimension * 0.44f
        val rootRadius = outerRadius * 0.73f
        val shoulderRadius = outerRadius * 0.86f

        val gearPath = createSettingsGearPath(
            centerX = cx,
            centerY = cy,
            rootRadius = rootRadius,
            shoulderRadius = shoulderRadius,
            outerRadius = outerRadius,
            teeth = 8
        )

        val strokeWidth = 2.dp.toPx()

        // Gear outline
        drawPath(
            path = gearPath,
            color = TopActionStyle.IconColor,
            style = Stroke(
                width = strokeWidth,
                join = StrokeJoin.Round,
                cap = StrokeCap.Round
            )
        )

        // Center
        drawCircle(
            color = TopActionStyle.IconColor,
            radius = outerRadius * 0.27f,
            center = Offset(cx, cy),
            style = Stroke(width = strokeWidth)
        )
    }
}

// ================================================================
// SETTINGS GEAR PATH
// ================================================================

private fun createSettingsGearPath(
    centerX: Float,
    centerY: Float,
    rootRadius: Float,
    shoulderRadius: Float,
    outerRadius: Float,
    teeth: Int
): Path {
    val path = Path()
    val sector = (2.0 * PI) / teeth
    var firstPoint = true

    for (tooth in 0 until teeth) {
        val centerAngle = -PI / 2.0 + tooth * sector

        /*
         * Profil mai clasic si mai putin sci-fi.
         */
        val points = arrayOf(
            -0.50 to rootRadius,
            -0.34 to shoulderRadius,
            -0.21 to outerRadius,
            0.21 to outerRadius,
            0.34 to shoulderRadius,
            0.50 to rootRadius
        )

        for ((angleFactor, radius) in points) {
            val angle = centerAngle + sector * angleFactor
            val x = centerX + cos(angle).toFloat() * radius
            val y = centerY + sin(angle).toFloat() * radius

            if (firstPoint) {
                path.moveTo(x, y)
                firstPoint = false
            } else {
                path.lineTo(x, y)
            }
        }
    }

    path.close()
    return path
}

// ================================================================
// SOUND BUTTON
// ================================================================

@Composable
fun SoundButton(
    isSoundOn: Boolean,
    modifier: Modifier = Modifier,
    buttonSize: Dp = TopActionStyle.CardSize,
    iconSize: Dp = TopActionStyle.SoundIconSize,
    onClick: () -> Unit = {}
) {
    TopActionCard(
        modifier = modifier,
        cardSize = buttonSize,
        onClick = onClick
    ) {
        SoundIcon(
            isSoundOn = isSoundOn,
            iconSize = iconSize
        )
    }
}

// ================================================================
// SOUND ICON
// ================================================================

@Composable
private fun SoundIcon(
    isSoundOn: Boolean,
    iconSize: Dp
) {
    Canvas(modifier = Modifier.size(iconSize)) {
        val w = size.width
        val h = size.height

        val iconColor = TopActionStyle.IconColor
        val strokeWidth = 2.dp.toPx()

        // ========================================================
        // SPEAKER BODY
        // ========================================================

        val speakerPath = Path().apply {
            moveTo(w * 0.10f, h * 0.39f)
            lineTo(w * 0.28f, h * 0.39f)
            lineTo(w * 0.49f, h * 0.21f)
            lineTo(w * 0.49f, h * 0.79f)
            lineTo(w * 0.28f, h * 0.61f)
            lineTo(w * 0.10f, h * 0.61f)
            close()
        }

        drawPath(
            path = speakerPath,
            color = iconColor,
            style = Stroke(
                width = strokeWidth,
                join = StrokeJoin.Round,
                cap = StrokeCap.Round
            )
        )

        // ========================================================
        // SOUND ON
        // ========================================================

        if (isSoundOn) {
            // Small wave
            drawArc(
                color = iconColor,
                startAngle = -48f,
                sweepAngle = 96f,
                useCenter = false,
                topLeft = Offset(w * 0.45f, h * 0.32f),
                size = Size(w * 0.27f, h * 0.36f),
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )

            // Large wave
            drawArc(
                color = iconColor,
                startAngle = -48f,
                sweepAngle = 96f,
                useCenter = false,
                topLeft = Offset(w * 0.42f, h * 0.18f),
                size = Size(w * 0.49f, h * 0.64f),
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )
        }

        // ========================================================
        // SOUND OFF
        // ========================================================

        if (!isSoundOn) {
            val muteCenterX = w * 0.76f
            val muteCenterY = h * 0.50f
            val muteRadius = w * 0.105f

            // \
            drawLine(
                color = iconColor,
                start = Offset(muteCenterX - muteRadius, muteCenterY - muteRadius),
                end = Offset(muteCenterX + muteRadius, muteCenterY + muteRadius),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // /
            drawLine(
                color = iconColor,
                start = Offset(muteCenterX + muteRadius, muteCenterY - muteRadius),
                end = Offset(muteCenterX - muteRadius, muteCenterY + muteRadius),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

// ================================================================
// PROFESSIONAL USAGE EXAMPLE
// ================================================================

@Composable
fun GameTopActions(
    modifier: Modifier = Modifier,
    isSoundOn: Boolean,
    onSoundClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SoundButton(
            isSoundOn = isSoundOn,
            onClick = onSoundClick
        )
        SettingsButton(onClick = onSettingsClick)
    }
}

// ================================================================
// PREVIEW / DEMO
// ================================================================

@Preview(
    name = "Sound + Settings",
    showBackground = true,
    backgroundColor = 0xFF020711,
    widthDp = 180,
    heightDp = 100
)
@Composable
private fun TopActionsPreview() {
    var soundOn by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .background(Color(0xFF020711))
            .padding(20.dp)
    ) {
        GameTopActions(
            isSoundOn = soundOn,
            onSoundClick = {
                soundOn = !soundOn
            },
            onSettingsClick = {
                // Preview only
            }
        )
    }
}
