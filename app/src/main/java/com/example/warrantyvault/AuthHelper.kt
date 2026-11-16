package com.example.warrantyvault

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object AuthHelper {
    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun isLoggedIn(): Boolean = currentUser != null

    fun signOut() {
        auth.signOut()
    }
}