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

import android.content.Context;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static android.system.Os.sysconf;


/**
 * Created by Dennis Lang on 2/23/17.
 */

@SuppressWarnings({"OctalInteger", "UnnecessaryLocalVariable"})
public class SysUtils {


    static final int pid = 1;
    static final int comm = 2;
    static final int state = 3;
    static final int ppid  = 4;
    static final int pgrp = 5;
    static final int session = 6;
    static int tty_nr = 7;
    static final int tpgid = 8;
    static final int flags = 9;
    static final int minflt = 10;
    static final int cminflt = 11;
    static final int majflt = 12;
    static final int cmajflt = 13;
    static final int utime = 14;
    static final int stime = 15;
    static final int cutime = 16;
    static final int cstime = 17;
    static final int priority = 18;
    static final int nice = 19;
    static final int num_threads = 20;
    static final int itrealvalue = 21;
    static final int starttime = 22;
    static final int vsize = 23;
    static final int rss = 24;
    static final int rsslim = 25;
    static final int startcode = 26;
    static final int endcode = 27;
    static final int startstack = 28;
    static final int kstkesp = 29;
    static final int kstkeip = 30;
    static final int signal = 31;
    static final int blocked = 32;
    static final int sigignore = 33;
    static final int sigcatch = 34;
    static final int wchan = 35;
    static final int nswap = 36;
    static final int cnswap = 37;
    static final int exit_signal = 38;
    static final int processor = 39;
    static final int rt_priority = 40;
    static final int policy = 41;
    static final int delayacct_blk = 42;
    static final int guest_time = 43;
    static final int cguest_time = 44;
    static final int start_data = 45;
    static final int end_data = 46;
    static final int start_brk = 47;


