package com.cbhard.pulseapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.cbhard.pulse.pulseTrace
import com.cbhard.pulseapp.ui.theme.PulseAppTheme

class RecompositionTestActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var badState by remember { mutableStateOf(0) }
            PulseAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .pulseTrace("MainActivity_Root_Column"), // 1. Attach our profiler
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Recomposition Count: $badState")

                        Button(onClick = {
                            badState++ // Normal increment
                        }) {
                            Text("Normal Button")
                        }

                        // 2. THE TRAP: We read a state, and immediately write to it during the composition phase.
                        // This will cause Compose to instantly re-evaluate this block infinitely.
                        if (badState > 5) {
                            badState++
                        }
                    }
                }
            }
        }
    }
}