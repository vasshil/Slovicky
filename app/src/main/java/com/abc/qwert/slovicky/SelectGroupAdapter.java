package com.abc.qwert.slovicky;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class SelectGroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int SELECT_GROUP_VIEW_HOLDER = 0;
    private final int ADD_NEW_GROUP_VIEW_HOLDER = 1;

    private String appPath;

    private Context context;
    private ArrayList<GroupData> groupDataList;

    SelectGroupAdapter(Context context, ArrayList<GroupData> groupDataList){
        this.context = context;
        this.groupDataList = (ArrayList<GroupData>) groupDataList.clone();
        this.groupDataList.add(new GroupData("", "", new ArrayList<>()));

        appPath = context.getFilesDir() + "/";
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if(viewType == SELECT_GROUP_VIEW_HOLDER) {
            return new SelectGroupViewHolder(inflater.inflate(R.layout.select_group_item, parent, false));
        } else {
            return new AddNewGroupViewHolder(inflater.inflate(R.layout.add_new_group_item, parent, false));
        }

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) { }

    @Override
    public int getItemViewType(int position) {
        super.getItemViewType(position);
        if(position != getItemCount() - 1) {
            return SELECT_GROUP_VIEW_HOLDER;
        } else {
            return ADD_NEW_GROUP_VIEW_HOLDER;
        }

    }

    @Override
    public int getItemCount() {
        return groupDataList.size();
    }


    void deleteGroup(int position) {
        deleteGroupFromMemory(position);

        groupDataList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    private void deleteGroupFromMemory(int position) {
        File file = new File(appPath + "slovicky/groups/" + groupDataList.get(position).getGroupId() + "/");
        deleteRecursive(file);
    }

    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()){
            File[] children = fileOrDirectory.listFiles();
            if(children != null){
                for (File child : children)
                    deleteRecursive(child);
            }

        }

        fileOrDirectory.delete();
    }

}

class SelectGroupViewHolder extends RecyclerView.ViewHolder {

    private TextView groupName;
    private TextView wordsCount;
    private ImageView editGroup;
    private ImageView deleteGroup;

    public SelectGroupViewHolder(@NonNull View itemView) {
        super(itemView);

        groupName = itemView.findViewById(R.id.group_name_item);
        wordsCount = itemView.findViewById(R.id.words_count_item);
        editGroup = itemView.findViewById(R.id.edit_group);
        deleteGroup = itemView.findViewById(R.id.delete_group);

    }

    public TextView getGroupName() {
        return groupName;
    }

    public TextView getWordsCount() {
        return wordsCount;
    }

    public ImageView getEditGroup() {
        return editGroup;
    }

    public ImageView getDeleteGroup() {
        return deleteGroup;
    }
}

class AddNewGroupViewHolder extends RecyclerView.ViewHolder {

    public AddNewGroupViewHolder(@NonNull View itemView) {
        super(itemView);

    }

}

class GroupData {
    private String groupName;
    private String groupId;
    private ArrayList<WordData> wordDataList;

    GroupData(String groupName, String groupId, ArrayList<WordData> wordDataList){
        this.groupName = groupName;
        this.groupId = groupId;
        this.wordDataList = wordDataList;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupId() {
        return groupId;
    }

    public ArrayList<WordData> getWordDataList() {
        return wordDataList;
    }

    public void setWordDataList(ArrayList<WordData> wordDataList) {
        this.wordDataList = wordDataList;
    }

}
