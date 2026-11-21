package com.forgecrew.drillschool

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val DeepSteel = Color(0xFF172D38)
val Orange = Color(0xFFFF981D)
val OrangeDark = Color(0xFFE77700)
val OrangePale = Color(0xFFFFEBD8)
val SchoolBg = Color(0xFFF9FAFA)
val SchoolMuted = Color(0xFF657681)
val SchoolLine = Color(0xFFDDE5E9)
val SafeGreen = Color(0xFF228B67)

@Composable
fun DrillSchoolTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = OrangeDark, onPrimary = Color.White,
            secondary = DeepSteel, background = SchoolBg, onBackground = DeepSteel,
            surface = Color.White, onSurface = DeepSteel,
            surfaceVariant = OrangePale, onSurfaceVariant = SchoolMuted,
            outline = SchoolLine, error = Color(0xFFB83535)
        ),
        content = content
    )
}

@Composable
fun SchoolCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = modifier, shape = RoundedCornerShape(22.dp), color = Color.White,
        border = BorderStroke(1.dp, SchoolLine), shadowElevation = 2.dp) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
fun Eyebrow(label: String) = Text(label.uppercase(), color = OrangeDark,
    fontSize = 11.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.ExtraBold)

@Composable
fun SubText(label: String) = Text(label, color = SchoolMuted,
    style = MaterialTheme.typography.bodySmall)

@Composable
fun SectionHeader(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, fontWeight = FontWeight.ExtraBold, color = DeepSteel,
            style = MaterialTheme.typography.titleLarge)
        if (subtitle != null) SubText(subtitle)
    }
}
