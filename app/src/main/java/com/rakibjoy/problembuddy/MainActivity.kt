package com.rakibjoy.problembuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProblemBuddyTheme {
                ProblemBuddyNavHost()
            }
        }
    }
}
