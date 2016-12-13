package net.mready.hover.demo.windows;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import net.mready.hover.HoverWindow;
import net.mready.hover.demo.R;

public class OverlayWindow extends HoverWindow {
    private static final String ACTION_CLOSE = "net.mready.hover.demo.CLOSE_OVERLAY_WINDOW";

    private CloseReceiver closeReceiver;

    @Override
    protected void onCreate(@Nullable Bundle arguments) {
        super.onCreate(arguments);

        addFlags(HoverWindow.FLAG_IGNORE_TOUCH);

        setTheme(R.style.AppTheme);
        setContentView(R.layout.window_overlay);

        closeReceiver = new CloseReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_CLOSE);
        registerReceiver(closeReceiver, intentFilter);

        Intent closeIntent = new Intent(ACTION_CLOSE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, closeIntent, 0);

        setNotification(new NotificationCompat.Builder(this)
                .setContentTitle("Hover Demo")
                .setContentText("Overlay Window")
                .setSmallIcon(R.mipmap.ic_launcher)
                .addAction(0, "Close", pendingIntent)
                .build());
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(closeReceiver);

        super.onDestroy();
    }

    private class CloseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            close();
        }
    }
}
