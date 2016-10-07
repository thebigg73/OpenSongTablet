package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class OptionMenuListeners extends Activity {

    public static int mychild;
    public static int myparent;

    public interface MyInterface {
        void optionLongClick(int myparent, int mychild);
        void openFragment();
        void closeMyDrawers(String which);
        void refreshActionBar();
        void loadSong();
        void shareSong();
        void showActionBar();
        void hideActionBar();
        void onSongImport();
        void prepareSongMenu();
        void rebuildSearchIndex();
        void callIntent(String what, Intent intent);
        void prepareOptionMenu();
        void findSongInFolders();
    }

    static DialogFragment newFragment;
    public static MyInterface mListener;

    public static LinearLayout prepareOptionMenu(Context c) {
        mListener = (MyInterface) c;
        LinearLayout menu = null;
        Log.d ("d","whichOptionMenu="+FullscreenActivity.whichOptionMenu);
        switch (FullscreenActivity.whichOptionMenu) {
            case "MAIN":
            default:
                menu = createMainMenu(c);
                break;

            case "SET":
                menu = createSetMenu(c);
                break;

            case "SONG":
                menu = createSongMenu(c);
                break;

            case "CHORDS":
                menu = createChordsMenu(c);
                break;

            case "DISPLAY":
                menu = createDisplayMenu(c);
                break;

            case "STORAGE":
                menu = createStorageMenu(c);
                break;

            case "GESTURES":
                menu = createGesturesMenu(c);
                break;

            case "AUTOSCROLL":
                menu = createAutoscrollMenu(c);
                break;

            case "PAD":
                menu = createPadMenu(c);
                break;

            case "OTHER":
                menu = createOtherMenu(c);
                break;

        }
        if (mListener!=null) {
            mListener.refreshActionBar();
        }
        return menu;
    }

    public static LinearLayout createMainMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return (LinearLayout) inflater.inflate(R.layout.popup_option,null);
    }

    public static LinearLayout createSetMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return (LinearLayout) inflater.inflate(R.layout.popup_option_set,null);
    }

    public static LinearLayout createSongMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return (LinearLayout) inflater.inflate(R.layout.popup_option_song,null);
    }

    public static LinearLayout createChordsMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return (LinearLayout) inflater.inflate(R.layout.popup_option_chords,null);
    }

    public static LinearLayout createDisplayMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return (LinearLayout) inflater.inflate(R.layout.popup_option_display,null);
    }

    public static LinearLayout createStorageMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return (LinearLayout) inflater.inflate(R.layout.popup_option_storage,null);
    }

    public static LinearLayout createGesturesMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return (LinearLayout) inflater.inflate(R.layout.popup_option_gestures,null);
    }

    public static LinearLayout createAutoscrollMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return (LinearLayout) inflater.inflate(R.layout.popup_option_autoscroll,null);
    }

    public static LinearLayout createPadMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return (LinearLayout) inflater.inflate(R.layout.popup_option_pad,null);
    }

    public static LinearLayout createOtherMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return (LinearLayout) inflater.inflate(R.layout.popup_option_other,null);
    }

    public static void optionListeners(View v, Context c) {

        // Decide which listeners we need based on the menu
        switch (FullscreenActivity.whichOptionMenu) {
            case "MAIN":
            default:
                mainOptionListener(v,c);
                break;

            case "SET":
                setOptionListener(v,c);
                break;

            case "SONG":
                songOptionListener(v,c);
                break;

            case "CHORDS":
                chordOptionListener(v,c);
                break;

            case "DISPLAY":
                displayOptionListener(v,c);
                break;

            case "STORAGE":
                storageOptionListener(v,c);
                break;

            case "GESTURES":
                gestureOptionListener(v,c);
                break;

            case "AUTOSCROLL":
                autoscrollOptionListener(v,c);
                break;

            case "PAD":
                padOptionListener(v,c);
                break;

            case "OTHER":
                otherOptionListener(v,c);
                break;
        }
    }

    public static void mainOptionListener(View v, Context c) {
        mListener = (MyInterface) c;

        // Identify the buttons
        Button menuSetButton = (Button) v.findViewById(R.id.menuSetButton);
        Button menuSongButton = (Button) v.findViewById(R.id.menuSongButton);
        Button menuChordsButton = (Button) v.findViewById(R.id.menuChordsButton);
        Button menuDisplayButton = (Button) v.findViewById(R.id.menuDisplayButton);
        Button menuGesturesButton = (Button) v.findViewById(R.id.menuGesturesButton);
        Button menuStorageButton = (Button) v.findViewById(R.id.menuStorageButton);
        Button menuPadButton = (Button) v.findViewById(R.id.menuPadButton);
        Button menuAutoScrollButton = (Button) v.findViewById(R.id.menuAutoScrollButton);
        Button menuOtherButton = (Button) v.findViewById(R.id.menuOtherButton);

        // Capitalise all the text by locale
        menuSetButton.setText(c.getString(R.string.options_set).toUpperCase(FullscreenActivity.locale));
        menuSongButton.setText(c.getString(R.string.options_song).toUpperCase(FullscreenActivity.locale));
        menuChordsButton.setText(c.getString(R.string.chords).toUpperCase(FullscreenActivity.locale));
        menuDisplayButton.setText(c.getString(R.string.options_display).toUpperCase(FullscreenActivity.locale));
        menuGesturesButton.setText(c.getString(R.string.options_gesturesandmenus).toUpperCase(FullscreenActivity.locale));
        menuStorageButton.setText(c.getString(R.string.options_storage).toUpperCase(FullscreenActivity.locale));
        menuPadButton.setText(c.getString(R.string.pad).toUpperCase(FullscreenActivity.locale));
        menuAutoScrollButton.setText(c.getString(R.string.autoscroll).toUpperCase(FullscreenActivity.locale));
        menuOtherButton.setText(c.getString(R.string.options_other).toUpperCase(FullscreenActivity.locale));


        // Set the listeners
        menuSetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whichOptionMenu = "SET";
                if (mListener!=null) {
                    mListener.prepareOptionMenu();
                }
            }
        });
        menuSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whichOptionMenu = "SONG";
                if (mListener!=null) {
                    mListener.prepareOptionMenu();
                }
            }
        });
        menuChordsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whichOptionMenu = "CHORDS";
                if (mListener!=null) {
                    mListener.prepareOptionMenu();
                }
            }
        });
        menuDisplayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whichOptionMenu = "DISPLAY";
                if (mListener!=null) {
                    mListener.prepareOptionMenu();
                }
            }
        });
        menuGesturesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whichOptionMenu = "GESTURES";
                if (mListener!=null) {
                    mListener.prepareOptionMenu();
                }
            }
        });
        menuStorageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whichOptionMenu = "STORAGE";
                if (mListener!=null) {
                    mListener.prepareOptionMenu();
                }
            }
        });
        menuAutoScrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whichOptionMenu = "AUTOSCROLL";
                if (mListener!=null) {
                    mListener.prepareOptionMenu();
                }
            }
        });
        menuPadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whichOptionMenu = "PAD";
                if (mListener!=null) {
                    mListener.prepareOptionMenu();
                }
            }
        });
        menuOtherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whichOptionMenu = "OTHER";
                if (mListener!=null) {
                    mListener.prepareOptionMenu();
                }
            }
        });

    }

    public static void setOptionListener(View v, Context c) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuup = (TextView) v.findViewById(R.id.setMenuTitle);
        Button setLoadButton = (Button) v.findViewById(R.id.setLoadButton);
        Button setSaveButton = (Button) v.findViewById(R.id.setSaveButton);
        Button setNewButton = (Button) v.findViewById(R.id.setNewButton);
        Button setDeleteButton = (Button) v.findViewById(R.id.setDeleteButton);
        Button setExportButton = (Button) v.findViewById(R.id.setExportButton);
        Button setCustomButton = (Button) v.findViewById(R.id.setCustomButton);
        Button setVariationButton = (Button) v.findViewById(R.id.setVariationButton);
        Button setEditButton = (Button) v.findViewById(R.id.setEditButton);
        LinearLayout setLinearLayout = (LinearLayout) v.findViewById(R.id.setLinearLayout);

        // Capitalise all the text by locale
        menuup.setText(c.getString(R.string.options_set).toUpperCase(FullscreenActivity.locale));
        setLoadButton.setText(c.getString(R.string.options_set_load).toUpperCase(FullscreenActivity.locale));
        setSaveButton.setText(c.getString(R.string.options_set_save).toUpperCase(FullscreenActivity.locale));
        setNewButton.setText(c.getString(R.string.options_set_clear).toUpperCase(FullscreenActivity.locale));
        setDeleteButton.setText(c.getString(R.string.options_set_delete).toUpperCase(FullscreenActivity.locale));
        setExportButton.setText(c.getString(R.string.options_set_export).toUpperCase(FullscreenActivity.locale));
        setCustomButton.setText(c.getString(R.string.add_custom_slide).toUpperCase(FullscreenActivity.locale));
        setVariationButton.setText(c.getString(R.string.customise_set_item).toUpperCase(FullscreenActivity.locale));
        setEditButton.setText(c.getString(R.string.options_set_edit).toUpperCase(FullscreenActivity.locale));

        // Set the button listeners
        menuup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whichOptionMenu = "MAIN";
                if (mListener!=null) {
                    mListener.prepareOptionMenu();
                }
            }
        });

        setLoadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "loadset";
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        setSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "saveset";
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        setNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "clearset";
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        setDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "deleteset";
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        setExportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "exportset";
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        setCustomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "customcreate";
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        setVariationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "setitemvariation";
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        setEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "editset";
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        // Add the set list to the menu
        if (FullscreenActivity.mSetList!=null) {
            for (int x = 0; x<FullscreenActivity.mSetList.length; x++) {
                TextView tv = new TextView(c);
                tv.setText(FullscreenActivity.mSetList[x]);
                tv.setTextColor(0xffffffff);
                tv.setTextSize(16.0f);
                tv.setPadding(16,16,16,16);
                LinearLayout.LayoutParams tvp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                tvp.setMargins(40,40,40,40);
                tv.setLayoutParams(tvp);
                final int val = x;
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FullscreenActivity.setView = "Y";
                        FullscreenActivity.pdfPageCurrent = 0;
                        FullscreenActivity.linkclicked = FullscreenActivity.mSetList[val];
                        FullscreenActivity.indexSongInSet = val;
                        SetActions.songIndexClickInSet();
                        SetActions.getSongFileAndFolder();
                        if (mListener!=null) {
                            mListener.closeMyDrawers("option");
                            mListener.loadSong();
                        }
                    }
                });
                setLinearLayout.addView(tv);
            }
        }

    }

    public static void songOptionListener(View v, Context c) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuup = (TextView) v.findViewById(R.id.songMenuTitle);
        Button songEditButton = (Button) v.findViewById(R.id.songEditButton);
        Button songStickyButton = (Button) v.findViewById(R.id.songStickyButton);
        Button songRenameButton = (Button) v.findViewById(R.id.songRenameButton);
        Button songNewButton = (Button) v.findViewById(R.id.songNewButton);
        Button songDeleteButton = (Button) v.findViewById(R.id.songDeleteButton);
        Button songExportButton = (Button) v.findViewById(R.id.songExportButton);
        final SwitchCompat songPresentationOrderButton = (SwitchCompat) v.findViewById(R.id.songPresentationOrderButton);

        // Capitalise all the text by locale
        menuup.setText(c.getString(R.string.options_song).toUpperCase(FullscreenActivity.locale));
        songEditButton.setText(c.getString(R.string.options_song_edit).toUpperCase(FullscreenActivity.locale));
        songStickyButton.setText(c.getString(R.string.options_song_stickynotes).toUpperCase(FullscreenActivity.locale));
        songRenameButton.setText(c.getString(R.string.options_song_rename).toUpperCase(FullscreenActivity.locale));
        songNewButton.setText(c.getString(R.string.options_song_new).toUpperCase(FullscreenActivity.locale));
        songDeleteButton.setText(c.getString(R.string.options_song_delete).toUpperCase(FullscreenActivity.locale));
        songExportButton.setText(c.getString(R.string.options_song_export).toUpperCase(FullscreenActivity.locale));
        songPresentationOrderButton.setText(c.getString(R.string.edit_song_presentation).toUpperCase(FullscreenActivity.locale));

        // Set the button listeners
        menuup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whichOptionMenu = "MAIN";
                if (mListener!=null) {
                    mListener.prepareOptionMenu();
                }
            }
        });

        songEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "editsong";
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        songStickyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "editnotes";
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        songRenameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "renamesong";
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        songNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "createsong";
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        songDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "deletesong";
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        songExportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "exportsong";
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                    mListener.shareSong();
                }
            }
        });

        songPresentationOrderButton.setChecked(FullscreenActivity.usePresentationOrder);
        songPresentationOrderButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.usePresentationOrder = b;
                Preferences.savePreferences();
                if (mListener!=null) {
                    Preferences.savePreferences();
                    mListener.loadSong();
                }
            }
        });
    }

    public static void chordOptionListener(View v, Context c) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuup = (TextView) v.findViewById(R.id.chordsMenuTitle);
        Button chordsTransposeButton = (Button) v.findViewById(R.id.chordsTransposeButton);
        Button chordsSharpButton = (Button) v.findViewById(R.id.chordsSharpButton);
        Button chordsFlatButton = (Button) v.findViewById(R.id.chordsFlatButton);
        SwitchCompat chordsToggleSwitch = (SwitchCompat) v.findViewById(R.id.chordsToggleSwitch);
        SwitchCompat chordsLyricsToggleSwitch = (SwitchCompat) v.findViewById(R.id.chordsLyricsToggleSwitch);
        SwitchCompat chordsCapoToggleSwitch = (SwitchCompat) v.findViewById(R.id.chordsCapoToggleSwitch);
        SwitchCompat chordsNativeAndCapoToggleSwitch = (SwitchCompat) v.findViewById(R.id.chordsNativeAndCapoToggleSwitch);
        Button chordsFormatButton = (Button) v.findViewById(R.id.chordsFormatButton);
        Button chordsConvertButton = (Button) v.findViewById(R.id.chordsConvertButton);

        // Capitalise all the text by locale
        menuup.setText(c.getString(R.string.chords).toUpperCase(FullscreenActivity.locale));
        chordsTransposeButton.setText(c.getString(R.string.options_song_transpose).toUpperCase(FullscreenActivity.locale));
        chordsSharpButton.setText(c.getString(R.string.options_song_sharp).toUpperCase(FullscreenActivity.locale));
        chordsFlatButton.setText(c.getString(R.string.options_song_flat).toUpperCase(FullscreenActivity.locale));
        chordsToggleSwitch.setText(c.getString(R.string.showchords).toUpperCase(FullscreenActivity.locale));
        chordsLyricsToggleSwitch.setText(c.getString(R.string.showlyrics).toUpperCase(FullscreenActivity.locale));
        chordsCapoToggleSwitch.setText(c.getString(R.string.showcapo).toUpperCase(FullscreenActivity.locale));
        chordsNativeAndCapoToggleSwitch.setText(c.getString(R.string.capo_toggle_bothcapo).toUpperCase(FullscreenActivity.locale));
        chordsFormatButton.setText(c.getString(R.string.options_options_chordformat).toUpperCase(FullscreenActivity.locale));
        chordsConvertButton.setText(c.getString(R.string.options_song_convert).toUpperCase(FullscreenActivity.locale));

        // Set the button listeners
        menuup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whichOptionMenu = "MAIN";
                if (mListener!=null) {
                    mListener.prepareOptionMenu();
                }
            }
        });

    }

    public static void displayOptionListener(View v, Context c) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuup = (TextView) v.findViewById(R.id.optionDisplayTitle);
        //Button setLoadButton = (Button) v.findViewById(R.id.setLoadButton);

        // Capitalise all the text by locale
        menuup.setText(c.getString(R.string.options_display).toUpperCase(FullscreenActivity.locale));

        // Set the button listeners
        menuup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whichOptionMenu = "MAIN";
                if (mListener!=null) {
                    mListener.prepareOptionMenu();
                }
            }
        });

    }

    public static void storageOptionListener(View v, Context c) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuup = (TextView) v.findViewById(R.id.optionStorageTitle);
        //Button setLoadButton = (Button) v.findViewById(R.id.setLoadButton);

        // Capitalise all the text by locale
        menuup.setText(c.getString(R.string.options_storage).toUpperCase(FullscreenActivity.locale));

        // Set the button listeners
        menuup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whichOptionMenu = "MAIN";
                if (mListener!=null) {
                    mListener.prepareOptionMenu();
                }
            }
        });

    }

    public static void gestureOptionListener(View v, Context c) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuup = (TextView) v.findViewById(R.id.optionGestureTitle);
        //Button setLoadButton = (Button) v.findViewById(R.id.setLoadButton);

        // Capitalise all the text by locale
        menuup.setText(c.getString(R.string.options_gesturesandmenus).toUpperCase(FullscreenActivity.locale));

        // Set the button listeners
        menuup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whichOptionMenu = "MAIN";
                if (mListener!=null) {
                    mListener.prepareOptionMenu();
                }
            }
        });

    }

    public static void autoscrollOptionListener(View v, Context c) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuup = (TextView) v.findViewById(R.id.optionAutoScrollTitle);
        //Button setLoadButton = (Button) v.findViewById(R.id.setLoadButton);

        // Capitalise all the text by locale
        menuup.setText(c.getString(R.string.autoscroll).toUpperCase(FullscreenActivity.locale));

        // Set the button listeners
        menuup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whichOptionMenu = "MAIN";
                if (mListener!=null) {
                    mListener.prepareOptionMenu();
                }
            }
        });

    }

    public static void padOptionListener(View v, Context c) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuup = (TextView) v.findViewById(R.id.optionPadTitle);
        //Button setLoadButton = (Button) v.findViewById(R.id.setLoadButton);

        // Capitalise all the text by locale
        menuup.setText(c.getString(R.string.pad).toUpperCase(FullscreenActivity.locale));

        // Set the button listeners
        menuup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whichOptionMenu = "MAIN";
                if (mListener!=null) {
                    mListener.prepareOptionMenu();
                }
            }
        });

    }

    public static void otherOptionListener(View v, Context c) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuup = (TextView) v.findViewById(R.id.optionOtherTitle);
        //Button setLoadButton = (Button) v.findViewById(R.id.setLoadButton);

        // Capitalise all the text by locale
        menuup.setText(c.getString(R.string.options_other).toUpperCase(FullscreenActivity.locale));

        // Set the button listeners
        menuup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whichOptionMenu = "MAIN";
                if (mListener!=null) {
                    mListener.prepareOptionMenu();
                }
            }
        });

    }



    public static void createFullMenu(Context c) {
        mListener = (MyInterface) c;

        // preparing list data
        FullscreenActivity.listDataHeaderOption = new ArrayList<>();
        FullscreenActivity.listDataChildOption = new HashMap<>();

        // Adding headers for option menu data
        FullscreenActivity.listDataHeaderOption.add(c.getResources().getString(R.string.options_set).toUpperCase(FullscreenActivity.locale));
        FullscreenActivity.listDataHeaderOption.add(c.getResources().getString(R.string.options_song).toUpperCase(FullscreenActivity.locale));
        FullscreenActivity.listDataHeaderOption.add(c.getResources().getString(R.string.chords).toUpperCase(FullscreenActivity.locale));
        FullscreenActivity.listDataHeaderOption.add(c.getResources().getString(R.string.options_display).toUpperCase(FullscreenActivity.locale));
        FullscreenActivity.listDataHeaderOption.add(c.getResources().getString(R.string.options_gesturesandmenus).toUpperCase(FullscreenActivity.locale));
        FullscreenActivity.listDataHeaderOption.add(c.getResources().getString(R.string.options_storage).toUpperCase(FullscreenActivity.locale));
        FullscreenActivity.listDataHeaderOption.add(c.getResources().getString(R.string.pad).toUpperCase(FullscreenActivity.locale));
        FullscreenActivity.listDataHeaderOption.add(c.getResources().getString(R.string.autoscroll).toUpperCase(FullscreenActivity.locale));
        FullscreenActivity.listDataHeaderOption.add(c.getResources().getString(R.string.options_other).toUpperCase(FullscreenActivity.locale));

        // Adding child data
        List<String> options_set = new ArrayList<>();
        options_set.add(c.getResources().getString(R.string.options_set_load));
        options_set.add(c.getResources().getString(R.string.options_set_save));
        options_set.add(c.getResources().getString(R.string.options_set_clear));
        options_set.add(c.getResources().getString(R.string.options_set_delete));
        options_set.add(c.getResources().getString(R.string.options_set_export));
        options_set.add(c.getResources().getString(R.string.add_custom_slide));
        options_set.add(c.getResources().getString(R.string.options_set_edit));
        options_set.add(c.getResources().getString(R.string.customise_set_item));

        options_set.add("");

        // Parse the saved set
        FullscreenActivity.mySet = FullscreenActivity.mySet.replace("_**$$**_", "_**$%%%$**_");
        // Break the saved set up into a new String[]
        FullscreenActivity.mSetList = FullscreenActivity.mySet.split("%%%");
        // Restore the set back to what it was
        FullscreenActivity.mySet = FullscreenActivity.mySet.replace("_**$%%%$**_", "_**$$**_");
        FullscreenActivity.setSize = FullscreenActivity.mSetList.length;

        for (int r = 0; r < FullscreenActivity.mSetList.length; r++) {
            FullscreenActivity.mSetList[r] = FullscreenActivity.mSetList[r].replace("$**_", "");
            FullscreenActivity.mSetList[r] = FullscreenActivity.mSetList[r].replace("_**$", "");
            if (!FullscreenActivity.mSetList[r].isEmpty()) {
                options_set.add(FullscreenActivity.mSetList[r]);
            }
        }

        List<String> options_song = new ArrayList<>();
        options_song.add(c.getResources().getString(R.string.options_song_edit));
        options_song.add(c.getResources().getString(R.string.options_song_stickynotes));
        options_song.add(c.getResources().getString(R.string.options_song_rename));
        options_song.add(c.getResources().getString(R.string.options_song_delete));
        options_song.add(c.getResources().getString(R.string.options_song_new));
        options_song.add(c.getResources().getString(R.string.options_song_export));
        options_song.add(c.getResources().getString(R.string.edit_song_presentation));

        List<String> options_chords = new ArrayList<>();
        options_chords.add(c.getResources().getString(R.string.options_song_transpose));
        options_chords.add(c.getResources().getString(R.string.capo_toggle));
        options_chords.add(c.getResources().getString(R.string.options_song_sharp));
        options_chords.add(c.getResources().getString(R.string.options_song_flat));
        options_chords.add(c.getResources().getString(R.string.options_options_chordformat));
        options_chords.add(c.getResources().getString(R.string.options_song_convert));
        options_chords.add(c.getResources().getString(R.string.options_options_showchords));

        List<String> options_display = new ArrayList<>();
        options_display.add(c.getResources().getString(R.string.options_options_theme));
        options_display.add(c.getResources().getString(R.string.options_options_scale));
        options_display.add(c.getResources().getString(R.string.options_options_fonts));
        options_display.add(c.getResources().getString(R.string.pagebuttons));
        options_display.add(c.getResources().getString(R.string.extra));
        options_display.add(c.getResources().getString(R.string.options_options_hidebar));
        options_display.add(c.getResources().getString(R.string.profile));

        List<String> options_gesturesandmenus = new ArrayList<>();
        options_gesturesandmenus.add(c.getResources().getString(R.string.options_options_pedal));
        options_gesturesandmenus.add(c.getResources().getString(R.string.options_options_gestures));
        options_gesturesandmenus.add(c.getResources().getString(R.string.options_options_menuswipe));
        options_gesturesandmenus.add(c.getResources().getString(R.string.options_options_songswipe));

        List<String> options_storage = new ArrayList<>();
        options_storage.add(c.getResources().getString(R.string.options_song_newfolder));
        options_storage.add(c.getResources().getString(R.string.options_song_editfolder));
        options_storage.add(c.getResources().getString(R.string.storage_choose));
        options_storage.add(c.getResources().getString(R.string.import_onsong_choose));
        options_storage.add(c.getResources().getString(R.string.refreshsongs));
        options_storage.add(c.getResources().getString(R.string.search_rebuild));
        options_storage.add(c.getResources().getString(R.string.search_log));

        List<String> options_pad = new ArrayList<>();
        options_pad.add(c.getResources().getString(R.string.crossfade_time));

        List<String> options_autoscroll = new ArrayList<>();
        options_autoscroll.add(c.getResources().getString(R.string.default_autoscroll));
        options_autoscroll.add(c.getResources().getString(R.string.options_options_autostartscroll));

        List<String> options_other = new ArrayList<>();
        options_other.add(c.getResources().getString(R.string.options_options_help));
        options_other.add("@OpenSongApp");
        options_other.add(c.getResources().getString(R.string.language));
        options_other.add(c.getResources().getString(R.string.options_options_start));
        options_other.add(c.getResources().getString(R.string.rate));

        FullscreenActivity.listDataChildOption.put(FullscreenActivity.listDataHeaderOption.get(0), options_set); // Header, Child data
        FullscreenActivity.listDataChildOption.put(FullscreenActivity.listDataHeaderOption.get(1), options_song);
        FullscreenActivity.listDataChildOption.put(FullscreenActivity.listDataHeaderOption.get(2), options_chords);
        FullscreenActivity.listDataChildOption.put(FullscreenActivity.listDataHeaderOption.get(3), options_display);
        FullscreenActivity.listDataChildOption.put(FullscreenActivity.listDataHeaderOption.get(4), options_gesturesandmenus);
        FullscreenActivity.listDataChildOption.put(FullscreenActivity.listDataHeaderOption.get(5), options_storage);
        FullscreenActivity.listDataChildOption.put(FullscreenActivity.listDataHeaderOption.get(6), options_pad);
        FullscreenActivity.listDataChildOption.put(FullscreenActivity.listDataHeaderOption.get(7), options_autoscroll);
        FullscreenActivity.listDataChildOption.put(FullscreenActivity.listDataHeaderOption.get(8), options_other);

        mListener.refreshActionBar();
    }

    public static ExpandableListView.OnChildClickListener myShortClickListener(final Context c) {

        return new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                mListener = (MyInterface) c;

                // Close the drawers
                mListener.closeMyDrawers("both");

                if (!FullscreenActivity.removingfromset) {
                    String chosenMenu = FullscreenActivity.listDataHeaderOption.get(groupPosition);

                    if (chosenMenu.equals(c.getResources().getString(R.string.options_set).toUpperCase(FullscreenActivity.locale))) {

                        // Now check for set options clicks

                        // 0 = Load a set
                        // 1 = Save current set
                        // 2 = Clear current set
                        // 3 = Delete saved set
                        // 4 = Export saved set
                        // 5 = Add a custom slide
                        // 6 = Edit current set
                        // 7 = Make a variation of an item in the set
                        // 8 =
                        // 9+  Set items (songs,etc.)

                        // Load up a list of saved sets as it will likely be needed
                        SetActions.updateOptionListSets();
                        Arrays.sort(FullscreenActivity.mySetsFiles);
                        Arrays.sort(FullscreenActivity.mySetsDirectories);
                        Arrays.sort(FullscreenActivity.mySetsFileNames);
                        Arrays.sort(FullscreenActivity.mySetsFolderNames);

                        switch (childPosition) {
                            case 0:
                                // Load a set
                                FullscreenActivity.whattodo = "loadset";
                                mListener.openFragment();
                                mListener.prepareOptionMenu();
                                break;

                            case 1:
                                // Save current set
                                FullscreenActivity.whattodo = "saveset";
                                mListener.openFragment();
                                break;

                            case 2:
                                // Clear current set
                                FullscreenActivity.whattodo = "clearset";
                                mListener.openFragment();
                                break;

                            case 3:
                                // Delete saved set
                                FullscreenActivity.whattodo = "deleteset";
                                mListener.openFragment();
                                break;

                            case 4:
                                // Export current set
                                FullscreenActivity.whattodo = "exportset";
                                mListener.openFragment();
                                break;

                            case 5:
                                // Add a custom slide
                                FullscreenActivity.whattodo = "customcreate";
                                mListener.openFragment();
                                break;

                            case 6:
                                // Edit current set
                                FullscreenActivity.whattodo = "editset";
                                mListener.openFragment();
                                break;

                            case 7:
                                // Make a variation of an item in the set
                                FullscreenActivity.whattodo = "setitemvariation";
                                mListener.openFragment();
                                break;
                        }

                        if (childPosition > 8) {
                            // Load song in set
                            FullscreenActivity.setView = "Y";
                            FullscreenActivity.pdfPageCurrent = 0;
                            // Set item is 9 less than childPosition
                            FullscreenActivity.indexSongInSet = childPosition - 9;
                            FullscreenActivity.linkclicked = FullscreenActivity.mSetList[FullscreenActivity.indexSongInSet];
                            if (FullscreenActivity.linkclicked == null) {
                                FullscreenActivity.linkclicked = "";
                            }
                            if (FullscreenActivity.indexSongInSet == 0) {
                                // Already first item
                                FullscreenActivity.previousSongInSet = "";
                            } else {
                                FullscreenActivity.previousSongInSet = FullscreenActivity.mSetList[FullscreenActivity.indexSongInSet - 1];
                            }

                            if (FullscreenActivity.indexSongInSet == (FullscreenActivity.setSize - 1)) {
                                // Last item
                                FullscreenActivity.nextSongInSet = "";
                            } else {
                                FullscreenActivity.nextSongInSet = FullscreenActivity.mSetList[FullscreenActivity.indexSongInSet + 1];
                            }
                            FullscreenActivity.whichDirection = "R2L";

                            if (!FullscreenActivity.linkclicked.contains("/")) {
                                // Right it doesn't, so add the /
                                FullscreenActivity.linkclicked = "/" + FullscreenActivity.linkclicked;
                            }

                            // Now split the FullscreenActivity.linkclicked into two song parts 0=folder 1=file
                            FullscreenActivity.songpart = FullscreenActivity.linkclicked.split("/");

                            if (FullscreenActivity.songpart.length < 2) {
                                FullscreenActivity.songpart = new String[2];
                                FullscreenActivity.songpart[0] = "";
                                FullscreenActivity.songpart[1] = "";
                            }

                            // If the folder length isn't 0, it is a folder
                            if (FullscreenActivity.songpart[0].length() > 0 && !FullscreenActivity.songpart[0].contains(FullscreenActivity.text_scripture) && !FullscreenActivity.songpart[0].contains(FullscreenActivity.image) && !FullscreenActivity.songpart[0].contains(FullscreenActivity.text_slide) && !FullscreenActivity.songpart[0].contains(FullscreenActivity.text_note)) {
                                FullscreenActivity.whichSongFolder = FullscreenActivity.songpart[0];

                            } else if (FullscreenActivity.songpart[0].length() > 0 && FullscreenActivity.songpart[0].contains(FullscreenActivity.text_scripture)) {
                                FullscreenActivity.whichSongFolder = "../Scripture/_cache";
                                FullscreenActivity.songpart[0] = "../Scripture/_cache";

                            } else if (FullscreenActivity.songpart[0].length() > 0 && FullscreenActivity.songpart[0].contains(FullscreenActivity.text_slide)) {
                                FullscreenActivity.whichSongFolder = "../Slides/_cache";
                                FullscreenActivity.songpart[0] = "../Slides/_cache";

                            } else if (FullscreenActivity.songpart[0].length() > 0 && FullscreenActivity.songpart[0].contains(FullscreenActivity.text_note)) {
                                FullscreenActivity.whichSongFolder = "../Notes/_cache";
                                FullscreenActivity.songpart[0] = "../Notes/_cache";

                            } else if (FullscreenActivity.songpart[0].length() > 0 && FullscreenActivity.songpart[0].contains(FullscreenActivity.image)) {
                                FullscreenActivity.whichSongFolder = "../Images/_cache";
                                FullscreenActivity.songpart[0] = "../Images/_cache";

                            } else {
                                FullscreenActivity.whichSongFolder = FullscreenActivity.mainfoldername;
                            }

                            FullscreenActivity.songfilename = null;
                            FullscreenActivity.songfilename = "";
                            FullscreenActivity.songfilename = FullscreenActivity.songpart[1];

                            // Save the preferences and load the song
                            Preferences.savePreferences();
                            mListener.refreshActionBar();
                            mListener.loadSong();
                        }

                    } else if (chosenMenu.equals(c.getResources().getString(R.string.options_song).toUpperCase(FullscreenActivity.locale))) {
                        FullscreenActivity.linkclicked = FullscreenActivity.listDataChildOption.get(FullscreenActivity.listDataHeaderOption.get(groupPosition)).get(childPosition);

                        // Now check for song options clicks

                        // 0 = Edit song
                        // 1 = Edit sticky notes
                        // 2 = Rename song
                        // 3 = Delete song
                        // 4 = New song
                        // 5 = Export song
                        // 6 = Presentation order

                        switch (childPosition) {
                            case 0:
                                // Edit song
                                if (FullscreenActivity.isPDF) {// Can't do this action on a pdf!
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.pdf_functionnotavailable);
                                    ShowToast.showToast(c);
                                } else if (!FullscreenActivity.isSong) {// Editing a slide / note / scripture / image
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.not_allowed);
                                    ShowToast.showToast(c);
                                } else {
                                    FullscreenActivity.whattodo = "editsong";
                                    mListener.openFragment();
                                }
                                break;

                            case 1:
                                // Edit sticky notes
                                if (FullscreenActivity.isPDF) {
                                    // Can't do this action on a pdf!
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.pdf_functionnotavailable);
                                    ShowToast.showToast(c);
                                } else if (!FullscreenActivity.isSong) {
                                    // Editing a slide / note / scripture / image
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.not_allowed);
                                    ShowToast.showToast(c);
                                } else {
                                    FullscreenActivity.whattodo = "editnotes";
                                    mListener.openFragment();
                                }
                                break;

                            case 2:
                                // Rename song
                                if (!FullscreenActivity.isPDF && !FullscreenActivity.isSong) {
                                    // Editing a slide / note / scripture / image
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.not_allowed);
                                    ShowToast.showToast(c);
                                } else {
                                    FullscreenActivity.whattodo = "renamesong";
                                    mListener.openFragment();
                                }
                                break;

                            case 3:
                                // Delete song
                                if (!FullscreenActivity.isPDF && !FullscreenActivity.isSong) {
                                    // Editing a slide / note / scripture / image
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.not_allowed);
                                    ShowToast.showToast(c);
                                } else {
                                    // Delete
                                    // Give the user an are you sure prompt!
                                    FullscreenActivity.whattodo = "deletesong";
                                    mListener.openFragment();
                                }
                                break;

                            case 4:
                                // New song
                                FullscreenActivity.whattodo = "createsong";
                                mListener.openFragment();
                                break;

                            case 5:
                                // Export song
                                mListener.shareSong();
                                break;

                            case 6:
                                // Presentation order
                                if (FullscreenActivity.usePresentationOrder) {
                                    FullscreenActivity.usePresentationOrder = false;
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.edit_song_presentation) + " - "
                                            + c.getResources().getString(R.string.off);
                                } else {
                                    FullscreenActivity.usePresentationOrder = true;
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.edit_song_presentation) + " - "
                                            + c.getResources().getString(R.string.on);
                                }
                                ShowToast.showToast(c);
                                Preferences.savePreferences();
                                mListener.loadSong();
                                break;
                        }


                    } else if (chosenMenu.equals(c.getResources().getString(R.string.chords).toUpperCase(FullscreenActivity.locale))) {
                        FullscreenActivity.linkclicked = FullscreenActivity.listDataChildOption.get(FullscreenActivity.listDataHeaderOption.get(groupPosition)).get(childPosition);

                        // Now check for chord options clicks

                        // 0 = Transpose
                        // 1 = Capo toggle
                        // 2 = Use # chords
                        // 3 = Use b chords
                        // 4 = Choose chord format
                        // 5 = Convert to preferred chord format
                        // 6 = Show/hide chords

                        switch (childPosition) {
                            case 0:
                                // Transpose

                                if (FullscreenActivity.isPDF) {
                                    // Can't do this action on a pdf!
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.pdf_functionnotavailable);
                                    ShowToast.showToast(c);
                                } else if (!FullscreenActivity.isSong) {
                                    // Editing a slide / note / scripture / image
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.not_allowed);
                                    ShowToast.showToast(c);
                                } else {
                                    FullscreenActivity.transposeDirection = "+1";
                                    FullscreenActivity.transposeTimes = 1;
                                    Transpose.checkChordFormat();
                                    FullscreenActivity.whattodo = "transpose";
                                    mListener.openFragment();
                                }
                                break;

                            case 1:
                                // Capo toggle
                                switch (FullscreenActivity.capoDisplay) {
                                    case "both":
                                        FullscreenActivity.capoDisplay = "capoonly";
                                        FullscreenActivity.myToastMessage = c.getResources().getString(R.string.capo_toggle_onlycapo);
                                        break;
                                    case "capoonly":
                                        FullscreenActivity.capoDisplay = "native";
                                        FullscreenActivity.myToastMessage = c.getResources().getString(R.string.capo_toggle_native);
                                        break;
                                    default:
                                        FullscreenActivity.capoDisplay = "both";
                                        FullscreenActivity.myToastMessage = c.getResources().getString(R.string.capo_toggle_bothcapo);
                                        break;
                                }
                                ShowToast.showToast(c);
                                Preferences.savePreferences();
                                mListener.loadSong();
                                break;

                            case 2:
                                // Use # chords
                                if (FullscreenActivity.isPDF) {
                                    // Can't do this action on a pdf!
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.pdf_functionnotavailable);
                                    ShowToast.showToast(c);
                                } else if (!FullscreenActivity.isSong) {
                                    // Editing a slide / note / scripture / image
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.not_allowed);
                                    ShowToast.showToast(c);
                                } else {
                                    FullscreenActivity.transposeStyle = "sharps";
                                    FullscreenActivity.transposeDirection = "0";
                                    FullscreenActivity.switchsharpsflats = true;
                                    Transpose.checkChordFormat();
                                    try {
                                        Transpose.doTranspose();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    FullscreenActivity.switchsharpsflats = false;
                                    mListener.loadSong();
                                }
                                break;

                            case 3:
                                // Use b chords
                                if (FullscreenActivity.isPDF) {
                                    // Can't do this action on a pdf!
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.pdf_functionnotavailable);
                                    ShowToast.showToast(c);
                                } else if (!FullscreenActivity.isSong) {
                                    // Editing a slide / note / scripture / image
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.not_allowed);
                                    ShowToast.showToast(c);
                                } else {
                                    FullscreenActivity.transposeStyle = "flats";
                                    FullscreenActivity.transposeDirection = "0";
                                    FullscreenActivity.switchsharpsflats = true;
                                    Transpose.checkChordFormat();
                                    try {
                                        Transpose.doTranspose();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    FullscreenActivity.switchsharpsflats = false;
                                    mListener.loadSong();
                                }
                                break;

                            case 4:
                                // Choose chord format
                                FullscreenActivity.whattodo = "chordformat";
                                mListener.openFragment();
                                break;

                            case 5:
                                // Convert to preferred chord format
                                if (FullscreenActivity.isPDF) {
                                    // Can't do this action on a pdf!
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.pdf_functionnotavailable);
                                    ShowToast.showToast(c);
                                } else if (!FullscreenActivity.isSong) {
                                    // Editing a slide / note / scripture / image
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.not_allowed);
                                    ShowToast.showToast(c);
                                } else {
                                    FullscreenActivity.transposeDirection = "0";
                                    //chord_converting = "Y";
                                    Transpose.checkChordFormat();
                                    try {
                                        Transpose.doTranspose();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    mListener.loadSong();
                                }
                                break;

                            case 6:
                                // Show/hide chords
                                if (FullscreenActivity.isPDF) {
                                    // Can't do this action on a pdf!
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.pdf_functionnotavailable);
                                    ShowToast.showToast(c);
                                } else {
                                    if (FullscreenActivity.showChords.equals("Y")) {
                                        FullscreenActivity.showChords = "N";
                                    } else {
                                        FullscreenActivity.showChords = "Y";
                                    }
                                    // Save the preferences
                                    Preferences.savePreferences();
                                    mListener.loadSong();
                                }
                                break;
                        }

                    } else if (chosenMenu.equals(c.getResources().getString(R.string.options_display).toUpperCase(FullscreenActivity.locale))) {
                        FullscreenActivity.linkclicked = FullscreenActivity.listDataChildOption.get(FullscreenActivity.listDataHeaderOption.get(groupPosition)).get(childPosition);

                        // Now check for display options clicks

                        // 0 = Change theme
                        // 1 = Toggle autoscale
                        // 2 = Fonts
                        // 3 = Page buttons
                        // 4 = Extra information
                        // 5 = Show/hide menu bar
                        // 6 = Profiles

                        switch (childPosition) {
                            case 0:
                                // Change theme
                                if (FullscreenActivity.isPDF) {
                                    // Can't do this action on a pdf!
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.pdf_functionnotavailable);
                                    ShowToast.showToast(c);
                                } else {
                                    FullscreenActivity.whattodo = "changetheme";
                                    mListener.openFragment();
                                }
                                break;

                            case 1:
                                // Toggle autoscale
                                FullscreenActivity.whattodo = "autoscale";
                                mListener.openFragment();
                                break;

                            case 2:
                                // Fonts
                                FullscreenActivity.whattodo = "changefonts";
                                mListener.openFragment();
                                break;

                            case 3:
                                // Page buttons
                                FullscreenActivity.whattodo = "pagebuttons";
                                mListener.openFragment();
                                break;

                            case 4:
                                // Extra information
                                FullscreenActivity.whattodo = "extra";
                                mListener.openFragment();
                                break;

                            case 5:
                                // Show/hide menu bar
                                if (FullscreenActivity.hideactionbaronoff.equals("Y")) {
                                    FullscreenActivity.hideactionbaronoff = "N";
                                    FullscreenActivity.myToastMessage = c.getResources()
                                            .getString(R.string.options_options_hidebar)
                                            + " - "
                                            + c.getResources().getString(R.string.off);
                                    ShowToast.showToast(c);
                                    mListener.showActionBar();


                                } else {
                                    FullscreenActivity.hideactionbaronoff = "Y";
                                    FullscreenActivity.myToastMessage = c.getResources()
                                            .getString(R.string.options_options_hidebar)
                                            + " - "
                                            + c.getResources().getString(R.string.on);
                                    ShowToast.showToast(c);
                                    mListener.hideActionBar();
                                }
                                break;

                            case 6:
                                // Profiles
                                FullscreenActivity.whattodo = "profiles";
                                mListener.openFragment();
                                break;
                        }


                    } else if (chosenMenu.equals(c.getResources().getString(R.string.options_gesturesandmenus).toUpperCase(FullscreenActivity.locale))) {
                        FullscreenActivity.linkclicked = FullscreenActivity.listDataChildOption.get(FullscreenActivity.listDataHeaderOption.get(groupPosition)).get(childPosition);

                        // Now check for gestures and menus options clicks

                        // 0 = Assign pedals
                        // 1 = Custom gestures
                        // 2 = Toggle menu swipe on/off
                        // 3 = Toggle song swipe on/off

                        switch (childPosition) {
                            case 0:
                                //Assign pedals
                                FullscreenActivity.whattodo = "footpedal";
                                mListener.openFragment();
                                break;

                            case 1:
                                // Custom gestures
                                FullscreenActivity.whattodo = "gestures";
                                mListener.openFragment();
                                break;

                            case 2:
                                // Toggle menu swipe on/off
                                if (FullscreenActivity.swipeDrawer.equals("Y")) {
                                    FullscreenActivity.swipeDrawer = "N";
                                    mListener.closeMyDrawers("locked");
                                    FullscreenActivity.myToastMessage = c.getResources().getString(
                                            R.string.drawerswipe)
                                            + " " + c.getResources().getString(R.string.off);
                                    ShowToast.showToast(c);
                                } else {
                                    FullscreenActivity.swipeDrawer = "Y";
                                    mListener.closeMyDrawers("unlocked");
                                    FullscreenActivity.myToastMessage = c.getResources().getString(
                                            R.string.drawerswipe)
                                            + " " + c.getResources().getString(R.string.on);
                                    ShowToast.showToast(c);
                                }
                                Preferences.savePreferences();
                                break;

                            case 3:
                                // Toggle song swipe on/off
                                switch (FullscreenActivity.swipeSet) {
                                    case "Y":
                                        FullscreenActivity.swipeSet = "S";
                                        Preferences.savePreferences();
                                        FullscreenActivity.myToastMessage = c.getResources()
                                                .getString(R.string.swipeSet)
                                                + " "
                                                + c.getResources().getString(R.string.on_set);
                                        ShowToast.showToast(c);
                                        break;
                                    case "S":
                                        FullscreenActivity.swipeSet = "N";
                                        Preferences.savePreferences();
                                        FullscreenActivity.myToastMessage = c.getResources()
                                                .getString(R.string.swipeSet)
                                                + " "
                                                + c.getResources().getString(R.string.off);
                                        ShowToast.showToast(c);
                                        break;
                                    default:
                                        FullscreenActivity.swipeSet = "Y";
                                        Preferences.savePreferences();
                                        FullscreenActivity.myToastMessage = c.getResources()
                                                .getString(R.string.swipeSet)
                                                + " "
                                                + c.getResources().getString(R.string.on);
                                        ShowToast.showToast(c);
                                        break;
                                }
                                break;
                        }


                    } else if (chosenMenu.equals(c.getResources().getString(R.string.options_storage).toUpperCase(FullscreenActivity.locale))) {
                        FullscreenActivity.linkclicked = FullscreenActivity.listDataChildOption.get(FullscreenActivity.listDataHeaderOption.get(groupPosition)).get(childPosition);

                        // Now check for storage options clicks

                        // 0 = Create new song folder
                        // 1 = Edit song folder name
                        // 2 = Manage storage
                        // 3 = Import OnSong
                        // 4 = Refresh songs menu
                        // 5 = Rebuild search index
                        // 6 = View search error log

                        switch (childPosition) {
                            case 0:
                                FullscreenActivity.whattodo = "newfolder";
                                mListener.openFragment();
                                break;

                            case 1:
                                FullscreenActivity.whattodo = "editfoldername";
                                mListener.openFragment();
                                break;

                            case 2:
                                // Manage storage
                                FullscreenActivity.whattodo = "managestorage";
                                mListener.openFragment();
                                break;

                            case 3:
                                // Import OnSong
                                mListener.onSongImport();
                                break;

                            case 4:
                                // Refresh songs menu
                                mListener.prepareSongMenu();
                                break;

                            case 5:
                                // Rebuild song index
                                FullscreenActivity.safetosearch = false;
                                mListener.rebuildSearchIndex();
                                break;

                            case 6:
                                // View search error log
                                FullscreenActivity.whattodo = "errorlog";
                                mListener.openFragment();
                                break;
                        }


                    } else if (chosenMenu.equals(c.getResources().getString(R.string.pad).toUpperCase(FullscreenActivity.locale))) {
                        FullscreenActivity.linkclicked = FullscreenActivity.listDataChildOption.get(FullscreenActivity.listDataHeaderOption.get(groupPosition)).get(childPosition);

                        // Now check for pad options clicks

                        // 0 = Cross fade time

                        switch (childPosition) {
                            case 0:
                                FullscreenActivity.whattodo = "crossfade";
                                mListener.openFragment();
                                break;
                        }


                    } else if (chosenMenu.equals(c.getResources().getString(R.string.autoscroll).toUpperCase(FullscreenActivity.locale))) {
                        FullscreenActivity.linkclicked = FullscreenActivity.listDataChildOption.get(FullscreenActivity.listDataHeaderOption.get(groupPosition)).get(childPosition);

                        // Now check for autoscroll options clicks

                        // 0 = Autoscroll defaults
                        // 1 = Autostart autoscroll

                        switch (childPosition) {
                            case 0:
                                // Autoscroll delay time
                                FullscreenActivity.whattodo = "autoscrolldefaults";
                                mListener.openFragment();
                                break;

                            case 1:
                                // Autostart autoscroll
                                if (FullscreenActivity.autostartautoscroll) {
                                    FullscreenActivity.autostartautoscroll = false;
                                    Preferences.savePreferences();
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.options_options_autostartscroll) + " - " + c.getResources().getString(R.string.off);
                                    ShowToast.showToast(c);
                                } else {
                                    FullscreenActivity.autostartautoscroll = true;
                                    Preferences.savePreferences();
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.options_options_autostartscroll) + " - " + c.getResources().getString(R.string.on);
                                    ShowToast.showToast(c);
                                }
                                mListener.loadSong();
                        }


                    } else if (chosenMenu.equals(c.getResources().getString(R.string.options_other).toUpperCase(FullscreenActivity.locale))) {
                        FullscreenActivity.linkclicked = FullscreenActivity.listDataChildOption.get(FullscreenActivity.listDataHeaderOption.get(groupPosition)).get(childPosition);

                        // Now check for other options clicks

                        // 0 = Help
                        // 1 = Twitter
                        // 2 = Language
                        // 3 = Start/splash screen
                        // 4 = Rate this app
                        Intent i;
                        switch (childPosition) {
                            case 0:
                                // Help
                                String url = "https://www.opensongapp.com";
                                i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(url));
                                mListener.callIntent("web", i);
                                break;

                            case 1:
                                // Twitter
                                mListener.callIntent("twitter", null);
                                break;

                            case 2:
                                // Choose Language
                                FullscreenActivity.whattodo = "language";
                                mListener.openFragment();
                                break;

                            case 3:
                                // Start/splash screen
                                // First though, set the preference to show the current version
                                // Otherwise it won't show the splash screen
                                SharedPreferences settings = c.getSharedPreferences("mysettings",
                                        Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putInt("showSplashVersion", 0);
                                editor.apply();
                                i = new Intent();
                                i.setClass(c, SettingsActivity.class);
                                mListener.callIntent("activity", i);
                                break;

                            case 4:
                                // Rate this app
                                String appPackage = c.getPackageName();
                                i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackage));
                                mListener.callIntent("web", i);
                                break;
                        }
                    }
                }
                return false;
            }
        };
    }

    public static ExpandableListView.OnItemLongClickListener myLongClickListener(final Context c) {

        return new ExpandableListView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                mListener = (MyInterface) c;
                FullscreenActivity.removingfromset = true;
                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    int childPosition = ExpandableListView.getPackedPositionChild(id);
                    FullscreenActivity.myOptionListClickedItem = position;

                    if (FullscreenActivity.myOptionListClickedItem >8 && groupPosition == 0) {
                        // Long clicking on the 9th or later options will remove the
                        // song from the set (all occurrences)
                        // Remove this song from the set. Remember it has tags at the start and end
                        // Vibrate to indicate something has happened
                        Vibrator vb = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
                        vb.vibrate(50);

                        // Take away the menu items (9)
                        String tempSong = FullscreenActivity.mSetList[childPosition - 9];
                        FullscreenActivity.mSetList[childPosition - 9] = "";

                        if (FullscreenActivity.indexSongInSet == (childPosition - 9)) {
                            FullscreenActivity.setView = "N";
                        }

                        FullscreenActivity.mySet = "";
                        for (String aMSetList : FullscreenActivity.mSetList) {
                            if (!aMSetList.isEmpty()) {
                                FullscreenActivity.mySet = FullscreenActivity.mySet + "$**_" + aMSetList + "_**$";
                            }
                        }

                        // Tell the user that the song has been removed.
                        FullscreenActivity.myToastMessage = "\"" + tempSong + "\" "
                                + c.getResources().getString(R.string.removedfromset);
                        ShowToast.showToast(c);

                        // Save set
                        Preferences.savePreferences();

                        mListener.optionLongClick(myparent,mychild);
                    }
                }
                FullscreenActivity.removingfromset = false;
                return false;
            }
        };
    }
}