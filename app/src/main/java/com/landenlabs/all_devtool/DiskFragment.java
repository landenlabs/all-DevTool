package com.landenlabs.all_devtool;

/*
 * Copyright (c) 2016 Dennis Lang (LanDen Labs) landenlabs@gmail.com
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

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.landenlabs.all_devtool.dialogs.FileBrowseDialog;
import com.landenlabs.all_devtool.util.OsUtils;
import com.landenlabs.all_devtool.util.Ui;
import com.landenlabs.all_devtool.util.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static com.landenlabs.all_devtool.FileBrowserFragment.isBit;
import static com.landenlabs.all_devtool.util.SysUtils.runShellCmd;

/**
 * Display "Build" system information.
 *
 * @author Dennis Lang
 */
public class DiskFragment extends DevFragment {

    final ArrayList<GroupInfo> m_list = new ArrayList<GroupInfo>();
    ExpandableListView m_listView;
    TextView m_titleTime;
    CheckBox m_writeGrantedCb;
    CheckBox m_diskUsageCb;
    CheckBox m_fileSystemCb;
    CheckBox m_diskStatsCb;

    Map<String, String> m_javaDirList;
    Map<String, String> m_duList;
    Map<String, String> m_lsList;
    Map<String, String> m_duMntList;
    Map<String, String> m_mntList;
    Map<String, String> m_duStorageList;
    Map<String, String> m_duSdcardList;
    Map<String, String> m_dfList;

    Map<String, String> m_diskDumpStats;
    ArrayList<String> m_diskProcStats;
    
    private static SimpleDateFormat m_timeFormat = new SimpleDateFormat("HH:mm:ss zz");

    public static String s_name = "Disk";

    public DiskFragment() {
    }

    public static DevFragment create() {
        return new DiskFragment();
    }

    // ============================================================================================
    // DevFragment methods

    @Override
    public String getName() {
        return s_name;
    }

    @Override
    public List<Bitmap> getBitmaps(int maxHeight) {
        return Utils.getListViewAsBitmaps(m_listView, maxHeight);
    }

    @Override
    public List<String> getListAsCsv() {
        return Utils.getListViewAsCSV(m_listView);
    }

    // ============================================================================================
    // Fragment methods

    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View rootView = inflater.inflate(R.layout.disk_tab, container, false);
        Ui.<TextView>viewById(rootView, R.id.disklist_title).setText(R.string.disk_title);
        m_listView = Ui.viewById(rootView, R.id.diskListView);

