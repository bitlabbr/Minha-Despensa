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

package com.bitlabbr.minhadespensa.uisystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import minhadespensa.uisystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopBar(
    modifier: Modifier = Modifier,
    backgroundColor: Color = getAppColors().primaryContainer,
    contentColor: Color = getAppColors().onPrimaryContainer,
    leftContent: @Composable (() -> Unit)? = null,
    centerContent: @Composable () -> Unit,
    rightContent: @Composable (() -> Unit)? = null
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = backgroundColor,
            titleContentColor = contentColor,
            navigationIconContentColor = contentColor,
            actionIconContentColor = contentColor
        ),
        title = {
            centerContent()
        },
        navigationIcon = {
            if (leftContent != null) {
                leftContent()
            }
        },
        actions = {
            if (rightContent != null) {
                rightContent()
            }
        }
    )
}

data class BottomNavItem<T : Any>(
    val title: String,
    val icon: ImageVector,
    val route: T
)

@Composable
fun CustomIconButton(
    iconPainter: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.size(50.dp),
    backgroundColor: Color = getAppColors().onBackground,
    iconTint: Color = Color.White,
    shape: Shape = RectangleShape
) {
    Box(
        modifier = modifier
            .background(color = backgroundColor, shape = shape)
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = iconPainter,
            contentDescription = contentDescription,
            colorFilter = ColorFilter.tint(iconTint),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun CustomizableSearchBar(
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

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(appDimens.cardCorner))
                .border(
                    width = 2.dp,
                    color = Color.White.copy(alpha = if (isDark) 0.1f else 0.5f),
                    shape = RoundedCornerShape(appDimens.cardCorner)
                ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearch(query)
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.1f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = appColors.primary,
                focusedTextColor = appColors.onSurface,
                unfocusedTextColor = appColors.onSurface
            ),
            textStyle = appTypography.displayMedium,
            singleLine = true,
            placeholder = {
                CustomText(
                    text = placeholder,
                    color = appColors.onSurface.copy(alpha = 0.5f),
                    fontStyle = appTypography.priceLabel
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = stringResource(Res.string.search),
                    tint = appColors.primary
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(Res.string.back),
                            tint = appColors.onSurface
                        )
                    }
                }
            }
        )

        AnimatedVisibility(visible = query.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .padding(top = 8.dp)
            ) {
                if (searchResults.isEmpty()) {
                    item {
                        CustomText(
                            text = stringResource(Res.string.no_itens_found),
                            color = appColors.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(count = searchResults.size) { index ->
                        val resultText = searchResults[index]
                        ListItem(
                            headlineContent = {
                                CustomText(text = resultText, color = appColors.onSurface)
                            },
                            supportingContent = supportingContent?.let { { it(resultText) } },
                            leadingContent = leadingContent,
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier
                                .clickable {
                                    onResultClick(resultText)
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                }
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ImagePickerCard(
    imageBytes: ByteArray? = null,
    onClick: () -> Unit,
    onClearImage: (() -> Unit)? = null
) {
    val colors = getAppColors()
    val typography = MinhaDespensaTheme.typography
    val dimens = MinhaDespensaTheme.dimens

    val imageBitmap = remember(imageBytes) {
        imageBytes?.let { bytes ->
            runCatching {
                bytes.decodeToImageBitmap()
            }.getOrNull()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .border(
                width = 1.dp,
                color = Color.Gray.copy(alpha = 0.6f),
                shape = RoundedCornerShape(dimens.cardCorner / 2)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (imageBitmap != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = stringResource(Res.string.image_picker_picture_desc),
                    modifier = Modifier.size(180.dp)
                        .clip(RoundedCornerShape(dimens.cardCorner)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.size(dimens.paddingSmall))
                CustomText(
                    text = stringResource(Res.string.image_preview_desc),
                    color = colors.onSecondaryContainer.copy(alpha = .72f),
                    fontStyle = typography.bodySmall,
                    fontWeight = FontWeight.Light,
                    alignment = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.size(dimens.paddingMedium))
            }
            if (onClearImage != null) {
                IconButton(
                    onClick = onClearImage,
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(Res.string.image_picker_remove_picture),
                        tint = Color.White
                    )
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
            ) {
                Spacer(modifier = Modifier.height(dimens.paddingLarge))
                Icon(
                    imageVector = Icons.Rounded.CameraAlt,
                    contentDescription = stringResource(Res.string.image_picker_icon_desc),
                    tint = colors.onSecondaryContainer.copy(alpha = .72f),
                    modifier = Modifier.size(38.dp),
                )

                CustomText(
                    text = stringResource(Res.string.image_picker_desc),
                    color = colors.onSecondaryContainer.copy(alpha = .72f),
                    fontStyle = typography.bodySmall,
                    fontWeight = FontWeight.Light,
                )
                Spacer(modifier = Modifier.height(dimens.paddingSmall))
            }
        }
    }
}