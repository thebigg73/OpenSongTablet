package com.garethevans.church.opensongtablet.webserver

import android.content.Context
import android.util.Log
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface
import io.ktor.http.ContentType
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.Collections

object KtorServer {
    private const val TAG = "KtorServer"
    private var server: NettyApplicationEngine? = null // Specific type for Netty
    private val serverMutex = Mutex() // Add: import kotlinx.coroutines.sync.Mutex

    private val sessions =
        Collections.synchronizedSet(LinkedHashSet<DefaultWebSocketServerSession>())

    fun start(c: Context, port: Int) {
        val mainActivityInterface = c as? MainActivityInterface ?: return

        CoroutineScope(Dispatchers.IO).launch {
            // Use withLock to prevent multiple threads from starting/stopping at once
            serverMutex.withLock {

                // 1. If we are already running on the correct port, don't restart
                // (Unless you explicitly want to refresh the IP)

                Log.d(TAG, "Start request received. Cleaning up...")
                stopServerInternal()
                delay(1500) // Give the OS "breathing room" to release the socket

                try {
                    val env = applicationEngineEnvironment {
                        this.developmentMode = false // Explicitly disable
                        log = LoggerFactory.getLogger("ktor.application") // Optional: requires slf4j-api
                        watchPaths = emptyList() // Ensure this is empty to prevent WatchService initialization
                        connector {
                            this.port = port
                            this.host = "0.0.0.0"
                        }

                        module {
                            // Move your routing/install logic here
                            install(WebSockets) {
                                pingPeriod = Duration.ofSeconds(15)
                                timeout = Duration.ofSeconds(30)
                            }
                            routing {
                                val ip = mainActivityInterface.webServer?.ip

                                // When we request a song page (from the set or song menu, or host song
                                // the host will send this song as html, but will also send the name of the
                                // previous and next songs as variables

                                // First load, so show the splash screen, get the user's preferences
                                // Then call the /hostsong/ page
                                get("/") {
                                    val html = CreateHTML.getSplashHTML(
                                        c,
                                        mainActivityInterface.getSong(),
                                        ip
                                    )
                                    // ADD THIS LINE TO BYPASS CHROME'S SECURITY
                                    call.response.headers.append(
                                        "Access-Control-Allow-Private-Network",
                                        "true"
                                    )
                                    call.response.headers.append("Access-Control-Allow-Origin", "*")
                                    call.respondText(html, ContentType.Text.Html)
                                }

                                // Get host song (also the default after the splash screen)
                                get("/" + mainActivityInterface.webServer?.hostSongString + "/") {
                                    val song = mainActivityInterface.song
                                    val chords =
                                        call.request.queryParameters["chords"]?.toBoolean() ?: true

                                    // Now prepare the html
                                    val html = CreateHTML.getSongHTML(
                                        c,
                                        song,
                                        ip,
                                        mainActivityInterface.webServer?.allowWebNavigation ?: true,
                                        chords,
                                        mainActivityInterface.webServer?.getPreviousAndNextSongForArrows(
                                            song!!
                                        )!!
                                    )
                                    // ADD THIS LINE TO BYPASS CHROME'S SECURITY
                                    call.response.headers.append(
                                        "Access-Control-Allow-Private-Network",
                                        "true"
                                    )
                                    call.response.headers.append("Access-Control-Allow-Origin", "*")
                                    call.respondText(html, ContentType.Text.Html)
                                }

                                // Show the set menu
                                get("/" + mainActivityInterface.webServer?.setMenuString + "/") {
                                    // The user sends his currently loaded song so they can return if needed
                                    val folder = call.request.queryParameters["folder"] ?: ""
                                    val filename = call.request.queryParameters["filename"] ?: ""
                                    val song = mainActivityInterface.sqLiteHelper?.getSpecificSong(
                                        folder,
                                        filename
                                    )

                                    // Now create the set menu
                                    val html = CreateHTML.getSetMenuHTML(
                                        c,
                                        song,
                                        ip,
                                        mainActivityInterface.webServer?.allowWebNavigation ?: true,
                                        mainActivityInterface.webServer?.getPreviousAndNextSongForArrows(
                                            song!!
                                        )!!
                                    )
                                    // ADD THIS LINE TO BYPASS CHROME'S SECURITY
                                    call.response.headers.append(
                                        "Access-Control-Allow-Private-Network",
                                        "true"
                                    )
                                    call.response.headers.append("Access-Control-Allow-Origin", "*")
                                    call.respondText(html, ContentType.Text.Html)
                                }

                                // Show the song menu
                                get("/" + mainActivityInterface.webServer?.songMenuString + "/") {
                                    // The user sends his currently loaded song so they can return if needed
                                    val folder = call.request.queryParameters["folder"] ?: ""
                                    val filename = call.request.queryParameters["filename"] ?: ""
                                    val song = mainActivityInterface.sqLiteHelper?.getSpecificSong(
                                        folder,
                                        filename
                                    )

                                    // Now create the song menu
                                    val html = CreateHTML.getSongMenuHTML(
                                        c,
                                        song,
                                        ip,
                                        mainActivityInterface.webServer?.allowWebNavigation ?: true,
                                        mainActivityInterface.webServer?.getPreviousAndNextSongForArrows(
                                            song!!
                                        )!!
                                    )
                                    // ADD THIS LINE TO BYPASS CHROME'S SECURITY
                                    call.response.headers.append(
                                        "Access-Control-Allow-Private-Network",
                                        "true"
                                    )
                                    call.response.headers.append("Access-Control-Allow-Origin", "*")
                                    call.respondText(html, ContentType.Text.Html)
                                }

                                // Show a specific song (chosen by the user)
                                get("/" + mainActivityInterface.webServer?.manualSongString + "/") {
                                    val folder = call.request.queryParameters["folder"] ?: ""
                                    val filename = call.request.queryParameters["filename"] ?: ""
                                    val chords =
                                        call.request.queryParameters["chords"]?.toBoolean() ?: true
                                    val song = mainActivityInterface.sqLiteHelper?.getSpecificSong(
                                        folder,
                                        filename
                                    )

                                    val html = CreateHTML.getSongHTML(
                                        c,
                                        song,
                                        ip,
                                        mainActivityInterface.webServer?.allowWebNavigation ?: true,
                                        chords,
                                        mainActivityInterface.webServer?.getPreviousAndNextSongForArrows(
                                            song!!
                                        )!!
                                    )
                                    // ADD THIS LINE TO BYPASS CHROME'S SECURITY
                                    call.response.headers.append(
                                        "Access-Control-Allow-Private-Network",
                                        "true"
                                    )
                                    call.response.headers.append("Access-Control-Allow-Origin", "*")
                                    call.respondText(html, ContentType.Text.Html)
                                }

                                // The WebSocket "Channel"
                                webSocket("/updates") {
                                    sessions.add(this)
                                    try {
                                        for (frame in incoming) { /* keepalive */
                                        }
                                    } finally {
                                        sessions.remove(this)
                                    }
                                }
                            }
                        }
                    }

                    Log.d(TAG, "Starting Netty Server on port $port...")
                    server = embeddedServer(Netty, env).start(wait = false)

                } catch (e: Exception) {
                    Log.e(TAG, "Server failed to start: ${e.message}")
                    if (e.toString().contains("BindException")) {
                        // Retry outside of the lock to avoid deadlocking
                        launchRetry(c, port)
                    }
                }
            }
        }
    }

