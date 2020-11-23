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
 * @see http://LanDenLabs.com/
 */

package com.landenlabs.all_devtool;

import android.os.Bundle;

import java.util.Date;

/**
 * Created by Dennis Lang on 2/7/2015.
 */

public class ConsoleState {

    public final static ConsoleState s_consoleState = new ConsoleState();

    public Date lastFreeze = new Date();
    public long usingMemory = 0;   // Current amount of memory we are using.
    public long freeMemory = 0;    // Free memory (available to use)
    public long totalMemory = 0;   // Total accessible memory

    private static final String KEY_CONSOLE = "Console";
    private static final String KEY_DATE1 = "Date1";
    private static final String KEY_USING = "Using";
    private static final String KEY_FREE = "Free";
    private static final String KEY_TOTAL = "Total";

    public long netRxBytes = 0;
    public long netRxPacks = 0;
    public long netTxBytes = 0;
    public long netTxPacks = 0;

    private static final String KEY_NET_RX_BYTES = "NetRxBytes";
    private static final String KEY_NET_RX_PACKS = "NetRxPacks";
    private static final String KEY_NET_TX_BYTES = "NetTxBytes";
    private static final String KEY_NET_TX_PACKS = "NetTxPacks";

    public long processCnt = 0;
    public long batteryLevel = 0;

    private static final String KEY_PROCESS_CNT = "ProcessCnt";
    private static final String KEY_BATTERY_LEVEL = "BatteryLevel";

    public void saveInstanceState(Bundle outState) {
        if (outState != null) {
            Bundle state = new Bundle();
            state.putLong(KEY_DATE1, lastFreeze.getTime());
            state.putLong(KEY_USING, usingMemory);
            state.putLong(KEY_FREE, freeMemory);
            state.putLong(KEY_TOTAL, totalMemory);

            state.putLong(KEY_NET_RX_BYTES, netRxBytes);
            state.putLong(KEY_NET_RX_PACKS, netRxPacks);
            state.putLong(KEY_NET_TX_BYTES, netTxBytes);
            state.putLong(KEY_NET_TX_PACKS, netTxPacks);

            state.putLong(KEY_PROCESS_CNT, processCnt);
            state.putLong(KEY_BATTERY_LEVEL, batteryLevel);

            outState.putBundle(KEY_CONSOLE, state);
        }
    }

    public void restoreInstanceState(Bundle inState) {
        if (inState != null) {
            Bundle state = inState.getBundle(KEY_CONSOLE);
            if (state != null) {
                lastFreeze.setTime(state.getLong(KEY_DATE1));
                usingMemory = state.getLong(KEY_USING);
                freeMemory = state.getLong(KEY_FREE);
                totalMemory = state.getLong(KEY_TOTAL);

                netRxBytes = state.getLong(KEY_NET_RX_BYTES);
                netRxPacks = state.getLong(KEY_NET_RX_PACKS);
                netTxBytes = state.getLong(KEY_NET_TX_BYTES);
                netTxPacks = state.getLong(KEY_NET_TX_PACKS);

                processCnt = state.getLong(KEY_PROCESS_CNT);
                batteryLevel = state.getLong(KEY_BATTERY_LEVEL);
            }
        }
    }
}
