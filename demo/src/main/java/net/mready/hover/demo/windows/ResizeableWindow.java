package net.mready.hover.demo.windows;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.TextView;

import net.mready.hover.HoverWindow;
import net.mready.hover.demo.R;

import java.util.Locale;

public class ResizeableWindow extends BaseWindow {

    private TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle arguments) {
        super.onCreate(arguments);

        addFlags(HoverWindow.FLAG_MOVABLE
                | HoverWindow.FLAG_RESIZABLE);

        setTheme(R.style.AppTheme);
        setContentView(R.layout.window_simple);

        findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });
        textView = findViewById(R.id.tv_hello);

        setNotification(new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Hover Demo")
                .setContentText("Resizeable Window")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build());
    }

    @Override
    protected void onWindowSizeChanged(int width, int height) {
        super.onWindowSizeChanged(width, height);

        textView.setText(String.format(Locale.US, "%d x %d", width, height));
    }

}