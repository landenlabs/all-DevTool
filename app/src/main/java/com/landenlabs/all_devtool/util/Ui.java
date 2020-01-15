package com.landenlabs.all_devtool.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.MailTo;
import android.net.Uri;
import androidx.annotation.FloatRange;
import androidx.fragment.app.FragmentActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Adapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.landenlabs.all_devtool.R;

@SuppressWarnings("Convert2Lambda")
public class Ui {

    public static String HTML_CENTER_BOX = "<div style='min-height:128px;'><table height='100%%' width='100%%'><tr valign='middle'><td style='border: 2px solid; border-radius: 25px;'><center>%s</center></table></div>";

    @SuppressWarnings("unchecked")
    public static <E extends View> E viewById(View rootView, int id) {
        return (E) rootView.findViewById(id);
    }

    public static <E extends View> E viewById(FragmentActivity fact, int id) {
        //noinspection unchecked
        return (E) fact.findViewById(id);
    }

    public static void ToastBig(Activity activity, String str) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_big, activity.findViewById(R.id.toast_layout_root));

        TextView text = Ui.viewById(layout, R.id.text);
        text.setText(str);

        Toast toast = new Toast(activity);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    public static AlertDialog ShowMessage(Activity activity, String message) {
        AlertDialog dialog = new AlertDialog.Builder(activity).setMessage(message)
                .setPositiveButton("More", null)
                .setNegativeButton("Close", null)
                .show();
        dialog.setCanceledOnTouchOutside(true);
        TextView textView = dialog.findViewById(android.R.id.message);
        textView.setSingleLine(false);
        textView.setTextSize(20);
        return dialog;
    }

    public static AlertDialog ShowFileDlg(Activity activity, String message) {
        AlertDialog dialog = new AlertDialog.Builder(activity).setMessage(message)
                .setPositiveButton("More", null)
                .setNegativeButton("Close", null)
                .setNeutralButton("Disk", null)
                .show();
        dialog.setCanceledOnTouchOutside(true);
        TextView textView = dialog.findViewById(android.R.id.message);
        textView.setSingleLine(false);
        textView.setTextSize(20);
        return dialog;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public static Intent newEmailIntent(Context context, String address, String subject, String body, String cc) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{address});
        intent.putExtra(Intent.EXTRA_TEXT, body);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_CC, cc);
        intent.setType("message/rfc822");
        return intent;
    }

    /***
     * Display html message in webView in a dialog.
     */
    public static void showWebMessage(final Context context, String... htmlStr) {

        String fullHtmlStr = htmlStr[0];
        if (htmlStr.length > 1) {
            String fmt = htmlStr[0];
            String arg = htmlStr[1];
            fullHtmlStr = String.format(fmt, arg);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("");

        WebView webView = new WebView(context);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setBackgroundResource(R.drawable.about_bg);
        webView.getSettings().setBuiltInZoomControls(true);


        // String htmlStr = String.format(LoadData("about.html"), getPackageInfo().versionName,
        //         Doc.CRYPTO_MODE);
        // webView.loadData(fullHtmlStr, "text/html; charset=utf-8", "utf-8");
        webView.loadDataWithBaseURL("file:///android_asset/", fullHtmlStr , "text/html", "utf-8", null);

        webView.setMinimumHeight(128 * 2);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("mailto:")) {
                    MailTo mt = MailTo.parse(url);
                    Intent emailIntent = newEmailIntent(context, mt.getTo(), mt.getSubject(), mt.getBody(), mt.getCc());
                    context.startActivity(emailIntent);
                    view.reload();
                    return true;
                } else {
                    // Open link in external browser.
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    context.startActivity(webIntent);
                }
                return true;
            }
        });

        builder.setView(webView);
        builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /***
     * Display image in webView dialog.
     *
     * @param imagePath  "file:///android_asset/world_timezone_map.png"
     */
    @SuppressWarnings("unused")
    public static void showWebImage(final Context context, String imagePath) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("");

        WebView webView = new WebView(context);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setBackgroundResource(R.drawable.about_bg);
        webView.getSettings().setBuiltInZoomControls(true);


        // String htmlStr = String.format(LoadData("about.html"), getPackageInfo().versionName,
        //         Doc.CRYPTO_MODE);
        webView.loadUrl(imagePath);
        webView.setMinimumHeight(128 * 2);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("mailto:")) {
                    MailTo mt = MailTo.parse(url);
                    Intent emailIntent = newEmailIntent(context, mt.getTo(), mt.getSubject(), mt.getBody(), mt.getCc());
                    context.startActivity(emailIntent);
                    view.reload();
                    return true;
                } else {
                    // Open link in external browser.
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    context.startActivity(webIntent);
                }
                return true;
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
            }
        });

        builder.setView(webView);
        builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * Dump info about view and its children into StringBuffer returned.
     */
    public static StringBuilder dumpViews(View view, int level) {
        StringBuilder sb  = new StringBuilder();
        String prefix;
        for (int i = 0; i < level; i++) {
            sb.append("  ");
        }
        prefix = sb.toString();

        sb = new StringBuilder();
        sb.append(String.format("hasOnClick=%-5b focusable=%-5b inTouch=%-5b vis=%-5b shown=%-5b %2d ",
                view.hasOnClickListeners(),
                view.isFocusable(),
                view.isFocusableInTouchMode(),
                view.getVisibility() == View.VISIBLE,
                view.isShown(),
                level
        ));
        sb.append(prefix).append(view.getClass().getSimpleName());
        if (!TextUtils.isEmpty(view.getContentDescription())) {
            sb.append(" desc=").append(view.getContentDescription());
        }
        if (view instanceof TextView) {
            sb.append(" text=").append(((TextView)view).getText());
        }

        sb.append("\n");

        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup)view;
            for (int idx = 0; idx < viewGroup.getChildCount(); idx++){
                View child = viewGroup.getChildAt(idx);
                sb.append(dumpViews(child, level+1));
            }

            if (view instanceof ListView) {
                ExpandableListView expView = (ExpandableListView) view;
                Adapter adapter = expView.getAdapter();
                int cnt = adapter.getCount();
                for (int idx = 0; idx < cnt; idx++) {
                    Object obj = adapter.getItem(idx);
                    if (obj instanceof View) {
                        sb.append(dumpViews((View) obj, level + 1));
                    }
                }
            }
        }

        return sb;
    }


    /**
     * Blends to colors according to target percent1.
     *
     * @param color1 first color to blend
     * @param color2 second color to blend
     * @param percent1  percent of color1
     *
     * @return blended color = (color1 * percent1) + (color2 * (1-percent1)
     */
    public static int blendColors(int color1, int color2, @FloatRange(from = 0, to = 1) float percent1) {
        float percent2 = (1f - percent1);
        float a = (Color.alpha(color1) * percent1) + (Color.alpha(color2) * percent2);
        float r = (Color.red(color1) * percent1) + (Color.red(color2) * percent2);
        float g = (Color.green(color1) * percent1) + (Color.green(color2) * percent2);
        float b = (Color.blue(color1) * percent1) + (Color.blue(color2) * percent2);
        return Color.argb((int) a, (int) r, (int) g, (int) b);
    }
}
