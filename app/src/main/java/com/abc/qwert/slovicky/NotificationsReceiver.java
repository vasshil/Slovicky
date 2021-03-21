package com.abc.qwert.slovicky;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class NotificationsReceiver extends BroadcastReceiver {

    private String appPath;
    private ArrayList<WordsListNotificationData> wordsListNotificationData;

    @Override
    public void onReceive(Context context, Intent intent) {
        appPath = context.getFilesDir() + "/";
        wordsListNotificationData = new ArrayList<>();
        readWordsToSend();
        WordsListNotificationData wordToSend = selectWordToSend();

        if (wordToSend != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel wordsChannel = new NotificationChannel(
                        "Words translations",
                        context.getResources().getString(R.string.notifications_group_name),
                        NotificationManager.IMPORTANCE_HIGH);
                wordsChannel.setDescription(context.getResources().getString(R.string.notifications_group_description));

                NotificationManager manager = context.getSystemService(NotificationManager.class);
                manager.createNotificationChannel(wordsChannel);
            }

            File notificationIdFile = new File(appPath + "slovicky/notifications/", "notificationId.txt");
            int notificationId = Integer.parseInt(readFromFile(notificationIdFile).get(0));
            writeToFile(notificationIdFile, false,
                    new String[]{ (notificationId >= 1000 ? 0 : notificationId + 1) + "" });

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            Intent broadcastIntent = new Intent(context, NotificationActionReceiver.class);
            broadcastIntent.putExtra("id", notificationId + "");
            PendingIntent actionIntent = PendingIntent.getBroadcast(context, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(context, "Words translations")
                            .setSmallIcon(R.drawable.ic_notification)
                            .setContentTitle(context.getResources().getString(R.string.notification_title))
                            .setContentText(wordToSend.wordWithTranslation)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .addAction(R.drawable.ic_close, context.getResources().getString(R.string.notification_action), actionIntent)
                            .setAutoCancel(true)
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(wordToSend.wordWithTranslation));

            if (intent.getAction().equals("MY_NOTIFICATION_MESSAGE")) {
                notificationManager.notify(notificationId, builder.build());
            }
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


    private WordsListNotificationData selectWordToSend() {
        WordsListNotificationData nextWord = null;

        for (int i = 0; i < wordsListNotificationData.size(); i++) {
            if (wordsListNotificationData.get(i).needToSend) {
                nextWord = wordsListNotificationData.remove(i);
                wordsListNotificationData.add(nextWord);
                saveWordsToSend(wordsListNotificationData);
                break;
            }
        }

        return nextWord;
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

