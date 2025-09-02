package com.carlitoswy.flashmeet.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.carlitoswy.flashmeet.R
import com.carlitoswy.flashmeet.core.deeplink.DeeplinkBuilder
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class FlashMessagingService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_ID = "event_channel"
        private const val CHANNEL_NAME = "Eventos"
        private const val CHANNEL_DESC = "Notificaciones de eventos de FlashMeet"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: envía el token a tu backend si es necesario
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Solo procesamos data messages
        val data = message.data
        if (data.isEmpty()) return

        val type = data["type"] ?: ""
        if (type != "event") return

        val eventId = data["eventId"] ?: return
        val lat = data["lat"]?.toDoubleOrNull()
        val lon = data["lon"]?.toDoubleOrNull()
        val title = data["title"] ?: getString(R.string.app_name)
        val body = data["body"] ?: "Ver evento"

        // Intent principal → HTTPS (App Links)
        val httpsUrl = DeeplinkBuilder.eventHttps(
            id = eventId,
            host = DeeplinkBuilder.PROD_HOST, // "123myway.com"
            shortPath = true,                 // usa /e/{id}
            lat = lat, lon = lon
        )
        val contentIntent = PendingIntent.getActivity(
            this,
            /* requestCode = */ Random.nextInt(),
            Intent(Intent.ACTION_VIEW, httpsUrl.toUri()).apply {
                // Sugerencia: no fijar package para dejar que App Links resuelva al mejor handler
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag()
        )

        // Acción fallback → custom scheme (abre siempre la app)
        val customUri = DeeplinkBuilder.eventCustomScheme(eventId).toUri()
        val fallbackIntent = PendingIntent.getActivity(
            this,
            Random.nextInt(),
            Intent(Intent.ACTION_VIEW, customUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag()
        )

        ensureChannel()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // ⛳️ pon tu propio vector; usa ic_launcher si no tienes
            .setColor(getColorCompat(R.color.purple_500))
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentIntent)
            .addAction(
                R.drawable.ic_notification, // o un ícono "open_in_new"
                "Abrir en la app",
                fallbackIntent
            )
            .build()

        NotificationManagerCompat.from(this)
            .notify(Random.nextInt(10_000, 99_999), notification)
    }

    private fun ensureChannel() {
        val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
            mgr.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                    description = CHANNEL_DESC
                    enableLights(true)
                    lightColor = Color.MAGENTA
                    enableVibration(true)
                }
            )
        }
    }

    private fun immutableFlag(): Int =
        PendingIntent.FLAG_IMMUTABLE

    private fun getColorCompat(colorRes: Int): Int =
        runCatching { getColor(colorRes) }.getOrDefault(Color.WHITE)
}
