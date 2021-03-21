package com.abc.qwert.slovicky;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.abc.qwert.slovicky.databinding.ActivityMainBinding;
import com.antonyt.infiniteviewpager.InfinitePagerAdapter;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.florent37.runtimepermission.RuntimePermission;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.skydoves.balloon.ArrowOrientation;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.OnBalloonDismissListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    static String appPath;

    ActivityMainBinding binding;

    RelativeLayout mainRelativeLayout;

    static boolean firstCreation;

    static final int languageCZ = 0;
    static final int languageRU = 1;

    public static int selectedLanguage = -1;
    private File selectedLanguageFile;

    static int selectedGroup;
    static File selectedGroupFile;

    static ArrayList<GroupData> groupDataList;


    private float viewPagerStartMoveX;
    private float viewPagerAllMovedPassX = 0;
    private float viewPagerStartMoveY;


    static boolean enableVibrations = true;
    static boolean enableAnimations = true;
    private static File settingsFile;

    static int wordsFontSize = 32;

    static NotificationTime notificationTime;

    static ArrayList<WordsListNotificationData> wordsListNotificationData;


    static boolean showTooltips = true;






    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        appPath = this.getFilesDir() + "/";

        mainRelativeLayout = binding.rootLayout;


        filesSetup();


        changeLanguage();


        // setup empty group
        binding.emptyGroup.setText(Html.fromHtml(
                "<font color=#FFFFFF>" +
                getResources().getString(R.string.empty_group) + "</font> <font color=#F6C7DC> " +
                getResources().getString(R.string.add) + "</font>"));
        binding.emptyGroup.setOnClickListener(view -> {
            if (groupDataList.size() == 1) {
                addNewGroup();
            } else {
                binding.edit.performClick();
            }
        });



        // setup settings
        binding.settings.setOnClickListener(view -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this, R.style.BottomSheetDialogTheme);
            View bottomSheetView = LayoutInflater.from(MainActivity.this).inflate(
                    R.layout.settings_content, findViewById(R.id.settings_content_container));

            // setup switch vibrations
            SwitchMaterial switchVibrations = bottomSheetView.findViewById(R.id.vibrations);
            switchVibrations.setChecked(enableVibrations);
            switchVibrations.setOnCheckedChangeListener((compoundButton, b) -> {
                enableVibrations = b;
                saveSettings();
            });

            // setup switch animations
            SwitchMaterial switchAnimations = bottomSheetView.findViewById(R.id.animations);
            switchAnimations.setChecked(enableAnimations);
            switchAnimations.setOnCheckedChangeListener((compoundButton, b) -> {
                enableAnimations = b;
                saveSettings();
            });


            // setup theme spinner
             ArrayAdapter<String> adapter = new ArrayAdapter<>(
                     this, R.layout.theme_spinner_item, getResources().getStringArray(R.array.app_themes));
            Spinner spinner = bottomSheetView.findViewById(R.id.select_theme);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()  {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (i != 0)
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.in_future_update), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });


            // setup font size
            TextView textSizeLabel = bottomSheetView.findViewById(R.id.text_size_label);
            textSizeLabel.setText(getResources().getString(R.string.text_size) + " " + wordsFontSize);
            SeekBar fontSize = bottomSheetView.findViewById(R.id.font_size);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                fontSize.setMin(20);
            }

            fontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    textSizeLabel.setText(getResources().getString(R.string.text_size) + " " + i);
