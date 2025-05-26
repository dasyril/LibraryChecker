package com.example.librarychecker.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.librarychecker.MainActivity;
import com.example.librarychecker.R;

public class ReturnReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "return_reminder_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String bookTitle = intent.getStringExtra("bookTitle");
        String message = "Пожалуйста, верните книгу \"" + bookTitle + "\" в течение 2 дней.";

        // Открывать MainActivity при нажатии
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Напоминания о возврате",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Напоминания о скором возврате книг");
            notificationManager.createNotificationChannel(channel);
        }

        // Само уведомление
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_book_notification)
                .setContentTitle("Напоминание о возврате")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(bookTitle.hashCode(), builder.build());
    }
}
