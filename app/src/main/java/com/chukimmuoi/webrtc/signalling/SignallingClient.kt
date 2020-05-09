package com.chukimmuoi.webrtc.signalling

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

class SignallingClient(
    private val listener: SignallingClientListener
): CoroutineScope {

    private val TAG = SignallingClient::class.java.simpleName

    companion object {
        private const val HOST_ADDRESS = "192.168.1.106"
    }

    private val job = Job()

    private val gson = Gson()

    override val coroutineContext = Dispatchers.IO + job

    private val client = HttpClient(CIO) {
        install(WebSockets)
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }

    private val sendChannel = ConflatedBroadcastChannel<String>()

    init {
        connect()
    }

    private fun connect() = launch {
        client.ws(host = HOST_ADDRESS, port = 8080, path = "/connect") {
            listener.onConnectionEstablished()

            val sendData = sendChannel.openSubscription()
            try {
                while (true) {
                    sendData.poll()?.let {
                        Log.e(TAG, "Sending: $it")
                        outgoing.send(Frame.Text(it))
                    }
                    incoming.poll()?.let {
                        if (it is Frame.Text) {
                            val data = it.readText()
                            Log.e(TAG, "Received: $data")

                            val jsonObject = gson.fromJson(data, JsonObject::class.java)
                            withContext(Dispatchers.Main) {
                                if (jsonObject.has("serverUrl")) {
                                    listener.onIceCandidateReceived(
                                        gson.fromJson(
                                            jsonObject,
                                            IceCandidate::class.java
                                        )
                                    )
                                } else if (jsonObject.has("type")
                                    && jsonObject.get("type").asString == "OFFER") {
                                    listener.onOfferReceived(
                                        gson.fromJson(
                                            jsonObject,
                                            SessionDescription::class.java
                                        )
                                    )
                                } else if (jsonObject.has("type")
                                    && jsonObject.get("type").asString == "ANSWER") {
                                    listener.onAnswerReceived(
                                        gson.fromJson(
                                            jsonObject,
                                            SessionDescription::class.java
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Throwable) {
                Log.e(TAG, "message", e)
            }
        }
    }

    fun send(dataObject: Any?) = runBlocking {
        sendChannel.send(gson.toJson(dataObject))
    }

    fun destroy() {
        client.close()
        job.complete()
    }
}