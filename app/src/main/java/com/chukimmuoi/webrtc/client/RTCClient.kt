package com.chukimmuoi.webrtc.client

import android.app.Application
import android.content.Context
import org.webrtc.*

//@see: https://amryousef.me/android-webrtc
class RTCClient(context: Application, observer: PeerConnection.Observer) {

    val TAG = RTCClient::class.java.simpleName

    companion object {
        private const val LOCAL_TRACK_ID  = "local_track"
        private const val LOCAL_STREAM_ID = "local_stream"
    }

    private val rootEglBase: EglBase by lazy {
        EglBase.create()
    }

    private val peerConnectionFactory: PeerConnectionFactory by lazy {
        buildPeerConnectionFactory()
    }

    // Number 06-1
    private val iceServer = listOf(
        PeerConnection
            .IceServer
            .builder("stun:stun.l.google.com:19302")
            .createIceServer()
    )

    private val videoCapture: VideoCapturer by lazy {
        getVideoCapture(context)
    }

    private val localVideoSource: VideoSource by lazy {
        peerConnectionFactory.createVideoSource(false)
    }

    private val peerConnection: PeerConnection? by lazy {
        buildPeerConnection(observer)
    }

    init {
        initPeerConnectionFactory(context)
    }

    // Number 01
    private fun initPeerConnectionFactory(context: Application) {
        val options = PeerConnectionFactory
            .InitializationOptions
            .builder(context)
            .setEnableInternalTracer(true)
            .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }

    // Number 02
    private fun buildPeerConnectionFactory(): PeerConnectionFactory {
        val output = PeerConnectionFactory
            .builder()
            .setVideoDecoderFactory(
                DefaultVideoDecoderFactory(
                    rootEglBase.eglBaseContext
                )
            )
            .setVideoEncoderFactory(
                DefaultVideoEncoderFactory(
                    rootEglBase.eglBaseContext,
                    true,
                    true
                )
            )
            .setOptions(
                PeerConnectionFactory
                    .Options()
                    .apply {
                        disableEncryption = true
                        disableNetworkMonitor = true
                    }
            )
            .createPeerConnectionFactory()

        return output
    }

    // Number 06-3
    private fun buildPeerConnection(observer: PeerConnection.Observer): PeerConnection? {
        val output = peerConnectionFactory.createPeerConnection(
            iceServer,
            observer
        )

        return output
    }

    // Number 04 Get camera trước.
    private fun getVideoCapture(context: Context): CameraVideoCapturer {
        val output = Camera2Enumerator(context)
            .run {
                deviceNames.find {
                    isFrontFacing(it)
                }?.let {
                    createCapturer(it, null)
                } ?: throw IllegalStateException()
            }

        return output
    }

    // Number 03
    fun initSurfaceView(view: SurfaceViewRenderer) {
        view.run {
            setMirror(true)               // Phản chiếu.
            setEnableHardwareScaler(true) // Tăng tốc phần cứng.
            init(rootEglBase.eglBaseContext, null)
        }
    }

    // Number 05
    fun startLocalVideoCapture(localVideoOutput: SurfaceViewRenderer) {
        val surfaceTextureHelper = SurfaceTextureHelper
            .create(
                Thread.currentThread().name,
                rootEglBase.eglBaseContext
            )
        videoCapture.initialize(
            surfaceTextureHelper,
            localVideoOutput.context,
            localVideoSource.capturerObserver
        )
        // width, height, frame per second
        videoCapture.startCapture(320, 240, 60)

        val localVideoTrack = peerConnectionFactory
            .createVideoTrack(
                LOCAL_TRACK_ID,
                localVideoSource
            )
        localVideoTrack.addSink(localVideoOutput)

        val localStream = peerConnectionFactory
            .createLocalMediaStream(
                LOCAL_STREAM_ID
            )
        localStream.addTrack(localVideoTrack)

        peerConnection?.addStream(localStream)
    }

    // Number 07-1
    private fun PeerConnection.call(sdpObserver: SdpObserver) {
        val constraints = MediaConstraints().apply {
            mandatory.add(
                MediaConstraints.KeyValuePair(
                    "OfferToReceiveVideo",
                    "true"
                )
            )
        }

        createOffer(object : SdpObserver by sdpObserver {
            override fun onCreateSuccess(desc: SessionDescription) {
                setLocalDescription(object: SdpObserver {
                    override fun onSetFailure(s: String) {
                    }

                    override fun onSetSuccess() {
                    }

                    override fun onCreateSuccess(desc: SessionDescription) {
                    }

                    override fun onCreateFailure(s: String) {
                    }
                }, desc)

                //TODO("Gửi nó đến máy chủ báo hiệu của bạn thông qua WebSocket hoặc các cách khác")
                //TODO("Send it to your signalling server via WebSocket or other ways")
                sdpObserver.onCreateSuccess(desc)
            }
        }, constraints)
    }

    // Number 08-1
    private fun PeerConnection.answer(sdpObserver: SdpObserver) {
        val constraints = MediaConstraints().apply {
            mandatory.add(
                MediaConstraints.KeyValuePair(
                    "OfferToReceiveVideo",
                    "true"
                )
            )
        }

        createAnswer(object : SdpObserver by sdpObserver {
            override fun onCreateSuccess(desc: SessionDescription) {
                setLocalDescription(object : SdpObserver {
                    override fun onSetFailure(s: String) {
                    }

                    override fun onSetSuccess() {
                    }

                    override fun onCreateSuccess(desc: SessionDescription) {
                    }

                    override fun onCreateFailure(s: String) {
                    }

                }, desc)

                //TODO("Gửi nó đến máy chủ báo hiệu của bạn thông qua WebSocket hoặc các cách khác")
                //TODO("Send it to your signalling server via WebSocket or other ways")
                sdpObserver.onCreateSuccess(desc)
            }
        }, constraints)
    }

    fun call(sdpObserver: SdpObserver) {
        peerConnection?.call(sdpObserver)
    }

    fun answer(sdpObserver: SdpObserver) {
        peerConnection?.answer(sdpObserver)
    }

    // Number 07:08-2
    fun onRemoteSessionReceived(sessionDescription: SessionDescription) {
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onSetFailure(s: String) {
            }

            override fun onSetSuccess() {
            }

            override fun onCreateSuccess(desc: SessionDescription) {
            }

            override fun onCreateFailure(s: String) {
            }
        }, sessionDescription)
    }

    fun addIceCandidate(iceCandidate: IceCandidate) {
        peerConnection?.addIceCandidate(iceCandidate)
    }
}