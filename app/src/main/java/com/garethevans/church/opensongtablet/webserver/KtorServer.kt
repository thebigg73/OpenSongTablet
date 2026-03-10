package com.garethevans.church.opensongtablet.webserver

import android.content.Context
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
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
        CoroutineScope(Dispatchers.IO).launch {
            try {
                server = embeddedServer(CIO, port = port, host = "0.0.0.0") {
                    install(WebSockets)

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
                            call.respondText(html, ContentType.Text.Html)
                        }

                        // Show the song menu
                        get("/" + mainActivityInterface?.webServer?.songMenuString+"/") {
                            // The user sends his currently loaded song so they can return if needed
                            val folder = call.request.queryParameters["folder"] ?: ""
                            val filename = call.request.queryParameters["filename"] ?: ""
                            val song = mainActivityInterface?.sqLiteHelper?.getSpecificSong(folder,filename)

                            // Now create the set menu
                            val html = CreateHTML.getSongMenuHTML(
                                c,
                                song,
                                ip,
                                mainActivityInterface?.webServer?.allowWebNavigation ?: true,
                                mainActivityInterface?.webServer?.getPreviousAndNextSongForArrows(song!!)!!
                            )
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
                try { it.send("REFRESH") } catch (e: Exception) { /* session closed */ }
            }
        }
    }
}