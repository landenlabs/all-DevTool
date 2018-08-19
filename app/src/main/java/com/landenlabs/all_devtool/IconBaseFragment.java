package com.landenlabs.all_devtool;

/*
 * Copyright (c) 2016 Dennis Lang (LanDen Labs) landenlabs@gmail.com
 *
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
 * @author Dennis Lang  (3/21/2015)
 * @see http://LanDenLabs.com/
 *
 */


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.landenlabs.all_devtool.util.Ui;
import com.landenlabs.all_devtool.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Base class for Icon asset display.
 * <p/>
 * <ul>
 * <li> scrollable list of image assets
 * <li> click on row to get detail asset information
 * </ul>
 *
 * @author Dennis Lang
 */
@SuppressWarnings("Convert2Lambda")
public abstract class IconBaseFragment extends DevFragment {

    final ArrayList<IconInfo> m_list = new ArrayList<>();
    ListView m_listView;
    FragmentActivity m_context;
    int m_backgroundColor = -1;
    int m_alternateColor = -1;

    // ============================================================================================
    // Abstract interface

    public abstract void addToList();

    // ============================================================================================
    // Override DevFragment

    @Override
    public List<Bitmap> getBitmaps(int maxHeight) {
        Toast.makeText(getActivity(), String.format("Please wait while\n %d items extracted...",
                m_listView.getCount()), Toast.LENGTH_LONG).show();

        return Utils.getListViewAsBitmaps(m_listView, maxHeight);
    }

    @Override
    public List<String> getListAsCsv() {
        return Utils.getListViewAsCSV(m_listView);
    }

    // ============================================================================================
    // Override Fragment

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View rootView = inflater.inflate(R.layout.icon_tab, container, false);
        m_context = this.getActivity();

        m_listView = Ui.viewById(rootView, R.id.iconListView);
        m_listView.removeAllViewsInLayout();

        int[] attrs = {android.R.id.background};
        TypedArray ta = m_context.getTheme().obtainStyledAttributes(attrs);
        m_backgroundColor = ta.getColor(0, -1);
        ta.recycle();
        m_alternateColor = Utils.blend(m_backgroundColor, 0x80d0ffe0);

        m_list.clear();
        addToList();
        Collections.sort(m_list, new Comparator<IconInfo>() {
            @Override
            public int compare(IconInfo icon1, IconInfo icon2) {
                return icon1.fieldStr().compareTo(icon2.fieldStr());
            }
        });

        final IconArrayAdapter adapter = new IconArrayAdapter(
                this.getActivity(), R.layout.icon_list_row, R.id.iconName,
                m_list);
        m_listView.setAdapter(adapter);

