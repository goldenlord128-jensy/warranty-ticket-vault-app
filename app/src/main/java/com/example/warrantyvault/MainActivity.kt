package com.example.warrantyvault

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.work.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.filled.Delete
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.TimeUnit

class MainActivity : androidx.fragment.app.FragmentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Permission result handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request Notification Permission on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Schedule Periodic Warranty Check Worker
        scheduleWarrantyWorker()

        // Initialize Google Play Billing
        BillingHelper.initialize(this)

        // Show lock screen/biometric prompt first
        showBiometricPrompt {
            // Renders UI only after authentication succeeds
            setContent {
                MaterialTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        VaultAppScreen()
                    }
                }
            }
        }
    }

    private fun scheduleWarrantyWorker() {
        // Enqueue a OneTimeWorkRequest so it runs immediately for testing
        val oneTimeWork = OneTimeWorkRequestBuilder<WarrantyNotificationWorker>().build()
        WorkManager.getInstance(applicationContext).enqueue(oneTimeWork)

        val workRequest = PeriodicWorkRequest.Builder(
            WarrantyNotificationWorker::class.java,
            24, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "WarrantyExpirationCheck",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun showBiometricPrompt(onSuccess: () -> Unit) {
        val executor = androidx.core.content.ContextCompat.getMainExecutor(this)
        val biometricPrompt = androidx.biometric.BiometricPrompt(
            this,
            executor,
            object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    finish() // Exit app if user cancels authentication
                }
            }
        )

        val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Warranty Vault")
            .setSubtitle("Use Biometrics or Screen Lock to continue")
            .setAllowedAuthenticators(
                androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}

// Helper to convert Uri to Bitmap for ReceiptParser
fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Set to true only when building the GitHub demo APK, false for your full unlimited version
private const val IS_DEMO_VERSION = true

@Composable
fun VaultAppScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = VaultDatabase.getDatabase(context)
    val dao = db.itemDao()

    val itemList by dao.getAllItems().collectAsState(initial = emptyList())

    var selectedFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showDemoLimitDialog by remember { mutableStateOf(false) }
    var selectedImageForView by remember { mutableStateOf<String?>(null) }

    val filteredItems = when (selectedFilter) {
        "Warranties & Insurance" -> itemList.filter { it.category == "Warranty Receipt" || it.category == "Insurance" }
        "Tickets" -> itemList.filter { it.category.contains("Ticket") || it.category == "Event Ticket" }
        else -> itemList
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = if (IS_DEMO_VERSION) "Warranty & Ticket Vault (Demo)" else "Warranty & Ticket Vault",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Filter Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterButton(text = "All", selected = selectedFilter == "All") { selectedFilter = "All" }
            FilterButton(text = "Warranties", selected = selectedFilter == "Warranties & Insurance") { selectedFilter = "Warranties & Insurance" }
            FilterButton(text = "Tickets", selected = selectedFilter == "Tickets") { selectedFilter = "Tickets" }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Add Item Button
        Button(
            onClick = {
                if (IS_DEMO_VERSION && itemList.size >= 1) {
                    showDemoLimitDialog = true
                } else {
                    showAddDialog = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                when {
                    !IS_DEMO_VERSION -> "+ Add New Item / Ticket"
                    itemList.isEmpty() -> "+ Add Demo Item (1/1 Free Trial)"
                    else -> "+ Add New Item (Demo Limit Reached)"
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Receipt Items List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(items = filteredItems) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.title.ifBlank { "Untitled Item" }, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            if (item.category.isNotBlank()) {
                                Text(text = "Type: ${item.category}", fontSize = 12.sp, color = Color(0xFF1976D2), fontWeight = FontWeight.SemiBold)
                            }
                            if (item.brand.isNotBlank()) {
                                Text(text = "Brand: ${item.brand}", fontSize = 12.sp, color = Color.Gray)
                            }
                            if (item.purchaseDate.isNotBlank()) {
                                Text(text = "Date: ${item.purchaseDate}", fontSize = 12.sp)
                            }
                            if (item.passengerCount > 1) {
                                Text(text = "Passengers: ${item.passengerCount}", fontSize = 12.sp)
                            }
                            if (item.eventTime.isNotBlank()) {
                                Text(text = "Time: ${item.eventTime}", fontSize = 12.sp)
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // View Image Button
                            item.imagePath?.let { path ->
                                Button(onClick = { selectedImageForView = path }) {
                                    Text("View")
                                }
                            }

                            // Delete Item Button
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        dao.deleteItem(item)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                                    contentDescription = "Delete Item",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Demo Limit Dialog
    if (showDemoLimitDialog) {
        AlertDialog(
            onDismissRequest = { showDemoLimitDialog = false },
            title = { Text("Demo Version Limit") },
            text = { Text("This GitHub demo version allows saving 1 item to test features like biometric unlock, OCR scanning, and background alerts.\n\nGet the full unlimited version on Gumroad to unlock unlimited items and full features!") },
            confirmButton = {
                TextButton(onClick = { showDemoLimitDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // Add Warranty Dialog
    if (showAddDialog) {
        AddWarrantyDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newItem, localImageUri ->
                scope.launch {
                    val localPath = localImageUri?.let { uri ->
                        StorageHelper.saveImageLocally(context, uri)
                    }
                    val updatedItem = newItem.copy(imagePath = localPath ?: newItem.imagePath)

                    dao.insertItem(updatedItem)
                    FirestoreSyncHelper.syncItemToCloud(updatedItem)
                }
                showAddDialog = false
            }
        )
    }

    // Fullscreen Interactive Receipt Zoom Viewer Dialog
    selectedImageForView?.let { imagePath ->
        ReceiptViewerDialog(
            imageUrl = imagePath,
            onDismiss = { selectedImageForView = null }
        )
    }
}

@Composable
fun AddWarrantyDialog(
    onDismiss: () -> Unit,
    onSave: (VaultItem, Uri?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var modelNumber by remember { mutableStateOf("") }
    var purchaseDate by remember { mutableStateOf("") }
    var warrantyMonths by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Warranty Receipt") }
    var passengerCount by remember { mutableStateOf(1) }
    var eventTime by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    var isSaving by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        if (uri != null) {
            isScanning = true
            scope.launch {
                val bitmap = loadBitmapFromUri(context, uri)
                if (bitmap != null) {
                    try {
                        val parser = ReceiptParser()
                        val parsedItem: VaultItem = parser.parseReceipt(bitmap)

                        if (parsedItem.title.isNotBlank() && parsedItem.title != "Scanned Warranty") {
                            title = parsedItem.title
                        }
                        if (parsedItem.brand.isNotBlank()) {
                            brand = parsedItem.brand
                        }
                        if (parsedItem.purchaseDate.isNotBlank()) {
                            purchaseDate = parsedItem.purchaseDate
                        }
                        if (parsedItem.warrantyMonths > 0) {
                            warrantyMonths = parsedItem.warrantyMonths.toString()
                        }
                        if (parsedItem.price > 0.0) {
                            price = parsedItem.price.toString()
                        }
                        category = parsedItem.category
                        passengerCount = parsedItem.passengerCount
                        eventTime = parsedItem.eventTime
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                isScanning = false
            }
        }
    }

    Dialog(onDismissRequest = { if (!isSaving && !isScanning) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f) // Restricts dialog height so it fits on any screen
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Add Item or Ticket",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Scrollable Form Container
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Item / Ticket Name") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving && !isScanning
                    )

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category / Type") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving && !isScanning
                    )

                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Brand / Operator") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving && !isScanning
                    )

                    OutlinedTextField(
                        value = purchaseDate,
                        onValueChange = { purchaseDate = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving && !isScanning
                    )

                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price / Fare") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving && !isScanning
                    )

                    if (category.contains("Ticket")) {
                        OutlinedTextField(
                            value = passengerCount.toString(),
                            onValueChange = { passengerCount = it.toIntOrNull() ?: 1 },
                            label = { Text("Passenger Count") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSaving && !isScanning
                        )
                        OutlinedTextField(
                            value = eventTime,
                            onValueChange = { eventTime = it },
                            label = { Text("Departure / Event Time") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSaving && !isScanning
                        )
                    } else {
                        OutlinedTextField(
                            value = warrantyMonths,
                            onValueChange = { warrantyMonths = it },
                            label = { Text("Warranty Duration (Months)") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSaving && !isScanning
                        )
                    }

                    // Always accessible file attachment button inside scroll body
                    Button(
                        onClick = { launcher.launch("image/*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        enabled = !isSaving && !isScanning
                    ) {
                        Text(
                            when {
                                isScanning -> "Scanning Document..."
                                imageUri != null -> "Change Document Image"
                                else -> "Attach Receipt / Ticket Image"
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Fixed Action Bar (Never scrolls away)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSaving || isScanning) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    TextButton(
                        onClick = onDismiss,
                        enabled = !isSaving && !isScanning
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        enabled = !isSaving && !isScanning,
                        onClick = {
                            val finalTitle = title.ifBlank { "Imported Item" }
                            val finalBrand = brand.trim()
                            val finalDate = purchaseDate.trim()
                            val finalMonths = warrantyMonths.toIntOrNull() ?: 0

                            isSaving = true

                            val item = VaultItem(
                                title = finalTitle,
                                brand = finalBrand,
                                modelNumber = modelNumber.trim(),
                                purchaseDate = finalDate,
                                warrantyMonths = finalMonths,
                                price = price.toDoubleOrNull() ?: 0.0,
                                imagePath = null,
                                category = category,
                                passengerCount = passengerCount,
                                eventTime = eventTime.trim()
                            )

                            onSave(item, imageUri)
                        }
                    ) {
                        Text(if (isSaving) "Saving..." else "Save")
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptViewerDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Receipt Image",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    if (scale > 1f) {
                        TextButton(onClick = {
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        }) {
                            Text("Reset Zoom")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 4f)
                                if (scale > 1f) {
                                    offsetX += pan.x
                                    offsetY += pan.y
                                } else {
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = File(imageUrl),
                        contentDescription = "Full-size Receipt Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offsetX,
                                translationY = offsetY
                            ),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun FilterButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.height(36.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
    ) {
        Text(text, fontSize = 12.sp)
    }
}
