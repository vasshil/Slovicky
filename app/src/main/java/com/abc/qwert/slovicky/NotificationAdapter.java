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

import com.google.android.material.checkbox.MaterialCheckBox;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;



class AddNewWordsToNotificationsAdapter extends RecyclerView.Adapter<AddNewWordsToNotificationsViewHolder> {

    public ArrayList<WordsListNotificationData> words;

    AddNewWordsToNotificationsAdapter(ArrayList<WordData> selectedGroupWords) {
        words = new ArrayList<>();
        for (WordData wordData : selectedGroupWords) {
            words.add(new WordsListNotificationData(
                    wordData.getGroupId(),
                    wordData.getWordId(),
                    wordData.getWordCZ() + " - " + wordData.getWordRU(),
                    true)
            );
        }
    }

    @NonNull
    @Override
    public AddNewWordsToNotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AddNewWordsToNotificationsViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.add_notification_word_item, parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull AddNewWordsToNotificationsViewHolder holder, int position) {
        int realPosition = holder.getAdapterPosition();
        holder.checkBox.setChecked(words.get(realPosition).needToSend);
        holder.checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
            words.get(realPosition).needToSend = b;
        });
        holder.word.setText(words.get(realPosition).wordWithTranslation);

        holder.itemView.setOnClickListener(view -> {
            holder.checkBox.setChecked(!holder.checkBox.isChecked());
        });
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    public void changeData(ArrayList<WordData> selectedGroupWords) {
        words = new ArrayList<>();
        for (WordData wordData : selectedGroupWords) {
            words.add(new WordsListNotificationData(
                    wordData.getGroupId(),
                    wordData.getWordId(),
                    wordData.getWordCZ() + " - " + wordData.getWordRU(),
                    true)
            );
        }
        notifyDataSetChanged();
    }


    public void selectAllWords() {
        for (WordsListNotificationData wordsListNotificationData : words) {
            wordsListNotificationData.needToSend = true;
        }
        notifyDataSetChanged();
    }

    public void unselectAllWords() {
        for (WordsListNotificationData wordsListNotificationData : words) {
            wordsListNotificationData.needToSend = false;
        }
        notifyDataSetChanged();
    }

}

class AddNewWordsToNotificationsViewHolder extends RecyclerView.ViewHolder {

    MaterialCheckBox checkBox;
    TextView word;

    public AddNewWordsToNotificationsViewHolder(@NonNull View itemView) {
        super(itemView);

        checkBox = itemView.findViewById(R.id.need_to_add);
        word = itemView.findViewById(R.id.words_with_translation);
    }

}




class WordsListNotificationAdapter extends RecyclerView.Adapter<WordsListNotificationViewHolder> {

    ArrayList<WordsListNotificationData> dataList;

    WordsListNotificationAdapter(ArrayList<WordsListNotificationData> dataList) {
        this.dataList = dataList;
    }


    @NonNull
    @Override
    public WordsListNotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new WordsListNotificationViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.word_list_notification_item,
                        parent, false)
        );
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull WordsListNotificationViewHolder holder, int position) {
        int realPosition = holder.getAdapterPosition();
        holder.needToSend.setChecked(dataList.get(realPosition).needToSend);
        holder.needToSend.setOnCheckedChangeListener((compoundButton, b) -> {
            dataList.get(realPosition).needToSend = b;
            MainActivity.saveWordsToSend(dataList, false);
        });


        holder.wordWithTranslation.setText(dataList.get(realPosition).wordWithTranslation);

        holder.deleteWord.setOnClickListener(view -> {
            if (realPosition >= 0) {
                dataList.remove(realPosition);
                notifyItemRemoved(realPosition);
                notifyItemRangeChanged(position, getItemCount());
                MainActivity.saveWordsToSend(dataList, false);
            }

        });

        holder.itemView.setOnClickListener(view -> {
            holder.needToSend.setChecked(!holder.needToSend.isChecked());
        });

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void updateData(ArrayList<WordsListNotificationData> dataList) {
        this.dataList.addAll(dataList);
        notifyDataSetChanged();
    }

    public void selectAllWords(RecyclerView recyclerView) {
        for (int i = 0; i < dataList.size(); i++) {
            ((WordsListNotificationViewHolder) recyclerView.findViewHolderForAdapterPosition(i)).needToSend.setChecked(true);
        }
    }

    public void unselectAllWords(RecyclerView recyclerView) {
        for (int i = 0; i < dataList.size(); i++) {
            ((WordsListNotificationViewHolder) recyclerView.findViewHolderForAdapterPosition(i)).needToSend.setChecked(false);
        }
    }

}

class WordsListNotificationViewHolder extends RecyclerView.ViewHolder {

    MaterialCheckBox needToSend;
    TextView wordWithTranslation;
    ImageView deleteWord;

    public WordsListNotificationViewHolder(@NonNull View itemView) {
        super(itemView);

        needToSend = itemView.findViewById(R.id.need_to_send);
        wordWithTranslation = itemView.findViewById(R.id.words_with_translation);
        deleteWord = itemView.findViewById(R.id.delete_word);

    }


}

class WordsListNotificationData {

    String groupId;
    String wordId;
    String wordWithTranslation;
    boolean needToSend;

    WordsListNotificationData(String groupId, String wordId, String wordWithTranslation, boolean needToSend) {
        this.groupId = groupId;
        this.wordId = wordId;
        this.wordWithTranslation = wordWithTranslation;
        this.needToSend = needToSend;
    }

}





