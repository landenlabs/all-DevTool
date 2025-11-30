/*
 * Copyright (c) 2023 Dennis Lang (LanDen Labs) landenlabs@gmail.com
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

package com.landenlabs.all_devtool.shortcuts.util;

import android.util.Log;

import com.landenlabs.all_devtool.GlobalInfo;

/**
 * Log wrapper which automatically generates tag from call stack (includes filename and line number)
 * and easy ON/OFF via local instance state.
 *
 * Created by Dennis Lang on 11/28/2015.
 */
public class LLog {
    
    public static final LLog LLOG = new LLog(GlobalInfo.s_globalInfo.isDebug);
    public static final String LOG_PREFIX = "all_dev";
    private final boolean mEnabled;
    
    public LLog(boolean enabled) {
        mEnabled = enabled;
    }

    public final void d(String ... args) {
        if (mEnabled)
            Log.d(LOG_PREFIX, String.join(" ", args));
    }

    public final void i(String ... args) {
        if (mEnabled)
            Log.i(LOG_PREFIX, String.join(" ", args));
    }

    public final void w(String ... args) {
        if (mEnabled)
            Log.w(LOG_PREFIX, String.join(" ", args));
    }

    public final void e(String ... args) {
        if (mEnabled)
            Log.e(LOG_PREFIX, String.join(" ", args));
    }
    
    /*
    private static final String NAME = LLog.class.getCanonicalName();

    public static String getTag() {
        String tag = "";
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        for (int idx = 0; idx < ste.length; idx++) {
            StackTraceElement elem = ste[idx];
            if (elem.getMethodName().equals("getTag") && elem.getClassName().equals(NAME)) {
                while (++idx < ste.length) {
                    elem = ste[idx];
                    if (!elem.getClassName().equals(NAME))
                        break;
                }
                tag = "("+elem.getFileName() + ":" + elem.getLineNumber()+") ";
                return tag;
            }
        }
        return tag;
    }
    */
}
