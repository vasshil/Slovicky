package com.abc.qwert.slovicky;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class EditWordsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<WordData> wordDataList;

    private final String appPath;

    private final String groupId;

    int separatorPosition;

    private final int EDIT_WORDS_VIEW_HOLDER = 0;
    private final int EDIT_WORDS_SEPARATE_VIEW_HOLDER = 1;

    boolean isBinding = false;


    EditWordsAdapter(ArrayList<WordData> wordDataList, int separatorPosition, Context context, String groupId){


        this.wordDataList = wordDataList;

        this.wordDataList.add(separatorPosition, new WordData(
                "", "", "", "-1", "", true,
                context.getResources().getString(R.string.separator_text)));

        this.separatorPosition = separatorPosition;

        appPath = context.getFilesDir() + "/";

        this.groupId = groupId;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if(viewType == EDIT_WORDS_VIEW_HOLDER){
            View view = inflater.inflate(R.layout.edit_words_item, null, false);
            return new EditWordsViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.sepparate_item, null, false);
            return new EditWordsSeparateViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) { }

    @Override
    public int getItemCount() {
        return wordDataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        super.getItemViewType(position);
        if(wordDataList.get(position).getSeparatorText().isEmpty()) {
            return EDIT_WORDS_VIEW_HOLDER;
        } else {
            return EDIT_WORDS_SEPARATE_VIEW_HOLDER;
        }

    }

    void deleteItem(int position){
        deleteWordFromMemory(position);

        wordDataList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());

        if(position < separatorPosition) separatorPosition--;

    }

    void addItem() {
        String id = "" + System.currentTimeMillis();

        wordDataList.add(separatorPosition, new WordData(
                "", "", "", groupId, id, true, ""));

        saveWordDataToFiles(separatorPosition);

        notifyItemInserted(separatorPosition);
        notifyItemRangeChanged(separatorPosition, getItemCount());

        separatorPosition++;

    }

    void saveWordDataToFiles(int position) {
        if (!groupId.isEmpty()) {
            File file = createFile(appPath + "slovicky/groups/" + groupId + "/", wordDataList.get(position).getWordId() + ".txt");
            writeToFile(file, false,
                    new String[]{
                            wordDataList.get(position).getWordCZ(),
                            wordDataList.get(position).getWordRU(),
                            wordDataList.get(position).isNeedToShow() + ""
            });
        }

    }

    void deleteWordFromMemory(int position) {
        File file = createFile(appPath + "slovicky/groups/" + groupId + "/", wordDataList.get(position).getWordId() + ".txt");
        file.delete();
    }

    private File createFile(String path, String name){
        File file = new File(path + name);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    private void writeToFile(File file, boolean append, String[] lines){
        try {
            FileWriter fw = new FileWriter(file, append);
            for (String line : lines) fw.write(line + "\n");
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}


class EditWordsViewHolder extends RecyclerView.ViewHolder {

    private EditText enterWordCZ;
    private EditText enterWordRU;

    private MaterialCheckBox needToShow;

    private ImageView delete;

    public EditWordsViewHolder(@NonNull View itemView) {
        super(itemView);

        enterWordCZ = itemView.findViewById(R.id.enter_word_cz);
        enterWordRU = itemView.findViewById(R.id.enter_word_ru);

        needToShow = itemView.findViewById(R.id.show_word);

        delete = itemView.findViewById(R.id.delete_word);

    }

    public EditText getEnterWordRU() {
        return enterWordRU;
    }

    public EditText getEnterWordCZ() {
        return enterWordCZ;
    }

    public MaterialCheckBox getNeedToShow() {
        return needToShow;
    }

    public ImageView getDelete() {
        return delete;
    }
}

class EditWordsSeparateViewHolder extends RecyclerView.ViewHolder {

    private TextView separatorText;

    private LinearLayout addNewWord;

    public EditWordsSeparateViewHolder(@NonNull View itemView) {
        super(itemView);

        separatorText = itemView.findViewById(R.id.separator_text);
        addNewWord = itemView.findViewById(R.id.add_new_word);

    }

    public TextView getSeparatorText() {
        return separatorText;
    }

    public LinearLayout getAddNewWord() {
        return addNewWord;
    }
}
