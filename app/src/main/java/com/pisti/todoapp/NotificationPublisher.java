package com.pisti.todoapp;
import android.app.Notification ;
import android.app.NotificationChannel ;
import android.app.NotificationManager ;
import android.content.BroadcastReceiver ;
import android.content.Context ;
import android.content.Intent ;
import static com.pisti.todoapp.MainActivity. CHANNEL_ID ;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationPublisher extends BroadcastReceiver {
    public static String NOTIFICATION_ID = "notification-id" ;
    public static String NOTIFICATION = "notification" ;
    public void onReceive (Context context , Intent intent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "CHANNEL_ID#1")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("Hello")
                .setContentText("Ez itt a lerívás")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationm = NotificationManagerCompat.from(context);
        notificationm.notify(1 , builder.build()) ;
    }
}