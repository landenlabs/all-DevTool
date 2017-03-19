package com.landenlabs.all_devtool.util;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.WindowManager;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.TableLayout;

import com.landenlabs.all_devtool.DevToolActivity;
import com.landenlabs.all_devtool.GlobalInfo;
import com.landenlabs.all_devtool.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

// import com.google.android.gms.maps.model.LatLng;

public class Utils {
    static class LatLng {
        double latitude;
        double longitude;
    }

    // Logger - set to LLog.DBG to only log in Debug build, use LLog.On for always log.
    private static final LLog s_log = LLog.DBG;

    // =============================================================================================
    // Theme

    public final static int sNoThemeIdx = -1;
    public final static int sTheme_00  = 0; //android:style/Theme 
    public final static int sTheme_01  = 1; //android:style/Theme.Black
    public final static int sTheme_02  = 2; //android:style/Theme.WithActionBa
    public final static int sTheme_03  = 3; //android:style/Theme.Translucent
    public final static int sTheme_04  = 4; //android:style/Theme.Wallpaper
    public final static int sTheme_05  = 5; //android:style/Theme.Holo.Light
    public final static int sTheme_06  = 6; //android:style/Theme.Holo
    public final static int sTheme_07  = 7; //android:style/Theme.Holo.Light.DarkActionBar
    public final static int sTheme_08  = 8; //android:style/Theme.Holo.Dialog
    public final static int sTheme_09  = 9; //android:style/Theme.Dialog
    public final static int sTheme_10  = 10; //android:style/Theme.Panel
    public final static int sTheme_11  = 11; //android:style/Theme.Material
    public final static int sTheme_12  = 12; //android:style/Theme.Material.Light
    public final static int sTheme_13  = 13; //android:style/Theme.Material.Light.DarkActionBar
    public final static int sTheme_14  = 14;
    public final static int sTheme_15  = 15;
    public final static int sTheme_16  = 16;
    public final static int sTheme_17  = 17;


    private static int sThemeIdx = sNoThemeIdx;

    public static int getThemeIdx() {
        return (sThemeIdx == sNoThemeIdx) ? sTheme_06 : sThemeIdx;
    }

    /**
     * Set the theme of the Activity, and restart it by creating a new Activity
     * of the same type.
     */
    public static void changeToTheme(Activity activity, int themeIdx, String themeName) {
        GlobalInfo.s_globalInfo.themeName = themeName;
        if (getThemeIdx() != themeIdx) {
            sThemeIdx = themeIdx;
            // assert(R.style.Theme_10 == R.style.Theme_00 + 10);
            GlobalInfo.s_globalInfo.mainFragActivity.recreate();
        }
    }

    /**
     * Set the theme of the activity, according to the configuration.
     */
    public static void onActivityCreateSetTheme(Activity activity) {
        if (sThemeIdx != sNoThemeIdx) {
            activity.setTheme(GlobalInfo.getThemeResId(sThemeIdx));  // R.style.Theme_00 + sThemeId);
            GlobalInfo.grabThemeSetings(activity);
        }
    }


    // =============================================================================================
    // Misc

    public static <E> E last(E[] array) {
        return (array == null || array.length == 0) ? null : array[array.length-1];
    }

    // =============================================================================================
    // File System

    public static class DirSizeCount  {
        public long size = 0;
        public long count = 0;

        DirSizeCount add(DirSizeCount rhs) {
            DirSizeCount lhs = new DirSizeCount();
            lhs.count = this.count + rhs.count;
            lhs.size = this.size + rhs.size;
            return lhs;
        }
    }

    public static DirSizeCount getDirectorySize(File  dir) {

        DirSizeCount dirSizeCount = null;
        if (dir != null)
        try {
            File[] files = dir.listFiles();

            if (files != null) {
                dirSizeCount = new DirSizeCount();
                for (File file : files) {
                    try {
                        if (file.isFile()) {
                            dirSizeCount.size += file.length();
                            dirSizeCount.count++;
                        } else if (file.isDirectory()) {
                            dirSizeCount = dirSizeCount.add(getDirectorySize(file));
                        }
                    } catch (Exception ex) {
                    }
                }
            }
        } catch (Exception ex) {
        }

        return dirSizeCount;
    }

