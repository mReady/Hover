package net.mready.hover;

import android.app.Notification;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.ContextThemeWrapper;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Scroller;


public class HoverWindow extends ContextThemeWrapper {

    /**
     * This window will never receive touch events
     */
    public static final int FLAG_IGNORE_TOUCH = 1 << 1;

    /**
     * Allow the window to capture key and input events
     */
    public static final int FLAG_FOCUSABLE = 1 << 2;

    /**
     * Allow the user to drag the window to a different position on screen
     */
    public static final int FLAG_MOVABLE = 1 << 3;

    /**
     * Allow the user to resize the window using scale gestures
     */
    public static final int FLAG_RESIZABLE = 1 << 4;

    /**
     * Receives focus events when the user taps on the window, the window
     * will not receive input focus unless {@link #FLAG_FOCUSABLE} is also set
     */
    public static final int FLAG_FOCUS_ON_TAP = 1 << 5;

    /**
     * As long as this window is visible to the user, keep
     * the device's screen turned on and bright.
     */
    public static final int FLAG_KEEP_SCREEN_ON = 1 << 6;


    private HoverService windowService;

    /*package*/ View contentView;
    /*package*/ WindowContentView windowView;
    /*package*/ WindowLayoutParams windowLayoutParams;

    /*package*/ boolean attached = false;
    /*package*/ boolean started = false;
    /*package*/ int id;

    private int flags;
    private Bundle arguments;


    public HoverWindow() {
        super(null, 0);
    }


    /*package*/ void performCreate(HoverService service, Bundle arguments) {
        attachBaseContext(service.getBaseContext());

        this.windowService = service;

        this.windowView = createWindowContentView();
        this.windowLayoutParams = new WindowLayoutParams();

        this.arguments = arguments;

        onCreate(arguments);
    }

    protected void onCreate(@Nullable Bundle arguments) {
    }

    /*package*/ void performStart() {
        started = true;
        onStart();
    }

    protected void onStart() {
    }

    /*package*/ void performStop() {
        started = false;
        onStop();
    }

    protected void onStop() {
    }

    /*package*/ void performDestroy() {
        onDestroy();
    }

    protected void onDestroy() {
    }

    protected void onFocusChanged(boolean hasFocus) {
    }

    protected boolean onBackPressed() {
        close();
        return true;
    }

    protected void onNewArguments(@Nullable Bundle arguments) {
    }

