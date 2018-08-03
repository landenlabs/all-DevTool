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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.landenlabs.all_devtool.dialogs.DeleteDialog;
import com.landenlabs.all_devtool.util.FileUtil;
import com.landenlabs.all_devtool.util.LLog;
import com.landenlabs.all_devtool.util.OsUtils;
import com.landenlabs.all_devtool.util.SysUtils;
import com.landenlabs.all_devtool.util.Ui;
import com.landenlabs.all_devtool.util.Utils;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.landenlabs.all_devtool.util.FileUtil.getMimeType;
import static com.landenlabs.all_devtool.util.SysUtils.runShellCmd;

// import android.system.Os;


/**
 * Display "Package" installed information.
 *
 * @author Dennis Lang
 */
@SuppressWarnings({"Convert2Lambda", "OctalInteger"})
public class FileBrowserFragment extends DevFragment
        implements  View.OnClickListener
        , View.OnLayoutChangeListener
        , AdapterView.OnItemSelectedListener
        , FileUtil.ExecCallback {

    // Logger - set to LLog.DBG to only log in Debug build, use LLog.On for always log.
    private final LLog m_log = LLog.DBG;

    ArrayList<FileUtil.FileInfo> m_list = new ArrayList<>();
    ArrayList<FileUtil.FileInfo> m_workList = new ArrayList<>();
    ExpandableListView m_listView;

    Spinner m_loadSpinner;
    Spinner m_sortSpinner;
    ToggleButton m_expand_collapse_toggle;
    TextView m_title;

    LinearLayout m_dirBar;
    ArrayList<FileUtil.DirInfo> m_dirList = new ArrayList<>();
    FileUtil.FileInfo m_dirInfo;
    File  m_dir;
    long m_rootSizeMB;
    long m_rootFreeMB;
    long m_rootDevID;
    StringBuilder m_errMsg;

    Button m_fbDeletelBtn;
    int m_checkCnt = 0;

    View m_rootView;
    SubMenu m_menu;

    PackageManager m_packageManager;

    int m_sortBy = R.id.filebrowser_sort_by_dir;
    int m_show = R.id.filebrowser_root;
    boolean m_showExtra = true;

    static final int MSG_UPDATE_DONE = 1;
    static final int MSG_SORT_LIST = 2;
    private final Handler m_handler = new Handler() {

        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_UPDATE_DONE:
                    m_list.clear();
                    m_list.addAll(m_workList);
                    // ((BaseAdapter) m_listView.getAdapter()).notifyDataSetChanged();
                    ((BaseExpandableListAdapter) m_listView.getExpandableListAdapter()).notifyDataSetChanged();

                    if (m_list != null) {
                        m_title.setText(String.format("%d Files", m_list.size()));
                        if (m_expand_collapse_toggle.isChecked())
                            expandAll();
                    } else
                        m_title.setText("No files");

                    if (m_errMsg != null && m_errMsg.length() > 0) {
                        Ui.ShowMessage(FileBrowserFragment.this.getActivitySafe(), m_errMsg.toString());
                        m_errMsg = null;
                    }
                    // m_fbDeletelBtn.setEnabled(m_list.size() > 0);
                    // Fall into sort

                case MSG_SORT_LIST:
                    if (m_list != null) {
                        switch (m_sortBy) {
                            case R.id.filebrowser_sort_by_name:
                                Collections.sort(m_list, new Comparator<FileUtil.FileInfo>() {
                                    @Override
                                    public int compare(FileUtil.FileInfo file1, FileUtil.FileInfo file2) {
                                        return file1.getName().compareTo(file2.getName());
                                    }
                                });
                                break;
                            case R.id.filebrowser_sort_by_dir:
                                Collections.sort(m_list, new Comparator<FileUtil.FileInfo>() {
                                    @Override
                                    public int compare(FileUtil.FileInfo file1, FileUtil.FileInfo file2) {
                                        if  (file1.isDirectory() == file2.isDirectory())
                                            return file1.getName().compareTo(file2.getName());
                                        else if (file1.isDirectory())
                                            return -1;
                                        else 
                                            return 1;
                                    }
                                });
                                break;
                            case R.id.filebrowser_sort_by_size:
                                Collections.sort(m_list, new Comparator<FileUtil.FileInfo>() {
                                    @Override
                                    public int compare(FileUtil.FileInfo file1, FileUtil.FileInfo file2) {
                                        return (int) (file2.getLength() - file1.getLength());
                                    }
                                });
                                break;
                            case R.id.filebrowser_sort_by_date:
                                Collections.sort(m_list, new Comparator<FileUtil.FileInfo>() {
                                    @Override
                                    public int compare(FileUtil.FileInfo file1, FileUtil.FileInfo file2) {
                                        return (int) Math.signum((file2.lastModified() - file1.lastModified()) * 1.0);
                                    }
                                });
                                break;
                        }

                        //((BaseAdapter) m_listView.getAdapter()).notifyDataSetChanged();
                        ((BaseExpandableListAdapter) m_listView.getExpandableListAdapter()).notifyDataSetChanged();
                    }
                    break;
            }
        }
    };

    public static String s_name = "Files";
    private static final int MB = 1 << 20;
    private static int s_rowColor1 = 0;
    private static int s_rowColor2 = 0x80d0ffe0;
    private static SimpleDateFormat s_timeFormat = new SimpleDateFormat("MM/dd/yyyy  HH:mm");

    // --------------------------------------------------------------------------------------------

    public FileBrowserFragment() {

        try {
            m_dirInfo = new FileUtil.FileInfo("/");
            m_dir = new File("/");
            m_rootSizeMB = m_dirInfo.getUsableSpace() / MB;
            m_rootFreeMB = m_dirInfo.getFreeSpace() / MB;
            m_rootDevID = m_dirInfo.getDeviceId();
        } catch (Exception ex) {
            Toast.makeText(this.getContext(), ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static DevFragment create() {
        return new FileBrowserFragment();
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

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        m_packageManager = getActivitySafe().getPackageManager();
        m_rootView = inflater.inflate(R.layout.file_browser_tab, container, false);

        m_listView = Ui.viewById(m_rootView, R.id.fb_listview);
        final FileArrayAdapter adapter = new FileArrayAdapter(this.getActivitySafe());
        m_listView.setAdapter(adapter);

        m_dirBar = Ui.viewById(m_rootView, R.id.fb_dirBar);


        m_listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                /*
                final int grpPos = (Integer) view.getTag();

                final TextView field = Ui.viewById(view, R.id.buildField);
                final TextView value = Ui.viewById(view, R.id.buildValue);
                if (field != null && value != null) {
                    Button btn = Ui.ShowMessage(FileBrowserFragment.this.getActivitySafe(), field.getText() + "\n" + value.getText()).getButton(AlertDialog.BUTTON_POSITIVE);
                    if (btn != null) {
                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // fireIntentOn(field.getText().toString(), value.getText().toString(), grpPos);
                            }
                        });
                    }
                }
                */
                return false;
            }
        });

        /*
        m_listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {

                if (groupPosition >= 0 && groupPosition < m_list.size()) {
                    FileInfo fileInfo = m_list.get(groupPosition);
                }
                return false;
            }
        });

        adapter.setOnItemLongClickListener1(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View view, int pos, long id) {
                int grpPos = ((Integer) view.getTag()).intValue();
                FileInfo fileInfo = m_list.get(pos);
                return true;
            }
        });
        */

        adapter.setOnItemLongClickListener1(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View view, int pos, long id) {
                // Toast.makeText(getActivitySafe(), String.format("Long Press on %d id:%d ", pos, id), Toast.LENGTH_LONG).show();
                // int grpPos = (Integer) view.getTag();
                if (pos >= 0 && pos < m_list.size()) {
                    final FileUtil.FileInfo fileInfo = m_list.get(pos);

                    StringBuilder  fileSb = new StringBuilder("Name: ").append(fileInfo.getName());
                    fileSb.append("\nMod Date: ").append(s_timeFormat.format(fileInfo.lastModified()));
                    fileSb.append("\nAcc Date: ").append(s_timeFormat.format(fileInfo.getAtime()));
                    if (!fileInfo.isDirectory())
                        fileSb.append("\nLength: ").append(String.format("%,d", fileInfo.length()));

                    long depthSize = fileInfo.findDepthSize(-1, 10000);
                    if (depthSize > 0) {
                        fileSb.append(String.format("\nDepthSize: %s", FileUtil.getSizeStr(depthSize)));
                    }

                    fileSb.append("\n");
                    appendPerm(fileSb, fileInfo);
                    long freeMb = fileInfo.getDevFreeMB();
                    long sizeMb = fileInfo.getDevSizeMB();
                    long devID = fileInfo.getDeviceId();

                    fileSb.append(String.format(
                            "\n\nStorage Free: %d MB\nStorage Size: %d MB\nStorage ID: %d\n",
                            freeMb, sizeMb, devID));

                    OsUtils.Stat stat = OsUtils.getStat(fileInfo);
                    if (stat != null) {
                        fileSb.append(String.format(
                                "\nPermMode: %03o\nDevice Id: %d\nInode: %d\n#Links: %d\nUserID: %d\nGroupID: %d",
                                (stat.st_mode & 0777),
                                stat.st_dev,
                                stat.st_ino,
                                stat.st_nlink,
                                stat.st_uid,
                                stat.st_gid));
                    }

                    try {
                        fileSb.append("\n\nPath:").append(fileInfo.getCanonicalPath().replaceAll("/", "\n  /"));
                    } catch (IOException ignore) {
                    }

                    AlertDialog fileInfoDlg =
                            fileInfo.isDirectory()
                                    ? Ui.ShowFileDlg(FileBrowserFragment.this.getActivitySafe(),fileSb.toString())
                                    : Ui.ShowMessage(FileBrowserFragment.this.getActivitySafe(),fileSb.toString());

                    Button btn = fileInfoDlg.getButton(AlertDialog.BUTTON_POSITIVE);
                    if (btn != null) {
                        // btn.setVisibility(View.GONE);

                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                            //    viewIntent.setData(Uri.fromFile(fileInfo));
                                viewIntent.setDataAndType(Uri.fromFile(fileInfo),
                                        getMimeType(fileInfo.getAbsolutePath()));
                                Intent openIntent = Intent.createChooser(viewIntent,
                                        "Choose an application to open with:");
                                startActivity(openIntent);

                                // fireIntentOn(field.getText().toString(), value.getText().toString(), grpPos);
                            }
                        });

                    }

                    if (fileInfo.isDirectory()) {
                        Button btn3 = fileInfoDlg.getButton(AlertDialog.BUTTON_NEUTRAL);
                        if (btn3 != null) {
                            // btn.setVisibility(View.GONE);

                            btn3.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // Run DU command on directory.
                                    List<String> duList = runShellCmd(new String[] { "du", "-chHL", fileInfo.getAbsolutePath() });
                                    // addString("du -chHL /sdcard", m_duSdcardList);
                                    if (duList.size() > 0) {
                                        StringBuilder sb = new StringBuilder("--Disk Usage--\n");
                                        for (String item : duList) {
                                            String[] parts = item.split("\t");
                                            if (parts.length == 2) {
                                                sb.append(String.format("%8s   %s\n", parts[0],
                                                        parts[1]));
                                            }
                                        }
                                        Ui.ShowMessage(FileBrowserFragment.this.getActivitySafe(),
                                                sb.toString());
                                    }
                                }
                            });

                        }
                    }
                    return true;
                }
                return false;
            }
        });

        m_title = Ui.viewById(m_rootView, R.id.fb_title);
        m_fbDeletelBtn = Ui.viewById(m_rootView, R.id.fb_delete);
        m_fbDeletelBtn.setOnClickListener(this);
        m_fbDeletelBtn.setEnabled(false);

        m_loadSpinner = Ui.viewById(m_rootView, R.id.fb_load_spinner);
        m_loadSpinner.addOnLayoutChangeListener(this);

        final String[] nameList = getResources().getStringArray(R.array.fb_load_array);

        new Thread(new Runnable() {
            @Override
            public void run() {
                getDirDetails(nameList);
            }
        }).start();

        ArrayAdapter<String> spinnerAdapter = new DirPathSpinnerAdapter(getContext(),
                android.R.layout.simple_spinner_item,
                nameList, mDirItems);

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_loadSpinner.setAdapter(spinnerAdapter);


        m_sortSpinner = Ui.viewById(m_rootView, R.id.fb_sort_spinner);
        if (m_menu != null) {
            MenuItem sortBy = m_menu.findItem(m_sortBy);
            if (sortBy != null) {
                int pos = Arrays.asList(getResources().getStringArray(R.array.fb_sort_array))
                        .indexOf(sortBy.getTitle());
                if (pos != -1)
                    m_sortSpinner.setSelection(pos);
            }
        }
        m_sortSpinner.addOnLayoutChangeListener(this);
        m_expand_collapse_toggle = Ui.viewById(m_rootView, R.id.fb_plus_minus_toggle);
        m_expand_collapse_toggle.setOnClickListener(this);

        setShowDir();
        // TODO - request permission
        updateList();
        return m_rootView;
    }


    List<SpannableString> mDirItems = new ArrayList<>();

    void getDirDetails(String[] dirList) {
        for (String dirName : dirList) {
            File dir;
            if (dirName.compareToIgnoreCase("root") == 0) {
                dir = File.listRoots()[0];
                // dir = new File("/");
            } else if (dirName.compareToIgnoreCase("data") == 0) {
                dir = Environment.getDataDirectory();
                if (OsUtils.getPermissions(m_dir) == -1) {
                    m_dir = Environment.getExternalStoragePublicDirectory("data");
                }
            } else if (dirName.compareToIgnoreCase("Sdcard") == 0) {
                dir = Environment.getExternalStorageDirectory();
            } else if (dirName.compareToIgnoreCase("documents") == 0) {
                if (Build.VERSION.SDK_INT >= 19)
                    dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                else
                    dir = Environment.getExternalStorageDirectory();
            } else {
                dir = Environment.getExternalStoragePublicDirectory(dirName);
            }

            String path = (dir == null) ? "" : dir.getAbsolutePath();
            int perm = -1;
            try {
                perm = OsUtils.getPermissions(dir);
            } catch (Exception ignore) {
            }

            String permStr = SysUtils.getPermissionString(perm);
            dirName = dirName + "  ";

            int nameColor = 0xffffffff;
            int resColor  = 0xff40ff80;
            int pathColor = 0xffd0d0d0;

            if (dir.listFiles() == null) {
                // No files - colorize yellow
                nameColor = resColor = pathColor = 0xffffffb0;
            }
            if (perm == -1) {
                // No permission - colorize pink
                nameColor = resColor = pathColor = 0xffffb0b0;
            }

            StringBuilder sb = new StringBuilder();
            sb.append(dirName);
            sb.append(permStr).append("\n");
            sb.append(path);

            SpannableString span = new SpannableString(sb.toString());
            int nameLen = dirName.length();
            int permLen = permStr.length();
            span.setSpan(new ForegroundColorSpan(nameColor), 0, nameLen, 0);
            span.setSpan(new ForegroundColorSpan(resColor), nameLen+1, nameLen+permLen, 0);
            span.setSpan(new ForegroundColorSpan(pathColor), nameLen+permLen+1, span.length(), 0);
            mDirItems.add(span);
        }
    }

    // =============================================================================================
    @SuppressWarnings("UnnecessaryLocalVariable")
    class DirPathSpinnerAdapter extends ArrayAdapter<String> {
        List<SpannableString> mSpanList;
        String[] mNameList;

        DirPathSpinnerAdapter(Context context, int txViewResId,
                String[] nameList, List<SpannableString> spanList) {
            super(context, txViewResId);
            mNameList = nameList;
            mSpanList = spanList;
        }

        @Override
        public boolean isEmpty() {
            return (mNameList == null || mNameList.length == 0);
        }

        @Override
        public int getCount() {
            return mNameList.length;
        }

        /*
            <item>Sdcard</item>
            <item>Download</item>
            <item>Data</item>
            <item>Dcim</item>
            <item>Documents</item>
        */
        @Override
        public @Nullable
        String getItem(int position) {
            return mNameList[position];
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view =  super.getView(position, convertView, parent);
            return view;
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            TextView view =  (TextView)super.getDropDownView(position, convertView, parent);
            view.setMaxLines(3);
            view.setSingleLine(false);
            view.setText(mSpanList.get(position));
            return view;
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSelected() {
        GlobalInfo.s_globalInfo.mainFragActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int pos;
        int id = item.getItemId();
        int show = m_show;
        switch (id) {
            case R.id.filebrowser_root:
            case R.id.filebrowser_sdcard:
            case R.id.filebrowser_download:
            case R.id.filebrowser_data:
            case R.id.filebrowser_dcim:
            case R.id.filebrowser_documents:
            case R.id.filebrowser_movies:
            case R.id.filebrowser_music:
            case R.id.filebrowser_picture:
            case R.id.filebrowser_podcast:
            case R.id.filebrowser_ringtones:
                show = id;
                break;
         
            case R.id.filebrowser_delete:
                deleteFiles();
                break;
            case R.id.filebrowser_collapseAll:
                collapseAll();
                m_expand_collapse_toggle.setChecked(false);
                break;
            case R.id.filebrowser_expandAll:
                expandAll();
                m_expand_collapse_toggle.setChecked(true);
                break;

            case R.id.filebrowser_show_extra:
                item.setChecked(!item.isChecked());
                m_showExtra = item.isChecked();
                m_handler.sendMessage(m_handler.obtainMessage(MSG_SORT_LIST));
                break;

            case 0:
                break;
            default:
                item.setChecked(true);
                pos = Arrays.asList(getResources().getStringArray(R.array.fb_sort_array)).indexOf(item.getTitle());
                m_sortSpinner.setSelection(pos);
                this.m_sortBy = id;
                Message msgObj = m_handler.obtainMessage(MSG_SORT_LIST);
                m_handler.sendMessage(msgObj);
                break;
        }

        if (m_show != show) {
            m_show = show;
            setShowDir();
            item.setChecked(true);
        //    m_loadSpinner.setSelection(m_show);
            updateList();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        m_menu = menu.addSubMenu("File Options");
        inflater.inflate(R.menu.filebrowser_menu, m_menu);

        m_menu.findItem(R.id.filebrowser_show_extra).setVisible(Build.VERSION.SDK_INT >= 21);

        MenuItem sortByItem = m_menu.findItem(m_sortBy);
        if (sortByItem != null)
            sortByItem.setChecked(true);
    }

    // ============================================================================================
    // onClickListener methods

    @Override
    public void onClick(View v) {

        int id = v.getId();
        switch (id) {
            case R.id.fb_delete:
                deleteFiles();
                break;
            case R.id.fb_plus_minus_toggle:
                if (m_expand_collapse_toggle.isChecked())
                    expandAll();
                else
                    collapseAll();
                break;
        }
    }

    // ============================================================================================
    // implement OnLayoutChangeListener
    @Override
    public void onLayoutChange(View v, int left, int top, int right,
            int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if (v == m_loadSpinner)
            m_loadSpinner.setOnItemSelectedListener(this);

        else if (v == m_sortSpinner)
            m_sortSpinner.setOnItemSelectedListener(this);
    }

    // ============================================================================================
    // implement onItemSelectedListener (spinner)
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (m_menu == null)
            return;
        // String itemStr = parent.getItemAtPosition(pos).toString();
        if (parent == m_loadSpinner) {
            m_show = pos;
            int menu_id = R.id.filebrowser_root;
            switch (pos) {
                case 0:
                    menu_id = R.id.filebrowser_root;
                    break;
                case 1:
                    menu_id = R.id.filebrowser_sdcard;
                    break;
                case 2:
                    menu_id = R.id.filebrowser_download;
                    break;
                case 3:
                    menu_id = R.id.filebrowser_data;
                    break;
                case 4:
                    menu_id = R.id.filebrowser_dcim;
                    break;
                case 5:
                    menu_id = R.id.filebrowser_documents;
                    break;
                case 6:
                    menu_id = R.id.filebrowser_movies;
                    break;
                case 7:
                    menu_id = R.id.filebrowser_music;
                    break;
                case 8:
                    menu_id = R.id.filebrowser_picture;
                    break;
                case 9:
                    menu_id = R.id.filebrowser_podcast;
                    break;
                case 10:
                    menu_id = R.id.filebrowser_ringtones;
                    break;
            }

            m_show = menu_id;
            setShowDir();
            m_menu.findItem(menu_id).setChecked(true);
            updateList();
        } else if (parent == m_sortSpinner) {
            int menuId = -1;
            switch (pos) {
                case 0: // app
                    menuId = R.id.filebrowser_sort_by_name;
                    break;
                case 1: // dir
                    menuId = R.id.filebrowser_sort_by_dir;
                    break;
                case 2: // size
                    menuId = R.id.filebrowser_sort_by_size;
                    break;
                case 3: // date;
                    menuId = R.id.filebrowser_sort_by_date;
                    break;
            }

            if (id != -1) {
                m_menu.findItem(menuId).setChecked(true);
                m_sortBy = menuId;
                Message msgObj = m_handler.obtainMessage(MSG_SORT_LIST);
                m_handler.sendMessage(msgObj);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

    // ============================================================================================
    // Permission
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (grantResults.length > 0)
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        updateList();
                    }
                }
            }
        }
    }

    // ============================================================================================
    // Internal methods

    private void collapseAll() {
        int count = m_list.size();
        for (int position = 0; position < count; position++)
            m_listView.collapseGroup(position);
    }

    private void expandAll() {
        int count = m_list.size();
        for (int position = 0; position < count; position++)
            m_listView.expandGroup(position);
    }

    private void setShowDir() {
        switch (m_show) {
            case R.id.filebrowser_root:
                m_dir = File.listRoots()[0];
                break;
            case R.id.filebrowser_sdcard:
                m_dir = Environment.getExternalStorageDirectory();
                break;
            case R.id.filebrowser_download:
                m_dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                break;
            case R.id.filebrowser_data:
                m_dir = Environment.getDataDirectory();
                if (OsUtils.getPermissions(m_dir) == -1) {
                    m_dir = Environment.getExternalStoragePublicDirectory("data");
                }
                break;
            case R.id.filebrowser_dcim:
                m_dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                break;
            case R.id.filebrowser_documents:
                if (Build.VERSION.SDK_INT >= 19)
                    m_dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                else
                    m_dir = Environment.getExternalStorageDirectory();
                break;
            case R.id.filebrowser_movies:
                m_dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
                break;
            case R.id.filebrowser_music:
                m_dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
                break;
            case R.id.filebrowser_picture:
                m_dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                break;
            case R.id.filebrowser_podcast:
                m_dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS);
                break;
            case R.id.filebrowser_ringtones:
                m_dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES);
                break;
        }

        m_log.d(m_dir.getAbsolutePath());
    }

    private void deleteFiles() {
        ArrayList<String> deleteList = new ArrayList<>();
        for (FileUtil.FileInfo fileInfo : m_list) {
            
            if (fileInfo.isChecked) {
                deleteList.add(fileInfo.getAbsolutePath());
                // m_checkCnt--;
                updateDeleteBtn();
            }
        }

        DeleteDialog.showDialog(this, deleteList, 0).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                m_checkCnt = 0;
                updateList();
            }
        });

        // m_checkCnt = 0;
        updateDeleteBtn();
    }

    private void addDir(File dir) {
        if (dir.getParentFile() != null)
            addDir(dir.getParentFile());

        FileUtil.DirInfo button = new FileUtil.DirInfo(m_dirBar.getContext(), dir);
        m_dirBar.addView(button);
        button.setTag(m_dirList.size());
        button.setTextColor(Color.WHITE);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View dirBtn) {
                int idx = (Integer) dirBtn.getTag();
                m_dir = m_dirList.get(idx).getDir();
                updateList();
            }
        });
        m_dirList.add(button);
    }
    /**
     * Update Package list, show progress indicator while loading in background.
     */
    public void updateList() {
        // Swap colors
        int color = s_rowColor1;
        s_rowColor1 = s_rowColor2;
        s_rowColor2 = color;
        
        m_fbDeletelBtn.setEnabled(false);

        if (!m_dir.isDirectory() || !m_dir.exists())
            m_dir = File.listRoots()[0];

        m_dirBar.removeAllViews();
        m_dirList.clear();
        addDir(m_dir);

        // Start lengthy operation loading files in background thread
        new Thread(new Runnable() {
            public void run() {
                loadFiles(m_dir.getAbsolutePath());
            }
        }).start();
    }


    /**
     * Load Files
     */
    void loadFiles(String dir) {

        checkPermissions(Manifest.permission.READ_EXTERNAL_STORAGE);

        m_workList.clear();
        try {
            File dirFile = new File(dir);

            // m_list.add("..");'
            File[] files = dirFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    FileUtil.FileInfo fileInfo = new FileUtil.FileInfo(file.getAbsolutePath());
                    if (fileInfo.isDirectory()) {
                        File[] subList = fileInfo.listFiles();
                        fileInfo.setFileCnt((subList == null ? 0 : subList.length));
                        fileInfo.findDepthSize(0, 100);
                    }
                    m_workList.add(fileInfo);
                }
            } else {
                /*
                if (Build.VERSION.SDK_INT >= 21) {
                    StructStat stat = Os.stat(dirFile.getAbsolutePath());
                    // Permission denied.
                    // ls -1 dir
                    // ls -l dir
                    // find dir -maxdepth
                    AsyncTaskCompat.executeParallel(
                            FileUtil.getAsyncExec(this, new StringBuilder(), new String[] {"find", dir, "-maxdepth", "1"} ));
                }
                */
            }
        } catch (NullPointerException ex) {
            // ignore null exception
            if (m_errMsg == null)
                m_errMsg = new StringBuilder();
            m_errMsg.append(ex.getMessage()).append("\n");

            /*
            m_errMsg = null;
            AsyncTaskCompat.executeParallel(
                FileUtil.getAsyncExec(this, new StringBuilder(), new String[] {"sh", "-c", "id"} ));
               */

        } catch (Exception ex) {
            if (m_errMsg == null)
                m_errMsg = new StringBuilder();
            m_errMsg.append(ex.getMessage()).append("\n");
        }

        Message msgObj = m_handler.obtainMessage(MSG_UPDATE_DONE);
        m_handler.sendMessage(msgObj);
    }

    @Override
    public void Exec(StringBuilder result, int flag) {
        Ui.ShowMessage(FileBrowserFragment.this.getActivitySafe(), result.toString());
    }
    
    void updateDeleteBtn() {
        m_fbDeletelBtn.setEnabled(m_checkCnt != 0);
        if (m_checkCnt != 0)
            m_fbDeletelBtn.setText(String.format("%s %d",getString(R.string.filebrowser_delete),m_checkCnt  ));
        else
            m_fbDeletelBtn.setText( getString(R.string.filebrowser_delete));
    }

    static boolean isBit(int val, int mask) {
        return (val & mask) != 0;
    }

    @SuppressWarnings("OctalInteger")
    void appendPerm(StringBuilder perm, FileUtil.FileInfo fileItem) {
        perm.append("Perm: ");

        try {
            OsUtils.Stat stat = OsUtils.getStat(fileItem);
            if (stat != null) {
                int mode = stat.st_mode & 0777;
                int world = mode & 0007;
                perm.append(isBit(world, 0004) ? "R" : "-");
                perm.append(isBit(world, 0002) ? "W" : "-");
                perm.append(isBit(world, 0001) ? "X" : "-");

                perm.append(fileItem.isFile() ? "F" : "");
                perm.append(fileItem.isDirectory() ? "D" : "");
                return;
            }
        } catch (Exception ignore) {
        }

        perm.append(fileItem.canRead() ? "R" : "-");
        perm.append(fileItem.canWrite() ? "W" : "-");
        perm.append(fileItem.canExecute() ? "X" : "-");
        perm.append(fileItem.isHidden() ? "H" : "");
        perm.append(fileItem.isFile() ? "F" : "");
        perm.append(fileItem.isDirectory() ? "D" : "");
    }

    // ============================================================================================
    
    final static int EXPANDED_LAYOUT = R.layout.build_list_row;
    final static int SUMMARY_LAYOUT = R.layout.file_browser_list_row;

    Map<String, Drawable> m_icons = new HashMap<>();

    void setIcon(Button imageView, FileUtil.FileInfo fileInfo) {
        String name = fileInfo.getName().replace(".apk", "");
        Drawable icon = m_icons.get(name);

        // StructStatVfs sb = Libcore.os.statvfs(path);
        // long sizeMB = fileInfo.getDevSizeMB();
        // long freeMB = fileInfo.getDevFreeMB();
        long devID = fileInfo.getDeviceId();

        // boolean isRoot = (sizeMB == m_rootSizeMB && freeMB == m_rootFreeMB) || sizeMB == 0;
        boolean isRoot = (devID == m_rootDevID);
        int folderRes =  isRoot ? R.drawable.folder : R.drawable.folder_other;

        if (name.matches("[a-z0-9]+\\.[a-z0-9]+\\..*")) {
            try {
                PackageInfo packInfo = m_packageManager.getPackageInfo(name, 0);
                if (packInfo != null) {
                    icon = packInfo.applicationInfo.loadIcon(m_packageManager);
                    m_icons.put(name, icon);
                }

                if (icon != null) {
                    if (fileInfo.isDirectory()) {
                        Drawable background = getResources().getDrawable(folderRes);
                        Drawable[] layers = {background, icon};
                        LayerDrawable layerDrawable = new LayerDrawable(layers);
                        layerDrawable.setLayerInset(0, 0, 0, 0, 0);
                        int offset = 8;
                        layerDrawable.setLayerInset(1, offset, offset, offset, offset);
                        imageView.setBackground(layerDrawable);
                    } else {
                        imageView.setBackground(icon);
                    }
                    return;
                }
            } catch (Exception ignore) {
            }
        }

        if (fileInfo.isDirectory()) {
            imageView.setBackgroundResource(folderRes);
        } else {
            // TODO - map extension to icons
            //    .mp4, .avi => movie
            //    .png, .jpg => image

            imageView.setBackgroundResource(R.drawable.file);
        }

    }

    public  void playNotificationSound() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(this.getContext(), notification);
            r.play();
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    // ============================================================================================
    /**
     * ExpandableLis UI 'data model' class
     */
    private class FileArrayAdapter extends BaseExpandableListAdapter implements  View.OnClickListener, View.OnLongClickListener {
        private final LayoutInflater m_inflater;

        FileArrayAdapter(Context context) {
            m_inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        AdapterView.OnItemLongClickListener m_onItemLongClickListener;
        void setOnItemLongClickListener1( AdapterView.OnItemLongClickListener longClickList) {
            m_onItemLongClickListener = longClickList;
        }

        /**
         * Generated expanded detail view object.
         */
        @Override
        public View getChildView(final int groupPosition,
                     final int childPosition, boolean isLastChild, View convertView,
                     ViewGroup parent) {

            if (groupPosition < 0 || groupPosition >= m_list.size())
                return null;

            // FileUtil.FileInfo fileItem = m_list.get(groupPosition);

            View expandView;    //  = convertView; Reuse had left overs
            // if (null == expandView) {
                expandView = m_inflater.inflate(EXPANDED_LAYOUT, parent, false);
            // }

            /*
            if (childPosition < fileItem.valueListStr().size()) {
                expandView.setTag(Integer.valueOf(groupPosition));

                Pair<String, String> keyVal = fileItem.valueListStr().get(childPosition);
                String key = keyVal.first;
                String val = keyVal.second;

                TextView textView = Ui.viewById(expandView, R.id.buildField);
                textView.setText(key);
                textView.setPadding(40, 0, 0, 0);

                textView = Ui.viewById(expandView, R.id.buildValue);
                textView.setText(val);

                if ((groupPosition & 1) == 1)
                    expandView.setBackgroundColor(s_rowColor1);
                else
                    expandView.setBackgroundColor(s_rowColor2);
            }
            */
            
            return expandView;
        }

        @Override
        public int getGroupCount() {
            return m_list.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return ((m_list == null || groupPosition >= m_list.size()) ? 0 : m_list.get(groupPosition).getCount());
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
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {

            if (m_list == null || groupPosition >= m_list.size() || groupPosition < 0)
                return convertView; // Should never get here.

            FileUtil.FileInfo fileItem = m_list.get(groupPosition);
            // long sizeMB = fileItem.getSizeMB();
            long freeMB = fileItem.getDevFreeMB();

            View summaryView = convertView;
            if (null == summaryView) {
                summaryView = m_inflater.inflate(SUMMARY_LAYOUT, parent, false);
            }

            summaryView.setTag(groupPosition);
            summaryView.setOnClickListener(this);
            summaryView.setOnLongClickListener(this);

            Button imageView = Ui.viewById(summaryView, R.id.fb_icon);

            setIcon(imageView, fileItem);
            imageView.setText((fileItem.getFileCnt() != 0) ? String.valueOf(Math.min(999, fileItem.getFileCnt())) : "");

            Ui.<TextView>viewById(summaryView, R.id.fb_name).setText(fileItem.getLongName());


            StringBuilder perm = new StringBuilder();
            appendPerm(perm, fileItem);
            if (fileItem.isDirectory() && fileItem.getDepthSize() > 0) {
                String depthSizeStr = FileUtil.getSizeStr(fileItem.getDepthSize());
                perm.append(String.format(" Depth:%s", depthSizeStr));
            }

            if (m_showExtra) {
                // perm.append(String.format("Disk Id:%d", fileItem.get))
                if (freeMB >= 1024)
                    perm.append(String.format("\n Vol Free:%.1f GB", freeMB / 1024.0));
                else
                    perm.append(String.format("\n Vol Free:%d MB", freeMB));

                // StatFs stat = new StatFs(fileItem.getAbsolutePath());

                OsUtils.Stat stat = OsUtils.getStat(fileItem);
                if (stat != null) {
                    //noinspection OctalInteger
                    perm.append(String.format("\n Mode:%03o  Vol Id: %d ",
                            (stat.st_mode & 0777), stat.st_dev));
                }
            }

            Ui.<TextView>viewById(summaryView, R.id.fb_aux).setText(perm.toString());


            switch (m_sortBy) {
                case R.id.filebrowser_sort_by_date:
                    Ui.<TextView>viewById(summaryView, R.id.fb_size).setText(s_timeFormat.format(fileItem.lastModified()));
                    break;
                default:
                    Ui.<TextView>viewById(summaryView, R.id.fb_size).setText(
                            fileItem.isDirectory() ? "" :
                            NumberFormat.getNumberInstance(Locale.getDefault()).format(fileItem.length()));
                    break;
            }

            CheckBox checkBox = Ui.viewById(summaryView, R.id.fb_checked);
            checkBox.setVisibility(fileItem.canWrite()  ? View.VISIBLE : View.INVISIBLE);
            checkBox.setChecked(fileItem.isChecked);
            checkBox.setTag(groupPosition);
            checkBox.setOnClickListener(this);

            if ((groupPosition & 1) == 1)
                summaryView.setBackgroundColor(s_rowColor1);
            else
                summaryView.setBackgroundColor(s_rowColor2);

            return summaryView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        // ============================================================================================
        // View.OnClickListener
        @Override
        public void onClick(View view) {
            int grpPos = (Integer) view.getTag();
            if (view instanceof  CheckBox) {
                boolean checked = ((CheckBox) view).isChecked();
                FileBrowserFragment.this.m_list.get(grpPos).isChecked = checked;
                m_checkCnt += (checked ? 1 : -1);
                updateDeleteBtn();
            } else if (m_list.get(grpPos).isDirectory()) {
                if (m_list.get(grpPos).getFileCnt() != 0) {
                    m_dir = m_list.get(grpPos);
                    updateList();
                } else {
                    playNotificationSound();
                }
            } else {
                if (m_listView.isGroupExpanded(grpPos))
                    m_listView.collapseGroup(grpPos);
                else
                    m_listView.expandGroup(grpPos);
            }
        }

        // ============================================================================================
        // View.OLongClickListener
        @Override
        public boolean onLongClick(View view) {
            int grpPos = (Integer) view.getTag();
            // PackageFragment.this.m_list.get(grpPos).m_checked = ((CheckBox)v).isChecked();
            return m_onItemLongClickListener == null || m_onItemLongClickListener
                    .onItemLongClick(null, view, grpPos, -1);

        }
    }
}