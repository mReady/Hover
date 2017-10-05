package net.mready.hover.demo.windows;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.view.View;

import net.mready.hover.HoverWindow;
import net.mready.hover.demo.R;

public class FullscreenWindow extends BaseWindow {

    @Override
    protected void onCreate(@Nullable Bundle arguments) {
        super.onCreate(arguments);

        addFlags(HoverWindow.FLAG_FOCUSABLE);

        setTheme(R.style.AppTheme);
        setContentView(R.layout.window_fullscreen);

        findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });

        setNotification(new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Hover Demo")
                .setContentText("Fullscreen Window")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build());
    }

}