    public void onConfigurationChanged(Configuration newConfig) {
    }


    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }


    public int getId() {
        return id;
    }

    protected void setArguments(Bundle arguments) {
        this.arguments = arguments;
    }

    public Bundle getArguments() {
        return arguments;
    }

    public boolean isAttached() {
        return attached;
    }


    protected void setContentView(@LayoutRes int layoutId) {
        setContentView(LayoutInflater.from(this).inflate(layoutId, windowView, false));
    }

    protected void setContentView(View view) {
        contentView = view;

        windowView.removeAllViews();
        windowView.addView(view);

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) view.getLayoutParams();
        windowLayoutParams.setFrom(lp);
        lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;

        windowView.requestLayout();
    }

    public <T extends View> T findViewById(@IdRes int id) {
        //noinspection unchecked
        return (T) windowView.findViewById(id);
    }

    protected ViewGroup getWindowView() {
        return windowView;
    }

    protected void setBackgroundColor(@ColorInt int color) {
        windowView.setBackgroundColor(color);
    }

    protected void setWindowSize(int width, int height) {
        windowLayoutParams.width = width;
        windowLayoutParams.height = height;
        windowView.scaleListener.aspectRatio = 0;

        updateLayoutParams();
    }

    protected void setWindowPosition(int x, int y) {
        windowLayoutParams.x = x;
        windowLayoutParams.y = y;

        updateLayoutParams();
    }

    public int getWindowWidth() {
        return windowView.getWidth();
    }

    public int getWindowHeight() {
        return windowView.getHeight();
    }

    public int getWindowX() {
        return windowLayoutParams.x;
    }

    public int getWindowY() {
        return windowLayoutParams.y;
    }

    public void bringToFront() {
        windowService.bringToFront(this);
    }


    protected void setNotification(Notification notification) {
        windowService.setNotification(this, notification);
    }

    protected void removeNotification() {
        windowService.setNotification(this, null);
    }


    public void requestWindowFocus() {
        if (hasFlags(FLAG_FOCUSABLE)) {
            windowLayoutParams.flags &= ~WindowLayoutParams.FLAG_NOT_FOCUSABLE;
        } else {
            windowLayoutParams.flags |= WindowLayoutParams.FLAG_NOT_FOCUSABLE;
        }

        updateLayoutParams();

        onFocusChanged(true);
    }

    public void clearWindowFocus() {
        if (hasInputFocus()) {
            //clear focus from all children
            windowView.requestFocus();
        }

        windowLayoutParams.flags |= WindowLayoutParams.FLAG_NOT_FOCUSABLE;
        updateLayoutParams();

        onFocusChanged(false);
    }

    public boolean hasInputFocus() {
        return (windowLayoutParams.flags & ~WindowLayoutParams.FLAG_NOT_FOCUSABLE) == windowLayoutParams.flags;
    }

    public void close() {
        windowService.removeWindow(this);
    }


    public void addFlags(int mask) {
        flags |= mask;
        updateWindowFlags();
    }

    public void clearFlags(int mask) {
        flags &= ~mask;
        updateWindowFlags();
    }

    private boolean hasFlags(int mask) {
        return (flags & mask) == mask;
    }


    private WindowContentView createWindowContentView() {
        WindowContentView windowContentView = new WindowContentView(this);
        windowContentView.setBackgroundColor(Color.TRANSPARENT);

        return windowContentView;
    }

    private void updateWindowFlags() {
        int windowFlags = windowLayoutParams.flags;

        if (hasFlags(FLAG_IGNORE_TOUCH)) {
            windowFlags |= WindowLayoutParams.FLAG_NOT_TOUCHABLE;
        } else {
            windowFlags &= ~WindowLayoutParams.FLAG_NOT_TOUCHABLE;
        }

        if (hasFlags(FLAG_FOCUS_ON_TAP)) {
            windowFlags |= WindowLayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        } else {
            windowFlags &= ~WindowLayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        }

        if (hasFlags(FLAG_FOCUSABLE)) {
            windowFlags &= ~WindowLayoutParams.FLAG_NOT_FOCUSABLE;
        } else {
            windowFlags |= WindowLayoutParams.FLAG_NOT_FOCUSABLE;
        }

        if (hasFlags(FLAG_KEEP_SCREEN_ON)) {
            windowFlags |= WindowLayoutParams.FLAG_KEEP_SCREEN_ON;
        } else {
            windowFlags &= ~WindowLayoutParams.FLAG_KEEP_SCREEN_ON;
        }

        windowLayoutParams.flags = windowFlags;
        updateLayoutParams();
    }

    private void updateLayoutParams() {
        windowService.updateWindow(this);
    }


    protected boolean onWindowResizeBegin() {
        return true;
    }

    protected void onWindowResizeEnd() {
    }

    protected void onWindowSizeChanged(int width, int height) {
    }


    private class WindowContentView extends FrameLayout {
        private float lastTouchX;
        private float lastTouchY;

        private boolean dragging = false;

        final ScaleGestureDetector scaleDetector;
        final ScaleListener scaleListener;
        final GestureDetector gestureDetector;
        final Scroller scroller;

        public WindowContentView(Context context) {
            super(context);

            setFocusableInTouchMode(true);

            scroller = new Scroller(getContext());
            scroller.setFriction(0.1f);

            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    animatePosition(velocityX, velocityY);
                    return true;
                }
            });

            scaleListener = new ScaleListener();
            scaleDetector = new ScaleGestureDetector(context, scaleListener);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                this.scaleDetector.setQuickScaleEnabled(false);
            }
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent event) {
            int action = event.getActionMasked();

            if (action == MotionEvent.ACTION_DOWN) {
                if (!hasInputFocus() && hasFlags(FLAG_FOCUS_ON_TAP)) {
                    requestWindowFocus();
                }
            }

            return super.onInterceptTouchEvent(event);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            int action = event.getActionMasked();

            if (action == MotionEvent.ACTION_OUTSIDE) {
                if (hasFlags(FLAG_FOCUS_ON_TAP)) {
                    clearWindowFocus();
                }
                return false;
            }


            if (HoverWindow.this.onTouchEvent(event)) {
                return true;
            }

            if (hasFlags(FLAG_MOVABLE)) {
                MotionEvent globalEvent = MotionEvent.obtain(event);
                globalEvent.offsetLocation(windowLayoutParams.x, windowLayoutParams.y);
                gestureDetector.onTouchEvent(globalEvent);
                globalEvent.recycle();
            }

            if (hasFlags(FLAG_RESIZABLE)) {
                scaleDetector.onTouchEvent(event);
            }

            if (scaleDetector.isInProgress()) {
                dragging = false;

                return true;
            }

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    if (hasFlags(FLAG_MOVABLE)) {
                        scroller.forceFinished(true);
                        lastTouchX = event.getRawX();
                        lastTouchY = event.getRawY();

                        dragging = true;
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (!dragging) {
                        break;
                    }

                    if (event.getPointerCount() != 1) {
                        break;
                    }

                    int maxX = getResources().getDisplayMetrics().widthPixels - windowView.getWidth();
                    int maxY = getResources().getDisplayMetrics().heightPixels - windowView.getHeight();

                    windowLayoutParams.x += event.getRawX() - lastTouchX;
                    windowLayoutParams.y += event.getRawY() - lastTouchY;

                    windowLayoutParams.x = Math.min(Math.max(0, windowLayoutParams.x), maxX);
                    windowLayoutParams.y = Math.min(Math.max(0, windowLayoutParams.y), maxY);

                    lastTouchX = event.getRawX();
                    lastTouchY = event.getRawY();

                    updateLayoutParams();
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    dragging = false;

                    break;
            }

            return true;
        }

        private void animatePosition(float velocityX, float velocityY) {
            int x = windowLayoutParams.x;
            int y = windowLayoutParams.y;
            int maxX = getResources().getDisplayMetrics().widthPixels - windowView.getWidth();
            int maxY = getResources().getDisplayMetrics().heightPixels - windowView.getHeight();

            scroller.fling(x, y, (int) velocityX, (int) velocityY, 0, maxX, 0, maxY);

            postInvalidateOnAnimation();
        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_UP
                    && event.getKeyCode() == KeyEvent.KEYCODE_BACK
                    && onBackPressed()) {
                return true;
            }

            return super.dispatchKeyEvent(event);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            onWindowSizeChanged(w, h);
        }

        @Override
        protected void dispatchDraw(Canvas canvas) {
            if (scroller.computeScrollOffset()) {
                windowLayoutParams.x = scroller.getCurrX();
                windowLayoutParams.y = scroller.getCurrY();
                updateLayoutParams();
                postInvalidateOnAnimation();
            }

            super.dispatchDraw(canvas);
        }
    }

    private static class WindowLayoutParams extends WindowManager.LayoutParams {
        WindowLayoutParams() {
            super(WRAP_CONTENT, WRAP_CONTENT,
                    TYPE_PHONE,
                    FLAG_NOT_TOUCH_MODAL | FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

            gravity = Gravity.TOP | Gravity.LEFT;
            softInputMode = SOFT_INPUT_ADJUST_RESIZE;
        }

        void setFrom(FrameLayout.LayoutParams lp) {
            width = lp.width;
            height = lp.height;

            x = lp.leftMargin;
            y = lp.topMargin;

            if (lp.gravity != -1) {
                gravity = lp.gravity;
            }
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        double aspectRatio;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return onWindowResizeBegin();
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            onWindowResizeEnd();
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            //Lock-in the current width and height when first scaling
            if (aspectRatio == 0) {
                windowLayoutParams.width = windowView.getWidth();
                windowLayoutParams.height = windowView.getHeight();
                aspectRatio = windowLayoutParams.width / (double) windowLayoutParams.height;
            }

            double scaleFactor = detector.getScaleFactor();

            int maxSize = Math.min(getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);

            double newWidth;
            double newHeight;

            int minHeight = contentView.getMinimumHeight();
            int minWidth = contentView.getMinimumWidth();

            if (aspectRatio > 1) {
                newWidth = Math.max(Math.min(windowView.getWidth() * scaleFactor, maxSize), minWidth);
                newHeight = newWidth / aspectRatio;
                if (newHeight < minHeight) {
                    newHeight = minHeight;
                    newWidth = newHeight * aspectRatio;
                }
            } else {
                newHeight = Math.max(Math.min(windowView.getHeight() * scaleFactor, maxSize), minHeight);
                newWidth = newHeight * aspectRatio;
                if (newWidth < minWidth) {
                    newWidth = minWidth;
                    newHeight = newWidth / aspectRatio;
                }
            }

            int wDiffHalf = (int) ((newWidth - windowLayoutParams.width) / 2);
            int hDiffHalf = (int) ((newHeight - windowLayoutParams.height) / 2);

            windowLayoutParams.width += wDiffHalf * 2;
            windowLayoutParams.height += hDiffHalf * 2;

            windowLayoutParams.x = Math.max(0, windowLayoutParams.x - wDiffHalf);
            windowLayoutParams.y = Math.max(0, windowLayoutParams.y - hDiffHalf);

            updateLayoutParams();

            return true;
        }
    }
}
