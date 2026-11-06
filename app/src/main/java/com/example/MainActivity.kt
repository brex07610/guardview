package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.GuardViewRepository
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.MainFeatureScreen
import com.example.ui.theme.GuardViewTheme
import com.example.ui.viewmodel.GuardViewViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GuardViewTheme(darkTheme = true) {
                GuardViewApp()
            }
        }
    }
}

@Composable
fun GuardViewApp() {
    val context = LocalContext.current.applicationContext
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var isPreviewSession by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null && !isPreviewSession) {
        AuthScreen(
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
                isPreviewSession = true
            }
        )
    } else {
        val activeUid = currentUser?.uid ?: "preview_user_demo"
        val databaseId = remember { context.getString(R.string.firestore_database_id) }
        val db = remember(databaseId) { FirebaseFirestore.getInstance(databaseId) }
        val repository = remember(db) { GuardViewRepository(db) }

        val viewModel: GuardViewViewModel = viewModel(
            key = activeUid,
            factory = object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GuardViewViewModel(repository, activeUid) as T
                }
            }
        )

        MainFeatureScreen(
            viewModel = viewModel,
            onSignOut = {
                Firebase.auth.signOut()
                isPreviewSession = false
                currentUser = null
            }
        )
    }
}
