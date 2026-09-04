package com.example.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Custom vector artwork for Strawberry Dessert theme special keys:
 * - Strawberry fruit (Shift key)
 * - Strawberry Smoothie / Frappe Cup (Backspace key)
 * - Ice Cream Sundae (Symbol/123/Option key)
 * - Strawberry Shortcake Slice (Enter key)
 */
object StrawberryThemeIcons {

    @Composable
    fun StrawberryShiftIcon(
        modifier: Modifier = Modifier,
        size: Dp = 26.dp,
        isShifted: Boolean = false
    ) {
        Canvas(modifier = modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // Strawberry Body
            val berryPath = Path().apply {
                moveTo(w * 0.5f, h * 0.95f) // bottom tip
                cubicTo(
                    w * 0.15f, h * 0.70f,
                    w * 0.05f, h * 0.40f,
                    w * 0.20f, h * 0.28f
                )
                cubicTo(
                    w * 0.35f, h * 0.18f,
                    w * 0.65f, h * 0.18f,
                    w * 0.80f, h * 0.28f
                )
                cubicTo(
                    w * 0.95f, h * 0.40f,
                    w * 0.85f, h * 0.70f,
                    w * 0.5f, h * 0.95f
                )
                close()
            }

            // Fill Strawberry Body
            drawPath(
                path = berryPath,
                color = if (isShifted) Color(0xFFE5395A) else Color(0xFFE85D75)
            )

            // Dark Strawberry Outline
            drawPath(
                path = berryPath,
                color = Color(0xFF6B1220),
                style = Stroke(width = 2.2f)
            )

            // Seeds
            val seedColor = Color(0xFF6B1220)
            drawCircle(color = seedColor, radius = 1.3f, center = Offset(w * 0.38f, h * 0.45f))
            drawCircle(color = seedColor, radius = 1.3f, center = Offset(w * 0.62f, h * 0.45f))
            drawCircle(color = seedColor, radius = 1.3f, center = Offset(w * 0.50f, h * 0.60f))
            drawCircle(color = seedColor, radius = 1.3f, center = Offset(w * 0.35f, h * 0.70f))
            drawCircle(color = seedColor, radius = 1.3f, center = Offset(w * 0.65f, h * 0.70f))

            // Green Crown / Leaves
            val leafPath = Path().apply {
                moveTo(w * 0.5f, h * 0.20f)
                lineTo(w * 0.30f, h * 0.12f)
                lineTo(w * 0.42f, h * 0.24f)
                lineTo(w * 0.50f, h * 0.08f)
                lineTo(w * 0.58f, h * 0.24f)
                lineTo(w * 0.70f, h * 0.12f)
                lineTo(w * 0.50f, h * 0.20f)
                close()
            }
            drawPath(path = leafPath, color = Color(0xFF4CAF50))
            drawPath(path = leafPath, color = Color(0xFF2E7D32), style = Stroke(width = 1.5f))
        }
    }

    @Composable
    fun FrappeBackspaceIcon(
        modifier: Modifier = Modifier,
        size: Dp = 26.dp
    ) {
        Canvas(modifier = modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // Straw
            val strawPath = Path().apply {
                moveTo(w * 0.38f, h * 0.10f)
                lineTo(w * 0.48f, h * 0.35f)
            }
            drawPath(path = strawPath, color = Color(0xFF6B1220), style = Stroke(width = 2.8f))

            // Whipped Cream Top (cloud-like arcs)
            val creamPath = Path().apply {
                moveTo(w * 0.26f, h * 0.40f)
                cubicTo(w * 0.20f, h * 0.26f, w * 0.45f, h * 0.22f, w * 0.50f, h * 0.26f)
                cubicTo(w * 0.55f, h * 0.22f, w * 0.80f, h * 0.26f, w * 0.74f, h * 0.40f)
                close()
            }
            drawPath(path = creamPath, color = Color(0xFFFFF7F8))
            drawPath(path = creamPath, color = Color(0xFF6B1220), style = Stroke(width = 2.0f))

            // Cup Body (tapered cup)
            val cupPath = Path().apply {
                moveTo(w * 0.26f, h * 0.40f)
                lineTo(w * 0.74f, h * 0.40f)
                lineTo(w * 0.68f, h * 0.88f)
                cubicTo(w * 0.68f, h * 0.92f, w * 0.32f, h * 0.92f, w * 0.32f, h * 0.88f)
                close()
            }

            // Red juice fill
            drawPath(path = cupPath, color = Color(0xFFE5395A))
            // Cup outline
            drawPath(path = cupPath, color = Color(0xFF6B1220), style = Stroke(width = 2.2f))

            // Drink highlight line
            drawLine(
                color = Color(0xFFFFB3C1),
                start = Offset(w * 0.36f, h * 0.48f),
                end = Offset(w * 0.38f, h * 0.82f),
                strokeWidth = 2f
            )
        }
    }