    /**
     * Extract Start Time from /proc/[pid]/stat file
     *
     * @return  Array of Proc Stat structure.
     *
     */
    public static String[] getProcStat(final int pid) {
        /*  http://man7.org/linux/man-pages/man5/proc.5.html

        (1) pid  %d
        The process ID.

        (2) comm  %s
        The filename of the executable, in parentheses.
                This is visible whether or not the executable is
        swapped out.

        (3) state  %c
        One of the following characters, indicating process
        state:

        R Running
        S  Sleeping in an interruptible wait
        D  Waiting in uninterruptible disk sleep
        Z  Zombie
        T  Stopped (on a signal) or (before Linux 2.6.33) trace stopped
        t  Tracing stop (Linux 2.6.33 onward)
        W  Paging (only before Linux 2.6.0)
        X  Dead (from Linux 2.6.0 onward)
        x  Dead (Linux 2.6.33 to 3.13 only)
        K  Wakekill (Linux 2.6.33 to 3.13 only)
        W  Waking (Linux 2.6.33 to 3.13 only)
        P  Parked (Linux 3.9 to 3.13 only)

        (4) ppid  %d
        The PID of the parent of this process.

        (5) pgrp  %d
        The process group ID of the process.

        (6) session  %d
        The session ID of the process.

        (7) tty_nr  %d
        The controlling terminal of the process.  (The minor
        device number is contained in the combination of
        bits 31 to 20 and 7 to 0; the major device number is
        in bits 15 to 8.)

        (8) tpgid  %d
        The ID of the foreground process group of the
        controlling terminal of the process.

        (9) flags  %u
        The kernel flags word of the process.  For bit
        meanings, see the PF_* defines in the Linux kernel
        source file include/linux/sched.h.  Details depend
        on the kernel version.

        The format for this field was %lu before Linux 2.6.

        (10) minflt  %lu
        The number of minor faults the process has made
        which have not required loading a memory page from
        disk.

        (11) cminflt  %lu
        The number of minor faults that the process's
        waited-for children have made.

        (12) majflt  %lu
        The number of major faults the process has made
        which have required loading a memory page from disk.

        (13) cmajflt  %lu
        The number of major faults that the process's
        waited-for children have made.

        (14) utime  %lu
        Amount of time that this process has been scheduled
        in user mode, measured in clock ticks (divide by
                sysconf(_SC_CLK_TCK)).  This includes guest time,
                guest_time (time spent running a virtual CPU, see
                        below), so that applications that are not aware of
        the guest time field do not lose that time from
        their calculations.

        (15) stime  %lu
        Amount of time that this process has been scheduled
        in kernel mode, measured in clock ticks (divide by ysconf(_SC_CLK_TCK)).

        (16) cutime  %ld
        Amount of time that this process's waited-for
        children have been scheduled in user mode, measured
        in clock ticks (divide by sysconf(_SC_CLK_TCK)).
        (See also times(2).)  This includes guest time,
        cguest_time (time spent running a virtual CPU, see  below).

        (17) cstime  %ld
        Amount of time that this process's waited-for
        children have been scheduled in kernel mode,
        measured in clock ticks (divide by sysconf(_SC_CLK_TCK)).

        (18) priority  %ld
        (Explanation for Linux 2.6) For processes running a
        real-time scheduling policy (policy below; see
        sched_setscheduler(2)), this is the negated
        scheduling priority, minus one; that is, a number in
        the range -2 to -100, corresponding to real-time
        priorities 1 to 99.  For processes running under a
        non-real-time scheduling policy, this is the raw
        nice value (setpriority(2)) as represented in the
        kernel.  The kernel stores nice values as numbers in
        the range 0 (high) to 39 (low), corresponding to the
        user-visible nice range of -20 to 19.

        Before Linux 2.6, this was a scaled value based on
        the scheduler weighting given to this process.

        (19) nice  %ld
        The nice value (see setpriority(2)), a value in the
        range 19 (low priority) to -20 (high priority).

        (20) num_threads  %ld
        Number of threads in this process (since Linux 2.6).
        Before kernel 2.6, this field was hard coded to 0 as
        a placeholder for an earlier removed field.

        (21) itrealvalue  %ld
        The time in jiffies before the next SIGALRM is sent
        to the process due to an interval timer.Since
        kernel 2.6.17, this field is no longer maintained,  and is hard coded as 0.

        (22) starttime  %llu
        The time the process started after system boot.  In
        kernels before Linux 2.6, this value was expressed
        in jiffies.  Since Linux 2.6, the value is expressed
        in clock ticks (divide by sysconf(_SC_CLK_TCK)).
        The format for this field was %lu before Linux 2.6.

        (23) vsize  %lu
        Virtual memory size in bytes.

        (24) rss  %ld
        Resident Set Size: number of pages the process has
        in real memory.  This is just the pages which count
        toward text, data, or stack space.  This does not
        include pages which have not been demand-loaded in,  or which are swapped out.

        (25) rsslim  %lu
        Current soft limit in bytes on the rss of the
        process; see the description of RLIMIT_RSS in getrlimit(2).

        (26) startcode  %lu  [PT]
        The address above which program text can run.

        (27) endcode  %lu  [PT]
        The address below which program text can run.

        (28) startstack  %lu  [PT]
        The address of the start (i.e., bottom) of the
        stack.

        (29) kstkesp  %lu  [PT]
        The current value of ESP (stack pointer), as found
        in the kernel stack page for the process.

        (30) kstkeip  %lu  [PT]
        The current EIP (instruction pointer).

        (31) signal  %lu
        The bitmap of pending signals, displayed as a
        decimal number.  Obsolete, because it does not
        provide information on real-time signals; use   /proc/[pid]/status instead.

        (32) blocked  %lu
        The bitmap of blocked signals, displayed as a
        decimal number.  Obsolete, because it does not
        provide information on real-time signals; use  /proc/[pid]/status instead.

        (33) sigignore  %lu
        The bitmap of ignored signals, displayed as a
        decimal number.  Obsolete, because it does not
        provide information on real-time signals; use /proc/[pid]/status instead.

        (34) sigcatch  %lu
        The bitmap of caught signals, displayed as a decimal
        number.  Obsolete, because it does not provide
        information on real-time signals; use  /proc/[pid]/status instead.

        (35) wchan  %lu  [PT]
        This is the "channel" in which the process is
        waiting.  It is the address of a location in the
        kernel where the process is sleeping.  The
        corresponding symbolic name can be found in /proc/[pid]/wchan.

        (36) nswap  %lu
        Number of pages swapped (not maintained).

        (37) cnswap  %lu
        Cumulative nswap for child processes (not  maintained).

        (38) exit_signal  %d  (since Linux 2.1.22)
        Signal to be sent to parent when we die.

        (39) processor  %d  (since Linux 2.2.8)
        CPU number last executed on.

        (40) rt_priority  %u  (since Linux 2.5.19)
        Real-time scheduling priority, a number in the range
        1 to 99 for processes scheduled under a real-time
        policy, or 0, for non-real-time processes (see  sched_setscheduler(2)).

        (41) policy  %u  (since Linux 2.5.19)
        Scheduling policy (see sched_setscheduler(2)).
        Decode using the SCHED_* constants in linux/sched.h.

        The format for this field was %lu before Linux 2.6.22.

        (42) delayacct_blkio_ticks  %llu  (since Linux 2.6.18)
        Aggregated block I/O delays, measured in clock ticks (centiseconds).

        (43) guest_time  %lu  (since Linux 2.6.24)
        Guest time of the process (time spent running a
        virtual CPU for a guest operating system), measured
        in clock ticks (divide by sysconf(_SC_CLK_TCK)).

        (44) cguest_time  %ld  (since Linux 2.6.24)
        Guest time of the process's children, measured in
        clock ticks (divide by sysconf(_SC_CLK_TCK)).

        (45) start_data  %lu  (since Linux 3.3)  [PT]
        Address above which program initialized and
        uninitialized (BSS) data are placed.

        (46) end_data  %lu  (since Linux 3.3)  [PT]
        Address below which program initialized and
        uninitialized (BSS) data are placed.

        (47) start_brk  %lu  (since Linux 3.3)  [PT]
        Address above which program heap can be expanded
        with brk(2).

        */

        // value in /proc/[PID]/stat file driectory is in clock ticks,
        // 100 is used to convert clock ticks to secs
        // final long SYSTEM_CLK_TCK = 100;
        // final int fieldStartTime = 21;  // Field 22 of the /proc/[PID]/stat file driectory

        final String path = ("/proc/" + pid + "/stat");
        final String stat;
        final String fieldSep = " ";


        long SYSTEM_CLK_TCK = 100;
        if (Build.VERSION.SDK_INT >= 21) {
            SYSTEM_CLK_TCK = sysconf(android.system.OsConstants._SC_CLK_TCK);
        }

        try {
            try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
                stat = reader.readLine();
            }

            // Example
            // 21999 (android.weather) S 355 355 0 0 -1 4194624 38743 0 1 0 135 20 0 0 20 0 22 0 9653936 1797201920 25056 4294967295 3069771776 3069789584 3198867776 3198863800 3066840876 0 6660 0 38136 4294967295 0 0 17 2 0 0 0 0 0 3069795544 3069796352 3072176128

            final String[] fields = stat.split(fieldSep);
            // final long startTimeTicks = Long.parseLong(fields[fieldStartTime]);
            // return startTimeTicks * 1000 / SYSTEM_CLK_TCK;
            return fields;
        } catch (IOException ex) {
            return new String[0];
        }
    }

    // =============================================================================================


    private static boolean isBit(int val, int mask) {
        return (val & mask) != 0;
    }

    public static String getPermissionString(int mode) {
        char r = '-';
        char w = '-';
        char x = '-';

        if (mode != -1) {
            int owner = mode & 0700;
            // int group = mode & 0070;
            int world = mode & 0007;

            r = isBit(owner, 0400) ? 'r' : '-';
            w = isBit(owner, 0200) ? 'w' : '-';
            x = isBit(owner, 0100) ? 'x' : '-';

            r = isBit(world, 0004) ? 'R' : r;
            w = isBit(world, 0002) ? 'W' : w;
            x = isBit(world, 0001) ? 'X' : x;
        }

        String rwStr = String.format("[%c%c%c] ", r,w, x);
        return rwStr;
    }


    @NonNull
    public static  <T> T getServiceSafe(Context context, String service) {
        //noinspection unchecked
        return (T) Objects.requireNonNull(context.getSystemService(service));
    }

    @NonNull
    public static Map<String, String> getShellCmd(String[] shellCmd) {
        Map<String, String> mapList = new LinkedHashMap<>();
        ArrayList<String> responseList = runShellCmd(shellCmd);
        for (String line : responseList) {
            String[] vals = line.split(": ");
            if (vals.length > 1) {
                mapList.put(vals[0], vals[1]);
            } else {
                mapList.put(line, "");
            }
        }
        return mapList;
    }


    public static ArrayList<String> runShellCmd(String[] shellCmd) {
        ArrayList<String> list = new ArrayList<>();
        try {
            Process process = new ProcessBuilder()
                    .command(shellCmd)
                    .redirectErrorStream(true)
                    .start();

            // Process process = Runtime.getRuntime().exec(shellCmd);
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                list.add(line);
            }
        }
        catch (IOException ex) {
            list.add(ex.getLocalizedMessage());
        }

        return list;
    }

    public static void vibrateDevice(@NonNull Context context, int durationMillis) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(durationMillis,  VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    public static MediaPlayer mediaPlayer;

    @Nullable
    public static MediaPlayer playSound(Context context, String soundType, String assetName) {
        try {
            if (mediaPlayer != null)
                mediaPlayer.release();

            if (TextUtils.isEmpty(assetName)) {
                return null;
            }

            mediaPlayer = new MediaPlayer();
            String path = "android.resource://" + context.getPackageName() + "/raw/" + assetName;
            mediaPlayer.setDataSource(context, Uri.parse(path));
            mediaPlayer.prepare();
            mediaPlayer.start();
            return mediaPlayer;
        } catch (Exception ex) {
            // ALog.e.tagMsg(this, ex);
        }
        return null;
    }


    @Nullable
    public static <T> T playRingtone(Context context) {
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        if (Build.VERSION.SDK_INT >= 28) {
            Ringtone ringtone = RingtoneManager.getRingtone(context, alarmUri);
            ringtone.setLooping(true);
            ringtone.play();
            return (T)ringtone;
        } else {
            try {
                MediaPlayer mediaPlayer = MediaPlayer.create(context, alarmUri);
                // mediaPlayer.setDataSource(context, alarmUri);
                mediaPlayer.setLooping(true);
                // mediaPlayer.prepare();
                mediaPlayer.start();
                return (T)mediaPlayer;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
}