    // Internal helper that doesn't use its own lock (since start() handles it)
    private suspend fun stopServerInternal() {
        server?.let {
            try {
                Log.d(TAG, "Gracefully shutting down Ktor...")
                // The first value is the grace period, the second is the timeout
                it.stop(500, 1000)
            } finally {
                server = null
                sessions.clear()
                Log.d(TAG, "Server resources cleared.")
            }
        }
    }

    private fun launchRetry(c: Context, port: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.w(TAG, "Port $port busy, retrying in 3s...")
            delay(3000)
            start(c, port)
        }
    }

    fun stopServerExternal() {
        CoroutineScope(Dispatchers.IO).launch {
            serverMutex.withLock {
                stopServerInternal() // Now suspends and cleans up properly
            }
        }
    }

    fun pushRefresh() {
        // 1. Create a snapshot of the sessions to avoid holding a lock during network I/O
        val currentSessions = synchronized(sessions) {
            sessions.toList() // Creates a shallow copy
        }

        // 2. Launch the network work in the background
        CoroutineScope(Dispatchers.IO).launch {
            currentSessions.forEach { session ->
                try {
                    // Now the suspension point is safe; we are iterating over a local list
                    session.send("REFRESH")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to send refresh, session likely closed: ${e.message}")
                }
            }
        }
    }

    fun pushPreferenceMessage(messageNumber: Int, mainActivityInterface: MainActivityInterface) {
        // Retrieve the message based on the preference index
        val message = when (messageNumber) {
            1 -> mainActivityInterface.webServer?.getWebServerMessage(1)
            2 -> mainActivityInterface.webServer?.getWebServerMessage(2)
            3 -> mainActivityInterface.webServer?.getWebServerMessage(3)
            4 -> mainActivityInterface.webServer?.getWebServerMessage(4)
            5 -> mainActivityInterface.webServer?.getWebServerMessage(5)
            else -> null
        } ?: return

        val currentSessions = synchronized(sessions) { sessions.toList() }

        CoroutineScope(Dispatchers.IO).launch {
            currentSessions.forEach { session ->
                try {
                    // Prefix with 'MSG:' so the JS knows this is an alert, not a refresh command
                    session.send("MSG:$message")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to send message: ${e.message}")
                }
            }
        }
    }
}


