package net.mready.hover;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.WindowManager;

import java.util.HashMap;
import java.util.Map;

public class HoverService extends Service {
    private static final String NOTIFICATION_TAG = "HoverWindow";

    //Determined by a fair dice roll, is there any way to make sure this is unique?
    private static final int FOREGROUND_NOTIFICATION_ID = 234120;

    /*package*/ static final String ACTION_SHOW = "ACTION_SHOW";
    /*package*/ static final String ACTION_CLOSE = "ACTION_CLOSE";
    /*package*/ static final String ACTION_CLOSE_ALL = "ACTION_CLOSE_ALL";

    /*package*/ static final String EXTRA_WINDOW_ID = "EXTRA_WINDOW_ID";
    /*package*/ static final String EXTRA_WINDOW_CLASS = "EXTRA_WINDOW_CLASS";
    /*package*/ static final String EXTRA_ARGUMENTS = "EXTRA_ARGUMENTS";

    private WindowManager windowManager;
    private PowerManager powerManager;
    private KeyguardManager keyguardManager;
    private NotificationManager notificationManager;

    private ScreenReceiver screenReceiver;

    //it's easier to iterate through values in a Map<>
    @SuppressLint("UseSparseArrays")
    private final Map<Integer, HoverWindow> windows = new HashMap<>();

    private final SparseArray<Notification> activeNotifications = new SparseArray<>();

    private int foregroundNotificationWindowId;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);

        screenReceiver = new ScreenReceiver();
        registerReceiver(screenReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        removeAllWindows();
        unregisterReceiver(screenReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        switch (intent.getAction()) {
            case ACTION_SHOW:
                //noinspection unchecked
                addWindow(intent.getIntExtra(EXTRA_WINDOW_ID, 0),
                        (Class<? extends HoverWindow>) intent.getSerializableExtra(EXTRA_WINDOW_CLASS),
                        intent.getBundleExtra(EXTRA_ARGUMENTS));
                break;

            case ACTION_CLOSE:
                removeWindow(intent.getIntExtra(EXTRA_WINDOW_ID, 0));
                break;

            case ACTION_CLOSE_ALL:
                removeAllWindows();
                break;
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        for (HoverWindow window : windows.values()) {
            window.onConfigurationChanged(newConfig);
        }
    }

    private HoverWindow createWindow(int id, Class<? extends HoverWindow> windowClass, Bundle arguments) {
        try {
            HoverWindow window = windowClass.newInstance();
            window.id = id;
            windows.put(id, window);
            window.performCreate(this, arguments);

            return window;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addWindow(int id, Class<? extends HoverWindow> windowClass, Bundle arguments) {
        HoverWindow window = windows.get(id);

        if (window == null) {
            window = createWindow(id, windowClass, arguments);
        } else if (!windowClass.equals(window.getClass())) {
            throw new IllegalStateException(
                    String.format("Already have a window (%s) with id %d", window.getClass(), id));
        } else {
            window.onNewArguments(arguments);
        }

        showWindow(window);
    }

    private void removeWindow(int id) {
        HoverWindow window = windows.get(id);

        if (window != null) {
            removeWindow(window);
        }
    }

    private void checkWindow(HoverWindow window) {
        if (!windows.containsValue(window)) {
            throw new IllegalStateException("Service does not contain the window");
        }
    }

    private void showWindow(HoverWindow window) {
        checkWindow(window);

        if (window.attached) {
            return;
        }

        windowManager.addView(window.windowView, window.windowLayoutParams);
        window.attached = true;

        //noinspection deprecation
        if (powerManager.isScreenOn() && !keyguardManager.inKeyguardRestrictedInputMode()) {
            window.performStart();
        }
    }

    /*package*/ void updateWindow(HoverWindow window) {
        checkWindow(window);

        if (!window.attached) {
            return;
        }

        windowManager.updateViewLayout(window.windowView, window.windowLayoutParams);
    }

    private void hideWindow(HoverWindow window) {
        checkWindow(window);

        if (!window.attached) {
            return;
        }

        windowManager.removeView(window.windowView);
        window.attached = false;
        window.performStop();
    }

    /*package*/ void removeWindow(HoverWindow window) {
        checkWindow(window);

        setNotification(window, null);

        hideWindow(window);
        window.performDestroy();

        windows.remove(window.id);
    }

    private void removeAllWindows() {
        for (HoverWindow window : windows.values()) {
            removeWindow(window);
        }
    }

    /*package*/ void bringToFront(HoverWindow window) {
        checkWindow(window);

        if (!window.attached) {
            return;
        }

        windowManager.removeView(window.windowView);
        windowManager.addView(window.windowView, window.windowLayoutParams);
    }

    /*package*/ void setNotification(HoverWindow window, Notification notification) {
        if (notification == null) {
            //remove the current notification for this window
            activeNotifications.remove(window.id);
            if (foregroundNotificationWindowId == window.id) {
                //if this window's notification was the service foreground notification, remove it
                //and set another one as foreground if any is available
                foregroundNotificationWindowId = 0;
                if (activeNotifications.size() > 0) {
                    int newWindowId = activeNotifications.keyAt(0);
                    //remove the notification in order to set it as foreground
                    notificationManager.cancel(NOTIFICATION_TAG, newWindowId);
                    setNotification(windows.get(newWindowId), activeNotifications.get(newWindowId));
                } else {
                    //no new notification is available, so we stop running as foreground
                    stopForeground(true);
                }
            } else {
                notificationManager.cancel(NOTIFICATION_TAG, window.id);
            }
        } else {
            activeNotifications.put(window.id, notification);
            if (foregroundNotificationWindowId == 0) {
                //if we have no foreground notification use this one
                foregroundNotificationWindowId = window.id;
                startForeground(FOREGROUND_NOTIFICATION_ID, notification);
            } else {
                //we already have a foreground notification, set this one are ongoing
                notification.flags |= Notification.FLAG_ONGOING_EVENT;
                notificationManager.notify(NOTIFICATION_TAG, window.id, notification);
            }
        }
    }

    private class ScreenReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Intent.ACTION_SCREEN_OFF:
                    for (HoverWindow window : windows.values()) {
                        if (window.attached) {
                            window.performStop();
                        }
                    }
                    break;
                case Intent.ACTION_SCREEN_ON:
                    if (keyguardManager.inKeyguardRestrictedInputMode()) {
                        break;
                    }
                    //fallthrough
                case Intent.ACTION_USER_PRESENT:
                    for (HoverWindow window : windows.values()) {
                        if (window.attached && !window.started) {
                            window.performStart();
                        }
                    }
                    break;
            }
        }
    }
}
