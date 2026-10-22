package io.bifri.interview.cmp.padel.feature.match.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val SetColumnWidth: Dp = 35.dp

/**
 * Renders a set score inside a fixed-width cell to ensure consistent horizontal alignment across match rows.
 *
 * @param score The raw set score string (e.g., `"6"`, `"6(5)"`, or `null`).
 * @param fontWeight The font weight used to display the score (e.g., bold for winning teams).
 * @param color The text color for the score.
 * @param modifier Optional [Modifier] for configuring the container layout.
 */
@Composable
fun SetScoreCell(
    score: String?,
    fontWeight: FontWeight,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.width(SetColumnWidth),
        contentAlignment = Alignment.Center,
    ) {
        if (!score.isNullOrBlank()) {
            Text(
                text = formatSetScore(score),
                fontWeight = fontWeight,
                color = color,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

/**
 * Formats a raw set score string into an [AnnotatedString].
 *
 * Standard scores like `"6"` or `"4"` are returned as plain text. Scores containing tiebreak notation
 * like `"6(5)"` are formatted such that the main games count (`"6"`) is rendered as standard text
 * and the tiebreak points (`"(5)"`) are styled as superscript with a smaller font size (`11.sp`),
 * following professional tennis/padel scoreboard conventions.
 *
 * @param score The raw score string to format (e.g., `"6"`, `"6(5)"`, or `null`).
 * @return An [AnnotatedString] representation of the set score.
 */
internal fun formatSetScore(score: String?): AnnotatedString {
    if (score.isNullOrBlank()) return AnnotatedString("")
    val regex = Regex("""^(\d+)\((\d+)\)$""")
    val match = regex.matchEntire(score.trim())
    return if (match != null) {
        val games = match.groupValues[1]
        val tiebreak = match.groupValues[2]
        buildAnnotatedString {
            append(games)
            withStyle(
                SpanStyle(
                    fontSize = 11.sp,
                    baselineShift = BaselineShift.Superscript,
                ),
            ) {
                append("($tiebreak)")
            }
        }
    } else {
        AnnotatedString(score)
    }
}
