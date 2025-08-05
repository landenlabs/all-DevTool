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
import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;

/**
 * Created by Dennis Lang on 2/10/2015.
 */
public class SoundMeter {

    private MediaRecorder mRecorder = null;

    public void start(Context context)  {
        if (mRecorder == null) {
            // Alternate approach is to use AudioRecorder
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            // Must be a real file, this no longer works.
            // mRecorder.setOutputFile("/dev/null");

            try {
                try {
                    File tempFile = File.createTempFile("temp_audio", ".dat", context.getCacheDir());
                    // IMPORTANT: Schedule for deletion when the JVM terminates.
                    // This is a best-effort mechanism.
                    tempFile.deleteOnExit();
                    mRecorder.setOutputFile(tempFile);
                } catch (Exception ignore) {
                    mRecorder.setOutputFile("temp_audio.dat");
                }

                mRecorder.prepare();
                mRecorder.start();
            } catch (Exception ex) {
                Log.e("soundMeter", "getMaxAmplitude", ex);
            }
        }
    }

    public void stop() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    public double getAmplitude() {
        if (mRecorder != null)
            try {
                return mRecorder.getMaxAmplitude();
            } catch (Exception ex) {
                Log.e("soundMeter", "getMaxAmplitude", ex);
                return 0;
            }
        else
            return 0;
    }
}