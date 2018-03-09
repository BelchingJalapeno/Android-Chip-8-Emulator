package com.belchingjalapeno.androidchip8;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Custom Canvas used to draw the chip-8 {@link com.belchingjalapeno.androidchip8.chip8.graphics.Display}
 */
public class CustomCanvas extends SurfaceView implements SurfaceHolder.Callback {

    private Bitmap bitmap;
    private boolean canDraw = false;

    public CustomCanvas(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    public CustomCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }

    public CustomCanvas(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getHolder().addCallback(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec, widthMeasureSpec / 2);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Bitmap.Config config = Bitmap.Config.ARGB_8888;
        final int virtWidth = 64;
        final int virtHeight = 32;
        bitmap = Bitmap.createBitmap(virtWidth, virtHeight, config);
        canDraw = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (width < height) {
            holder.setFixedSize(width, width / 2);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        canDraw = false;
    }

    /**
     * Does the actual drawing to the Android screen.
     *
     * @param pixels DO NOT MODIFY the pixels of the chip 8 display, being either on or off.
     */
    public void draw(boolean[][] pixels) {
        //if we cant draw no point in copying to the bitmap
        if (!canDraw) {
            return;
        }
        for (int i = 0; i < pixels.length; i++) {
            for (int j = 0; j < pixels[0].length; j++) {
                if (pixels[i][j] == true) {
                    bitmap.setPixel(i, j, Color.argb(255, 200, 200, 200));
                } else {
                    bitmap.setPixel(i, j, Color.argb(255, 25, 25, 25));
                }
            }
        }
        //check before we try to actually draw to the Canvas to prevent errors
        if (!canDraw || getHolder() == null) {
            return;
        }

        Canvas canvas = getHolder().lockCanvas();
        if (canvas == null) {
            return;
        }

        canvas.drawBitmap(bitmap,
                null,
                getHolder().getSurfaceFrame(),
                null);
        getHolder().unlockCanvasAndPost(canvas);
    }
}
