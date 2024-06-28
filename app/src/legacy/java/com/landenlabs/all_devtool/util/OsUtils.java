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

package com.landenlabs.all_devtool.util;

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
        return null;
    }

    public static int getPermissions(File file) {
        int owner = 0;
        owner |= file.canRead() ? 4:0;
        owner |= file.canWrite() ? 2:0;
        owner |= file.canExecute()?  1:0;

        int world = 0;
        world |= file.setReadable(true, false) ? 0400 : 0;
        world |= file.setWritable(true, false) ? 0200 : 0;
        world |= file.setExecutable(true, false) ? 0100 : 0;

        return owner | world;
    }
}