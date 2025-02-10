package com.edgetech.bbscout.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class BillboardCapture(
    val id: String = UUID.randomUUID().toString(),
    val imageFile: File,
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double,
    val longitude: Double,
    val detectedText: String?,
    val brands: List<String>,
    val contacts: List<String>
)

@Composable
fun HistoryScreen(
    captures: List<BillboardCapture>,
    onCaptureClick: (BillboardCapture) -> Unit,
    onDeleteCapture: (BillboardCapture) -> Unit
) {
    var selectedCapture by remember { mutableStateOf<BillboardCapture?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Captured Billboards",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        if (captures.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No billboards captured yet",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(captures, key = { it.id }) { capture ->
                    BillboardCaptureCard(
                        capture = capture,
                        onClick = { onCaptureClick(capture) },
                        onDelete = { onDeleteCapture(capture) }
                    )
                }
            }
        }
    }

    // Detail dialog
    selectedCapture?.let { capture ->
        AlertDialog(
            onDismissRequest = { selectedCapture = null },
            title = { Text("Billboard Details") },
            text = {
                Column {
                    Image(
                        bitmap = remember {
                            capture.imageFile.inputStream().buffered().use {
                                BitmapFactory.decodeStream(it)
                            }.asImageBitmap()
                        },
                        contentDescription = "Billboard Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Location: ${capture.latitude}, ${capture.longitude}")

                    if (!capture.brands.isNullOrEmpty()) {
                        Text("Brands: ${capture.brands.joinToString(", ")}")
                    }

                    if (!capture.contacts.isNullOrEmpty()) {
                        Text("Contacts: ${capture.contacts.joinToString(", ")}")
                    }

                    capture.detectedText?.let { text ->
                        Text("Detected Text: $text")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedCapture = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun BillboardCaptureCard(
    capture: BillboardCapture,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Image(
                bitmap = remember {
                    capture.imageFile.inputStream().buffered().use {
                        BitmapFactory.decodeStream(it)
                    }.asImageBitmap()
                },
                contentDescription = "Billboard Thumbnail",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = dateFormat.format(Date(capture.timestamp)),
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (capture.brands.isNotEmpty()) {
                        Text(
                            text = "Brands: ${capture.brands.joinToString(", ")}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete capture"
                    )
                }
            }
        }
    }
}