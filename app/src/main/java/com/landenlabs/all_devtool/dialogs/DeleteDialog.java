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
 * @see https://LanDenLabs.com/
 */

package com.landenlabs.all_devtool.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.landenlabs.all_devtool.DevFragment;
import com.landenlabs.all_devtool.R;
import com.landenlabs.all_devtool.shortcuts.util.GoogleAnalyticsHelper;
import com.landenlabs.all_devtool.shortcuts.util.Ui;

import java.io.File;
import java.util.ArrayList;


/**
 * Show Files/Directories to delete and then delete them.
 *
 * @author Dennis Lang
 *
 */
@SuppressWarnings("Convert2Lambda")
public class DeleteDialog extends DialogFragment   {

    Context m_context;
    ArrayList<String> m_fileList;
    int m_idx;
    LinearLayout m_fileGroup;
    View m_dialogLayout;
    View m_deleteBtn;
    View m_cancelBtn;
    ArrayList<CheckBox> m_checkBoxes;
    Dialog.OnDismissListener m_onDismissListener;

    public DeleteDialog() {
        m_fileList = null;
        m_idx = -1;
    }

    public static DeleteDialog create(DevFragment devFragment,  final ArrayList<String> fileList, final int idx) {
        DeleteDialog deleteDialog = new DeleteDialog();
        deleteDialog.m_context = devFragment.getActivity();
        deleteDialog.m_fileList = fileList;
        deleteDialog.m_idx = idx;

        GoogleAnalyticsHelper.event(deleteDialog.getActivity(), "", "dialog", deleteDialog.getClass().getName());
        return deleteDialog;
    }

    public static DeleteDialog showDialog(DevFragment devFragment, final ArrayList<String> fileList, final int idx) {
        DeleteDialog dialog =  DeleteDialog.create(devFragment, fileList, idx);
        dialog.show(devFragment.getParentFragmentManager(), "dialog");
        return dialog;
    }

    public void setOnDismissListener(Dialog.OnDismissListener onDismissListener) {
        m_onDismissListener = onDismissListener;
    }

    static final String DELETE_LIST = "delete_list";
    static final String DELETE_IDX = "delete_idx";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            m_fileList = savedInstanceState.getStringArrayList(DELETE_LIST);
            m_idx = savedInstanceState.getInt(DELETE_IDX, m_idx);
        }

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        m_dialogLayout = inflater.inflate(R.layout.delete_dlg, null);
        m_fileGroup = Ui.viewById(m_dialogLayout, R.id.delete_group);
        m_deleteBtn = Ui.viewById(m_dialogLayout, R.id.delete_all_btn);
        m_cancelBtn = Ui.viewById(m_dialogLayout, R.id.delete_cancel_btn);

        updateDialog();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(m_dialogLayout);

        m_deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFiles();
            }
        });

        m_cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
            }
        });

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putStringArrayList(DELETE_LIST, m_fileList);
        savedInstanceState.putInt(DELETE_IDX, m_idx);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    private void updateDialog() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        m_checkBoxes = new ArrayList<>();
        if (m_fileList != null && m_fileList.size() > 0) {
            m_fileGroup.removeAllViews();
            for (int idx = 0; idx < m_fileList.size(); idx++) {
                String fileInfo = m_fileList.get(idx);
                CheckBox checkBox = new CheckBox(m_fileGroup.getContext());
                checkBox.setChecked(true);
                checkBox.setText(fileInfo);
                checkBox.setTextColor(Color.WHITE);
                checkBox.setPadding(20, 0, 20, 0);	// set padding left and right.
                checkBox.setLayoutParams(params);
                m_checkBoxes.add(checkBox);
                m_fileGroup.addView(checkBox);
            }
        } else {
            m_fileGroup.removeAllViews();
            TextView textView = new TextView(m_fileGroup.getContext());
            textView.setText("No Files Selected");
            textView.setTextColor(Color.WHITE);
            textView.setPadding(20, 0, 20, 0);	// set padding left and right.
            textView.setLayoutParams(params);

            m_fileGroup.addView(textView);
            m_deleteBtn.setVisibility(View.GONE);
        }
    }

    /**
     * Delete list of files.
     */
    private void deleteFiles() {
        int idx = 0;
        if (m_checkBoxes != null) {
            while (idx < m_checkBoxes.size()) {
                if (!m_checkBoxes.get(idx).isChecked()) {
                    m_checkBoxes.get(idx).setVisibility(View.GONE);
                    m_checkBoxes.remove(idx);
                    m_fileList.remove(idx);
                    continue;
                }
                idx++;
            }
        }

        while (m_fileList != null && m_fileList.size() != 0) {
            // String fileName = m_fileList.get(0);
            if (m_checkBoxes != null && m_checkBoxes.size() != 0) {
                m_checkBoxes.get(0).setVisibility(View.GONE);
                m_checkBoxes.remove(0);
            }

            //noinspection StatementWithEmptyBody
            if (!deleteFile(m_fileList.get(0), true)) {
                // delete failed.
            }

            m_fileList.remove(0);
            m_fileGroup.invalidate();
        }

        if (m_onDismissListener != null)
            m_onDismissListener.onDismiss(null);

        this.dismiss();
    }

    /**
     * @param fileName file to delete
     */
    private boolean deleteFile(String fileName, boolean recurse)
    {
        boolean deleted = false;
        File fileInfo = new File(fileName);
        if (fileInfo.exists()) {
            try  {
                deleted = fileInfo.delete();
                if (!deleted) {
                    Toast.makeText(this.getActivity(), "Failed to delete\n" + fileInfo.getName(), Toast.LENGTH_SHORT).show();
                }
            }
            catch (Exception ex)  {
                if (fileInfo.isDirectory() && recurse) {
                   File[] files = fileInfo.listFiles();
                    if (files != null)
                        for (File item : files) {
                            deleted |= deleteFile(item.getAbsolutePath(), true);
                        }
                    return deleteFile(fileInfo.getAbsolutePath(), false) || deleted;
                }
                Toast.makeText(this.getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
                // e.printStackTrace();
                return false;
            }
        }
        return deleted;
    }
}
