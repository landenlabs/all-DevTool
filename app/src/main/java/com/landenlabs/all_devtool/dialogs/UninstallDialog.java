package com.landenlabs.all_devtool.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.landenlabs.all_devtool.DevFragment;
import com.landenlabs.all_devtool.R;
import com.landenlabs.all_devtool.util.GoogleAnalyticsHelper;
import com.landenlabs.all_devtool.util.LLog;
import com.landenlabs.all_devtool.util.Ui;

import java.util.ArrayList;


/**
 * Show Packages to uninstall.
 *
 * @author Dennis Lang
 *
 */
public class UninstallDialog extends DialogFragment {
    private final LLog mLog = LLog.DBG;

    Context m_context;
    ArrayList<PackageInfo> m_pkgInfoList;
    int m_idx;
    LinearLayout m_installGroup;
    View m_dialogLayout;
    View m_uninstallBtn;
    View m_cancelBtn;
    boolean m_prompt;
    ArrayList<CheckBox> m_checkBoxes;

/*
    private BroadcastReceiver m_uninstallReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
        mLog.i("onReceive ");
        // uninstallPackages();
        }
    };
*/

    public UninstallDialog() {
        m_pkgInfoList = null;
        m_idx = -1;
    }

    public static UninstallDialog create(DevFragment devFragment,  final ArrayList<PackageInfo> pkgInfoList, final int idx) {
        UninstallDialog uninstallDialog = new UninstallDialog();
        uninstallDialog.m_context = devFragment.getActivity();
        uninstallDialog.m_pkgInfoList = pkgInfoList;
        uninstallDialog.m_idx = idx;

        GoogleAnalyticsHelper.event(uninstallDialog.getActivity(), "", "dialog", uninstallDialog.getClass().getName());
        return uninstallDialog;
    }

    public static void showDialog(DevFragment devFragment, final ArrayList<PackageInfo> pkgInfoList, final int idx) {
        DialogFragment newFragment =  UninstallDialog.create(devFragment, pkgInfoList, idx);
        newFragment.show(devFragment.getActivity().getFragmentManager(), "dialog");
    }

    /**
     * @param pkgName package Name of Application which you want to delete
     */
    private void uninstallPackage(String pkgName)
    {
        if (!TextUtils.isEmpty(pkgName))
        {
            try
            {
                Intent intent = new Intent(Intent.ACTION_DELETE);
                intent.setData(Uri.parse("package:" + pkgName));
                startActivity(intent);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    static final String PKGINFO_LIST = "pkg_list";
    static final String PKG_IDX = "pkg_idx";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            m_pkgInfoList = savedInstanceState.getParcelableArrayList(PKGINFO_LIST);
            m_idx = savedInstanceState.getInt(PKG_IDX, m_idx);
            mLog.i("onCreateDialog " + ((m_pkgInfoList != null) ? m_pkgInfoList.size() : 0));
        } else {
            mLog.i("onCreateDialog ");
        }

        LayoutInflater inflater = getActivity().getLayoutInflater();
        m_dialogLayout = inflater.inflate(R.layout.uninstall_dlg, null);
        m_installGroup = Ui.viewById(m_dialogLayout, R.id.uninstall_group);
        m_uninstallBtn = Ui.viewById(m_dialogLayout, R.id.uninstall_all_btn);
        m_cancelBtn = Ui.viewById(m_dialogLayout, R.id.uninstall_cancel_btn);

        m_prompt = true;
        updateDialog();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(m_dialogLayout);

        m_uninstallBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!uninstallPackages()) {
                    getDialog().cancel();
                }
            }
        });

        m_cancelBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
            }
        });

/*
        this.getActivity().getApplicationContext().registerReceiver(m_uninstallReceiver, new IntentFilter(
                Intent.ACTION_PACKAGE_REMOVED));
*/
        return builder.create();
    }

    @Override
    public void onDestroy() {
        mLog.i("onDestroy");
/*
        this.getActivity().getApplicationContext().unregisterReceiver(m_uninstallReceiver);
*/
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        mLog.i("onSaveInstanceState " + m_pkgInfoList.size());
        // Save the user's current game state
        savedInstanceState.putParcelableArrayList(PKGINFO_LIST, m_pkgInfoList);
        savedInstanceState.putInt(PKG_IDX, m_idx);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mLog.i("onStart " + ((m_pkgInfoList != null) ? m_pkgInfoList.size() : 0));
        if (!m_prompt) {
            if (!uninstallPackages()) {
                getDialog().cancel();
            }
        }
        m_prompt = false;
    }

    @Override
    public void onStop() {
        mLog.i("onStop UninstallDialog");
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mLog.i("onActivityResult UninstallDialog");
    }

    private void updateDialog() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        // params.gravity= Gravity.RIGHT;

        m_checkBoxes = new ArrayList<CheckBox>();
        if (m_pkgInfoList != null && m_pkgInfoList.size() > 0) {
            m_installGroup.removeAllViews();
            for (int idx = 0; idx < m_pkgInfoList.size(); idx++) {
                PackageInfo pkgInfo = m_pkgInfoList.get(idx);
                CheckBox checkBox = new CheckBox(m_installGroup.getContext());
                checkBox.setChecked(true);
                checkBox.setText(pkgInfo.packageName);
                checkBox.setTextColor(Color.WHITE);
                checkBox.setPadding(20, 0, 20, 0);	// set padding left and right.
                checkBox.setLayoutParams(params);
                m_checkBoxes.add(checkBox);
                m_installGroup.addView(checkBox);
            }
        } else {
            m_installGroup.removeAllViews();
            TextView textView = new TextView(m_installGroup.getContext());
            textView.setText("No Packages Selected");
            textView.setTextColor(Color.WHITE);
            textView.setPadding(20, 0, 20, 0);	// set padding left and right.
            textView.setLayoutParams(params);

            m_installGroup.addView(textView);
            m_uninstallBtn.setVisibility(View.GONE);
        }
    }

    private boolean uninstallPackages() {
        int idx = 0;
        if (m_checkBoxes != null) {
            while (idx < m_checkBoxes.size()) {
                if (!m_checkBoxes.get(idx).isChecked()) {
                    m_checkBoxes.get(idx).setVisibility(View.GONE);
                    m_checkBoxes.remove(idx);
                    m_pkgInfoList.remove(idx);
                    continue;
                }
                idx++;
            }
        }

        if (m_pkgInfoList != null && m_pkgInfoList.size() != 0) {
            String packageName = m_pkgInfoList.get(0).packageName;
            if (m_checkBoxes != null && m_checkBoxes.size() != 0) {
                m_checkBoxes.get(0).setVisibility(View.GONE);
                m_checkBoxes.remove(0);
            }
            m_pkgInfoList.remove(0);
            mLog.i(m_pkgInfoList.size() + " uninstallPackages " + packageName);
            m_installGroup.invalidate();
            uninstallPackage(packageName);
            return (m_pkgInfoList.size() != 0);
        }

        mLog.i(" uninstallPackages DONE");
        return false;
    }
}
