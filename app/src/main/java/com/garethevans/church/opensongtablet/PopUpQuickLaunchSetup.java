package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

public class PopUpQuickLaunchSetup extends DialogFragment {

    static PopUpQuickLaunchSetup newInstance() {
        PopUpQuickLaunchSetup frag;
        frag = new PopUpQuickLaunchSetup();
        return frag;
    }

    private FloatingActionButton button1_image, button2_image, button3_image, button4_image;

    private MyInterface mListener;

    private Preferences preferences;

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    private Spinner button1_spinner, button2_spinner, button3_spinner, button4_spinner;
    private Button showAll_Button;
    private View V;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.dismiss();
        }
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        mListener.pageButtonAlpha("");

        V = inflater.inflate(R.layout.popup_quicklaunch, container, false);

        preferences = new Preferences();

        // Do this in a new thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initialiseViews();
                        setFABS();
                        setUpSpinners();
                        setListeners();
                    }
                });
            }
        }).start();

        PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog(), preferences);

        return V;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    private void initialiseViews() {
        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getString(R.string.quicklaunch_title));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                dismiss();
            }
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(saveMe,getActivity());
                saveMe.setEnabled(false);
                doSave();
            }
        });

        // Initialise the views
        button1_image = V.findViewById(R.id.button1_image);
        button2_image = V.findViewById(R.id.button2_image);
        button3_image = V.findViewById(R.id.button3_image);
        button4_image = V.findViewById(R.id.button4_image);
        button1_spinner = V.findViewById(R.id.button1_spinner);
        button2_spinner = V.findViewById(R.id.button2_spinner);
        button3_spinner = V.findViewById(R.id.button3_spinner);
        button4_spinner = V.findViewById(R.id.button4_spinner);
        showAll_Button = V.findViewById(R.id.showAll_Button);
    }

    private void setFABS() {
        // Set the colors
        int color;
        Preferences preferences = new Preferences();
        switch (StaticVariables.mDisplayTheme) {
            case "dark":
            default:
                color = preferences.getMyPreferenceInt(getActivity(),"dark_pageButtonsColor",StaticVariables.purplyblue);
                break;
            case "light":
                color = preferences.getMyPreferenceInt(getActivity(),"light_pageButtonsColor",StaticVariables.purplyblue);
                break;
            case "custom1":
                color = preferences.getMyPreferenceInt(getActivity(),"custom1_pageButtonsColor",StaticVariables.purplyblue);
                break;
            case "custom2":
                color = preferences.getMyPreferenceInt(getActivity(),"custom2_pageButtonsColor",StaticVariables.purplyblue);
                break;
        }
        // Set the floatingactionbuttons to the correct color
        button1_image.setBackgroundTintList(ColorStateList.valueOf(color));
        button2_image.setBackgroundTintList(ColorStateList.valueOf(color));
        button3_image.setBackgroundTintList(ColorStateList.valueOf(color));
        button4_image.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    private void setUpSpinners() {
        ArrayList<String> actionOptions = new ArrayList<>();
        //0
        actionOptions.add("");

        //1
        actionOptions.add(getResources().getString(R.string.edit));

        //2,3,4,5
        actionOptions.add(getResources().getString(R.string.choose_theme));
        actionOptions.add(getResources().getString(R.string.autoscale_toggle));
        actionOptions.add(getResources().getString(R.string.choose_fonts));
        actionOptions.add(getResources().getString(R.string.profile));

        //6,7
        actionOptions.add(getResources().getString(R.string.custom_gestures));
        actionOptions.add(getResources().getString(R.string.footpedal));

        //8,9,10,11
        actionOptions.add(getResources().getString(R.string.transpose));
        actionOptions.add(getResources().getString(R.string.showchords));
        actionOptions.add(getResources().getString(R.string.showcapo));
        actionOptions.add(getResources().getString(R.string.showlyrics));

        //12
        actionOptions.add(getString(R.string.action_search));

        //13
        actionOptions.add(getString(R.string.random_song));

        //14
        actionOptions.add(getString(R.string.music_score));

        //15
        actionOptions.add(getString(R.string.inc_autoscroll_speed));
        //16
        actionOptions.add(getString(R.string.dec_autoscroll_speed));
        //17
        actionOptions.add(getString(R.string.toggle_autoscroll_pause));
        //18
        actionOptions.add(getString(R.string.midi));
        //19  Exit/close app
        actionOptions.add(getString(R.string.drawer_close));

        ArrayAdapter<String> adapter_1 = new ArrayAdapter<>(Objects.requireNonNull(getActivity()), R.layout.my_spinner, actionOptions);
        ArrayAdapter<String> adapter_2 = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, actionOptions);
        ArrayAdapter<String> adapter_3 = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, actionOptions);
        ArrayAdapter<String> adapter_4 = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, actionOptions);
        adapter_1.setDropDownViewResource(R.layout.my_spinner);
        adapter_2.setDropDownViewResource(R.layout.my_spinner);
        adapter_3.setDropDownViewResource(R.layout.my_spinner);
        adapter_3.setDropDownViewResource(R.layout.my_spinner);
        adapter_4.setDropDownViewResource(R.layout.my_spinner);
        button1_spinner.setAdapter(adapter_1);
        button2_spinner.setAdapter(adapter_2);
        button3_spinner.setAdapter(adapter_3);
        button4_spinner.setAdapter(adapter_4);

        button1_spinner.setSelection(decideOnItemPosition(preferences.getMyPreferenceString(getActivity(),"pageButtonCustom1Action","")));
        button2_spinner.setSelection(decideOnItemPosition(preferences.getMyPreferenceString(getActivity(),"pageButtonCustom2Action","")));
        button3_spinner.setSelection(decideOnItemPosition(preferences.getMyPreferenceString(getActivity(),"pageButtonCustom3Action","")));
        button4_spinner.setSelection(decideOnItemPosition(preferences.getMyPreferenceString(getActivity(),"pageButtonCustom4Action","")));

        button1_image.setBackgroundDrawable(getButtonImage(getActivity(),preferences.getMyPreferenceString(getActivity(),"pageButtonCustom1Action","")));
        button2_image.setBackgroundDrawable(getButtonImage(getActivity(),preferences.getMyPreferenceString(getActivity(),"pageButtonCustom2Action","")));
        button3_image.setBackgroundDrawable(getButtonImage(getActivity(),preferences.getMyPreferenceString(getActivity(),"pageButtonCustom3Action","")));
        button4_image.setBackgroundDrawable(getButtonImage(getActivity(),preferences.getMyPreferenceString(getActivity(),"pageButtonCustom4Action","")));
    }

    private void setListeners() {
        showAll_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullscreenActivity.whattodo = "pagebuttons";
                try {
                    if (mListener != null) {
                        mListener.openFragment();
                    }
                    dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        button1_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                button1_image.setImageDrawable(getButtonImage(getActivity(),decideOnItemText(i)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        button2_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                button2_image.setImageDrawable(getButtonImage(getActivity(),decideOnItemText(i)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        button3_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                button3_image.setImageDrawable(getButtonImage(getActivity(),decideOnItemText(i)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        button4_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                button4_image.setImageDrawable(getButtonImage(getActivity(),decideOnItemText(i)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    public interface MyInterface {
        void pageButtonAlpha(String s);

        void setupQuickLaunchButtons();

        void openFragment();
    }

    private void doSave() {
        preferences.setMyPreferenceString(getActivity(),"pageButtonCustom1Action", decideOnItemText(button1_spinner.getSelectedItemPosition()));
        preferences.setMyPreferenceString(getActivity(),"pageButtonCustom2Action", decideOnItemText(button2_spinner.getSelectedItemPosition()));
        preferences.setMyPreferenceString(getActivity(),"pageButtonCustom3Action", decideOnItemText(button3_spinner.getSelectedItemPosition()));
        preferences.setMyPreferenceString(getActivity(),"pageButtonCustom4Action", decideOnItemText(button4_spinner.getSelectedItemPosition()));
        if (mListener!=null) {
            mListener.setupQuickLaunchButtons();
        }
        dismiss();
    }

    private static String decideOnItemText(int i) {
        String t;
        switch (i) {
            case 0:
            default:
                t = "";
                break;

            case 1:
                t = "editsong";
                break;

            case 2:
                t = "changetheme";
                break;

            case 3:
                t = "autoscale";
                break;

            case 4:
                t = "changefonts";
                break;

            case 5:
                t = "profiles";
                break;

            case 6:
                t = "gestures";
                break;

            case 7:
                t = "footpedal";
                break;

            case 8:
                t = "transpose";
                break;

            case 9:
                t = "showchords";
                break;

            case 10:
                t = "showcapo";
                break;

            case 11:
                t = "showlyrics";
                break;

            case 12:
                t = "fullsearch";
                break;

            case 13:
                t = "randomsong";
                break;

            case 14:
                t = "abcnotation_edit";
                break;

            case 15:
                t = "inc_autoscroll_speed";
                break;

            case 16:
                t = "dec_autoscroll_speed";
                break;

            case 17:
                t = "toggle_autoscroll_pause";
                break;

            case 18:
                t = "showmidicommands";
                break;

            case 19:
                t = "exit";
        }

        return t;
    }

    private static int decideOnItemPosition(String s) {
        int i;
        switch (s) {
            case "":
            default:
                i = 0;
                break;

            case "editsong":
                i = 1;
                break;

            case "changetheme":
                i = 2;
                break;

            case "autoscale":
                i = 3;
                break;

            case "changefonts":
                i = 4;
                break;

            case "profiles":
                i = 5;
                break;

            case "gestures":
                i = 6;
                break;

            case "footpedal":
                i = 7;
                break;

            case "transpose":
                i = 8;
                break;

            case "showchords":
                i = 9;
                break;

            case "showcapo":
                i = 10;
                break;

            case "showlyrics":
                i = 11;
                break;

            case "search":
                i = 12;
                break;

            case "randomsong":
                i = 13;
                break;

            case "inc_autoscroll_speed":
                i = 15;
                break;

            case "dec_autoscroll_speed":
                i = 16;
                break;

            case "toggle_autoscroll_pause":
                i = 17;
                break;

            case "showmidicommands":
                i = 18;
                break;

            case "exit":
                i = 19;
                break;
        }

        return i;

    }

    static Drawable getButtonImage(Context c, String t) {
        Drawable d;
        switch (t) {
            case "":
            default:
                d = c.getResources().getDrawable(R.drawable.ic_help_outline_white_36dp);
                break;

            case "editsong":
                d = c.getResources().getDrawable(R.drawable.ic_table_edit_white_36dp);
                break;

            case "changetheme":
                d = c.getResources().getDrawable(R.drawable.ic_theme_light_dark_white_36dp);
                break;

            case "autoscale":
                d = c.getResources().getDrawable(R.drawable.ic_arrow_expand_white_36dp);
                break;

            case "changefonts":
                d = c.getResources().getDrawable(R.drawable.ic_format_text_white_36dp);
                break;

            case "profiles":
                d = c.getResources().getDrawable(R.drawable.ic_account_white_36dp);
                break;

            case "gestures":
                d = c.getResources().getDrawable(R.drawable.ic_fingerprint_white_36dp);
                break;

            case "footpedal":
                d = c.getResources().getDrawable(R.drawable.ic_pedal_white_36dp);
                break;

            case "transpose":
                d = c.getResources().getDrawable(R.drawable.ic_transpose_white_36dp);
                break;

            case "showchords":
                d = c.getResources().getDrawable(R.drawable.ic_guitar_electric_white_36dp);
                break;

            case "showcapo":
                d = c.getResources().getDrawable(R.drawable.ic_capo_white_36dp);
                break;

            case "showlyrics":
                d = c.getResources().getDrawable(R.drawable.ic_voice_white_36dp);
                break;

            case "fullsearch":
                d = c.getResources().getDrawable(R.drawable.ic_magnify_white_36dp);
                break;

            case "randomsong":
                d = c.getResources().getDrawable(R.drawable.ic_shuffle_white_36dp);
                break;

            case "abcnotation_edit":
                d = c.getResources().getDrawable(R.drawable.ic_clef_white_36dp);
                break;

            case "inc_autoscroll_speed":
                d = c.getResources().getDrawable(R.drawable.ic_autoscroll_plus_white_36dp);
                break;

            case "dec_autoscroll_speed":
                d = c.getResources().getDrawable(R.drawable.ic_autoscroll_minus_white_36dp);
                break;

            case "toggle_autoscroll_pause":
                d = c.getResources().getDrawable(R.drawable.ic_autoscroll_pause_white_36dp);
                break;

            case "showmidicommands":
                d = c.getResources().getDrawable(R.drawable.ic_midi_white_36dp);
                break;

            case "exit":
                d = c.getResources().getDrawable(R.drawable.ic_exit_to_app_white_36dp);
                break;
        }
        return d;
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        if (mListener!=null) {
            mListener.pageButtonAlpha("");
        }
        super.onDismiss(dialog);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}
