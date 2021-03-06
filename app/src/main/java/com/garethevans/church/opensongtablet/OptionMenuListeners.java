package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class OptionMenuListeners extends AppCompatActivity implements MenuInterface{

    @SuppressLint("StaticFieldLeak")
    static TextView connectionLog;
    Context context;

    public OptionMenuListeners() {}

    public OptionMenuListeners(Context context) {
        this.context = context;
    }

    @Override
    public void updateConnectionsLog() {
        if (StaticVariables.whichOptionMenu.equals("CONNECT") && connectionLog != null) {
            try {
                setTextTextView(connectionLog,StaticVariables.connectionLog);
            } catch (Exception e) {
                Log.d("d", "Connections menu closed");
            }
        }
    }

    public interface MyInterface {
        void openFragment();
        void openMyDrawers(String which);
        void closeMyDrawers(String which);
        void refreshActionBar();
        void loadSong();
        void prepareSongMenu();
        void rebuildSearchIndex();
        void callIntent(String what, Intent intent);
        void prepareOptionMenu();
        void removeSongFromSet(int val);
        void splashScreen();
        void showActionBar();
        void hideActionBar();
        void useCamera();
        void doDownload(String file);
        void connectHDMI();
        void takeScreenShot();
        void prepareLearnAutoScroll();
        // IV - Activities use the gestures - these have the stop and start logic
        void gesture5();
        void gesture6();
        void gesture7();
        void doExport();
        void updateExtraInfoColorsAndSizes(String s);
        void selectAFileUri(String s);
        void profileWork(String s);
        boolean requestNearbyPermissions();
        void installPlayServices();
    }

    private static MyInterface mListener;

    private static NearbyInterface nearbyInterface;

    private static FragmentManager fm;
    private static float textSize = 14.0f;

    static LinearLayout prepareOptionMenu(Context c, FragmentManager fragman) {
        mListener = (MyInterface) c;
        nearbyInterface = (NearbyInterface) c;

        fm = fragman;
        LinearLayout menu;
        switch (StaticVariables.whichOptionMenu) {
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

            case "PROFILE":
                menu = createProfileMenu(c);
                break;

            case "FIND":
                menu = createFindSongsMenu(c);
                break;

            case "CHORDS":
                menu = createChordsMenu(c);
                break;

            case "DISPLAY":
                menu = createDisplayMenu(c);
                break;

            case "CONNECT":
                menu = createConnectMenu(c);
                break;

            case "MIDI":
                menu = createMidiMenu(c);
                break;

            case "MODE":
                menu = createModeMenu(c);
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

            case "METRONOME":
                menu = createMetronomeMenu(c);
                break;

            case "CCLI":
                menu = createCCLIMenu(c);
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

    @SuppressLint("InflateParams")
    private static LinearLayout createMainMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (inflater != null) {
            return (LinearLayout) inflater.inflate(R.layout.popup_option, null);
        } else {
            return null;
        }
    }

    @SuppressLint("InflateParams")
    private static LinearLayout createSetMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            return (LinearLayout) inflater.inflate(R.layout.popup_option_set,null);
        } else {
            return null;
        }
    }

    @SuppressLint("InflateParams")
    private static LinearLayout createSongMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            return (LinearLayout) inflater.inflate(R.layout.popup_option_song,null);
        } else {
            return null;
        }
    }

    @SuppressWarnings("InflateParams")
    private static LinearLayout createProfileMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            return (LinearLayout) inflater.inflate(R.layout.popup_option_profile,null);
        } else {
            return null;
        }
    }

    @SuppressLint("InflateParams")
    private static LinearLayout createChordsMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            return (LinearLayout) inflater.inflate(R.layout.popup_option_chords,null);
        } else {
            return null;
        }
    }

    @SuppressLint("InflateParams")
    private static LinearLayout createDisplayMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            return (LinearLayout) inflater.inflate(R.layout.popup_option_display,null);
        } else {
            return null;
        }
    }

    @SuppressLint("InflateParams")
    private static LinearLayout createStorageMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            return (LinearLayout) inflater.inflate(R.layout.popup_option_storage,null);
        } else {
            return null;
        }
    }

    @SuppressLint("InflateParams")
    private static LinearLayout createMidiMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            return (LinearLayout) inflater.inflate(R.layout.popup_option_midi,null);
        } else {
            return null;
        }
    }

    @SuppressLint("InflateParams")
    private static LinearLayout createFindSongsMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            return (LinearLayout) inflater.inflate(R.layout.popup_option_findsongs,null);
        } else {
            return null;
        }
    }

    @SuppressLint("InflateParams")
    private static LinearLayout createConnectMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            return (LinearLayout) inflater.inflate(R.layout.popup_option_connections,null);
        } else {
            return null;
        }
    }

    @SuppressLint("InflateParams")
    private static LinearLayout createModeMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            return (LinearLayout) inflater.inflate(R.layout.popup_option_modes,null);
        } else {
            return null;
        }
    }

    @SuppressLint("InflateParams")
    private static LinearLayout createGesturesMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            return (LinearLayout) inflater.inflate(R.layout.popup_option_gestures,null);
        } else {
            return null;
        }
    }

    @SuppressLint("InflateParams")
    private static LinearLayout createAutoscrollMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            return (LinearLayout) inflater.inflate(R.layout.popup_option_autoscroll,null);
        } else {
            return null;
        }
    }

    @SuppressLint("InflateParams")
    private static LinearLayout createPadMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            return (LinearLayout) inflater.inflate(R.layout.popup_option_pad,null);
        } else {
            return null;
        }
    }

    @SuppressLint("InflateParams")
    private static LinearLayout createMetronomeMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            return (LinearLayout) inflater.inflate(R.layout.popup_option_metronome,null);
        } else {
            return null;
        }
    }

    @SuppressLint("InflateParams")
    private static LinearLayout createCCLIMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            return (LinearLayout) inflater.inflate(R.layout.popup_option_ccli,null);
        } else {
            return null;
        }
    }

    @SuppressLint("InflateParams")
    private static LinearLayout createOtherMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            return (LinearLayout) inflater.inflate(R.layout.popup_option_other,null);
        } else {
            return null;
        }
    }

    static void optionListeners(View v, Context c, Preferences preferences, StorageAccess storageAccess) {

        textSize = preferences.getMyPreferenceFloat(c,"songMenuAlphaIndexSize",14.0f);

        // Decide which listeners we need based on the menu
        switch (StaticVariables.whichOptionMenu) {
            case "MAIN":
            default:
                mainOptionListener(v,c);
                break;

            case "SET":
                setOptionListener(v, c, preferences, storageAccess);
                break;

            case "SONG":
                songOptionListener(v,c,preferences);
                break;

            case "PROFILE":
                profileOptionListener(v,c);
                break;

            case "CHORDS":
                chordOptionListener(v, c, storageAccess, preferences);
                break;

            case "DISPLAY":
                displayOptionListener(v,c);
                break;

            case "FIND":
                findSongsOptionListener(v,c);
                break;

            case "STORAGE":
                storageOptionListener(v, c, preferences);
                break;

            case "CONNECT":
                connectOptionListener(v,c,preferences);
                break;

            case "MIDI":
                midiOptionListener(v,c,preferences);
                break;

            case "MODE":
                modeOptionListener(v,c,preferences);
                break;

            case "AUTOSCROLL":
                autoscrollOptionListener(v,c,preferences);
                break;

            case "PAD":
                padOptionListener(v,c,preferences);
                break;

            case "METRONOME":
                metronomeOptionListener(v,c, preferences);
                break;

            case "CCLI":
                ccliOptionListener(v,c,preferences);
                break;

            case "OTHER":
                otherOptionListener(v, c, preferences);
                break;
        }
    }

    private static void setTextButtons(Button b, String text) {
        b.setTextSize(textSize);
        b.setText(text.toUpperCase(StaticVariables.locale));
    }
    private static void setTextTextView(TextView t, String text) {
        t.setTextSize(textSize);
        t.setText(text.toUpperCase(StaticVariables.locale));
    }

    private static void setTextSwitch(SwitchCompat t, String text) {
        t.setTextSize(textSize);
        t.setText(text.toUpperCase(StaticVariables.locale));
    }

    private static void mainOptionListener(View v, final Context c) {
        mListener = (MyInterface) c;
        // Identify the buttons
        Button menuSetButton = v.findViewById(R.id.menuSetButton);
        Button menuSongButton = v.findViewById(R.id.menuSongButton);
        Button menuProfileButton = v.findViewById(R.id.menuProfileButton);
        Button menuChordsButton = v.findViewById(R.id.menuChordsButton);
        Button menuDisplayButton = v.findViewById(R.id.menuDisplayButton);
        Button menuConnectButton = v.findViewById(R.id.menuConnectButton);
        Button menuModeButton = v.findViewById(R.id.menuModeButton);
        Button menuMidiButton = v.findViewById(R.id.menuMidiButton);
        Button menuFindSongsButton = v.findViewById(R.id.menuFindSongsButton);
        Button menuStorageButton = v.findViewById(R.id.menuStorageButton);
        Button menuPadButton = v.findViewById(R.id.menuPadButton);
        Button menuAutoScrollButton = v.findViewById(R.id.menuAutoScrollButton);
        Button menuMetronomeButton = v.findViewById(R.id.menuMetronomeButton);
        Button menuCCLIButton = v.findViewById(R.id.menuCCLIButton);
        Button menuOtherButton = v.findViewById(R.id.menuOtherButton);
        FloatingActionButton closeOptionsFAB = v.findViewById(R.id.closeOptionsFAB);

        // Capitalise all the text by locale
        setTextButtons(menuSetButton,c.getString(R.string.set));
        setTextButtons(menuSongButton,c.getString(R.string.song));
        setTextButtons(menuProfileButton,c.getString(R.string.profile));
        setTextButtons(menuChordsButton,c.getString(R.string.chords));
        setTextButtons(menuDisplayButton,c.getString(R.string.display));
        setTextButtons(menuConnectButton,c.getString(R.string.connections_connect));
        setTextButtons(menuMidiButton,c.getString(R.string.midi));
        setTextButtons(menuModeButton,c.getString(R.string.choose_app_mode));
        setTextButtons(menuFindSongsButton,c.getString(R.string.findnewsongs));
        setTextButtons(menuStorageButton,c.getString(R.string.storage));
        setTextButtons(menuPadButton,c.getString(R.string.pad));
        setTextButtons(menuAutoScrollButton,c.getString(R.string.autoscroll));
        setTextButtons(menuMetronomeButton,c.getString(R.string.metronome));
        setTextButtons(menuCCLIButton,c.getString(R.string.edit_song_ccli));
        setTextButtons(menuOtherButton,c.getString(R.string.other));

        menuConnectButton.setVisibility(View.VISIBLE);

        // Only allow MIDI menu for Marshmallow+ and if it is available
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            menuMidiButton.setVisibility(View.VISIBLE);
        } else {
            menuMidiButton.setVisibility(View.GONE);
        }

        // Set the listeners
        menuSetButton.setOnClickListener(view -> {
            StaticVariables.whichOptionMenu = "SET";
            if (mListener!=null) {
                mListener.prepareOptionMenu();
            }
        });
        menuSongButton.setOnClickListener(view -> {
            StaticVariables.whichOptionMenu = "SONG";
            if (mListener!=null) {
                mListener.prepareOptionMenu();
            }
        });
        menuProfileButton.setOnClickListener(view -> {
            StaticVariables.whichOptionMenu = "PROFILE";
            if (mListener!=null) {
                mListener.prepareOptionMenu();
            }
        });
        menuChordsButton.setOnClickListener(view -> {
            StaticVariables.whichOptionMenu = "CHORDS";
            if (mListener!=null) {
                mListener.prepareOptionMenu();
            }
        });
        menuDisplayButton.setOnClickListener(view -> {
            StaticVariables.whichOptionMenu = "DISPLAY";
            if (mListener!=null) {
                mListener.prepareOptionMenu();
            }
        });
        menuFindSongsButton.setOnClickListener(view -> {
            StaticVariables.whichOptionMenu = "FIND";
            if (mListener!=null) {
                mListener.prepareOptionMenu();
            }
        });
        menuStorageButton.setOnClickListener(view -> {
            StaticVariables.whichOptionMenu = "STORAGE";
            if (mListener!=null) {
                mListener.prepareOptionMenu();
            }
        });

        if (c.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI)) {
            menuMidiButton.setOnClickListener(view -> {
                StaticVariables.whichOptionMenu = "MIDI";
                if (mListener != null) {
                    mListener.prepareOptionMenu();
                }
            });
        } else {
            menuMidiButton.setOnClickListener(view -> {
                StaticVariables.myToastMessage = "MIDI - " + c.getString(R.string.nothighenoughapi);
                ShowToast.showToast(c);
            });
        }
        menuConnectButton.setOnClickListener(view -> {
            // Check for Google Play availability
            if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(c) == ConnectionResult.SUCCESS) {
                Log.d("d","Success");
                StaticVariables.whichOptionMenu = "CONNECT";
                if (mListener!=null) {
                    mListener.prepareOptionMenu();
                }
            } else {
                mListener.installPlayServices();
            }
        });
        menuModeButton.setOnClickListener(view -> {
            StaticVariables.whichOptionMenu = "MODE";
            if (mListener!=null) {
                mListener.prepareOptionMenu();
            }
        });
        menuAutoScrollButton.setOnClickListener(view -> {
            StaticVariables.whichOptionMenu = "AUTOSCROLL";
            if (mListener!=null) {
                mListener.prepareOptionMenu();
            }
        });
        menuPadButton.setOnClickListener(view -> {
            StaticVariables.whichOptionMenu = "PAD";
            if (mListener!=null) {
                mListener.prepareOptionMenu();
            }
        });
        menuMetronomeButton.setOnClickListener(view -> {
            StaticVariables.whichOptionMenu = "METRONOME";
            if (mListener!=null) {
                mListener.prepareOptionMenu();
            }
        });
        menuCCLIButton.setOnClickListener(view -> {
            StaticVariables.whichOptionMenu = "CCLI";
            if (mListener!=null) {
                mListener.prepareOptionMenu();
            }
        });
        menuOtherButton.setOnClickListener(view -> {
            StaticVariables.whichOptionMenu = "OTHER";
            if (mListener!=null) {
                mListener.prepareOptionMenu();
            }
        });

        closeOptionsFAB.setOnClickListener(view -> {
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
            }
        });

    }

    private static void setOptionListener(View v, final Context c, final Preferences preferences, final StorageAccess storageAccess) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuup = v.findViewById(R.id.setMenuTitle);
        Button setLoadButton = v.findViewById(R.id.setLoadButton);
        Button setSaveButton = v.findViewById(R.id.setSaveButton);
        Button setNewButton = v.findViewById(R.id.setNewButton);
        Button setDeleteButton = v.findViewById(R.id.setDeleteButton);
        Button setOrganiseButton = v.findViewById(R.id.setOrganiseButton);
        Button setImportButton = v.findViewById(R.id.setImportButton);
        Button setExportButton = v.findViewById(R.id.setExportButton);
        Button setCustomButton = v.findViewById(R.id.setCustomButton);
        Button setVariationButton = v.findViewById(R.id.setVariationButton);
        Button setEditButton = v.findViewById(R.id.setEditButton);
        SwitchCompat showSetTickBoxInSongMenu = v.findViewById(R.id.showSetTickBoxInSongMenu);
        //LinearLayout setLinearLayout = v.findViewById(R.id.setLinearLayout);
        FloatingActionButton closeOptionsFAB = v.findViewById(R.id.closeOptionsFAB);

        // Capitalise all the text by locale
        menuup.setText(c.getString(R.string.set).toUpperCase(StaticVariables.locale));
        setTextButtons(setLoadButton,c.getString(R.string.load));
        setTextButtons(setSaveButton,c.getString(R.string.save));
        setTextButtons(setNewButton,c.getString(R.string.set_new));
        setTextButtons(setDeleteButton,c.getString(R.string.delete));
        setTextButtons(setOrganiseButton,c.getString(R.string.managesets));
        setTextButtons(setImportButton,c.getString(R.string.importnewset));
        setTextButtons(setExportButton,c.getString(R.string.export));
        setTextButtons(setCustomButton,c.getString(R.string.add_custom_slide));
        setTextButtons(setVariationButton,c.getString(R.string.customise_set_item));
        setTextButtons(setEditButton,c.getString(R.string.edit));
        setTextButtons(showSetTickBoxInSongMenu,c.getString(R.string.setquickcheck));

        showSetTickBoxInSongMenu.setChecked(preferences.getMyPreferenceBoolean(c,"songMenuSetTicksShow",true));

        // Set the button listeners
        menuup.setOnClickListener(view -> {
            StaticVariables.whichOptionMenu = "MAIN";
            if (mListener!=null) {
                mListener.prepareOptionMenu();
            }
        });

        setLoadButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "loadset";
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });

        setSaveButton.setOnClickListener(view -> {
            String lastSetName = preferences.getMyPreferenceString(c,"setCurrentLastName","");
            Uri settosave = storageAccess.getUriForItem(c, preferences, "Sets", "", lastSetName);
            if (lastSetName==null || lastSetName.equals("")) {
                FullscreenActivity.whattodo = "saveset";
                if (mListener != null) {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            } else if (storageAccess.uriExists(c, settosave)) {
                // Load the are you sure prompt
                FullscreenActivity.whattodo = "saveset";
                String setnamenice = lastSetName.replace("__"," / ");
                String message = c.getResources().getString(R.string.save) + " \"" + setnamenice + "\"?";
                StaticVariables.myToastMessage = message;
                try {
                    DialogFragment newFragment = PopUpAreYouSureFragment.newInstance(message);
                    newFragment.show(fm,message);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                FullscreenActivity.whattodo = "saveset";
                if (mListener != null) {
                    mListener.openFragment();
                }
            }
        });

        setNewButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "clearset";
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });

        setOrganiseButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "managesets";
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });

        setDeleteButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "deleteset";
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });

        setImportButton.setOnClickListener(v1 -> {
            FullscreenActivity.whattodo = "doimportset";
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.selectAFileUri(c.getString(R.string.importnewset));
            }
        });

        setExportButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "exportset";
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });

        setCustomButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "customcreate";
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });

        setVariationButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "setitemvariation";
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });

        setEditButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "editset";
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });

        showSetTickBoxInSongMenu.setOnCheckedChangeListener((compoundButton, b) -> {
            preferences.setMyPreferenceBoolean(c,"songMenuSetTicksShow",b);
            if (mListener!=null) {
                mListener.prepareSongMenu();
            }
        });

        closeOptionsFAB.setOnClickListener(view -> {
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
            }
        });
    }

    private static void songOptionListener(View v, final Context c, final Preferences preferences) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuup = v.findViewById(R.id.songMenuTitle);
        Button songEditButton = v.findViewById(R.id.songEditButton);
        Button songDuplicateButton = v. findViewById(R.id.songDuplicateButton);
        Button songPadButton = v.findViewById(R.id.songPadButton);
        Button songAutoScrollButton = v.findViewById(R.id.songAutoScrollButton);
        Button songMetronomeButton = v.findViewById(R.id.songMetronomeButton);
        Button songStickyButton = v.findViewById(R.id.songStickyButton);
        Button songDrawingButton = v.findViewById(R.id.songDrawingButton);
        Button songChordsButton = v.findViewById(R.id.songChordsButton);
        Button songScoreButton = v.findViewById(R.id.songScoreButton);
        Button songOnYouTubeButton = v.findViewById(R.id.songOnYouTubeButton);
        Button songOnWebButton = v.findViewById(R.id.songOnWebButton);
        Button songLinksButton = v.findViewById(R.id.songLinksButton);
        Button songRenameButton = v.findViewById(R.id.songRenameButton);
        Button songNewButton = v.findViewById(R.id.songNewButton);
        Button songDeleteButton = v.findViewById(R.id.songDeleteButton);
        Button songExportButton = v.findViewById(R.id.songExportButton);
        Button songImportButton = v.findViewById(R.id.songImportButton);
        final SwitchCompat songPresentationOrderButton = v.findViewById(R.id.songPresentationOrderButton);
        SwitchCompat songKeepMultiLineCompactButton = v.findViewById(R.id.songKeepMultiLineCompactButton);
        FloatingActionButton closeOptionsFAB = v.findViewById(R.id.closeOptionsFAB);

        // Capitalise all the text by locale
        menuup.setText(c.getString(R.string.song).toUpperCase(StaticVariables.locale));
        setTextButtons(songPadButton,c.getString(R.string.pad));
        setTextButtons(songAutoScrollButton,c.getString(R.string.autoscroll));
        setTextButtons(songMetronomeButton,c.getString(R.string.metronome));
        setTextButtons(songChordsButton,c.getString(R.string.chords));
        setTextButtons(songLinksButton,c.getString(R.string.link));
        setTextButtons(songDuplicateButton,c.getString(R.string.duplicate));
        setTextButtons(songEditButton,c.getString(R.string.edit));
        setTextButtons(songStickyButton,c.getString(R.string.stickynotes_edit));
        setTextButtons(songDrawingButton,c.getString(R.string.highlight));
        setTextButtons(songScoreButton,c.getString(R.string.music_score));
        setTextButtons(songOnYouTubeButton,c.getString(R.string.youtube));
        setTextButtons(songOnWebButton,c.getString(R.string.websearch));
        setTextButtons(songRenameButton,c.getString(R.string.rename));
        setTextButtons(songNewButton,c.getString(R.string.new_something));
        setTextButtons(songDeleteButton,c.getString(R.string.delete));
        setTextButtons(songImportButton,c.getString(R.string.importnewsong));
        setTextButtons(songExportButton,c.getString(R.string.export));
        setTextButtons(songPresentationOrderButton,c.getString(R.string.edit_song_presentation));
        setTextButtons(songKeepMultiLineCompactButton,c.getString(R.string.keepmultiline));

        // Hide the drawing option unless we are in performance mode
        if (StaticVariables.whichMode.equals("Performance")) {
            songDrawingButton.setVisibility(View.VISIBLE);
        } else {
            songDrawingButton.setVisibility(View.GONE);
        }

        // Set the switches up based on preferences
        songPresentationOrderButton.setChecked(preferences.getMyPreferenceBoolean(c,"usePresentationOrder",false));
        songKeepMultiLineCompactButton.setChecked(preferences.getMyPreferenceBoolean(c,"multiLineVerseKeepCompact",false));

        // Set the button listeners
        menuup.setOnClickListener(view -> {
            StaticVariables.whichOptionMenu = "MAIN";
            if (mListener!=null) {
                mListener.prepareOptionMenu();
            }
        });

        songDuplicateButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "duplicate";
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });

        songPadButton.setOnClickListener(v1 -> {
            if (FullscreenActivity.isSong) {
                FullscreenActivity.whattodo = "page_pad";
                if (mListener != null) {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        songAutoScrollButton.setOnClickListener(v12 -> {
            if (FullscreenActivity.isSong) {
                FullscreenActivity.whattodo = "page_autoscroll";
                if (mListener != null) {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        songMetronomeButton.setOnClickListener(v13 -> {
            if (FullscreenActivity.isSong) {
                FullscreenActivity.whattodo = "page_metronome";
                if (mListener != null) {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        songChordsButton.setOnClickListener(v14 -> {
            if (FullscreenActivity.isSong) {
                FullscreenActivity.whattodo = "page_chords";
                if (mListener != null) {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        songLinksButton.setOnClickListener(v15 -> {
            if (FullscreenActivity.isSong) {
                FullscreenActivity.whattodo = "page_links";
                if (mListener != null) {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        songEditButton.setOnClickListener(view -> {
            if (FullscreenActivity.isSong) {
                FullscreenActivity.whattodo = "editsong";
                if (mListener != null) {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            } else {
                StaticVariables.myToastMessage = c.getString(R.string.not_allowed);
                ShowToast.showToast(c);
            }
        });

        songStickyButton.setOnClickListener(view -> {
            if (FullscreenActivity.isSong) {
                FullscreenActivity.whattodo = "editnotes";
                if (mListener != null) {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            } else {
                StaticVariables.myToastMessage = c.getString(R.string.not_allowed);
                ShowToast.showToast(c);
            }
        });

        songDrawingButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "drawnotes";
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                // Take a snapshot of the songwindow
                mListener.takeScreenShot();
                if (FullscreenActivity.bmScreen!=null) {
                    mListener.openFragment();
                } else {
                    Log.d("OptionMenuListeners", "screenshot is null");
                }
            }
        });

        songScoreButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "abcnotation_edit";
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });

        songOnYouTubeButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "youtube";
            if (mListener != null) {
                Intent youtube = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.youtube.com/results?search_query=" + StaticVariables.mTitle + "+" + StaticVariables.mAuthor));
                mListener.callIntent("web", youtube);
                mListener.closeMyDrawers("option");
            }
        });

        songOnWebButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "websearch";
            if (mListener!=null) {
                Intent web = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.com/search?q=" + StaticVariables.mTitle + "+" + StaticVariables.mAuthor));
                mListener.callIntent("web", web);
                mListener.closeMyDrawers("option");
            }
        });

        songRenameButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "renamesong";
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });

        songNewButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "createsong";
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });

        songDeleteButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "deletesong";
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });

        songImportButton.setOnClickListener(v16 -> {
            FullscreenActivity.whattodo = "doimport";
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.selectAFileUri(c.getString(R.string.importnewsong));
            }
        });

        songExportButton.setOnClickListener(view -> {
            if (FullscreenActivity.isSong) {
                FullscreenActivity.whattodo = "customise_exportsong";
                if (mListener != null) {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            } else {
                StaticVariables.myToastMessage = c.getString(R.string.not_allowed);
                ShowToast.showToast(c);
            }
        });

        songPresentationOrderButton.setOnCheckedChangeListener((compoundButton, b) -> {
            preferences.setMyPreferenceBoolean(c,"usePresentationOrder",b);
            if (FullscreenActivity.isSong) {
                if (mListener != null) {
                    mListener.loadSong();
                }
            }
        });

        songKeepMultiLineCompactButton.setOnCheckedChangeListener((compoundButton, b) -> {
            preferences.setMyPreferenceBoolean(c,"multiLineVerseKeepCompact",b);
            if (FullscreenActivity.isSong) {
                if (mListener != null) {
                    mListener.loadSong();
                }
            }
        });

        closeOptionsFAB.setOnClickListener(view -> {
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
            }
        });

    }

    private static void chordOptionListener(View v, final Context c, final StorageAccess storageAccess, final Preferences preferences) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuup = v.findViewById(R.id.chordsMenuTitle);
        Button chordsButton = v.findViewById(R.id.chordsButton);
        Button chordsTransposeButton = v.findViewById(R.id.chordsTransposeButton);
        Button chordsSharpButton = v.findViewById(R.id.chordsSharpButton);
        Button chordsFlatButton = v.findViewById(R.id.chordsFlatButton);
        SwitchCompat chordsToggleSwitch = v.findViewById(R.id.chordsToggleSwitch);
        SwitchCompat chordsLyricsToggleSwitch = v.findViewById(R.id.chordsLyricsToggleSwitch);
        final SwitchCompat chordsCapoToggleSwitch = v.findViewById(R.id.chordsCapoToggleSwitch);
        final SwitchCompat chordsNativeAndCapoToggleSwitch = v.findViewById(R.id.chordsNativeAndCapoToggleSwitch);
        final SwitchCompat capoAsNumeralsToggleSwitch = v.findViewById(R.id.capoAsNumeralsToggleSwitch);
        SwitchCompat switchCapoTextSize = v.findViewById(R.id.switchCapoTextSize);
        Button chordsFormatButton = v.findViewById(R.id.chordsFormatButton);
        Button chordsConvertButton = v.findViewById(R.id.chordsConvertButton);
        FloatingActionButton closeOptionsFAB = v.findViewById(R.id.closeOptionsFAB);

        // Get text for b button by adjusting the b button to use the unicode character instead
        String newflat = c.getString(R.string.chords_flat).replace(" b "," \u266d ");
        String newsharp = c.getString(R.string.chords_sharp).replace(" # "," \u266f ");

        // Capitalise all the text by locale
        menuup.setText(c.getString(R.string.chords).toUpperCase(StaticVariables.locale));
        setTextButtons(chordsButton,c.getString(R.string.chords));
        setTextButtons(chordsTransposeButton,c.getString(R.string.transpose));
        chordsSharpButton.setTransformationMethod(null);
        chordsFlatButton.setTransformationMethod(null);
        setTextButtons(chordsSharpButton,newsharp);
        setTextButtons(chordsFlatButton,newflat);
        setTextButtons(chordsToggleSwitch,c.getString(R.string.showchords));
        setTextButtons(chordsLyricsToggleSwitch,c.getString(R.string.showlyrics));
        setTextButtons(chordsCapoToggleSwitch,c.getString(R.string.showcapo));
        setTextButtons(chordsNativeAndCapoToggleSwitch,c.getString(R.string.capo_toggle_bothcapo));
        setTextButtons(capoAsNumeralsToggleSwitch,c.getString(R.string.capo_style));
        setTextButtons(switchCapoTextSize,c.getString(R.string.size));
        setTextButtons(chordsFormatButton,c.getString(R.string.choose_chordformat));
        setTextButtons(chordsConvertButton,c.getString(R.string.chord_convert));

        // Set the switches up based on preferences
        if (preferences.getMyPreferenceBoolean(c,"displayChords",true)) {
            chordsToggleSwitch.setChecked(true);
        } else {
            chordsToggleSwitch.setChecked(false);
            chordsCapoToggleSwitch.setEnabled(false);
            chordsNativeAndCapoToggleSwitch.setEnabled(false);
        }

        switchCapoTextSize.setChecked(preferences.getMyPreferenceBoolean(c,"capoLargeFontInfoBar",true));

        chordsLyricsToggleSwitch.setChecked(preferences.getMyPreferenceBoolean(c,"displayLyrics",true));
        boolean capochordsbuttonenabled = preferences.getMyPreferenceBoolean(c,"displayChords",true);
        chordsCapoToggleSwitch.setChecked(preferences.getMyPreferenceBoolean(c,"displayCapoChords",true));
        chordsCapoToggleSwitch.setEnabled(capochordsbuttonenabled);
        if (!capochordsbuttonenabled) {
            chordsCapoToggleSwitch.setAlpha(0.4f);
        }

        boolean nativeandcapobuttonenabled = preferences.getMyPreferenceBoolean(c,"displayChords",true) &&
                capochordsbuttonenabled;
        chordsNativeAndCapoToggleSwitch.setChecked(preferences.getMyPreferenceBoolean(c,"displayCapoAndNativeChords",false));
        chordsNativeAndCapoToggleSwitch.setEnabled(nativeandcapobuttonenabled);
        if (!nativeandcapobuttonenabled) {
            chordsNativeAndCapoToggleSwitch.setAlpha(0.4f);
        }
        capoAsNumeralsToggleSwitch.setChecked(preferences.getMyPreferenceBoolean(c,"capoInfoAsNumerals",false));

        // Set the button listeners
        menuup.setOnClickListener(view -> {
            StaticVariables.whichOptionMenu = "MAIN";
            if (mListener!=null) {
                mListener.prepareOptionMenu();
            }
        });

        chordsButton.setOnClickListener(v1 -> {
            if (FullscreenActivity.isSong) {
                FullscreenActivity.whattodo = "page_chords";
                if (mListener != null) {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        chordsTransposeButton.setOnClickListener(view -> {
            if (FullscreenActivity.isSong) {
                FullscreenActivity.whattodo = "transpose";
                if (mListener != null) {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            } else {
                StaticVariables.myToastMessage = c.getString(R.string.not_allowed);
                ShowToast.showToast(c);
            }
        });

        chordsSharpButton.setOnClickListener(view -> {
            Transpose transpose = new Transpose();
            FullscreenActivity.whattodo = "transpose";
            if (FullscreenActivity.isPDF) {
                // Can't do this action on a pdf!
                StaticVariables.myToastMessage = c.getResources().getString(R.string.pdf_functionnotavailable);
                ShowToast.showToast(c);
            } else if (!FullscreenActivity.isSong) {
                // Editing a slide / note / scripture / image
                StaticVariables.myToastMessage = c.getResources().getString(R.string.not_allowed);
                ShowToast.showToast(c);
            } else {
                StaticVariables.transposeDirection = "0";
                transpose.checkChordFormat(c,preferences);
                if (preferences.getMyPreferenceBoolean(c,"chordFormatUsePreferred",true)) {
                    StaticVariables.detectedChordFormat = preferences.getMyPreferenceInt(c,"chordFormat",1);
                }
                try {
                    transpose.doTranspose(c,storageAccess, preferences, true, false, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (mListener!=null) {
                    mListener.loadSong();
                }
            }
        });

        chordsFlatButton.setOnClickListener(view -> {
            Transpose transpose = new Transpose();
            FullscreenActivity.whattodo = "transpose";
            if (FullscreenActivity.isPDF) {
                // Can't do this action on a pdf!
                StaticVariables.myToastMessage = c.getResources().getString(R.string.pdf_functionnotavailable);
                ShowToast.showToast(c);
            } else if (!FullscreenActivity.isSong) {
                // Editing a slide / note / scripture / image
                StaticVariables.myToastMessage = c.getResources().getString(R.string.not_allowed);
                ShowToast.showToast(c);
            } else {
                StaticVariables.transposeDirection = "0";
                transpose.checkChordFormat(c,preferences);
                if (preferences.getMyPreferenceBoolean(c,"chordFormatUsePreferred",true)) {
                    StaticVariables.detectedChordFormat = preferences.getMyPreferenceInt(c,"chordFormat",1);
                }
                try {
                    transpose.doTranspose(c, storageAccess, preferences, false, true, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (mListener!=null) {
                    mListener.loadSong();
                }
            }
        });

        chordsToggleSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            preferences.setMyPreferenceBoolean(c,"displayChords",b);
            chordsCapoToggleSwitch.setEnabled(b);
            if (!b) {
                chordsCapoToggleSwitch.setAlpha(0.4f);
            } else {
                chordsCapoToggleSwitch.setAlpha(1.0f);
            }

            boolean nativeandcapobuttonenabled1 = preferences.getMyPreferenceBoolean(c,"displayCapoChords",true) && b;
            chordsNativeAndCapoToggleSwitch.setEnabled(nativeandcapobuttonenabled1);
            if (!nativeandcapobuttonenabled1) {
                chordsNativeAndCapoToggleSwitch.setAlpha(0.4f);
            } else {
                chordsNativeAndCapoToggleSwitch.setAlpha(1.0f);
            }

            if (mListener!=null) {
                mListener.loadSong();
            }
        });

        chordsLyricsToggleSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            preferences.setMyPreferenceBoolean(c,"displayLyrics",b);
            if (mListener!=null) {
                mListener.loadSong();
            }
        });

        chordsCapoToggleSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            preferences.setMyPreferenceBoolean(c,"displayCapoChords",b);
            boolean nativeandcapobuttonenabled12 = preferences.getMyPreferenceBoolean(c,"displayChords",true) && b;
            chordsNativeAndCapoToggleSwitch.setEnabled(nativeandcapobuttonenabled12);
            if (!nativeandcapobuttonenabled12) {
                chordsNativeAndCapoToggleSwitch.setAlpha(0.4f);
            } else {
                chordsNativeAndCapoToggleSwitch.setAlpha(1.0f);
            }
            if (mListener!=null) {
                mListener.loadSong();
            }
        });

        chordsNativeAndCapoToggleSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            preferences.setMyPreferenceBoolean(c,"displayCapoAndNativeChords",b);
            if (mListener!=null) {
                mListener.loadSong();
            }
        });

        capoAsNumeralsToggleSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            preferences.setMyPreferenceBoolean(c,"capoInfoAsNumerals",b);
            if (mListener!=null) {
                mListener.loadSong();
            }
        });
        chordsFormatButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "choosechordformat";
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });
        switchCapoTextSize.setOnCheckedChangeListener((compoundButton, b) -> {
            preferences.setMyPreferenceBoolean(c,"capoLargeFontInfoBar",b);
            if (mListener!=null) {
                mListener.updateExtraInfoColorsAndSizes("capo");
            }
        });
        chordsConvertButton.setOnClickListener(view -> {
            Transpose transpose = new Transpose();
            if (FullscreenActivity.isPDF) {
                // Can't do this action on a pdf!
                StaticVariables.myToastMessage = c.getResources().getString(R.string.pdf_functionnotavailable);
                ShowToast.showToast(c);
            } else if (!FullscreenActivity.isSong) {
                // Editing a slide / note / scripture / image
                StaticVariables.myToastMessage = c.getResources().getString(R.string.not_allowed);
                ShowToast.showToast(c);
            } else {
                transpose.convertChords(c,storageAccess,preferences);
            }
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.loadSong();
            }
        });

        closeOptionsFAB.setOnClickListener(view -> {
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
            }
        });
    }

    private static void profileOptionListener(View v, final Context c) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuup = v.findViewById(R.id.optionProfileTitle);
        Button profileLoadButton = v.findViewById(R.id.profileLoadButton);
        Button profileSaveButton = v.findViewById(R.id.profileSaveButton);
        FloatingActionButton closeOptionsFAB = v.findViewById(R.id.closeOptionsFAB);

        // Capitalise all the text by locale
        menuup.setText(c.getString(R.string.profile).toUpperCase(StaticVariables.locale));
        setTextButtons(profileLoadButton,c.getString(R.string.load));
        setTextButtons(profileSaveButton,c.getString(R.string.save));

        // Set the button listeners
        menuup.setOnClickListener(view -> {
            StaticVariables.whichOptionMenu = "MAIN";
            if (mListener!=null) {
                mListener.prepareOptionMenu();
            }
        });
        profileLoadButton.setOnClickListener(view -> {
            if (mListener!=null) {
                mListener.profileWork("load");
            }
        });
        profileSaveButton.setOnClickListener(view -> {

            if (mListener!=null) {
                mListener.profileWork("save");
            }
        });

        closeOptionsFAB.setOnClickListener(view -> {
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
            }
        });
    }

    private static void displayOptionListener(View v, final Context c) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuup = v.findViewById(R.id.optionDisplayTitle);
        Button displayThemeButton = v.findViewById(R.id.displayThemeButton);
        Button displayAutoScaleButton = v.findViewById(R.id.displayAutoScaleButton);
        Button displayFontButton = v.findViewById(R.id.displayFontButton);
        Button displayButtonsButton = v.findViewById(R.id.displayButtonsButton);
        Button displayPopUpsButton = v.findViewById(R.id.displayPopUpsButton);
        Button displayInfoButton = v.findViewById(R.id.displayInfoButton);
        Button displayActionBarButton = v.findViewById(R.id.displayActionBarButton);
        Button displayConnectedDisplayButton = v.findViewById(R.id.displayConnectedDisplayButton);
        Button displayHDMIButton = v.findViewById(R.id.displayHDMIButton);
        FloatingActionButton closeOptionsFAB = v.findViewById(R.id.closeOptionsFAB);

        Button gesturesPedalButton = v.findViewById(R.id.gesturesPedalButton);
        Button gesturesCustomButton = v.findViewById(R.id.gesturesCustomButton);
        Button gesturesMenuOptions = v.findViewById(R.id.gesturesMenuOptions);
        Button gesturesScrollButton = v.findViewById(R.id.gesturesScrollButton);
        //SwitchCompat displayMenuToggleSwitch = v.findViewById(R.id.displayMenuToggleSwitch);
        Button gesturesSongSwipeButton = v.findViewById(R.id.gesturesSongSwipeButton);

        // Capitalise all the text by locale
        menuup.setText(c.getString(R.string.display).toUpperCase(StaticVariables.locale));
        setTextButtons(displayThemeButton,c.getString(R.string.choose_theme));
        setTextButtons(displayAutoScaleButton,c.getString(R.string.autoscale_toggle));
        setTextButtons(displayFontButton,c.getString(R.string.choose_fonts));
        setTextButtons(displayButtonsButton,c.getString(R.string.pagebuttons));
        setTextButtons(displayPopUpsButton,c.getString(R.string.display_popups));
        setTextButtons(displayInfoButton,c.getString(R.string.extra));
        setTextButtons(displayActionBarButton,c.getString(R.string.actionbar));
        setTextButtons(displayConnectedDisplayButton,c.getString(R.string.connected_display));
        setTextButtons(displayHDMIButton,c.getString(R.string.hdmi));

        setTextButtons(gesturesPedalButton,c.getString(R.string.footpedal));
        setTextButtons(gesturesCustomButton,c.getString(R.string.custom_gestures));
        setTextButtons(gesturesMenuOptions,c.getString(R.string.menu_settings));
        setTextButtons(gesturesScrollButton,c.getString(R.string.scrollbuttons));
        setTextButtons(gesturesSongSwipeButton,c.getString(R.string.swipe));

        // Set the switches up based on preferences
        //displayMenuToggleSwitch.setChecked(preferences.getMyPreferenceBoolean(c,"hideActionBar",false));

        // Set the button listeners
        menuup.setOnClickListener(view -> {
            StaticVariables.whichOptionMenu = "MAIN";
            if (mListener!=null) {
                mListener.prepareOptionMenu();
            }
        });

        displayThemeButton.setOnClickListener(view -> {
            if (mListener!=null) {
                FullscreenActivity.whattodo = "changetheme";
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });

        displayAutoScaleButton.setOnClickListener(view -> {
            if (mListener!=null) {
                FullscreenActivity.whattodo = "autoscale";
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });

        displayFontButton.setOnClickListener(view -> {
            if (mListener!=null) {
                FullscreenActivity.whattodo = "changefonts";
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });

        displayButtonsButton.setOnClickListener(view -> {
            if (mListener!=null) {
                FullscreenActivity.whattodo = "pagebuttons";
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });

        displayPopUpsButton.setOnClickListener(view -> {
            if (mListener!=null) {
                FullscreenActivity.whattodo = "popupsettings";
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });

        displayInfoButton.setOnClickListener(view -> {
            if (mListener!=null) {
                FullscreenActivity.whattodo = "extra";
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });

        displayActionBarButton.setOnClickListener(view -> {
            if (mListener!=null) {
                FullscreenActivity.whattodo = "actionbarinfo";
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });

        displayConnectedDisplayButton.setOnClickListener(view -> {
            if (mListener!=null && (FullscreenActivity.isPresenting || FullscreenActivity.isHDMIConnected)) {
                FullscreenActivity.whattodo = "connecteddisplay";
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            } else {
                StaticVariables.myToastMessage = view.getContext().getString(R.string.nodisplays);
                ShowToast.showToast(view.getContext());
            }
        });

        displayHDMIButton.setOnClickListener(view -> {
            if (mListener!=null) {
                StaticVariables.myToastMessage = view.getContext().getString(R.string.connections_searching);
                ShowToast.showToast(view.getContext());
                FullscreenActivity.whattodo = "hdmi";
                mListener.connectHDMI();
            }
        });
        gesturesPedalButton.setOnClickListener(view -> {
            if (mListener!=null) {
                FullscreenActivity.whattodo = "footpedal";
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });
        gesturesCustomButton.setOnClickListener(view -> {
            if (mListener!=null) {
                FullscreenActivity.whattodo = "gestures";
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });
        gesturesMenuOptions.setOnClickListener(view -> {
            if (mListener!=null) {
                FullscreenActivity.whattodo = "menuoptions";
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });
        gesturesScrollButton.setOnClickListener(view -> {
            if (mListener!=null) {
                FullscreenActivity.whattodo = "scrollsettings";
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });
        gesturesSongSwipeButton.setOnClickListener(view -> {
            if (mListener!=null) {
                FullscreenActivity.whattodo = "swipesettings";
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });

        closeOptionsFAB.setOnClickListener(view -> {
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
            }
        });
    }

    private static void findSongsOptionListener(View v, final Context c) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuup = v.findViewById(R.id.findSongMenuTitle);
        Button ugSearchButton = v.findViewById(R.id.ugSearchButton);
        Button chordieSearchButton = v.findViewById(R.id.chordieSearchButton);
        Button songselectSearchButton = v.findViewById(R.id.songselectSearchButton);
        Button worshiptogetherSearchButton = v.findViewById(R.id.worshiptogetherSearchButton);
        Button worshipreadySearchButton = v.findViewById(R.id.worshipreadySearchButton);
        Button ukutabsSearchButton = v.findViewById(R.id.ukutabsSearchButton);
        Button holychordsSearchButton = v.findViewById(R.id.holychordsSearchButton);
        Button bandDownloadButton = v.findViewById(R.id.bandDownloadButton);
        Button churchDownloadButton = v.findViewById(R.id.churchDownloadButton);
        Button songImportButton = v.findViewById(R.id.songImportButton);
        Button cameraButton = v.findViewById(R.id.cameraButton);
        FloatingActionButton closeOptionsFAB = v.findViewById(R.id.closeOptionsFAB);

        worshipreadySearchButton.setVisibility(View.GONE);

        // Capitalise all the text by locale
        menuup.setText(c.getString(R.string.findnewsongs).toUpperCase(StaticVariables.locale));
        setTextButtons(ugSearchButton,c.getString(R.string.ultimateguitarsearch));
        setTextButtons(chordieSearchButton,c.getString(R.string.chordiesearch));
        setTextButtons(songselectSearchButton,c.getString(R.string.songselect)+ " " + c.getString(R.string.subscription));
        setTextButtons(worshiptogetherSearchButton,c.getString(R.string.worshiptogether)+ " " + c.getString(R.string.subscription));
        setTextButtons(worshipreadySearchButton,c.getString(R.string.worshipready)+ " " + c.getString(R.string.subscription));
        setTextButtons(ukutabsSearchButton,c.getString(R.string.ukutabs));
        setTextButtons(holychordsSearchButton,c.getString(R.string.holychords));
        setTextButtons(bandDownloadButton,c.getString(R.string.my_band));
        setTextButtons(churchDownloadButton,c.getString(R.string.my_church));
        setTextButtons(songImportButton,c.getString(R.string.importnewsong));
        setTextButtons(cameraButton,c.getString(R.string.camera));

        // Set the button listeners
        menuup.setOnClickListener(view -> {
            StaticVariables.whichOptionMenu = "MAIN";
            if (mListener!=null) {
                mListener.prepareOptionMenu();
            }
        });

        ugSearchButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "ultimate-guitar";
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });
        chordieSearchButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "chordie";
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });
        worshiptogetherSearchButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "worshiptogether";
            if (mListener != null) {
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });
        worshipreadySearchButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "worshipready";
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });
        songselectSearchButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "songselect";
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });
        ukutabsSearchButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "ukutabs";
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });
        holychordsSearchButton.setOnClickListener(v1 -> {
            FullscreenActivity.whattodo = "holychords";
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });
        bandDownloadButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "download_band";
            StaticVariables.myToastMessage = c.getString(R.string.wait);
            ShowToast.showToast(c);
            if (mListener!=null) {
                mListener.doDownload("https://drive.google.com/uc?export=download&id=0B-GbNhnY_O_leDR5bFFjRVVxVjA");
                mListener.closeMyDrawers("option");
            }
        });
        churchDownloadButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "download_church";
            StaticVariables.myToastMessage = c.getString(R.string.wait);
            ShowToast.showToast(c);
            if (mListener!=null) {
                mListener.doDownload("https://drive.google.com/uc?export=download&id=0B-GbNhnY_O_lbVY3VVVOMkc5OGM");
                mListener.closeMyDrawers("option");
            }
        });
        songImportButton.setOnClickListener(v12 -> {
            FullscreenActivity.whattodo = "doimport";
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.selectAFileUri(c.getString(R.string.importnewsong));
            }
        });
        cameraButton.setOnClickListener(view -> {

            if (mListener!=null) {
                mListener.useCamera();
            }
       });

        closeOptionsFAB.setOnClickListener(view -> {
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
            }
        });

    }

    private static void storageOptionListener(View v, final Context c, final Preferences preferences) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuup = v.findViewById(R.id.optionStorageTitle);
        Button storageNewFolderButton = v.findViewById(R.id.storageNewFolderButton);
        Button storageEditButton = v.findViewById(R.id.storageEditButton);
        Button storageManageButton = v.findViewById(R.id.storageManageButton);
        Button exportSongListButton = v.findViewById(R.id.exportSongListButton);
        Button storageImportOSBButton = v.findViewById(R.id.storageImportOSBButton);
        Button storageExportOSBButton = v.findViewById(R.id.storageExportOSBButton);
        Button storageImportOnSongButton = v.findViewById(R.id.storageImportOnSongButton);
        Button storageSongMenuButton = v.findViewById(R.id.storageSongMenuButton);
        Button storageDatabaseButton = v.findViewById(R.id.storageDatabaseButton);
        Button storageLogButton = v.findViewById(R.id.storageLogButton);
        FloatingActionButton closeOptionsFAB = v.findViewById(R.id.closeOptionsFAB);

        // Capitalise all the text by locale
        menuup.setText(c.getString(R.string.storage).toUpperCase(StaticVariables.locale));
        setTextButtons(storageNewFolderButton,c.getString(R.string.folder_new));
        setTextButtons(storageEditButton,c.getString(R.string.folder_rename));
        setTextButtons(storageManageButton,c.getString(R.string.storage_choose));
        setTextButtons(exportSongListButton,c.getString(R.string.exportsongdirectory));
        setTextButtons(storageImportOSBButton,c.getString(R.string.backup_import));
        setTextButtons(storageExportOSBButton,c.getString(R.string.backup_export));
        setTextButtons(storageImportOnSongButton,c.getString(R.string.import_onsong_choose));
        setTextButtons(storageSongMenuButton,c.getString(R.string.refreshsongs));
        setTextButtons(storageDatabaseButton,c.getString(R.string.search_rebuild));
        setTextButtons(storageLogButton,c.getString(R.string.search_log));

        // Set the button listeners
        menuup.setOnClickListener(view -> {
            StaticVariables.whichOptionMenu = "MAIN";
            if (mListener!=null) {
                mListener.prepareOptionMenu();
            }
        });
        storageNewFolderButton.setOnClickListener(view -> {
            if (mListener!=null) {
                FullscreenActivity.whattodo = "newfolder";
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });
        storageEditButton.setOnClickListener(view -> {
            if (mListener!=null) {
                FullscreenActivity.whattodo = "editfoldername";
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });
        storageManageButton.setOnClickListener(view -> {
            if (mListener!=null) {
                preferences.setMyPreferenceInt(c, "lastUsedVersion", 0);
                mListener.closeMyDrawers("option");
                mListener.splashScreen();
            }
        });

        exportSongListButton.setOnClickListener(view -> {
            if (mListener!=null) {
                FullscreenActivity.whattodo = "exportsonglist";
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });

        storageImportOSBButton.setOnClickListener(view -> {
            if (mListener!=null) {
                FullscreenActivity.whattodo = "processimportosb";
                mListener.selectAFileUri(c.getString(R.string.backup_import));
                mListener.closeMyDrawers("option");
            }
        });

        storageExportOSBButton.setOnClickListener(view -> {
            if (mListener!=null) {
                FullscreenActivity.whattodo = "exportosb";
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });

        storageImportOnSongButton.setOnClickListener(view -> {
            if (mListener!=null) {
                FullscreenActivity.whattodo = "importos";
                mListener.closeMyDrawers("option");
                mListener.selectAFileUri(c.getString(R.string.import_onsong_choose));
            }
        });

        storageSongMenuButton.setOnClickListener(view -> {
            if (mListener!=null) {
                mListener.prepareSongMenu();
                mListener.closeMyDrawers("option");
                mListener.openMyDrawers("song");
                mListener.closeMyDrawers("song_delayed");
            }
        });

        storageDatabaseButton.setOnClickListener(view -> {
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.rebuildSearchIndex();
            }
        });

        storageLogButton.setOnClickListener(view -> {
            if (mListener!=null) {
                FullscreenActivity.whattodo = "errorlog";
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });

        closeOptionsFAB.setOnClickListener(view -> {
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
            }
        });
    }

    private static void connectOptionListener(View v, final Context c, final Preferences preferences) {
        mListener = (MyInterface) c;
        nearbyInterface = (NearbyInterface) c;

        // Identify the buttons
        TextView menuUp = v.findViewById(R.id.connectionsMenuTitle);

        // We keep a static reference to these in the FullscreenActivity
        Button deviceName = v.findViewById(R.id.deviceName);
        SwitchCompat actAsHost = v.findViewById(R.id.actAsHost);
        actAsHost.requestFocus();
        SwitchCompat nearbyHostMenuOnly = v.findViewById(R.id.nearbyHostMenuOnly);
        SwitchCompat enableNearby = v.findViewById(R.id.enableNearby);
        SwitchCompat receiveHostFiles = v.findViewById(R.id.receiveHostFiles);
        SwitchCompat keepHostFiles = v.findViewById(R.id.keepHostFiles);
        connectionLog = v.findViewById(R.id.options_connections_log);
        deviceName.setText(StaticVariables.deviceName);
        if (StaticVariables.connectionLog==null || StaticVariables.connectionLog.isEmpty()) {
            StaticVariables.connectionLog = c.getResources().getString(R.string.connections_log) + "\n\n";
        }

        setTextTextView(connectionLog,StaticVariables.connectionLog);
        setTextTextView(deviceName,StaticVariables.deviceName);
        setTextSwitch(actAsHost,c.getResources().getString(R.string.connections_actashost));
        setTextSwitch(nearbyHostMenuOnly,c.getResources().getString(R.string.nearby_host_menu_only));
        setTextSwitch(enableNearby,c.getResources().getString(R.string.connections_enable));
        setTextSwitch(receiveHostFiles,c.getResources().getString(R.string.connections_receive_host));
        setTextSwitch(keepHostFiles,c.getResources().getString(R.string.connections_keephostsongs));
        setTextTextView(menuUp,c.getResources().getString(R.string.connections_connect));
        FloatingActionButton closeOptionsFAB = v.findViewById(R.id.closeOptionsFAB);

        // Set the default values
        enableNearby.setChecked(StaticVariables.usingNearby);
        actAsHost.setChecked(StaticVariables.isHost);
        nearbyHostMenuOnly.setChecked(preferences.getMyPreferenceBoolean(c,"nearbyHostMenuOnly",false));
        nearbyHostMenuOnly.setEnabled(StaticVariables.isHost);
        receiveHostFiles.setChecked(StaticVariables.receiveHostFiles);
        receiveHostFiles.setEnabled(!StaticVariables.isHost);
        keepHostFiles.setChecked(StaticVariables.keepHostFiles);
        keepHostFiles.setEnabled(!StaticVariables.isHost);

        // Set the listeners
        menuUp.setOnClickListener(view -> {
            StaticVariables.whichOptionMenu = "MAIN";
            if (mListener!=null) {
                mListener.prepareOptionMenu();
            }
        });
        closeOptionsFAB.setOnClickListener(view -> {
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
            }
        });

        enableNearby.setOnCheckedChangeListener((view,isChecked) -> {
            StaticVariables.usingNearby = isChecked;
            StaticVariables.isHost = actAsHost.isChecked();
            if (isChecked) {
                if (StaticVariables.isHost) {
                    nearbyInterface.startAdvertising();
                } else {
                    nearbyInterface.startDiscovery();
                }
            } else {
                if (StaticVariables.isHost) {
                    try {
                        nearbyInterface.stopAdvertising();
                    } catch (Exception e) {
                        Log.d("OptionMenuListener", "Can't stop advertising, probably wasn't a host!");
                    }
                } else {
                    try {
                        nearbyInterface.stopDiscovery();
                    } catch (Exception e) {
                        Log.d("OptionMenuListener", "Can't stop discovery, probably wasn't discovering");
                    }
                }
                // IV - We have been asked to turn off  nearby
                nearbyInterface.turnOffNearby();
            }
        });
        actAsHost.setOnCheckedChangeListener((view,isChecked) -> {
            StaticVariables.isHost = isChecked;
            receiveHostFiles.setEnabled(!isChecked);
            keepHostFiles.setEnabled(!isChecked);

            // Hosts can choose to listen for new clients only when the menu is open
            nearbyHostMenuOnly.setEnabled(isChecked);

            // Disable the switches that are for clients only
            receiveHostFiles.setEnabled(!isChecked);
            keepHostFiles.setEnabled(!isChecked);

            // If we are already connected, that's fine, we stay connected
            // However, we need to switch between advertising/discovery.  This is just a reset.
            // First we stop advertising/discovery
            try {
                nearbyInterface.stopAdvertising();
            } catch (Exception e) {
                Log.d("OptionMenuListener","Can't stop advertising, probably wasn't a host!");
            }
            try {
                nearbyInterface.stopDiscovery();
            } catch (Exception e) {
                Log.d("OptionMenuListener","Can't stop discovery, probably wasn't discovering");
            }

            // If we have chosen to be the host, we will automatically turn on the enable nearby switch
            // This starts advertising as part of that switch's logic

            if (isChecked && !enableNearby.isChecked()) {
                enableNearby.setChecked(true);
            } else if (isChecked) {
                // If the nearby switch is already on, just start advertising
                nearbyInterface.startAdvertising();
            } else if (!isChecked && enableNearby.isChecked()) {
                // If we have stopped being the host but was already using nearby, start discovery
                nearbyInterface.startDiscovery();
            }

        });
        nearbyHostMenuOnly.setOnCheckedChangeListener((View,isChecked) -> preferences.setMyPreferenceBoolean(c,"nearbyHostMenuOnly",isChecked));
        receiveHostFiles.setOnCheckedChangeListener((view,isChecked) -> {
            StaticVariables.receiveHostFiles = isChecked;
            keepHostFiles.setEnabled(isChecked);
        });
        keepHostFiles.setOnCheckedChangeListener((view,isChecked) -> StaticVariables.keepHostFiles = isChecked);

        deviceName.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "connect_name";
            if (mListener!=null) {
                mListener.openFragment();
            }
        });
        connectionLog.setOnClickListener(view -> {
            StaticVariables.connectionLog = c.getResources().getString(R.string.connections_log) + "\n\n";
            setTextTextView(connectionLog,StaticVariables.connectionLog);
        });



        if (!mListener.requestNearbyPermissions()) {
            StaticVariables.whichOptionMenu = "MAIN";
            mListener.closeMyDrawers("option");
        }
    }

    private static void midiOptionListener(View v, final Context c, final Preferences preferences) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuUp = v.findViewById(R.id.midiMenuTitle);

        Button midiBluetooth = v.findViewById(R.id.midiBluetooth);
        Button midiUSB = v.findViewById(R.id.midiUSB);
        Button midiCommands = v.findViewById(R.id.midiCommands);
        Button midiSend = v.findViewById(R.id.midiSend);
        SwitchCompat midiAuto = v.findViewById(R.id.midiAuto);
        FloatingActionButton closeOptionsFAB = v.findViewById(R.id.closeOptionsFAB);

        // Capitalise all the text by locale
        menuUp.setText(c.getString(R.string.midi).toUpperCase(StaticVariables.locale));
        setTextButtons(midiBluetooth,c.getString(R.string.midi_bluetooth));
        setTextButtons(midiUSB,c.getString(R.string.midi_usb));
        setTextButtons(midiCommands,c.getString(R.string.midi_commands));
        setTextButtons(midiSend,c.getString(R.string.midi_send));
        setTextButtons(midiAuto,c.getString(R.string.midi_auto));

        // Set the default
        midiAuto.setChecked(preferences.getMyPreferenceBoolean(c,"midiSendAuto",false));

        // Set the button listeners
        menuUp.setOnClickListener(view -> {
            StaticVariables.whichOptionMenu = "MAIN";
            if (mListener!=null) {
                mListener.prepareOptionMenu();
            }
        });
        midiBluetooth.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "bluetoothmidi";
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });
        midiUSB.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "usbmidi";
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });
        midiCommands.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "midicommands";
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });
        midiSend.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "showmidicommands";
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });
        midiAuto.setOnCheckedChangeListener((compoundButton, b) -> preferences.setMyPreferenceBoolean(c,"midiSendAuto",b));
        closeOptionsFAB.setOnClickListener(view -> {
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
            }
        });
    }

    private static void modeOptionListener(View v, final Context c, final Preferences preferences) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuUp = v.findViewById(R.id.modeMenuTitle);
        Button modePerformanceButton = v.findViewById(R.id.modePerformanceButton);
        Button modeStageButton = v.findViewById(R.id.modeStageButton);
        Button modePresentationButton = v.findViewById(R.id.modePresentationButton);
        FloatingActionButton closeOptionsFAB = v.findViewById(R.id.closeOptionsFAB);
        // ImageView connectionsStatusImage = (ImageView) v.findViewById(R.id.connectionsStatusImage);

        // Capitalise all the text by locale
        menuUp.setText(c.getString(R.string.choose_app_mode).toUpperCase(StaticVariables.locale));
        setTextButtons(modePerformanceButton,c.getString(R.string.performancemode));
        setTextButtons(modeStageButton,c.getString(R.string.stagemode));
        setTextButtons(modePresentationButton,c.getString(R.string.presentermode));

        // Set a tick next to the current mode
        switch (StaticVariables.whichMode) {
            case "Performance":
                modePerformanceButton.setEnabled(false);
                modeStageButton.setEnabled(true);
                modePresentationButton.setEnabled(true);
                break;

            case "Stage":
                modePerformanceButton.setEnabled(true);
                modeStageButton.setEnabled(false);
                modePresentationButton.setEnabled(true);
                break;

            case "Presentation":
                modePerformanceButton.setEnabled(true);
                modeStageButton.setEnabled(true);
                modePresentationButton.setEnabled(false);
                break;

        }
        // Set the button listeners
        menuUp.setOnClickListener(view -> {
            StaticVariables.whichOptionMenu = "MAIN";
            if (mListener!=null) {
                mListener.prepareOptionMenu();
            }
        });
        modePerformanceButton.setOnClickListener(view -> {
            if (!StaticVariables.whichMode.equals("Performance")) {
                // Switch to performance mode
                StaticVariables.whichMode = "Performance";
                preferences.setMyPreferenceString(c,"whichMode","Performance");
                Intent performmode = new Intent();
                performmode.setClass(c, StageMode.class);
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                    mListener.callIntent("activity", performmode);
                }
            }
        });
        modeStageButton.setOnClickListener(view -> {
            if (!StaticVariables.whichMode.equals("Stage")) {
                // Switch to stage mode
                StaticVariables.whichMode = "Stage";
                preferences.setMyPreferenceString(c,"whichMode","Stage");
                Intent stagemode = new Intent();
                stagemode.setClass(c, StageMode.class);
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                    mListener.callIntent("activity", stagemode);
                }
            }
        });
        modePresentationButton.setOnClickListener(view -> {
            if (!StaticVariables.whichMode.equals("Presentation")) {
                // Switch to presentation mode
                StaticVariables.whichMode = "Presentation";
                preferences.setMyPreferenceString(c,"whichMode","Presentation");
                Intent presentmode = new Intent();
                presentmode.setClass(c, PresenterMode.class);
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                    mListener.callIntent("activity", presentmode);
                }
            }
        });

        closeOptionsFAB.setOnClickListener(view -> {
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
            }
        });

    }

    private static void autoscrollOptionListener(View v, final Context c, final Preferences preferences) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuup = v.findViewById(R.id.optionAutoScrollTitle);
        Button autoScrollButton = v.findViewById(R.id.autoScrollButton);
        Button autoScrollTimeDefaultsButton = v.findViewById(R.id.autoScrollTimeDefaultsButton);
        Button autoScrollLearnButton = v.findViewById(R.id.autoScrollLearnButton);
        SwitchCompat switchTimerSize = v.findViewById(R.id.switchTimerSize);
        SwitchCompat autoScrollStartButton = v.findViewById(R.id.autoScrollStartButton);
        SwitchCompat autoscrollActivatedSwitch = v.findViewById(R.id.autoscrollActivatedSwitch);
        FloatingActionButton closeOptionsFAB = v.findViewById(R.id.closeOptionsFAB);

        // Capitalise all the text by locale
        menuup.setText(c.getString(R.string.autoscroll).toUpperCase(StaticVariables.locale));
        setTextButtons(autoScrollButton,c.getString(R.string.autoscroll));
        setTextButtons(autoScrollTimeDefaultsButton,c.getString(R.string.default_autoscroll));
        setTextButtons(autoScrollStartButton,c.getString(R.string.autostart_autoscroll));
        setTextButtons(autoscrollActivatedSwitch,c.getString(R.string.activated));
        setTextButtons(switchTimerSize,c.getString(R.string.timer_size));
        setTextButtons(autoScrollLearnButton,c.getString(R.string.timer_learn));

        // Set the switches up based on preferences
        autoScrollStartButton.setChecked(preferences.getMyPreferenceBoolean(c,"autoscrollAutoStart",false));
        autoscrollActivatedSwitch.setChecked(StaticVariables.clickedOnAutoScrollStart);

        switchTimerSize.setChecked(preferences.getMyPreferenceBoolean(c,"autoscrollLargeFontInfoBar",true));

        // Set the button listeners
        menuup.setOnClickListener(view -> {
            StaticVariables.whichOptionMenu = "MAIN";
            if (mListener!=null) {
                mListener.prepareOptionMenu();
            }
        });
        autoScrollButton.setOnClickListener(v1 -> {
            if (FullscreenActivity.isSong) {
                FullscreenActivity.whattodo = "page_autoscroll";
                if (mListener != null) {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });
        autoscrollActivatedSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            // gesture contains the start/stop logic.
            if (mListener != null) {
                // Request a start/stop to get to the desired state
                if ((!StaticVariables.isautoscrolling && b) || (StaticVariables.isautoscrolling && !b)) {
                    StaticVariables.doVibrateActive = false;
                    mListener.gesture5();
                }
            }
        });
        autoScrollTimeDefaultsButton.setOnClickListener(view -> {
            if (mListener!=null) {
                FullscreenActivity.whattodo = "autoscrolldefaults";
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });

        autoScrollStartButton.setOnCheckedChangeListener((compoundButton, b) -> preferences.setMyPreferenceBoolean(c,"autoscrollAutoStart",b));

        autoScrollLearnButton.setOnClickListener(view -> {
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.prepareLearnAutoScroll();
            }
        });
        switchTimerSize.setOnCheckedChangeListener((compoundButton, b) -> {
            preferences.setMyPreferenceBoolean(c,"autoscrollLargeFontInfoBar",b);
            if (mListener!=null) {
                mListener.updateExtraInfoColorsAndSizes("autoscroll");
            }
        });
        closeOptionsFAB.setOnClickListener(view -> {
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
            }
        });
    }

    private static void padOptionListener(View v, final Context c, final Preferences preferences) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuup = v.findViewById(R.id.optionPadTitle);
        Button padButton = v.findViewById(R.id.padButton);
        Button padCrossFadeButton = v.findViewById(R.id.padCrossFadeButton);
        Button padCustomButton = v.findViewById(R.id.padCustomButton);
        FloatingActionButton closeOptionsFAB = v.findViewById(R.id.closeOptionsFAB);
        SwitchCompat switchTimerSize = v.findViewById(R.id.switchTimerSize);
        SwitchCompat padStartButton = v.findViewById(R.id.padStartButton);
        SwitchCompat padActivatedSwitch = v.findViewById(R.id.padActivatedSwitch);


        // Capitalise all the text by locale
        menuup.setText(c.getString(R.string.pad).toUpperCase(StaticVariables.locale));
        setTextButtons(padButton,c.getString(R.string.pad));
        setTextButtons(padStartButton,c.getString(R.string.autostartpad));
        setTextButtons(padCustomButton,c.getString(R.string.custom));
        setTextButtons(padCrossFadeButton,c.getString(R.string.crossfade_time));
        setTextButtons(padActivatedSwitch,c.getString(R.string.activated));
        setTextButtons(switchTimerSize,c.getString(R.string.timer_size));

        // Set the switch
        switchTimerSize.setChecked(preferences.getMyPreferenceBoolean(c,"padLargeFontInfoBar",true));

        padStartButton.setChecked(preferences.getMyPreferenceBoolean(c,"padAutoStart",false));
        padActivatedSwitch.setChecked(StaticVariables.clickedOnPadStart);
        // Set the button listeners
        menuup.setOnClickListener(view -> {
            StaticVariables.whichOptionMenu = "MAIN";
            if (mListener!=null) {
                mListener.prepareOptionMenu();
            }
        });
        padButton.setOnClickListener(v1 -> {
            if (FullscreenActivity.isSong) {
                FullscreenActivity.whattodo = "page_pad";
                if (mListener != null) {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });
        padStartButton.setOnCheckedChangeListener((compoundButton, b) -> preferences.setMyPreferenceBoolean(c,"padAutoStart",b));
        padCustomButton.setOnClickListener(view -> {
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                FullscreenActivity.whattodo = "custompads";
                mListener.openFragment();
            }
        });
        padCrossFadeButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "crossfade";
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });
        closeOptionsFAB.setOnClickListener(view -> {
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
            }
        });
        switchTimerSize.setOnCheckedChangeListener((compoundButton, b) -> {
            // IV - Correction to preference name
            preferences.setMyPreferenceBoolean(c,"padLargeFontInfoBar",b);
            if (mListener!=null) {
                mListener.updateExtraInfoColorsAndSizes("pad");
            }
        });
        padActivatedSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            // gesture contains the start/stop logic
            if (mListener != null) {
                    mListener.gesture6();
            }
        });

    }

    private static void metronomeOptionListener(View v, final Context c, final Preferences preferences) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuup = v.findViewById(R.id.optionMetronomeTitle);
        Button metronomeButton = v.findViewById(R.id.metronomeButton);
        Button metronomeLengthButton = v.findViewById(R.id.metronomeLengthButton);
        SwitchCompat metronomeStartButton = v.findViewById(R.id.metronomeStartButton);
        SwitchCompat metronomeActivatedSwitch = v.findViewById(R.id.metronomeActivatedSwitch);
        FloatingActionButton closeOptionsFAB = v.findViewById(R.id.closeOptionsFAB);

        int val = preferences.getMyPreferenceInt(c,"metronomeLength",0);
        String str;
        if (val==0) {
            str = c.getString(R.string.metronome_duration) + ": " + c.getString(R.string.continuous);
        } else {
            str = c.getString(R.string.metronome_duration) + ": "+val;
        }

        // Capitalise all the text by locale
        menuup.setText(c.getString(R.string.metronome).toUpperCase(StaticVariables.locale));
        setTextButtons(metronomeButton,c.getString(R.string.metronome));
        setTextButtons(metronomeLengthButton,str);
        setTextButtons(metronomeActivatedSwitch,c.getString(R.string.activated));
        setTextButtons(metronomeStartButton,c.getString(R.string.autostartmetronome));

        // Set the switches up based on preferences
        metronomeStartButton.setChecked(preferences.getMyPreferenceBoolean(c,"metronomeAutoStart",false));
        metronomeActivatedSwitch.setChecked(StaticVariables.clickedOnMetronomeStart);

        // Set the button listeners
        menuup.setOnClickListener(view -> {
            StaticVariables.whichOptionMenu = "MAIN";
            if (mListener!=null) {
                mListener.prepareOptionMenu();
            }
        });

        metronomeButton.setOnClickListener(v1 -> {
            if (FullscreenActivity.isSong) {
                FullscreenActivity.whattodo = "page_metronome";
                if (mListener != null) {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });
        metronomeLengthButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "page_metronome";
            if (mListener!=null) {
                try {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                    mListener.prepareOptionMenu();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        metronomeStartButton.setOnCheckedChangeListener((compoundButton, b) -> preferences.setMyPreferenceBoolean(c,"metronomeAutoStart",b));
        metronomeActivatedSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            // gesture contains the start/stop logic
            if (mListener != null) {
                mListener.gesture7();
            }
        });
        closeOptionsFAB.setOnClickListener(view -> {
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
            }
        });
    }

    private static void ccliOptionListener(View v, final Context c, final Preferences preferences) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuup = v.findViewById(R.id.optionCCLITitle);
        Button ccliChurchButton = v.findViewById(R.id.ccliChurchButton);
        Button ccliLicenceButton = v.findViewById(R.id.ccliLicenceButton);
        SwitchCompat ccliAutoButton = v.findViewById(R.id.ccliAutoButton);
        Button ccliViewButton = v.findViewById(R.id.ccliViewButton);
        Button ccliExportButton = v.findViewById(R.id.ccliExportButton);
        Button ccliResetButton = v.findViewById(R.id.ccliResetButton);
        FloatingActionButton closeOptionsFAB = v.findViewById(R.id.closeOptionsFAB);

        String mcname = preferences.getMyPreferenceString(c,"ccliChurchName","");
        String mcnum  = preferences.getMyPreferenceString(c,"ccliLicence","");
        if (!mcname.isEmpty()) {
            mcname = "\n"+mcname;
        }
        if (!mcnum.isEmpty()) {
            mcnum = "\n"+mcnum;
        }
        String cname = c.getString(R.string.ccli_church).toUpperCase(StaticVariables.locale) + mcname;
        String clice = c.getString(R.string.ccli_licence).toUpperCase(StaticVariables.locale) + mcnum;

        // Capitalise all the text by locale
        menuup.setText(c.getString(R.string.edit_song_ccli).toUpperCase(StaticVariables.locale));

        setTextButtons(ccliChurchButton,cname);
        setTextButtons(ccliLicenceButton,clice);
        setTextButtons(ccliAutoButton,c.getString(R.string.ccli_automatic));
        setTextButtons(ccliViewButton,c.getString(R.string.ccli_view));
        setTextButtons(ccliExportButton,c.getString(R.string.export));
        setTextButtons(ccliResetButton,c.getString(R.string.ccli_reset));

        // Set the switches up based on preferences
        ccliAutoButton.setChecked(preferences.getMyPreferenceBoolean(c,"ccliAutomaticLogging",false));

        // Set the button listeners
        menuup.setOnClickListener(view -> {
            StaticVariables.whichOptionMenu = "MAIN";
            if (mListener!=null) {
                mListener.prepareOptionMenu();
            }
        });

        ccliAutoButton.setOnCheckedChangeListener((compoundButton, b) -> preferences.setMyPreferenceBoolean(c,"ccliAutomaticLogging",b));
        ccliChurchButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "ccli_church";
            if (mListener!=null) {
                mListener.openFragment();
            }
        });
        ccliLicenceButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "ccli_licence";
            if (mListener!=null) {
                mListener.openFragment();
            }
        });
        ccliViewButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "ccli_view";
            if (mListener!=null) {
                mListener.openFragment();
            }
        });
        ccliExportButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "ccli_export";
            if (mListener!=null) {
                mListener.doExport();
            }
        });
        ccliResetButton.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "ccli_reset";
            if (mListener!=null) {
                mListener.openFragment();
            }
        });
        closeOptionsFAB.setOnClickListener(view -> {
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
            }
        });
    }

    private static void otherOptionListener(View v, final Context c, final Preferences preferences) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuup = v.findViewById(R.id.optionOtherTitle);
        Button otherHelpButton = v.findViewById(R.id.otherHelpButton);
        Button otherTweetButton = v.findViewById(R.id.otherTweetButton);
        Button otherLanguageButton = v.findViewById(R.id.otherLanguageButton);
        Button otherStartButton = v.findViewById(R.id.otherStartButton);
        Button otherRateButton = v.findViewById(R.id.otherRateButton);
        Button otherPayPalButton = v.findViewById(R.id.otherPayPalButton);
        Button otherEmailButton = v.findViewById(R.id.otherEmailButton);
        FloatingActionButton closeOptionsFAB = v.findViewById(R.id.closeOptionsFAB);

        // Capitalise all the text by locale
        menuup.setText(c.getString(R.string.other).toUpperCase(StaticVariables.locale));
        setTextButtons(otherHelpButton,c.getString(R.string.help));
        setTextButtons(otherTweetButton,c.getString(R.string.twitteruser));
        setTextButtons(otherLanguageButton,c.getString(R.string.language));
        setTextButtons(otherStartButton,c.getString(R.string.start_screen));
        setTextButtons(otherRateButton,c.getString(R.string.rate));
        setTextButtons(otherEmailButton,c.getString(R.string.forum));
        setTextButtons(otherPayPalButton,c.getString(R.string.paypal));

        // Set the button listeners
        menuup.setOnClickListener(view -> {
            StaticVariables.whichOptionMenu = "MAIN";
            if (mListener!=null) {
                mListener.prepareOptionMenu();
            }
        });
        otherHelpButton.setOnClickListener(view -> {
            String url = "https://www.opensongapp.com/user-guide";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.callIntent("web",i);
            }
        });
        otherTweetButton.setOnClickListener(view -> {
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.callIntent("twitter",null);
            }
        });
        otherEmailButton.setOnClickListener(view -> {
            String url = "https://www.opensongapp.com/forum";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
                mListener.callIntent("web",i);
            }
        });
        otherLanguageButton.setOnClickListener(view -> {
            if (mListener!=null) {
                FullscreenActivity.whattodo = "language";
                mListener.closeMyDrawers("option");
                mListener.openFragment();
            }
        });

        otherStartButton.setOnClickListener(view -> {
            if (mListener!=null) {
                // Set the last used version to 1
                // Setting to 0 is now only for fresh installs
                preferences.setMyPreferenceInt(c, "lastUsedVersion", 1);
                mListener.closeMyDrawers("option");
                mListener.splashScreen();
            }
        });
        otherRateButton.setOnClickListener(view -> {
            if (mListener!=null) {
                // Rate this app
                String appPackage = c.getPackageName();
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackage));
                mListener.closeMyDrawers("option");
                mListener.callIntent("web", i);
            }
        });
        otherPayPalButton.setOnClickListener(view -> {
            if (mListener!=null) {
                // PayPal.Me
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.me/opensongapp"));
                mListener.closeMyDrawers("option");
                mListener.callIntent("web", i);
            }
        });
        closeOptionsFAB.setOnClickListener(view -> {
            if (mListener!=null) {
                mListener.closeMyDrawers("option");
            }
        });
    }

    public static void updateMenuVersionNumber(Context c, TextView showVersion) {
        // Update the app version in the menu
        PackageInfo pinfo;
        int versionNumber = 0;
        String versionName = "?";
        try {
            pinfo = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            versionNumber = pinfo.versionCode;
            versionName = pinfo.versionName;
        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
        }

        if (!versionName.equals("?") && versionNumber > 0) {
            String temptext = "V" + versionName + " (" + versionNumber + ")";
            if (showVersion != null) {
                showVersion.setVisibility(View.VISIBLE);
                showVersion.setText(temptext);
            }
        } else {
            if (showVersion != null) {
                showVersion.setVisibility(View.GONE);
            }
        }
    }
}