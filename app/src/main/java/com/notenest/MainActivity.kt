package com.notenest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notenest.ui.navigation.NoteNestNavHost
import com.notenest.ui.theme.NoteNestTheme
import com.notenest.viewmodel.NoteViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: NoteViewModel by viewModels {
        NoteViewModel.provideFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val darkModePref by viewModel.isDarkMode.collectAsStateWithLifecycle()
            val isDark = darkModePref ?: isSystemInDarkTheme()

            NoteNestTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NoteNestNavHost(viewModel = viewModel)
                }
            }
        }
    }
}
