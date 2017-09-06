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

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.landenlabs.all_devtool.dialogs.FileBrowseDialog;
import com.landenlabs.all_devtool.dialogs.UninstallDialog;
import com.landenlabs.all_devtool.receivers.UninstallIntentReceiver;
import com.landenlabs.all_devtool.util.LLog;
import com.landenlabs.all_devtool.util.Ui;
import com.landenlabs.all_devtool.util.Utils;

import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static android.content.pm.ApplicationInfo.FLAG_ALLOW_BACKUP;
import static com.landenlabs.all_devtool.R.id.appName;


/**
 * Display "Package" installed information.
 *
 * @author Dennis Lang
 */
public class PackageFragment extends DevFragment
        implements  View.OnClickListener
        , View.OnLongClickListener
        , View.OnLayoutChangeListener
        , AdapterView.OnItemSelectedListener {
    
    // Logger - set to LLog.DBG to only log in Debug build, use LLog.On for always log.
    private final LLog m_log = LLog.DBG;

    ArrayList<PackingItem> m_list = new ArrayList<PackingItem>();
    ArrayList<PackingItem> m_workList;
    ArrayList<PackingItem> m_beforeFilter = new ArrayList<PackingItem>();
    ExpandableListView m_listView;

    ToggleButton m_pkgLoadBtn;
    ProgressDialog m_progress;
    Date m_date = new Date();

    static final String m_regPermissionsStr = "RegPermissions";
    static final String m_permissionsStr = "Permissions";
    static final String m_activitiesStr = "Activities";
    static final String m_servicesStr = "Services";
    static final String m_providers = "Providers";

    Spinner m_loadSpinner;
    Spinner m_sortSpinner;
    ToggleButton m_expand_collapse_toggle;
    EditText m_title;
    Button m_pkgUninstallBtn;
    int m_checkCnt = 0;

    View m_rootView;
    SubMenu m_menu;

    int m_sortBy = R.id.package_sort_by_size;
    final int SHOW_USER = 0;
    final int SHOW_SYS = 1;
    final int SHOW_PREF = 2;
    final int SHOW_CACHE = 3;
    final int SHOW_LIB = 4;

    int m_show = SHOW_USER;

    static final int MSG_UPDATE_DONE = 1;
    static final int MSG_SORT_LIST = 2;
    private final Handler m_handler = new Handler() {

        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_UPDATE_DONE:
                    m_list.clear();
                    ((BaseExpandableListAdapter) m_listView.getExpandableListAdapter()).notifyDataSetChanged();

                    if (m_workList != null) {
                        m_list.addAll(m_workList);
                    }
                    // m_listView.removeAllViewsInLayout();
                    // ((BaseAdapter) m_listView.getAdapter()).notifyDataSetChanged();
                    ((BaseExpandableListAdapter) m_listView.getExpandableListAdapter()).notifyDataSetChanged();

                    // m_listView.invalidateViews();
                    // m_listView.refreshDrawableState();

                    updatePkgTitle();

                    m_progress.hide();
                    m_pkgLoadBtn.setVisibility(View.GONE);
                    // m_pkgUninstallBtn.setEnabled(m_list.size() > 0);
                    // Fall into sort

                case MSG_SORT_LIST:
                    if (m_list != null) {
                        switch (m_sortBy) {
                            case R.id.package_sort_by_appname:
                                Collections.sort(m_list, new Comparator<PackingItem>() {
                                    @Override
                                    public int compare(PackingItem pkgItem1, PackingItem pkgItem2) {
                                        return pkgItem1.m_appName.compareTo(pkgItem2.m_appName);
                                    }
                                });
                                break;
                            case R.id.package_sort_by_pkgname:
                                Collections.sort(m_list, new Comparator<PackingItem>() {
                                    @Override
                                    public int compare(PackingItem pkgItem1, PackingItem pkgItem2) {
                                        return pkgItem1.m_packInfo.packageName.compareTo(pkgItem2.m_packInfo.packageName);
                                    }
                                });
                                break;
                            case R.id.package_sort_by_size:
                                Collections.sort(m_list, new Comparator<PackingItem>() {
                                    @Override
                                    public int compare(PackingItem pkgItem1, PackingItem pkgItem2) {
                                        return (int) (pkgItem2.m_pkgSize - pkgItem1.m_pkgSize);
                                    }
                                });
                                break;
                            case R.id.package_sort_by_update_date:
                                Collections.sort(m_list, new Comparator<PackingItem>() {
                                    @Override
                                    public int compare(PackingItem pkgItem1, PackingItem pkgItem2) {
                                        return (int) Math.signum((pkgItem2.m_packInfo.lastUpdateTime - pkgItem1.m_packInfo.lastUpdateTime) * 1.0);
                                    }
                                });
                                break;
                            case R.id.package_sort_by_install_date:
                                Collections.sort(m_list, new Comparator<PackingItem>() {
                                    @Override
                                    public int compare(PackingItem pkgItem1, PackingItem pkgItem2) {
                                        return (int) Math.signum((pkgItem2.m_packInfo.lastUpdateTime - pkgItem1.m_packInfo.firstInstallTime) * 1.0);
                                    }
                                });
                                break;
                        }

                        // ((BaseAdapter) m_listView.getAdapter()).notifyDataSetChanged();
                        ((BaseExpandableListAdapter) m_listView.getExpandableListAdapter()).notifyDataSetChanged();
                    }
                    break;
            }
        }
    };

    // handler for received Intents for the "my-event" event
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            m_log.i(" mMessageReceiver onReceive");

            // Extract data included in the Intent
            String packageName = intent.getStringExtra("package");
            if (!TextUtils.isEmpty(packageName)) {
                removePackage(packageName);
                sendNotification(context, intent.getAction(), packageName);
            }
        }
    };

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(Context context, String from, String message) {
        /*
        Intent intent = new Intent(context, DevToolActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        */

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.shortcut_pkg)
                .setContentTitle("Uninstalled")
                .setContentText(message)
                .setAutoCancel(true)
                ;
         //       .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
         //       .setLights(Color.RED, 3000, 3000)
         //       .setSound(defaultSoundUri)
         //       .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
         //       .setContentIntent(pendingIntent);

        // <uses-permission android:name="android.permission.VIBRATE" />

        Notification note = notificationBuilder.build();
        note.defaults |= Notification.DEFAULT_VIBRATE;
        note.defaults |= Notification.DEFAULT_SOUND;
        note.defaults |= Notification.DEFAULT_LIGHTS;

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, note);
    }

    public static String s_name = "Packages";
    private static final int MB = 1 << 20;
    private static int s_rowColor1 = 0;
    private static int s_rowColor2 = 0x80d0ffe0;
    private static SimpleDateFormat s_timeFormat = new SimpleDateFormat("MM/dd/yyyy  HH:mm");
    private Set<String> m_storageDirs = new HashSet<String>();

    // --------------------------------------------------------------------------------------------

    public PackageFragment() {

        String primary_sd = System.getenv("EXTERNAL_STORAGE");
        if (primary_sd != null)
            m_storageDirs.add(primary_sd);
        String secondary_sd = System.getenv("SECONDARY_STORAGE");
        if (secondary_sd != null)
            m_storageDirs.add(secondary_sd);

        File storageDir = new File("/storage");

        try {
            File[] files = storageDir.listFiles();
            if (files != null) {
                for (File sdcard : files) {
                    if (sdcard.isDirectory()) {
                        m_storageDirs.add(sdcard.getAbsolutePath());
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    public static DevFragment create() {
        return new PackageFragment();
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

    FileBrowseDialog m_fileOpenDialog;
    void fireIntentOn(String field, String value, int grpPos) {
        try {
            File root = new File(Environment.getExternalStorageDirectory().getPath() + value);
            if (!root.exists())
                root = new File(value);
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
            }
        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        m_rootView = inflater.inflate(R.layout.package_tab, container, false);

        m_listView = Ui.viewById(m_rootView, R.id.pkgListView);
        final PkgArrayAdapter adapter = new PkgArrayAdapter(this.getActivity());
        m_listView.setAdapter(adapter);

        m_listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (view == null || view.getTag() == null)
                    return false;

                final int grpPos = ((Integer) view.getTag()).intValue();
                final TextView field = Ui.viewById(view, R.id.buildField);
                final TextView value = Ui.viewById(view, R.id.buildValue);
                if (field != null && value != null) {
                    Button btn = Ui.ShowMessage(PackageFragment.this.getActivity(), field.getText() + "\n" + value.getText()).getButton(AlertDialog.BUTTON_POSITIVE);
                    if (btn != null) {
                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                fireIntentOn(field.getText().toString(), value.getText().toString(), grpPos);
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

        adapter.setOnItemLongClickListener1(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View view, int pos, long id) {
                int grpPos = ((Integer)view.getTag()).intValue();
                PackingItem packingItem = m_list.get(pos);
                openPackageInfo(packingItem.m_packInfo.packageName);
                return true;
            }
        });

        m_listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {

                if (groupPosition >= 0 && groupPosition < m_list.size()) {
                    PackingItem packingItem = m_list.get(groupPosition);
                    Pair<String, String> keyVal = packingItem.valueListStr().get(childPosition);
                    if (keyVal.first.equals(m_regPermissionsStr)) {
                        toggleRegPermissions(packingItem, childPosition);
                    } else if (keyVal.first.equals(m_permissionsStr)) {
                        togglePermissions(packingItem, childPosition);
                    } else if (keyVal.first.equals(m_activitiesStr)) {
                        toggleActivities(packingItem, childPosition);
                    } else if (keyVal.first.equals(m_servicesStr)) {
                        toggleServices(packingItem, childPosition);
                    } else if (keyVal.first.equals(m_providers)) {
                        toggleProviders(packingItem, childPosition);
                    } else {
                        return false;
                    }

                    m_listView.collapseGroup(groupPosition);
                    m_listView.expandGroup(groupPosition);

                    return true;
                }
                return false;
            }
        });

        adapter.setOnItemLongClickListener1(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View view, int pos, long id) {
                Toast.makeText(getActivity(), String.format("Long Press on %d id:%d ", pos, id), Toast.LENGTH_LONG).show();
                int grpPos = ((Integer)view.getTag()).intValue();
                if (pos >= 0 && pos < m_list.size()) {
                    PackingItem packingItem = m_list.get(pos);
                    openPackageInfo(packingItem.m_packInfo.packageName);
                    return true;
                }
                return false;
            }
        });

        m_pkgLoadBtn = Ui.viewById(m_rootView, R.id.pkgLoadBtn);
        m_pkgLoadBtn.setOnClickListener(this);
        m_pkgLoadBtn.setVisibility((m_list == null || m_list.isEmpty()) ? View.VISIBLE : View.GONE);
        m_pkgLoadBtn.setChecked(false);

        m_title = Ui.viewById(m_rootView, R.id.pkg_title);
        m_title.setOnClickListener(this);
        m_pkgUninstallBtn = Ui.viewById(m_rootView, R.id.package_uninstall);
        m_pkgUninstallBtn.setOnClickListener(this);
        m_pkgUninstallBtn.setOnLongClickListener(this);
        // m_pkgUninstallBtn.setEnabled(false);

        m_loadSpinner = Ui.viewById(m_rootView, R.id.pkg_load_spinner);
        m_loadSpinner.addOnLayoutChangeListener(this);
        m_sortSpinner = Ui.viewById(m_rootView, R.id.pkg_sort_spinner);
        m_sortSpinner.addOnLayoutChangeListener(this);
        m_expand_collapse_toggle = Ui.viewById(m_rootView, R.id.pkg_plus_minus_toggle);
        m_expand_collapse_toggle.setOnClickListener(this);

        updatePkgTitle();

        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(this.getContext()).registerReceiver(mMessageReceiver,
                new IntentFilter(UninstallIntentReceiver.MSG_PACKAGE_UNINSTALLED));


        return m_rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSelected() {
        GlobalInfo.s_globalInfo.mainFragActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        // updateList();
        // m_listView.invalidateViews();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int pos = -1;
        int id = item.getItemId();
        int show = m_show;
        switch (id) {
            case R.id.package_user:
                show = SHOW_USER;
                break;
            case R.id.package_system:
                show = SHOW_SYS;
                break;
            case R.id.package_pref:
                show = SHOW_PREF;
                break;
            case R.id.package_cache:
                show = SHOW_CACHE;
                break;
            case R.id.package_lib:
                show = SHOW_LIB;
                break;

            case R.id.package_uninstall:
                if (m_uninstallResId == R.string.package_del_cache)
                    deleteCaches();
                else
                    uninstallPackages();
                break;
            case R.id.package_uncheck_all:
                uncheckAll();
                break;

            case R.id.package_collapseAll:
                collapseAll();
                m_expand_collapse_toggle.setChecked(false);
                break;
            case R.id.package_expandAll:
                expandAll();
                m_expand_collapse_toggle.setChecked(true);
                break;
            case 0:
                break;
            default:
                item.setChecked(true);
                pos = Arrays.asList(getResources().getStringArray(R.array.pkg_sort_array)).indexOf(item.getTitle());
                m_sortSpinner.setSelection(pos);
                this.m_sortBy = id;
                Message msgObj = m_handler.obtainMessage(MSG_SORT_LIST);
                m_handler.sendMessage(msgObj);
                break;
        }

        if (m_show != show) {
            m_show = show;
            item.setChecked(true);
            m_loadSpinner.setSelection(m_show);
            // updateList();
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
        m_menu = menu.addSubMenu("Pkg Options");
        inflater.inflate(R.menu.package_menu, m_menu);
        m_menu.findItem(m_sortBy).setChecked(true);
    }

    // ============================================================================================
    // onClickListener methods

    @Override
    public void onClick(View v) {

        int id = v.getId();
        switch (id) {
            case R.id.pkgLoadBtn:
                m_pkgLoadBtn.setVisibility(View.GONE);
                updateList();
                break;
            case R.id.package_uninstall:
                 if (m_uninstallResId == R.string.package_del_cache)
                     deleteCaches();
                 else
                    uninstallPackages();
                break;
            case R.id.pkg_plus_minus_toggle:
                if (m_expand_collapse_toggle.isChecked())
                    expandAll();
                else
                    collapseAll();
                break;
            case R.id.pkg_title:
                if (TextUtils.isEmpty(m_title.getHint())) {
                    // m_title.setTag(m_title.getText());
                    m_title.setText("");
                    m_title.setHint("Filter");
                    if (m_list.size() > m_beforeFilter.size()) {
                        m_beforeFilter.clear();
                        m_beforeFilter.addAll(m_list);
                    }

                    m_title.setOnEditorActionListener(new TextView.OnEditorActionListener()
                    {
                        @Override
                        public boolean onEditorAction(TextView edView, int actionId, KeyEvent event)
                        {
                            if(actionId == EditorInfo.IME_ACTION_DONE)
                            {
                                String filter = edView.getText().toString();
                                filterPackages(filter);
                                // hideKeyboard
                                InputMethodManager imm= (InputMethodManager) edView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(edView.getWindowToken(), 0);
                                return true; // consume.
                            }
                            return false; // pass on to other listeners.
                        }
                    });
                }
                break;
        }
    }

    // ============================================================================================
    // View.OLongClickListener

    @Override
    public boolean onLongClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.package_uninstall:
                uncheckAll();
                return true;
        }

        return false;
    }

    // ============================================================================================
    // implement OnLayoutChangeListener
    @Override
    public void onLayoutChange(
            View v, int left, int top, int right,
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
        String itemStr = parent.getItemAtPosition(pos).toString();
        if (parent == m_loadSpinner) {
            m_show = pos;
            int menu_id = R.id.package_user;
            switch (pos) {
                case 0:
                    menu_id = R.id.package_user;
                    break;
                case 1:
                    menu_id = R.id.package_system;
                    break;
                case 2:
                    menu_id = R.id.package_pref;
                    break;
                case 3:
                    menu_id = R.id.package_cache;
                    break;
                case 4:
                    menu_id = R.id.package_lib;
                    break;
            }
            m_menu.findItem(menu_id).setChecked(true);
            updateList();
        } else if (parent == m_sortSpinner) {
            int menuId = -1;
            switch (pos) {
                case 0: // app
                    menuId = R.id.package_sort_by_appname;
                    break;
                case 1: // pkg
                    menuId = R.id.package_sort_by_pkgname;
                    break;
                case 2: // size
                    menuId = R.id.package_sort_by_size;
                    break;
                case 3: // update date;
                    menuId = R.id.package_sort_by_update_date;
                    break;
                case 4: // install date;
                    menuId = R.id.package_sort_by_install_date;
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
    // Internal classes

    class ArrayListPair<S, T> extends ArrayList<Pair<S, T>> {

        public boolean add(S v1, T v2) {
            return this.add(new Pair<S, T>(v1, v2));
        }
    }

    class ArrayListPairString extends ArrayListPair<String, String> {
    }


    // ============================================================================================
    // Internal methods

    private void collapseAll() {
        int count = m_listView.getAdapter().getCount(); // m_list.size();
        for (int position = 0; position < count; position++) {
            // m_log.d("Collapse " + position);
            m_listView.collapseGroup(position);
        }
    }

    private void expandAll() {
        int count = m_listView.getAdapter().getCount(); // m_list.size();
        for (int position = 0; position < count; position++)
            m_listView.expandGroup(position);
    }

    private void filterPackages(String filter) {
        if (TextUtils.isEmpty(filter) || filter.equals("*")) {
            m_workList = new ArrayList<>();
            m_workList.addAll(m_beforeFilter);
        } else {
            m_workList = new ArrayList<>();
            for (PackingItem packageItem : m_beforeFilter) {
                if (packageItem.m_packInfo.packageName.contains(filter)
                        || packageItem.m_appName.contains(filter)) {
                    m_workList.add(packageItem);
                }
            }
        }

        if (m_workList.size() != m_list.size()) {
            Message msgObj = m_handler.obtainMessage(MSG_UPDATE_DONE);
            m_handler.sendMessage(msgObj);

            // ((BaseExpandableListAdapter) m_listView.getExpandableListAdapter()).notifyDataSetChanged();
            // updateUninstallBtn();
            // updatePkgTitle();
        }
    }

    static final String PERM_PREFIX = "  ";
    private void toggleRegPermissions(PackingItem packingItem, int row) {
        PackageInfo packInfo = packingItem.m_packInfo;
        ArrayListPairString valueListStr = packingItem.valueListStr();
        row++;
        if (row < valueListStr.size() && valueListStr.get(row).first.startsWith(PERM_PREFIX)) {
            do {
                if (valueListStr.get(row).first.startsWith(PERM_PREFIX)) {
                    valueListStr.remove(row);
                } else {
                    break;
                }
            } while (row < valueListStr.size());
        } else {
            for (int pidx = 0; pidx != packInfo.requestedPermissions.length; pidx++) {
                String perm = packInfo.requestedPermissions[pidx].replaceAll("[a-z.]*", "");
                if (perm.length() > 30)
                    perm = perm.substring(0, 30);
                valueListStr.add(row++, new Pair<String, String>(String.format("  %2d", pidx), perm));
            }
        }
    }

    private void togglePermissions(PackingItem packingItem, int row) {
        PackageInfo packInfo = packingItem.m_packInfo;
        ArrayListPairString valueListStr = packingItem.valueListStr();
        row++;
        if (row < valueListStr.size() && valueListStr.get(row).first.startsWith(PERM_PREFIX)) {
            do {
                if (valueListStr.get(row).first.startsWith(PERM_PREFIX)) {
                    valueListStr.remove(row);
                } else {
                    break;
                }
            } while (row < valueListStr.size());
        } else {
            for (int pidx = 0; pidx != packInfo.permissions.length; pidx++) {
                PermissionInfo pInfo = packInfo.permissions[pidx];
                String perm = pInfo.name.replaceAll("[a-z.]*", "");
                if (perm.length() > 30)
                    perm = perm.substring(0, 30);
                valueListStr.add(row++, new Pair<String, String>(String.format("  %2d", pidx), perm));
            }
        }
    }

    private void toggleActivities(PackingItem packingItem, int row) {
        PackageInfo packInfo = packingItem.m_packInfo;
        ArrayListPairString valueListStr = packingItem.valueListStr();
        row++;
        if (row < valueListStr.size() && valueListStr.get(row).first.startsWith(PERM_PREFIX)) {
            do {
                if (valueListStr.get(row).first.startsWith(PERM_PREFIX)) {
                    valueListStr.remove(row);
                } else {
                    break;
                }
            } while (row < valueListStr.size());
        } else {
            for (int aidx = 0; aidx != packInfo.activities.length; aidx++) {
                ActivityInfo  aInfo = packInfo.activities[aidx];
                String name = aInfo.name.replaceAll(packInfo.packageName, "");
                if (name.length() > 30)
                    name = name.substring(0, 30);
                valueListStr.add(row++, new Pair<String, String>(String.format("  %2d", aidx), name));
            }
        }
    }

    private void toggleServices(PackingItem packingItem, int row) {
        PackageInfo packInfo = packingItem.m_packInfo;
        ArrayListPairString valueListStr = packingItem.valueListStr();
        row++;
        if (row < valueListStr.size() && valueListStr.get(row).first.startsWith(PERM_PREFIX)) {
            do {
                if (valueListStr.get(row).first.startsWith(PERM_PREFIX)) {
                    valueListStr.remove(row);
                } else {
                    break;
                }
            } while (row < valueListStr.size());
        } else {
            for (int aidx = 0; aidx != packInfo.services.length; aidx++) {
                ServiceInfo sInfo = packInfo.services[aidx];
                String name = sInfo.name;
                if (name.length() > 30)
                    name = name.substring(0, 30);
                valueListStr.add(row++, new Pair<String, String>(String.format("  %2d", aidx), name));
            }
        }
    }

    private void toggleProviders(PackingItem packingItem, int row) {
        PackageInfo packInfo = packingItem.m_packInfo;
        ArrayListPairString valueListStr = packingItem.valueListStr();
        row++;
        if (row < valueListStr.size() && valueListStr.get(row).first.startsWith(PERM_PREFIX)) {
            do {
                if (valueListStr.get(row).first.startsWith(PERM_PREFIX)) {
                    valueListStr.remove(row);
                } else {
                    break;
                }
            } while (row < valueListStr.size());
        } else {
            for (int pidx = 0; pidx != packInfo.providers.length; pidx++) {
                ProviderInfo providerInfo = packInfo.providers[pidx];
                String provStr = providerInfo.name;
                if (provStr.length() > 30)
                    provStr = provStr.substring(provStr.length() - 30);
                valueListStr.add(row++, new Pair<String, String>(String.format("  %2d", pidx), provStr));
            }
        }
    }

    private void updatePkgTitle() {
        if (m_list != null) {
            m_title.setText(String.format("%d Packages", m_list.size()));
            if (m_expand_collapse_toggle.isChecked())
                expandAll();
        } else
            m_title.setText("No packages");

        m_title.setHint("");
    }

    /**
     * Uncheck all items.
     */
    private void uncheckAll() {
        for (PackingItem packageItem : m_list) {
            packageItem.m_checked = false;
        }
        ((BaseExpandableListAdapter) m_listView.getExpandableListAdapter()).notifyDataSetChanged();
        updateUninstallBtn();
    }

    private void removePackage(String packageName) {
        for (PackingItem packageItem : m_list) {
            if (packageName.equals(packageItem.m_packInfo.packageName)) {
                m_list.remove(packageItem);
                m_workList = new ArrayList<PackingItem>();
                m_workList.addAll(m_list);
                Message msgObj = m_handler.obtainMessage(MSG_UPDATE_DONE);
                m_handler.sendMessage(msgObj);
                break;
            }
        }
        // ((BaseExpandableListAdapter) m_listView.getExpandableListAdapter()).notifyDataSetChanged();
        updateUninstallBtn();
        updatePkgTitle();
    }

    /**
     * Uninstall checked items.
     */
    private void uninstallPackages() {
        ArrayList<PackageInfo> uninstallList = new ArrayList<PackageInfo>();
        for (PackingItem packageItem : m_list) {
            if (packageItem.m_checked) {
                uninstallList.add(packageItem.m_packInfo);
                // m_checkCnt--;
                updateUninstallBtn();
            }
        }

        UninstallDialog.showDialog(this, uninstallList, 0);
        // m_checkCnt = 0;
        updateUninstallBtn();
    }

    /**
     * Delete cache files.
     */
    private void deleteCaches() {
        ArrayList<PackageInfo> uninstallList = new ArrayList<PackageInfo>();
        for (PackingItem packageItem : m_list) {
            if (packageItem.m_checked) {

                try {
                    PackageInfo packInfo = packageItem.m_packInfo;
                    long cacheSize = 0;
                    long fileCount = 0;
                    Context mContext = getActivity().createPackageContext(packInfo.packageName, Context.CONTEXT_IGNORE_SECURITY);

                    File cacheDirectory = null;
                    Utils.DirSizeCount cacheDirSize = null;
                    if (mContext.getCacheDir() != null) {
                        cacheDirectory = mContext.getCacheDir();
                        // cacheSize = cacheDirectory.length()/1024;
                        cacheDirSize = Utils.getDirectorySize(cacheDirectory);
                        if (cacheDirSize == null) {
                            // Cache is not readable or empty,
                            // Try and map cache dir to one of the sd storage paths
                            for (String storageDir : m_storageDirs) {
                                try {
                                    File cacheDirectory2 = new File(cacheDirectory.getCanonicalPath()
                                            .replace("/data/data", storageDir + "/Android/data"));
                                    if (cacheDirectory2.exists()) {
                                        cacheDirectory = cacheDirectory2;
                                        cacheDirSize = Utils.getDirectorySize(cacheDirectory);
                                        break;
                                    }
                                } catch (Exception ex) {
                                    m_log.d(ex.getMessage());
                                }
                            }
                        }

                        if (cacheDirSize != null) {
                            List<String> deletedFiles = Utils.deleteFiles(cacheDirectory);
                            if (deletedFiles == null || deletedFiles.isEmpty()) {

                            } else {
                                String fileMsg = TextUtils.join("\n", deletedFiles.toArray());
                                Toast.makeText(getContext(), packageItem.m_appName + "\n" + fileMsg, Toast.LENGTH_LONG).show();

                            }
                        }
                    }
                } catch (Exception ex) {
                    m_log.e(ex.getLocalizedMessage());
                }
            }
        }

        // ((BaseExpandableListAdapter) m_listView.getExpandableListAdapter()).notifyDataSetChanged();
        // updateList();
        loadPackages();
    }

    /**
     * Update Package list, show progress indicator while loading in background.
     */
    public void updateList() {
        // Swap colors
        int color = s_rowColor1;
        s_rowColor1 = s_rowColor2;
        s_rowColor2 = color;

        m_progress = new ProgressDialog(m_listView.getContext());
        m_progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        m_progress.setIndeterminate(true);
        m_progress.setMessage("Loading...");
        m_progress.show();

        m_pkgUninstallBtn.setEnabled((updateCheckCnt() > 0));

        // Start lengthy operation loading packages in background thread
        new Thread(new Runnable() {
            public void run() {
                loadPackages();
            }
        }).start();
    }

    private int updateCheckCnt() {
        m_checkCnt = 0;
        for (PackingItem packageItem : m_list) {
            if (packageItem.m_checked)
                m_checkCnt++;
        }
        return m_checkCnt;
    }

    /**
     * Open Application Detail Info dialog for package.
     *
     * @param packageName
     */
    void openPackageInfo(String packageName) {
        //redirect user to app Settings
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("package:" + packageName));
        startActivity(intent);
    }

    /**
     * Load Package list with User, System or Preferred items.
     */
    void loadPackages() {
        m_uninstallResId = R.string.package_uninstall;
        m_pkgUninstallBtn.post(new Runnable() {
            @Override
            public void run() {
                updateUninstallBtn();
            }
        });

        switch (m_show) {
        case SHOW_USER:
        case SHOW_SYS:
            loadInstalledPackages();
            break;
        case SHOW_PREF:
            loadDefaultPackages();
            break;
        case SHOW_CACHE:
            loadCachedPackages();
            break;
        case SHOW_LIB:
            loadLibraries();
            break;
        }

        Message msgObj = m_handler.obtainMessage(MSG_UPDATE_DONE);
        m_handler.sendMessage(msgObj);
    }

    /**
     * Load packages which are default (associated) with specific mime types.
     *
     * Use "adb shell dumpsys package r" to get full list
     */
    void loadDefaultPackages() {
        m_workList = new ArrayList<PackingItem>();

        String[] actions = {
                Intent.ACTION_SEND, Intent.ACTION_SEND, Intent.ACTION_SEND, Intent.ACTION_SEND,

                Intent.ACTION_VIEW, Intent.ACTION_VIEW,
                Intent.ACTION_VIEW, Intent.ACTION_VIEW, Intent.ACTION_VIEW,
                Intent.ACTION_VIEW, Intent.ACTION_VIEW, Intent.ACTION_VIEW,

                MediaStore.ACTION_IMAGE_CAPTURE, MediaStore.ACTION_VIDEO_CAPTURE,

                Intent.ACTION_CREATE_SHORTCUT
        };

        String[] types = {
                "audio/*", "video/*", "image/*", "text/plain",

                "application/pdf", "application/zip",
                "audio/*", "video/*", "image/*",
                "text/html", "text/plain", "text/csv",

                "image/png", "video/*",

                ""
        };


        long orderCnt = 1;
        for (int idx = 0; idx != actions.length; idx++) {
            String type = types[idx];
            Intent resolveIntent = new Intent(actions[idx]);

            if (!TextUtils.isEmpty(type)) {
                if (type.startsWith("audio/*")) {
                    Uri uri = Uri.withAppendedPath(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, "1");
                    resolveIntent.setDataAndType(uri, type);
                } else if (type.startsWith("video/*")) {
                    Uri uri = Uri.withAppendedPath(MediaStore.Video.Media.INTERNAL_CONTENT_URI, "1");
                    resolveIntent.setDataAndType(uri, type);
                } else if (type.startsWith("text/")) {
                    Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath());
                    resolveIntent.setDataAndType(uri, type);
                } else {
                    resolveIntent.setType(type);
                }
            }

            PackageManager pm = getActivity().getPackageManager();


            // PackageManager.GET_RESOLVED_FILTER);  // or PackageManager.MATCH_DEFAULT_ONLY
            List<ResolveInfo> resolveList = pm
                    .queryIntentActivities(resolveIntent, -1); // PackageManager.MATCH_DEFAULT_ONLY | PackageManager.GET_INTENT_FILTERS);

            if (resolveList != null) {
                String actType = type =  Utils.last(actions[idx].split("[.]")) + ":" + type;
                for (ResolveInfo resolveInfo : resolveList) {
                    ArrayListPairString pkgList = new ArrayListPairString();
                    String appName = resolveInfo.activityInfo.loadLabel(pm).toString().trim();

                    addList(pkgList, "Type", actType);
                    String pkgName = resolveInfo.activityInfo.packageName;
                    PackageInfo packInfo = null;
                    try {
                        packInfo = pm.getPackageInfo(pkgName, 0);
                        addList(pkgList, "Version", packInfo.versionName);
                        addList(pkgList, "VerCode", String.valueOf(packInfo.versionCode));
                        addList(pkgList, "TargetSDK", String.valueOf(packInfo.applicationInfo.targetSdkVersion));
                        m_date.setTime(packInfo.firstInstallTime);
                        addList(pkgList, "Install First", s_timeFormat.format(m_date));
                        m_date.setTime(packInfo.lastUpdateTime);
                        addList(pkgList, "Install Last", s_timeFormat.format(m_date));
                        if (resolveInfo.filter != null) {
                            if (resolveInfo.filter.countDataSchemes() > 0) {
                                addList(pkgList, "Intent Scheme", "");
                                for (int sIdx = 0; sIdx != resolveInfo.filter.countDataSchemes(); sIdx++)
                                    addList(pkgList, " ", resolveInfo.filter.getDataScheme(sIdx));
                            }
                            if (resolveInfo.filter.countActions() > 0) {
                                addList(pkgList, "Intent Action", "");
                                for (int aIdx = 0; aIdx != resolveInfo.filter.countActions(); aIdx++)
                                    addList(pkgList, " ", resolveInfo.filter.getAction(aIdx));
                            }
                            if (resolveInfo.filter.countCategories() > 0) {
                                addList(pkgList, "Intent Category", "");
                                for (int cIdx = 0; cIdx != resolveInfo.filter.countCategories(); cIdx++)
                                    addList(pkgList, " ", resolveInfo.filter.getCategory(cIdx));
                            }
                            if (resolveInfo.filter.countDataTypes() > 0) {
                                addList(pkgList, "Intent DataType", "");
                                for (int dIdx = 0; dIdx != resolveInfo.filter.countDataTypes(); dIdx++)
                                    addList(pkgList, " ", resolveInfo.filter.getDataType(dIdx));
                            }
                        }
                        m_workList.add(new PackingItem(pkgName.trim(), pkgList, packInfo, orderCnt++, appName, actType));
                    } catch (Exception ex) {
                    }
                }
            }

            if (false) {
                // TODO - look into this method, see loadCachedPackages
                int flags = PackageManager.GET_PROVIDERS;
                List<PackageInfo> packList = pm.getPreferredPackages(flags);
                if (packList != null) {
                    for (int pkgIdx = 0; pkgIdx < packList.size(); pkgIdx++) {
                        PackageInfo packInfo = packList.get(pkgIdx);

                        // if (((packInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) == showSys) {
                        addPackageInfo(packInfo);
                        // }
                    }
                }
            }
        }

        // getPreferredAppInfo();

        /*
        List<ProviderInfo> providerList = getActivity().getPackageManager().queryContentProviders(null, 0, 0);
        if (providerList != null) {
            for (ProviderInfo providerInfo : providerList) {
                String name = providerInfo.name;
                String pkg = providerInfo.packageName;

            }
        }
        */
    }

    /**
     * Get info on the preferred (launch by default) applications.
     * @return
     */
    public String getPreferredAppInfo() {
        List<PackageInfo> packages = getActivity().getPackageManager()
                .getInstalledPackages(0);
        List<IntentFilter> filters = new ArrayList<IntentFilter>();
        List<ComponentName> activities = new ArrayList<ComponentName>();
        String info = "";
        int nPref = 0, nFilters = 0, nActivities = 0;
        PackageInfo packInfo = null;
        // int orderCnt = 0;
        for (int i = 0; i < packages.size(); i++) {
            packInfo = packages.get(i);
            nPref = getActivity().getPackageManager().getPreferredActivities(filters,
                    activities, packInfo.packageName);
            nFilters = filters.size();
            nActivities = activities.size();
            if (nPref > 0 || nFilters > 0 || nActivities > 0) {
                // This is a launch by default package
                // info += "\n" + packInfo.packageName + "\n";
                ArrayListPairString pkgList = new ArrayListPairString();

                for (IntentFilter filter : filters) {
                    info += "IntentFilter:\n";
                    for (int j = 0; j < filter.countActions(); j++) {
                        addList(pkgList, "Action", filter.getAction(j));
                    }
                    for (int j = 0; j < filter.countCategories(); j++) {
                        addList(pkgList, "Category", filter.getCategory(j));
                    }
                    for (int j = 0; j < filter.countDataTypes(); j++) {
                        addList(pkgList, "Type", filter.getDataType(j));
                    }
                    for (int j = 0; j < filter.countDataAuthorities(); j++) {
                        addList(pkgList, "Authority", filter.getDataAuthority(j).toString());
                    }
                    for (int j = 0; j < filter.countDataPaths(); j++) {
                        addList(pkgList, "Path", filter.getDataPath(j).toString());
                    }
                    for (int j = 0; j < filter.countDataSchemes(); j++) {
                        addList(pkgList, "Scheme", filter.getDataScheme(j));
                    }
                    // for (ComponentName activity : activities) {
                    // info += "activity="
                    // + activity.flattenToString() + "\n";
                    // }
                }
                if (pkgList.size() != 0) {
                    m_workList.add(new PackingItem(packInfo.packageName, pkgList, packInfo, i, packInfo.applicationInfo.processName));
                }
            }
        }

        return info;
    }

    /**
     * Load installed (user or system) packages.
     */
    void loadInstalledPackages() {
        try {
            m_workList = new ArrayList<PackingItem>();
            int flags1 = PackageManager.GET_PERMISSIONS
                    | PackageManager.GET_PROVIDERS           // use hides some app, may require permissions
                    | PackageManager.GET_ACTIVITIES
                    | PackageManager.GET_RECEIVERS           // use hides some app, may require permissions
                    | PackageManager.GET_SERVICES;

            int flags2 = PackageManager.GET_PERMISSIONS
                     | PackageManager.GET_ACTIVITIES
                     | PackageManager.GET_SERVICES;


            int flags3 = PackageManager.GET_PERMISSIONS;
            int flags4 = 0;
            boolean showSys = (m_show == SHOW_SYS);

            // Some packages will not appear with some flags.
            loadAndAddPackages(showSys, flags1);
            loadAndAddPackages(showSys, flags2);
            loadAndAddPackages(showSys, flags3);
            loadAndAddPackages(showSys, flags4);

            // Sort per settings.
            // TODO *** This does not seem to be working ***
            Message msgObj = m_handler.obtainMessage(MSG_SORT_LIST);
            m_handler.sendMessage(msgObj);

        } catch (Exception ex) {
            m_log.e(ex.getMessage());
        }
    }

    void loadAndAddPackages(boolean showSys, int flags) {
        List<PackageInfo> packList = getActivity().getPackageManager().getInstalledPackages(flags);
        if (packList != null) {
            for (int idx = 0; idx < packList.size(); idx++) {
                PackageInfo packInfo = packList.get(idx);

                if (((packInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) == showSys) {
                    addPackageInfo(packInfo);
                }
            }
        }
    }

    List<PackageInfo>  mergePackages(List<PackageInfo> pkgMain, List<PackageInfo> pkgAdd) {
        if (pkgAdd != null) {
            for (int addIdx = 0; addIdx < pkgAdd.size(); addIdx++) {
                PackageInfo addInfo = pkgAdd.get(addIdx);
                boolean dup = false;
                for (int mainIdx = 0; !dup && mainIdx < pkgMain.size(); mainIdx++) {
                    PackageInfo mainInfo = pkgMain.get(mainIdx);
                    dup = mainInfo.packageName.equals(addInfo.packageName);
                }
                if (!dup) {
                    pkgMain.add(addInfo);
                }
            }
        }

        return pkgMain;
    }

    interface IPackageStatsObserver1 {
        void onGetStatsCompleted(PackageStats pStats, boolean succeeded);
    }

    boolean addPackageInfo(PackageInfo packInfo) {
        if (packInfo == null)
            return false;

        String packageName = packInfo.packageName.trim();
        for (PackingItem item : m_workList) {
            if (item.fieldStr().equals(packageName)) {
                return false;
            }
        }

        ArrayListPairString pkgList = new ArrayListPairString();
        String appName = packInfo.applicationInfo.loadLabel(getActivity().getPackageManager()).toString().trim();
        long pkgSize = 0;

        addList(pkgList, "Version", packInfo.versionName);
        addList(pkgList, "VerCode", String.valueOf(packInfo.versionCode));
        // addList(pkgList, "Directory", packInfo.applicationInfo.sourceDir);

        try {
            File file = new File(packInfo.applicationInfo.sourceDir);
            pkgSize = file.length();
            addList(pkgList, "FileSize", NumberFormat.getNumberInstance(Locale.getDefault()).format(pkgSize));
        } catch (Exception ex) {
        }

        if (!TextUtils.isEmpty(packInfo.applicationInfo.permission))
            addList(pkgList, "Permission", packInfo.applicationInfo.permission);
        if (packInfo.applicationInfo.sharedLibraryFiles != null)
            addList(pkgList, "ShrLibs", packInfo.applicationInfo.sharedLibraryFiles.toString());
        addList(pkgList, "TargetSDK", String.valueOf(packInfo.applicationInfo.targetSdkVersion));
        m_date.setTime(packInfo.firstInstallTime);
        addList(pkgList, "Install First", s_timeFormat.format(m_date));
        m_date.setTime(packInfo.lastUpdateTime);
        addList(pkgList, "Install Last", s_timeFormat.format(m_date));
        if (packInfo.requestedPermissions != null) {
            // addList(pkgList, "RegPermissions", Arrays.toString(packInfo.requestedPermissions).replaceAll("[a-z]*", "").replaceAll(",", "\n"));
            addList(pkgList, m_regPermissionsStr, String.format(" #%d", packInfo.requestedPermissions.length));
            if (false) {
                for (int pidx = 0; pidx != packInfo.requestedPermissions.length; pidx++) {
                    String perm = packInfo.requestedPermissions[pidx].replaceAll("[a-z.]*", "");
                    if (perm.length() > 30)
                        perm = perm.substring(0, 30);
                    addList(pkgList, String.format("  %2d", pidx), perm);
                }
            }
        }

        if (packInfo.permissions != null) {
            addList(pkgList, m_permissionsStr, String.format(" #%d", packInfo.permissions.length));
        }

        if (packInfo.activities != null) {
            addList(pkgList, m_activitiesStr, String.format(" #%d", packInfo.activities.length));
        }
        if (packInfo.services != null) {
            addList(pkgList, m_servicesStr, String.format(" #%d", packInfo.services.length));
        }

        if (Build.VERSION.SDK_INT > 21) {
            if (packInfo.splitNames != null && packInfo.splitNames.length != 0) {
                for (int splitIdx = 0; splitIdx != packInfo.splitNames.length; splitIdx++) {
                    addList(pkgList, String.format("  SplitName%2d", splitIdx), packInfo.splitNames[splitIdx]);
                }
            }
        }

        addList(pkgList, "Apk File", packInfo.applicationInfo.publicSourceDir);
        StringBuilder flagStr = new StringBuilder();
        if ((packInfo.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0)
            flagStr.append(" Debug ");
        if ((packInfo.applicationInfo.flags & ApplicationInfo.FLAG_IS_GAME) != 0)
            flagStr.append(" Game ");
        if ((packInfo.applicationInfo.flags & ApplicationInfo.FLAG_ALLOW_BACKUP) != 0)
            flagStr.append(" AllowBackup ");
        if ((packInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
            flagStr.append(" System ");
        if ((packInfo.applicationInfo.flags & ApplicationInfo.FLAG_LARGE_HEAP) != 0)
            flagStr.append(" LargeHeap ");

        if (flagStr.length() != 0) {
            addList(pkgList, "Flags", flagStr.toString());
        }


        if (packInfo.signatures != null) {
            String signatures = "";
            for (android.content.pm.Signature sig : packInfo.signatures) {
                signatures = signatures + " " + sig.toCharsString();
            }
            addList(pkgList, "Signature", signatures);
        }

        if (packInfo.providers != null) {
            addList(pkgList, m_providers, String.valueOf(packInfo.providers.length));
            if (false) {
                String providers = "";
                for (ProviderInfo providerInfo : packInfo.providers) {
                    providers = providers + " " + providerInfo.name;
                }
                addList(pkgList, "Providers", providers);
            }
        }

        m_workList.add(new PackingItem(packInfo.packageName.trim(), pkgList, packInfo, pkgSize, appName));
        return true;
    }

    /**
     * Load installed (user or system) packages.
     *
     * TODO - include
     *    /data/local/tmp
     *    /sdcard/local/tmp ?
     *    /storage/sdcardx/LOST.DIR/
     *    /sdcard/download
     *
     */
    void loadCachedPackages() {
        try {
            // m_pkgUninstallBtn.setText(R.string.package_uninstall);
            m_uninstallResId = R.string.package_del_cache;
            m_pkgUninstallBtn.post(new Runnable() {
                @Override
                public void run() {
                    updateUninstallBtn();
                }
            });

            m_workList = new ArrayList<PackingItem>();

            // PackageManager.GET_SIGNATURES | PackageManager.GET_PERMISSIONS | PackageManager.GET_PROVIDERS;
            int flags1 = PackageManager.GET_META_DATA
                    | PackageManager.GET_SHARED_LIBRARY_FILES
                    | PackageManager.GET_INTENT_FILTERS
                    | PackageManager.MATCH_DISABLED_UNTIL_USED_COMPONENTS;
            int flags2 = PackageManager.GET_META_DATA
                    | PackageManager.GET_SHARED_LIBRARY_FILES;
            int flags3 = PackageManager.GET_META_DATA;
            int flags4 = 0;

            List<PackageInfo> packList = getActivity().getPackageManager().getInstalledPackages(flags1);
            /*
            packList = mergePackages(packList,
                    getActivity().getPackageManager().getInstalledPackages(flags2));
            packList = mergePackages(packList,
                    getActivity().getPackageManager().getInstalledPackages(flags3));
            packList = mergePackages(packList,
                    getActivity().getPackageManager().getInstalledPackages(flags4));
            */

            if (packList != null)
                for (int idx = 0; idx < packList.size(); idx++) {
                    PackageInfo packInfo = packList.get(idx);
                    long cacheSize = 0;
                    long fileCount = 0;

                    if (packInfo == null || packInfo.lastUpdateTime <= 0) {
                        continue;   // Bad package
                    }

                    Context pkgContext;
                    try {
                        m_log.d(String.format("%3d/%d : %s", idx, packList.size(), packInfo.packageName));
                        pkgContext = getActivity().createPackageContext(packInfo.packageName, Context.CONTEXT_IGNORE_SECURITY);
                    } catch (Exception ex) {
                        m_log.e(ex.getLocalizedMessage());
                        continue;   // Bad package
                    }

                    File cacheDirectory = null;
                    Utils.DirSizeCount cacheDirSize = null;
                    if (pkgContext.getCacheDir() != null) {
                        cacheDirectory = pkgContext.getCacheDir();
                    } else {
                        // cacheDirectory = new File(mContext.getPackageResourcePath());
                        if (pkgContext.getFilesDir() != null) {
                            String dataPath = pkgContext.getFilesDir().getPath(); // "/data/data/"
                            cacheDirectory = new File(dataPath, pkgContext.getPackageName() + "/cache");
                        }
                    }

                    /*
                    Method myUserId=UserHandle.class.getDeclaredMethod("myUserId");//ignore check this when u set ur min SDK < 17
                    int userID = (Integer) myUserId.invoke(getActivity().getPackageManager());

                    getActivity().getPackageManager().getPackageSizeInfoAsUser(packInfo.packageName, userID,
                            new android.content.pm.IPackageStatsObserver.Stub() {

                                public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
                                        throws RemoteException {

                                    Log.i("fxx", "codeSize: " + pStats.codeSize);
                                }
                            });
                    */


                    if (cacheDirectory != null)
                    {
                        // cacheSize = cacheDirectory.length()/1024;
                        cacheDirSize  = Utils.getDirectorySize(cacheDirectory);
                        if (true) {
                            // Cache is not readable or empty,
                            // Try and map cache dir to one of the sd storage paths
                            for (String storageDir : m_storageDirs) {
                                try {
                                    String path = cacheDirectory.getCanonicalPath();
                                    File cacheDirectory2 = new File(path
                                            .replace("/data/data", storageDir + "/Android/data"));
                                    if (cacheDirectory2.exists()) {
                                        cacheDirectory = cacheDirectory2;
                                        Utils.DirSizeCount dirSize = Utils.getDirectorySize(cacheDirectory2);
                                        if (cacheDirSize == null || dirSize.size > cacheDirSize.size) {
                                            cacheDirSize = dirSize;
                                            cacheDirectory = cacheDirectory2;
                                        }
                                    }
                                } catch (Exception ex) {
                                    m_log.d(ex.getMessage());
                                }
                            }
                        }
                    } else {
                        m_log.d(packInfo.packageName + " missing cache dir");
                    }

                    Utils.DirSizeCount datDirSize = null;
                    if (packInfo.applicationInfo.dataDir != null) {
                        try {
                            datDirSize = Utils.getDirectorySize(new File(packInfo.applicationInfo.dataDir));
                        } catch (Exception ex) {

                        }
                    }


/*
                    Method getPackageSizeInfo;
                    try {
                        getPackageSizeInfo = getActivity().getPackageManager().getClass().getMethod(
                                "getPackageSizeInfo", String.class,
                                Class.forName("android.content.pm.IPackageStatsObserver"));

                        getPackageSizeInfo.invoke(getActivity().getPackageManager(), packInfo.packageName,
                                new IPackageStatsObserver() {

                                    @Override
                                    public void onGetStatsCompleted(
                                            PackageStats pStats, boolean succeeded)
                                            throws RemoteException {

                                        totalSize = totalSize + pStats.cacheSize;
                                    }
                                }
                        );
                    } catch (Exception e) {
                        continue;
                    }
*/
                    /* if ((packInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) */ {
                        ArrayListPairString pkgList = new ArrayListPairString();
                        String appName = "unknown";
                        try {
                            appName = packInfo.applicationInfo.loadLabel(getActivity().getPackageManager()).toString().trim();
                        } catch (Exception ex) {
                            m_log.e(ex.getLocalizedMessage());
                        }
                        long pkgSize = 0;

                        addList(pkgList, "Version", packInfo.versionName);
                        addList(pkgList, "TargetSDK", String.valueOf(packInfo.applicationInfo.targetSdkVersion));
                        String installTyp = "auto";
                        if (Build.VERSION.SDK_INT >= 21) {
                            switch (packInfo.installLocation) {
                                case PackageInfo.INSTALL_LOCATION_AUTO:
                                    break;
                                case PackageInfo.INSTALL_LOCATION_INTERNAL_ONLY:
                                    installTyp = "internal";
                                    break;
                                case PackageInfo.INSTALL_LOCATION_PREFER_EXTERNAL:
                                    installTyp = "external";
                                    break;
                            }
                        }
                        addList(pkgList, "Install", installTyp);

                        // Add application info.
                        try {
                            addList(pkgList, "Allow Backup", String.valueOf((packInfo.applicationInfo.flags & FLAG_ALLOW_BACKUP) != 0));
                            addList(pkgList, "Debuggable", String.valueOf((packInfo.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0));
                            addList(pkgList, "External Storage", String.valueOf((packInfo.applicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0));
                            String themeName = getResourceName(packInfo, packInfo.applicationInfo.theme);
                            if (!TextUtils.isEmpty(themeName))
                                addList(pkgList, "Theme", themeName);
                        } catch (Exception ex) {
                            Log.d("foo", ex.getMessage());
                        }


                        try {
                            File file = new File(packInfo.applicationInfo.sourceDir);
                            pkgSize = file.length();
                        } catch (Exception ex) {
                        }

                        addList(pkgList, "Apk File", packInfo.applicationInfo.publicSourceDir);
                        addList(pkgList, "Apk Size", NumberFormat.getNumberInstance(Locale.getDefault()).format(pkgSize));

                        addList(pkgList, "Src Dir", packInfo.applicationInfo.sourceDir);
                        addList(pkgList, "lib Dir", packInfo.applicationInfo.nativeLibraryDir);

                        addList(pkgList, "dat Dir", packInfo.applicationInfo.dataDir);
                        if (null != datDirSize) {
                            addList(pkgList, "*  Dir Size", NumberFormat.getNumberInstance(Locale.getDefault()).format(datDirSize.size));
                            addList(pkgList, "*  File Count", NumberFormat.getNumberInstance(Locale.getDefault()).format(datDirSize.count));
                        }

                        if (null != cacheDirectory) {
                            addList(pkgList, "Cache", cacheDirectory.getCanonicalPath());
                            if (null != cacheDirSize) {
                                cacheSize = cacheDirSize.size;
                                addList(pkgList, "*  Dir Size", NumberFormat.getNumberInstance(Locale.getDefault()).format(cacheDirSize.size));
                                addList(pkgList, "*  File Count", NumberFormat.getNumberInstance(Locale.getDefault()).format(cacheDirSize.count));
                            }
                        }

                        if (null != packInfo.applicationInfo.sharedLibraryFiles && packInfo.applicationInfo.sharedLibraryFiles.length != 0) {
                            addList(pkgList, "ShareLibs", NumberFormat.getNumberInstance(Locale.getDefault()).format(packInfo.applicationInfo.sharedLibraryFiles.length));
                            for (String shrLibStr : packInfo.applicationInfo.sharedLibraryFiles) {
                                addList(pkgList, "  ", shrLibStr);
                            }
                        }

                        if (true) {
                            // packInfo.configPreferences; use with flag= GET_CONFIGURATIONS;
                            // packInfo.providers use with GET_PROVIDERS;

                            List< IntentFilter > outFilters = new ArrayList<IntentFilter>();
                            List < ComponentName > outActivities  = new ArrayList<ComponentName>();
                            int num = getActivity().getPackageManager().getPreferredActivities(outFilters, outActivities, packInfo.packageName);
                            if (num > 0) {
                                addList(pkgList, "Preferred #", String.valueOf(num));
                            }
                        }

                        /* if (null != cacheDirectory) */
                        // if (cacheDirSize != null)
                        {
                            m_workList.add(new PackingItem(packInfo.packageName.trim(), pkgList, packInfo, cacheSize, appName));
                        }
                    }
                }

        } catch (Exception ex) {
            m_log.e(ex.getMessage());
        }
    }


    // http://stackoverflow.com/questions/12600974/applicationinfo-loadlabel-increase-memory-consumption-of-my-service-android
    // http://stackoverflow.com/questions/12600974/applicationinfo-loadlabel-increase-memory-consumption-of-my-service-android
    String  getResourceName(PackageInfo packInfo, int resId ) throws PackageManager.NameNotFoundException {
        String resName = "";
        if (resId != 0) {
            AssetManager assetMgr = this.getActivity().createPackageContext(packInfo.packageName, 0).getAssets();
            try {
                Resources res;
                Configuration config = new Configuration();
                DisplayMetrics metrics = Utils.getDisplayMetrics(GlobalInfo.s_globalInfo.mainFragActivity);

                res = new Resources(assetMgr, metrics, config);
                resName = res.getResourceName(resId);
                if (resName.length() > 30)
                    resName = resName.substring(resName.length() - 30);
                // String resName = res.getText(resId).toString();
            } finally {
                // res.getAssets().close();
                // assetMgr.close();
            }
        }
        return resName;
    }

    /**
     * Load installed libraries
     */
    void loadLibraries() {
        try {
            m_workList = new ArrayList<PackingItem>();
            int flag = PackageManager.GET_META_DATA;
            String[] libraries = getActivity().getPackageManager().getSystemSharedLibraryNames();
            if (libraries != null && libraries.length != 0) {
                ArrayListPairString libList = new ArrayListPairString();
                addList(libList, "Libraries", String.format("%,d", libraries.length));
                long libSize = 0;
                for (String lib : libraries) {
                    addList(libList, "  ", lib);
                }

                m_workList.add(new PackingItem("Libraries", libList, null, libSize, "lib"));
            }

            // Getting status
            int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getActivity());
            if(status == ConnectionResult.SUCCESS) {

                PackageInfo packInfo = getActivity().getPackageManager().getPackageInfo(GooglePlayServicesUtil.GOOGLE_PLAY_SERVICES_PACKAGE, flag);
                addPackageInfo(packInfo);
                // GooglePlayServicesUtil.GOOGLE_PLAY_SERVICES_PACKAGE;
                // GooglePlayServicesUtil.GOOGLE_PLAY_SERVICES_VERSION_CODE;
            }

            FeatureInfo[] features = getActivity().getPackageManager().getSystemAvailableFeatures();
            if (features != null && features.length != 0) {
                // features.name
            }

        } catch (Exception ex) {
            m_log.e(ex.getMessage());
        }
    }

    void addList(ArrayListPairString list, String value1, String value2) {
        list.add(value1, value2);
    }

    void addList(Map<String, String> list, String name, String value) {
        if (!TextUtils.isEmpty(value)) {
            list.put(name, value);
        }
    }

    // Put values in List ifValue true.
    private static <M extends Map<E, E>, E> void putIf(M listObj, E v1, E v2, boolean ifValue) {
        if (ifValue) {
            listObj.put(v1, v2);
        }
    }

    int m_uninstallResId = R.string.package_uninstall;
    void updateUninstallBtn() {
        m_pkgUninstallBtn.setEnabled(updateCheckCnt() != 0);
        if (m_checkCnt != 0)
            m_pkgUninstallBtn.setText(String.format("%s %d",getString(m_uninstallResId),m_checkCnt  ));
        else
            m_pkgUninstallBtn.setText( getString(R.string.package_uninstall));
    }


    // ============================================================================================

    /**
     * Hold Package information
     */
    class PackingItem {
        final String m_fieldStr;
        final String m_valueStr;
        final ArrayListPairString m_valueList;
        final PackageInfo m_packInfo;
        String m_typeStr;
        Drawable m_iconDrawable = null;
        long m_pkgSize = 0;
        String m_appName;
        boolean m_checked = false;

        PackingItem() {
            m_fieldStr = m_valueStr = null;
            m_valueList = null;
            m_iconDrawable = null;
            m_packInfo = null;
        }

        PackingItem(String str1, ArrayListPairString list2, PackageInfo packInfo, long pkgSize, String appName) {
            m_fieldStr = str1;
            m_valueStr = null;
            m_valueList = list2;
            m_packInfo = packInfo;
            m_pkgSize = pkgSize;
            m_appName = appName;
        }

        PackingItem(String str1, ArrayListPairString list2, PackageInfo packInfo, long pkgSize, String appName, String typeStr) {
            m_fieldStr = str1;
            m_valueStr = null;
            m_valueList = list2;
            m_packInfo = packInfo;
            m_pkgSize = pkgSize;
            m_appName = appName;
            m_typeStr = typeStr;
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

        public String typeStr() {
            return m_typeStr;
        }

        public ArrayListPairString valueListStr() {
            return m_valueList;
        }

        public int getCount() {
            return (m_valueList == null) ? 0 : m_valueList.size();
        }
    }

    final static int EXPANDED_LAYOUT = R.layout.build_list_row;
    final static int SUMMARY_LAYOUT = R.layout.package_list_row;

    // ============================================================================================
    /**
     * ExpandableLis UI 'data model' class
     */
    private class PkgArrayAdapter extends BaseExpandableListAdapter
            implements  View.OnClickListener, View.OnLongClickListener {
        private final LayoutInflater m_inflater;

        public PkgArrayAdapter(Context context) {
            m_inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        AdapterView.OnItemLongClickListener m_onItemLongClickListener;
        public void setOnItemLongClickListener1( AdapterView.OnItemLongClickListener longClickList) {
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

            PackingItem packingItem = m_list.get(groupPosition);

            View expandView = null;    //  = convertView; Reuse had left overs
            if (null == expandView) {
                expandView = m_inflater.inflate(EXPANDED_LAYOUT, parent, false);
            }

            if (packingItem == null || packingItem.m_packInfo == null || packingItem.m_packInfo.lastUpdateTime <= 0)
                return expandView; // package is broken

            if (childPosition < packingItem.valueListStr().size()) {
                expandView.setTag(Integer.valueOf(groupPosition));

                Pair<String, String> keyVal = packingItem.valueListStr().get(childPosition);
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

            PackingItem packingItem = m_list.get(groupPosition);

            View summaryView = convertView;
            if (null == summaryView) {
                summaryView = m_inflater.inflate(SUMMARY_LAYOUT, parent, false);
            }

            if (packingItem == null || packingItem.m_packInfo == null || packingItem.m_packInfo.lastUpdateTime <= 0)
                return summaryView; // package is broken

            summaryView.setTag(Integer.valueOf(groupPosition));
            summaryView.setOnClickListener(this);
            summaryView.setOnLongClickListener(this);


            if (packingItem.m_iconDrawable == null && packingItem.m_packInfo != null) {
                packingItem.m_iconDrawable = packingItem.m_packInfo.applicationInfo.loadIcon(getActivity().getPackageManager());
            }

            ImageView imageView = Ui.viewById(summaryView, R.id.packageIcon);
            imageView.setImageDrawable(packingItem.m_iconDrawable);

            Ui.<TextView>viewById(summaryView, R.id.packageName).setText(packingItem.fieldStr());
            String ver =  (packingItem.m_packInfo != null ? String.format(" v%.5s", packingItem.m_packInfo.versionName) : "");
            Ui.<TextView>viewById(summaryView, appName).setText(packingItem.m_appName + ver);

            if (m_show == SHOW_PREF) {
                Ui.<TextView>viewById(summaryView, R.id.pkgSize).setText(packingItem.typeStr());
            } else {
                switch (m_sortBy) {
                    case R.id.package_sort_by_update_date:
                        if (packingItem.m_packInfo != null) {
                            Ui.<TextView>viewById(summaryView, R.id.pkgSize).setText(s_timeFormat.format(packingItem.m_packInfo.lastUpdateTime));
                        } else {
                            Ui.<TextView>viewById(summaryView, R.id.pkgSize).setText("");
                        }
                        break;
                    case R.id.package_sort_by_install_date:
                        if (packingItem.m_packInfo != null) {
                            Ui.<TextView>viewById(summaryView, R.id.pkgSize).setText(s_timeFormat.format(packingItem.m_packInfo.firstInstallTime));
                        } else {
                            Ui.<TextView>viewById(summaryView, R.id.pkgSize).setText("");
                        }
                        break;
                    default:
                        TextView sizeTv = Ui.<TextView>viewById(summaryView, R.id.pkgSize);
                        sizeTv.setText(
                                NumberFormat.getNumberInstance(Locale.getDefault()).format(packingItem.m_pkgSize));
                        int color = 0xff800000;
                        if (packingItem.m_packInfo != null
                            && packingItem.m_packInfo.installLocation != PackageInfo.INSTALL_LOCATION_INTERNAL_ONLY
                            && packingItem.m_packInfo.applicationInfo.sourceDir.startsWith("/mnt"))
                            color = 0xff008000;
                        sizeTv.setTextColor(color);
                        break;
                }
            }

            CheckBox checkBox = Ui.viewById(summaryView, R.id.pkgChecked);
            checkBox.setVisibility((m_show == SHOW_SYS) ? View.INVISIBLE : View.VISIBLE);
            checkBox.setChecked(packingItem.m_checked);
            checkBox.setTag(Integer.valueOf(groupPosition));
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
            int grpPos = ((Integer)view.getTag()).intValue();
            if (view instanceof  CheckBox) {
                boolean checked = ((CheckBox) view).isChecked();
                PackageFragment.this.m_list.get(grpPos).m_checked = checked;
                // m_checkCnt += (checked ? 1 : -1);
                updateUninstallBtn();
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
            int grpPos = ((Integer)view.getTag()).intValue();
            // PackageFragment.this.m_list.get(grpPos).m_checked = ((CheckBox)v).isChecked();
            if (m_onItemLongClickListener != null)
                return m_onItemLongClickListener.onItemLongClick(null, view, grpPos, -1);

            return true;
        }
    }
}