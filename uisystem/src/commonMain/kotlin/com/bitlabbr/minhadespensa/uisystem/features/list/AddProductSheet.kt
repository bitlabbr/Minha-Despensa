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
package com.bitlabbr.minhadespensa.uisystem.features.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductSheet(
    state: ProductFormState,
    sheetState: SheetState,
    onNameChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onUnitChange: (MeasureUnit) -> Unit,
    onSaveClick: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp) // Extra padding for bottom navigation area
        ) {
            Text(
                text = "New Product",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 1. Name Input
            OutlinedTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = { Text("Product Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Quantity and Unit Row
            Row(modifier = Modifier.fillMaxWidth()) {

                // Quantity
                OutlinedTextField(
                    value = state.quantity,
                    onValueChange = onQuantityChange,
                    label = { Text("Qty") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Unit Dropdown (Simple version)
                UnitSelector(
                    selectedUnit = state.unit,
                    onUnitSelected = onUnitChange,
                    modifier = Modifier.weight(1f).align(Alignment.CenterVertically)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                        onSaveClick()
                        onDismiss()
                    },
                    enabled = state.name.isNotBlank() && state.quantity.isNotBlank()
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
fun UnitSelector(
    selectedUnit: MeasureUnit,
    onUnitSelected: (MeasureUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    // Dropdown Box visual simulation
    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedUnit.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Unit") },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Select Unit") },
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            enabled = false // Disable input, but allow click via Box
        )

        // Transparent overlay to capture click
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            MeasureUnit.entries.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit.name) },
                    onClick = {
                        onUnitSelected(unit)
                        expanded = false
                    }
                )
            }
        }
    }
}
