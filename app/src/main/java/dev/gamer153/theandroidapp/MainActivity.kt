package dev.gamer153.theandroidapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import dev.gamer153.theandroidapp.ui.theme.TheAndroidAppTheme

data class AndroidVersion(val version: String, val apiLevel: Int, val name: String, val features: List<String>, val link: String)

val versions = listOf(
    AndroidVersion("1", 7, "Basic", listOf("keine", "really"), "https://vlant.de"),
    AndroidVersion("2.0", 9, "Eclair", listOf("keine2", "really2"), "https://vlant.de/#")
)

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                                colors = topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                ),
                                title = {
                                    Text("The Android App", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                                }
                            )
                        },
                    ) { pad ->
                        Column(
                            modifier = Modifier
                                .padding(pad)
                                .padding(8.dp, 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            for (version in versions) {
                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        Text(
                                            "${version.version}: ${version.name}",
                                            fontSize = 5.em
                                        )
                                        Text("API: ${version.apiLevel}")
                                        Text("Features:\n${version.features.joinToString("\n- ", "- ")}")
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = "Chat:")
                                            IconButton(onClick = {
                                                startActivity(Intent(this@MainActivity, ChatActivity::class.java).apply { putExtra("api_version", version.apiLevel) })
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Default.Email,
                                                    contentDescription = null
                                                )
                                            }
                                            Text(text = "Info:")
                                            IconButton(onClick = {
                                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(version.link)))
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Default.Info,
                                                    contentDescription = null
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
