package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

public class PopUpGroupedPageButtonsFragment extends DialogFragment {

    static PopUpGroupedPageButtonsFragment newInstance() {
        PopUpGroupedPageButtonsFragment frag;
        frag = new PopUpGroupedPageButtonsFragment();
        return frag;
    }

    public interface MyInterface {
        void openFragment();
        void loadSong();
        void gesture5();
        void gesture6();
        void gesture7();
        void displayHighlight(boolean fromautoshow);
        void takeScreenShot();
    }

    private PopUpGroupedPageButtonsFragment.MyInterface mListener;

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        mListener = (PopUpGroupedPageButtonsFragment.MyInterface) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    FloatingActionButton group_set;
    FloatingActionButton group_pad;
    FloatingActionButton group_autoscroll;
    FloatingActionButton group_metronome;
    FloatingActionButton group_chords;
    FloatingActionButton group_links;
    FloatingActionButton group_sticky;
    FloatingActionButton group_notation;
    FloatingActionButton group_highlight;
    FloatingActionButton group_pages;
    FloatingActionButton group_custom1;
    FloatingActionButton group_custom2;
    FloatingActionButton group_custom3;
    FloatingActionButton group_custom4;

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        View V = inflater.inflate(R.layout.popup_groupedpagebuttons, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.pagebuttons));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                dismiss();
            }
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setVisibility(View.GONE);

        // Initialise the views
        group_set = V.findViewById(R.id.group_set);
        group_pad = V.findViewById(R.id.group_pad);
        group_autoscroll = V.findViewById(R.id.group_autoscroll);
        group_metronome = V.findViewById(R.id.group_metronome);
        group_chords = V.findViewById(R.id.group_chords);
        group_links = V.findViewById(R.id.group_links);
        group_sticky = V.findViewById(R.id.group_sticky);
        group_notation = V.findViewById(R.id.group_notation);
        group_highlight = V.findViewById(R.id.group_highlight);
        group_pages = V.findViewById(R.id.group_pages);
        group_custom1 = V.findViewById(R.id.group_custom1);
        group_custom2 = V.findViewById(R.id.group_custom2);
        group_custom3 = V.findViewById(R.id.group_custom3);
        group_custom4 = V.findViewById(R.id.group_custom4);

        // Set the quicklaunch icons
        setupQuickLaunchButtons();

        // Set the colors
        group_set.setBackgroundTintList(ColorStateList.valueOf(FullscreenActivity.pagebuttonsColor));
        group_pad.setBackgroundTintList(ColorStateList.valueOf(FullscreenActivity.pagebuttonsColor));
        group_autoscroll.setBackgroundTintList(ColorStateList.valueOf(FullscreenActivity.pagebuttonsColor));
        group_metronome.setBackgroundTintList(ColorStateList.valueOf(FullscreenActivity.pagebuttonsColor));
        group_chords.setBackgroundTintList(ColorStateList.valueOf(FullscreenActivity.pagebuttonsColor));
        group_links.setBackgroundTintList(ColorStateList.valueOf(FullscreenActivity.pagebuttonsColor));
        group_sticky.setBackgroundTintList(ColorStateList.valueOf(FullscreenActivity.pagebuttonsColor));
        group_notation.setBackgroundTintList(ColorStateList.valueOf(FullscreenActivity.pagebuttonsColor));
        group_highlight.setBackgroundTintList(ColorStateList.valueOf(FullscreenActivity.pagebuttonsColor));
        group_pages.setBackgroundTintList(ColorStateList.valueOf(FullscreenActivity.pagebuttonsColor));
        group_custom1.setBackgroundTintList(ColorStateList.valueOf(FullscreenActivity.pagebuttonsColor));
        group_custom2.setBackgroundTintList(ColorStateList.valueOf(FullscreenActivity.pagebuttonsColor));
        group_custom3.setBackgroundTintList(ColorStateList.valueOf(FullscreenActivity.pagebuttonsColor));
        group_custom4.setBackgroundTintList(ColorStateList.valueOf(FullscreenActivity.pagebuttonsColor));

        // Set shortclick listeners
        group_set.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAction("editset");
            }
        }));
        group_pad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAction("page_pad");
            }
        });
        group_autoscroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { openAction("page_autoscroll");
            }
        });
        group_metronome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { openAction("page_metronome");
            }
        });
        group_chords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAction("page_chords");
            }
        });
        group_links.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { openAction("page_links");
            }
        });
        group_sticky.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAction("page_sticky");
            }
        });
        group_notation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (FullscreenActivity.mNotation.equals("")) {
                    openAction("abcnotation_edit");
                } else {
                    openAction("abcnotation");
                }
            }
        });
        group_highlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.highlightOn = !FullscreenActivity.highlightOn;
                FullscreenActivity.whattodo = "page_highlight";
                if (mListener!=null) {
                    mListener.displayHighlight(false);
                }
                dismiss();
            }
        });
        group_pages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { openAction("page_pageselect");
            }
        });
        group_custom1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {customButtonAction(FullscreenActivity.quickLaunchButton_1);}
        });
        group_custom2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {customButtonAction(FullscreenActivity.quickLaunchButton_2);}
        });
        group_custom3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {customButtonAction(FullscreenActivity.quickLaunchButton_3);}
        });
        group_custom4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {customButtonAction(FullscreenActivity.quickLaunchButton_4);}
        });

        // Set longclick listeners
        group_pad.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mListener!=null) {
                    mListener.gesture6();
                }
                dismiss();
                return true;
            }
        });
        group_notation.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                openAction("abcnotation_edit");
                return true;
            }
        });
        group_autoscroll.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mListener!=null) {
                    mListener.gesture5();
                }
                dismiss();
                return true;
            }
        });

        group_metronome.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mListener!=null) {
                    mListener.gesture7();
                }
                dismiss();
                return true;
            }
        });

        group_custom1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                openAction("quicklaunch");
                return true;
            }
        });
        group_custom2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                openAction("quicklaunch");
                return true;
            }
        });
        group_custom3.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                openAction("quicklaunch");
                return true;
            }
        });
        group_custom4.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                openAction("quicklaunch");
                return true;
            }
        });

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());

        return V;
    }

    public void setupQuickLaunchButtons() {
        // Based on the user's choices for the custom quicklaunch buttons,
        // set the appropriate icons and onClick listeners
        group_custom1.setImageDrawable(PopUpQuickLaunchSetup.getButtonImage(getActivity(), FullscreenActivity.quickLaunchButton_1));
        group_custom2.setImageDrawable(PopUpQuickLaunchSetup.getButtonImage(getActivity(), FullscreenActivity.quickLaunchButton_2));
        group_custom3.setImageDrawable(PopUpQuickLaunchSetup.getButtonImage(getActivity(), FullscreenActivity.quickLaunchButton_3));
        group_custom4.setImageDrawable(PopUpQuickLaunchSetup.getButtonImage(getActivity(), FullscreenActivity.quickLaunchButton_4));
    }

    public void customButtonAction(String s) {
        switch (s) {
            case "":
            default:
                openAction("quicklaunch");
                break;

            case "editsong":
            case "changetheme":
            case "autoscale":
            case "changefonts":
            case "profiles":
            case "gestures":
            case "footpedal":
            case "transpose":
            case "fullsearch":
            case "editset":
                openAction(s);
                break;

            case "showchords":
                FullscreenActivity.showChords = !FullscreenActivity.showChords;
                saveSongAndLoadIt();
                break;

            case "showcapo":
                FullscreenActivity.showCapo = !FullscreenActivity.showCapo;
                saveSongAndLoadIt();
                break;

            case "showlyrics":
                FullscreenActivity.showLyrics = !FullscreenActivity.showLyrics;
                saveSongAndLoadIt();
                break;
        }
    }

    public void openAction(String s) {
        FullscreenActivity.whattodo = s;
        if (mListener!=null) {
            mListener.openFragment();
        }
        dismiss();
    }

    public void saveSongAndLoadIt() {
        Preferences.savePreferences();
        if (mListener!=null) {
            mListener.loadSong();
        }
        dismiss();
    }
}