        m_listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                IconInfo iconInfo = (IconInfo) parent.getItemAtPosition(position);
                showIconDialog(iconInfo);
            }
        });

        return rootView;
    }

    // ============================================================================================
    // IconBaseFragment methods

    protected void addIcon(String name, int resId) {
        try {
            m_list.add(new IconInfo(name, resId));
        } catch (Exception ex) {
            // Failed to add asset.
        }
    }

    protected void addAttr(String name, int attrId) {
        int[] attrs = {attrId};
        TypedArray ta = m_context.getTheme().obtainStyledAttributes(attrs);
        Drawable indicator = ta.getDrawable(0);
        ta.recycle();
        m_list.add(new IconInfo(name, indicator));
    }

    // ============================================================================================
    // Internal classes

    private class IconInfo {
        final String m_fieldStr;
        final int m_value;
        final Drawable m_drawable;

        IconInfo(String str1, int value) {
            m_fieldStr = str1;
            m_value = value;
            m_drawable = null;
        }

        IconInfo(String str1, Drawable drawable) {
            m_fieldStr = str1;
            m_value = -1;
            m_drawable = drawable;
        }

        public String toString() {
            return m_fieldStr;
        }

        public String fieldStr() {
            return m_fieldStr;
        }

        boolean hasValue() {
            return m_value != -1;
        }

        public int getValue() {
            return m_value;
        }

        public Drawable getDrawable() {
            if (Build.VERSION.SDK_INT >= 21) {
                return hasValue() ? getResources().getDrawable(getValue(), getContextSafe().getTheme()) : m_drawable;
            } else {
                return hasValue() ? getResources().getDrawable(getValue()) : m_drawable;
            }
        }
    }

    private class IconArrayAdapter extends ArrayAdapter<IconInfo> {

        IconArrayAdapter(Context context, int rowLayoutId,
                                int textViewResourceId, List<IconInfo> objects) {
            super(context, rowLayoutId, textViewResourceId, objects);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView imageInfo = Ui.viewById(view, R.id.iconInfo);
            IconInfo iconInfo = getItem(position);
            if (iconInfo == null) {
                return view;
            }

            Drawable iconD = iconInfo.getDrawable();
            if (iconD != null) {
                String className = iconD.getClass().getSimpleName();
                String infoStr = String.format(
                        Locale.getDefault(), "%d x %d (%c)",
                        iconD.getIntrinsicWidth(), iconD.getIntrinsicHeight(), className.charAt(0));

                // infoStr = infoStr + " " + className;
                // NinePatchDrawable ninePatch = (NinePatchDrawable)iconD;
                imageInfo.setText(infoStr);
            }

            ImageView imageView = Ui.viewById(view, R.id.iconImage1);
            imageView.setImageDrawable(iconD);
            imageView = Ui.viewById(view, R.id.iconImage2);
            imageView.setImageDrawable(iconD);
            imageView = Ui.viewById(view, R.id.iconImage3);
            imageView.setImageDrawable(iconD);
            imageView = Ui.viewById(view, R.id.iconImage4);
            imageView.setImageDrawable(iconD);

            if ((position & 1) == 1)
                view.setBackgroundColor(m_backgroundColor);
            else
                view.setBackgroundColor(m_alternateColor);


            return view;
        }

    }

    /**
     * Show a 'StateListDrawable' information
     */
    private void showStateIcon(final ImageView imageView, TableRow row1, TableRow row2,
               StateListDrawable stateListDrawable, int state, String desc, Set<Drawable> stateIcons) {

        stateListDrawable.setState(new int[]{state});
        Drawable stateD = stateListDrawable.getCurrent();
        if (!stateIcons.contains(stateD)) {
            stateIcons.add(stateD);
            ImageButton stateImageView = new ImageButton(imageView.getContext());
            Drawable[] drawables = new Drawable[]{stateD, getResources().getDrawable(R.drawable.button_border_sel)};

            LayerDrawable layerDrawable = new LayerDrawable(drawables);
            stateImageView.setImageDrawable(layerDrawable);
            //	stateImageView.setBackgroundResource(R.drawable.button_border_sel);
            stateImageView.setPadding(10, 10, 10, 10);
            stateImageView.setMinimumHeight(8);
            stateImageView.setMinimumWidth(8);
            stateImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imageView.setImageDrawable(((ImageView) v).getDrawable());
                }
            });

            TextView stateTextView = new TextView(imageView.getContext());
            stateTextView.setText(desc);
            stateTextView.setTextSize(12);
            stateTextView.setGravity(Gravity.CENTER);

            row1.addView(stateTextView);
            row2.addView(stateImageView);
        }
    }

    /**
     * Show a 'LayerDrawable'  information.
     */
    private void showLayerIcon(final ImageView imageView, TableRow row1, TableRow row2,
               Drawable iconD, int layerIdx) {

        if (iconD != null) {
            ImageView layerImageView = new ImageView(imageView.getContext());

            layerImageView.setImageDrawable(iconD);
            layerImageView.setPadding(10, 10, 10, 10);
            layerImageView.setMinimumHeight(8);
            layerImageView.setMinimumWidth(8);
            layerImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imageView.setImageDrawable(((ImageView) v).getDrawable());
                }
            });

            TextView stateTextView = new TextView(imageView.getContext());
            stateTextView.setText(String.valueOf(layerIdx));
            stateTextView.setTextSize(12);
            stateTextView.setGravity(Gravity.CENTER);

            row1.addView(stateTextView);
            row2.addView(layerImageView);
        }
    }


    /**
     * Show 'AnimationDrawable'  information
     */
    private void showAnimationBtns(final ImageView imageView,
               final AnimationDrawable animationDrawable, TableRow row1, TableRow row2) {

        int[] imageResIds = new int[]{
                android.R.drawable.ic_media_pause
                , android.R.drawable.ic_media_play
                // , android.R.drawable.ic_media_next
        };

        String[] descBtns = new String[]{
                "Pause"
                , "Play"
                // , "Next"
        };

        ImageButton btnImage;
        TextView btnDesc;

        for (int idx = 0; idx < imageResIds.length; idx++) {

            btnImage = new ImageButton(imageView.getContext());
            btnImage.setTag(idx);

            btnImage.setImageResource(imageResIds[idx]);
            btnImage.setPadding(10, 10, 10, 10);
            btnImage.setMinimumHeight(8);
            btnImage.setMinimumWidth(8);
            btnImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int n = (Integer) v.getTag();
                    switch (n) {
                        case 0:  // pause
                            animationDrawable.stop();
                            break;
                        case 1:    // play
                            animationDrawable.stop();
                            animationDrawable.start();
                            break;
                        case 2:    // next
                            break;
                    }

                }
            });

            row1.addView(btnImage);

            btnDesc = new TextView(imageView.getContext());
            btnDesc.setText(descBtns[idx]);
            btnDesc.setTextSize(12);
            btnDesc.setGravity(Gravity.CENTER);
            row2.addView(btnDesc);
        }

        // Can't start now - icon not fully rendered, see onChangeFocus
        // animationDrawable.stop();
        // animationDrawable.start();

    }

    /**
     * Display icon (drawable) information
     */
    private void showIconDialog(IconInfo iconInfo) {
        Drawable iconD = iconInfo.getDrawable();
        String iconType = iconD.getClass().getSimpleName();

        LayoutInflater inflater = m_context.getLayoutInflater();
        final View dialogLayout = inflater.inflate(R.layout.icon_dlg, null);

        View shareBtn = dialogLayout.findViewById(R.id.icon_dlg_share);
        shareBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.shareScreen(dialogLayout, "iconDetail", null);
            }
        });

        final TextView imageName = Ui.viewById(dialogLayout, R.id.icon_dlg_name);
        final TextView imageSize = Ui.viewById(dialogLayout, R.id.icon_dlg_size);
        final TextView imageType = Ui.viewById(dialogLayout, R.id.icon_dlg_type);
        final TextView imageExtra = Ui.viewById(dialogLayout, R.id.icon_dlg_extra);

        imageName.setText(iconInfo.fieldStr());
        imageSize.setText(String.format("Size: %d x %d",
                iconD.getIntrinsicWidth(), iconD.getIntrinsicHeight()));
        imageType.setText(iconType);

        final ImageView imageView = Ui.viewById(dialogLayout, R.id.icon_dlg_image);
        // imageView.setImageDrawable(iconD);
        boolean hasStates = iconD.isStateful();

        final View stateTitle = dialogLayout.findViewById(R.id.icon_dlg_state_title);
        stateTitle.setVisibility(hasStates ? View.VISIBLE : View.GONE);

        final TableRow row1 = Ui.viewById(dialogLayout, R.id.icon_dlg_state_row1);
        row1.removeAllViews();

        final TableRow row2 = Ui.viewById(dialogLayout, R.id.icon_dlg_state_row2);
        row2.removeAllViews();

        boolean showRows = false;
        String extraInfo = "";

        if (hasStates) {
            extraInfo = "StateFul";
            showRows = true;

            StateListDrawable stateListDrawable = (StateListDrawable) iconD;
            Set<Drawable> stateIcons = new HashSet<>();
            showStateIcon(imageView, row1, row2, stateListDrawable, android.R.attr.state_enabled, "Enabled", stateIcons);
            showStateIcon(imageView, row1, row2, stateListDrawable, android.R.attr.state_pressed, "Pressed", stateIcons);
            showStateIcon(imageView, row1, row2, stateListDrawable, android.R.attr.state_checked, "Checked", stateIcons);
            showStateIcon(imageView, row1, row2, stateListDrawable, android.R.attr.state_selected, "Selected", stateIcons);
        }

        if (iconD instanceof LayerDrawable) {
            showRows = true;
            LayerDrawable layerDrawable = (LayerDrawable) iconD;
            int layerCnt = layerDrawable.getNumberOfLayers();
            extraInfo = String.format(Locale.getDefault(), "Layers:%d", layerCnt);
            for (int layerIdx = 0; layerIdx < Math.min(layerCnt, 3); layerIdx++) {
                showLayerIcon(imageView, row1, row2, layerDrawable.getDrawable(layerIdx), layerIdx);
            }
        } else if (iconD instanceof AnimationDrawable) {
            final AnimationDrawable animationDrawable = (AnimationDrawable) iconD;
            extraInfo = String.format(Locale.getDefault(), "Frames:%d", animationDrawable.getNumberOfFrames());
            showRows = true;
            showAnimationBtns(imageView, animationDrawable, row1, row2);

            // Can't control animation at this time, drawable not rendered yet.
            // animationDrawable.stop();
        }

        row1.setVisibility(showRows ? View.VISIBLE : View.GONE);
        row2.setVisibility(showRows ? View.VISIBLE : View.GONE);

        imageExtra.setText(extraInfo);
        imageView.setImageDrawable(iconD);

        dialogLayout.findViewById(R.id.icon_dlg_whiteBtn).setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onClick(View v) {
                imageView.setBackgroundDrawable(v.getBackground());
            }
        });

        dialogLayout.findViewById(R.id.icon_dlg_grayBtn).setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onClick(View v) {
                imageView.setBackgroundDrawable(v.getBackground());
            }
        });

        dialogLayout.findViewById(R.id.icon_dlg_blackBtn).setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onClick(View v) {
                imageView.setBackgroundDrawable(v.getBackground());
            }
        });

        dialogLayout.findViewById(R.id.icon_dlg_squaresBtn).setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onClick(View v) {
                imageView.setBackgroundDrawable(v.getBackground());
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(m_context);
        builder.setView(dialogLayout);

        builder.setMessage("Icon")
                .setCancelable(false)
                .setPositiveButton("Close",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, close
                                dialog.cancel();
                            }
                        });

        builder.show();
    }
}