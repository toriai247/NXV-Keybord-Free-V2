package com.example.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object CatThemeIcons {

    /**
     * Cat Backspace Icon matching the user's reference image:
     * Dark 3D cat face with yellow eyes, cat ears, pink/white snout, and whiskers.
     */
    @Composable
    fun CatBackspaceIcon(
        size: Dp = 32.dp,
        modifier: Modifier = Modifier
    ) {
        Canvas(modifier = modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // 1. Dark Cat Ears (Left and Right)
            val leftEar = Path().apply {
                moveTo(w * 0.18f, h * 0.45f)
                lineTo(w * 0.12f, h * 0.12f)
                lineTo(w * 0.40f, h * 0.32f)
                close()
            }
            val rightEar = Path().apply {
                moveTo(w * 0.82f, h * 0.45f)
                lineTo(w * 0.88f, h * 0.12f)
                lineTo(w * 0.60f, h * 0.32f)
                close()
            }
            val earColor = Color(0xFF1E232B)
            drawPath(path = leftEar, color = earColor)
            drawPath(path = rightEar, color = earColor)

            // Inner ear highlights
            val leftInnerEar = Path().apply {
                moveTo(w * 0.20f, h * 0.40f)
                lineTo(w * 0.16f, h * 0.20f)
                lineTo(w * 0.35f, h * 0.32f)
                close()
            }
            val rightInnerEar = Path().apply {
                moveTo(w * 0.80f, h * 0.40f)
                lineTo(w * 0.84f, h * 0.20f)
                lineTo(w * 0.65f, h * 0.32f)
                close()
            }
            drawPath(path = leftInnerEar, color = Color(0xFF4A525D))
            drawPath(path = rightInnerEar, color = Color(0xFF4A525D))

            // 2. Main Cat Head Oval
            val headRect = Rect(
                left = w * 0.12f,
                top = h * 0.28f,
                right = w * 0.88f,
                bottom = h * 0.88f
            )
            val headPath = Path().apply {
                addOval(headRect)
            }
            drawPath(path = headPath, color = earColor)

            // 3. White / Light Cream Snout/Muzzle
            val muzzleRect = Rect(
                left = w * 0.32f,
                top = h * 0.52f,
                right = w * 0.68f,
                bottom = h * 0.82f
            )
            drawOval(color = Color(0xFFF4F5F7), topLeft = Offset(muzzleRect.left, muzzleRect.top), size = Size(muzzleRect.width, muzzleRect.height))

            // 4. Cat Eyes (Vibrant Yellow / Amber with dark pupils & white glint)
            val eyeRadius = w * 0.085f
            val eyeY = h * 0.48f
            val leftEyeX = w * 0.33f
            val rightEyeX = w * 0.67f

            // Yellow eyes
            val eyeColor = Color(0xFFEAB308) // Bright Cat Yellow
            drawCircle(color = eyeColor, radius = eyeRadius, center = Offset(leftEyeX, eyeY))
            drawCircle(color = eyeColor, radius = eyeRadius, center = Offset(rightEyeX, eyeY))

            // Black Pupils
            drawCircle(color = Color(0xFF1E232B), radius = eyeRadius * 0.55f, center = Offset(leftEyeX, eyeY))
            drawCircle(color = Color(0xFF1E232B), radius = eyeRadius * 0.55f, center = Offset(rightEyeX, eyeY))

            // White shine glints
            drawCircle(color = Color.White, radius = eyeRadius * 0.22f, center = Offset(leftEyeX - eyeRadius * 0.25f, eyeY - eyeRadius * 0.25f))
            drawCircle(color = Color.White, radius = eyeRadius * 0.22f, center = Offset(rightEyeX - eyeRadius * 0.25f, eyeY - eyeRadius * 0.25f))

            // 5. Nose & Mouth
            val noseY = h * 0.60f
            val nosePath = Path().apply {
                moveTo(w * 0.50f, noseY)
                lineTo(w * 0.45f, noseY - h * 0.04f)
                lineTo(w * 0.55f, noseY - h * 0.04f)
                close()
            }
            drawPath(path = nosePath, color = Color(0xFF282E37))

            // Mouth Y line
            drawLine(
                color = Color(0xFF282E37),
                start = Offset(w * 0.50f, noseY),
                end = Offset(w * 0.50f, noseY + h * 0.06f),
                strokeWidth = 1.8f
            )

            // 6. Whiskers (3 on left, 3 on right)
            val whiskerColor = Color(0xFFE0E4E9)
            val strokeW = 1.4f

            // Left Whiskers
            drawLine(color = whiskerColor, start = Offset(w * 0.22f, h * 0.58f), end = Offset(w * 0.02f, h * 0.52f), strokeWidth = strokeW)
            drawLine(color = whiskerColor, start = Offset(w * 0.20f, h * 0.65f), end = Offset(w * 0.00f, h * 0.65f), strokeWidth = strokeW)
            drawLine(color = whiskerColor, start = Offset(w * 0.22f, h * 0.72f), end = Offset(w * 0.02f, h * 0.78f), strokeWidth = strokeW)

            // Right Whiskers
            drawLine(color = whiskerColor, start = Offset(w * 0.78f, h * 0.58f), end = Offset(w * 0.98f, h * 0.52f), strokeWidth = strokeW)
            drawLine(color = whiskerColor, start = Offset(w * 0.80f, h * 0.65f), end = Offset(w * 1.00f, h * 0.65f), strokeWidth = strokeW)
            drawLine(color = whiskerColor, start = Offset(w * 0.78f, h * 0.72f), end = Offset(w * 0.98f, h * 0.78f), strokeWidth = strokeW)
        }
    }

    /**
     * Shift Icon with white arrow and amber/yellow accent underline badge
     * matching the Shift key in the reference image.
     */
    @Composable
    fun CatShiftIcon(
        size: Dp = 22.dp,
        isShifted: Boolean = false,
        modifier: Modifier = Modifier
    ) {
        Canvas(modifier = modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // Upward Arrow Path
            val arrowPath = Path().apply {
                moveTo(w * 0.50f, h * 0.15f)
                lineTo(w * 0.20f, h * 0.50f)
                lineTo(w * 0.38f, h * 0.50f)
                lineTo(w * 0.38f, h * 0.70f)
                lineTo(w * 0.62f, h * 0.70f)
                lineTo(w * 0.62f, h * 0.50f)
                lineTo(w * 0.80f, h * 0.50f)
                close()
            }

            drawPath(
                path = arrowPath,
                color = if (isShifted) Color(0xFFFACC15) else Color.White
            )

            // Accent underline bar
            val barY = h * 0.82f
            drawLine(
                color = Color(0xFFE2B040), // Warm golden amber underline
                start = Offset(w * 0.25f, barY),
                end = Offset(w * 0.75f, barY),
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }

    /**
     * Top header brand badge showing "JIANG-YUNXI" and cute cat avatar
     */
    @Composable
    fun CatBrandToolbarBadge(
        modifier: Modifier = Modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.padding(end = 6.dp)
        ) {
            Text(
                text = "JIANG-YUNXI",
                color = Color(0xFF9EA7B3),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Box(
                modifier = Modifier
                    .padding(start = 6.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF4F5F7)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "A",
                    color = Color(0xFF282E37),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

/**
     * 3D Slate Cat character popup preview when key is pressed
     */
@Composable
fun Cat3dPopupCharacter(
    char: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width = 48.dp, height = 54.dp)
            .shadow(10.dp, RoundedCornerShape(12.dp), spotColor = Color(0x66000000))
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF282E37))
            .border(1.5.dp, Color(0xFF3E4652), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Subtle top ears decoration on popup
        Canvas(modifier = Modifier.size(48.dp, 54.dp)) {
            val w = size.width
            val earL = Path().apply {
                moveTo(w * 0.15f, 0f)
                lineTo(w * 0.02f, -12f)
                lineTo(w * 0.32f, 0f)
                close()
            }
            val earR = Path().apply {
                moveTo(w * 0.85f, 0f)
                lineTo(w * 0.98f, -12f)
                lineTo(w * 0.68f, 0f)
                close()
            }
            drawPath(earL, Color(0xFF282E37))
            drawPath(earR, Color(0xFF282E37))
        }

        Text(
            text = char,
            color = Color(0xFFF4F5F7),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
