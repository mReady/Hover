package net.mready.hover.demo.windows;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;

import net.mready.hover.HoverWindow;

class BaseWindow extends HoverWindow {

    protected static final String NOTIFICATION_CHANNEL_ID = "default";

    @Override
    protected void onCreate(@Nullable Bundle arguments) {
        super.onCreate(arguments);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "Floating windows", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

}