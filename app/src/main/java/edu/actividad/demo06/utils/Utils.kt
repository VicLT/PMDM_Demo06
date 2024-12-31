package edu.actividad.demo06.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import edu.actividad.demo06.R

private const val CHANNEL_ID = "visited_cities_channel"
private const val NOTIFICATION_ID = 1

/**
 * Crea un canal de notificación para las ciudades visitadas.
 */
@SuppressLint("ObsoleteSdkInt")
fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Actualización de Ciudades Visitadas"
        val descriptionText = "Notifica cuando se actualizan las visitas a las ciudades"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

/**
 * Lanza una notificación para informar sobre la actualización de ciudades visitadas.
 */
@SuppressLint("MissingPermission", "NotificationPermission")
fun sendNotification(context: Context) {
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle("Ciudades visitadas actualizadas")
        .setContentText("Las visitas a las ciudades se han actualizado.")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Para API 27, esto funciona

    with(NotificationManagerCompat.from(context)) {
        notify(NOTIFICATION_ID, builder.build())
    }
}