package net.mready.hover.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import net.mready.hover.Hover;
import net.mready.hover.HoverWindow;
import net.mready.hover.demo.windows.FocusableWindow;
import net.mready.hover.demo.windows.FullscreenWindow;
import net.mready.hover.demo.windows.OverlayWindow;
import net.mready.hover.demo.windows.ResizeableWindow;
import net.mready.hover.demo.windows.SimpleWindow;


public class MainActivity extends AppCompatActivity {
    private static final int WINDOW_SIMPLE_ID = 1;
    private static final int WINDOW_RESIZEABLE_ID = 2;
    private static final int WINDOW_FOCUSABLE_ID = 3;
    private static final int WINDOW_FULLSCREEN_ID = 4;
    private static final int WINDOW_OVERLAY_ID = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_simple_window).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWindow(WINDOW_SIMPLE_ID, SimpleWindow.class);
            }
        });

        findViewById(R.id.btn_resizable_window).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWindow(WINDOW_RESIZEABLE_ID, ResizeableWindow.class);
            }
        });

        findViewById(R.id.btn_focusable_window).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWindow(WINDOW_FOCUSABLE_ID, FocusableWindow.class);
            }
        });

        findViewById(R.id.btn_fullscreen_window).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWindow(WINDOW_FULLSCREEN_ID, FullscreenWindow.class);
            }
        });

        findViewById(R.id.btn_overlay_window).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWindow(WINDOW_OVERLAY_ID, OverlayWindow.class);
            }
        });
    }

    private void openWindow(int id, Class<? extends HoverWindow> window) {
        //APKs not installed via the Google Play Store require explicit permission to
        //display overlay windows
        if (!Hover.hasOverlayPermission(this)) {
            Hover.requestOverlayPermission(this, 0);
        } else {
            Hover.showWindow(this, id, window);
        }
    }
}