    /**
     * Delete all files in directory tree.
     *
     * @param dirFile
     * @return
     */
    public static List<String> deleteFiles(File dirFile) {
        File[] files = dirFile.listFiles();
        List<String> deletedFiles = new ArrayList<String>();

        if (files != null) {
            for (File file : files) {
                try {
                    if (file.isFile()) {
                        try {
                            if (file.delete()) {
                                deletedFiles.add(file.getName());
                            }
                        } catch (Exception ex) {
                            Log.d("deleteFiles", ex.getMessage());
                        }
                    } else if (file.isDirectory()) {
                        deletedFiles.addAll(deleteFiles(file));
                    }
                } catch (Exception ex) {
                }
            }
        }

        return deletedFiles;
    }

    private static <typ> String joinPath(typ path1, typ path2) {
        return new File(path1.toString(), path2.toString()).toString();
    }

    // =============================================================================================
    // Display

    /**
     * @return Convert dp to px, return px
     */
    public static float dpToPx(int dp) {
        return (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * @return Convert px to dp, return dp
     */
    public static float pxToDp(int px) {
        return (px / Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * @return DisplayMetrics
     */
    public static DisplayMetrics getDisplayMetrics(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getMetrics(dm);
        return dm;
    }

    // =============================================================================================
    // Math - earth

    public static final float EARTH_RADIUS_KM = 6371.009f; // kilometers
    /**
     * Spherical trigonometry laws allow calculating distance between 2 points on a sphere. The
     * shortest distance between points A and B on Earth surface (assumed its has spherical form) is
     * determined by formula:<br>
     * <code>
     * d = arccos {sin(Φa)*sin(Φb) + cos(Φa)*cos(Φb)*cos(Λa - Λb)}
     * </code><br>
     * where <code>Φa</code> and <code>Φb</code> - latitudes,<br>
     * <code>Λa</code>, <code>Λb</code> - longitudes of appropriate points,<br>
     * <code>d</code> - distance between the points measured in radians with the length of big arc
     * of Earth globe. <br>
     * Distance between points measured in kilometers can be calculated with formula: <br>
     * <code>
     * L = d*R
     * </code><br>
     * where <code>R</code> = 6371 km - the average radius of Earth globe.<br>
     * In order to calculate distance between points located in different hemispheres
     * (northern-southern or eastern-western hemispheres) the appropriate values of
     * latitude/longitude should have different signs (+/-).
     *
     * @param g1Position
     *            first LatLng object
     * @param g2Position
     *            second LatLng object
     * @return distance in kilometers between provided positions.
     */
    public static double kilometersBetweenLatLng(LatLng g1Position, LatLng g2Position) {
        return EARTH_RADIUS_KM
                * Math.acos(Math.sin(Math.toRadians(g1Position.latitude)) * Math.sin(Math.toRadians(g2Position.latitude))
                + Math.cos(Math.toRadians(g1Position.latitude)) * Math.cos(Math.toRadians(g2Position.latitude))
                * Math.cos(Math.toRadians(g2Position.longitude - g1Position.longitude)));
    }

    public static double kilometersBetweenLocations(Location g1Position, Location g2Position) {
        return EARTH_RADIUS_KM
                * Math.acos(Math.sin(Math.toRadians(g1Position.getLatitude())) * Math.sin(Math.toRadians(g2Position.getLatitude()))
                + Math.cos(Math.toRadians(g1Position.getLatitude())) * Math.cos(Math.toRadians(g2Position.getLatitude()))
                * Math.cos(Math.toRadians(g2Position.getLongitude() - g1Position.getLongitude())));
    }

    // =============================================================================================
    // Colors

    /**
     * Blend two colors
     *
     * @param color1 24bit colors rgb
     * @param color2 24bit colors rgb
     * @param ratio  Percent of color1 to use.
     * @return 24bit color = c1 * r + c2 * (1-r)
     */
    public static int blend(int color1, int color2, float ratio) {
        final float inverseRation = 1f - ratio;
        float r = (Color.red(color1) * ratio) + (Color.red(color2) * inverseRation);
        float g = (Color.green(color1) * ratio) + (Color.green(color2) * inverseRation);
        float b = (Color.blue(color1) * ratio) + (Color.blue(color2) * inverseRation);
        return Color.rgb((int) r, (int) g, (int) b);
    }

    /**
     * Blend two colors using their alpha
     *
     * @param color1 32bit colors argb
     * @param color2 32bit colors argb
     * @return 24bit color
     */
    public static int blend(int color1, int color2) {
        final float aTot = Color.alpha(color1) + Color.alpha(color2);
        final float ratio = Color.alpha(color1) / aTot;
        final float inverseRation = 1f - ratio;
        float r = (Color.red(color1) * ratio) + (Color.red(color2) * inverseRation);
        float g = (Color.green(color1) * ratio) + (Color.green(color2) * inverseRation);
        float b = (Color.blue(color1) * ratio) + (Color.blue(color2) * inverseRation);
        return Color.rgb((int) r, (int) g, (int) b);
    }

    // =============================================================================================
    // Bitmap

    /**
     * Helper to get screen shot of View object.
     *
     * @param view
     * @return bitmap.
     */
    private static Bitmap getBitmap(View view) {
        Bitmap screenBitmap =
                Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(screenBitmap);
        view.draw(canvas);
        return screenBitmap;
    }

    public static Bitmap grabScreen(Activity activity) {
        View view = activity.getWindow().getDecorView().getRootView();
        Bitmap screenBitmap = getBitmap(view);
        if (screenBitmap != null && screenBitmap.isRecycled()) {
            return null;
        }

        return screenBitmap;
    }

    /**
     * Render all items from ListView into bitmaps, such that no bitmap is larger than maxHeight.
     * <li> <a href="http://stackoverflow.com/questions/12742343/android-get-screenshot-of-all-listview-items">
     * Code from - get screenshot of all listview items </a>
     *
     * @param listview  input listView to render into bitmaps
     * @param maxHeight max height of output bitmaps
     * @return List of bitmaps.
     */
    public static List<Bitmap> getListViewAsBitmaps(ListView listview, int maxHeight) {

        ListAdapter adapter = listview.getAdapter();
        int itemscount = adapter.getCount();
        int allitemsheight = 0;
        List<Bitmap> itemBms = new ArrayList<Bitmap>();

        // Render each row into its own bitmap, compute total size.
        for (int row = 0; row < itemscount; row++) {

            View childView = adapter.getView(row, null, listview);
            childView.measure(MeasureSpec.makeMeasureSpec(listview.getWidth(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

            childView.layout(0, 0, childView.getMeasuredWidth(), childView.getMeasuredHeight());
            int childHeight = childView.getMeasuredHeight();
            if (childHeight < maxHeight) {
                itemBms.add(getBitmap(childView));
                allitemsheight += childHeight;
            }
        }

        List<Bitmap> outBigBms = new ArrayList<Bitmap>();
        int outHeight = Math.min(maxHeight, allitemsheight);
        Bitmap bigBitmap = null;
        Canvas bigcanvas = null;
        Paint paint = new Paint();
        int iHeight = 0;

        for (int idx = 0; idx < itemBms.size(); idx++) {
            Bitmap bmp = itemBms.get(idx);
            if (!isBitmapValid(bmp)) {
                s_log.e("invalid bitmap");
                continue;
            }

            if (iHeight + bmp.getHeight() >= outHeight) {
                outBigBms.add(bigBitmap);
                bigBitmap = null;
                allitemsheight -= iHeight;
                iHeight = 0;
            }

            if (bigBitmap == null) {
                outHeight = Math.min(maxHeight, allitemsheight);
                // TODO - handle outOfMemory exception.
                bigBitmap = Bitmap.createBitmap(listview.getMeasuredWidth(), outHeight, Bitmap.Config.ARGB_8888);
                bigcanvas = new Canvas(bigBitmap);
            }

            bigcanvas.drawBitmap(bmp, 0, iHeight, paint);
            iHeight += bmp.getHeight();

            bmp.recycle();
            bmp = null;
        }

        return outBigBms;
    }

    /**
     * Render table rows into bitmaps, group multiple rows into single image until maxHeight is
     * exceeded.
     *
     * @param tableLayout
     * @param maxHeight
     * @return List of bitmaps.
     */
    public static List<Bitmap> getTableLayoutAsBitmaps(TableLayout tableLayout, int maxHeight) {

        int itemscount = tableLayout.getChildCount();
        int allitemsheight = 0;
        List<Bitmap> itemBms = new ArrayList<Bitmap>();

        // Render each row into its own bitmap, compute total size.
        for (int row = 0; row < itemscount; row++) {

            View childView = tableLayout.getChildAt(row);
            childView.measure(MeasureSpec.makeMeasureSpec(tableLayout.getWidth(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

            childView.layout(0, 0, childView.getMeasuredWidth(), childView.getMeasuredHeight());
            int childHeight = childView.getMeasuredHeight();
            if (childHeight < maxHeight) {
                itemBms.add(getBitmap(childView));
                allitemsheight += childHeight;
            }

        }

        // If only two pages, try and divide into two even pages
        // Dont' try exactly 50% per page because row items may not
        // fit evenly, so give pages 60% of total height.
        if (allitemsheight / maxHeight == 2)
            maxHeight = (int) (allitemsheight * 0.6);

        List<Bitmap> outBigBms = new ArrayList<Bitmap>();
        int outHeight = Math.min(maxHeight, allitemsheight);
        Bitmap bigBitmap = null;
        Canvas bigcanvas = null;
        Paint paint = new Paint();
        int iHeight = 0;

        for (int idx = 0; idx < itemBms.size(); idx++) {
            Bitmap bmp = itemBms.get(idx);
            if (!isBitmapValid(bmp)) {
                s_log.e("invalid bitmap");
                continue;
            }

            if (iHeight + bmp.getHeight() >= outHeight) {
                outBigBms.add(bigBitmap);
                bigBitmap = null;
                allitemsheight -= iHeight;
                iHeight = 0;
            }

            if (bigBitmap == null) {
                outHeight = Math.min(maxHeight, allitemsheight);
                bigBitmap = Bitmap.createBitmap(tableLayout.getMeasuredWidth(), outHeight, Bitmap.Config.ARGB_8888);
                bigcanvas = new Canvas(bigBitmap);
            }

            bigcanvas.drawBitmap(bmp, 0, iHeight, paint);
            iHeight += bmp.getHeight();

            bmp.recycle();
            bmp = null;
        }

        if (bigBitmap != null)
            outBigBms.add(bigBitmap);

        // tableLayout.restoreHierarchyState(stateArray);
        return outBigBms;
    }


    /**
     * Save bitmap to local filesystem
     *
     * @param bitmap   Bitmap to save
     * @param baseName Base filename used to save image, ex: "screenshot.png"
     * @return full filename path
     */
    public static String saveBitmap(Context context, Bitmap bitmap, String baseName) {
        // String filePath = joinPath(joinPath(Environment.getExternalStorageDirectory(),"Pictures"),
        String filePath = joinPath(context.getExternalCacheDir(),
                baseName);
        File imagePath = new File(filePath);
        FileOutputStream fileOutStrm;
        try {
            fileOutStrm = new FileOutputStream(imagePath);
            bitmap.compress(CompressFormat.PNG, 100, fileOutStrm);
            fileOutStrm.flush();
            fileOutStrm.close();
            return filePath;
        } catch (FileNotFoundException ex) {
            s_log.e("saveBitmap", ex);
        } catch (IOException ex) {
            s_log.e("saveBitmap", ex);
        }
        return null;
    }


    /**
     * Share screen capture
     *
     * @param activity
     * @param shareActionProvider
     */
    public static void shareScreen(FragmentActivity activity, String what, ShareActionProvider shareActionProvider) {
        Bitmap screenBitmap = Utils.grabScreen(activity);
        List<Bitmap> bitmapList = new ArrayList<Bitmap>();
        bitmapList.add(screenBitmap);
        shareBitmap(activity, bitmapList, what, "screenshot.png", shareActionProvider);
        GoogleAnalyticsHelper.event(activity, "share", "screen", activity.getClass().getName());
    }

    public static void shareScreen(View view, String what, ShareActionProvider shareActionProvider) {
        Bitmap screenBitmap = getBitmap(view);
        List<Bitmap> bitmapList = new ArrayList<Bitmap>();
        bitmapList.add(screenBitmap);
        shareBitmap(view.getContext(), bitmapList, what, "screenshot.png", shareActionProvider);
    }

    public static String getScreenImagePath(View view, Activity activity) {
        Bitmap viewBitmap = getBitmap(view);
        return Images.Media.insertImage(activity.getContentResolver(), viewBitmap, "view.png", null);
    }

    public static boolean isBitmapValid(Bitmap bitmap) {
        return bitmap != null && !bitmap.isRecycled() && bitmap.getHeight() * bitmap.getWidth() > 0;
    }

    public static void shareBitmap(
            Context context, List<Bitmap> shareImages, String what, String imageName, ShareActionProvider shareActionProvider) {

        int imgCnt = shareImages.size();
        Intent shareIntent = new Intent(imgCnt == 1 ? Intent.ACTION_SEND : Intent.ACTION_SEND_MULTIPLE);
        final String IMAGE_TYPE = "image/png";
        final String TEXT_TYPE = "text/plain";

        if (imgCnt > 0) {
            shareIntent.setType(IMAGE_TYPE);
            if (imgCnt == 1) {
                Bitmap bitmap = shareImages.get(0);
                if (!isBitmapValid(bitmap))
                    return;

                // String screenImgFilename = Images.Media.insertImage(getContentResolver(), bitmap, imageName, null);
                String screenImgFilename = Utils.saveBitmap(context, bitmap, imageName);
                bitmap.recycle();
                Uri uri = Uri.fromFile(new File(screenImgFilename));
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            } else {
                ArrayList<Uri> uris = new ArrayList<Uri>();
                for (int bmIdx = 0; bmIdx != shareImages.size(); bmIdx++) {
                    Bitmap bitmap = shareImages.get(bmIdx);
                    if (isBitmapValid(bitmap)) {
                        String screenImgFilename = Utils.saveBitmap(context, bitmap, String.valueOf(bmIdx) + imageName);
                        bitmap.recycle();
                        Uri uri = Uri.fromFile(new File(screenImgFilename));
                        uris.add(uri);
                    } else
                        s_log.e("invalid bitmap");
                }
                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            }
        } else {
            shareIntent.setType(TEXT_TYPE);
        }

        String shareBody = String.format("%s v%s\n%s\n%s\n",
                GlobalInfo.s_globalInfo.appName,
                GlobalInfo.s_globalInfo.version,
                what,
                context.getString(R.string.websiteLanDenLabs));
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, GlobalInfo.s_globalInfo.appName + " " + what);
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);

    	/*
        if (IMAGE_TYPE.equals(shareIntent.getMimeType())) {
        	shareActionProvider.setHistoryFileName(SHARE_IMAGE_HISTORY_FILE_NAME);
        } else if (TEXT_TYPE.equals(shareIntent.getMimeType())) {
        	shareActionProvider.setHistoryFileName(SHARE_TEXT_HISTORY_FILE_NAME);
        }
        */
        //	if (shareActionProvider != null) {
        //		shareActionProvider.setShareIntent(shareIntent);
        //	} else {
        GlobalInfo.s_globalInfo.mainFragActivity.startActivity(Intent.createChooser(shareIntent, "Share"));
        //	}

        GoogleAnalyticsHelper.event(GlobalInfo.s_globalInfo.mainFragActivity, "", "share-screen", imageName);
    }

    /*
     * http://www.truiton.com/2013/03/android-take-screenshot-programmatically-and-send-email/
     *
     * Need permission -
     * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
     */
    public static void sendMail(Activity activity, String path) {
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                new String[]{"receiver@website.com"});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                "Truiton Test Mail");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                "This is an autogenerated mail from Truiton's InAppMail app");
        emailIntent.setType("image/png");
        Uri myUri = Uri.parse("file://" + path);
        emailIntent.putExtra(Intent.EXTRA_STREAM, myUri);
        activity.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        GoogleAnalyticsHelper.event(GlobalInfo.s_globalInfo.mainFragActivity, "", "share-email", activity.getClass().getName());
    }

    public static final int CLOCK_NOTIFICATION_ID = 1;

    public static void sendNotification(Context context, int id, String msg) {
        s_log.i("Preparing to send notification...: " + msg);
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, DevToolActivity.class), 0);

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.dev_tool_ic);
        NotificationCompat.Builder alamNotificationBuilder =
                new NotificationCompat.Builder(context)
                        .setContentTitle("DevTool Alarm")
                        .setSmallIcon(R.drawable.dev_tool)
                        .setLargeIcon(largeIcon)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                        .setContentText(msg);

        alamNotificationBuilder.setContentIntent(contentIntent);
        notificationManager.notify(id, alamNotificationBuilder.build());

        s_log.i("Notification sent.");
    }

    public static void cancelNotification(Context context, int id) {
        s_log.i("Cancel notification");
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }

    // =============================================================================================
    // Web

    /**
     * Load asset file.
     *
     * @param inFile Asset file name.
     * @return String with asset contents.
     */
    public static String LoadData(Context context, String inFile) {
        String tContents = "";

        try {
            InputStream stream = context.getAssets().open(inFile);

            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            tContents = new String(buffer);
        } catch (IOException e) {
            // Handle exceptions here
        }

        return tContents;
    }
}
