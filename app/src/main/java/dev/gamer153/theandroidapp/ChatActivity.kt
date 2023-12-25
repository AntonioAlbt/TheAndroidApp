package dev.gamer153.theandroidapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.gamer153.theandroidapp.ui.theme.TheAndroidAppTheme

class ChatActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val version = versions.find { it.apiLevel == intent.getIntExtra("api_version", 0) }
        setContent {
            TheAndroidAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                ),
                                navigationIcon = {
                                     IconButton(onClick = { finish() }) {
                                         Icon(
                                             imageVector = Icons.Default.ArrowBack,
                                             contentDescription = null
                                         )
                                     }
                                },
                                title = {
                                    Text("Chat for Android ${version?.version} (${version?.name})", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                                }
                            )
                        },
                    ) { pad ->
                        if (version != null) {
                            Column(modifier = Modifier
                                .padding(pad)
                                .padding(8.dp)) {
                                ChatView(version = version)
                            }
                        } else {
                            Text(text = "Error???")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatView(version: AndroidVersion) {
    Text(version.toString())
}