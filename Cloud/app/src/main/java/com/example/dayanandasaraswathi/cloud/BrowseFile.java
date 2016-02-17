package com.example.dayanandasaraswathi.cloud;

/**
 * Created by DayanandaSaraswathi on 10/26/2015.
 */
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class BrowseFile extends ListActivity {

    public final static String EXTRA_FILE_PATH = "file_path";
    public final static String EXTRA_SHOW_HIDDEN_FILES = "show_hidden_files";
    public final static String EXTRA_ACCEPTED_FILE_EXTENSIONS = "accepted_file_extensions";
    private final static String DEFAULT_INITIAL_DIRECTORY = "/";

    protected File Directory;
    protected ArrayList<File> Files;
    protected BrowseFileListAdapter Adapter;
    protected boolean ShowHiddenFiles = false;
    protected String[] acceptedFileExtensions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflator = (LayoutInflater)
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View emptyView = inflator.inflate(R.layout.nofiles_view, null);
        ((ViewGroup) getListView().getParent()).addView(emptyView);
        getListView().setEmptyView(emptyView);

        // Set initial directory
        Directory = new File(DEFAULT_INITIAL_DIRECTORY);

        // Initialize the ArrayList
        Files = new ArrayList<File>();

        Adapter = new BrowseFileListAdapter(this, Files);
        setListAdapter(Adapter);

        acceptedFileExtensions = new String[] {};

        if(getIntent().hasExtra(EXTRA_FILE_PATH))
            Directory = new File(getIntent().getStringExtra(EXTRA_FILE_PATH));

        if(getIntent().hasExtra(EXTRA_SHOW_HIDDEN_FILES))
            ShowHiddenFiles = getIntent().getBooleanExtra(EXTRA_SHOW_HIDDEN_FILES, false);

        if(getIntent().hasExtra(EXTRA_ACCEPTED_FILE_EXTENSIONS)) {

            ArrayList<String> collection =
                    getIntent().getStringArrayListExtra(EXTRA_ACCEPTED_FILE_EXTENSIONS);

            acceptedFileExtensions = (String[])
                    collection.toArray(new String[collection.size()]);
        }
    }

    @Override
    protected void onResume() {
        refreshFilesList();
        super.onResume();
    }

    protected void refreshFilesList() {

        Files.clear();
        ExtensionFilenameFilter filter =
                new ExtensionFilenameFilter(acceptedFileExtensions);

        File[] files = Directory.listFiles(filter);

        if(files != null && files.length > 0) {

            for(File f : files) {

                if(f.isHidden() && !ShowHiddenFiles) {

                    continue;
                }

                Files.add(f);
            }

            Collections.sort(Files, new FileComparator());
        }

        Adapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {

        if(Directory.getParentFile() != null) {

            Directory = Directory.getParentFile();
            refreshFilesList();
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        File newFile = (File)l.getItemAtPosition(position);

        if(newFile.isFile()) {

            Intent extra = new Intent();
            extra.putExtra(EXTRA_FILE_PATH, newFile.getAbsolutePath());
            setResult(RESULT_OK, extra);
            finish();
        }
        else {

            Directory = newFile;
            refreshFilesList();
        }

        super.onListItemClick(l, v, position, id);
    }

    private class BrowseFileListAdapter extends ArrayAdapter<File> {

        private List<File> mObjects;

        public BrowseFileListAdapter(Context context, List<File> objects) {

            super(context, R.layout.filelist_view, android.R.id.text1, objects);
            mObjects = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View row = null;

            if(convertView == null) {

                LayoutInflater inflater = (LayoutInflater)
                        getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                row = inflater.inflate(R.layout.filelist_view, parent, false);
            }
            else
                row = convertView;

            File object = mObjects.get(position);

            ImageView imageView = (ImageView)row.findViewById(R.id.file_picker_image);
            TextView textView = (TextView)row.findViewById(R.id.file_picker_text);
            textView.setSingleLine(true);
            textView.setText(object.getName());

            if(object.isFile())
                imageView.setImageResource(R.drawable.ic_launcher);

            else
                imageView.setImageResource(R.drawable.ic_launcher);

            return row;
        }
    }

    private class FileComparator implements Comparator<File> {

        public int compare(File f1, File f2) {

            if(f1 == f2)
                return 0;

            if(f1.isDirectory() && f2.isFile())
                return -1;

            if(f1.isFile() && f2.isDirectory())
                return 1;
            return f1.getName().compareToIgnoreCase(f2.getName());
        }
    }

    private class ExtensionFilenameFilter implements FilenameFilter {

        private String[] Extensions;

        public ExtensionFilenameFilter(String[] extensions) {

            super();
            Extensions = extensions;
        }

        public boolean accept(File dir, String filename) {

            if(new File(dir, filename).isDirectory()) {
                return true;
            }

            if(Extensions != null && Extensions.length > 0) {

                for(int i = 0; i < Extensions.length; i++) {

                    if(filename.endsWith(Extensions[i])) {
                        return true;
                    }
                }
                return false;
            }
            return true;
        }
    }
}
