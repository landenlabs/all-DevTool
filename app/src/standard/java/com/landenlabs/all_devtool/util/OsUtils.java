package com.landenlabs.all_devtool.util;

/*
 * Copyright (c) 2015 - 2020 Dennis Lang (LanDen Labs) landenlabs@gmail.com
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

import android.os.Build;
import android.system.ErrnoException;
import android.system.Os;
import android.system.StructStat;
import android.util.Log;

import java.io.File;

/**
 * Created by Dennis Lang on 2/22/17.
 */

public class OsUtils {
    public static class Stat {
        public int st_mode;
        public long st_dev;
        public long st_ino;
        public long st_nlink;
        public int st_uid;
        public int st_gid;
    }

    public static Stat getStat(File file) {
        if (Build.VERSION.SDK_INT >= 21) {
            try {
                StructStat osStat = Os.stat(file.getAbsolutePath());
                Stat stat = new Stat();
                stat.st_mode = osStat.st_mode;
                stat.st_dev = osStat.st_dev;
                stat.st_ino = osStat.st_ino;
                stat.st_nlink = osStat.st_nlink;
                stat.st_uid = osStat.st_uid;
                stat.st_gid = osStat.st_gid;
                return stat;
            } catch (ErrnoException ex) {
                Log.e("OsUtils", ex.getLocalizedMessage());
            }
        }
        return null;
    }

    public static int getPermissions(File file) {
        if (Build.VERSION.SDK_INT >= 21) {
            try {
                StructStat stat = Os.stat(file.getCanonicalPath());
                return stat.st_mode;
            } catch (Exception ex) {
                Log.e("OsUtils", ex.getLocalizedMessage());
            }
        }
        return -1;
    }
}
