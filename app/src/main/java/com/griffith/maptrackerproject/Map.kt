package com.griffith.maptrackerproject

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Button
import androidx.compose.material.Text

class Map : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Button(onClick = {
                val intent = Intent(this,Compass::class.java).apply{
                    type = "text/plain"
                    putExtra("ExtraSubject", arrayListOf("student@griffith.ie"))
                }
                if(intent.resolveActivity(packageManager) != null){
                    startActivity(intent)
                }
            }) {
                Text("Go to Compas")
            }
        }
    }
}
