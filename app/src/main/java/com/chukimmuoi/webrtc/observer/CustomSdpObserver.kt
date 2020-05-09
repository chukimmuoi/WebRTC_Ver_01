package com.chukimmuoi.webrtc.observer

import android.util.Log
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

open class CustomSdpObserver: SdpObserver {
    
    val TAG = CustomSdpObserver::class.java.simpleName

    override fun onSetFailure(s: String) {
        Log.e(TAG, "onSetFailure s = $s")
    }

    override fun onSetSuccess() {
        Log.e(TAG, "onSetSuccess")
    }

    override fun onCreateSuccess(sessionDescription: SessionDescription) {
        Log.e(TAG, "onCreateSuccess sessionDescription = $sessionDescription")
    }

    override fun onCreateFailure(s: String) {
        Log.e(TAG, "onCreateFailure s = $s")
    }
}