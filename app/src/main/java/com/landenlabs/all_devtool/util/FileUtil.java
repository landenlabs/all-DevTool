package com.landenlabs.all_devtool.util;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.system.StructStat;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import static android.content.ContentValues.TAG;

/**
 * Created by Dennis Lang on 7/13/16.
 */
@SuppressWarnings("UnnecessaryLocalVariable")
public class FileUtil {
    public static final int KB = 1 << 10;
    public static final int MB = 1 << 20;
    public static final int GB = 1 << 30;
    public static final double KB_D = 1024.0;

    public static String getMimeType(String url)
    {
        String parts[]=url.split("\\.");
        String extension=parts[parts.length-1];
        String type = null;
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public static String getSizeStr(long size) {
        if (size <= 0)
            return "";
        else if (size < KB)
            return String.format("%d B", size);
        else if (size < MB)
            return String.format("%.1f KB", size / KB_D);
        else if (size < GB)
            return String.format("%.1f MB", size / KB / KB_D);
        else
            return String.format("%.1f GB", size / MB / KB_D);
    }

    public static class FileInfo extends File {
        // public static final int FILESIZE_MAX_DIR_DEPTH = 3;
        public boolean isChecked = false;
        int mFileCount = 0;
        long mDepthSize = 0;


        public FileInfo(String name) {
            super(name);
        }

        public String getLongName() {
            try {
                String path1 = getCanonicalPath();
                String path2 = getAbsolutePath();
                if (!path2.equals(path1)) {
                    return String.format("%s -> %s", getName(), path1);
                }
            } catch (IOException ex) {
                //
            }

            return getName();
        }

        public int getCount() {
            return 0;
        }
        public long getLength() {
            return isDirectory() ? -1 : length();
        }
        public void setFileCnt(int cnt) { mFileCount = cnt;}
        public int getFileCnt() { return mFileCount; }

        public long getDevFreeMB() {
            long free = getUsableSpace();
            return (free != -1) ? free /MB : 0;
        }
        public long getDevSizeMB() {
            long size = getTotalSpace();
            return (size != -1) ? size /MB : 0;
        }

        public long getDeviceId() {
            if (Build.VERSION.SDK_INT >= 21) {
                try {
                    StructStat st = android.system.Os.stat(this.getCanonicalPath());
                    return st.st_dev;
                } catch (Exception ignore) {
                }
            }

            return getTotalSpace() % 100;
        }

        /**
         * Returns the time when this file was last accessed, measured in
         * milliseconds since January 1st, 1970, midnight.
         * Returns 0 if the file does not exist.
         *
         * @return the time when this file was last accessed.
         */
        public long getAtime() {
            if (Build.VERSION.SDK_INT >= 21) {
                try {
                    StructStat st = android.system.Os.stat(this.getCanonicalPath());
                    return st.st_atime * 1000L;
                } catch (Exception ignore) {
                }
            }

            return 0;
        }


        public long getDepthSize() {
            return mDepthSize;
        }

        /**
         * Follow subdirectories and compute total file sizes, up to a maximum of <b>maxFiles</b>
         * files.
         *
         * @param fileCnt  Caller pass in -1. Used internally during recursion.
         * @return  Total file size of files encountered during recursion, stopping at maxFiles.
         */
        public long findDepthSize(int fileCnt, int maxFiles) {
            if (fileCnt > maxFiles)
                return mDepthSize;
            else if (fileCnt == 0 && mDepthSize != 0) {
                return mDepthSize;
            } else if (fileCnt == -1) {
                mDepthSize = 0;
                fileCnt = 0;
            } else if (this.getAbsolutePath().startsWith("/proc")) {
                mDepthSize = 0;
                return mDepthSize;
            }

            try {
                File[] files = this.listFiles();
                if (files != null) {
                    for (File file : files) {
                        try {
                            FileUtil.FileInfo fileInfo = new FileUtil.FileInfo(file.getAbsolutePath());
                            if (fileInfo.isDirectory()) {
                                //noinspection ResultOfMethodCallIgnored
                                fileInfo.findDepthSize(fileCnt + 1, fileCnt);
                            } else if (file.isFile()) {
                                mDepthSize += fileInfo.length();
                                if (fileCnt++ > maxFiles) {
                                    return mDepthSize;
                                }
                            }
                        } catch (Exception ex) {
                            Log.e("FileUtil", ex.getLocalizedMessage(), ex);
                        }
                    }
                }
            } catch (Exception ex) {
                Log.e("FileUtil", ex.getLocalizedMessage(), ex);
            }

            return mDepthSize;
        }
    }

    // android.support.v7.widget.AppCompatButton
    public static class DirInfoButton extends android.support.v7.widget.AppCompatButton {

        File m_dir;
        public DirInfoButton(Context context, File dir) {
            super(context,null, android.R.attr.buttonStyle);
            setText("/" + dir.getName());
            m_dir = dir;
        }

        public File getDir() {
            return m_dir;
        }
    }

    // =============================================================================================

    public interface ExecCallback {
        void Exec(StringBuilder result, int flag);
    }

    /**
     * Async Task which continuously reads LogCat output and updates TextView and advances scrollView.
     * Call must call execute() to start task.
     *
     * @param callable - callback when command is done.
     * @return Created async task.
     */
    public static AsyncTask<Void, String, Void> getAsyncExec(
            final ExecCallback callable,
            final StringBuilder resultSb,
            final String[] cmd)  {
        AsyncTask<Void, String, Void> asyncLogCat =
                new AsyncTask<Void, String, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            Process process = Runtime.getRuntime().exec(cmd);
                            BufferedReader bufferedReader = new BufferedReader(
                                    new InputStreamReader(process.getInputStream()));
                            BufferedReader bufferedReaderErr = new BufferedReader(
                                    new InputStreamReader(process.getErrorStream()));

                            String line;
                            while ((line = bufferedReader.readLine()) != null) {
                                if (line.trim().length() > 2) {
                                    publishProgress(line);
                                }
                            }

                            while ((line = bufferedReaderErr.readLine()) != null) {
                                if (line.trim().length() > 2) {
                                    publishProgress(line);
                                }
                            }
                        }
                        catch (IOException ex) {
                            Log.e(TAG, ex.getMessage());
                        }
                        return null;
                    }

                    @Override
                    protected void onProgressUpdate(String... values) {
                        resultSb.append(values[0]).append("\n");
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        callable.Exec(resultSb, 0);
                    }
                };

        return asyncLogCat;
    }

}