class NotificationTimesAdapter extends RecyclerView.Adapter<NotificationTimesViewHolder> {

    ArrayList<String> dataList;

    private Context context;

    NotificationTimesAdapter(ArrayList<String> dataList, Context context) {
        dataList.add(" + ");
        this.dataList = dataList;

        this.context = context;
    }

    @NonNull
    @Override
    public NotificationTimesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NotificationTimesViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_times_item, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationTimesViewHolder holder, int position) {}

    boolean noDuplicates(int hour, int minutes) {
        for (TimePair timePair : MainActivity.notificationTime.timeDataAuto) {
            if (timePair.hour == hour && timePair.minutes == minutes) {
                return false;
            }
        }
        for (TimePair timePair : MainActivity.notificationTime.timeDataManual) {
            if (timePair.hour == hour && timePair.minutes == minutes) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    void changeData(ArrayList<TimePair> dataList) {
        this.dataList = TimePair.generateStringList(dataList);
        this.dataList.add(" + ");
        notifyDataSetChanged();
    }


}

class NotificationTimesViewHolder extends RecyclerView.ViewHolder {

    TextView time;

    public NotificationTimesViewHolder(@NonNull View itemView) {
        super(itemView);
        time = itemView.findViewById(R.id.time);
    }
}



class NotificationTime {
    int fromHour;
    int fromMinutes;
    int toHour;
    int toMinutes;
    int everyMinutes;

    ArrayList<TimePair> timeDataAuto;
    ArrayList<TimePair> timeDataManual;

    NotificationTime(int fromHour, int fromMinutes, int toHour, int toMinutes, int everyMinutes) {
        this.fromHour = fromHour;
        this.fromMinutes = fromMinutes;
        this.toHour = toHour;
        this.toMinutes = toMinutes;
        this.everyMinutes = everyMinutes;

        timeDataAuto = getTimeDataAuto();
        timeDataManual = new ArrayList<>();
    }

    NotificationTime() {
        timeDataAuto = new ArrayList<>();
        timeDataManual = new ArrayList<>();
    }

    TimePair getFromTimePair() {
        return new TimePair(fromHour, fromMinutes);
    }

    TimePair getToTimePair() {
        return new TimePair(toHour, toMinutes);
    }


    ArrayList<TimePair> getTimeDataAuto() {
        timeDataAuto = new ArrayList<>();
        int currentHour = this.fromHour, currentMinutes = this.fromMinutes;


        for (int i = 1; i <= (toHour * 60 + toMinutes - fromHour * 60 - fromMinutes) / everyMinutes + 1; i++) {
            timeDataAuto.add(new TimePair(currentHour, currentMinutes));
            currentMinutes += everyMinutes;
            if (currentMinutes >= 60) {
                currentHour++;
                currentMinutes = currentMinutes % 60;
            }
        }

        return timeDataAuto;
    }

    void addTimeAuto(TimePair timePair) {
        timeDataAuto.add(timePair);
    }

    void addTimeManual(TimePair timePair) {
        timeDataManual.add(timePair);
    }

    public ArrayList<TimePair> generateAllData() {
        timeDataAuto = getTimeDataAuto();
        ArrayList<TimePair> data = (ArrayList<TimePair>) timeDataAuto.clone();
        data.addAll(timeDataManual);
        return data;
    }

    public ArrayList<TimePair> getAllData() {
        ArrayList<TimePair> allTime = new ArrayList<>();
        allTime.addAll(timeDataAuto);
        allTime.addAll(timeDataManual);
        return allTime;
    }



    void saveDetailData() {
        File detailData = new File(MainActivity.appPath + "slovicky/notifications/detailData.txt");
        MainActivity.writeToFile(detailData, false, new String[]{
                String.valueOf(this.fromHour),
                String.valueOf(this.fromMinutes),
                String.valueOf(this.toHour),
                String.valueOf(this.toMinutes),
                String.valueOf(this.everyMinutes)
        });
    }

    void saveTimeAuto() {
        File timeAuto = new File(MainActivity.appPath + "slovicky/notifications/timeAuto.txt");
        MainActivity.writeToFile(timeAuto, false, TimePair.generateStringArray(this.timeDataAuto));
    }

    void saveTimeManual() {
        File timeManual = new File(MainActivity.appPath + "slovicky/notifications/timeManual.txt");
        MainActivity.writeToFile(timeManual, false, TimePair.generateStringArray(this.timeDataManual));
    }


}


class TimePair {
    int hour;
    int minutes;

    public TimePair(int hour, int minutes) {
        this.hour = hour;
        this.minutes = minutes;
    }

    public TimePair(String hour, String minutes) {
        this.hour = Integer.parseInt(hour);
        this.minutes = Integer.parseInt(minutes);
    }

    public TimePair(String time) {
        String[] t = time.split(":");
        this.hour = Integer.parseInt(t[0]);
        this.minutes = Integer.parseInt(t[1]);
    }

    public int convertToMinutes() {
        return hour * 60 + minutes;
    }

    @NotNull
    @Override
    public String toString() {
        return hour + ":" + (minutes < 10 ? "0" + minutes : minutes);
    }

    static ArrayList<String> generateStringList(ArrayList<TimePair> dataList) {
        ArrayList<String> list = new ArrayList<>();
        for (TimePair timePair : dataList) list.add(timePair.toString());

        return list;
    }

    static String[] generateStringArray(ArrayList<TimePair> dataList) {
        String[] list = new String[dataList.size()];
        for (int i = 0; i < list.length; i++) {
            list[i] = dataList.get(i).toString();
        }

        return list;
    }


}








