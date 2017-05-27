package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Callbacks.SalutDeviceCallback;
import com.peak.salut.SalutDevice;

import java.io.IOException;

public class OptionMenuListeners extends Activity {

    public interface MyInterface {
        void openFragment();
        void openMyDrawers(String which);
        void closeMyDrawers(String which);
        void refreshActionBar();
        void loadSong();
        void shareSong();
        void prepareSongMenu();
        void rebuildSearchIndex();
        void callIntent(String what, Intent intent);
        void prepareOptionMenu();
        void findSongInFolders();
        void removeSongFromSet(int val);
        void splashScreen();
        void showActionBar();
        void hideActionBar();
        void useCamera();
    }

    public static MyInterface mListener;

    @SuppressWarnings("all")
    public static LinearLayout prepareOptionMenu(Context c) {
        mListener = (MyInterface) c;
        LinearLayout menu;
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

            case "OTHER":
                menu = createOtherMenu(c);
                break;

        }
        if (mListener!=null) {
            mListener.refreshActionBar();
        }
        return menu;
    }

    @SuppressWarnings("all")
    public static LinearLayout createMainMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return (LinearLayout) inflater.inflate(R.layout.popup_option,null);
    }

    @SuppressWarnings("all")
    public static LinearLayout createSetMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return (LinearLayout) inflater.inflate(R.layout.popup_option_set,null);
    }

    @SuppressWarnings("all")
    public static LinearLayout createSongMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return (LinearLayout) inflater.inflate(R.layout.popup_option_song,null);
    }

    @SuppressWarnings("all")
    public static LinearLayout createChordsMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return (LinearLayout) inflater.inflate(R.layout.popup_option_chords,null);
    }

    @SuppressWarnings("all")
    public static LinearLayout createDisplayMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return (LinearLayout) inflater.inflate(R.layout.popup_option_display,null);
    }

    @SuppressWarnings("all")
    public static LinearLayout createStorageMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return (LinearLayout) inflater.inflate(R.layout.popup_option_storage,null);
    }

    @SuppressWarnings("all")
    public static LinearLayout createFindSongsMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return (LinearLayout) inflater.inflate(R.layout.popup_option_findsongs,null);
    }

    @SuppressWarnings("all")
    public static LinearLayout createConnectMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return (LinearLayout) inflater.inflate(R.layout.popup_option_connections,null);
    }

    @SuppressWarnings("all")
    public static LinearLayout createModeMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return (LinearLayout) inflater.inflate(R.layout.popup_option_modes,null);
    }

    @SuppressWarnings("all")
    public static LinearLayout createGesturesMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return (LinearLayout) inflater.inflate(R.layout.popup_option_gestures,null);
    }

    @SuppressWarnings("all")
    public static LinearLayout createAutoscrollMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return (LinearLayout) inflater.inflate(R.layout.popup_option_autoscroll,null);
    }

    @SuppressWarnings("all")
    public static LinearLayout createPadMenu(Context c) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return (LinearLayout) inflater.inflate(R.layout.popup_option_pad,null);
    }

    @SuppressWarnings("all")
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

            case "FIND":
                findSongsOptionListener(v,c);
                break;

            case "STORAGE":
                storageOptionListener(v,c);
                break;

            case "CONNECT":
                connectOptionListener(v,c);
                break;

            case "MODE":
                modeOptionListener(v,c);
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
        Button menuConnectButton = (Button) v.findViewById(R.id.menuConnectButton);
        Button menuModeButton = (Button) v.findViewById(R.id.menuModeButton);
        Button menuFindSongsButton = (Button) v.findViewById(R.id.menuFindSongsButton);
        Button menuStorageButton = (Button) v.findViewById(R.id.menuStorageButton);
        Button menuPadButton = (Button) v.findViewById(R.id.menuPadButton);
        Button menuAutoScrollButton = (Button) v.findViewById(R.id.menuAutoScrollButton);
        Button menuOtherButton = (Button) v.findViewById(R.id.menuOtherButton);
        FloatingActionButton closeOptionsFAB = (FloatingActionButton) v.findViewById(R.id.closeOptionsFAB);

        // Capitalise all the text by locale
        menuSetButton.setText(c.getString(R.string.options_set).toUpperCase(FullscreenActivity.locale));
        menuSongButton.setText(c.getString(R.string.options_song).toUpperCase(FullscreenActivity.locale));
        menuChordsButton.setText(c.getString(R.string.chords).toUpperCase(FullscreenActivity.locale));
        menuDisplayButton.setText(c.getString(R.string.options_display).toUpperCase(FullscreenActivity.locale));
        menuGesturesButton.setText(c.getString(R.string.options_gesturesandmenus).toUpperCase(FullscreenActivity.locale));
        menuConnectButton.setText(c.getString(R.string.options_connections).toUpperCase(FullscreenActivity.locale));
        menuModeButton.setText(c.getString(R.string.options_modes).toUpperCase(FullscreenActivity.locale));
        menuFindSongsButton.setText(c.getString(R.string.findnewsongs).toUpperCase(FullscreenActivity.locale));
        menuStorageButton.setText(c.getString(R.string.options_storage).toUpperCase(FullscreenActivity.locale));
        menuPadButton.setText(c.getString(R.string.pad).toUpperCase(FullscreenActivity.locale));
        menuAutoScrollButton.setText(c.getString(R.string.autoscroll).toUpperCase(FullscreenActivity.locale));
        menuOtherButton.setText(c.getString(R.string.options_other).toUpperCase(FullscreenActivity.locale));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            menuConnectButton.setVisibility(View.VISIBLE);
        } else {
            menuConnectButton.setVisibility(View.GONE);
        }

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
        menuFindSongsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whichOptionMenu = "FIND";
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
        menuConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whichOptionMenu = "CONNECT";
                if (mListener!=null) {
                    mListener.prepareOptionMenu();
                }
            }
        });
        menuModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whichOptionMenu = "MODE";
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

        closeOptionsFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                }
            }
        });

    }

    public static void setOptionListener(View v, final Context c) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuup = (TextView) v.findViewById(R.id.setMenuTitle);
        Button setLoadButton = (Button) v.findViewById(R.id.setLoadButton);
        Button setSaveButton = (Button) v.findViewById(R.id.setSaveButton);
        Button setNewButton = (Button) v.findViewById(R.id.setNewButton);
        Button setDeleteButton = (Button) v.findViewById(R.id.setDeleteButton);
        Button setOrganiseButton = (Button) v.findViewById(R.id.setOrganiseButton);
        Button setExportButton = (Button) v.findViewById(R.id.setExportButton);
        Button setCustomButton = (Button) v.findViewById(R.id.setCustomButton);
        Button setVariationButton = (Button) v.findViewById(R.id.setVariationButton);
        Button setEditButton = (Button) v.findViewById(R.id.setEditButton);
        SwitchCompat showSetTickBoxInSongMenu = (SwitchCompat) v.findViewById(R.id.showSetTickBoxInSongMenu);
        LinearLayout setLinearLayout = (LinearLayout) v.findViewById(R.id.setLinearLayout);
        FloatingActionButton closeOptionsFAB = (FloatingActionButton) v.findViewById(R.id.closeOptionsFAB);

        // Capitalise all the text by locale
        menuup.setText(c.getString(R.string.options_set).toUpperCase(FullscreenActivity.locale));
        setLoadButton.setText(c.getString(R.string.options_set_load).toUpperCase(FullscreenActivity.locale));
        setSaveButton.setText(c.getString(R.string.options_set_save).toUpperCase(FullscreenActivity.locale));
        setNewButton.setText(c.getString(R.string.options_set_clear).toUpperCase(FullscreenActivity.locale));
        setDeleteButton.setText(c.getString(R.string.options_set_delete).toUpperCase(FullscreenActivity.locale));
        setOrganiseButton.setText(c.getString(R.string.managesets).toUpperCase(FullscreenActivity.locale));
        setExportButton.setText(c.getString(R.string.options_set_export).toUpperCase(FullscreenActivity.locale));
        setCustomButton.setText(c.getString(R.string.add_custom_slide).toUpperCase(FullscreenActivity.locale));
        setVariationButton.setText(c.getString(R.string.customise_set_item).toUpperCase(FullscreenActivity.locale));
        setEditButton.setText(c.getString(R.string.options_set_edit).toUpperCase(FullscreenActivity.locale));
        showSetTickBoxInSongMenu.setText(c.getString(R.string.setquickcheck).toUpperCase(FullscreenActivity.locale));

        showSetTickBoxInSongMenu.setChecked(FullscreenActivity.showSetTickBoxInSongMenu);

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

        setOrganiseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "managesets";
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

        showSetTickBoxInSongMenu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.showSetTickBoxInSongMenu = b;
                Preferences.savePreferences();
                if (mListener!=null) {
                    mListener.prepareSongMenu();
                }
            }
        });

        closeOptionsFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
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
                        FullscreenActivity.setView = true;
                        FullscreenActivity.pdfPageCurrent = 0;
                        FullscreenActivity.linkclicked = FullscreenActivity.mSetList[val];
                        FullscreenActivity.indexSongInSet = val;
                        SetActions.songIndexClickInSet();
                        SetActions.getSongFileAndFolder(c);
                        if (mListener!=null) {
                            mListener.closeMyDrawers("option");
                            mListener.loadSong();
                        }
                    }
                });
                tv.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        FullscreenActivity.linkclicked = FullscreenActivity.mSetList[val];
                        if (mListener!=null) {
                            mListener.removeSongFromSet(val);
                        }
                        return false;
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
        Button songOnYouTubeButton = (Button) v.findViewById(R.id.songOnYouTubeButton);
        Button songOnWebButton = (Button) v.findViewById(R.id.songOnWebButton);
        Button songRenameButton = (Button) v.findViewById(R.id.songRenameButton);
        Button songNewButton = (Button) v.findViewById(R.id.songNewButton);
        Button songDeleteButton = (Button) v.findViewById(R.id.songDeleteButton);
        Button songExportButton = (Button) v.findViewById(R.id.songExportButton);
        final SwitchCompat songPresentationOrderButton = (SwitchCompat) v.findViewById(R.id.songPresentationOrderButton);
        FloatingActionButton closeOptionsFAB = (FloatingActionButton) v.findViewById(R.id.closeOptionsFAB);

        // Capitalise all the text by locale
        menuup.setText(c.getString(R.string.options_song).toUpperCase(FullscreenActivity.locale));
        songEditButton.setText(c.getString(R.string.options_song_edit).toUpperCase(FullscreenActivity.locale));
        songStickyButton.setText(c.getString(R.string.options_song_stickynotes).toUpperCase(FullscreenActivity.locale));
        songOnYouTubeButton.setText(c.getString(R.string.youtube).toUpperCase(FullscreenActivity.locale));
        songOnWebButton.setText(c.getString(R.string.websearch).toUpperCase(FullscreenActivity.locale));
        songRenameButton.setText(c.getString(R.string.options_song_rename).toUpperCase(FullscreenActivity.locale));
        songNewButton.setText(c.getString(R.string.options_song_new).toUpperCase(FullscreenActivity.locale));
        songDeleteButton.setText(c.getString(R.string.options_song_delete).toUpperCase(FullscreenActivity.locale));
        songExportButton.setText(c.getString(R.string.options_song_export).toUpperCase(FullscreenActivity.locale));
        songPresentationOrderButton.setText(c.getString(R.string.edit_song_presentation).toUpperCase(FullscreenActivity.locale));

        // Set the switches up based on preferences
        songPresentationOrderButton.setChecked(FullscreenActivity.usePresentationOrder);

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

        songOnYouTubeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "youtube";
                if (mListener!=null) {
                    Intent youtube = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.youtube.com/results?search_query=" + FullscreenActivity.mTitle + "+" + FullscreenActivity.mAuthor));
                    mListener.callIntent("web", youtube);
                    mListener.closeMyDrawers("option");
                }
            }
        });

        songOnWebButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "websearch";
                if (mListener!=null) {
                    Intent web = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.google.com/search?q=" + FullscreenActivity.mTitle + "+" + FullscreenActivity.mAuthor));
                    mListener.callIntent("web", web);
                    mListener.closeMyDrawers("option");
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

        closeOptionsFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                }
            }
        });

    }

    public static void chordOptionListener(View v, final Context c) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuup = (TextView) v.findViewById(R.id.chordsMenuTitle);
        Button chordsTransposeButton = (Button) v.findViewById(R.id.chordsTransposeButton);
        Button chordsSharpButton = (Button) v.findViewById(R.id.chordsSharpButton);
        Button chordsFlatButton = (Button) v.findViewById(R.id.chordsFlatButton);
        SwitchCompat chordsToggleSwitch = (SwitchCompat) v.findViewById(R.id.chordsToggleSwitch);
        SwitchCompat chordsLyricsToggleSwitch = (SwitchCompat) v.findViewById(R.id.chordsLyricsToggleSwitch);
        final SwitchCompat chordsCapoToggleSwitch = (SwitchCompat) v.findViewById(R.id.chordsCapoToggleSwitch);
        final SwitchCompat chordsNativeAndCapoToggleSwitch = (SwitchCompat) v.findViewById(R.id.chordsNativeAndCapoToggleSwitch);
        Button chordsFormatButton = (Button) v.findViewById(R.id.chordsFormatButton);
        Button chordsConvertButton = (Button) v.findViewById(R.id.chordsConvertButton);
        FloatingActionButton closeOptionsFAB = (FloatingActionButton) v.findViewById(R.id.closeOptionsFAB);

        // Capitalise all the text by locale
        menuup.setText(c.getString(R.string.chords).toUpperCase(FullscreenActivity.locale));
        chordsTransposeButton.setText(c.getString(R.string.options_song_transpose).toUpperCase(FullscreenActivity.locale));
        chordsSharpButton.setText(c.getString(R.string.options_song_sharp).toUpperCase(FullscreenActivity.locale));
        String temp = c.getString(R.string.options_song_flat).replace("b","#");
        temp = temp.toUpperCase(FullscreenActivity.locale);
        temp = temp.replace("#","\u266d");
        chordsFlatButton.setTransformationMethod(null);
        chordsFlatButton.setText(temp);
        chordsToggleSwitch.setText(c.getString(R.string.showchords).toUpperCase(FullscreenActivity.locale));
        chordsLyricsToggleSwitch.setText(c.getString(R.string.showlyrics).toUpperCase(FullscreenActivity.locale));
        chordsCapoToggleSwitch.setText(c.getString(R.string.showcapo).toUpperCase(FullscreenActivity.locale));
        chordsNativeAndCapoToggleSwitch.setText(c.getString(R.string.capo_toggle_bothcapo).toUpperCase(FullscreenActivity.locale));
        chordsFormatButton.setText(c.getString(R.string.options_options_chordformat).toUpperCase(FullscreenActivity.locale));
        chordsConvertButton.setText(c.getString(R.string.options_song_convert).toUpperCase(FullscreenActivity.locale));

        // Set the switches up based on preferences
        if (FullscreenActivity.showChords) {
            chordsToggleSwitch.setChecked(true);
        } else {
            chordsToggleSwitch.setChecked(false);
            chordsCapoToggleSwitch.setEnabled(false);
            chordsNativeAndCapoToggleSwitch.setEnabled(false);
        }
        if (FullscreenActivity.showLyrics) {
            chordsLyricsToggleSwitch.setChecked(true);
        } else {
            chordsLyricsToggleSwitch.setChecked(false);
        }
        boolean capochordsbuttonenabled = FullscreenActivity.showChords;
        chordsCapoToggleSwitch.setChecked(FullscreenActivity.showCapoChords);
        chordsCapoToggleSwitch.setEnabled(capochordsbuttonenabled);
        if (!capochordsbuttonenabled) {
            chordsCapoToggleSwitch.setAlpha(0.4f);
        }

        boolean nativeandcapobuttonenabled = FullscreenActivity.showChords && capochordsbuttonenabled;
        chordsNativeAndCapoToggleSwitch.setChecked(FullscreenActivity.showNativeAndCapoChords);
        chordsNativeAndCapoToggleSwitch.setEnabled(nativeandcapobuttonenabled);
        if (!nativeandcapobuttonenabled) {
            chordsNativeAndCapoToggleSwitch.setAlpha(0.4f);
        }

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

        chordsTransposeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "transpose";
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        chordsSharpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "transpose";
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
                    Preferences.savePreferences();
                    FullscreenActivity.switchsharpsflats = false;
                    if (mListener!=null) {
                        mListener.loadSong();
                    }
                }
            }
        });

        chordsFlatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "transpose";
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
                    Preferences.savePreferences();
                    FullscreenActivity.switchsharpsflats = false;
                    if (mListener!=null) {
                        mListener.loadSong();
                    }
                }
            }
        });

        chordsToggleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.showChords = b;
                chordsCapoToggleSwitch.setEnabled(b);
                if (!b) {
                    chordsCapoToggleSwitch.setAlpha(0.4f);
                } else {
                    chordsCapoToggleSwitch.setAlpha(1.0f);
                }

                boolean nativeandcapobuttonenabled = FullscreenActivity.showCapoChords && b;
                chordsNativeAndCapoToggleSwitch.setEnabled(nativeandcapobuttonenabled);
                if (!nativeandcapobuttonenabled) {
                    chordsNativeAndCapoToggleSwitch.setAlpha(0.4f);
                } else {
                    chordsNativeAndCapoToggleSwitch.setAlpha(1.0f);
                }

                Preferences.savePreferences();
                if (mListener!=null) {
                    mListener.loadSong();
                }
            }
        });

        chordsLyricsToggleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.showLyrics = b;
                Preferences.savePreferences();
                if (mListener!=null) {
                    mListener.loadSong();
                }
            }
        });

        chordsCapoToggleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.showCapoChords = b;
                boolean nativeandcapobuttonenabled = FullscreenActivity.showChords && b;
                chordsNativeAndCapoToggleSwitch.setEnabled(nativeandcapobuttonenabled);
                if (!nativeandcapobuttonenabled) {
                    chordsNativeAndCapoToggleSwitch.setAlpha(0.4f);
                } else {
                    chordsNativeAndCapoToggleSwitch.setAlpha(1.0f);
                }
                Preferences.savePreferences();
                if (mListener!=null) {
                    mListener.loadSong();
                }
            }
        });

        chordsNativeAndCapoToggleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.showNativeAndCapoChords = b;
                Preferences.savePreferences();
                if (mListener!=null) {
                    mListener.loadSong();
                }
            }
        });
        chordsFormatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "choosechordformat";
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        chordsConvertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                    Transpose.checkChordFormat();
                    try {
                        Transpose.doTranspose();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (mListener!=null) {
                    mListener.loadSong();
                }
            }
        });

        closeOptionsFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                }
            }
        });
    }

    public static void displayOptionListener(View v, final Context c) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuup = (TextView) v.findViewById(R.id.optionDisplayTitle);
        Button displayThemeButton = (Button) v.findViewById(R.id.displayThemeButton);
        Button displayAutoScaleButton = (Button) v.findViewById(R.id.displayAutoScaleButton);
        Button displayFontButton = (Button) v.findViewById(R.id.displayFontButton);
        Button displayButtonsButton = (Button) v.findViewById(R.id.displayButtonsButton);
        Button displayPopUpsButton = (Button) v.findViewById(R.id.displayPopUpsButton);
        Button displayInfoButton = (Button) v.findViewById(R.id.displayInfoButton);
        Button displayProfileButton = (Button) v.findViewById(R.id.displayProfileButton);
        Button displayConnectedDisplayButton = (Button) v.findViewById(R.id.displayConnectedDisplayButton);
        FloatingActionButton closeOptionsFAB = (FloatingActionButton) v.findViewById(R.id.closeOptionsFAB);

        // Capitalise all the text by locale
        menuup.setText(c.getString(R.string.options_display).toUpperCase(FullscreenActivity.locale));
        displayThemeButton.setText(c.getString(R.string.options_options_theme).toUpperCase(FullscreenActivity.locale));
        displayAutoScaleButton.setText(c.getString(R.string.options_options_scale).toUpperCase(FullscreenActivity.locale));
        displayFontButton.setText(c.getString(R.string.options_options_fonts).toUpperCase(FullscreenActivity.locale));
        displayButtonsButton.setText(c.getString(R.string.pagebuttons).toUpperCase(FullscreenActivity.locale));
        displayPopUpsButton.setText(c.getString(R.string.options_display_popups).toUpperCase(FullscreenActivity.locale));
        displayInfoButton.setText(c.getString(R.string.extra).toUpperCase(FullscreenActivity.locale));
        displayProfileButton.setText(c.getString(R.string.profile).toUpperCase(FullscreenActivity.locale));
        displayConnectedDisplayButton.setText(c.getString(R.string.connected_display).toUpperCase(FullscreenActivity.locale));

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

        displayThemeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    FullscreenActivity.whattodo = "changetheme";
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        displayAutoScaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    FullscreenActivity.whattodo = "autoscale";
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        displayFontButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    FullscreenActivity.whattodo = "changefonts";
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        displayButtonsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    FullscreenActivity.whattodo = "pagebuttons";
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        displayPopUpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    FullscreenActivity.whattodo = "popupsettings";
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        displayInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    FullscreenActivity.whattodo = "extra";
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        displayProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    FullscreenActivity.whattodo = "profiles";
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        displayConnectedDisplayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null && FullscreenActivity.isPresenting) {
                    FullscreenActivity.whattodo = "connecteddisplay";
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                } else {
                    FullscreenActivity.myToastMessage = view.getContext().getString(R.string.nodisplays);
                    ShowToast.showToast(view.getContext());
                }
            }
        });

        closeOptionsFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                }
            }
        });

    }

    public static void findSongsOptionListener(View v, final Context c) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuup = (TextView) v.findViewById(R.id.findSongMenuTitle);
        Button ugSearchButton = (Button) v.findViewById(R.id.ugSearchButton);
        Button chordieSearchButton = (Button) v.findViewById(R.id.chordieSearchButton);
        Button worshipreadySearchButton = (Button) v.findViewById(R.id.worshipreadySearchButton);
        Button cameraButton = (Button) v.findViewById(R.id.cameraButton);
        FloatingActionButton closeOptionsFAB = (FloatingActionButton) v.findViewById(R.id.closeOptionsFAB);

        worshipreadySearchButton.setVisibility(View.GONE);

        // Capitalise all the text by locale
        menuup.setText(c.getString(R.string.findnewsongs).toUpperCase(FullscreenActivity.locale));
        ugSearchButton.setText(c.getString(R.string.ultimateguitarsearch).toUpperCase(FullscreenActivity.locale));
        chordieSearchButton.setText(c.getString(R.string.chordiesearch).toUpperCase(FullscreenActivity.locale));
        String wr = c.getString(R.string.worshipready) + " " + c.getString(R.string.subscription);
        worshipreadySearchButton.setText(wr.toUpperCase(FullscreenActivity.locale));
        cameraButton.setText(c.getString(R.string.camera).toUpperCase(FullscreenActivity.locale));

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

        ugSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "ultimate-guitar";
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });
        chordieSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "chordie";
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });
        worshipreadySearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "worshipready";
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mListener!=null) {
                    mListener.useCamera();
                }
           }
        });

        closeOptionsFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                }
            }
        });

    }

    public static void storageOptionListener(View v, final Context c) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuup = (TextView) v.findViewById(R.id.optionStorageTitle);
        Button storageNewFolderButton = (Button) v.findViewById(R.id.storageNewFolderButton);
        Button storageEditButton = (Button) v.findViewById(R.id.storageEditButton);
        Button storageManageButton = (Button) v.findViewById(R.id.storageManageButton);
        Button exportSongListButton = (Button) v.findViewById(R.id.exportSongListButton);
        Button storageImportOSBButton = (Button) v.findViewById(R.id.storageImportOSBButton);
        Button storageExportOSBButton = (Button) v.findViewById(R.id.storageExportOSBButton);
        Button storageImportOnSongButton = (Button) v.findViewById(R.id.storageImportOnSongButton);
        Button storageSongMenuButton = (Button) v.findViewById(R.id.storageSongMenuButton);
        Button storageDatabaseButton = (Button) v.findViewById(R.id.storageDatabaseButton);
        Button storageLogButton = (Button) v.findViewById(R.id.storageLogButton);
        FloatingActionButton closeOptionsFAB = (FloatingActionButton) v.findViewById(R.id.closeOptionsFAB);

        // Capitalise all the text by locale
        menuup.setText(c.getString(R.string.options_storage).toUpperCase(FullscreenActivity.locale));
        storageNewFolderButton.setText(c.getString(R.string.options_song_newfolder).toUpperCase(FullscreenActivity.locale));
        storageEditButton.setText(c.getString(R.string.options_song_editfolder).toUpperCase(FullscreenActivity.locale));
        storageManageButton.setText(c.getString(R.string.storage_choose).toUpperCase(FullscreenActivity.locale));
        exportSongListButton.setText(c.getString(R.string.exportsongdirectory).toUpperCase(FullscreenActivity.locale));
        storageImportOSBButton.setText(c.getString(R.string.backup_import).toUpperCase(FullscreenActivity.locale));
        storageExportOSBButton.setText(c.getString(R.string.backup_export).toUpperCase(FullscreenActivity.locale));
        storageImportOnSongButton.setText(c.getString(R.string.import_onsong_choose).toUpperCase(FullscreenActivity.locale));
        storageSongMenuButton.setText(c.getString(R.string.refreshsongs).toUpperCase(FullscreenActivity.locale));
        storageDatabaseButton.setText(c.getString(R.string.search_rebuild).toUpperCase(FullscreenActivity.locale));
        storageLogButton.setText(c.getString(R.string.search_log).toUpperCase(FullscreenActivity.locale));

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

        storageNewFolderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    FullscreenActivity.whattodo = "newfolder";
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        storageEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    FullscreenActivity.whattodo = "editfoldername";
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        storageManageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    FullscreenActivity.whattodo = "managestorage";
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        exportSongListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    FullscreenActivity.whattodo = "exportsonglist";
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        storageImportOSBButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    //FullscreenActivity.whattodo = "importosb";
                    FullscreenActivity.filechosen = null;
                    FullscreenActivity.whattodo = "processimportosb";
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        storageExportOSBButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*FullscreenActivity.myToastMessage = c.getResources().getString(R.string.wait);
                ShowToast.showToast(c);
                ExportPreparer.createOpenSongBackup(c);*/
                if (mListener!=null) {
                    FullscreenActivity.whattodo = "exportosb";
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        storageImportOnSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    FullscreenActivity.whattodo = "importos";
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        storageSongMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    mListener.prepareSongMenu();
                    mListener.closeMyDrawers("option");
                    mListener.openMyDrawers("song");
                    mListener.closeMyDrawers("song_delayed");
                }
            }
        });

        storageDatabaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                    mListener.rebuildSearchIndex();
                }
            }
        });

        storageLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    FullscreenActivity.whattodo = "errorlog";
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        closeOptionsFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                }
            }
        });
    }

    public static void connectOptionListener(View v, final Context c) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuUp = (TextView) v.findViewById(R.id.connectionsMenuTitle);

        // We keep a static reference to these in the FullscreenActivity
        FullscreenActivity.hostButton = (Button) v.findViewById(R.id.connectionsHostButton);
        FullscreenActivity.clientButton = (Button) v.findViewById(R.id.connectionsGuestButton);
        SwitchCompat connectionsReceiveHostFile = (SwitchCompat) v.findViewById(R.id.connectionsReceiveHostFile);
        FullscreenActivity.connectionsLog = (TextView) v.findViewById(R.id.options_connections_log);

        if (FullscreenActivity.salutLog==null || FullscreenActivity.salutLog.equals("")) {
            FullscreenActivity.salutLog = c.getResources().getString(R.string.options_connections_log) + "\n\n";
        }
        FullscreenActivity.connectionsLog.setText(FullscreenActivity.salutLog);

        FloatingActionButton closeOptionsFAB = (FloatingActionButton) v.findViewById(R.id.closeOptionsFAB);

        // Capitalise all the text by locale
        connectionsReceiveHostFile.setText(c.getResources().getString(R.string.options_connections_receive_host).toUpperCase(FullscreenActivity.locale));
        menuUp.setText(c.getString(R.string.options_connections).toUpperCase(FullscreenActivity.locale));
        if (FullscreenActivity.hostButtonText==null || FullscreenActivity.hostButtonText.equals("")) {
            FullscreenActivity.hostButtonText = c.getResources().getString(R.string.options_connections_service_start).toUpperCase(FullscreenActivity.locale);
        }
        FullscreenActivity.hostButton.setText(FullscreenActivity.hostButtonText);
        if (FullscreenActivity.clientButtonText==null || FullscreenActivity.clientButtonText.equals("")) {
            FullscreenActivity.clientButtonText = c.getResources().getString(R.string.options_connections_discover_start).toUpperCase(FullscreenActivity.locale);
        }
        FullscreenActivity.clientButton.setText(FullscreenActivity.clientButtonText);

        connectionsReceiveHostFile.setChecked(FullscreenActivity.receiveHostFiles);
        
        // Set the button listeners
        menuUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whichOptionMenu = "MAIN";
                if (mListener!=null) {
                    mListener.prepareOptionMenu();
                }
            }
        });
        connectionsReceiveHostFile.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.receiveHostFiles = b;
            }
        });
        FullscreenActivity.connectionsLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.salutLog = c.getResources().getString(R.string.options_connections_log) + "\n\n";
                FullscreenActivity.connectionsLog.setText(FullscreenActivity.salutLog);
            }
        });
        FullscreenActivity.hostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupNetwork(c);
            }
        });
        FullscreenActivity.clientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discoverServices(c);
            }
        });
        closeOptionsFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                }
            }
        });

    }

    public static void setupNetwork(final Context c) {
        if(!FullscreenActivity.network.isRunningAsHost) {
            try {
                FullscreenActivity.network.startNetworkService(new SalutDeviceCallback() {
                    @Override
                    public void call(SalutDevice salutDevice) {
                        FullscreenActivity.myToastMessage = salutDevice.readableName + " - " +
                                c.getResources().getString(R.string.options_connections_success);
                        FullscreenActivity.salutLog += "\n" + FullscreenActivity.myToastMessage;
                        FullscreenActivity.connectionsLog.setText(FullscreenActivity.salutLog);
                        ShowToast.showToast(c);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            FullscreenActivity.hostButtonText = c.getResources().getString(R.string.options_connections_service_stop).toUpperCase(FullscreenActivity.locale);
            FullscreenActivity.hostButton.setText(FullscreenActivity.hostButtonText);
            FullscreenActivity.clientButton.setAlpha(0.5f);
            FullscreenActivity.clientButton.setClickable(false);
            FullscreenActivity.myToastMessage = c.getResources().getString(R.string.options_connections_broadcast) +
                    " " + FullscreenActivity.mBluetoothName;
            FullscreenActivity.salutLog += "\n" + FullscreenActivity.myToastMessage;
            FullscreenActivity.connectionsLog.setText(FullscreenActivity.salutLog);
            ShowToast.showToast(c);
        } else {
            try {
                FullscreenActivity.network.stopNetworkService(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            FullscreenActivity.hostButtonText = c.getResources().getString(R.string.options_connections_service_start).toUpperCase(FullscreenActivity.locale);
            FullscreenActivity.hostButton.setText(FullscreenActivity.hostButtonText);
            FullscreenActivity.salutLog += "\n" + c.getResources().getString(R.string.options_connections_service_stop);
            FullscreenActivity.connectionsLog.setText(FullscreenActivity.salutLog);
            FullscreenActivity.clientButton.setAlpha(1f);
            FullscreenActivity.clientButton.setClickable(true);
        }
    }

    public static void discoverServices(final Context c) {
        if(!FullscreenActivity.network.isRunningAsHost && !FullscreenActivity.network.isDiscovering) {
            try {
                FullscreenActivity.network.discoverNetworkServices(new SalutCallback() {
                    @Override
                    public void call() {
                        SalutDevice hostname = FullscreenActivity.network.foundDevices.get(0);
                        FullscreenActivity.myToastMessage = c.getResources().getString(R.string.options_connections_host) +
                                " " + hostname.readableName;
                        FullscreenActivity.salutLog += "\n" + FullscreenActivity.myToastMessage;
                        FullscreenActivity.connectionsLog.setText(FullscreenActivity.salutLog);
                        ShowToast.showToast(c);
                        registerWithHost(c,hostname);
                    }
                }, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            FullscreenActivity.salutLog += "\n" + c.getResources().getString(R.string.options_connections_searching);
            FullscreenActivity.connectionsLog.setText(FullscreenActivity.salutLog);
            FullscreenActivity.clientButtonText = c.getResources().getString(R.string.options_connections_discover_stop).toUpperCase(FullscreenActivity.locale);
            FullscreenActivity.clientButton.setText(FullscreenActivity.clientButtonText);
            FullscreenActivity.hostButton.setAlpha(0.5f);
            FullscreenActivity.hostButton.setClickable(false);
        } else {
            FullscreenActivity.salutLog += "\n" +c.getResources().getString(R.string.options_connections_discover_stop);
            FullscreenActivity.connectionsLog.setText(FullscreenActivity.salutLog);
            try {
                FullscreenActivity.network.stopServiceDiscovery(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            FullscreenActivity.clientButtonText = c.getResources().getString(R.string.options_connections_discover_start).toUpperCase(FullscreenActivity.locale);
            FullscreenActivity.clientButton.setText(FullscreenActivity.clientButtonText);
            FullscreenActivity.hostButton.setAlpha(1f);
            FullscreenActivity.hostButton.setClickable(true);
        }

    }

    public static void registerWithHost(final Context c, final SalutDevice possibleHost) {
        try {
            FullscreenActivity.network.registerWithHost(possibleHost, new SalutCallback() {
                @Override
                public void call() {
                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.options_connections_connected) +
                            " " + possibleHost.readableName;
                    FullscreenActivity.salutLog += "\n" + FullscreenActivity.myToastMessage;
                    FullscreenActivity.connectionsLog.setText(FullscreenActivity.salutLog);
                    ShowToast.showToast(c);
                    FullscreenActivity.clientButtonText = (c.getResources().getString(R.string.options_connections_disconnect) +
                            " " + possibleHost.readableName).toUpperCase(FullscreenActivity.locale);
                    FullscreenActivity.clientButton.setText(FullscreenActivity.clientButtonText);

                }
            }, new SalutCallback() {
                @Override
                public void call() {
                    FullscreenActivity.myToastMessage = possibleHost.readableName + ": " +
                            c.getResources().getString(R.string.options_connections_failure);
                    FullscreenActivity.salutLog += "\n" + FullscreenActivity.myToastMessage;
                    FullscreenActivity.connectionsLog.setText(FullscreenActivity.salutLog);
                    ShowToast.showToast(c);
                    try {
                        FullscreenActivity.network.stopServiceDiscovery(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    FullscreenActivity.clientButtonText = c.getResources().getString(R.string.options_connections_discover_start).toUpperCase(FullscreenActivity.locale);
                    FullscreenActivity.clientButton.setText(FullscreenActivity.clientButtonText);
                    FullscreenActivity.hostButton.setAlpha(1f);
                    FullscreenActivity.hostButton.setClickable(true);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void modeOptionListener(View v, final Context c) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuUp = (TextView) v.findViewById(R.id.modeMenuTitle);
        Button modePerformanceButton = (Button) v.findViewById(R.id.modePerformanceButton);
        Button modeStageButton = (Button) v.findViewById(R.id.modeStageButton);
        Button modePresentationButton = (Button) v.findViewById(R.id.modePresentationButton);
        FloatingActionButton closeOptionsFAB = (FloatingActionButton) v.findViewById(R.id.closeOptionsFAB);
        // ImageView connectionsStatusImage = (ImageView) v.findViewById(R.id.connectionsStatusImage);

        // Capitalise all the text by locale
        menuUp.setText(c.getString(R.string.options_modes).toUpperCase(FullscreenActivity.locale));
        modePerformanceButton.setText(c.getString(R.string.performancemode).toUpperCase(FullscreenActivity.locale));
        modeStageButton.setText(c.getString(R.string.stagemode).toUpperCase(FullscreenActivity.locale));
        modePresentationButton.setText(c.getString(R.string.presentermode).toUpperCase(FullscreenActivity.locale));

        // Set a tick next to the current mode
        switch (FullscreenActivity.whichMode) {
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
        menuUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whichOptionMenu = "MAIN";
                if (mListener!=null) {
                    mListener.prepareOptionMenu();
                }
            }
        });
        modePerformanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!FullscreenActivity.whichMode.equals("Performance")) {
                    // Switch to performance mode
                    FullscreenActivity.whichMode = "Performance";
                    Preferences.savePreferences();
                    Intent performmode = new Intent();
                    performmode.setClass(c, FullscreenActivity.class);
                    if (mListener!=null) {
                        mListener.closeMyDrawers("option");
                        mListener.callIntent("activity", performmode);
                    }
                }
            }
        });
        modeStageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!FullscreenActivity.whichMode.equals("Stage")) {
                    // Switch to stage mode
                    FullscreenActivity.whichMode = "Stage";
                    Preferences.savePreferences();
                    Intent stagemode = new Intent();
                    stagemode.setClass(c, StageMode.class);
                    if (mListener!=null) {
                        mListener.closeMyDrawers("option");
                        mListener.callIntent("activity", stagemode);
                    }
                }
            }
        });
        modePresentationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!FullscreenActivity.whichMode.equals("Presentation")) {
                    // Switch to presentation mode
                    FullscreenActivity.whichMode = "Presentation";
                    Preferences.savePreferences();
                    Intent presentmode = new Intent();
                    presentmode.setClass(c, PresenterMode.class);
                    if (mListener!=null) {
                        mListener.closeMyDrawers("option");
                        mListener.callIntent("activity", presentmode);
                    }
                }
            }
        });

        closeOptionsFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                }
            }
        });

    }

    public static void gestureOptionListener(View v, Context c) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuup = (TextView) v.findViewById(R.id.optionGestureTitle);
        Button gesturesPedalButton = (Button) v.findViewById(R.id.gesturesPedalButton);
        Button gesturesPageButton = (Button) v.findViewById(R.id.gesturesPageButton);
        Button gesturesCustomButton = (Button) v.findViewById(R.id.gesturesCustomButton);
        Button gesturesMenuOptions = (Button) v.findViewById(R.id.gesturesMenuOptions);
        SwitchCompat displayMenuToggleSwitch = (SwitchCompat) v.findViewById(R.id.displayMenuToggleSwitch);
        SwitchCompat gesturesSongSwipeButton = (SwitchCompat) v.findViewById(R.id.gesturesSongSwipeButton);
        FloatingActionButton closeOptionsFAB = (FloatingActionButton) v.findViewById(R.id.closeOptionsFAB);

        // Capitalise all the text by locale
        menuup.setText(c.getString(R.string.options_gesturesandmenus).toUpperCase(FullscreenActivity.locale));
        gesturesPedalButton.setText(c.getString(R.string.options_options_pedal).toUpperCase(FullscreenActivity.locale));
        gesturesPageButton.setText(c.getString(R.string.quicklaunch_title).toUpperCase(FullscreenActivity.locale));
        gesturesCustomButton.setText(c.getString(R.string.options_options_gestures).toUpperCase(FullscreenActivity.locale));
        gesturesMenuOptions.setText(c.getString(R.string.menu_settings).toUpperCase(FullscreenActivity.locale));
        displayMenuToggleSwitch.setText(c.getString(R.string.options_options_hidebar).toUpperCase(FullscreenActivity.locale));
        gesturesSongSwipeButton.setText(c.getString(R.string.options_options_songswipe).toUpperCase(FullscreenActivity.locale));

        // Set the switches up based on preferences
        gesturesSongSwipeButton.setChecked(FullscreenActivity.swipeForSongs);
        displayMenuToggleSwitch.setChecked(FullscreenActivity.hideActionBar);

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

        gesturesPedalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    FullscreenActivity.whattodo = "footpedal";
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        gesturesPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    FullscreenActivity.whattodo = "quicklaunch";
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        gesturesCustomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    FullscreenActivity.whattodo = "gestures";
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        gesturesMenuOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    FullscreenActivity.whattodo = "menuoptions";
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        displayMenuToggleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.hideActionBar = b;
                if (mListener!=null) {
                    if (b) {
                        mListener.hideActionBar();
                    } else {
                        mListener.showActionBar();
                    }
                }
                Preferences.savePreferences();
                mListener.loadSong();
            }
        });

        gesturesSongSwipeButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.swipeForSongs = b;
                Preferences.savePreferences();
            }
        });

        closeOptionsFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                }
            }
        });

    }

    public static void autoscrollOptionListener(View v, Context c) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuup = (TextView) v.findViewById(R.id.optionAutoScrollTitle);
        Button autoScrollTimeDefaultsButton = (Button) v.findViewById(R.id.autoScrollTimeDefaultsButton);
        SwitchCompat autoScrollStartButton = (SwitchCompat) v.findViewById(R.id.autoScrollStartButton);
        FloatingActionButton closeOptionsFAB = (FloatingActionButton) v.findViewById(R.id.closeOptionsFAB);

        // Capitalise all the text by locale
        menuup.setText(c.getString(R.string.autoscroll).toUpperCase(FullscreenActivity.locale));
        autoScrollTimeDefaultsButton.setText(c.getString(R.string.default_autoscroll).toUpperCase(FullscreenActivity.locale));
        autoScrollStartButton.setText(c.getString(R.string.options_options_autostartscroll).toUpperCase(FullscreenActivity.locale));

        // Set the switches up based on preferences
        autoScrollStartButton.setChecked(FullscreenActivity.autostartautoscroll);

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

        autoScrollTimeDefaultsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    FullscreenActivity.whattodo = "autoscrolldefaults";
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        autoScrollStartButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.autostartautoscroll = b;
                Preferences.savePreferences();
            }
        });

        closeOptionsFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                }
            }
        });
    }

    public static void padOptionListener(View v, Context c) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuup = (TextView) v.findViewById(R.id.optionPadTitle);
        Button padCrossFadeButton = (Button) v.findViewById(R.id.padCrossFadeButton);
        FloatingActionButton closeOptionsFAB = (FloatingActionButton) v.findViewById(R.id.closeOptionsFAB);

        // Capitalise all the text by locale
        menuup.setText(c.getString(R.string.pad).toUpperCase(FullscreenActivity.locale));
        padCrossFadeButton.setText(c.getString(R.string.crossfade_time).toUpperCase(FullscreenActivity.locale));

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

        padCrossFadeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "crossfade";
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        closeOptionsFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                }
            }
        });

    }

    public static void otherOptionListener(View v, final Context c) {
        mListener = (MyInterface) c;

        // Identify the buttons
        TextView menuup = (TextView) v.findViewById(R.id.optionOtherTitle);
        Button otherHelpButton = (Button) v.findViewById(R.id.otherHelpButton);
        Button otherTweetButton = (Button) v.findViewById(R.id.otherTweetButton);
        Button otherLanguageButton = (Button) v.findViewById(R.id.otherLanguageButton);
        Button otherStartButton = (Button) v.findViewById(R.id.otherStartButton);
        Button otherRateButton = (Button) v.findViewById(R.id.otherRateButton);
        FloatingActionButton closeOptionsFAB = (FloatingActionButton) v.findViewById(R.id.closeOptionsFAB);

        // Capitalise all the text by locale
        menuup.setText(c.getString(R.string.options_other).toUpperCase(FullscreenActivity.locale));
        otherHelpButton.setText(c.getString(R.string.options_options_help).toUpperCase(FullscreenActivity.locale));
        otherLanguageButton.setText(c.getString(R.string.language).toUpperCase(FullscreenActivity.locale));
        otherStartButton.setText(c.getString(R.string.options_options_start).toUpperCase(FullscreenActivity.locale));
        otherRateButton.setText(c.getString(R.string.rate).toUpperCase(FullscreenActivity.locale));

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

        otherHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "http://www.opensongapp.com";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                    mListener.callIntent("web",i);
                }
            }
        });

        otherTweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                    mListener.callIntent("twitter",null);
                }
            }
        });

        otherLanguageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    FullscreenActivity.whattodo = "language";
                    mListener.closeMyDrawers("option");
                    mListener.openFragment();
                }
            }
        });

        otherStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                    mListener.splashScreen();
                }
            }
        });

        otherRateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    // Rate this app
                    String appPackage = c.getPackageName();
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackage));
                    mListener.closeMyDrawers("option");
                    mListener.callIntent("web", i);
                }
            }
        });
        closeOptionsFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    mListener.closeMyDrawers("option");
                }
            }
        });
    }

}