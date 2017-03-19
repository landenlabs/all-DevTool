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

/**
 * Display Icon assets in the android.R.attr section.
 *
 * @author Dennis Lang
 */
public class IconAttrFragment extends IconBaseFragment {

    public static final String s_name = "IconAttr";

    public IconAttrFragment() {
    }

    public static IconAttrFragment create() {
        return new IconAttrFragment();
    }

    // ============================================================================================
    // DevFragment methods

    @Override
    public String getName() {
        return s_name;
    }

    // ============================================================================================
    // IconBaseFragment methods

    @Override
    public void addToList() {
        addAttr("actionBarItemBackground", android.R.attr.actionBarItemBackground);
        addAttr("actionModeCloseDrawable", android.R.attr.actionModeCloseDrawable);
        addAttr("actionModeCopyDrawable", android.R.attr.actionModeCopyDrawable);
        addAttr("actionModeCutDrawable", android.R.attr.actionModeCutDrawable);
        addAttr("actionModePasteDrawable", android.R.attr.actionModePasteDrawable);
        addAttr("actionModeSelectAllDrawable", android.R.attr.actionModeSelectAllDrawable);
        addAttr("alertDialogIcon", android.R.attr.alertDialogIcon);
        addAttr("dialogIcon", android.R.attr.dialogIcon);

        addAttr("textCheckMark", android.R.attr.textCheckMark);
        addAttr("listChoiceIndicatorMultiple", android.R.attr.listChoiceIndicatorMultiple);
        addAttr("listChoiceIndicatorSingle", android.R.attr.listChoiceIndicatorSingle);
        addAttr("listChoiceBackgroundIndicator", android.R.attr.listChoiceBackgroundIndicator);
    }
}