package com.abc.qwert.slovicky;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class NotificationActionReceiver extends BroadcastReceiver {

    private String appPath;
    private ArrayList<WordsListNotificationData> wordsListNotificationData;

    @Override
    public void onReceive(Context context, Intent intent) {
        appPath = context.getFilesDir() + "/";
        wordsListNotificationData = new ArrayList<>();


        String id = intent.getStringExtra("id");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Integer.parseInt(id));

        readWordsToSend();
        if (!wordsListNotificationData.isEmpty()) {
            wordsListNotificationData.get(wordsListNotificationData.size() - 1).needToSend = false;
            saveWordsToSend(wordsListNotificationData);
        }



    }

    private void readWordsToSend() {
        File wordsToSend = new File(appPath + "slovicky/notifications/wordsToSend.txt");
        ArrayList<String> data = readFromFile(wordsToSend);

        for (String word : data) {
            String[] split = word.split(" ");

            String wordWithTranslation = getWordWithTranslationById(split[0], split[1]);

            if (!wordWithTranslation.isEmpty()) {
                wordsListNotificationData.add(new WordsListNotificationData(
                        split[0],
                        split[1],
                        wordWithTranslation,
                        Boolean.parseBoolean(split[2])
                ));

            }

        }

    }

    private void saveWordsToSend(ArrayList<WordsListNotificationData> words) {
        File wordsToSend = new File(appPath + "slovicky/notifications/wordsToSend.txt");
        String[] output = new String[words.size()];
        for (int i = 0; i < words.size(); i++) {
            WordsListNotificationData word = words.get(i);
            output[i] = word.groupId + " " + word.wordId + " " + word.needToSend;
        }
        writeToFile(wordsToSend, false, output);
    }

    private String getWordWithTranslationById(String groupId, String wordId) {
        String wordWithTranslation = "";

        File word = new File(appPath + "slovicky/groups/" + groupId, wordId + ".txt");
        if (word.exists()) {
            ArrayList<String> wordData = readFromFile(word);
            wordWithTranslation = wordData.get(0) + " - " + wordData.get(1);
        }

        return wordWithTranslation;
    }

    private static ArrayList<String> readFromFile(File file){
        ArrayList<String> lines = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            String line;
            while((line = br.readLine()) != null){
                lines.add(line);
            }

            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return lines;
    }

    static void writeToFile(File file, boolean append, String[] lines) {
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
