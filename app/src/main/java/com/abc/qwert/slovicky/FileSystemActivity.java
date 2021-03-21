package com.abc.qwert.slovicky;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.florent37.runtimepermission.RuntimePermission;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class FileSystemActivity extends AppCompatActivity {

    private FileSystemAdapter fileSystemAdapter;
    private ArrayList<FileSystemData> pathList;

    private TextView currentFolderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_system);

        currentFolderName = findViewById(R.id.folderName);


        ArrayList<FileSystemData> files = new ArrayList<>();


        pathList = new ArrayList<>();
        String startPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        FileSystemData firstFile = new FileSystemData(startPath, "");
        files.add(firstFile);


        RecyclerView filesList = findViewById(R.id.list_files);
        fileSystemAdapter = new FileSystemAdapter(files) {

            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onBindViewHolder(@NonNull FileSystemViewHolder holder, int position) {
                holder.fileIcon.setImageDrawable(
                        files.get(position).isFolder() ?
                                FileSystemActivity.this.getResources().getDrawable(R.drawable.ic_folder) :
                                FileSystemActivity.this.getResources().getDrawable(R.drawable.ic_file)
                );

                holder.fileName.setText(files.get(position).fileName);

                if (files.get(position).isFolder()) {
                    holder.fileDescription.setVisibility(View.GONE);

                    holder.itemView.setOnClickListener(view -> {
                        pathList.add(files.get(position));
                        ArrayList<FileSystemData> filesList = getFilesList(getAllPath());

                        if (!filesList.isEmpty()) {
                            changeData(filesList);
                            changeFolderName(pathList.get(pathList.size() - 1).fileName);
                        } else {
                            pathList.remove(pathList.size() - 1);
                            Toast.makeText(
                                    FileSystemActivity.this,
                                    getResources().getString(R.string.empty_folder),
                                    Toast.LENGTH_SHORT).show();
                        }

                    });

                } else {
                    holder.fileDescription.setVisibility(View.VISIBLE);
                    holder.fileDescription.setText(getFileSize(Integer.parseInt(files.get(position).fileDescription)));

                    holder.itemView.setOnClickListener(view -> {
                        Toast.makeText(
                                FileSystemActivity.this,
                                getResources().getString(R.string.file_selected),
                                Toast.LENGTH_SHORT).show();
                        addGroupFromMemory(Compress.unpackZip(getAllPath(), files.get(position).fileName));

                        finish();
                    });

                }

            }

        };
        filesList.setAdapter(fileSystemAdapter);
        filesList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));


        ImageView bottomButtonBack = findViewById(R.id.bottom_button_back);
        bottomButtonBack.setOnClickListener(view -> {
            onBackPressed();
        });

        ImageView bottomButtonClose = findViewById(R.id.bottom_button_close);
        bottomButtonClose.setOnClickListener(view -> {
            finish();
        });


    }


    private void addGroupFromMemory(ArrayList<ArrayList<String>> readData) {
        GroupData groupData;
        String groupName = "";
        String groupId = System.currentTimeMillis() + "";
        ArrayList<WordData> wordData = new ArrayList<>();

        long startWordId = System.currentTimeMillis();

        for (ArrayList<String> data : readData) {
            if (data.size() == 2) {
                if (data.get(0).equals("groupName.txt")) {
                    groupName = data.get(1);
                }

            } else if (data.size() >= 4) {
                if (data.get(0).endsWith(".txt")) {
                    String wordsCZ = data.get(1);
                    String wordsRU = data.get(2);
                    String needToShow = data.get(3);
                    String wordId = (startWordId++) + "";
                    boolean nts;

                    try {
                        nts = Boolean.parseBoolean(needToShow);
                    } catch (RuntimeException e) {
                        nts = true;
                    }
                    wordData.add(new WordData(wordsCZ, wordsRU, "", groupId, wordId, nts, ""));

                }
            }

        }

        groupData = new GroupData(groupName, groupId, wordData);
        MainActivity.groupDataList.add(groupData);
        MainActivity.saveGroupToMemory(groupData);

    }

    private String getAllPath() {
        String allPath = "";
        for (FileSystemData file : pathList) {
            allPath += file.fileName;
            if (file.isFolder()) allPath += "/";
        }
        return allPath;
    }

    private ArrayList<FileSystemData> getFilesList(String path) {
        ArrayList<FileSystemData> files = new ArrayList<>();

        if (isStoragePermissionGranted()) {
            File file = new File(path);
            File[] listFilesArray = file.listFiles();

            if (listFilesArray != null) {
                for (File f : listFilesArray) {
                    if (f.isDirectory()) {
                        files.add(new FileSystemData(f.getName(), ""));
                    } else {
                        if (f.getName().endsWith(".zip")) {
                            files.add(new FileSystemData(f.getName(), f.length() + ""));
                        }
                    }
                }
            }

        } else {
            RuntimePermission.askPermission(this)
                    .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)

                    .onAccepted((result) -> {
                        getFilesList(path);
                    })
                    .onDenied((result) -> {
                        new AlertDialog.Builder(this)
                                .setMessage(getResources().getString(R.string.permissions))
                                .setPositiveButton(getResources().getString(R.string.yes), (dialog, which) -> {
                                    result.askAgain();
                                })
                                .setNegativeButton(getResources().getString(R.string.no), (dialog, which) -> {
                                    dialog.dismiss();
                                })
                                .show();

                    })
                    .onForeverDenied((result) -> {

                    })
                    .ask();
        }

        return files;
    }

    @Override
    public void onBackPressed() {
        if (pathList.size() == 0) {
            super.onBackPressed();
        } else {
            pathList.remove(pathList.size() - 1);
            ArrayList<FileSystemData> filesList = new ArrayList<>();
            if (pathList.size() == 0) {
                filesList.add(new FileSystemData(Environment.getExternalStorageDirectory().getAbsolutePath(), ""));
                changeFolderName(getResources().getString(R.string.startFolderName));
            } else {
                filesList = getFilesList(getAllPath());
                changeFolderName(pathList.get(pathList.size() - 1).fileName);
            }
            fileSystemAdapter.changeData(filesList);
        }
    }

    private void changeFolderName(String newName) {
        currentFolderName
                .animate()
                .alpha(0)
                .setDuration(300)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);

                        currentFolderName.setText(newName);

                        currentFolderName
                                .animate()
                                .alpha(1)
                                .setDuration(300)
                                .setInterpolator(new FastOutSlowInInterpolator())
                                .start();
                    }
                }).start();
    }

    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (this.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    private String getFileSize(int bytes) {
        String size;

        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);

        if (bytes < 1024) { // bytes
            size = bytes + " " + getResources().getString(R.string.byte_size);

        } else if (bytes < 1024 * 1024) { // kilobytes
            size = df.format(bytes / 1024f) + " " + getResources().getString(R.string.kilobyte_size);

        } else if (bytes < 1024 * 1024 * 1024) { // megabytes
            size = df.format(bytes / 1024f / 1024f) + " " + getResources().getString(R.string.megabyte_size);

        } else { // gigabytes and larger
            size = df.format(bytes / 1024f / 1024f / 1024f) + " " + getResources().getString(R.string.gigabyte_size);

        }

        return size;
    }


}



class FileSystemAdapter extends RecyclerView.Adapter<FileSystemViewHolder> {

    ArrayList<FileSystemData> files;

    FileSystemAdapter(ArrayList<FileSystemData> files) {
        this.files = files;
    }


    @NonNull
    @Override
    public FileSystemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileSystemViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.select_file_item, parent, false)
        );
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull FileSystemViewHolder holder, int position) {


    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public void changeData(ArrayList<FileSystemData> files) {
        this.files = files;
        notifyDataSetChanged();
    }

}

class FileSystemViewHolder extends RecyclerView.ViewHolder {

    ImageView fileIcon;
    TextView fileName;
    TextView fileDescription;

    public FileSystemViewHolder(@NonNull View itemView) {
        super(itemView);

        fileIcon = itemView.findViewById(R.id.file_icon);
        fileName = itemView.findViewById(R.id.file_name);
        fileDescription = itemView.findViewById(R.id.file_description);
    }
}

class FileSystemData {
    String fileName;
    String fileDescription;

    FileSystemData(String fileName, String fileDescription) {
        this.fileName = fileName;
        this.fileDescription = fileDescription;
    }

    public boolean isFolder() {
        return fileDescription.isEmpty();
    }

}