package com.landenlabs.all_devtool.dialogs;

/*
 * Copyright (c) 2016 Dennis Lang (LanDen Labs) landenlabs@gmail.com
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * @author Dennis Lang  (3/27/2015)
 * @see http://LanDenLabs.com/
*/

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.landenlabs.all_devtool.R;
import com.landenlabs.all_devtool.util.Ui;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"Convert2Lambda", "FieldCanBeLocal"})
public class FileBrowseDialog {

    // ============================================================================================
    private static final int DARK_GRAY = 0xff444444;
    private final int FileOpen = 0;
    private final int FileSave = 1;
    private final int FolderChoose = 2;
    private final int Browser = 3;

    // Default file or directory.
    public String DefaultFileName = "default.txt";

    private boolean ShowDirs;
    private String DirPattern = ".+";

    private boolean ShowFiles;
    private String FilePattern = ".+";   // .+\..+

    private boolean ShowPerm = true;
    private boolean ShowNewFolderBtn = false;
    private boolean ShowExt = true;
    private DateFormat DateFmt = DateFormat.getDateInstance();

    private int m_selectType; // = Browser;
    private String m_defaultDirectory;
    private Context m_context;
    private TextView m_titleView;
    private TextView m_dirView;
    private String m_selectedFileName = DefaultFileName;
    private EditText m_inputText;

    private String m_dir = "";
    // private String m_ext = "";
    private List<String> m_fileList = null;
    private SimpleFileDialogListener m_simpleFileDialogListener;
    private ArrayAdapter<String> m_listAdapter = null;
    private int m_dialogHeight;

    // ============================================================================================
    // Constructor for File/Dir selection dialog.
    public FileBrowseDialog(Context context, String file_select_type, int dialogHeight,
                            SimpleFileDialogListener SimpleFileDialogListener) {

        m_dialogHeight = dialogHeight;
        switch (file_select_type) {
            case "FolderChoose":
                m_selectType = FolderChoose;
                ShowFiles = false;
                ShowDirs = true;
                break;
            case "FileSave":
                m_selectType = FileSave;
                ShowFiles = true;
                ShowDirs = false;
                break;
            case "FileOpen":
                m_selectType = FileOpen;
                ShowFiles = true;
                ShowDirs = false;
                break;
            default:
                m_selectType = Browser;
                ShowFiles = true;
                ShowDirs = true;
                break;
        }

        m_context = context;
        // m_defaultDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
        m_defaultDirectory = File.listRoots()[0].getAbsolutePath();
        m_simpleFileDialogListener = SimpleFileDialogListener;
    }

