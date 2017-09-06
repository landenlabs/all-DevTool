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

