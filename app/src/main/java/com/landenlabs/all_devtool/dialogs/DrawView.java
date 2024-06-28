/*
 * Copyright (c) 2020 Dennis Lang (LanDen Labs) landenlabs@gmail.com
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * @author Dennis Lang
 * @see https://LanDenLabs.com/
 */

package com.landenlabs.all_devtool.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.landenlabs.all_devtool.shortcuts.util.ObjUtil;

import java.util.ArrayList;

/**
 * Simple class to draw touch events
 * {@link} http://code.tutsplus.com/tutorials/android-sdk-create-a-drawing-app-touch-interaction--mobile-19202
 */
public class DrawView extends View {

    // ---- Timer ----
    private final Handler m_handler = new Handler();
    private boolean m_autoPrune = true;
    private static final int MAX_POINTS = 10000;
    private static final int mDurationMsec = 3000;
    private static final int TIMER_MSEC = 100;
    private final Runnable m_pruneTimerTask = new Runnable() {
        public void run() {
            prunePath();
            m_handler.postDelayed(this, TIMER_MSEC);   // Re-execute after msec.
        }
    };

    private Path m_drawPath;
    private Paint m_drawPaint;

    public interface TouchInfo {
        void onTouchInfo(MotionEvent event);
    }

    private TouchInfo m_touchInfo;

    public DrawView(Context context) {
        super(context);
        init();
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void clear() {
        m_pathPoints.clear();
        prunePath();
    }

     public void setAutoPrune(boolean prune) {
        m_autoPrune = prune;
        if (m_autoPrune) {
            prunePath();
        }
    }

    public boolean getAutoPrune() {
        return m_autoPrune;
    }


    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawPath(m_drawPath, m_drawPaint);
    }

    static class PointfTime extends PointF {
        final long msec;
        PointfTime(float x, float y) {
            this.x = x;
            this.y = y;
            msec = SystemClock.currentThreadTimeMillis();
        }

        static void age(ArrayList<PointfTime> pathPoints) {
            long now = SystemClock.currentThreadTimeMillis();
            for (int idx = pathPoints.size() - 1; idx >= 0; idx--) {
                if (now - pathPoints.get(idx).msec > mDurationMsec) {
                    pathPoints.remove(idx);
                }
            }
        }
    }
    final ArrayList<PointfTime> m_pathPoints = new ArrayList<>();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        m_pathPoints.add(new PointfTime(touchX, touchY));

        if (ObjUtil.notNull(m_touchInfo))
            m_touchInfo.onTouchInfo(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                return true;
            default:
                return false;
        }

        prunePath();
        return true;
    }


    public void setOnTouchInfo(TouchInfo touchInfo) {
        m_touchInfo = touchInfo;
    }
    /**
     * Prune out old path points.
     */
    private void prunePath() {
        if (m_autoPrune || m_pathPoints.size() > MAX_POINTS) {
            PointfTime.age(m_pathPoints);
        }
        m_drawPath.reset();
        if (m_pathPoints.size() > 1) {
            // PointF lpf = new PointF(0, 0);
            for (PointF pf : m_pathPoints) {
                if (m_drawPath.isEmpty())
                    m_drawPath.moveTo(pf.x, pf.y);
                else {
                    m_drawPath.lineTo(pf.x, pf.y);
                    // m_drawPath.quadTo(pf.x, pf.y, (lpf.x + pf.x) / 2, (lpf.y + pf.y) / 2);
                }
                // lpf = pf;
            }
            invalidate();
        }
    }

    private void init() {
        m_drawPath = new Path();

        m_drawPaint = new Paint();
        m_drawPaint.setColor(0xffff0000);
        m_drawPaint.setStrokeWidth(10);
        m_drawPaint.setStyle(Paint.Style.STROKE);

        m_drawPaint.setAntiAlias(true);
        m_drawPaint.setShadowLayer(5 + 4, 0, 0, 0xffffffff);
        setLayerType(LAYER_TYPE_SOFTWARE, m_drawPaint);

        m_handler.postDelayed(m_pruneTimerTask, TIMER_MSEC);
    }
}