    /**
     * Open (show) file or directory selection dialog.
     *
     * @param dir Starting directory
     *            If null or empty start on SD card directory.
     */
    public void choose(String dir) {
        if (dir == null || dir.isEmpty())
            dir = m_defaultDirectory;

        File dirFile = new File(dir);
        if (!dirFile.exists())  // TODO - try and remove trailing subdir.
            dir = m_defaultDirectory;
        else if (!dirFile.isDirectory())
            dir = dirFile.getParent();

        try {
            dir = new File(dir).getCanonicalPath();
        } catch (IOException ioe) {
            return;
        }

        m_dir = dir;
        m_fileList = getDirList(dir);

        class SimpleFileDialogOnClickListener implements OnClickListener {
            public void onClick(DialogInterface dialog, int itemIdx) {
                String orgDir = m_dir;
                String sel = "" + ((AlertDialog) dialog).getListView().getAdapter().getItem(itemIdx);
                // String sel2 = m_fileList.get(itemIdx);
                if (sel.charAt(0) == '/')
                    sel = sel.substring(1, sel.length());

                // Navigate into the sub-directory
                if (sel.equals("..")) {
                    int len = Math.max(0, m_dir.lastIndexOf("/"));
                    m_dir = m_dir.substring(0, len);
                } else {
                    m_dir += "/" + sel;
                }
                m_selectedFileName = DefaultFileName;

                if ((new File(m_dir).isFile())) // If the selection is a regular file
                {
                    m_dir = orgDir;
                    m_selectedFileName = sel;
                }

                updateDirectory();
            }
        }

        AlertDialog.Builder dialogBuilder = createDirectoryChooserDialog(dir, m_fileList,
                new SimpleFileDialogOnClickListener());

        dialogBuilder.setPositiveButton("OK", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Current directory chosen
                // Call registered listener supplied with the chosen directory
                if (m_simpleFileDialogListener != null) {
                    {
                        if (m_selectType == FileOpen || m_selectType == FileSave) {
                            m_selectedFileName = m_inputText.getText() + "";
                            m_simpleFileDialogListener.onChosenDir(m_dir + "/" + m_selectedFileName);
                        } else {
                            m_simpleFileDialogListener.onChosenDir(m_dir);
                        }
                    }
                }
            }
        }).setNegativeButton("Cancel", null);

        final AlertDialog dirsDialog = dialogBuilder.create();

        // Show directory chooser dialog
        dirsDialog.show();
    }

    private boolean createSubDir(String newDir) {
        File newDirFile = new File(newDir);
        return !newDirFile.exists() && newDirFile.mkdir();
    }

    /**
     * Get Directory List (optionally subDir and/or files)
     * See ShowFiles, FilePattern, ShowDir, DirPattern
     *
     * @param dir Start directory scan at 'dir'
     * @return List of directory entries (files and/or directories)
     */
    private List<String> getDirList(String dir) {

        List<String> dirs = new ArrayList<>();

        if (dir.isEmpty() || dir.equals("/")) {
            for (File file :  File.listRoots()) {
                dirs.add("/" + file.getName());
            }
            if (dirs.size() > 1)
                return dirs;
            dir = dirs.get(0);
            dirs.clear();
        }

        try {
            File dirFile = new File(dir);

            // If directory is not the base sd card directory add ".." for going up one directory
            if (ShowDirs)
                dirs.add("..");

            File[] files= dirFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    try {
                        if (file.isDirectory()) {
                            if (ShowDirs && file.getName().matches(DirPattern)) {
                                // Add "/" to directory names to identify them in the list
                                dirs.add("/" + file.getName());
                            }
                        } else {
                            if (ShowFiles && file.getName().matches(FilePattern)) {
                                dirs.add(file.getName());
                            }
                        }
                    } catch (Exception ex) {
                        Toast.makeText(m_context, ex.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        } catch(Exception ex){
            Toast.makeText(m_context, ex.getMessage(), Toast.LENGTH_LONG).show();
        }

        Collections.sort(dirs, String.CASE_INSENSITIVE_ORDER);
        return dirs;
    }

    private AlertDialog.Builder createDirectoryChooserDialog(
            String title, List<String> listItems, OnClickListener onClickListener) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(m_context);

        // Create title text showing file select type //
        m_titleView = new TextView(m_context);
        m_titleView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        //m_titleView.setTextAppearance(m_context, android.R.style.TextAppearance_Large);
        //m_titleView.setTextColor( m_context.getResources().getColor(android.R.color.black) );

        String prompt = "";
        switch (m_selectType) {
            case FileOpen:
                prompt = "Open:";
                break;
            case FileSave:
                prompt = "Save As:";
                break;
            case FolderChoose:
                prompt = "Folder Select:";
                break;
            case Browser:
                prompt = "Browse";
        }

        m_titleView.setText(prompt);

        // Need to make this a variable Save as, Open, Select Directory
        m_titleView.setGravity(Gravity.CENTER_VERTICAL);
        m_titleView.setBackgroundColor(DARK_GRAY);
        m_titleView.setTextColor(m_context.getResources().getColor(android.R.color.white));

        // Create custom view for AlertDialog title
        LinearLayout titleLayout = new LinearLayout(m_context);
        titleLayout.setOrientation(LinearLayout.VERTICAL);
        titleLayout.addView(m_titleView);

        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        // ---- Add "Show Perm" checkbox
        CheckBox showPermCb = new CheckBox(m_context);
        showPermCb.setLayoutParams(layoutParams);
        showPermCb.setText("Show Perm");
        showPermCb.setChecked(ShowPerm);
        titleLayout.addView(showPermCb);
        showPermCb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowPerm = ((CheckBox)view).isChecked();
            }
        });

        if (ShowNewFolderBtn) {

            // ----Add  "New Folder" Button
            Button newDirButton = new Button(m_context);
            newDirButton.setLayoutParams(layoutParams);
            newDirButton.setText("New Folder");
            newDirButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final EditText input = new EditText(m_context);

                        // Show new folder name input dialog
                        new AlertDialog.Builder(m_context).
                                setTitle("New Folder Name").
                                setView(input).setPositiveButton("OK", new OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Editable newDir = input.getText();
                                String newDirName = newDir.toString();
                                // Create new directory
                                if (createSubDir(m_dir + "/" + newDirName)) {
                                    // Navigate into the new directory
                                    m_dir += "/" + newDirName;
                                    updateDirectory();
                                } else {
                                    Toast.makeText(m_context, "Failed to create '"
                                            + newDirName + "' folder", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).setNegativeButton("Cancel", null).show();
                    }
                }
            );
            titleLayout.addView(newDirButton);
        }

        // ---- Create View with folder path and entry text box
        LinearLayout viewLayout = new LinearLayout(m_context);
        viewLayout.setOrientation(LinearLayout.VERTICAL);

        m_dirView = new TextView(m_context);
        m_dirView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        m_dirView.setBackgroundColor(DARK_GRAY);
        m_dirView.setTextColor(m_context.getResources().getColor(android.R.color.white));
        m_dirView.setGravity(Gravity.CENTER_VERTICAL);
        m_dirView.setText(title);

        viewLayout.addView(m_dirView);

        if (m_selectType == FileOpen || m_selectType == FileSave) {
            m_inputText = new EditText(m_context);
            m_inputText.setText(DefaultFileName.replaceAll(".*/", ""));
            viewLayout.addView(m_inputText);
        }

        // ---- Set Views and Finish Dialog builder
        dialogBuilder.setView(viewLayout);
        dialogBuilder.setCustomTitle(titleLayout);
        m_listAdapter = createListAdapter(listItems);
        dialogBuilder.setSingleChoiceItems(m_listAdapter, -1, onClickListener);
        dialogBuilder.setCancelable(false);
        return dialogBuilder;
    }

    private void updateDirectory() {
        // Must refill list and not replace list !!
        m_fileList.clear();
        m_fileList.addAll(getDirList(m_dir));

        m_dirView.setText(m_dir);
        m_listAdapter.notifyDataSetChanged();

        if (m_selectType == FileSave || m_selectType == FileOpen) {
            m_inputText.setText(m_selectedFileName);
        }
    }

    private ArrayAdapter<String> createListAdapter(List<String> items) {
        return new ArrayAdapter<String>(m_context, R.layout.file_list_row, R.id.fl_name, items) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View itemView = super.getView(position, convertView, parent);
                String item = getItem(position);
                if (item == null) {
                    return itemView;
                }

                int rowHeight = m_dialogHeight / (this.getCount() + 2);
                if (itemView.getHeight() < rowHeight) {
                    AbsListView.LayoutParams params = (AbsListView.LayoutParams) itemView.getLayoutParams();
                    params.height = Math.min(params.height * 2, rowHeight);
                    itemView.setLayoutParams(params);
                }

                File file = new File(m_dir, item);
                TextView nameTv = Ui.viewById(itemView, R.id.fl_name);
                TextView dateTv = Ui.viewById(itemView, R.id.fl_date);
                TextView sizeTv = Ui.viewById(itemView, R.id.fl_size);
                TextView permTv = Ui.viewById(itemView, R.id.fl_perm);

                if (!ShowExt)
                    nameTv.setText(item.replaceAll("\\..*", ""));

                dateTv.setText(DateFmt.format(file.lastModified()));
                sizeTv.setText(file.isFile() ? String.format("%,d", file.length()) : "");
                //  NumberFormat.getNumberInstance(Locale.getDefault()).format( file.length());

                StringBuilder perm = new StringBuilder("Perm: ");
                perm.append(file.canRead() ? "R" : "-");
                perm.append(file.canWrite() ? "W" : "-");
                perm.append(file.canExecute() ? "X" : "-");
                perm.append(file.isHidden() ? "H" : "");
                perm.append(file.isFile() ? "F" : "");
                perm.append(file.isDirectory() ? "D" : "");
                permTv.setText(perm.toString());
                permTv.setVisibility(ShowPerm ? View.VISIBLE : View.GONE);

                return itemView;
            }
        };
    }

    // Callback interface for selected directory
    public interface SimpleFileDialogListener {
        void onChosenDir(String chosenDir);
    }
}