        m_titleTime = Ui.viewById(rootView, R.id.disklist_time);
        m_titleTime.setVisibility(View.VISIBLE);
        m_titleTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateList(true);
                m_listView.invalidateViews();
            }
        });

        m_writeGrantedCb = Ui.viewById(rootView, R.id.diskGrantCb);
        m_writeGrantedCb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_writeGrantedCb.isChecked()) {
                    grantWritePermission();
                    updateList(true);
                } else {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                    startActivity(intent);
                }
            }
        });

        m_writeGrantedCb.setChecked(hasWritePermission());


        m_diskUsageCb = Ui.viewById(rootView, R.id.diskUsageCb);
        m_diskUsageCb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateList(true);
            }
        });

        m_fileSystemCb = Ui.viewById(rootView, R.id.fileSystemCb);
        m_fileSystemCb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateList(true);
            }
        });

        m_diskStatsCb = Ui.viewById(rootView, R.id.diskStatsCb);
        m_diskStatsCb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateList(true);
            }
        });

        m_listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (view == null)
                    return false;

                final TextView field = Ui.viewById(view, R.id.buildField);
                final TextView value = Ui.viewById(view, R.id.buildValue);
                if (field != null && value != null) {
                    Button btn = Ui.ShowMessage(DiskFragment.this.getActivity(), field.getText() + "\n" + value.getText()).getButton(
                            AlertDialog.BUTTON_POSITIVE);
                    if (btn != null) {
                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String[] parts = value.getText().toString().split(" ");
                                if (parts.length == 2) {
                                    fireIntentOn(parts[1]);
                                }
                            }
                        });
                    }
                    /*
                    ShowMessage(field.getText() + "\n" + value.getText()).setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            Button btn = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                            if (btn != null) {
                                btn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        fireIntentOn(field.getText().toString(), value.getText().toString(), grpPos);
                                    }
                                });
                            }
                        }
                    });
                    */

                }
                return false;
            }
        });
        return rootView;
    }

    // Coming into forground - update list.
    @Override
    public void onResume() {
        super.onResume();
        updateList(false);
    }

    // ============================================================================================
    // Permission

    private boolean hasWritePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            return getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }
    private void grantWritePermission() {
        checkPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // checkPermissions(Manifest.permission.DUMP);
    }

    @Override
    protected boolean checkPermissions(String needPermission) {
        boolean okay = true;
        if (Build.VERSION.SDK_INT >= 23) {
            if (getContext().checkSelfPermission(needPermission) != PackageManager.PERMISSION_GRANTED) {
                okay = false;
                m_writeGrantedCb.setChecked(false);
            //    m_writeGrantedCb.setText("Grant Write [Failed]");
                requestPermissions(new String[]{ needPermission }, MY_PERMISSIONS_REQUEST);
            } else {
            //    m_writeGrantedCb.setText("Grant Write");
                m_writeGrantedCb.setChecked(true);
                updateList(true);
            }
        }

        return okay;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (grantResults != null)
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    m_writeGrantedCb.setText("Grant Write");
                    m_writeGrantedCb.setChecked(true);
                } else {
                    m_writeGrantedCb.setText("Grant Write [Failed]");
                    m_writeGrantedCb.setChecked(false);
                }
                updateList(true);
            }
        }
    }

    // ============================================================================================
    // Internal methods

    /**
     * Populate list with 'Build' parameters.
     */
    void updateList(boolean force) {
        if (!m_list.isEmpty() && !force)
            return;
        
        // Time today = new Time(Time.getCurrentTimezone());
        // today.setToNow();
        // today.format(" %H:%M:%S")
        Date dt = new Date();
        m_titleTime.setText(m_timeFormat.format(dt));

        m_list.clear();
        m_writeGrantedCb.setChecked(hasWritePermission());
        addString("Permission", hasWritePermission() ? "Granted Write" : "Denied Write");

        if (true) {
            m_javaDirList = new LinkedHashMap<>();
            try {
                if (Build.VERSION.SDK_INT >= 24) {
                    addFile("getFilesDir", getActivity().getApplicationContext().getDataDir());
                }
                try {
                    addFile("getDir(null)", getContext().getDir(null, Context.MODE_WORLD_READABLE));
                } catch (Exception ex) {
                }
                try {
                    addFile("getFilesDir", getActivity().getFilesDir());
                } catch (Exception ex) {
                }

                try {
                    addFile("getCacheDir", getActivity().getCacheDir());
                } catch (Exception ex) {
                }

                if (false) {
                    try {
                        String FILENAME = "test.txt";
                        String string = "hello world!";

                        FileOutputStream fos = getContext().openFileOutput(FILENAME, Context.MODE_PRIVATE);
                        fos.write(string.getBytes());
                        addString("openFileOutput", fos.toString());
                        fos.close();
                    } catch (Exception ex) {
                    }
                }

                addString("External State", Environment.getExternalStorageState());
                
                try {
                    addFile("getExternalCacheDir", getActivity().getExternalCacheDir());
                } catch (Exception ex) {
                }

                try {
                    // getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    addFile("getExternalCacheDir", getActivity().getExternalFilesDir(null));
                } catch (Exception ex) {
                }

                addFile("getExternalStorageDirectory", Environment.getExternalStorageDirectory());
                m_javaDirList.put("isExternalStorageEmulated", (Environment.isExternalStorageEmulated() ? "yes" : "no"));
                m_javaDirList.put("isExternalStorageRemovable", (Environment.isExternalStorageRemovable() ? "yes" : "no"));
                addFile("external downloads", Environment.getExternalStoragePublicDirectory(
                        DIRECTORY_DOWNLOADS));
                addFile("getRootDirectory", Environment.getRootDirectory());


            } catch (Exception ex) {
                addString("Exception", ex.getMessage());
            }

            addString("java dir\n[rwx]=owner [RWX]=world", m_javaDirList);
        }

        if (true) {
            if (false) {
                m_lsList = getShellCmd(new String[]{"ls", "-l"});
                addString("ls -l", m_lsList);
            }
            if (false) {
                m_duList = getFileList(new String[] {"du", "-ks", "/"}, "[^ ]+ ([^:]+).*", "$1", ".*/(proc|acct|dev)/.*");
                addString("du -ks /", m_duList);
            }


            if (m_fileSystemCb.isChecked()) {
                m_dfList = getShellCmd(new String[] { "df" });
                addString("df", m_dfList);

                m_mntList = getShellCmd(new String[] {"mount"});
                addString("mount", m_mntList);
            }

            if (m_diskUsageCb.isChecked()) {
                m_duStorageList = getShellCmd(new String[]{"du", "-chHLd", "2", "/storage"});
                addString("du -chHLd 2 /storage", m_duStorageList);

                m_duMntList = getShellCmd(new String[]{"du", "-chHLd", "1", "/mnt/"});
                addString("du -chHLs /mnt", m_duMntList);

                m_duSdcardList = getShellCmd(new String[] { "du", "-chHL", "/sdcard/" });
                addString("du -chHL /sdcard", m_duSdcardList);
            }

            if (m_diskStatsCb.isChecked()) {
                // m_diskDumpStats = getShellCmd(new String[]{"dumpsys", "diskstats"});
                // addString("dumpsys diskstats", m_diskDumpStats);
                m_diskProcStats = readFile("/proc/diskstats", " ", 1);
                Map<String, String> diskProcStatsMap = new LinkedHashMap<>();
                int rowCnt = 0;
                for (String rowStr : m_diskProcStats) {
                    diskProcStatsMap.put(String.format("%3d", rowCnt), rowStr);
                    rowCnt++;
                }
                addString("/proc/diskstats/", diskProcStatsMap);
            }

            /*
            TODO

            readFile("/proc/partitions");  // grep for ext4
            readFile("/proc/mounts");      // assocuated partions with mounts to get disk layout
            */
        }


        final BuildArrayAdapter adapter = new BuildArrayAdapter(this.getActivity());
        m_listView.setAdapter(adapter);

        int count = adapter.getGroupCount();
        for (int position = 0; position < count; position++)
            m_listView.expandGroup(position);

        m_listView.invalidate();
    }


    void addFile(String name, File file) {
        if (file != null) {
            char r = file.canRead() ? 'r' : '-';
            char w = file.canWrite() ? 'w' : '-';
            char x = file.canExecute() ? 'x' : '-';
            r = file.setReadable(true, false) ? 'R' : r;
            w = file.setWritable(true, false) ? 'W' : w;
            x = file.setExecutable(true, false) ? 'X' : x;

            int mode = OsUtils.getPermissions(file);
            if (mode != -1) {
                int owner = mode & 0700;
                int group = mode & 0070;
                int world = mode & 0007;

                r = isBit(owner, 0400) ? 'r' : '-';
                w = isBit(owner, 0200) ? 'w' : '-';
                x = isBit(owner, 0100) ? 'x' : '-';

                r = isBit(world, 0004) ? 'R' : r;
                w = isBit(world, 0002) ? 'W' : w;
                x = isBit(world, 0001) ? 'X' : x;
            }

            String rwStr = String.format("[%c%c%c] ", r,w, x);
            m_javaDirList.put(name, rwStr + file.getAbsolutePath());
        }
    }


    void addString(String name, String value) {
        if (!TextUtils.isEmpty(value))
            m_list.add(new GroupInfo(name, value.trim()));
    }

    void addString(String name, Map<String, String> value) {
        if (!value.isEmpty())
            m_list.add(new GroupInfo(name, value));
    }

    private Map<String, String> getShellCmd(String[] shellCmd) {
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

    private Map<String, String> getFileList(String[] shellCmd, String regStr, String repStr, String excPat) {
        Map<String, String> mapList = new LinkedHashMap<>();
        ArrayList<String> responseList = runShellCmd(shellCmd);
        for (String line : responseList) {
            if (!line.matches(excPat)) {
                line = line.replaceAll(regStr, repStr);
                if (line.length() > 0) {
                    try {
                        File f1 = new File(line);
                        mapList.put(line, "" + f1.length());
                    } catch (Exception ex) {
                        mapList.put(line, ex.getMessage());
                    }
                }
            }
        }
        return mapList;
    }



    /**
     *
     * @param diskFileName example /proc/cpuinfo
     * @param splitPat Ex  " " or ": "
     * @param splitMinCnt   Ex 1
     *
     * @return
     */
    private static ArrayList<String> readFile(String diskFileName, String splitPat, int splitMinCnt) {
        ArrayList<String> list = new ArrayList<String>();
        try {
            Scanner scan = new Scanner(new File(diskFileName));
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                String[] vals = line.split(splitPat);
                if (vals.length > splitMinCnt) {
                    list.add(line);
                    // map.put(vals[0].trim(), vals[1].trim());
                }
            }
        } catch (Exception e) {
            Log.e("readFile",Log.getStackTraceString(e));}
        return list;
    }

    // =============================================================================================


    class GroupInfo {
        final String m_fieldStr;
        final String m_valueStr;
        final Map<String, String> m_valueList;

        GroupInfo() {
            m_fieldStr = m_valueStr = null;
            m_valueList = null;
        }

        GroupInfo(String str1, String str2) {
            m_fieldStr = str1;
            m_valueStr = str2;
            m_valueList = null;
        }

        GroupInfo(String str1, Map<String, String> list2) {
            m_fieldStr = str1;
            m_valueStr = null;
            m_valueList = list2;
        }

        public String toString() {
            return m_fieldStr;
        }

        public String fieldStr() {
            return m_fieldStr;
        }

        public String valueStr() {
            return m_valueStr;
        }

        public Map<String, String> valueListStr() {
            return m_valueList;
        }

        public int getCount() {
            return (m_valueList == null) ? 0 : m_valueList.size();
        }
    }


    // =============================================================================================

    final static int EXPANDED_LAYOUT = R.layout.build_list_row;
    final static int SUMMARY_LAYOUT = R.layout.build_list_row;

    /**
     * ExpandableLis UI 'data model' class
     */
    private class BuildArrayAdapter extends BaseExpandableListAdapter {
        private final LayoutInflater m_inflater;

        public BuildArrayAdapter(Context context) {
            m_inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        /**
         * Generated expanded detail view object.
         */
        @Override
        public View getChildView(
                final int groupPosition, final int childPosition, boolean isLastChild,
                View convertView, ViewGroup parent) {

            GroupInfo buildInfo = m_list.get(groupPosition);

            View expandView = convertView;
            // if (null == expandView) {
            expandView = m_inflater.inflate(SUMMARY_LAYOUT, parent, false);
            // }

            String key = (String) buildInfo.valueListStr().keySet().toArray()[childPosition];
            String val = buildInfo.valueListStr().get(key);

            TextView textView = Ui.viewById(expandView, R.id.buildField);
            textView.setText(key);
            textView.setPadding(40, 0, 0, 0);

            textView = Ui.viewById(expandView, R.id.buildValue);
            textView.setText(val);

            if ((groupPosition & 1) == 1)
                expandView.setBackgroundColor(0);
            else
                expandView.setBackgroundColor(0x80d0ffe0);

            return expandView;
        }

        @Override
        public int getGroupCount() {
            return m_list.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return m_list.get(groupPosition).getCount();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return null;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return null;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        /**
         * Generate summary (row) presentation view object.
         */
        @Override
        public View getGroupView(
                int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

            GroupInfo buildInfo = m_list.get(groupPosition);

            View summaryView = convertView;
            if (null == summaryView) {
                summaryView = m_inflater.inflate(SUMMARY_LAYOUT, parent, false);
            }

            TextView textView = Ui.viewById(summaryView, R.id.buildField);
            textView.setText(buildInfo.fieldStr());
            textView.setPadding(10, 0, 0, 0);
            textView.setTypeface(Typeface.MONOSPACE);

            textView = Ui.viewById(summaryView, R.id.buildValue);
            textView.setText(buildInfo.valueStr());

            if ((groupPosition & 1) == 1)
                summaryView.setBackgroundColor(0);
            else
                summaryView.setBackgroundColor(0x80d0ffe0);

            return summaryView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    // =============================================================================================
    // TODO move thsi into common code and share with PackageFragment (and others)

    FileBrowseDialog m_fileOpenDialog;
    void fireIntentOn(String value) {
        try {
            File root = new File(value);
            if (root.exists()) {
                /*
                Uri uri = Uri.fromFile(root);
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                // intent.setAction(android.content.Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                // intent.setDataAndType(uri, "file/*");
                // intent.setDataAndType(uri, "directory/*");
                // intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
                intent.setData(uri);
                // startActivityForResult(intent, 1);
                startActivity(intent);

                // intent.setAction(android.content.Intent.ACTION_GET_CONTENT);
                // intent.setDataAndType(uri, "*");
                // startActivity(Intent.createChooser(intent, "Open folder"));

                */
                m_fileOpenDialog = new FileBrowseDialog(this.getActivity(), "Browse",
                        this.getActivity().getWindow().getDecorView().getHeight(),null);

                m_fileOpenDialog.DefaultFileName = root.getPath();
                m_fileOpenDialog.choose(root.getPath());
            } else {
                ArrayList<String> responseList = runShellCmd(new String[]{"ls", "-l", value });
                Toast.makeText(getActivity(), TextUtils.join("\n", responseList), Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}