    @Composable
    fun IceCreamIcon(
        modifier: Modifier = Modifier,
        size: Dp = 26.dp
    ) {
        Canvas(modifier = modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // Spoon sticking out
            val spoonPath = Path().apply {
                moveTo(w * 0.68f, h * 0.15f)
                lineTo(w * 0.56f, h * 0.40f)
            }
            drawPath(path = spoonPath, color = Color(0xFF6B1220), style = Stroke(width = 2.8f))

            // Ice cream scoop (dome)
            val scoopPath = Path().apply {
                moveTo(w * 0.22f, h * 0.52f)
                cubicTo(w * 0.20f, h * 0.26f, w * 0.80f, h * 0.26f, w * 0.78f, h * 0.52f)
                close()
            }
            drawPath(path = scoopPath, color = Color(0xFFFF8DA1))
            drawPath(path = scoopPath, color = Color(0xFF6B1220), style = Stroke(width = 2.2f))

            // Cup Base (horizontal dessert bowl)
            val bowlPath = Path().apply {
                moveTo(w * 0.18f, h * 0.52f)
                lineTo(w * 0.82f, h * 0.52f)
                lineTo(w * 0.72f, h * 0.88f)
                lineTo(w * 0.28f, h * 0.88f)
                close()
            }
            drawPath(path = bowlPath, color = Color(0xFFFFF7F8))
            drawPath(path = bowlPath, color = Color(0xFF6B1220), style = Stroke(width = 2.2f))

            // Cherry on top left
            drawCircle(color = Color(0xFFD81B60), radius = 2.8f, center = Offset(w * 0.38f, h * 0.35f))
        }
    }

    @Composable
    fun ShortcakeEnterIcon(
        modifier: Modifier = Modifier,
        size: Dp = 26.dp
    ) {
        Canvas(modifier = modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // Cake Slice Wedge
            val cakePath = Path().apply {
                moveTo(w * 0.15f, h * 0.78f) // bottom left
                lineTo(w * 0.85f, h * 0.62f) // bottom right
                lineTo(w * 0.85f, h * 0.38f) // top right tip
                lineTo(w * 0.15f, h * 0.54f) // top left
                close()
            }

            // Cake sponge body (cream white)
            drawPath(path = cakePath, color = Color(0xFFFFF7F8))
            drawPath(path = cakePath, color = Color(0xFF6B1220), style = Stroke(width = 2.2f))

            // Red strawberry jam/cream layer lines
            drawLine(
                color = Color(0xFFE5395A),
                start = Offset(w * 0.15f, h * 0.66f),
                end = Offset(w * 0.85f, h * 0.50f),
                strokeWidth = 2.2f
            )

            // Strawberry topper on top of cake
            val topBerryPath = Path().apply {
                moveTo(w * 0.72f, h * 0.38f)
                lineTo(w * 0.80f, h * 0.22f)
                lineTo(w * 0.88f, h * 0.38f)
                close()
            }
            drawPath(path = topBerryPath, color = Color(0xFFE5395A))
            drawPath(path = topBerryPath, color = Color(0xFF6B1220), style = Stroke(width = 1.8f))
        }
    }
}

