/*
 *   Copyright (c) 2026 Willian Santos
 *
 *   This work is licensed under the Creative Commons
 *   Attribution-NonCommercial 4.0 International License (CC BY-NC 4.0).
 *
 *   You are free to:
 *     - Share  — copy and redistribute the material in any medium or format
 *     - Adapt  — remix, transform, and build upon the material
 *
 *   Under the following terms:
 *     - Attribution    — You must give appropriate credit, provide a link to
 *                        the license, and indicate if changes were made.
 *     - NonCommercial  — You may not use the material for commercial purposes.
 *
 *   Owner rights:
 *     - Willian Santos retains all commercial rights.
 *    - The copyright holder may use, sell, sublicense, or relicense this
 *       work under different terms at any time.
 *
 *   Full license: https://creativecommons.org/licenses/by-nc/4.0/legalcode
 */

package com.bitlabbr.minhadespensa.uisystem.components.core.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import minhadespensa.uisystem.generated.resources.Res
import minhadespensa.uisystem.generated.resources.back
import minhadespensa.uisystem.generated.resources.no_itens_found
import minhadespensa.uisystem.generated.resources.search
import org.jetbrains.compose.resources.stringResource

@Composable
fun MinhaDespensaSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    searchResults: List<String>,
    onResultClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    supportingContent: (@Composable (String) -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
) {
    val appColors = getAppColors()
    val appTypography = MinhaDespensaTheme.typography
    val appDimens = MinhaDespensaTheme.dimens
    val isDark = isSystemInDarkTheme()

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val shape = RoundedCornerShape(appDimens.cardCorner * 0.55f)

    Column(modifier = modifier.fillMaxWidth()) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .border(
                    width = 1.dp,
                    color = if (isDark) Color.White.copy(alpha = 0.15f) else appColors.onSecondaryContainer.copy(alpha = 0.20f),
                    shape = shape,
                ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearch(query)
                    keyboardController?.hide()
                    focusManager.clearFocus()
                },
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = appColors.surface.copy(alpha = 0.60f),
                unfocusedContainerColor = appColors.surface.copy(alpha = 0.40f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = appColors.primary,
                focusedTextColor = appColors.onSurface,
                unfocusedTextColor = appColors.onSurface,
            ),
            textStyle = appTypography.bodyLarge,
            singleLine = true,
            placeholder = {
                MinhaDespensaText(
                    text = placeholder,
                    color = appColors.onSurface.copy(alpha = 0.45f),
                    fontStyle = appTypography.bodySmall,
                )
            },
            leadingIcon = leadingContent ?: {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(Res.string.search),
                    tint = appColors.primary,
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(Res.string.back),
                            tint = appColors.onSurface.copy(alpha = 0.7f),
                        )
                    }
                }
            },
        )

        AnimatedVisibility(visible = query.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp)
                    .padding(top = 8.dp),
            ) {
                if (searchResults.isEmpty()) {
                    item {
                        MinhaDespensaText(
                            text = stringResource(Res.string.no_itens_found),
                            color = appColors.onSurface.copy(alpha = 0.5f),
                            fontStyle = appTypography.bodySmall,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                } else {
                    items(searchResults) { resultText ->
                        ListItem(
                            headlineContent = {
                                MinhaDespensaText(
                                    text = resultText,
                                    fontStyle = appTypography.bodySmall,
                                    color = appColors.onSurface,
                                )
                            },
                            supportingContent = supportingContent?.let { { it(resultText) } },
                            leadingContent = leadingContent,
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onResultClick(resultText)
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                },
                        )
                    }
                }
            }
        }
    }
}