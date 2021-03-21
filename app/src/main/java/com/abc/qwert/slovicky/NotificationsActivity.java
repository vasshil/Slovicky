package com.abc.qwert.slovicky;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.skydoves.balloon.ArrowOrientation;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.OnBalloonDismissListener;
import com.xiaofeng.flowlayoutmanager.FlowLayoutManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import static com.abc.qwert.slovicky.MainActivity.notificationTime;
import static com.abc.qwert.slovicky.MainActivity.wordsListNotificationData;


public class NotificationsActivity extends AppCompatActivity {

    private int selectedGroup = 0;

    private WordsListNotificationAdapter wordsListNotificationAdapter;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);



        // times recycler
        RecyclerView timesRecyclerView = findViewById(R.id.times_recycler_view);
        NotificationTimesAdapter notificationTimesAdapter =
                new NotificationTimesAdapter(TimePair.generateStringList(notificationTime.generateAllData()), this) {

                    @Override
                    public void onBindViewHolder(@NonNull NotificationTimesViewHolder holder, int position) {
                        holder.time.setText(dataList.get(position));

                        int realPosition = holder.getAdapterPosition();

                        if (realPosition == getItemCount() - 1) {
                            holder.itemView.setOnClickListener(view -> {
                                TimePickerDialog timePickerDialog = new TimePickerDialog(NotificationsActivity.this, (timePicker, i, i1) -> {

                                    if (noDuplicates(i, i1)) {
                                        TimePair time = new TimePair(i, i1);
                                        holder.time.setText(time.toString());
                                        dataList.add(getItemCount() - 1, time.toString());
                                        notifyItemInserted(getItemCount() - 1);
                                        notifyItemRangeChanged(0, getItemCount());
                                        MainActivity.notificationTime.addTimeManual(time);
                                        MainActivity.notificationTime.saveTimeManual();


                                        createRegularNotification(time);
                                    }

                                }, 0, 0, true);
                                timePickerDialog.show();
                            });
                        } else {
                            holder.itemView.setOnLongClickListener(view -> {

                                deleteRegularNotification(new TimePair(dataList.remove(realPosition)));

                                int autoLength = MainActivity.notificationTime.timeDataAuto.size();
                                if (autoLength == 0 && realPosition != 0) {
                                    MainActivity.notificationTime.timeDataManual.remove(realPosition);
                                    MainActivity.notificationTime.saveTimeManual();
                                } else {
                                    if (realPosition < autoLength) {
                                        MainActivity.notificationTime.timeDataAuto.remove(realPosition);
                                        MainActivity.notificationTime.saveTimeAuto();
                                    } else {
                                        MainActivity.notificationTime.timeDataManual.remove(realPosition - autoLength);
                                        MainActivity.notificationTime.saveTimeManual();
                                    }
                                }

                                notifyItemRemoved(realPosition);
                                notifyItemRangeChanged(0, getItemCount());


                                return false;
                            });
                        }
                    }

                };
        timesRecyclerView.setAdapter(notificationTimesAdapter);
        FlowLayoutManager manager = new FlowLayoutManager();
        manager.setAutoMeasureEnabled(true);
        timesRecyclerView.setLayoutManager(manager);


        TextView fromTime = findViewById(R.id.from_time);
        TextView toTime = findViewById(R.id.to_time);

        // from time
        fromTime.setText(notificationTime.getFromTimePair().toString());
        fromTime.setOnClickListener(view -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (timePicker, i, i1) -> {
//                int fromHour = Math.min(i, notificationTime.toHour), fromMinutes = Math.min(i1, notificationTime.toMinutes);
                TimePair newTime = new TimePair(i, i1);
                TimePair maxPossibleTime = new TimePair(notificationTime.toHour, notificationTime.toMinutes);

                if (newTime.convertToMinutes() > maxPossibleTime.convertToMinutes()) {
                    notificationTime.toHour = i;
                    notificationTime.toMinutes = i1;
                    toTime.setText(notificationTime.getToTimePair().toString());
//                    fromHour = maxPossibleTime.hour;
//                    fromMinutes = maxPossibleTime.minutes;
                }


                fromTime.setText(new TimePair(i, i1).toString());
                notificationTime.fromHour = i;
                notificationTime.fromMinutes = i1;

                notificationTimesAdapter.changeData(notificationTime.generateAllData());
                notificationTime.saveDetailData();
                notificationTime.saveTimeAuto();

                recreateNotifications();

            }, notificationTime.fromHour, notificationTime.fromMinutes, true);
            timePickerDialog.show();
        });

        // to time
        toTime.setText(notificationTime.getToTimePair().toString());
        toTime.setOnClickListener(view -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (timePicker, i, i1) -> {
//                int toHour = Math.max(i, notificationTime.fromHour), toMinutes = Math.max(i1, notificationTime.fromMinutes);

                TimePair newTime = new TimePair(i, i1);
                TimePair minPossibleTime = new TimePair(notificationTime.fromHour, notificationTime.fromMinutes);

                if (newTime.convertToMinutes() < minPossibleTime.convertToMinutes()) {
                    notificationTime.fromHour = i;
                    notificationTime.fromMinutes = i1;
                    fromTime.setText(notificationTime.getFromTimePair().toString());

//                    toHour = minPossibleTime.hour;
//                    toMinutes = minPossibleTime.minutes;
                }


                toTime.setText(new TimePair(i, i1).toString());
                notificationTime.toHour = i;
                notificationTime.toMinutes = i1;

                notificationTimesAdapter.changeData(notificationTime.generateAllData());
                notificationTime.saveDetailData();
                notificationTime.saveTimeAuto();

                recreateNotifications();

            }, notificationTime.toHour, notificationTime.toMinutes, true);
            timePickerDialog.show();
        });

        // every minutes
        TextView everyMinutes = findViewById(R.id.every_minutes);
        everyMinutes.setText(notificationTime.everyMinutes + "");
        everyMinutes.setOnClickListener(view -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (timePicker, i, i1) -> {
                int timeInMinutes = i * 60 + i1;
                if (timeInMinutes > 0) {
                    notificationTime.everyMinutes = timeInMinutes;
                    everyMinutes.setText("" + notificationTime.everyMinutes);

                    notificationTimesAdapter.changeData(notificationTime.generateAllData());
                    notificationTime.saveDetailData();
                    notificationTime.saveTimeAuto();

                    recreateNotifications();

                }

            }, notificationTime.everyMinutes / 60, notificationTime.everyMinutes % 60, true);
            timePickerDialog.show();
        });


        //////////////////////////////////////////////////////////////


        // words to send recycler
        RecyclerView recyclerView = findViewById(R.id.words_list_notification_recycler);
        recyclerView.setNestedScrollingEnabled(false);
        MainActivity.readWordsToSend();
        wordsListNotificationAdapter = new WordsListNotificationAdapter(MainActivity.wordsListNotificationData);
        recyclerView.setAdapter(wordsListNotificationAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        ((ViewGroup) findViewById(R.id.notifications_card)).getLayoutTransition()
                .enableTransitionType(LayoutTransition.CHANGING);

        // add new words to send
        LinearLayout addNotificationWords = findViewById(R.id.add_notification_words);
        addNotificationWords.setOnClickListener(view -> showAddWordsBottomSheet());





        // create all notifications
        ImageView createAllNotifications = findViewById(R.id.create_notifications);
        createAllNotifications.setOnClickListener(view -> {
            createAllNotifications();
            Toast.makeText(this, getResources().getText(R.string.notifications_created), Toast.LENGTH_LONG).show();
        });

        // delete all notifications
        ImageView deleteAllNotification = findViewById(R.id.delete_all_notifications);
        deleteAllNotification.setOnClickListener(view -> {
            deleteAllNotifications(notificationTime.getAllData());
            Toast.makeText(this, getResources().getText(R.string.notifications_canceled), Toast.LENGTH_LONG).show();
        });

        // select all words
        ImageView selectAllWords = findViewById(R.id.select_all_words);
        selectAllWords.setOnClickListener(view -> {
            wordsListNotificationAdapter.selectAllWords(recyclerView);
        });

        // unselect all words
        ImageView unselectAllWords = findViewById(R.id.unselect_all_words);
        unselectAllWords.setOnClickListener(view -> {
            wordsListNotificationAdapter.unselectAllWords(recyclerView);
        });

        // delete all words
        ImageView deleteAllWords = findViewById(R.id.delete_all_words);
        deleteAllWords.setOnClickListener(view -> {
            int lastSize = wordsListNotificationData.size();
            wordsListNotificationData.clear();
            wordsListNotificationAdapter.updateData(wordsListNotificationData);
            wordsListNotificationAdapter.notifyItemRangeRemoved(0, lastSize);

            MainActivity.saveWordsToSend(wordsListNotificationData, false);
        });


        ImageView closeButton = findViewById(R.id.button_close);
        closeButton.setOnClickListener(view -> {
            finish();
        });





        new Handler().post(() -> {
            showTooltip(
                    fromTime,
                    getTipText(R.string.setup_time),
                    TooltipOrientation.RIGHT,
                    ArrowOrientation.LEFT,
                    () -> {
                        new Handler().post(() -> {
                            showTooltip(
                                    timesRecyclerView.findViewHolderForAdapterPosition(0).itemView,
                                    getTipText(R.string.delete_time),
                                    TooltipOrientation.RIGHT,
                                    ArrowOrientation.LEFT,
                                    () -> {
                                        new Handler().post(() -> {
                                            showTooltip(
                                                    addNotificationWords,
                                                    getTipText(R.string.add_words_to_send),
                                                    TooltipOrientation.TOP,
                                                    ArrowOrientation.BOTTOM,
                                                    () -> {
                                                        new Handler().post(() -> {
                                                            showTooltip(
                                                                    createAllNotifications,
                                                                    getTipText(R.string.create_all_notifications_tip),
                                                                    TooltipOrientation.RIGHT,
                                                                    ArrowOrientation.LEFT,
                                                                    () -> {
                                                                        finish();
                                                                        MainActivity.showTooltips = false;
                                                                        MainActivity.saveSettings();
                                                                    });

                                                        });
                                                    });

                                        });
                                    });

                        });
                    });

        });

    }



    @SuppressLint("UseCompatLoadingForDrawables")
    public void showAddWordsBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(this).inflate(
                R.layout.notifications_bottom_sheet, findViewById(R.id.notification_content_container));


        ArrayList<String> groupNames = new ArrayList<>();
        for (GroupData groupData : MainActivity.groupDataList) {
            groupNames.add(groupData.getGroupName().isEmpty() ?
                    this.getResources().getString(R.string.no_name_group_name) :
                    groupData.getGroupName());
        }


        RecyclerView wordsToAddRecycler = bottomSheetView.findViewById(R.id.words_to_add);
        AddNewWordsToNotificationsAdapter adapter =
                new AddNewWordsToNotificationsAdapter(MainActivity.groupDataList.get(selectedGroup).getWordDataList());
        wordsToAddRecycler.setAdapter(adapter);
        wordsToAddRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));


        Spinner selectGroup = bottomSheetView.findViewById(R.id.select_group_dialog);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.theme_spinner_item, groupNames);
        selectGroup.setAdapter(arrayAdapter);
        selectGroup.setSelection(selectedGroup);
        selectGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedGroup = i;
                adapter.changeData(MainActivity.groupDataList.get(selectedGroup).getWordDataList());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        ImageView addWords = bottomSheetView.findViewById(R.id.add_words);
        addWords.setOnClickListener(view -> {
            ArrayList<WordsListNotificationData> dataList = new ArrayList<>();
            for (WordsListNotificationData word : adapter.words) {
                if (word.needToSend && noDuplicates(word)) {
                    dataList.add(word);
                }
            }
            wordsListNotificationAdapter.updateData(dataList);
            MainActivity.saveWordsToSend(dataList, true);

            bottomSheetDialog.cancel();
        });

        ImageView selectAllWords = bottomSheetView.findViewById(R.id.select_all_words);
        selectAllWords.setOnClickListener(view -> {
            adapter.selectAllWords();
        });

        ImageView unselectAllWords = bottomSheetView.findViewById(R.id.unselect_all_words);
        unselectAllWords.setOnClickListener(view -> {
            adapter.unselectAllWords();
        });




        bottomSheetDialog.setOnDismissListener(dialogInterface -> {
//            scaleWithSheetClose(openedFrom);

        });


        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();

    }

    private boolean noDuplicates(WordsListNotificationData word) {
        for (WordsListNotificationData wordsListNotificationData : wordsListNotificationAdapter.dataList) {
            if (wordsListNotificationData.wordId.equals(word.wordId)) {
                return false;
            }
        }
        return true;
    }



    private void createRegularNotification(TimePair timePair) {

        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, timePair.hour);
        calendar.set(Calendar.MINUTE, timePair.minutes);
        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND) + 5);


        Intent intent = new Intent(getApplicationContext(), NotificationsReceiver.class);
        intent.setAction("MY_NOTIFICATION_MESSAGE");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, timePair.convertToMinutes(), intent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
