package com.cmw.eduflow

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "New FCM Token: $token")
        // Here you would save this token to your user's document in Firestore
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // This function is called when a notification is received while the app is open.
        // You can create a custom notification here if you want.
    }
}