/*
package com.garethevans.church.opensongtablet.webserver

import android.content.Context
import android.util.Log
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface
import io.ktor.http.ContentType
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.Duration
import java.util.Collections

class KtorServer(context: Context, private val port: Int) {
    private val TAG = "KtorServer"
    private var server: ApplicationEngine? = null
    // Thread-safe set to keep track of active iOS connections
    private val sessions = Collections.synchronizedSet(LinkedHashSet<DefaultWebSocketServerSession>())
    private val c: Context = context
    private var mainActivityInterface: MainActivityInterface? = context as MainActivityInterface?

    fun start() {
        // Launch in a background Dispatcher
        // Get the ip address first
        CoroutineScope(Dispatchers.IO).launch {
            val currentIp = mainActivityInterface?.webServer?.ip ?: "0.0.0.0"

            try {
                //server = embeddedServer(CIO, port = port, host = "0.0.0.0") {
                server = embeddedServer(CIO, port = port, host = currentIp) { // Use explicit IP
                    install(WebSockets) {
                        pingPeriod = Duration.ofSeconds(15) // How often the server pings the Mac
                        timeout = Duration.ofSeconds(30)    // How long to wait before giving up
                        maxFrameSize = Long.MAX_VALUE
                        masking = false
                    }

                    routing {
                        // 1. THE LIVE VIEW (Port 8080 /)

                        // Get your current IP
                        val ip = mainActivityInterface?.webServer?.ip


                        // When we request a song page (from the set or song menu, or host song
                        // the host will send this song as html, but will also send the name of the
                        // previous and next songs as variables

                        // First load, so show the splash screen, get the user's preferences
                        // Then call the /hostsong/ page
                        get("/") {
                            val html = CreateHTML.getSplashHTML(
                                c,
                                mainActivityInterface?.getSong(),
                                ip
                            )
                            // ADD THIS LINE TO BYPASS CHROME'S SECURITY
                            call.response.headers.append("Access-Control-Allow-Private-Network", "true")
                            call.response.headers.append("Access-Control-Allow-Origin", "*")
                            call.respondText(html, ContentType.Text.Html)
                        }

                        // Get host song (also the default after the splash screen)
                        get("/" + mainActivityInterface?.webServer?.hostSongString+"/") {
                            val song = mainActivityInterface?.song
                            val chords = call.request.queryParameters["chords"]?.toBoolean() ?: true

                            // Now prepare the html
                            val html = CreateHTML.getSongHTML(
                                c,
                                song,
                                ip,
                                mainActivityInterface?.webServer?.allowWebNavigation ?: true,
                                chords,
                                mainActivityInterface?.webServer?.getPreviousAndNextSongForArrows(song!!)!!
                            )
                            // ADD THIS LINE TO BYPASS CHROME'S SECURITY
                            call.response.headers.append("Access-Control-Allow-Private-Network", "true")
                            call.response.headers.append("Access-Control-Allow-Origin", "*")
                            call.respondText(html, ContentType.Text.Html)
                        }

                        // Show the set menu
                        get("/" + mainActivityInterface?.webServer?.setMenuString+"/") {
                            // The user sends his currently loaded song so they can return if needed
                            val folder = call.request.queryParameters["folder"] ?: ""
                            val filename = call.request.queryParameters["filename"] ?: ""
                            val song = mainActivityInterface?.sqLiteHelper?.getSpecificSong(folder,filename)

                            // Now create the set menu
                            val html = CreateHTML.getSetMenuHTML(
                                c,
                                song,
                                ip,
                                mainActivityInterface?.webServer?.allowWebNavigation ?: true,
                                mainActivityInterface?.webServer?.getPreviousAndNextSongForArrows(song!!)!!
                            )
                            // ADD THIS LINE TO BYPASS CHROME'S SECURITY
                            call.response.headers.append("Access-Control-Allow-Private-Network", "true")
                            call.response.headers.append("Access-Control-Allow-Origin", "*")
                            call.respondText(html, ContentType.Text.Html)
                        }

                        // Show the song menu
                        get("/" + mainActivityInterface?.webServer?.songMenuString+"/") {
                            // The user sends his currently loaded song so they can return if needed
                            val folder = call.request.queryParameters["folder"] ?: ""
                            val filename = call.request.queryParameters["filename"] ?: ""
                            val song = mainActivityInterface?.sqLiteHelper?.getSpecificSong(folder,filename)

                            // Now create the song menu
                            val html = CreateHTML.getSongMenuHTML(
                                c,
                                song,
                                ip,
                                mainActivityInterface?.webServer?.allowWebNavigation ?: true,
                                mainActivityInterface?.webServer?.getPreviousAndNextSongForArrows(song!!)!!
                            )
                            // ADD THIS LINE TO BYPASS CHROME'S SECURITY
                            call.response.headers.append("Access-Control-Allow-Private-Network", "true")
                            call.response.headers.append("Access-Control-Allow-Origin", "*")
                            call.respondText(html, ContentType.Text.Html)
                        }

                        // Show a specific song (chosen by the user)
                        get("/" + mainActivityInterface?.webServer?.manualSongString+"/") {
                            val folder = call.request.queryParameters["folder"] ?: ""
                            val filename = call.request.queryParameters["filename"] ?: ""
                            val chords = call.request.queryParameters["chords"]?.toBoolean() ?: true
                            val song = mainActivityInterface?.sqLiteHelper?.getSpecificSong(folder,filename)

                            val html = CreateHTML.getSongHTML(
                                c,
                                song,
                                ip,
                                mainActivityInterface?.webServer?.allowWebNavigation ?: true,
                                chords,
                                mainActivityInterface?.webServer?.getPreviousAndNextSongForArrows(song!!)!!
                            )
                            // ADD THIS LINE TO BYPASS CHROME'S SECURITY
                            call.response.headers.append("Access-Control-Allow-Private-Network", "true")
                            call.response.headers.append("Access-Control-Allow-Origin", "*")
                            call.respondText(html, ContentType.Text.Html)
                        }

                        // The WebSocket "Channel"
                        webSocket("/updates") {
                            sessions.add(this)
                            try {
                                for (frame in incoming) {
                                    // We don't need to read anything from the phone,
                                    // just keep the connection alive.
                                }
                            } finally {
                                sessions.remove(this)
                            }
                        }
                    }
                }.start(wait = false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stop() {
        server?.stop(1000, 2000)
    }

    // Function to "Shout" to all devices
    @OptIn(DelicateCoroutinesApi::class)
    fun pushRefresh() {
        // Run on a background thread so it doesn't block the UI
        GlobalScope.launch {
            sessions.forEach {
                try { it.send("REFRESH") } catch (e: Exception) { */
/* session closed *//*
 }
            }
        }
    }

}*/
