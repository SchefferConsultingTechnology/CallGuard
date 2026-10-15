package com.lsp.callguard.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lsp.callguard.R

private const val WORDMARK_ACCENT = "Guard"

/**
 * Full logo (spec 006): symbol, "Call" + "Guard" in the primary colour, translated tagline.
 * Real text instead of an image, so it follows the theme, the language and screen readers.
 */
@Composable
fun BrandLogo(
    modifier: Modifier = Modifier,
    symbolSize: Dp = 88.dp
) {
    val name = stringResource(R.string.app_name)
    val tagline = stringResource(R.string.brand_tagline)
    val accentStart = name.lastIndexOf(WORDMARK_ACCENT).takeIf { it > 0 }

    Column(
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = "$name. $tagline" },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.ic_brand_symbol),
            contentDescription = null,
            modifier = Modifier.size(symbolSize)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = buildAnnotatedString {
                if (accentStart == null) {
                    append(name)
                } else {
                    append(name.substring(0, accentStart))
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append(name.substring(accentStart))
                    }
                }
            },
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = tagline.toUpperCase(Locale.current),
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
