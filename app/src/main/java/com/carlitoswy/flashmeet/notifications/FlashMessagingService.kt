package com.carlitoswy.flashmeet.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.carlitoswy.flashmeet.MainActivity
import com.carlitoswy.flashmeet.R
import com.carlitoswy.flashmeet.utils.PendingEventManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * ✅ Servicio para recibir mensajes FCM y mostrar notificaciones con deep link a un evento.
 */
class FlashMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "📩 Push recibido: ${remoteMessage.data}")

        val eventId = remoteMessage.data["eventId"] ?: return
        val title = remoteMessage.data["title"] ?: "Nuevo Evento"
        val description = remoteMessage.data["description"] ?: "Tienes un nuevo evento cerca."

        // ✅ Guardamos evento pendiente (para cold start)
        PendingEventManager.savePendingEvent(this, eventId)

        // ✅ Mostrar notificación local
        showNotification(eventId, title, description)
    }

    override fun onNewToken(token: String) {
        Log.d("FCM", "🔥 Nuevo token FCM: $token")
        // Aquí podrías enviar el token al backend
    }

    private fun showNotification(eventId: String, title: String, message: String) {
        val channelId = "event_push_channel"

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Eventos FlashMeet", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }

        // ✅ Intent para abrir MainActivity → PendingEventManager redirige al evento
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("eventId", eventId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            eventId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_flashmeet_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(this).notify(eventId.hashCode(), notification)
    }
}
