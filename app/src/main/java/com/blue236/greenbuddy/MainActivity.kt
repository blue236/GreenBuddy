package com.blue236.greenbuddy

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.blue236.greenbuddy.model.Tab
import com.blue236.greenbuddy.notifications.reminderDestinationTabOrNull
import com.blue236.greenbuddy.ui.GreenBuddyApp
import com.blue236.greenbuddy.ui.theme.GreenBuddyTheme

class MainActivity : AppCompatActivity() {
    private var initialTab by mutableStateOf(Tab.HOME)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialTab = intent.reminderDestinationTabOrNull() ?: Tab.HOME
        enableEdgeToEdge()
        setContent {
            GreenBuddyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    GreenBuddyApp(initialTab = initialTab)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        initialTab = intent.reminderDestinationTabOrNull() ?: Tab.HOME
    }

}
