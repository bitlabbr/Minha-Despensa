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

package com.bitlabbr.minhadespensa.uisystem.features.catalog.widgets.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.core.domain.model.ProductCategory
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import com.bitlabbr.minhadespensa.core.domain.util.normalizeForSearch
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaPrimaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaSecondaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.dialog.MinhaDespensaDialog
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.components.domain.catalog.ProductTextField
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import minhadespensa.uisystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun ManageCategoriesDialog(
    categories: List<ProductCategory>,
    onCreateCategory: (String) -> Unit,
    onRenameCategory: (categoryId: String, newName: String) -> Unit,
    onDeleteCategory: (categoryId: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = getAppColors()
    val dimens = MinhaDespensaTheme.dimens
    val typography = MinhaDespensaTheme.typography

    var newCategoryName by remember { mutableStateOf("") }
    var categoryToRename by remember { mutableStateOf<ProductCategory?>(null) }
    var categoryToDelete by remember { mutableStateOf<ProductCategory?>(null) }

    val defaultProtectedNorm = remember {
        CoreConstants.Product.DEFAULT_CATEGORY.normalizeForSearch()
    }

    MinhaDespensaDialog(
        onDismissRequest = onDismiss,
        title = stringResource(Res.string.category_manage_dialog_title),
        description = stringResource(Res.string.category_manage_dialog_desc),
        buttons = {
            MinhaDespensaSecondaryButton(
                text = stringResource(Res.string.category_manage_close_button),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
        ) {
            ProductTextField(
                value = newCategoryName,
                onValueChange = { newCategoryName = it.take(CoreConstants.Product.CATEGORY_MAX_LENGTH) },
                label = stringResource(Res.string.category_manage_new_input_label),
                placeholder = stringResource(Res.string.category_manage_new_input_placeholder),
                maxCharacters = CoreConstants.Product.CATEGORY_MAX_LENGTH,
                modifier = Modifier.fillMaxWidth(),
            )

            MinhaDespensaPrimaryButton(
                text = stringResource(Res.string.category_manage_add_button),
                leadingIcon = Icons.Rounded.Add,
                enabled = newCategoryName.trim().isNotEmpty(),
                onClick = {
                    val trimmed = newCategoryName.trim()
                    if (trimmed.isNotEmpty()) {
                        onCreateCategory(trimmed)
                        newCategoryName = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = colors.onSurface.copy(alpha = 0.1f),
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(categories, key = { it.id }) { category ->
                    val isProtected = category.name.normalizeForSearch() == defaultProtectedNorm

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surfaceContainerHigh.copy(alpha = 0.45f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        MinhaDespensaText(
                            text = category.name,
                            fontStyle = typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = colors.onSurface,
                            modifier = Modifier.weight(1f),
                        )

                        if (isProtected) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colors.onSurface.copy(alpha = 0.08f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = null,
                                    tint = colors.onSurfaceVariant,
                                    modifier = Modifier.size(12.dp),
                                )
                                MinhaDespensaText(
                                    text = stringResource(Res.string.category_manage_protected_badge),
                                    fontStyle = typography.bodySmall,
                                    color = colors.onSurfaceVariant,
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                IconButton(
                                    onClick = { categoryToRename = category },
                                    modifier = Modifier.size(32.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = stringResource(Res.string.category_manage_edit_action),
                                        tint = colors.primary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                                IconButton(
                                    onClick = { categoryToDelete = category },
                                    modifier = Modifier.size(32.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = stringResource(Res.string.category_manage_delete_action),
                                        tint = colors.error,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    categoryToRename?.let { target ->
        RenameCategoryDialog(
            category = target,
            onConfirm = { updatedName ->
                onRenameCategory(target.id, updatedName)
                categoryToRename = null
            },
            onDismiss = { categoryToRename = null },
        )
    }

    categoryToDelete?.let { target ->
        DeleteCategoryConfirmDialog(
            category = target,
            onConfirm = {
                onDeleteCategory(target.id)
                categoryToDelete = null
            },
            onDismiss = { categoryToDelete = null },
        )
    }
}

@Composable
private fun RenameCategoryDialog(
    category: ProductCategory,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val dimens = MinhaDespensaTheme.dimens
    var editedName by remember(category.id) { mutableStateOf(category.name) }

    MinhaDespensaDialog(
        onDismissRequest = onDismiss,
        title = stringResource(Res.string.category_manage_edit_dialog_title),
        description = stringResource(Res.string.category_manage_edit_dialog_desc, category.name),
        buttons = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
            ) {
                MinhaDespensaSecondaryButton(
                    text = stringResource(Res.string.action_cancel),
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                MinhaDespensaPrimaryButton(
                    text = stringResource(Res.string.category_manage_edit_save_button),
                    enabled = editedName.trim().isNotEmpty() && editedName.trim() != category.name,
                    onClick = { onConfirm(editedName.trim()) },
                    modifier = Modifier.weight(1f),
                )
            }
        },
    ) {
        ProductTextField(
            value = editedName,
            onValueChange = { editedName = it.take(CoreConstants.Product.CATEGORY_MAX_LENGTH) },
            label = stringResource(Res.string.category_manage_edit_input_label),
            placeholder = stringResource(Res.string.category_manage_new_input_placeholder),
            maxCharacters = CoreConstants.Product.CATEGORY_MAX_LENGTH,
            isRequired = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun DeleteCategoryConfirmDialog(
    category: ProductCategory,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val dimens = MinhaDespensaTheme.dimens

    MinhaDespensaDialog(
        onDismissRequest = onDismiss,
        title = stringResource(Res.string.category_manage_delete_dialog_title),
        description = stringResource(Res.string.category_manage_delete_dialog_desc, category.name),
        buttons = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
            ) {
                MinhaDespensaSecondaryButton(
                    text = stringResource(Res.string.action_cancel),
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                MinhaDespensaPrimaryButton(
                    text = stringResource(Res.string.category_manage_delete_confirm_button),
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                )
            }
        },
    ) {}
}

@Composable
fun QuickCreateCategoryDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val dimens = MinhaDespensaTheme.dimens
    var categoryName by remember { mutableStateOf("") }

    MinhaDespensaDialog(
        onDismissRequest = onDismiss,
        title = stringResource(Res.string.category_create_dialog_title),
        description = stringResource(Res.string.category_create_dialog_desc),
        buttons = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
            ) {
                MinhaDespensaSecondaryButton(
                    text = stringResource(Res.string.action_cancel),
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                MinhaDespensaPrimaryButton(
                    text = stringResource(Res.string.category_create_save_button),
                    enabled = categoryName.trim().isNotEmpty(),
                    onClick = { onConfirm(categoryName.trim()) },
                    modifier = Modifier.weight(1f),
                )
            }
        },
    ) {
        ProductTextField(
            value = categoryName,
            onValueChange = { categoryName = it.take(CoreConstants.Product.CATEGORY_MAX_LENGTH) },
            label = stringResource(Res.string.category_manage_edit_input_label),
            placeholder = stringResource(Res.string.category_manage_new_input_placeholder),
            maxCharacters = CoreConstants.Product.CATEGORY_MAX_LENGTH,
            isRequired = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
