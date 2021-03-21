package com.abc.qwert.slovicky;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;

public class ViewPagerAdapter extends PagerAdapter {

    private Context context;
    ArrayList<WordData> wordDataList;

    public ViewPagerAdapter(Context context, ArrayList<WordData> wordDataList) {
        this.context = context;

        for(int i = wordDataList.size() - 1; i >= 0; i--){
            if(!wordDataList.get(i).getSeparatorText().isEmpty() || !wordDataList.get(i).isNeedToShow()){
                wordDataList.remove(i);
            }
        }

        this.wordDataList = wordDataList;

    }

    @Override
    public int getCount() {
        return wordDataList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }


    @Override
    public int getItemPosition(@NonNull Object object) {
        super.getItemPosition(object);
        return POSITION_NONE;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        ((ViewPager) container).removeView((View) object);
    }

    void vibrate(int time){
        if(MainActivity.enableVibrations) {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if(vibrator != null){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(time, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(time);
                }
            }
        }

    }


}

class WordData {
    private String wordCZ;
    private String wordRU;
    private String enteredTranslation;

    private final String groupId;
    private final String wordId;

    private boolean needToShow;
    private final String separatorText;

    WordData(String wordCZ, String wordRU, String enteredTranslation, String groupId, String wordId, boolean needToShow, String separatorText){
        this.wordCZ = wordCZ;
        this.wordRU = wordRU;
        this.enteredTranslation = enteredTranslation;

        this.groupId = groupId;
        this.wordId = wordId;

        this.needToShow = needToShow;
        this.separatorText = separatorText;

    }

    public String getWordCZ() {
        return wordCZ;
    }

    public void setWordCZ(String wordCZ) {
        this.wordCZ = wordCZ;
    }

    public String getWordRU() {
        return wordRU;
    }

    public void setWordRU(String wordRU) {
        this.wordRU = wordRU;
    }

    public String getEnteredTranslation() {
        return enteredTranslation;
    }

    public void setEnteredTranslation(String enteredTranslation) {
        this.enteredTranslation = enteredTranslation;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getWordId() {
        return wordId;
    }

    public boolean isNeedToShow() {
        return needToShow;
    }

    public void setNeedToShow(boolean needToShow) {
        this.needToShow = needToShow;
    }

    public String getSeparatorText() {
        return separatorText;
    }

}
