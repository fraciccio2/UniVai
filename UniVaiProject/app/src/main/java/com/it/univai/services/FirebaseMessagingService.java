package com.it.univai.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.RemoteMessage;
import com.it.univai.R;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("registrationToken");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            mDatabase.child(token).setValue(user.getUid());
            super.onNewToken(token);
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        if (message.getNotification() != null) {
            String title = message.getNotification().getTitle();
            String body = message.getNotification().getBody();
            String CHANNEL_ID = "MESSAGE";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "MESSAGE NOTIFICATION", NotificationManager.IMPORTANCE_HIGH);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
            Notification.Builder builder = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setSmallIcon(R.mipmap.ic_launcher_logo)
                    .setAutoCancel(true);
            NotificationManagerCompat.from(this).notify(1, builder.build());
        }
        super.onMessageReceived(message);
    }
}
