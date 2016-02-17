package com.example.dayanandasaraswathi.cloud;

/**
 * Created by DayanandaSaraswathi on 10/28/2015.
 */
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferType;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Filedownload extends ListActivity {

    private static final int DOWNLOAD_SELECTION_REQUEST_CODE = 1;

    // Indicates no row element has beens selected
    private static final int INDEX_NOT_CHECKED = -1;

    private TransferUtility transferUtility;
    private Button downloadBtn;
    private Button pauseBtn;
    private Button resumeBtn;
    private Button cancelBtn;
    private Button deleteBtn;
    private Button pauseAllBtn;
    private Button cancelAllBtn;

    private SimpleAdapter simpleAdapter;
    private List<TransferObserver> observers;

    private ArrayList<HashMap<String, Object>> transferRecordMaps;
    private int checkedIndex;
    private static final String BUCKET_NAME = "cloudcomputing-phase1";
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filedownload);
        transferUtility = S3Utils.getTransferUtility(this);
        initData();
        initUI();
    }

    private void initData() {
        checkedIndex = INDEX_NOT_CHECKED;
        transferRecordMaps = new ArrayList<HashMap<String, Object>>();
        observers = transferUtility.getTransfersWithType(TransferType.DOWNLOAD);
        for (TransferObserver observer : observers) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            S3Utils.fillMap(map, observer, false);
            transferRecordMaps.add(map);

            if (!TransferState.COMPLETED.equals(observer.getState())
                    && !TransferState.FAILED.equals(observer.getState())
                    && !TransferState.CANCELED.equals(observer.getState())) {
                observer.setTransferListener(new DownloadListener());
            }
        }
    }

    private void initUI() {
        simpleAdapter = new SimpleAdapter(this, transferRecordMaps,
                R.layout.listitem_view, new String[] {
                "checked", "fileName", "progress", "bytes", "state", "percentage"
        },
                new int[] {
                        R.id.radioButton1, R.id.textFileName, R.id.progressBar1, R.id.textBytes,
                        R.id.textState, R.id.textPercentage
                });
        simpleAdapter.setViewBinder(new ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data,
                                        String textRepresentation) {
                switch (view.getId()) {
                    case R.id.radioButton1:
                        RadioButton radio = (RadioButton) view;
                        radio.setChecked((Boolean) data);
                        return true;
                    case R.id.textFileName:
                        TextView fileName = (TextView) view;
                        fileName.setText((String) data);
                        return true;
                    case R.id.progressBar1:
                        ProgressBar progress = (ProgressBar) view;
                        progress.setProgress((Integer) data);
                        return true;
                    case R.id.textBytes:
                        TextView bytes = (TextView) view;
                        bytes.setText((String) data);
                        return true;
                    case R.id.textState:
                        TextView state = (TextView) view;
                        state.setText(((TransferState) data).toString());
                        return true;
                    case R.id.textPercentage:
                        TextView percentage = (TextView) view;
                        percentage.setText((String) data);
                        return true;
                }
                return false;
            }
        });
        setListAdapter(simpleAdapter);

        getListView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                if (checkedIndex != pos) {
                    transferRecordMaps.get(pos).put("checked", true);
                    if (checkedIndex >= 0) {
                        transferRecordMaps.get(checkedIndex).put("checked", false);
                    }
                    checkedIndex = pos;
                    updateButtonAvailability();
                    simpleAdapter.notifyDataSetChanged();
                }
            }
        });

        downloadBtn = (Button) findViewById(R.id.buttonDownload);
        downloadBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Filedownload.this, SelectDownloadFile.class);
                startActivityForResult(intent, DOWNLOAD_SELECTION_REQUEST_CODE);
            }
        });

        pauseBtn = (Button) findViewById(R.id.buttonPause);
        pauseBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkedIndex >= 0 && checkedIndex < observers.size()) {
                    Boolean paused = transferUtility.pause(observers.get(checkedIndex)
                            .getId());
                    if (!paused) {
                        Toast.makeText(
                                Filedownload.this,
                                "Cannot Pause transfer.  You can only pause transfers in a WAITING or IN_PROGRESS state.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        resumeBtn = (Button) findViewById(R.id.buttonResume);
        resumeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkedIndex >= 0 && checkedIndex < observers.size()) {
                    TransferObserver resumed = transferUtility.resume(observers.get(checkedIndex)
                            .getId());

                    if (resumed == null) {
                        Toast.makeText(
                                Filedownload.this,
                                "Cannot resume transfer.  You can only resume transfers in a PAUSED state.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        cancelBtn = (Button) findViewById(R.id.buttonCancel);
        cancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkedIndex >= 0 && checkedIndex < observers.size()) {
                    Boolean canceled = transferUtility.cancel(observers.get(checkedIndex).getId());
                    if (!canceled) {
                        Toast.makeText(
                                Filedownload.this,
                                "Cannot cancel transfer.  You can only resume transfers in a PAUSED, WAITING, or IN_PROGRESS state.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        deleteBtn = (Button) findViewById(R.id.buttonDelete);
        deleteBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkedIndex >= 0 && checkedIndex < observers.size()) {
                    transferUtility.deleteTransferRecord(observers.get(checkedIndex).getId());
                    observers.remove(checkedIndex);
                    transferRecordMaps.remove(checkedIndex);
                    checkedIndex = INDEX_NOT_CHECKED;
                    updateButtonAvailability();
                    updateList();
                }
            }
        });

        pauseAllBtn = (Button) findViewById(R.id.buttonPauseAll);
        pauseAllBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                transferUtility.pauseAllWithType(TransferType.DOWNLOAD);
            }
        });

        cancelAllBtn = (Button) findViewById(R.id.buttonCancelAll);
        cancelAllBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                transferUtility.cancelAllWithType(TransferType.DOWNLOAD);
            }
        });

        updateButtonAvailability();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DOWNLOAD_SELECTION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String key = data.getStringExtra("key");
                beginDownload(key);
            }
        }
    }

    private void beginDownload(String key) {
        String fileDownloadLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/Cloud/";

        file = new File(fileDownloadLocation + key);
        TransferObserver observer = transferUtility.download(BUCKET_NAME, key, file);

        observers.add(observer);
        HashMap<String, Object> map = new HashMap<String, Object>();
        S3Utils.fillMap(map, observer, false);
        transferRecordMaps.add(map);
        observer.setTransferListener(new DownloadListener());
        simpleAdapter.notifyDataSetChanged();
    }

    private void updateList() {
        TransferObserver observer = null;
        HashMap<String, Object> map = null;
        for (int i = 0; i < observers.size(); i++) {
            observer = observers.get(i);
            map = transferRecordMaps.get(i);
            S3Utils.fillMap(map, observer, i == checkedIndex);
        }
        simpleAdapter.notifyDataSetChanged();
    }

    private void updateButtonAvailability() {
        boolean availability = checkedIndex >= 0;
        pauseBtn.setEnabled(availability);
        resumeBtn.setEnabled(availability);
        cancelBtn.setEnabled(availability);
        deleteBtn.setEnabled(availability);
    }

    private class DownloadListener implements TransferListener {
        @Override
        public void onError(int id, Exception e)
        {
            updateList();
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal)
        {
            updateList();
        }

        @Override
        public void onStateChanged(int id, TransferState state) {
            updateList();
            if(state==TransferState.COMPLETED){
                if (file.getName().contains("userzipped_")){
                    String newname = file.getName().replace("userzipped_","");
                    file.renameTo(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/Cloud/"+newname));

                }else{
                    ZipManager.unzip(file.getName(),Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/Cloud/");
                    file.delete();
                }
            }
        }
    }
}