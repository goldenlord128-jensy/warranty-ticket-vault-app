package com.example.warrantyvault

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun AddManualItemDialog(
    onDismiss: () -> Unit,
    onSave: (VaultItem) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var modelNumber by remember { mutableStateOf("") }
    var purchaseDate by remember { mutableStateOf("") }
    var warrantyMonths by remember { mutableStateOf("12") }
    var price by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Add Warranty Manually",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Item Title (e.g. Headphones)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand (e.g. Sony)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = modelNumber,
                    onValueChange = { modelNumber = it },
                    label = { Text("Model/Serial No.") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = purchaseDate,
                    onValueChange = { purchaseDate = it },
                    label = { Text("Purchase Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = warrantyMonths,
                    onValueChange = { warrantyMonths = it },
                    label = { Text("Warranty Duration (Months)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (title.isNotBlank() && purchaseDate.isNotBlank()) {
                                onSave(
                                    VaultItem(
                                        title = title,
                                        brand = if (brand.isBlank()) "N/A" else brand,
                                        modelNumber = if (modelNumber.isBlank()) "N/A" else modelNumber,
                                        purchaseDate = purchaseDate,
                                        warrantyMonths = warrantyMonths.toIntOrNull() ?: 12,
                                        price = price.toDoubleOrNull() ?: 0.0,
                                        imagePath = null
                                    )
                                )
                            }
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}