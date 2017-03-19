package com.landenlabs.all_devtool.dialogs;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.ExpandableListView;

/**
 * ListView with a bounce animation when you overscroll.
 * <p/>
 * Found on net:
 *
 * @author internet
 * {@link http://www.phonesdevelopers.com/1714656/}
 * {@link http://stackoverflow.com/questions/7201907/android-listviews-bounce-to-
 * scrollview}
 * <p/>
 * Also code found to scroll background image with scrolling listview.
 * {@link http
 * ://stackoverflow.com/questions/14155881/how-to-get-a-background-image}
 * -scrollable-with-androids-listview
 */
public class BounceExpandableListView extends ExpandableListView {
    private static final int MAX_Y_OVERSCROLL_DISTANCE = 100;

    private int m_maxYOverscrollDistance = MAX_Y_OVERSCROLL_DISTANCE;
    private Bitmap m_backgroundBm;

    public BounceExpandableListView(Context context) {
        super(context);
        initBounceExpandableListView(context, null, 0);
    }

    public BounceExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initBounceExpandableListView(context, attrs, 0);
    }

    public BounceExpandableListView(Context context, AttributeSet attrs,
                                    int defStyle) {
        super(context, attrs, defStyle);
        initBounceExpandableListView(context, attrs, defStyle);
    }

    static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = null;
        if (drawable != null) {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }

        return bitmap;
    }

    private void initBounceExpandableListView(Context context,
                                              AttributeSet attrs, int defStyle) {
        // Get the density of the screen and do some maths with it on the max
        // overscroll distance
        // variable so that you get similar behaviors no matter what the screen
        // size.

        final DisplayMetrics metrics = context.getResources()
                .getDisplayMetrics();
        final float density = metrics.density;

        m_maxYOverscrollDistance = (int) (density * MAX_Y_OVERSCROLL_DISTANCE);

        TypedArray stdAttrs = context.obtainStyledAttributes(attrs,
                new int[]{android.R.attr.background}, // attribute[s] to access
                defStyle, 0); // Style to access

        if (stdAttrs != null) {
            Drawable bgDrawable = stdAttrs.getDrawable(0);
            m_backgroundBm = drawableToBitmap(bgDrawable);
            stdAttrs.recycle();
        }
    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,
                                   int scrollY, int scrollRangeX, int scrollRangeY,
                                   int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {

        // This is where the magic happens, we have replaced the incoming
        // maxOverScrollY with our
        // own custom variable mMaxYOverscrollDistance;
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY,
                scrollRangeX, scrollRangeY, maxOverScrollX,
                m_maxYOverscrollDistance, isTouchEvent);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (m_backgroundBm != null) {

            // Draw scrolled and tiled background.

            int count = getChildCount();
            int top = count > 0 ? getChildAt(0).getTop() : 0;
            int backgroundWidth = m_backgroundBm.getWidth();
            int backgroundHeight = m_backgroundBm.getHeight();
            int width = getWidth();
            int height = getHeight();

            for (int y = top; y < height; y += backgroundHeight) {
                for (int x = 0; x < width; x += backgroundWidth) {
                    canvas.drawBitmap(m_backgroundBm, x, y, null);
                }
            }
        }
        super.dispatchDraw(canvas);
    }

}
