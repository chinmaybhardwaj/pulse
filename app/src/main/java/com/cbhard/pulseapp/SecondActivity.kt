package com.cbhard.pulseapp

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cbhard.pulseapp.ui.theme.PulseAppTheme

class SecondActivity : ComponentActivity() {

    // A static list that will hold onto instances of this Activity forever
    companion object {
        val leakedActivities = mutableListOf<Activity>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // 1. Trap the activity in memory!
        leakedActivities.add(this)
        setContent {
            PulseAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("I am trapped in memory forever.")

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(onClick = {
                            // TERRIBLE PRACTICE: Blocking the Main Thread deliberately
                            Thread.sleep(600)
                        }) {
                            Text("Simulate Heavy UI Jank")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(onClick = {
                            finish()
                        }) {
                            Text("Finish Activity (Trigger Leak)")
                        }
                    }
                }
            }
        }
    }
}