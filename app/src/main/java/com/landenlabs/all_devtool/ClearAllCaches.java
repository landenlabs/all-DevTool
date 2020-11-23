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

/**
 * Created by Dennis Lang on 7/18/17.
 */

/*
public class ClearAllCaches implements Runnable {
    static boolean aOkay = false;
    final String TAG = "ClearAllCaches";
    private boolean isStarted = false;
    private Context mContext;   // e
    private int retryCnt = 0;
    

    public static void deleteTree(File dirFile)
    {
        if (!dirFile.exists()) {
            return;
        }

        for (File file : dirFile.listFiles()) {
            if (file.isDirectory()) {
                deleteTree(file);
            } else {
                file.delete();
            }
        }
    }


    public ClearAllCaches(Context context, int paramInt)
    {
        mContext = context;     // e
        retryCnt = paramInt;
    }

    static void deleteFiles()
    {
        if (Environment.getExternalStorageState().equals("mounted"))
        {
            File localObject = Environment.getExternalStorageDirectory();
            if (localObject != null)
            {
                String absPath = localObject.getAbsolutePath();
                File dataDir = new File(absPath + "/Android/data/");
                if (dataDir.exists())
                {
                    File[] files = dataDir.listFiles();
                    if (files != null)
                    {
                        for (int i = 0; i < files.length; i++)
                        {
                            File localFile = files[i];
                            if (localFile.isDirectory()) {
                                localFile = new File(localFile.getAbsolutePath() + "/cache/");
                            }

                            try {
                                deleteTree(localFile);
                                i += 1; 
                            } catch (Exception localException){
                                Log.d("ClearAllCache", localException.getMessage());
                            }
                        }
                    }
                }
            }
        }
    }

    static class Observer extends IPackageDataObserver.Stub
    {
        public void onRemoveCompleted(String paramString, boolean paramBoolean)
        {
            Log.d("ClearAllCacheTask", "result: " + paramString + " " + paramBoolean);
            ClearAllCaches.aOkay = true;
        }
    }


    public static boolean clear(Context context)
    {
        long lValue = 2147483647L;
        if (context == null) {
            return false;
        }
        deleteFiles();

        try
        {
            Method localMethod = Class.forName("android.content.pm.PackageManager")
                    .getDeclaredMethod("freeStorageAndNotify",
                            new Class[] { Long.TYPE, Class.forName("android.content.pm.IPackageDataObserver") });
            localMethod.setAccessible(true);
            aOkay = false;
            Observer localObserver = new Observer();
            try
            {
                localMethod.invoke(context.getPackageManager(), new Object[] { lValue, localObserver });
                while (!aOkay) {
                    Thread.sleep(10L);
                }
            }
            catch (Exception localException1)
            {
                for (;;)
                {
                    aOkay = true;
                }
                aOkay = false;
                try
                {
                    localMethod.invoke(context.getPackageManager(), new Object[] { lValue * 20L, localObserver });
                    while (!aOkay) {
                        Thread.sleep(10L);
                    }
                }
                catch (Exception localException2)
                {
                    for (;;)
                    {
                        aOkay = true;
                    }
                    aOkay = false;
                    try
                    {
                        localMethod.invoke(context.getPackageManager(), new Object[] { lValue * 200L, localObserver });
                        while (!aOkay) {
                            Thread.sleep(10L);
                        }
                    }
                    catch (Exception ex)
                    {
                        for (;;)
                        {
                            aOkay = true;
                        }
                    }
                    return true;
                }
            }
            return false;
        }
        catch (Exception ex) {

        }
    }

    public void run()
    {
        isStarted = true;
        if (retryCnt == 0)
        {
            clear(mContext);
        }
    }
}
*/