/**
 * Puppy Character Popup Preview for Puppy Pop theme (Reference Image 1):
 * - Adorable white Puppy/Kitty head with pointy ears & pink interior
 * - Cute eyes, nose, and open mouth with red/pink tongue
 * - Displays the active character in vibrant blue font!
 */
@Composable
fun PuppyPopupCharacter(
    char: String,
    modifier: Modifier = Modifier,
    width: Dp = 54.dp,
    height: Dp = 68.dp
) {
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .shadow(6.dp, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 8.dp, bottomEnd = 8.dp), spotColor = Color(0x40000000))
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
            .background(Color.White)
            .border(
                1.dp,
                Color(0x22000000),
                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        // Character Face Canvas (Ears, Eyes, Nose, Mouth/Tongue)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Left Ear
            val leftEar = Path().apply {
                moveTo(w * 0.12f, h * 0.18f)
                lineTo(w * 0.24f, h * 0.02f)
                lineTo(w * 0.36f, h * 0.18f)
                close()
            }
            drawPath(path = leftEar, color = Color.White)
            drawPath(path = leftEar, color = Color(0xFFD3D8DF), style = Stroke(width = 1.5f))

            // Left Ear Pink Interior
            val leftEarInner = Path().apply {
                moveTo(w * 0.16f, h * 0.16f)
                lineTo(w * 0.24f, h * 0.06f)
                lineTo(w * 0.32f, h * 0.16f)
                close()
            }
            drawPath(path = leftEarInner, color = Color(0xFFFF8DA1))

            // Right Ear
            val rightEar = Path().apply {
                moveTo(w * 0.64f, h * 0.18f)
                lineTo(w * 0.76f, h * 0.02f)
                lineTo(w * 0.88f, h * 0.18f)
                close()
            }
            drawPath(path = rightEar, color = Color.White)
            drawPath(path = rightEar, color = Color(0xFFD3D8DF), style = Stroke(width = 1.5f))

            // Right Ear Pink Interior
            val rightEarInner = Path().apply {
                moveTo(w * 0.68f, h * 0.16f)
                lineTo(w * 0.76f, h * 0.06f)
                lineTo(w * 0.84f, h * 0.16f)
                close()
            }
            drawPath(path = rightEarInner, color = Color(0xFFFF8DA1))

            // Eyes (small cute black dots)
            drawCircle(color = Color(0xFF1E242B), radius = 2.2f, center = Offset(w * 0.28f, h * 0.25f))
            drawCircle(color = Color(0xFF1E242B), radius = 2.2f, center = Offset(w * 0.72f, h * 0.25f))

            // Tongue sticking out at bottom (playful puppy expression!)
            val tonguePath = Path().apply {
                moveTo(w * 0.42f, h * 0.82f)
                cubicTo(w * 0.42f, h * 0.98f, w * 0.58f, h * 0.98f, w * 0.58f, h * 0.82f)
                close()
            }
            drawPath(path = tonguePath, color = Color(0xFFE53935))
            drawPath(path = tonguePath, color = Color(0xFFB71C1C), style = Stroke(width = 1.2f))
        }

        // Center Large Active Character (Vibrant Blue!)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, bottom = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = char,
                color = Color(0xFF1E88E5),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

object KittyThemeIcons {