//                    ((TextView) bottomSheetView.findViewById(R.id.settings)).setTextSize(i);
                    wordsFontSize = i;
                    saveSettings();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }

            });
            fontSize.setProgress(wordsFontSize);


            // setup notifications
            bottomSheetView.findViewById(R.id.notifications).setOnClickListener(view1 -> {
                groupDataList.remove(0);
                createAllWordsGroup();
                Intent intent = new Intent(MainActivity.this, NotificationsActivity.class);
                startActivity(intent);
            });


            // setup import words
            bottomSheetView.findViewById(R.id.import_group).setOnClickListener(view1 -> {
                Intent intent = new Intent(MainActivity.this, FileSystemActivity.class);
                startActivity(intent);
            });

            // setup export words
            bottomSheetView.findViewById(R.id.export_group).setOnClickListener(view1 -> {
                saveZip(bottomSheetDialog, bottomSheetView);
            });

            ((TextView) bottomSheetView.findViewById(R.id.group_export_name)).setText(
                    groupDataList.get(selectedGroup).getGroupName().isEmpty() ?
                            getResources().getString(R.string.no_name_group_name) :
                            groupDataList.get(selectedGroup).getGroupName());



            // setup dismiss
            bottomSheetDialog.setOnDismissListener(dialogInterface -> {

            });


            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();


            new Handler().postDelayed(() -> {
                showTooltip(
                        bottomSheetView.findViewById(R.id.notifications),
                        getTipText(R.string.notifications_tip),
                        TooltipOrientation.TOP,
                        ArrowOrientation.BOTTOM,
                        () -> {
                            bottomSheetView.findViewById(R.id.notifications).callOnClick();
                        }

                );

            }, 100);

        });


        // setup edit words
        binding.edit.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this, R.style.BottomSheetDialogTheme);
            View bottomSheetView = LayoutInflater.from(MainActivity.this).inflate(
                    R.layout.edit_words_content, findViewById(R.id.edit_words_content_container));

            bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

            // setup group name
            EditText groupName = bottomSheetView.findViewById(R.id.group_name);
            groupName.setText(groupDataList.get(selectedGroup).getGroupName());
            groupName.clearFocus();
            if(selectedGroup != 0){
                groupName.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        if(groupName.getText().toString().isEmpty()) groupName.setHint(getResources().getString(R.string.enter_group_name_hint));
                        else groupName.setHint("");

                        File file = new File(
                                appPath + "slovicky/groups/" + groupDataList.get(selectedGroup).getGroupId() + "/",
                                "groupName.txt");
                        writeToFile(file, false, new String[]{groupName.getText().toString()});

                        groupDataList.get(selectedGroup).setGroupName(groupName.getText().toString());
                        selectGroup(selectedGroup);

                    }

                });

            } else {
                groupName.setInputType(InputType.TYPE_NULL);
            }


            // setup words count
            int wordsCount = groupDataList.get(selectedGroup).getWordDataList().size();
            String w = (wordsCount == 1 ? getResources().getString(R.string.word) : getResources().getString(R.string.words));
            ((TextView) bottomSheetView.findViewById(R.id.words_count)).setText(wordsCount + " " + w);


            // setup words in group
            ArrayList<WordData> wordData = new ArrayList<>();
            ArrayList<WordData> visibleWords = new ArrayList<>();
            ArrayList<WordData> invisibleWords = new ArrayList<>();
            for (WordData word : groupDataList.get(selectedGroup).getWordDataList()) {
                if (!word.isNeedToShow()) invisibleWords.add(word);
                else visibleWords.add(word);
            }
            wordData.addAll(visibleWords);
            wordData.addAll(invisibleWords);

            RecyclerView editWordsRecyclerView = bottomSheetView.findViewById(R.id.edit_words_recycler_view);

            EditWordsAdapter editWordsAdapter = new EditWordsAdapter(
                    wordData, visibleWords.size(),
                    MainActivity.this, groupDataList.get(selectedGroup).getGroupId()) {

                @Override
                public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                    if (holder instanceof EditWordsViewHolder) {
                        EditWordsViewHolder viewHolder = (EditWordsViewHolder) holder;

                        viewHolder.getEnterWordCZ().setText(wordDataList.get(position).getWordCZ());
                        viewHolder.getEnterWordCZ().setOnFocusChangeListener((view, b) -> {
                            if (selectedGroup != 0){
                                if(b){
                                    // focused
                                    viewHolder.getDelete().animate().alpha(1).setDuration(150).setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationStart(Animator animation) {
                                            super.onAnimationStart(animation);
                                            viewHolder.getDelete().setVisibility(View.VISIBLE);
                                        }
                                    }).start();
                                } else {
                                    // lost focus
                                    viewHolder.getDelete().animate().alpha(0).setDuration(150).setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            super.onAnimationEnd(animation);
                                            viewHolder.getDelete().setVisibility(View.INVISIBLE);
                                        }
                                    }).start();
                                }
                            }

                        });
                        viewHolder.getEnterWordCZ().addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                            @Override
                            public void afterTextChanged(Editable editable) {
                                wordDataList.get(holder.getAdapterPosition()).setWordCZ(viewHolder.getEnterWordCZ().getText().toString());
                                saveWordDataToFiles(holder.getAdapterPosition());
                            }

                        });
                        viewHolder.getEnterWordCZ().clearFocus();

                        viewHolder.getEnterWordRU().setText(wordDataList.get(position).getWordRU());
                        viewHolder.getEnterWordRU().setOnFocusChangeListener((view, b) -> {
                            if(selectedGroup != 0){
                                if(b){
                                    // focused
                                    viewHolder.getDelete().animate().alpha(1).setDuration(150).setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationStart(Animator animation) {
                                            super.onAnimationStart(animation);
                                            viewHolder.getDelete().setVisibility(View.VISIBLE);
                                        }
                                    }).start();
                                } else {
                                    // lost focus
                                    viewHolder.getDelete().animate().alpha(0).setDuration(150).setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            super.onAnimationEnd(animation);
                                            viewHolder.getDelete().setVisibility(View.INVISIBLE);
                                        }
                                    }).start();
                                }
                            }


                        });
                        viewHolder.getEnterWordRU().addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                            @Override
                            public void afterTextChanged(Editable editable) {
                                wordDataList.get(holder.getAdapterPosition()).setWordRU(viewHolder.getEnterWordRU().getText().toString());
                                saveWordDataToFiles(holder.getAdapterPosition());
                            }
                        });
                        viewHolder.getEnterWordRU().setOnKeyListener((v1, keyCode, event) -> {
                            if(viewHolder.getEnterWordRU().getText().toString().isEmpty() && keyCode == KeyEvent.KEYCODE_DEL) {
                                viewHolder.getEnterWordCZ().requestFocus();
                            }
                            return false;
                        });
                        viewHolder.getEnterWordRU().clearFocus();

                        isBinding = true;
                        viewHolder.getNeedToShow().setChecked(wordDataList.get(position).isNeedToShow());
                        isBinding = false;

                        viewHolder.getNeedToShow().setOnCheckedChangeListener((compoundButton, b) -> {
                            if (!isBinding) {
                                // b is a new value

                                wordDataList.get(holder.getAdapterPosition()).setNeedToShow(b);

                                if (b) {
                                    String id = wordDataList.get(holder.getAdapterPosition()).getWordId();
                                    int movingPosition = getMovingPosition(id);
                                    groupDataList.get(selectedGroup).getWordDataList().get(movingPosition).setNeedToShow(b);
                                    saveWordDataToFiles(holder.getAdapterPosition());
                                    WordData movingItem = wordDataList.remove(holder.getAdapterPosition());

                                    wordDataList.add(separatorPosition, movingItem);
                                    notifyItemMoved(holder.getAdapterPosition(), separatorPosition);
                                    if (separatorPosition == 0) editWordsRecyclerView.scrollToPosition(0);
                                    separatorPosition++;

                                } else {
                                    groupDataList.get(selectedGroup).getWordDataList().get(holder.getAdapterPosition()).setNeedToShow(b);
                                    saveWordDataToFiles(holder.getAdapterPosition());
                                    WordData movingItem = wordDataList.remove(holder.getAdapterPosition());

                                    wordDataList.add(separatorPosition, movingItem);
                                    notifyItemMoved(holder.getAdapterPosition(), separatorPosition);
                                    if (holder.getAdapterPosition() == 0) editWordsRecyclerView.scrollToPosition(0);
                                    separatorPosition--;
                                }

                            }

                        });

                        viewHolder.getDelete().setOnClickListener(v -> {
                            deleteItem(position);

                            ArrayList<WordData> wordData = new ArrayList<>();
                            wordData.addAll(wordDataList);
                            wordData.remove(wordData.size() - 1);
                            groupDataList.get(selectedGroup).setWordDataList(wordData);


                            int wordsCount = wordData.size();
                            String w = (wordsCount == 1 ? getResources().getString(R.string.word) : getResources().getString(R.string.words));
                            ((TextView) bottomSheetView.findViewById(R.id.words_count)).setText(wordsCount + " " + w);

                        });

                        if(selectedGroup == 0) {
                            viewHolder.getEnterWordCZ().setInputType(InputType.TYPE_NULL);
                            viewHolder.getEnterWordRU().setInputType(InputType.TYPE_NULL);

                            viewHolder.getDelete().setOnClickListener(null);
                        }

                    } else {
                        EditWordsSeparateViewHolder viewHolder = (EditWordsSeparateViewHolder) holder;
                        viewHolder.getSeparatorText().setText(wordDataList.get(position).getSeparatorText());

                        viewHolder.getAddNewWord().setOnClickListener(view -> {
                            AutoTransition transition = new AutoTransition();
                            transition.addTarget(bottomSheetView);
                            transition.setInterpolator(new FastOutSlowInInterpolator());
                            transition.setDuration(600);
                            TransitionManager.beginDelayedTransition(binding.mainContextView, transition);


                            addItem();

                            ArrayList<WordData> wordData = new ArrayList<>();
                            wordData.addAll(wordDataList);
                            wordData.remove(wordData.size() - 1);
                            groupDataList.get(selectedGroup).setWordDataList(wordData);


                            int wordsCount = groupDataList.get(selectedGroup).getWordDataList().size();
                            String w = (wordsCount == 1 ? getResources().getString(R.string.word) : getResources().getString(R.string.words));
                            ((TextView) bottomSheetView.findViewById(R.id.words_count)).setText(wordsCount + " " + w);


                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                TransitionManager.endTransitions((ViewGroup) bottomSheetView);
                            }

                            new Handler().post(() -> {
                                editWordsRecyclerView.smoothScrollToPosition(separatorPosition);
                                EditWordsViewHolder lastHolder = ((EditWordsViewHolder) editWordsRecyclerView.findViewHolderForAdapterPosition(separatorPosition - 1));
                                if (lastHolder != null) {
                                    lastHolder.getEnterWordCZ().requestFocus();
                                }

                            });

                        });

                        if (selectedGroup == 0) viewHolder.getAddNewWord().setVisibility(View.GONE);

                        separatorPosition = position;

                        showTooltip(
                                viewHolder.getAddNewWord().findViewById(R.id.add_new_word_text),
                                getTipText(R.string.add_first_word),
                                TooltipOrientation.TOP,
                                ArrowOrientation.BOTTOM,
                                () -> {
                                    viewHolder.getAddNewWord().callOnClick();
                                    bottomSheetDialog.cancel();

                                    new Handler().postDelayed(() -> {
                                        showTooltip(
                                                binding.mixWords,
                                                getTipText(R.string.mix_words),
                                                TooltipOrientation.TOP,
                                                ArrowOrientation.BOTTOM,
                                                () -> {
                                                    new Handler().postDelayed(() -> {
                                                        showTooltip(
                                                                binding.changeLanguage,
                                                                getTipText(R.string.change_language),
                                                                TooltipOrientation.LEFT,
                                                                ArrowOrientation.RIGHT,
                                                                () -> {
                                                                    new Handler().postDelayed(() -> {
                                                                        showTooltip(
                                                                                binding.settings,
                                                                                getTipText(R.string.settings_tip),
                                                                                TooltipOrientation.LEFT,
                                                                                ArrowOrientation.RIGHT,
                                                                                () -> {
                                                                                    binding.settings.callOnClick();
                                                                                }

                                                                        );

                                                                    }, 100);
                                                                }

                                                        );

                                                    }, 100);
                                                }

                                        );

                                    }, 100);

                                });

                    }

                }

            };

            editWordsRecyclerView.setAdapter(editWordsAdapter);
            editWordsRecyclerView.setLayoutManager(
                    new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false));

            bottomSheetDialog.setOnDismissListener(dialogInterface -> {
                setupViewPager();
            });

            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();

        });


        // setup change language
        binding.changeLanguage.setOnClickListener(view -> {
            changeLanguage();
        });


        // setup select group
        binding.selectGroupContainer.setOnClickListener(view -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this, R.style.BottomSheetDialogTheme);
            View bottomSheetView = LayoutInflater.from(MainActivity.this).inflate(
                    R.layout.select_group_content, findViewById(R.id.select_group_content_container));

            groupDataList.remove(0);
            createAllWordsGroup();

            int groupsCount = groupDataList.size() - 1;
            String w = (groupsCount == 1 ? getResources().getString(R.string.group) : getResources().getString(R.string.groups));
            ((TextView) bottomSheetView.findViewById(R.id.groups_count)).setText(groupsCount + " " + w);

            new Handler().post(() -> {

                SelectGroupAdapter selectGroupAdapter = new SelectGroupAdapter(this, groupDataList) {

                    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
                    @Override
                    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                        if(holder instanceof SelectGroupViewHolder) {
                            SelectGroupViewHolder viewHolder = (SelectGroupViewHolder) holder;

                            // setup group name
                            viewHolder.getGroupName().setText(groupDataList.get(position).getGroupName().isEmpty() ?
                                    getResources().getString(R.string.no_name_group_name) : groupDataList.get(position).getGroupName());

                            // setup words count
                            int itemCount = groupDataList.get(position).getWordDataList().size();
                            String w;
                            w = (itemCount == 1 ? getResources().getString(R.string.word) : getResources().getString(R.string.words));
                            viewHolder.getWordsCount().setText(itemCount + " " + w);


                            // setup select group
                            viewHolder.itemView.findViewById(R.id.select_group_item_content).setOnClickListener(view1 -> {
                                selectGroup(position);
                                bottomSheetDialog.cancel();
                            });



                            // setup edit group
                            viewHolder.getEditGroup().setOnClickListener(view -> {
                                if(position != 0) {
                                    bottomSheetDialog.cancel();
                                    selectGroup(position);
                                    binding.edit.performClick();
                                }
                            });

                            if(position == selectedGroup) viewHolder.getEditGroup().setBackground(getResources().getDrawable(R.drawable.main_transparent_background));
                            else viewHolder.getEditGroup().setBackground(null);

                            if(position == 0) {
                                viewHolder.getEditGroup().setImageResource(R.drawable.ic_article);
                                viewHolder.getEditGroup().setOnClickListener(null);
                            }


                            // setup delete group
                            viewHolder.getDeleteGroup().setOnClickListener(view -> {
                                if(position != 0) {
                                    deleteGroup(position);

                                    groupDataList.remove(position);

                                    int groupsCount = groupDataList.size() - 1;
                                    String ww = (groupsCount == 1 ? getResources().getString(R.string.group) : getResources().getString(R.string.groups));
                                    ((TextView) bottomSheetView.findViewById(R.id.groups_count)).setText(groupsCount + " " + ww);

                                    groupDataList.remove(0);
                                    createAllWordsGroup();

                                    selectGroup(selectedGroup >= groupDataList.size() ? selectedGroup - 1 : selectedGroup);

                                    notifyDataSetChanged();

                                }
                            });

                            if(position == 0) {
                                viewHolder.getDeleteGroup().setVisibility(View.INVISIBLE);
                            }

                        } else {
                            AddNewGroupViewHolder viewHolder = (AddNewGroupViewHolder) holder;

                            viewHolder.itemView.setOnClickListener(view1 -> {
                                bottomSheetDialog.cancel();
                                addNewGroup();

                            });

                        }

                    }

                };

                RecyclerView selectGroupRecyclerView = bottomSheetView.findViewById(R.id.select_group_recycler_view);
                selectGroupRecyclerView.setAdapter(selectGroupAdapter);
                selectGroupRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false));

            });

            bottomSheetDialog.setOnDismissListener(dialogInterface -> {
                setupViewPager();
            });

            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();

        });



        // setup view pager
        setupViewPager();


        // setup swiper
        binding.swiper.setOnTouchListener((view, motionEvent) -> {
            int[] viewPagerCoordinates = new int[2];
            binding.viewPager.getLocationOnScreen(viewPagerCoordinates);

            switch (motionEvent.getAction()){
                case MotionEvent.ACTION_DOWN:
                    viewPagerStartMoveX = motionEvent.getRawX();
                    viewPagerStartMoveY = motionEvent.getRawY();

                    return false;

                case MotionEvent.ACTION_MOVE:
                    float dx = motionEvent.getRawX() - viewPagerStartMoveX;
                    float dy = motionEvent.getRawY() - viewPagerStartMoveY;

                    viewPagerAllMovedPassX += dx;

                    viewPagerStartMoveX = motionEvent.getRawX();
                    viewPagerStartMoveY = motionEvent.getRawY();

                    binding.swiper
                            .animate()
                            .translationXBy(dx)
                            .translationYBy(dy)
                            .setDuration(0)
                            .start();
                    return false;

                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if (Math.abs(viewPagerAllMovedPassX) > 50) {
                        if(viewPagerAllMovedPassX > 0) {
                            // swipe ltr
                            new Thread(() -> {
                                movePage(true, 100);
                            }).start();

                        } else if(viewPagerAllMovedPassX < 0) {
                            // swipe rtl
                            new Thread(() -> {
                                movePage(false, 100);
                            }).start();

                        }
                    }

                    viewPagerAllMovedPassX = 0;

                    new Handler().post(() -> {
                        binding.swiper
                                .animate()
                                .translationX(0)
                                .translationY(0)
                                .setDuration(500)
                                .setInterpolator(new LinearOutSlowInInterpolator())
                                .start();
                    });

                    return false;

                default:
                    return false;
            }

        });


        // setup mix words
        binding.mixWords.setOnClickListener(view -> {
            ArrayList<WordData> currentList = groupDataList.get(selectedGroup).getWordDataList();
            ArrayList<WordData> mixedList = new ArrayList<>();
            while (currentList.size() > 0) {
                mixedList.add(currentList.remove(new Random().nextInt(currentList.size())));
            }
            groupDataList.get(selectedGroup).setWordDataList(mixedList);
            setupViewPager();
        });


        // hide logo
        binding.rootLayout
                .animate()
                .alpha(1)
                .setDuration(700)
                .setStartDelay(1000)
                .setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        new Handler().post(() -> {
                            showTooltip(
                                    binding.emptyGroup,
                                    getTipText(R.string.click_on_me),
                                    TooltipOrientation.BOTTOM,
                                    ArrowOrientation.TOP,
                                    () -> {
                                        binding.emptyGroup.performClick();
                                    });

                        });
                    }
                })
                .start();


    }


    private void movePage(boolean ltr, int time) {
        int[] viewPagerCoordinates = new int[2];
        binding.viewPager.getLocationOnScreen(viewPagerCoordinates);
        int fromX = viewPagerCoordinates[0];
        int fromY = viewPagerCoordinates[1] + binding.viewPager.getHeight() / 2;

        int steps = 20;
        int stepLength = binding.viewPager.getWidth() / steps;
        int stepTime = time / steps;

        if (!ltr) {
            fromX += binding.viewPager.getWidth() - 1;
            stepLength *= -1;
        }

        int x = fromX;

        Instrumentation instrumentation = new Instrumentation();

        MotionEvent motionEvent =
                MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, x, fromY, 0);
        try {
            instrumentation.sendPointerSync(motionEvent);
        } catch (SecurityException ignored) { }

        for (int i = 0; i < steps; i++) {
            motionEvent =
                    MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + stepTime,
                            MotionEvent.ACTION_MOVE, x, fromY, 0);
            try {
                instrumentation.sendPointerSync(motionEvent);
            } catch (SecurityException ignored) { }

            x += stepLength;

        }

        motionEvent =
                MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, x, fromY, 0);
        try {
            instrumentation.sendPointerSync(motionEvent);
        } catch (SecurityException ignored) { }

        motionEvent.recycle();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        selectedLanguage = -1;
    }

    private void addNewGroup() {
        String id = "" + System.currentTimeMillis();

        createFolder(appPath + "slovicky/groups/", id + "");
        createFile(appPath + "slovicky/groups/" + id + "/", "groupName.txt");

        groupDataList.add(groupDataList.size(), new GroupData("", id, new ArrayList<>()));

        selectGroup(groupDataList.size() - 1);
        binding.edit.performClick();

    }

    private void createAllWordsGroup(){
        ArrayList<WordData> allWords = new ArrayList<>();
        for(GroupData groupData : groupDataList) {
            for(WordData wordData : groupData.getWordDataList()){
                if(wordData.getSeparatorText().isEmpty()){
                    allWords.add(wordData);
                }
            }

        }

        groupDataList.add(0, new GroupData(getResources().getString(R.string.all_word_group_name), "", allWords));

    }

    private void setupViewPager(int selectedPosition) {
        binding.viewPager
                .animate()
                .alpha(0)
                .setDuration(200)
                .setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);

                        if(groupDataList.get(selectedGroup).getWordDataList().size() == 0 || !checkOnVisibility()) {
                            binding.emptyGroup.setVisibility(View.VISIBLE);
                        } else {
                            binding.emptyGroup.setVisibility(View.INVISIBLE);
                        }

                        ArrayList<WordData> wordData = new ArrayList<>();
                        wordData.addAll(groupDataList.get(selectedGroup).getWordDataList());

                        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(MainActivity.this, wordData) {

                            @SuppressLint("ClickableViewAccessibility")
                            @NonNull
                            @Override
                            public Object instantiateItem(@NonNull ViewGroup container, int currentPosition) {

                                LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                View itemView = inflater.inflate(R.layout.learning_card, container, false);

                                TextView mainWord;
                                TextView translation;
                                EditText enterTranslation;
                                ImageView sound;
                                ImageView hide;
                                ImageView info;
                                ImageView done;



                                // setup Main word
                                mainWord = itemView.findViewById(R.id.main_word);
                                String word = (MainActivity.selectedLanguage == MainActivity.languageCZ ?
                                        wordDataList.get(currentPosition).getWordCZ() : wordDataList.get(currentPosition).getWordRU());
                                mainWord.setText(word.isEmpty() ? getResources().getString(R.string.no_word) : word);

                                //setup translation
                                translation = itemView.findViewById(R.id.translation);
                                String wordTranslation = (MainActivity.selectedLanguage == MainActivity.languageRU ?
                                        wordDataList.get(currentPosition).getWordCZ() : wordDataList.get(currentPosition).getWordRU());
                                translation.setText(
                                        wordTranslation.isEmpty() ?
                                                getResources().getString(R.string.no_translation) :
                                                wordTranslation
                                );


                                mainWord.post(() -> {
                                    mainWord.setTranslationY((itemView.findViewById(R.id.text_container).getHeight() - mainWord.getHeight()) / 3f);
                                });


                                //setup enter translation
                                enterTranslation = itemView.findViewById(R.id.enter_translation);
                                enterTranslation.setText(wordDataList.get(currentPosition).getEnteredTranslation());
                                enterTranslation.addTextChangedListener(new TextWatcher() {
                                    @Override
                                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                    }

                                    @Override
                                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                        if(!enterTranslation.getText().toString().isEmpty()){
                                            wordDataList.get(currentPosition).setEnteredTranslation(enterTranslation.getText().toString());
                                        } else {
                                            wordDataList.get(currentPosition).setEnteredTranslation("");
                                        }
                                    }

                                    @Override
                                    public void afterTextChanged(Editable editable) { }

                                });


                                // setup sound button
                                sound = itemView.findViewById(R.id.sound);
                                sound.setOnClickListener(view -> {
                                    Toast.makeText(MainActivity.this, getResources().getString(R.string.in_future_update), Toast.LENGTH_LONG).show();
                                });


                                // setup hide button
                                hide = itemView.findViewById(R.id.hide);
                                hide.setOnClickListener(v -> {
                                    ArrayList<WordData> groupWords = groupDataList.get(selectedGroup).getWordDataList();
                                    for (int i = 0; i < groupWords.size(); i++) {
                                        if (groupWords.get(i).getWordId().equals(wordDataList.get(currentPosition).getWordId())) {
                                            groupDataList.get(selectedGroup).getWordDataList().get(i).setNeedToShow(false);

                                            File file = createFile(
                                                    appPath +
                                                            "slovicky/groups/" +
                                                            wordDataList.get(currentPosition).getGroupId() + "/",
                                                    wordDataList.get(currentPosition).getWordId() + ".txt");

                                            writeToFile(file, false,
                                                    new String[]{
                                                            wordDataList.get(currentPosition).getWordCZ(),
                                                            wordDataList.get(currentPosition).getWordRU(),
                                                            "false"
                                                    });
                                        }
                                    }

                                    setupViewPager(currentPosition);

                                });


                                //setup info
                                info = itemView.findViewById(R.id.info);
                                info.setOnTouchListener((v, event) -> {
                                    switch(event.getAction()) {
                                        case MotionEvent.ACTION_DOWN:
//                                            mainWord.setText(wordTranslation);
                                            mainWord.setText(wordTranslation.isEmpty() ?
                                                    getResources().getString(R.string.no_translation) :
                                                    wordTranslation);

                                            break;
                                        case MotionEvent.ACTION_CANCEL:
                                        case MotionEvent.ACTION_UP:
//                                            mainWord.setText(word);

                                            mainWord.setText(word.isEmpty() ? getResources().getString(R.string.no_word) : word);

                                            break;
                                    }
                                    return true;
                                });


                                // setup done button
                                done = itemView.findViewById(R.id.done);
                                done.setOnClickListener(v -> {

                                    String t;
                                    if (MainActivity.selectedLanguage == MainActivity.languageRU) {
                                        t = wordDataList.get(currentPosition).getWordCZ();
                                    } else {
                                        t = wordDataList.get(currentPosition).getWordRU();
                                    }

                                    if(wordDataList.get(currentPosition).getEnteredTranslation().equalsIgnoreCase(t)) {
                                        // correct answer

                                        if(MainActivity.enableAnimations){
                                            new Handler().post(() -> YoYo.with(Techniques.Pulse)
                                                    .duration(700)
                                                    .repeat(0)
                                                    .playOn(container));
                                        }



                                        vibrate(1);
                                        wordDataList.get(currentPosition).setEnteredTranslation("");
                                        enterTranslation.setText("");

                                        if(translation.getAlpha() == 0){
                                            mainWord.animate()
//                                                    .translationY((itemView.findViewById(R.id.text_container).getHeight() - mainWord.getHeight() - translation.getHeight()) / 2f)
                                                    .translationY((itemView.findViewById(R.id.text_container).getHeight() - mainWord.getHeight() - translation.getHeight()))
                                                    .setDuration(700)
                                                    .setInterpolator(new FastOutSlowInInterpolator())
                                                    .setUpdateListener(valueAnimator -> {
                                                        translation.setAlpha((Float) valueAnimator.getAnimatedValue());
//                                                        translation.setTranslationY(mainWord.getBottom() + mainWord.getHeight());
                                                    }).start();

                                        }

                                        int colorFrom = enterTranslation.getBackgroundTintList().getDefaultColor();
                                        int colorTo = MainActivity.this.getResources().getColor(R.color.green);
                                        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                                        colorAnimation.setDuration(700); // milliseconds
                                        colorAnimation.addUpdateListener(animator -> {
                                            enterTranslation.setBackgroundTintList(ColorStateList.valueOf((int) animator.getAnimatedValue()));
                                        });
                                        colorAnimation.start();

                                    } else {
                                        // wrong answer

                                        if (MainActivity.enableAnimations){
                                            new Handler().post(() -> YoYo.with(Techniques.Wobble)
                                                    .duration(1000)
                                                    .repeat(0)
                                                    .playOn(container));
                                        }



                                        vibrate(1);
                                        wordDataList.get(currentPosition).setEnteredTranslation("");
                                        enterTranslation.setText("");

                                        if(translation.getAlpha() == 0) {
                                            mainWord.animate()
                                                    .translationY((itemView.findViewById(R.id.text_container).getHeight() - mainWord.getHeight() - translation.getHeight()))
                                                    .setDuration(500)
                                                    .setInterpolator(new FastOutSlowInInterpolator())
                                                    .setUpdateListener(valueAnimator -> {
                                                        translation.setAlpha((Float) valueAnimator.getAnimatedValue());
//                                                        translation.setTranslationY(mainWord.getBottom() + mainWord.getHeight());
                                                    }).start();


                                        }

                                        int colorFrom = enterTranslation.getBackgroundTintList().getDefaultColor();//context.getResources().getColor(R.color.purple_500);
                                        int colorTo = MainActivity.this.getResources().getColor(R.color.red);
                                        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                                        colorAnimation.setDuration(500); // milliseconds
                                        colorAnimation.addUpdateListener(animator -> {
                                            enterTranslation.setBackgroundTintList(ColorStateList.valueOf((int) animator.getAnimatedValue()));
                                        });
                                        colorAnimation.start();

                                    }

                                });


                                container.addView(itemView);

                                return itemView;
                            }

                        };


                        binding.viewPager.setPaddingRelative(
                                0, getResources().getDimensionPixelOffset(R.dimen.view_pager_margin),
                                0, getResources().getDimensionPixelOffset(R.dimen.view_pager_margin));
                        binding.viewPager.setPageTransformer(true,
                                new CardsPagerTransformerBasic(getResources().getDimensionPixelOffset(R.dimen.distance_between_cards)));
                        binding.viewPager.setOffscreenPageLimit(3);

                        PagerAdapter wrappedAdapter = new InfinitePagerAdapter(viewPagerAdapter);
                        binding.viewPager.setAdapter(wrappedAdapter);
                        binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                            @Override
                            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

                            @Override
                            public void onPageSelected(int position) { }

                            @Override
                            public void onPageScrollStateChanged(int state) {
                                vibrate(1);

                            }
                        });


                        binding.viewPager.setCurrentItem(selectedPosition);


                        binding.viewPager
                                .animate()
                                .alpha(1)
                                .setDuration(300)
                                .setInterpolator(new LinearOutSlowInInterpolator())
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {
                                        super.onAnimationStart(animation);
                                    }
                                })
                                .start();

                    }
                })
                .start();

    }

    private void setupViewPager(){
        setupViewPager(0);
    }

    private boolean checkOnVisibility() {
        for (WordData wordData : groupDataList.get(selectedGroup).getWordDataList()) {
            if (wordData.isNeedToShow()) return true;
        }

        return false;
    }

    private int getMovingPosition(String id) {
        ArrayList<WordData> wordData = groupDataList.get(selectedGroup).getWordDataList();
        for (int i = 0; i < wordData.size(); i++) {
            if (wordData.get(i).getWordId().equals(id)) return i;
        }

        return 0;
    }

    static void saveGroupToMemory(GroupData groupData) {
        createFolder(appPath + "slovicky/groups/", groupData.getGroupId() + "");
        File groupName = createFile(appPath + "slovicky/groups/" + groupData.getGroupId() + "/", "groupName.txt");
        writeToFile(groupName, false, new String[]{groupData.getGroupName()});

        for (WordData wordData : groupData.getWordDataList()) {
            File file = createFile(appPath + "slovicky/groups/" + groupData.getGroupId() + "/", wordData.getWordId() + ".txt");
            writeToFile(file, false,
                    new String[]{
                            wordData.getWordCZ(),
                            wordData.getWordRU(),
                            wordData.isNeedToShow() + ""
                    });
        }

    }

    void selectGroup(int groupNumber){
        selectedGroup = groupNumber;

        binding.selectGroup.setText(groupDataList.get(selectedGroup).getGroupName().isEmpty() ?
                getResources().getString(R.string.no_name_group_name) : groupDataList.get(selectedGroup).getGroupName());

        writeToFile(selectedGroupFile, false, new String[]{selectedGroup + ""});

    }

    @SuppressLint("SetTextI18n")
    private void changeLanguage(){
        if(selectedLanguage == -1){
            if(selectedLanguageFile.length() == 0){
                selectedLanguage = languageCZ;
                writeToFile(selectedLanguageFile, false, new String[]{selectedLanguage + ""});
            } else {
                selectedLanguage = Integer.parseInt(readFromFile(selectedLanguageFile).get(0));
            }

        } else {
            if (selectedLanguage == languageCZ){
                selectedLanguage = languageRU;
            } else {
                selectedLanguage = languageCZ;
            }

            writeToFile(selectedLanguageFile, false, new String[]{selectedLanguage + ""});

            setupViewPager(binding.viewPager.getCurrentItem());

        }

        if(selectedLanguage == languageCZ){
            binding.changeLanguage.setText(new String(Character.toChars(0x1F1E8)) + new String(Character.toChars(0x1F1FF)));
        } else {
            binding.changeLanguage.setText(new String(Character.toChars(0x1F1F7)) + new String(Character.toChars(0x1F1FA)));
        }

    }

    void vibrate(int time){
        if(enableVibrations){
            Vibrator vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            if(vibrator != null){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(time, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(time);
                }
            }
        }

    }



    private void filesSetup(){
        firstCreation = createFolder(appPath, "slovicky");

        if(firstCreation){
            createFolder(appPath + "slovicky/", "groups");
            selectedGroupFile = createFile(appPath + "slovicky/", "lastSelectedGroup.txt");

            loadDataFromFiles();

            selectGroup(0);

            selectedLanguageFile = createFile(appPath + "slovicky/", "selectedLanguage.txt");

            settingsFile = createFile(appPath + "slovicky/", "settings.txt");
            saveSettings();

            notificationTime = new NotificationTime(7, 0, 23, 0, 40);

            createFolder(appPath + "slovicky/", "notifications");
            createFile(appPath + "slovicky/notifications/", "detailData.txt");
            createFile(appPath + "slovicky/notifications/", "timeAuto.txt");
            createFile(appPath + "slovicky/notifications/", "timeManual.txt");
            createFile(appPath + "slovicky/notifications/", "wordsToSend.txt");
            File notificationId = createFile(appPath + "slovicky/notifications/", "notificationId.txt");
            writeToFile(notificationId, false, new String[] {"0"});

            notificationTime.saveDetailData();
            notificationTime.saveTimeAuto();
            wordsListNotificationData = new ArrayList<>();

        } else {

            selectedGroupFile = createFile(appPath + "slovicky/", "lastSelectedGroup.txt");

            loadDataFromFiles();

            selectGroup(Integer.parseInt(readFromFile(selectedGroupFile).get(0)));

            selectedLanguageFile = createFile(appPath + "slovicky/", "selectedLanguage.txt");

            settingsFile = createFile(appPath + "slovicky/", "settings.txt");
            readFromSettings();

            notificationTime = new NotificationTime();
            wordsListNotificationData = new ArrayList<>();
            readWordsToSend();

            readDetailData();
            readTimeAuto();
            readTimeManual();

        }

    }

     private void loadDataFromFiles(){

        groupDataList = new ArrayList<>();

        File groups = new File(appPath + "slovicky/groups/");
        File[] groupsFiles = groups.listFiles();
        if(groupsFiles != null && groupsFiles.length != 0){

            for(File groupNameFile : groupsFiles){
                String groupName = "";
                ArrayList<String> arrayList = readFromFile(new File(groupNameFile.getPath(), "groupName.txt"));
                if(arrayList.size() != 0) groupName = arrayList.get(0);

                String groupId = groupNameFile.getName();

                ArrayList<WordData> wordDataList = new ArrayList<>();
                File[] files = groupNameFile.listFiles();
                if(files != null && files.length != 0){
                    for(File word : files) {
                        if(!word.getName().equals("groupName.txt")){
                            ArrayList<String> wordData = readFromFile(word);

                            String wordCZ = wordData.get(0);

                            String wordRU = wordData.get(1);

                            boolean needToShow = Boolean.parseBoolean(wordData.get(2));

                            String wordId = word.getName();
                            wordId = wordId.substring(0, wordId.length() - 4);

                            wordDataList.add(new WordData(wordCZ, wordRU, "", groupId, wordId, needToShow, ""));

                        }
                    }
                }

                groupDataList.add(new GroupData(groupName, groupId, wordDataList));
            }

        }

        createAllWordsGroup();

    }

    private static boolean createFolder(String path, String name){
        File file = new File(path + name + "/");
        return file.mkdir();
    }

    private static File createFile(String path, String name){
        File file = new File(path + name);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    static void writeToFile(File file, boolean append, String[] lines){
        try {
            FileWriter fw = new FileWriter(file, append);
            for (String line : lines) fw.write(line + "\n");
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    static void saveSettings(){
        writeToFile(settingsFile, false, new String[]{
                enableVibrations + "\n" + enableAnimations + "\n" + wordsFontSize + "\n" + showTooltips
        });
    }

    private void readFromSettings(){
        ArrayList<String> settings = readFromFile(settingsFile);
        enableVibrations = Boolean.parseBoolean(settings.get(0));
        enableAnimations = Boolean.parseBoolean(settings.get(1));
        wordsFontSize = Integer.parseInt(settings.get(2));
        showTooltips = Boolean.parseBoolean(settings.get(3));
    }

    static void saveWordsToSend(ArrayList<WordsListNotificationData> words, boolean append) {
        File wordsToSend = new File(appPath + "slovicky/notifications/wordsToSend.txt");
        String[] output = new String[words.size()];
        for (int i = 0; i < words.size(); i++) {
            WordsListNotificationData word = words.get(i);
            output[i] =
                    word.groupId + " " +
                    word.wordId + " " +
                    word.needToSend;
        }
        writeToFile(wordsToSend, append, output);
    }



    private void readDetailData() {
        File detailData = new File(appPath + "slovicky/notifications/detailData.txt");
        ArrayList<String> data = readFromFile(detailData);
        notificationTime.fromHour = Integer.parseInt(data.get(0));
        notificationTime.fromMinutes = Integer.parseInt(data.get(1));
        notificationTime.toHour = Integer.parseInt(data.get(2));
        notificationTime.toMinutes = Integer.parseInt(data.get(3));
        notificationTime.everyMinutes = Integer.parseInt(data.get(4));
    }

    private void readTimeAuto() {
        File timeAuto = new File(appPath + "slovicky/notifications/timeAuto.txt");
        ArrayList<String> data = readFromFile(timeAuto);
        for (String line : data) {
            String[] split = line.split(":");
            notificationTime.addTimeAuto(new TimePair(split[0], split[1]));
        }
    }

    private void readTimeManual() {
        File timeManual = new File(appPath + "slovicky/notifications/timeManual.txt");
        ArrayList<String> data = readFromFile(timeManual);
        for (String line : data) {
            String[] split = line.split(":");
            notificationTime.addTimeManual(new TimePair(split[0], split[1]));
        }
    }

    static void readWordsToSend() {
        wordsListNotificationData = new ArrayList<>();
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

        if (data.size() != wordsListNotificationData.size()) {
            saveWordsToSend(wordsListNotificationData, false);
        }

    }

    private static String getWordWithTranslationById(String groupId, String wordId) {
        String wordWithTranslation = "";

        File word = new File(appPath + "slovicky/groups/" + groupId, wordId + ".txt");
        if (word.exists()) {
            ArrayList<String> wordData = readFromFile(word);
            wordWithTranslation = wordData.get(0) + " - " + wordData.get(1);
        }

        return wordWithTranslation;
    }



    private void saveZip(BottomSheetDialog bottomSheetDialog, View contentView){
        if (isStoragePermissionGranted()){
            String[] paths;

            ArrayList<String> pathsList;

            if (selectedGroup == 0) {
                pathsList = new ArrayList<>();
                for (int group = 1; group < groupDataList.size(); group++) {
                    final File folder = new File(appPath + "slovicky/groups/" +
                            groupDataList.get(group).getGroupId() + "/");

                    File[] files = folder.listFiles();
                    for (File file : files) {
                        if (!file.getAbsolutePath().contains("groupName.txt")) {
                            pathsList.add(file.getAbsolutePath());
                        }
                    }

                }

                paths = new String[pathsList.size()];
                for (int i = 0; i < pathsList.size(); i++) {
                    paths[i] = pathsList.get(i);
                }

            } else {
                final File folder = new File(appPath + "slovicky/groups/" +
                        groupDataList.get(selectedGroup).getGroupId() + "/");

                File[] files = folder.listFiles();
                paths = new String[files.length];
                for(int i = 0; i < files.length; i++) paths[i] = files[i].getAbsolutePath();

            }


            File save = new File(Environment.getExternalStorageDirectory().toString() + "/Slovíčky");
            if (!save.exists()) {
                save.mkdirs();
            }


            String zipName = groupDataList.get(selectedGroup).getGroupName();
            if (zipName.isEmpty()) zipName = getResources().getString(R.string.no_name_group_name);
            Compress compress = new Compress(paths, save.getAbsolutePath() + "/" + zipName + ".zip");
            compress.zip();


            Snackbar.make(bottomSheetDialog.getWindow().getDecorView(), getResources().getString(R.string.group_saved), Snackbar.LENGTH_LONG)
                    .setAnchorView(contentView)
                    .show();


        } else {
            RuntimePermission.askPermission(this)
                    .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)

                    .onAccepted((result) -> {
                        saveZip(bottomSheetDialog, contentView);
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
        else {
            return true;
        }
    }





    // ToolTips

    private void showTooltip(View target,
                             String text,
                             TooltipOrientation orientation,
                             ArrowOrientation arrowOrientation,
                             OnBalloonDismissListener listener) {

        if (showTooltips) {
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


enum TooltipOrientation {
    TOP,
    RIGHT,
    BOTTOM,
    LEFT
}







