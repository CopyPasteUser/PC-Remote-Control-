package com.example.appington_city;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


public class ThumbstickView extends View {

    private Paint paint;
    private int thumbstickX, thumbstickY;
    private boolean isThumbstickPressed = false;
    private String vector = "";
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "app_settings";
    private static final String DARK_MODE_KEY = "dark_mode_enabled";

    public ThumbstickView(Context context) {
        super(context);
        init();
    }

    public ThumbstickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();

        sharedPreferences = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        boolean isDarkModeEnabled = sharedPreferences.getBoolean(DARK_MODE_KEY, false);
        if (isDarkModeEnabled) {
            paint.setColor(Color.WHITE);
        } else {
            paint.setColor(Color.BLACK);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        thumbstickX = w / 2;
        thumbstickY = h / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        canvas.drawColor(Color.TRANSPARENT);

        int radius = Math.min(getWidth(), getHeight()) / 2;

        // Den Kreis zeichnen
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, radius, paint);
        canvas.drawCircle(thumbstickX, thumbstickY, radius / 2, paint); // Daumenstick mit halbem Radius zeichnen
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                isThumbstickPressed = true;


                float distanceX = event.getX() - getWidth() / 2;
                float distanceY = event.getY() - getHeight() / 2;


                int vectorX = (int) distanceX;
                int vectorY = (int) distanceY;


                vector = vectorX + "/" + vectorY;


                Log.d("Thumbstick", "VectorX: " + vectorX + ", VectorY: " + vectorY);




                invalidate();
                return true;
            case MotionEvent.ACTION_UP:

                vector = "";




                invalidate();
                return true;
            default:
                return false;
        }
    }
}
