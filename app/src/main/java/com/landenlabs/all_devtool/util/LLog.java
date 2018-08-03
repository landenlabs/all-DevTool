package com.landenlabs.all_devtool.util;

import android.util.Log;

import com.landenlabs.all_devtool.GlobalInfo;

/*
 * Copyright (c) 2016 Dennis Lang (LanDen Labs) landenlabs@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 *  following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all copies or substantial
 *  portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *  @author Dennis Lang  (3/21/2015)
 *  @see http://landenlabs.com/
 */

/**
 * Log wrapper which automatically generates tag from call stack (includes filename and line number)
 * and easy ON/OFF via local instance state.
 *
 * Created by Dennis Lang on 11/28/2015.
 */
@SuppressWarnings("FieldCanBeLocal")
public class LLog {

    // LLog states OFF or ON, use DBG to auto enable in Debug build.
    private static final LLog OFF = new LLog(false);
    private static final LLog ON = new LLog(true);
    public static final LLog DBG = GlobalInfo.s_globalInfo.isDebug ? ON : OFF;

    // Auxilary 'fmt' meta tags
    private static final String PID = "{pid}";   // Process ID
    private static final String TID = "{tid}";   // Thread ID

    private static final String NAME = LLog.class.getCanonicalName();
    private final boolean mEnabled;

    public LLog(boolean enabled) {
        mEnabled = enabled;
    }

    public final boolean isOn() {
        return this == ON;
    }

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
                tag = "("+elem.getFileName() + ":" + elem.getLineNumber()+")";
                return tag;
            }
        }
        return tag;
    }

    private static String fmt(String msg, Object... args) {
        return fmt(String.format(msg, args));
    }

    public final void d(String msg) {
        if (isOn())
            Log.d(getTag(), msg);
    }

    public final void i(String msg) {
        if (isOn())
            Log.i(getTag(), msg);
    }

    public final void e(String msg) {
        if (isOn())
            Log.e(getTag(), msg);
    }

    private static String fmt(String msg) {
        msg = msg.replace(PID, String.valueOf(android.os.Process.myPid()));
        msg = msg.replace(TID, String.valueOf(android.os.Process.myTid()));
        return msg;
    }

    public final void d(String msg, Object... args) {
        if (isOn())
            Log.d(getTag(), fmt(msg, args));
    }

    public final void i(String msg, Object... args) {
        if (isOn())
            Log.i(getTag(), fmt(msg, args));
    }


    public final void e(String msg, Exception ex) {
        if (isOn())
            Log.e(getTag(), fmt(msg), ex);
    }

    public final void e(String msg, Object... args) {
        if (isOn())
            Log.e(getTag(), fmt(msg, args));
    }
}
