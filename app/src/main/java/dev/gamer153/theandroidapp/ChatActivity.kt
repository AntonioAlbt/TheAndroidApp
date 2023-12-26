package dev.gamer153.theandroidapp

import android.content.Context
import android.os.Bundle
import android.os.CancellationSignal
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gamer153.theandroidapp.ui.theme.TheAndroidAppTheme
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

@Immutable
data class Message(
    val id: Int,
    val uid: Int,
    val name: String,
    val content: String
) {
    constructor(data: JSONObject) : this(
        data.getInt("id"),
        data.getInt("uid"),
        data.getString("name"),
        data.getString("content")
    )
}

class UiState {
    val messages: SnapshotStateList<Message> = mutableStateListOf()

    val connected: MutableState<Boolean> = mutableStateOf(false)
    val isConnected
        get() = connected.component1()

    val authed: MutableState<Boolean> = mutableStateOf(false)
    val isAuthed
        get() = authed.component1()

    val uid: MutableState<Int?> = mutableStateOf(null)
    val gUid
        get() = uid.component1()

    fun addMessage(msg: Message) {
        messages.add(msg)
    }
    fun setMessages(data: List<Message>) {
        messages.clear()
        messages.addAll(data)
    }
}

var state = UiState()

class ChatActivity : ComponentActivity() {
    private val version by lazy { versions.find { it.apiLevel == intent.getIntExtra("api_version", 0) } }
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        state = UiState()

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
                                ChatView(version = version!!, state = state, connect = ::connect, sendMessage = {
                                    ws?.send("""{"action":"send","content":"${it.replace("\"", "'")}"}""")
                                }, enabled = ws != null)
                            }
                        } else {
                            Text(text = "Error???")
                        }
                    }
                }
            }
        }
    }

    private var ws: WebSocket? = null
    private val client by lazy { OkHttpClient() }
    private var refreshThread: Thread? = null
    private var refreshThreadCancellation = CancellationSignal()

    override fun onResume() {
        super.onResume()
        connect()
    }

    private fun connect() {
        ws = client.newWebSocket(
            Request.Builder().url("ws://10.0.2.2:3000/?auth=dh287h187h328").build(),
            object : WebSocketListener() {
                override fun onMessage(webSocket: WebSocket, text: String) {
                    val data = JSONObject(text)
                    Log.d("websocket-data", data.toString())
                    when (data.getString("happening")) {
                        "alive" -> {
                            if (!state.isAuthed) {
                                webSocket.send("""{"action":"auth","name":"ChatApp","uid":101,"api":${version?.apiLevel ?: 0}}""")
                            }
                        }
                        "auth" -> {
                            if (data.getBoolean("success")) {
                                state.uid.component2()(data.getInt("uid"))
                                state.authed.component2()(true)
                                webSocket.send("""{"action":"get"}""")
                            } else {
                                webSocket.close(1000, "")
                            }
                        }
                        "get" -> {
                            val msgs = data.getJSONArray("data")
                            if (msgs.length() == 0) return
                            state.messages.clear()
                            for (i in 0 until msgs.length()) {
                                state.messages.add(Message(msgs.getJSONObject(i)))
                            }
                            Log.d("websocket-get", "onMessage: got ${msgs.length()} messages")
                        }
                        "new-message" -> {
                            state.messages.add(Message(data))
                        }
                    }
                }

                override fun onOpen(webSocket: WebSocket, response: Response) {
                    state.connected.component2()(true)
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d("websocket", "onClosing: websocket closing $code $reason")
                    webSocket.close(1000, "")
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d("websocket", "onClosed: websocket closed")
                    handleDisconnect()
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.e("websocket", "onFailure: websocket failed", t)
                    handleDisconnect()
                }

                fun handleDisconnect() {
                    state.connected.component2()(false)
                    state.authed.component2()(false)
                    refreshThreadCancellation.cancel()
                    ws = null
                }
            }
        )
        refreshThreadCancellation = CancellationSignal()
        refreshThread = thread {
            while (!refreshThreadCancellation.isCanceled) {
                val r = ws!!.send("""{"action": "alive"}""")
                Log.d("websocket-alive", "connect: sent alive, $r")
                TimeUnit.SECONDS.sleep(7)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ws?.close(1000, "")
    }
}

@Composable
fun ChatView(version: AndroidVersion, state: UiState, connect: () -> Unit, sendMessage: (message: String) -> Unit, enabled: Boolean) {
//    Text(version.toString())
    val connected by remember { state.connected }
    val authed by remember { state.authed }
    val uid by remember { state.uid }
    Text("connected: $connected, authed: $authed, uid: $uid")

    if (!connected) Button(onClick = connect) {
        Text("reconnect")
    }
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f)) {
            for (msg in state.messages) {
                MessageCard(message = msg)
            }
        }
        var inputValue by remember { mutableStateOf("") } // 2

        Row {
            TextField( // 4
                modifier = Modifier.weight(1f),
                value = inputValue,
                onValueChange = { inputValue = it },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions {
                    sendMessage(inputValue)
                    inputValue = ""
                },
                enabled = enabled
            )
            val view = LocalView.current
            Button( // 5
                modifier = Modifier.height(56.dp),
                onClick = {
                    sendMessage(inputValue)
                    inputValue = ""
                    val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.hideSoftInputFromWindow(view.windowToken, 0)
                },
                enabled = inputValue.isNotBlank() && enabled,
            ) {
                Icon( // 6
                    imageVector = Icons.Default.Send,
                    contentDescription = ""
                )
            }
        }
    }
}

@Composable
fun MessageCard(message: Message) { // 1
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = when (message.uid) {
            state.gUid -> Alignment.End
            else -> Alignment.Start
        },
    ) {
        Card(
            modifier = Modifier.widthIn(max = 340.dp),
            shape = cardShapeFor(message),
            colors = CardDefaults.cardColors(containerColor = if (message.uid == state.gUid) Color(0xff0c8976) else Color(0xff0c5d89))
        ) {
            Text(

                modifier = Modifier.padding(8.dp),
                text = message.content,
            )
        }

        Text( // 4
            text = message.name,
            fontSize = 12.sp,
        )
    }
}

@Composable
fun cardShapeFor(message: Message): Shape {
    val roundedCorners = RoundedCornerShape(16.dp)
    return when (message.uid) {
        state.gUid -> roundedCorners.copy(bottomEnd = CornerSize(0))
        else -> roundedCorners.copy(bottomStart = CornerSize(0))
    }
}
