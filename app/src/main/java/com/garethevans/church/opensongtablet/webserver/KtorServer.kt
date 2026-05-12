package com.garethevans.church.opensongtablet.webserver

import android.content.Context
import android.util.Log
import com.garethevans.church.opensongtablet.MainActivity
import com.garethevans.church.opensongtablet.R
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface
import com.garethevans.church.opensongtablet.nearby.ShareableObject
import com.garethevans.church.opensongtablet.songprocessing.Song
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.plugins.cors.routing.CORS
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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.Collections
import io.ktor.websocket.CloseReason
import io.ktor.websocket.close
import kotlinx.coroutines.cancelChildren

object KtorServer {
    private const val TAG = "KtorServer"
    private var server: NettyApplicationEngine? = null // Specific type for Netty
    private val serverMutex = Mutex() // Add: import kotlinx.coroutines.sync.Mutex
    private var currentPort: Int? = null
    private var mainActivityInterface: MainActivityInterface? = null
    //private var interfaceRef: java.lang.ref.WeakReference<MainActivityInterface>? = null
    private val serverJob = SupervisorJob()
    private val serverScope = CoroutineScope(Dispatchers.IO + serverJob)
    private var isShuttingDown = false
    private var isRestartPending = false
    private var pendingPort: Int = 8080

    fun setInterface(mainActivityInterface: MainActivityInterface) {
        this.mainActivityInterface = mainActivityInterface
    }

    // Use this helper inside your routing to get the interface
    //private fun getInterface(): MainActivityInterface? = interfaceRef?.get()

    private val sessions =
        Collections.synchronizedSet(LinkedHashSet<DefaultWebSocketServerSession>())