    @Composable
    fun CutePawIcon(
        modifier: Modifier = Modifier,
        size: Dp = 24.dp
    ) {
        Canvas(modifier = modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // Background / Outer border of the paw (White filled with Black outline)
            val outerPawPath = Path().apply {
                moveTo(w * 0.5f, h * 0.92f)
                cubicTo(w * 0.12f, h * 0.92f, w * 0.10f, h * 0.40f, w * 0.32f, h * 0.34f)
                cubicTo(w * 0.42f, h * 0.31f, w * 0.58f, h * 0.31f, w * 0.68f, h * 0.34f)
                cubicTo(w * 0.90f, h * 0.40f, w * 0.88f, h * 0.92f, w * 0.5f, h * 0.92f)
                close()
            }
            drawPath(path = outerPawPath, color = Color.White)
            drawPath(path = outerPawPath, color = Color.Black, style = Stroke(width = 2.2f))

            // Center pink main pad
            val centerPad = Path().apply {
                moveTo(w * 0.5f, h * 0.54f)
                cubicTo(w * 0.32f, h * 0.54f, w * 0.28f, h * 0.84f, w * 0.5f, h * 0.86f)
                cubicTo(w * 0.72f, h * 0.84f, w * 0.68f, h * 0.54f, w * 0.5f, h * 0.54f)
                close()
            }
            drawPath(path = centerPad, color = Color(0xFFFFB2BC))
            drawPath(path = centerPad, color = Color.Black, style = Stroke(width = 1.6f))

            // 4 outer small toe pads (filled with pink, black outline)
            val toeRadius = w * 0.08f
            val toes = listOf(
                Offset(w * 0.28f, h * 0.46f),
                Offset(w * 0.40f, h * 0.32f),
                Offset(w * 0.60f, h * 0.32f),
                Offset(w * 0.72f, h * 0.46f)
            )
            toes.forEach { center ->
                drawCircle(color = Color(0xFFFFB2BC), radius = toeRadius, center = center)
                drawCircle(color = Color.Black, radius = toeRadius, center = center, style = Stroke(width = 1.6f))
            }
        }
    }

    @Composable
    fun KittenFaceIcon(
        modifier: Modifier = Modifier,
        size: Dp = 24.dp
    ) {
        Canvas(modifier = modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // Outer Head Shape (horizontal oval)
            val headPath = Path().apply {
                addOval(Rect(w * 0.15f, h * 0.30f, w * 0.85f, h * 0.80f))
            }

            // Left Ear
            val leftEar = Path().apply {
                moveTo(w * 0.22f, h * 0.38f)
                lineTo(w * 0.18f, h * 0.12f)
                lineTo(w * 0.38f, h * 0.32f)
                close()
            }

            // Right Ear
            val rightEar = Path().apply {
                moveTo(w * 0.62f, h * 0.32f)
                lineTo(w * 0.82f, h * 0.12f)
                lineTo(w * 0.78f, h * 0.38f)
                close()
            }

            // Draw Ears (Fill with White, then outline with Black)
            drawPath(path = leftEar, color = Color.White)
            drawPath(path = leftEar, color = Color.Black, style = Stroke(width = 2.2f))

            // Pink inner ears
            val leftEarInner = Path().apply {
                moveTo(w * 0.24f, h * 0.34f)
                lineTo(w * 0.20f, h * 0.18f)
                lineTo(w * 0.34f, h * 0.30f)
                close()
            }
            drawPath(path = leftEarInner, color = Color(0xFFFFB2BC))

            drawPath(path = rightEar, color = Color.White)
            drawPath(path = rightEar, color = Color.Black, style = Stroke(width = 2.2f))

            val rightEarInner = Path().apply {
                moveTo(w * 0.66f, h * 0.30f)
                lineTo(w * 0.80f, h * 0.18f)
                lineTo(w * 0.76f, h * 0.34f)
                close()
            }
            drawPath(path = rightEarInner, color = Color(0xFFFFB2BC))

            // Fill Head with White, then outline with Black
            drawPath(path = headPath, color = Color.White)
            drawPath(path = headPath, color = Color.Black, style = Stroke(width = 2.2f))

            // Eyes (two tiny black circles)
            drawCircle(color = Color.Black, radius = 2.2f, center = Offset(w * 0.35f, h * 0.55f))
            drawCircle(color = Color.Black, radius = 2.2f, center = Offset(w * 0.65f, h * 0.55f))

            // Nose (tiny pink dot)
            drawCircle(color = Color(0xFFFFB2BC), radius = 1.5f, center = Offset(w * 0.50f, h * 0.62f))

            // Whiskers (two lines on left, two on right)
            // Left whiskers
            drawLine(color = Color.Black, start = Offset(w * 0.22f, h * 0.58f), end = Offset(w * 0.08f, h * 0.56f), strokeWidth = 1.6f)
            drawLine(color = Color.Black, start = Offset(w * 0.22f, h * 0.62f), end = Offset(w * 0.08f, h * 0.64f), strokeWidth = 1.6f)
            // Right whiskers
            drawLine(color = Color.Black, start = Offset(w * 0.78f, h * 0.58f), end = Offset(w * 0.92f, h * 0.56f), strokeWidth = 1.6f)
            drawLine(color = Color.Black, start = Offset(w * 0.78f, h * 0.62f), end = Offset(w * 0.92f, h * 0.64f), strokeWidth = 1.6f)

            // Bow on the right ear (H Kitty Style!)
            // Left loop
            val bowLeft = Path().apply {
                addOval(Rect(w * 0.64f, h * 0.24f, w * 0.74f, h * 0.34f))
            }
            // Right loop
            val bowRight = Path().apply {
                addOval(Rect(w * 0.78f, h * 0.24f, w * 0.88f, h * 0.34f))
            }
            drawPath(path = bowLeft, color = Color(0xFFFFB2BC))
            drawPath(path = bowLeft, color = Color.Black, style = Stroke(width = 1.6f))
            drawPath(path = bowRight, color = Color(0xFFFFB2BC))
            drawPath(path = bowRight, color = Color.Black, style = Stroke(width = 1.6f))

            // Center knot
            drawCircle(color = Color(0xFFFFB2BC), radius = 2.8f, center = Offset(w * 0.76f, h * 0.29f))
            drawCircle(color = Color.Black, radius = 2.8f, center = Offset(w * 0.76f, h * 0.29f), style = Stroke(width = 1.6f))
        }
    }

