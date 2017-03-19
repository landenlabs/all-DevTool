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
 * Display Numeric assets in the android.R.dimen section.
 * 
 * @author Dennis Lang
 *
 */
public class NumDimenFragment  extends NumBaseFragment {
	
	public static final String s_name = "NumDimen";

	public NumDimenFragment() {
	}

	public static NumDimenFragment create() {
		return new NumDimenFragment();
	}
	
	// ============================================================================================
	// NumBaseFragment methods
	
	@Override
	public String getName() {
		return s_name;
	}

	@Override
	public void addToList() {
		addNum("app_icon_size", android.R.dimen.app_icon_size);
		addNum("dialog_min_width_major", android.R.dimen.dialog_min_width_major);
		addNum("dialog_min_width_minor", android.R.dimen.dialog_min_width_minor);
		addNum("notification_large_icon_height", android.R.dimen.notification_large_icon_height);
		addNum("notification_large_icon_width", android.R.dimen.notification_large_icon_width);
		addNum("thumbnail_height", android.R.dimen.thumbnail_height);
		addNum("thumbnail_width", android.R.dimen.thumbnail_width);
	}
}