//        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

    }

    private void deleteRegularNotification(TimePair timePair) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent intent = new Intent(getApplicationContext(), NotificationsReceiver.class);
        intent.setAction("MY_NOTIFICATION_MESSAGE");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, timePair.convertToMinutes(), intent, 0);

        alarmManager.cancel(pendingIntent);

    }

    private void recreateNotifications() {
        deleteAllNotifications(notificationTime.timeDataAuto);
        createAllNotifications();

    }


    private void createAllNotifications() {
        for (TimePair timePair : notificationTime.getAllData()) {
            createRegularNotification(timePair);
        }
    }


    private void deleteAllNotifications(ArrayList<TimePair> notificationsTimesToDelete) {

        for (TimePair timePair : notificationsTimesToDelete) {
            deleteRegularNotification(timePair);
        }
//        for (TimePair timePair : notificationTime.getAllData()) {
//            deleteRegularNotification(timePair);
//        }
    }




    // ToolTips

    private void showTooltip(View target,
                             String text,
                             TooltipOrientation orientation,
                             ArrowOrientation arrowOrientation,
                             OnBalloonDismissListener listener) {

        if (MainActivity.showTooltips) {
            Balloon balloon = new Balloon.Builder(this)
                    .setArrowOrientation(arrowOrientation)
                    .setPaddingTop(6)
                    .setPaddingBottom(6)
                    .setPaddingLeft(8)
                    .setPaddingRight(8)
                    .setArrowPosition(0.5f)
                    .setTextSize(13f)
                    .setCornerRadius(10f)
                    .setText(text)
                    .setTextColor(ContextCompat.getColor(this, R.color.purple_500))
                    .setBackgroundColor(ContextCompat.getColor(this, R.color.teal_200))
                    .setDismissWhenClicked(true)
                    .setDismissWhenTouchOutside(true)
                    .setOnBalloonDismissListener(listener)
                    .setBalloonAnimation(BalloonAnimation.OVERSHOOT)
                    .build();


            switch (orientation) {
                case TOP:
                    balloon.showAlignTop(target);
                    break;
                case RIGHT:
                    balloon.showAlignRight(target);
                    break;
                case BOTTOM:
                    balloon.showAlignBottom(target);
                    break;
                case LEFT:
                    balloon.showAlignLeft(target);
                    break;
            }

        }

    }



    private String getTipText(int id) {
        return getResources().getString(id);
    }


}