    fun start(c: Context, appContext: Context, port: Int) {
        if (isShuttingDown || mainActivityInterface==null) return // Refuse to start

        //val mainActivityInterface = c.applicationContext as? MainActivityInterface ?: return
        //val mainActivityInterface = getInterface();

        CoroutineScope(Dispatchers.IO).launch {
            // Use withLock to prevent multiple threads from starting/stopping at once
            serverMutex.withLock {

                if (isShuttingDown) {
                    Log.d(TAG, "Shutdown in progress. Flagging restart for port $port.")
                    isRestartPending = true
                    pendingPort = port
                    return@withLock
                }

                if (server != null) {
                    if (currentPort == port) {
                        Log.d(TAG, "Server already running on port $port.")
                        return@withLock
                    }
                    // If port changed, trigger shutdown and set restart flag
                    isRestartPending = true
                    pendingPort = port
                    stopServerInternal()
                    return@withLock
                }


                // 2. If server exists but is on a different port or is dead, stop it first
                //stopServerInternal()

                // Small delay to ensure sockets are released by the OS
                delay(500)

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

                            install(CORS) {
                                // Allow the PWA to access the API from any origin
                                anyHost()

                                // Corrected function names for Ktor 2.x/3.x
                                allowHeader(HttpHeaders.ContentType)
                                allowHeader(HttpHeaders.AccessControlAllowOrigin)

                                allowMethod(HttpMethod.Get)
                                allowMethod(HttpMethod.Options) // Important for browser pre-flight checks
                            }

                            routing {
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
                                    call.response.headers.append(
                                        "Access-Control-Allow-Private-Network",
                                        "true"
                                    )
                                    call.response.headers.append("Access-Control-Allow-Origin", "*")
                                    call.respondText(html, ContentType.Text.Html)
                                }

                                // Get host song (also the default after the splash screen)
                                get("/" + mainActivityInterface?.webServer?.hostSongString + "/") {
                                    val song = mainActivityInterface?.song
                                    val chords =
                                        call.request.queryParameters["chords"]?.toBoolean() ?: true

                                    // Now prepare the html
                                    val html = CreateHTML.getSongHTML(
                                        c,
                                        song,
                                        ip,
                                        mainActivityInterface?.webServer?.allowWebNavigation ?: true,
                                        chords,
                                        mainActivityInterface?.webServer?.getPreviousAndNextSongForArrows(
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
                                get("/" + mainActivityInterface?.webServer?.setMenuString + "/") {
                                    // The user sends his currently loaded song so they can return if needed
                                    val folder = call.request.queryParameters["folder"] ?: ""
                                    val filename = call.request.queryParameters["filename"] ?: ""
                                    val song = mainActivityInterface?.sqLiteHelper?.getSpecificSong(
                                        folder,
                                        filename
                                    )

                                    // Now create the set menu
                                    val html = CreateHTML.getSetMenuHTML(
                                        c,
                                        song,
                                        ip,
                                        mainActivityInterface?.webServer?.allowWebNavigation ?: true,
                                        mainActivityInterface?.webServer?.getPreviousAndNextSongForArrows(
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
                                get("/" + mainActivityInterface?.webServer?.songMenuString + "/") {
                                    // The user sends his currently loaded song so they can return if needed
                                    val folder = call.request.queryParameters["folder"] ?: ""
                                    val filename = call.request.queryParameters["filename"] ?: ""
                                    val song = mainActivityInterface?.sqLiteHelper?.getSpecificSong(
                                        folder,
                                        filename
                                    )

                                    // Now create the song menu
                                    val html = CreateHTML.getSongMenuHTML(
                                        c,
                                        song,
                                        ip,
                                        mainActivityInterface?.webServer?.allowWebNavigation ?: true,
                                        mainActivityInterface?.webServer?.getPreviousAndNextSongForArrows(
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
                                get("/" + mainActivityInterface?.webServer?.manualSongString + "/") {
                                    val folder = call.request.queryParameters["folder"] ?: ""
                                    val filename = call.request.queryParameters["filename"] ?: ""
                                    val chords =
                                        call.request.queryParameters["chords"]?.toBoolean() ?: true
                                    val song = mainActivityInterface?.sqLiteHelper?.getSpecificSong(
                                        folder,
                                        filename
                                    )

                                    val html = CreateHTML.getSongHTML(
                                        c,
                                        song,
                                        ip,
                                        mainActivityInterface?.webServer?.allowWebNavigation ?: true,
                                        chords,
                                        mainActivityInterface?.webServer?.getPreviousAndNextSongForArrows(
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

                                // An API to receive the song via json for a PWS such as SongEditorWeb
                                get("/api/song") {
                                    val folder = call.request.queryParameters["folder"] ?: ""
                                    val filename = call.request.queryParameters["filename"] ?: ""
                                    // Kotlin's 'if' is an expression, so we can assign the result directly
                                    val song: Song? = if (folder.isNullOrBlank() || filename.isNullOrBlank() ||
                                        mainActivityInterface?.webServer?.allowWebNavigation == false) {
                                        mainActivityInterface?.getSong()
                                    } else {
                                        mainActivityInterface?.sqLiteHelper?.getSpecificSong(folder, filename)
                                    }

                                    try {
                                        if (song != null) {
                                            // Serialize the full object using your existing Gson instance
                                            val jsonString = MainActivity.gson.toJson(song)
                                            call.respondText(jsonString, ContentType.Application.Json)
                                        } else {
                                            call.respondText(
                                                "{\"error\": \"Song not found in database\"}",
                                                ContentType.Application.Json,
                                                HttpStatusCode.NotFound
                                            )
                                        }
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error fetching song: ${e.message}")
                                        call.respondText(
                                            "{\"error\": \"Internal server error\"}",
                                            ContentType.Application.Json,
                                            HttpStatusCode.InternalServerError
                                        )
                                    }
                                }

                                // API to trigger the webServer host device to load a song (act as a client to SongEditorWeb or other API device)
                                get("/api/remote") {
                                    if (mainActivityInterface?.webServer?.listenForWebAPI == true) {
                                        // An API to receive the song via json for a PWS such as SongEditorWeb
                                        val folder = call.request.queryParameters["folder"] ?: ""
                                        val filename =
                                            call.request.queryParameters["filename"] ?: ""
                                        // Kotlin's 'if' is an expression, so we can assign the result directly
                                        if (!folder.isNullOrBlank() && !filename.isNullOrBlank() &&
                                            mainActivityInterface?.webServer?.allowWebNavigation == true) {
                                            mainActivityInterface?.doSongLoad(
                                                folder,
                                                filename,
                                                true
                                            );
                                            // 2. Respond with a 200 OK so the PWA knows it worked
                                            call.respondText(
                                                "{\"status\": \"success\", \"message\": \"Loading $filename\"}",
                                                ContentType.Application.Json,
                                                HttpStatusCode.OK
                                            )

                                        } else {
                                            // Respond with an error if parameters are missing
                                            call.respondText(
                                                "{\"status\": \"error\", \"message\": \"Missing parameters or the device has blocked manual navigation\"}",
                                                ContentType.Application.Json,
                                                HttpStatusCode.BadRequest
                                            )
                                        }
                                    } else {
                                        // Respond that the host isn't listening
                                        call.respondText(
                                            "{\"status\": \"error\", \"message\": \"Device not listening\"}",
                                            ContentType.Application.Json,
                                            HttpStatusCode.BadRequest
                                        )
                                    }
                                }

                                // API to return the full list of available songs as a JSON array
                                get("/api/list") {
                                    if (mainActivityInterface?.webServer?.allowWebNavigation ?: false) {
                                        try {
                                            // 1. Call your existing logic to get the populated ArrayList
                                            // (Assuming you've exposed this via your interface)
                                            val songList: ArrayList<ShareableObject> =
                                                mainActivityInterface?.sqLiteHelper?.shareableSongs
                                                    ?: arrayListOf()

                                            // 2. Serialize the entire list to a JSON Array using Gson
                                            val jsonString = MainActivity.gson.toJson(songList)

                                            // 3. Return the response
                                            call.respondText(
                                                jsonString,
                                                ContentType.Application.Json
                                            )
                                        } catch (e: Exception) {
                                            Log.e(TAG, "Error fetching song list: ${e.message}")
                                            call.respondText(
                                                "{\"error\": \"Could not retrieve song list\"}",
                                                ContentType.Application.Json,
                                                HttpStatusCode.InternalServerError
                                            )
                                        }
                                    }
                                }

                                // API to return the full list of available songs as a JSON array
                                get("/api/save") {
                                    Log.d(TAG,"save attempt");
                                    if (mainActivityInterface?.webServer?.listenForWebAPI == true) {
                                        // An API to receive the song via json from a PWS such as SongEditorWeb
                                        val newSongString = call.request.queryParameters["song"] ?: "";
                                        val newSetString = call.request.queryParameters["set"] ?: "";

                                        // Kotlin's 'if' is an expression, so we can assign the result directly
                                        if (!newSongString.isNullOrBlank()) {
                                            // Get an Song Object from the JSON string
                                            try {
                                                val newSong: Song = MainActivity.gson.fromJson(
                                                    newSongString,
                                                    Song::class.java
                                                )

                                                if (newSong.filename!=null && newSong.folder!=null) {
                                                    // The song didn't exist, so prompt the user to see if they want to save the new incoming song.
                                                    val remoteSaveSongBottomSheet = RemoteSaveBottomSheet(newSong,null);
                                                    Log.d(TAG,"Getting here: mainActivityInterface:"+mainActivityInterface);
                                                    mainActivityInterface?.let {
                                                        remoteSaveSongBottomSheet.show(
                                                            it.myFragmentManager,
                                                            "RemoteSaveBottomSheet"
                                                        )
                                                    }
                                                    // 2. Respond with a 200 OK so the PWA knows it worked
                                                    call.respondText(
                                                        "{\"status\": \"success\", \"message\": \"Saving ${newSong.filename}\"}",
                                                        ContentType.Application.Json,
                                                        HttpStatusCode.OK
                                                    )
                                                } else {
                                                    // Respond with an error if parameters are missing
                                                    call.respondText(
                                                        "{\"status\": \"error\", \"message\": \"Missing parameters or the device isn't listening\"}",
                                                        ContentType.Application.Json,
                                                        HttpStatusCode.BadRequest
                                                    )
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                // Respond with an error if parameters are missing
                                                call.respondText(
                                                    "{\"status\": \"error\", \"message\": \"The song json has errors\"}",
                                                    ContentType.Application.Json,
                                                    HttpStatusCode.BadRequest
                                                )
                                            }

                                        } else if (!newSongString.isNullOrBlank()) {
                                            // TODO currently just return an error as we aren't dealing with this yet
                                            call.respondText(
                                                "{\"status\": \"error\", \"message\": \"OpenSongApp isn't yet dealing with saving a set\"}",
                                                ContentType.Application.Json,
                                                HttpStatusCode.BadRequest
                                            )
                                        }
                                    } else {
                                        // Respond that the host isn't listening
                                        call.respondText(
                                            "{\"status\": \"error\", \"message\": \"Device not listening\"}",
                                            ContentType.Application.Json,
                                            HttpStatusCode.BadRequest
                                        )
                                    }
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
                    server = embeddedServer(Netty, env).apply {
                        start(wait = false)
                    }
                    currentPort = port
                    Log.d(TAG, "Server started successfully on $port")

                } catch (e: Exception) {
                    Log.e(TAG, "Failed to start server: ${e.message}")
                    server = null
                    currentPort = null
                }
            }
        }
    }

    private suspend fun stopServerInternal() {
        if (isShuttingDown) return
        isShuttingDown = true
        serverJob.cancelChildren()

        try {
            server?.let {
                Log.d(TAG, "Gracefully shutting down Ktor...")

                // 1. Close all active WebSocket sessions first
                val sessionSnapshot = synchronized(sessions) { sessions.toList() }
                sessionSnapshot.forEach { session ->
                    try {
                        // Use a specific reason so the client knows why it's disconnecting
                        session.close(CloseReason(CloseReason.Codes.GOING_AWAY, "Server Shutting Down"))
                    } catch (e: Exception) {
                        Log.e(TAG, "Error closing session: ${e.message}")
                    }
                }
                sessions.clear()

                // 2. Stop the engine with a grace period
                it.stop(500, 1000)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Shutdown error: ${e.message}")
        } finally {
            server = null
            currentPort = null
            isShuttingDown = false
            Log.d(TAG, "Server resources cleared.")

            // CHECK FOR PENDING RESTART
            if (isRestartPending) {
                isRestartPending = false
                Log.d(TAG, "Triggering pending restart on port $pendingPort")
                // Call start again - it will now pass the isShuttingDown check
                // You may need to pass the context through or store a reference
                mainActivityInterface?.let {
                    start(it as Context, (it as Context).applicationContext, pendingPort)
                }
            }
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
        val currentSong: Song? = mainActivityInterface?.getSong()

        // Create a simple map that tells the client what to do
        val payload = mapOf(
            "action" to "REFRESH", // Existing browsers can look for this
            "data" to currentSong  // PWAs can use this
        )

        val jsonString = MainActivity.gson.toJson(payload)

        // 1. Create a snapshot of the sessions to avoid holding a lock during network I/O
        val currentSessions = synchronized(sessions) { sessions.toList() }

        // Use our controlled scope here
        serverScope.launch {
            currentSessions.forEach { session ->
                try {
                    session.send(jsonString)
                } catch (e: Exception) {
                    Log.e(TAG, "WebSocket send failed: ${e.message}")
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

        // Use our controlled scope here
        serverScope.launch {
            currentSessions.forEach { session ->
                try {
                    // Prefix with 'MSG:' so the JS knows this is an alert, not a refresh command
                    session.send("MSG:$message")
                } catch (e: Exception) {
                    Log.e(TAG, "WebSocket send failed: ${e.message}")
                }
            }
        }
    }
}
