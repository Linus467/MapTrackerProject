package com.griffith.maptrackerproject.Views

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.griffith.maptrackerproject.R
import com.griffith.maptrackerproject.ui.theme.GreenPrimary


@Composable
fun BottomBar(
    context: Context,
    mapIntent: Intent,
    historyIntent: Intent,
    locationServiceIntent: Intent,

    content: @Composable (innerPadding: PaddingValues) -> Unit
) {
    Scaffold(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxWidth()
            .shadow(0.dp)
            .background(GreenPrimary),
        bottomBar = {
            BottomNavigation(
                modifier = Modifier
                    .fillMaxWidth()
                    .size(45.dp)
                    .background(GreenPrimary)
                    .shadow(2.dp),
                backgroundColor = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxWidth()
                        .background(GreenPrimary)
                ) {
                    TextButton(
                        onClick = { context.startActivity(mapIntent) },
                        modifier = Modifier.background(GreenPrimary)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_map_24),
                            contentDescription = "Map View",
                            Modifier.background(GreenPrimary),
                            Color.White
                        )
                    }
                    TextButton(
                        onClick = {
                            context.startActivity(locationServiceIntent)
                        },
                        modifier = Modifier
                            .background(GreenPrimary)
                            .align(Alignment.Center)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.play_circle_24),
                            contentDescription = "Tracking on off",
                            Modifier.background(GreenPrimary),
                            Color.White
                        )
                    }

                    // Go to the History activity
                    TextButton(
                        onClick = {
                            context.startActivity(historyIntent)
                        },
                        modifier = Modifier
                            .background(GreenPrimary)
                            .align(Alignment.BottomEnd)
                    ) {
                        Icon(
                            painterResource(id = R.drawable.baseline_history_24),
                            contentDescription = "Map View",
                            Modifier.background(GreenPrimary),
                            Color.White
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // Call the content lambda to add the custom content
        content(innerPadding)
    }
}
