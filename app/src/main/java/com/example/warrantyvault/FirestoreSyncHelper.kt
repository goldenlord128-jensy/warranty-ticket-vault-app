package com.example.warrantyvault

import android.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirestoreSyncHelper {

    private val db: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    private val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    // Save or Update item in Firestore
    suspend fun syncItemToCloud(item: VaultItem) {
        val uid = currentUserId ?: return
        try {
            db.collection("users")
                .document(uid)
                .collection("items")
                .document(item.id.toString()) // Convert Int ID to String document ID
                .set(item)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Delete item from Firestore
    suspend fun deleteItemFromCloud(itemId: String) {
        val uid = currentUserId ?: return
        try {
            db.collection("users")
                .document(uid)
                .collection("items")
                .document(itemId.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Fetch all items from Firestore and store into Room
    suspend fun downloadCloudItemsToRoom(dao: ItemDao) {
        val uid = currentUserId ?: return
        try {
            val snapshot = db.collection("users")
                .document(uid)
                .collection("items")
                .get()
                .await()

            val remoteItems = snapshot.toObjects(VaultItem::class.java)
            remoteItems.forEach { item ->
                dao.insertItem(item)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}