    @Composable
    fun KittyShiftIcon(
        modifier: Modifier = Modifier,
        size: Dp = 24.dp,
        isShifted: Boolean = false
    ) {
        Canvas(modifier = modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // A cute bow or arrow pointing up
            val arrowPath = Path().apply {
                moveTo(w * 0.5f, h * 0.15f)
                lineTo(w * 0.15f, h * 0.55f)
                lineTo(w * 0.38f, h * 0.55f)
                lineTo(w * 0.38f, h * 0.85f)
                lineTo(w * 0.62f, h * 0.85f)
                lineTo(w * 0.62f, h * 0.55f)
                lineTo(w * 0.85f, h * 0.55f)
                close()
            }
            
            drawPath(path = arrowPath, color = if (isShifted) Color(0xFFFFB2BC) else Color.White)
            drawPath(path = arrowPath, color = Color.Black, style = Stroke(width = 2.2f))
        }
    }

    @Composable
    fun KittyBackspaceIcon(
        modifier: Modifier = Modifier,
        size: Dp = 24.dp
    ) {
        Canvas(modifier = modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val tagPath = Path().apply {
                moveTo(w * 0.15f, h * 0.5f)
                lineTo(w * 0.40f, h * 0.20f)
                lineTo(w * 0.85f, h * 0.20f)
                lineTo(w * 0.85f, h * 0.80f)
                lineTo(w * 0.40f, h * 0.80f)
                close()
            }
            drawPath(path = tagPath, color = Color.White)
            drawPath(path = tagPath, color = Color.Black, style = Stroke(width = 2.2f))

            // Pink 'X' inside
            drawLine(
                color = Color(0xFFFFB2BC),
                start = Offset(w * 0.50f, h * 0.38f),
                end = Offset(w * 0.72f, h * 0.62f),
                strokeWidth = 2.2f
            )
            drawLine(
                color = Color(0xFFFFB2BC),
                start = Offset(w * 0.72f, h * 0.38f),
                end = Offset(w * 0.50f, h * 0.62f),
                strokeWidth = 2.2f
            )
            // Black thin outline on 'X'
            drawLine(
                color = Color.Black,
                start = Offset(w * 0.50f, h * 0.38f),
                end = Offset(w * 0.72f, h * 0.62f),
                strokeWidth = 1.0f
            )
            drawLine(
                color = Color.Black,
                start = Offset(w * 0.72f, h * 0.38f),
                end = Offset(w * 0.50f, h * 0.62f),
                strokeWidth = 1.0f
            )
        }
    }
}
