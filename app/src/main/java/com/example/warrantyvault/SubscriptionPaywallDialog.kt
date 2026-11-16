package com.example.warrantyvault

import android.app.Activity
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun SubscriptionPaywallDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val products by BillingHelper.productDetailsList.collectAsState()
    var selectedPlanIndex by remember { mutableIntStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Upgrade to Vault Premium",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Unlimited receipt scans, cloud backup & priority alerts",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Plan 1: 1 Month (₹99)
                PlanOptionCard(
                    title = "1 Month Plan",
                    priceText = "₹99 / month",
                    subtitle = "Standard monthly access",
                    isSelected = selectedPlanIndex == 0,
                    onClick = { selectedPlanIndex = 0 }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Plan 2: 3 Months (₹250 first 3 months, then ₹299)
                PlanOptionCard(
                    title = "3 Month Plan",
                    priceText = "₹250 for first 3 months",
                    subtitle = "Then ₹299 every 3 months",
                    isSelected = selectedPlanIndex == 1,
                    onClick = { selectedPlanIndex = 1 }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Plan 3: 1 Year (₹1100 / year)
                PlanOptionCard(
                    title = "1 Year Plan (Best Value)",
                    priceText = "₹1,100 / year",
                    subtitle = "Saves you over 20% yearly",
                    isSelected = selectedPlanIndex == 2,
                    onClick = { selectedPlanIndex = 2 }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        activity?.let {
                            if (products.isNotEmpty() && selectedPlanIndex < products.size) {
                                BillingHelper.launchPurchaseFlow(it, products[selectedPlanIndex])
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Subscribe Now", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                TextButton(onClick = onDismiss) {
                    Text("Maybe Later", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun PlanOptionCard(
    title: String,
    priceText: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = priceText, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                Text(text = subtitle, fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}