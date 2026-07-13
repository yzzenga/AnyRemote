package com.tvremote.ir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.tvremote.ir.UI.navigation.NavGraph
import com.tvremote.ir.UI.theme.TVRemoteTheme
import com.tvremote.ir.UI.viewmodels.RemoteViewModel

class MainActivity : ComponentActivity() {

    private lateinit var remoteViewModel: RemoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        remoteViewModel = RemoteViewModel(application)

        setContent {
            TVRemoteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        remoteViewModel = remoteViewModel
                    )
                }
            }
        }
    }
}
