package com.moonsolstudios.kavvoro.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Wide neon CTA matching play_cta_reference.webp across all screen sizes.
 *
 * The caller owns the surrounding screen/background. The component keeps the
 * reference's 2048:559 ratio and scales all artwork from the actual canvas.
 */
@Composable
fun PlayCtaButton(
    modifier: Modifier = Modifier,
    playText: String = "PLAY",
    subText: String = "CHOOSE YOUR MODE",
    onClick: () -> Unit = {}
) {
    val cyan = Color(0xFF00EFFF)
    val cyanHot = Color(0xFFB8FDFF)
    val blue = Color(0xFF438DFF)
    val violet = Color(0xFF735DFF)
    val pink = Color(0xFFFF00B8)
    val pinkHot = Color(0xFFFFB2EF)

    BoxWithConstraints(
        modifier = modifier
            .widthIn(max = 820.dp)
            .fillMaxWidth()
            .aspectRatio(2048f / 559f)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) {
                contentDescription = "$playText. $subText"
            },
        contentAlignment = Alignment.Center
    ) {
        val textScale = (maxWidth.value / 820f).coerceIn(0.60f, 1f)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val neonBrush = Brush.horizontalGradient(
                colors = listOf(cyan, cyan, blue, violet, pink, pink),
                startX = 0f,
                endX = w
            )
            val hotNeonBrush = Brush.horizontalGradient(
                colors = listOf(cyanHot, Color.White, Color(0xFFDCE8FF), Color.White, pinkHot),
                startX = 0f,
                endX = w
            )
            val panelBrush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF041526),
                    Color(0xFF020A16),
                    Color(0xFF080318),
                    Color(0xFF180318)
                ),
                startX = 0f,
                endX = w
            )

            fun framePath(
                left: Float,
                top: Float,
                right: Float,
                bottom: Float,
                cut: Float
            ): Path = Path().apply {
                moveTo(left + cut, top)
                lineTo(right - cut, top)
                lineTo(right, top + cut)
                lineTo(right, bottom - cut)
                lineTo(right - cut, bottom)
                lineTo(left + cut, bottom)
                lineTo(left, bottom - cut)
                lineTo(left, top + cut)
                close()
            }

            fun hexCellPath(
                left: Float,
                top: Float,
                right: Float,
                bottom: Float
            ): Path {
                val midY = (top + bottom) / 2f
                val cutX = (right - left) * 0.22f
                return Path().apply {
                    moveTo(left + cutX, top)
                    lineTo(right - cutX, top)
                    lineTo(right, midY)
                    lineTo(right - cutX, bottom)
                    lineTo(left + cutX, bottom)
                    lineTo(left, midY)
                    close()
                }
            }

            fun drawRailSegment(
                startX: Float,
                endX: Float,
                isTop: Boolean,
                accent: Color
            ) {
                val railY = if (isTop) h * 0.026f else h * 0.974f
                val depth = h * 0.052f
                val slant = h * 0.050f
                val rail = Path().apply {
                    if (isTop) {
                        moveTo(startX + slant, railY)
                        lineTo(endX - slant, railY)
                        lineTo(endX, railY + depth)
                        lineTo(startX, railY + depth)
                    } else {
                        moveTo(startX, railY - depth)
                        lineTo(endX, railY - depth)
                        lineTo(endX - slant, railY)
                        lineTo(startX + slant, railY)
                    }
                    close()
                }

                drawPath(
                    path = rail,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF24435E),
                            Color(0xFF0A1726),
                            Color(0xFF1A334B)
                        )
                    )
                )
                drawPath(
                    path = rail,
                    color = accent.copy(alpha = 0.52f),
                    style = Stroke(width = h * 0.006f, join = StrokeJoin.Bevel)
                )
                drawLine(
                    color = accent.copy(alpha = 0.25f),
                    start = if (isTop) Offset(startX + slant, railY + h * 0.010f)
                    else Offset(startX + slant, railY - h * 0.010f),
                    end = if (isTop) Offset(endX - slant, railY + h * 0.010f)
                    else Offset(endX - slant, railY - h * 0.010f),
                    strokeWidth = h * 0.003f
                )
            }

            // Outer chassis.
            val outerPath = framePath(
                left = h * 0.022f,
                top = h * 0.022f,
                right = w - h * 0.022f,
                bottom = h - h * 0.022f,
                cut = h * 0.185f
            )
            drawPath(
                path = outerPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1D3B57),
                        Color(0xFF091523),
                        Color(0xFF050D17),
                        Color(0xFF18334C)
                    )
                )
            )
            drawPath(
                path = outerPath,
                brush = neonBrush,
                alpha = 0.20f,
                style = Stroke(width = h * 0.105f, join = StrokeJoin.Bevel)
            )
            drawPath(
                path = outerPath,
                brush = neonBrush,
                alpha = 0.40f,
                style = Stroke(width = h * 0.046f, join = StrokeJoin.Bevel)
            )
            drawPath(
                path = outerPath,
                brush = neonBrush,
                alpha = 0.95f,
                style = Stroke(width = h * 0.012f, join = StrokeJoin.Bevel)
            )

            // Side armor fins make the silhouette match the reference instead
            // of leaving the chassis as a plain octagon.
            fun drawSideFin(isLeft: Boolean, accent: Color) {
                val x = if (isLeft) 0f else w
                val innerX = if (isLeft) w * 0.070f else w * 0.930f
                val direction = if (isLeft) 1f else -1f
                val fin = Path().apply {
                    moveTo(x + direction * w * 0.010f, h * 0.385f)
                    lineTo(x + direction * w * 0.042f, h * 0.300f)
                    lineTo(innerX, h * 0.350f)
                    lineTo(innerX, h * 0.650f)
                    lineTo(x + direction * w * 0.042f, h * 0.700f)
                    lineTo(x + direction * w * 0.010f, h * 0.615f)
                    close()
                }
                drawPath(
                    path = fin,
                    brush = Brush.horizontalGradient(
                        colors = if (isLeft) {
                            listOf(Color(0xFF0C2034), Color(0xFF244A67))
                        } else {
                            listOf(Color(0xFF4A133E), Color(0xFF180D25))
                        },
                        startX = if (isLeft) 0f else w,
                        endX = if (isLeft) w else 0f
                    )
                )
                drawPath(
                    path = fin,
                    color = accent.copy(alpha = 0.65f),
                    style = Stroke(width = h * 0.008f, join = StrokeJoin.Bevel)
                )
            }

            drawSideFin(isLeft = true, accent = cyan)
            drawSideFin(isLeft = false, accent = pink)

            // Segmented top and bottom rails.
            val rails = listOf(
                Triple(0.085f, 0.245f, cyan),
                Triple(0.255f, 0.410f, blue),
                Triple(0.420f, 0.595f, violet),
                Triple(0.605f, 0.765f, pink),
                Triple(0.775f, 0.915f, pink)
            )
            rails.forEach { (start, end, accent) ->
                drawRailSegment(w * start, w * end, isTop = true, accent = accent)
                drawRailSegment(w * start, w * end, isTop = false, accent = accent)
            }

            // Inner luminous panel.
            val innerPath = framePath(
                left = h * 0.135f,
                top = h * 0.135f,
                right = w - h * 0.135f,
                bottom = h - h * 0.135f,
                cut = h * 0.135f
            )
            drawPath(path = innerPath, brush = panelBrush)
            drawPath(
                path = innerPath,
                brush = neonBrush,
                alpha = 0.16f,
                style = Stroke(width = h * 0.100f, join = StrokeJoin.Bevel)
            )
            drawPath(
                path = innerPath,
                brush = neonBrush,
                alpha = 0.34f,
                style = Stroke(width = h * 0.045f, join = StrokeJoin.Bevel)
            )
            drawPath(
                path = innerPath,
                brush = neonBrush,
                alpha = 0.82f,
                style = Stroke(width = h * 0.020f, join = StrokeJoin.Bevel)
            )
            drawPath(
                path = innerPath,
                brush = hotNeonBrush,
                alpha = 0.98f,
                style = Stroke(width = h * 0.0065f, join = StrokeJoin.Bevel)
            )

            // Faint tech lines in the panel.
            drawLine(
                color = cyan.copy(alpha = 0.12f),
                start = Offset(w * 0.105f, h * 0.255f),
                end = Offset(w * 0.310f, h * 0.255f),
                strokeWidth = h * 0.0025f
            )
            drawLine(
                color = pink.copy(alpha = 0.11f),
                start = Offset(w * 0.690f, h * 0.745f),
                end = Offset(w * 0.905f, h * 0.745f),
                strokeWidth = h * 0.0025f
            )

            // Left honeycomb module.
            val moduleCenter = Offset(w * 0.190f, h * 0.500f)
            drawCircle(
                color = cyan.copy(alpha = 0.045f),
                radius = h * 0.355f,
                center = moduleCenter
            )
            val cellWidth = h * 0.068f
            val cellHeight = h * 0.070f
            val startX = w * 0.112f
            val startY = h * 0.260f
            val gapX = h * 0.016f
            val gapY = h * 0.014f
            repeat(6) { column ->
                repeat(5) { row ->
                    val left = startX + column * (cellWidth + gapX)
                    val top = startY + row * (cellHeight + gapY)
                    val right = left + cellWidth
                    val bottom = top + cellHeight
                    if (right < w * 0.315f && bottom < h * 0.745f) {
                        val cell = hexCellPath(left, top, right, bottom)
                        drawPath(
                            path = cell,
                            color = cyan.copy(alpha = 0.18f),
                            style = Stroke(width = h * 0.0035f, join = StrokeJoin.Bevel)
                        )
                        drawLine(
                            color = cyan.copy(alpha = 0.08f),
                            start = Offset(left + h * 0.011f, (top + bottom) / 2f),
                            end = Offset(right - h * 0.011f, (top + bottom) / 2f),
                            strokeWidth = h * 0.002f
                        )
                    }
                }
            }

            // Right target pod.
            val podCenter = Offset(w * 0.812f, h * 0.500f)
            val podRadius = h * 0.175f
            drawCircle(
                color = pink.copy(alpha = 0.035f),
                radius = podRadius * 2.10f,
                center = podCenter
            )
            drawCircle(
                color = pink.copy(alpha = 0.065f),
                radius = podRadius * 1.62f,
                center = podCenter
            )
            drawLine(
                color = pink.copy(alpha = 0.13f),
                start = Offset(podCenter.x - podRadius * 2.05f, podCenter.y),
                end = Offset(podCenter.x + podRadius * 2.05f, podCenter.y),
                strokeWidth = h * 0.0025f
            )
            drawLine(
                color = pink.copy(alpha = 0.10f),
                start = Offset(podCenter.x, podCenter.y - podRadius * 1.80f),
                end = Offset(podCenter.x, podCenter.y + podRadius * 1.80f),
                strokeWidth = h * 0.0025f
            )
            drawCircle(
                color = pink.copy(alpha = 0.35f),
                radius = podRadius * 1.52f,
                center = podCenter,
                style = Stroke(
                    width = h * 0.005f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(h * 0.018f, h * 0.022f), 0f)
                )
            )
            drawCircle(
                color = pink.copy(alpha = 0.20f),
                radius = podRadius * 1.30f,
                center = podCenter,
                style = Stroke(width = h * 0.005f)
            )
            drawCircle(
                color = pink.copy(alpha = 0.17f),
                radius = podRadius,
                center = podCenter,
                style = Stroke(width = h * 0.085f)
            )
            drawCircle(
                color = pink.copy(alpha = 0.50f),
                radius = podRadius,
                center = podCenter,
                style = Stroke(width = h * 0.024f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF3D0A39), Color(0xFF17041D), Color(0xFF05020A)),
                    center = podCenter,
                    radius = podRadius
                ),
                radius = podRadius * 0.86f,
                center = podCenter
            )
            drawCircle(
                color = pinkHot.copy(alpha = 0.95f),
                radius = podRadius * 0.73f,
                center = podCenter,
                style = Stroke(width = h * 0.009f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.10f),
                radius = podRadius * 0.56f,
                center = podCenter,
                style = Stroke(width = h * 0.0035f)
            )

            // White chevron with a soft neon under-stroke.
            val arrowSize = h * 0.105f
            val chevron = Path().apply {
                moveTo(podCenter.x - arrowSize * 0.48f, podCenter.y - arrowSize)
                lineTo(podCenter.x + arrowSize * 0.42f, podCenter.y)
                lineTo(podCenter.x - arrowSize * 0.48f, podCenter.y + arrowSize)
            }
            drawPath(
                path = chevron,
                color = pink.copy(alpha = 0.36f),
                style = Stroke(width = h * 0.075f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            drawPath(
                path = chevron,
                color = Color.White.copy(alpha = 0.20f),
                style = Stroke(width = h * 0.043f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            drawPath(
                path = chevron,
                color = Color.White,
                style = Stroke(width = h * 0.022f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        Column(
            modifier = Modifier
                .padding(end = (32f * textScale).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy((3f * textScale).dp)
        ) {
            Text(
                text = playText,
                color = Color.White,
                fontSize = (90f * textScale).sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (1.0f * textScale).sp,
                maxLines = 1
            )
            Text(
                text = subText,
                color = Color(0xFFF3F7FF),
                fontSize = (18f * textScale).sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (3.8f * textScale).sp,
                maxLines = 1
            )
        }
    }
}

// ---------------- PREVIEWS FOR ALL SCREEN SIZES ----------------

@Preview(name = "Phone Portrait (Compact)", showBackground = true, backgroundColor = 0xFF020B16, widthDp = 360, heightDp = 140)
@Composable
fun PlayCtaButtonPhonePreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020B16))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        PlayCtaButton(modifier = Modifier.fillMaxWidth())
    }
}

@Preview(name = "Large Phone / Medium", showBackground = true, backgroundColor = 0xFF020B16, widthDp = 430, heightDp = 160)
@Composable
fun PlayCtaButtonMediumPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020B16))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        PlayCtaButton(modifier = Modifier.fillMaxWidth())
    }
}

@Preview(name = "Tablet / Foldable (Wide)", showBackground = true, backgroundColor = 0xFF020B16, widthDp = 840, heightDp = 240)
@Composable
fun PlayCtaButtonTabletPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020B16))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        PlayCtaButton(modifier = Modifier.fillMaxWidth())
    }
}
