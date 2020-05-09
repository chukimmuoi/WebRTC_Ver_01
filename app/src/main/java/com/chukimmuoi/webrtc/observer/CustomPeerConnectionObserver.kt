package com.chukimmuoi.webrtc.observer

import android.util.Log
import org.webrtc.*

open class CustomPeerConnectionObserver: PeerConnection.Observer {

    val TAG = CustomPeerConnectionObserver::class.java.simpleName

    override fun onIceCandidate(iceCandidate: IceCandidate) {
        Log.e(TAG, "onIceCandidate iceCandidate = $iceCandidate")
    }

    override fun onDataChannel(dataChannel: DataChannel) {
        Log.e(TAG, "onDataChannel dataChannel = $dataChannel")
    }

    override fun onIceConnectionReceivingChange(b: Boolean) {
        Log.e(TAG, "onIceConnectionReceivingChange b = $b")
    }

    override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
        Log.e(TAG, "onIceConnectionChange iceConnectionState = $iceConnectionState")
    }

    override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState) {
        Log.e(TAG, "onIceGatheringChange iceGatheringState = $iceGatheringState")
    }

    override fun onAddStream(mediaStream: MediaStream) {
        Log.e(TAG, "onAddStream mediaStream = $mediaStream")
    }

    override fun onSignalingChange(signalingState: PeerConnection.SignalingState) {
        Log.e(TAG, "onSignalingChange signalingState = $signalingState")
    }

    override fun onIceCandidatesRemoved(iceCandidates: Array<out IceCandidate>) {
        Log.e(TAG, "onIceCandidatesRemoved iceCandidates = $iceCandidates")
    }

    override fun onRemoveStream(mediaStream: MediaStream) {
        Log.e(TAG, "onRemoveStream mediaStream = $mediaStream")
    }

    override fun onRenegotiationNeeded() {
        Log.e(TAG, "onRenegotiationNeeded")
    }

    override fun onAddTrack(rtpReceiver: RtpReceiver, mediaStreams: Array<out MediaStream>) {
        Log.e(TAG, "onAddTrack rtpReceiver = $rtpReceiver, mediaStreams = $mediaStreams")
    }
}