package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PopUpEditSongFragment extends DialogFragment implements PopUpPresentationOrderFragment.MyInterface {

    static PopUpEditSongFragment newInstance() {
        PopUpEditSongFragment frag;
        frag = new PopUpEditSongFragment();
        return frag;
    }

    // The General views available
    EditText edit_song_title;
    EditText edit_song_author;
    EditText edit_song_copyright;
    Spinner edit_song_key;
    EditText edit_song_duration;
    SeekBar predelay_SeekBar;
    TextView predelay_TextView;
    SeekBar edit_song_tempo;
    TextView tempo_text;
    Spinner edit_song_timesig;
    Spinner edit_song_capo;
    Spinner edit_song_capo_print;
    EditText edit_song_presentation;
    EditText edit_song_notes;
    EditText edit_song_lyrics;
    TextView abcnotation;
    Button fix_lyrics;

    // Advanced
    EditText edit_song_CCLI;
    EditText edit_song_aka;
    EditText edit_song_key_line;
    EditText edit_song_hymn;
    EditText edit_song_user1;
    EditText edit_song_user2;
    EditText edit_song_user3;
    Spinner edit_song_pad_file;
    EditText edit_song_midi;
    EditText edit_song_midi_index;
    EditText edit_song_restrictions;
    EditText edit_song_books;
    EditText edit_song_pitch;
    EditText customTheme;
/*
    CheckBox edit_song_theme_christ_attributes;
    CheckBox edit_song_theme_christ_birth;
    CheckBox edit_song_theme_christ_death_atonement;
    CheckBox edit_song_theme_christ_power_majesty;
    CheckBox edit_song_theme_christ_love_mercy;
    CheckBox edit_song_theme_christ_resurrection;
    CheckBox edit_song_theme_christ_second_coming;
    CheckBox edit_song_theme_christ_victory;
    CheckBox edit_song_theme_church_commitment_obedience;
    CheckBox edit_song_theme_church_country;
    CheckBox edit_song_theme_church_eternal_life_heaven;
    CheckBox edit_song_theme_church_evangelism;
    CheckBox edit_song_theme_church_family_fellowship;
    CheckBox edit_song_theme_church_fellowship_w_god;
    CheckBox edit_song_theme_church_purity_holiness;
    CheckBox edit_song_theme_church_renewal;
    CheckBox edit_song_theme_church_repentance_salvation;
    CheckBox edit_song_theme_church_service_ministry;
    CheckBox edit_song_theme_church_spiritual_hunger;
    CheckBox edit_song_theme_fruit_faith_hope;
    CheckBox edit_song_theme_fruit_humility_meekness;
    CheckBox edit_song_theme_fruit_joy;
    CheckBox edit_song_theme_fruit_love;
    CheckBox edit_song_theme_fruit_patience_kindness;
    CheckBox edit_song_theme_fruit_peace_comfort;
    CheckBox edit_song_theme_god_attributes;
    CheckBox edit_song_theme_god_creator_creation;
    CheckBox edit_song_theme_god_father;
    CheckBox edit_song_theme_god_guidance_care;
    CheckBox edit_song_theme_god_holiness;
    CheckBox edit_song_theme_god_holy_spirit;
    CheckBox edit_song_theme_god_love_mercy;
    CheckBox edit_song_theme_god_power_majesty;
    CheckBox edit_song_theme_god_promises;
    CheckBox edit_song_theme_god_victory;
    CheckBox edit_song_theme_god_word;
    CheckBox edit_song_theme_worship_assurance_trust;
    CheckBox edit_song_theme_worship_call_opening;
    CheckBox edit_song_theme_worship_celebration;
    CheckBox edit_song_theme_worship_declaration;
    CheckBox edit_song_theme_worship_intimacy;
    CheckBox edit_song_theme_worship_invitation;
    CheckBox edit_song_theme_worship_praise_adoration;
    CheckBox edit_song_theme_worship_prayer_devotion;
    CheckBox edit_song_theme_worship_provision_deliverance;
    CheckBox edit_song_theme_worship_thankfulness;
*/
    LinearLayout generalSettings;
    LinearLayout advancedSettings;

    // Buttons
    Button toggleGeneralAdvanced;

    static int temposlider;
    View V;
    String title;

    @Override
    public void updatePresentationOrder() {
        edit_song_presentation.setText(FullscreenActivity.mPresentation);
    }

    public interface MyInterface {
        void refreshAll();
    }

    private MyInterface mListener;

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (MyInterface) activity;
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        V = inflater.inflate(R.layout.popup_editsong, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.options_song_edit));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                cancelEdit();
            }
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(saveMe,getActivity());
                saveMe.setEnabled(false);
                saveEdit(true);
            }
        });

        // Initialise the basic views
        edit_song_title = V.findViewById(R.id.edit_song_title);
        edit_song_author = V.findViewById(R.id.edit_song_author);
        edit_song_copyright = V.findViewById(R.id.edit_song_copyright);
        edit_song_key = V.findViewById(R.id.edit_song_key);
        edit_song_duration = V.findViewById(R.id.edit_song_duration);
        predelay_SeekBar = V.findViewById(R.id.predelay_SeekBar);
        predelay_TextView = V.findViewById(R.id.predelay_TextView);
        edit_song_tempo = V.findViewById(R.id.edit_song_tempo);
        tempo_text = V.findViewById(R.id.tempo_text);
        edit_song_timesig = V.findViewById(R.id.edit_song_timesig);
        edit_song_capo = V.findViewById(R.id.edit_song_capo);
        edit_song_capo_print = V.findViewById(R.id.edit_song_capo_print);
        edit_song_presentation = V.findViewById(R.id.edit_song_presentation);
        edit_song_presentation.setFocusable(false);
        edit_song_presentation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save the song first - false to stop everything reloading
                saveEdit(false);

                DialogFragment newFragment = PopUpPresentationOrderFragment.newInstance();
                newFragment.show(getFragmentManager(), "dialog");
                dismiss();
            }
        });
        edit_song_notes = V.findViewById(R.id.edit_song_notes);
        edit_song_lyrics = V.findViewById(R.id.edit_song_lyrics);
        edit_song_lyrics.setHorizontallyScrolling(true);
        toggleGeneralAdvanced = V.findViewById(R.id.show_general_advanced);
        generalSettings = V.findViewById(R.id.general_settings);
        abcnotation = V.findViewById(R.id.abcnotation);
        abcnotation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Save the song first - false to stop everything reloading
                saveEdit(false);

                FullscreenActivity.whattodo = "abcnotation_edit";
                DialogFragment newFragment = PopUpABCNotationFragment.newInstance();
                newFragment.show(getFragmentManager(), "dialog");
                dismiss();
            }
        });
        fix_lyrics = V.findViewById(R.id.fix_lyrics);
        fix_lyrics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s = TextSongConvert.convertText(getActivity(),edit_song_lyrics.getText().toString());
                edit_song_lyrics.setText(s);
            }
        });

        // Initialise the advanced views
        edit_song_CCLI = V.findViewById(R.id.edit_song_ccli);
        edit_song_aka = V.findViewById(R.id.edit_song_aka);
        edit_song_key_line = V.findViewById(R.id.edit_song_keyline);
        edit_song_hymn = V.findViewById(R.id.edit_song_hymn);
        edit_song_user1 = V.findViewById(R.id.edit_song_user1);
        edit_song_user2 = V.findViewById(R.id.edit_song_user2);
        edit_song_user3 = V.findViewById(R.id.edit_song_user3);
        edit_song_pad_file = V.findViewById(R.id.edit_pad_file);
        edit_song_midi = V.findViewById(R.id.edit_song_midi);
        edit_song_midi_index = V.findViewById(R.id.edit_song_midi_index);
        edit_song_restrictions = V.findViewById(R.id.edit_song_restrictions);
        edit_song_books = V.findViewById(R.id.edit_song_books);
        edit_song_pitch = V.findViewById(R.id.edit_song_pitch);
        customTheme = V.findViewById(R.id.customTheme);
/*
        edit_song_theme_christ_attributes = V.findViewById(R.id.edit_song_theme_christ_attributes);
        edit_song_theme_christ_birth = V.findViewById(R.id.edit_song_theme_christ_birth);
        edit_song_theme_christ_death_atonement = V.findViewById(R.id.edit_song_theme_christ_death_atonement);
        edit_song_theme_christ_power_majesty = V.findViewById(R.id.edit_song_theme_christ_power_majesty);
        edit_song_theme_christ_love_mercy = V.findViewById(R.id.edit_song_theme_christ_love_mercy);
        edit_song_theme_christ_resurrection = V.findViewById(R.id.edit_song_theme_christ_resurrection);
        edit_song_theme_christ_second_coming = V.findViewById(R.id.edit_song_theme_christ_second_coming);
        edit_song_theme_christ_victory = V.findViewById(R.id.edit_song_theme_christ_victory);
        edit_song_theme_church_commitment_obedience = V.findViewById(R.id.edit_song_theme_church_commitment_obedience);
        edit_song_theme_church_country = V.findViewById(R.id.edit_song_theme_church_country);
        edit_song_theme_church_eternal_life_heaven = V.findViewById(R.id.edit_song_theme_church_eternal_life_heaven);
        edit_song_theme_church_evangelism = V.findViewById(R.id.edit_song_theme_church_evangelism);
        edit_song_theme_church_family_fellowship = V.findViewById(R.id.edit_song_theme_church_family_fellowship);
        edit_song_theme_church_fellowship_w_god = V.findViewById(R.id.edit_song_theme_church_fellowship_w_god);
        edit_song_theme_church_purity_holiness = V.findViewById(R.id.edit_song_theme_church_purity_holiness);
        edit_song_theme_church_renewal = V.findViewById(R.id.edit_song_theme_church_renewal);
        edit_song_theme_church_repentance_salvation = V.findViewById(R.id.edit_song_theme_church_repentance_salvation);
        edit_song_theme_church_service_ministry = V.findViewById(R.id.edit_song_theme_church_service_ministry);
        edit_song_theme_church_spiritual_hunger = V.findViewById(R.id.edit_song_theme_church_spiritual_hunger);
        edit_song_theme_fruit_faith_hope = V.findViewById(R.id.edit_song_theme_fruit_faith_hope);
        edit_song_theme_fruit_humility_meekness = V.findViewById(R.id.edit_song_theme_fruit_humility_meekness);
        edit_song_theme_fruit_joy = V.findViewById(R.id.edit_song_theme_fruit_joy);
        edit_song_theme_fruit_love = V.findViewById(R.id.edit_song_theme_fruit_love);
        edit_song_theme_fruit_patience_kindness = V.findViewById(R.id.edit_song_theme_fruit_patience_kindness);
        edit_song_theme_fruit_peace_comfort = V.findViewById(R.id.edit_song_theme_fruit_peace_comfort);
        edit_song_theme_god_attributes = V.findViewById(R.id.edit_song_theme_god_attributes);
        edit_song_theme_god_creator_creation = V.findViewById(R.id.edit_song_theme_god_creator_creation);
        edit_song_theme_god_father = V.findViewById(R.id.edit_song_theme_god_father);
        edit_song_theme_god_guidance_care = V.findViewById(R.id.edit_song_theme_god_guidance_care);
        edit_song_theme_god_holiness = V.findViewById(R.id.edit_song_theme_god_holiness);
        edit_song_theme_god_holy_spirit = V.findViewById(R.id.edit_song_theme_god_holy_spirit);
        edit_song_theme_god_love_mercy = V.findViewById(R.id.edit_song_theme_god_love_mercy);
        edit_song_theme_god_power_majesty = V.findViewById(R.id.edit_song_theme_god_power_majesty);
        edit_song_theme_god_promises = V.findViewById(R.id.edit_song_theme_god_promises);
        edit_song_theme_god_victory = V.findViewById(R.id.edit_song_theme_god_victory);
        edit_song_theme_god_word = V.findViewById(R.id.edit_song_theme_god_word);
        edit_song_theme_worship_assurance_trust = V.findViewById(R.id.edit_song_theme_worship_assurance_trust);
        edit_song_theme_worship_call_opening = V.findViewById(R.id.edit_song_theme_worship_call_opening);
        edit_song_theme_worship_celebration = V.findViewById(R.id.edit_song_theme_worship_celebration);
        edit_song_theme_worship_declaration = V.findViewById(R.id.edit_song_theme_worship_declaration);
        edit_song_theme_worship_intimacy = V.findViewById(R.id.edit_song_theme_worship_intimacy);
        edit_song_theme_worship_invitation = V.findViewById(R.id.edit_song_theme_worship_invitation);
        edit_song_theme_worship_praise_adoration = V.findViewById(R.id.edit_song_theme_worship_praise_adoration);
        edit_song_theme_worship_prayer_devotion = V.findViewById(R.id.edit_song_theme_worship_prayer_devotion);
        edit_song_theme_worship_provision_deliverance = V.findViewById(R.id.edit_song_theme_worship_provision_deliverance);
        edit_song_theme_worship_thankfulness = V.findViewById(R.id.edit_song_theme_worship_thankfulness);
*/
        advancedSettings = V.findViewById(R.id.advanced_settings);

        // Listeners for the buttons
        toggleGeneralAdvanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                 if (generalSettings.getVisibility() == View.VISIBLE) {
                    // Slide the general settings left
                    generalSettings.startAnimation(AnimationUtils.loadAnimation(
                            V.getContext(), R.anim.slide_out_left));
                    // Wait 300ms before hiding the general settings and unhiding the advanced settings
                    Handler delayfadeinredraw = new Handler();
                    delayfadeinredraw.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            generalSettings.setVisibility(View.GONE);
                        }
                    }, 300); // 300ms

                    // Wait 300ms before sliding in the advanced settings
                    Handler delayfadeinredraw2 = new Handler();
                    delayfadeinredraw2.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            advancedSettings.startAnimation(AnimationUtils
                                    .loadAnimation(V.getContext(),
                                            R.anim.slide_in_right));
                            advancedSettings.setVisibility(View.VISIBLE);
                        }
                    }, 600); // 300ms

                } else {
                    // Slide the advanced settings right
                    advancedSettings.startAnimation(AnimationUtils.loadAnimation(
                            V.getContext(), R.anim.slide_out_right));
                    // Wait 300ms before hiding the advanced settings and unhiding the general settings
                    Handler delayfadeinredraw = new Handler();
                    delayfadeinredraw.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            advancedSettings.setVisibility(View.GONE);
                        }
                    }, 300); // 300ms

                    // Wait 300ms before sliding in the general settings
                    Handler delayfadeinredraw2 = new Handler();
                    delayfadeinredraw2.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            generalSettings.startAnimation(AnimationUtils
                                    .loadAnimation(V.getContext(),
                                            R.anim.slide_in_left));
                            generalSettings.setVisibility(View.VISIBLE);
                        }
                    }, 600); // 300ms
                }
            }
        });

        // Fill in the current values
        // Start with the simple EditTexts
        edit_song_title.setText(FullscreenActivity.mTitle);
        edit_song_author.setText(FullscreenActivity.mAuthor);
        edit_song_copyright.setText(FullscreenActivity.mCopyright);
        edit_song_presentation.setText(FullscreenActivity.mPresentation);
        edit_song_duration.setText(FullscreenActivity.mDuration);
        if (FullscreenActivity.mPreDelay.isEmpty()) {
            predelay_SeekBar.setProgress(0);
            predelay_TextView.setText("");
        } else {
            int val=Integer.parseInt(FullscreenActivity.mPreDelay.replaceAll("[\\D]",""));
            if (val<0) {
                val=0;
            }
            String text = val + " s";
            predelay_SeekBar.setProgress(val+1);
            predelay_TextView.setText(text);
        }
        edit_song_notes.setText(FullscreenActivity.mNotes);
        edit_song_duration.setText(FullscreenActivity.mDuration);
        edit_song_lyrics.setTypeface(Typeface.MONOSPACE);
        // Get the lyrics into a temp string (so we can get rid of rubbish tabs, etc)
        String editBoxLyrics = FullscreenActivity.mLyrics;
        editBoxLyrics = editBoxLyrics.replaceAll("\r\n", "\n");
        editBoxLyrics = editBoxLyrics.replaceAll("\n\r", "\n");
        editBoxLyrics = editBoxLyrics.replaceAll("\t", "    ");
        //editBoxLyrics = editBoxLyrics.replaceAll("\b", "    ");
        editBoxLyrics = editBoxLyrics.replace("\b", "    ");
        editBoxLyrics = editBoxLyrics.replaceAll("\f", "    ");
        edit_song_lyrics.setText(editBoxLyrics);
        edit_song_CCLI.setText(FullscreenActivity.mCCLI);
        edit_song_aka.setText(FullscreenActivity.mAka);
        edit_song_key_line.setText(FullscreenActivity.mKeyLine);
        edit_song_hymn.setText(FullscreenActivity.mHymnNumber);
        edit_song_user1.setText(FullscreenActivity.mUser1);
        edit_song_user2.setText(FullscreenActivity.mUser2);
        edit_song_user3.setText(FullscreenActivity.mUser3);
        edit_song_midi.setText(FullscreenActivity.mMidi);
        edit_song_midi_index.setText(FullscreenActivity.mMidiIndex);
        edit_song_restrictions.setText(FullscreenActivity.mRestrictions);
        edit_song_books.setText(FullscreenActivity.mBooks);
        edit_song_pitch.setText(FullscreenActivity.mPitch);
        abcnotation.setText(FullscreenActivity.mNotation);

        customTheme.setText(FullscreenActivity.mTheme);

/*
        // Now the checkboxes
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_christ_attributes))) {
            edit_song_theme_christ_attributes.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_christ_birth))) {
            edit_song_theme_christ_birth.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_christ_death_atonement))) {
            edit_song_theme_christ_death_atonement.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_christ_love_mercy))) {
            edit_song_theme_christ_love_mercy.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_christ_power_majesty))) {
            edit_song_theme_christ_power_majesty.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_christ_resurrection))) {
            edit_song_theme_christ_resurrection.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_christ_second_coming))) {
            edit_song_theme_christ_second_coming.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_christ_victory))) {
            edit_song_theme_christ_victory.setChecked(true);
        }

        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_church_commitment_obedience))) {
            edit_song_theme_church_commitment_obedience.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_church_country))) {
            edit_song_theme_church_country.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_church_eternal_life_heaven))) {
            edit_song_theme_church_eternal_life_heaven.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_church_evangelism))) {
            edit_song_theme_church_evangelism.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_church_family_fellowship))) {
            edit_song_theme_church_family_fellowship.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_church_fellowship_w_god))) {
            edit_song_theme_church_fellowship_w_god.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_church_purity_holiness))) {
            edit_song_theme_church_purity_holiness.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_church_renewal))) {
            edit_song_theme_church_renewal.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_church_repentance_salvation))) {
            edit_song_theme_church_repentance_salvation.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_church_service_ministry))) {
            edit_song_theme_church_service_ministry.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_church_spiritual_hunger))) {
            edit_song_theme_church_spiritual_hunger.setChecked(true);
        }

        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_fruit_faith_hope))) {
            edit_song_theme_fruit_faith_hope.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_fruit_humility_meekness))) {
            edit_song_theme_fruit_humility_meekness.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_fruit_joy))) {
            edit_song_theme_fruit_joy.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_fruit_love))) {
            edit_song_theme_fruit_love.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_fruit_patience_kindness))) {
            edit_song_theme_fruit_patience_kindness.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_fruit_peace_comfort))) {
            edit_song_theme_fruit_peace_comfort.setChecked(true);
        }

        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_god_attributes))) {
            edit_song_theme_god_attributes.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_god_creator_creation))) {
            edit_song_theme_god_creator_creation.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_god_father))) {
            edit_song_theme_god_father.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_god_guidance_care))) {
            edit_song_theme_god_guidance_care.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_god_holiness))) {
            edit_song_theme_god_holiness.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_god_holy_spirit))) {
            edit_song_theme_god_holy_spirit.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_god_love_mercy))) {
            edit_song_theme_god_love_mercy.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_god_power_majesty))) {
            edit_song_theme_god_power_majesty.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_god_promises))) {
            edit_song_theme_god_promises.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_god_victory))) {
            edit_song_theme_god_victory.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_god_word))) {
            edit_song_theme_god_word.setChecked(true);
        }

        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_worship_assurance_trust))) {
            edit_song_theme_worship_assurance_trust.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_worship_call_opening))) {
            edit_song_theme_worship_call_opening.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_worship_celebration))) {
            edit_song_theme_worship_celebration.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_worship_declaration))) {
            edit_song_theme_worship_declaration.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_worship_intimacy))) {
            edit_song_theme_worship_intimacy.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_worship_invitation))) {
            edit_song_theme_worship_invitation.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_worship_praise_adoration))) {
            edit_song_theme_worship_praise_adoration.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_worship_prayer_devotion))) {
            edit_song_theme_worship_prayer_devotion.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_worship_provision_deliverance))) {
            edit_song_theme_worship_provision_deliverance.setChecked(true);
        }
        if (FullscreenActivity.mTheme.contains(getResources().getString(
                R.string.theme_worship_thankfulness))) {
            edit_song_theme_worship_thankfulness.setChecked(true);
        }
*/

        // Now the Spinners
        // The Key
        ArrayAdapter<CharSequence> song_key = ArrayAdapter.createFromResource(getActivity(),
                R.array.key_choice,
                R.layout.my_spinner);
        song_key.setDropDownViewResource(R.layout.my_spinner);
        edit_song_key.setAdapter(song_key);
        // Where is the key in the available array
        int index = -1;
        List<String> key_choice = Arrays.asList(getResources().getStringArray(R.array.key_choice));
        for (int w=0;w<key_choice.size();w++) {
            if (FullscreenActivity.mKey.equals(key_choice.get(w))) {
                index = w;
            }
        }
        edit_song_key.setSelection(index);

        // The time sig
        ArrayAdapter<CharSequence> time_sigs = ArrayAdapter.createFromResource(getActivity(),
                R.array.timesig,
                R.layout.my_spinner);
        time_sigs.setDropDownViewResource(R.layout.my_spinner);
        edit_song_timesig.setAdapter(time_sigs);
        // Where is the key in the available array
        index = -1;
        List<String> timesig = Arrays.asList(getResources().getStringArray(R.array.timesig));
        for (int w=0;w<timesig.size();w++) {
            if (FullscreenActivity.mTimeSig.equals(timesig.get(w))) {
                index = w;
            }
        }
        edit_song_timesig.setSelection(index);

        // The capo fret
        ArrayAdapter<CharSequence> capo_fret = ArrayAdapter.createFromResource(getActivity(),
                R.array.capo,
                R.layout.my_spinner);
        capo_fret.setDropDownViewResource(R.layout.my_spinner);
        edit_song_capo.setAdapter(capo_fret);
        // Where is the key in the available array
        index = -1;
        List<String> capo_choice = Arrays.asList(getResources().getStringArray(R.array.capo));
        for (int w=0;w<capo_choice.size();w++) {
            if (FullscreenActivity.mCapo.equals(capo_choice.get(w))) {
                index = w;
            }
        }
        edit_song_capo.setSelection(index);

        // The capo print
        ArrayAdapter<CharSequence> capo_print = ArrayAdapter.createFromResource(getActivity(),
                R.array.capoprint,
                R.layout.my_spinner);
        capo_print.setDropDownViewResource(R.layout.my_spinner);
        edit_song_capo_print.setAdapter(capo_print);
        // Where is the key in the available array
        // List<String> capoprint_choice = Arrays.asList(getResources().getStringArray(R.array.capoprint));
        switch (FullscreenActivity.mCapoPrint) {
            case "true":
                edit_song_capo_print.setSelection(1);
                break;
            case "false":
                edit_song_capo_print.setSelection(2);
                break;
            default:
                edit_song_capo_print.setSelection(0);
                break;
        }

        // The pad file
        // Currently only auto or off
        ArrayList<String> pad_option = new ArrayList<>();
        pad_option.add(getResources().getString(R.string.pad_auto));
        pad_option.add(getResources().getString(R.string.link_audio));
        pad_option.add(getResources().getString(R.string.off));
        ArrayAdapter<String> pad_file;
        pad_file = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, pad_option);
        pad_file.setDropDownViewResource(R.layout.my_spinner);
        edit_song_pad_file.setAdapter(pad_file);
        // Only allow auto for now (first index)
        edit_song_pad_file.setSelection(0);


        // Now the seekbars
        String temp_tempo = FullscreenActivity.mTempo;
        temp_tempo = temp_tempo.replace("Very Fast", "140");
        temp_tempo = temp_tempo.replace("Fast", "120");
        temp_tempo = temp_tempo.replace("Moderate", "100");
        temp_tempo = temp_tempo.replace("Slow", "80");
        temp_tempo = temp_tempo.replace("Very Slow", "60");
        temp_tempo = temp_tempo.replaceAll("[\\D]", "");
        // Get numerical value for slider
        try {
            temposlider = Integer.parseInt(temp_tempo);
        } catch(NumberFormatException nfe) {
            temposlider = 39;
        }
        temposlider = temposlider - 39;

        String newtext = getResources().getString(R.string.notset);
        if (temposlider<1) {
            temposlider=0;
        } else {
            newtext = temp_tempo + " " + getResources().getString(R.string.bpm);
        }
        tempo_text.setText(newtext);
        edit_song_tempo.setProgress(temposlider);
        edit_song_tempo.setOnSeekBarChangeListener(new seekBarListener());

        predelay_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress==0) {
                    predelay_TextView.setText("");
                } else {
                    String text = (progress-1)+"s";
                    predelay_TextView.setText(text);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        return V;
    }

    public void cancelEdit() {
        // Load the song back up with the default values
        try {
            LoadXML.loadXML(getActivity());
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
        dismiss();
    }

    public void saveEdit(boolean ended) {

        // Go through the fields and save them
        // Get the variables
        // Set the newtext to the FullscreenActivity variables
        FullscreenActivity.mTitle = edit_song_title.getText().toString();
        FullscreenActivity.mAuthor = edit_song_author.getText().toString();
        FullscreenActivity.mCopyright = edit_song_copyright.getText().toString();
        FullscreenActivity.mLyrics = ProcessSong.fixStartOfLines(edit_song_lyrics.getText().toString());
        FullscreenActivity.mPresentation = edit_song_presentation.getText().toString();
        FullscreenActivity.mHymnNumber = edit_song_hymn.getText().toString();
        FullscreenActivity.mCCLI = edit_song_CCLI.getText().toString();
        FullscreenActivity.mUser1 = edit_song_user1.getText().toString();
        FullscreenActivity.mUser2 = edit_song_user2.getText().toString();
        FullscreenActivity.mUser3 = edit_song_user3.getText().toString();
        FullscreenActivity.mAka = edit_song_aka.getText().toString();
        FullscreenActivity.mKeyLine = edit_song_key_line.getText().toString();
        FullscreenActivity.mKey = edit_song_key.getItemAtPosition(edit_song_key.getSelectedItemPosition()).toString();
        FullscreenActivity.mDuration = edit_song_duration.getText().toString();
        int predelayval = predelay_SeekBar.getProgress();
        if (predelayval==0) {
            FullscreenActivity.mPreDelay = "";
        } else {
            FullscreenActivity.mPreDelay = ""+(predelayval-1);
        }
        FullscreenActivity.mBooks = edit_song_books.getText().toString();
        FullscreenActivity.mMidi = edit_song_midi.getText().toString();
        FullscreenActivity.mMidiIndex = edit_song_midi_index.getText().toString();
        FullscreenActivity.mPitch = edit_song_pitch.getText().toString();
        FullscreenActivity.mRestrictions = edit_song_restrictions.getText().toString();
        FullscreenActivity.mNotes = edit_song_notes.getText().toString();
        FullscreenActivity.mPadFile = edit_song_pad_file.getItemAtPosition(edit_song_pad_file.getSelectedItemPosition()).toString();

        FullscreenActivity.mCapo = edit_song_capo.getItemAtPosition(edit_song_capo.getSelectedItemPosition()).toString();
        int tempmCapoPrint = edit_song_capo_print.getSelectedItemPosition();
        if (tempmCapoPrint==1) {
            FullscreenActivity.mCapoPrint="true";
        } else if (tempmCapoPrint==2) {
            FullscreenActivity.mCapoPrint="false";
        } else {
            FullscreenActivity.mCapoPrint="";
        }
        int valoftempobar = edit_song_tempo.getProgress() + 39;
        if (valoftempobar>39) {
            FullscreenActivity.mTempo = ""+valoftempobar;
        } else {
            FullscreenActivity.mTempo = "";
        }
        FullscreenActivity.mTimeSig = edit_song_timesig.getItemAtPosition(edit_song_timesig.getSelectedItemPosition()).toString();

        FullscreenActivity.mTheme = customTheme.getText().toString();
        if (!FullscreenActivity.mTheme.endsWith(";")) {
            FullscreenActivity.mTheme += ";";
        }

        /*if (edit_song_theme_christ_attributes.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_christ_attributes.getText().toString() + "; ";
        }
        if (edit_song_theme_christ_birth.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_christ_birth.getText().toString() + "; ";
        }
        if (edit_song_theme_christ_death_atonement.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_christ_death_atonement.getText().toString() + "; ";
        }
        if (edit_song_theme_christ_love_mercy.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_christ_love_mercy.getText().toString() + "; ";
        }
        if (edit_song_theme_christ_power_majesty.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_christ_power_majesty.getText().toString() + "; ";
        }
        if (edit_song_theme_christ_resurrection.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_christ_resurrection.getText().toString() + "; ";
        }
        if (edit_song_theme_christ_second_coming.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_christ_second_coming.getText().toString() + "; ";
        }
        if (edit_song_theme_christ_victory.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_christ_victory.getText().toString() + "; ";
        }

        if (edit_song_theme_church_commitment_obedience.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_church_commitment_obedience.getText().toString() + "; ";
        }
        if (edit_song_theme_church_country.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_church_country.getText().toString() + "; ";
        }
        if (edit_song_theme_church_eternal_life_heaven.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_church_eternal_life_heaven.getText().toString() + "; ";
        }
        if (edit_song_theme_church_evangelism.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_church_evangelism.getText().toString() + "; ";
        }
        if (edit_song_theme_church_family_fellowship.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_church_family_fellowship.getText().toString() + "; ";
        }
        if (edit_song_theme_church_fellowship_w_god.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_church_fellowship_w_god.getText().toString() + "; ";
        }
        if (edit_song_theme_church_purity_holiness.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_church_purity_holiness.getText().toString() + "; ";
        }
        if (edit_song_theme_church_renewal.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_church_renewal.getText().toString() + "; ";
        }
        if (edit_song_theme_church_repentance_salvation.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_church_repentance_salvation.getText().toString() + "; ";
        }
        if (edit_song_theme_church_service_ministry.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_church_service_ministry.getText().toString() + "; ";
        }
        if (edit_song_theme_church_spiritual_hunger.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_church_spiritual_hunger.getText().toString() + "; ";
        }

        if (edit_song_theme_fruit_faith_hope.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_fruit_faith_hope.getText().toString() + "; ";
        }
        if (edit_song_theme_fruit_humility_meekness.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_fruit_humility_meekness.getText().toString() + "; ";
        }
        if (edit_song_theme_fruit_joy.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_fruit_joy.getText().toString() + "; ";
        }
        if (edit_song_theme_fruit_love.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_fruit_love.getText().toString() + "; ";
        }
        if (edit_song_theme_fruit_patience_kindness.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_fruit_patience_kindness.getText().toString() + "; ";
        }
        if (edit_song_theme_fruit_peace_comfort.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_fruit_peace_comfort.getText().toString() + "; ";
        }

        if (edit_song_theme_god_attributes.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_god_attributes.getText().toString() + "; ";
        }
        if (edit_song_theme_god_creator_creation.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_god_creator_creation.getText().toString() + "; ";
        }
        if (edit_song_theme_god_father.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_god_father.getText().toString() + "; ";
        }
        if (edit_song_theme_god_guidance_care.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_god_guidance_care.getText().toString() + "; ";
        }
        if (edit_song_theme_god_holiness.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_god_holiness.getText().toString() + "; ";
        }
        if (edit_song_theme_god_holy_spirit.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_god_holy_spirit.getText().toString() + "; ";
        }
        if (edit_song_theme_god_love_mercy.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_god_love_mercy.getText().toString() + "; ";
        }
        if (edit_song_theme_god_power_majesty.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_god_power_majesty.getText().toString() + "; ";
        }
        if (edit_song_theme_god_promises.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_god_promises.getText().toString() + "; ";
        }
        if (edit_song_theme_god_victory.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_god_victory.getText().toString() + "; ";
        }
        if (edit_song_theme_god_word.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_god_word.getText().toString() + "; ";
        }

        if (edit_song_theme_worship_assurance_trust.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_worship_assurance_trust.getText().toString() + "; ";
        }
        if (edit_song_theme_worship_call_opening.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_worship_call_opening.getText().toString() + "; ";
        }
        if (edit_song_theme_worship_celebration.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_worship_celebration.getText().toString() + "; ";
        }
        if (edit_song_theme_worship_declaration.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_worship_declaration.getText().toString() + "; ";
        }
        if (edit_song_theme_worship_intimacy.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_worship_intimacy.getText().toString() + "; ";
        }
        if (edit_song_theme_worship_invitation.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_worship_invitation.getText().toString() + "; ";
        }
        if (edit_song_theme_worship_praise_adoration.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_worship_praise_adoration.getText().toString() + "; ";
        }
        if (edit_song_theme_worship_prayer_devotion.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_worship_prayer_devotion.getText().toString() + "; ";
        }
        if (edit_song_theme_worship_provision_deliverance.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_worship_provision_deliverance.getText().toString() + "; ";
        }
        if (edit_song_theme_worship_thankfulness.isChecked()) {
            FullscreenActivity.mTheme += edit_song_theme_worship_thankfulness.getText().toString() + "; ";
        }
*/
        // Set the AltTheme to the same as the Theme?
        FullscreenActivity.mAltTheme = FullscreenActivity.mTheme;

        // Prepare the new XML file
        prepareSongXML();

        // Makes sure all & are replaced with &amp;
        FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("&amp;","&");
        FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("&","&amp;");

        // Now write the modified song
        try {
            FileOutputStream overWrite;
            if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
                overWrite = new FileOutputStream(
                        FullscreenActivity.dir + "/" + FullscreenActivity.songfilename,
                        false);
            } else {
                overWrite = new FileOutputStream(
                        FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.songfilename,
                        false);
            }
            overWrite.write(FullscreenActivity.mynewXML.getBytes());
            overWrite.flush();
            overWrite.close();

            // If we are autologging CCLI information
            if (FullscreenActivity.ccli_automatic) {
                PopUpCCLIFragment.addUsageEntryToLog(FullscreenActivity.whichSongFolder+"/"+FullscreenActivity.songfilename,
                        FullscreenActivity.mTitle.toString(), FullscreenActivity.mAuthor.toString(),
                        FullscreenActivity.mCopyright.toString(), FullscreenActivity.mCCLI, "3"); // Edited
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        FullscreenActivity.mynewXML = "";

        // Save the preferences
        Preferences.savePreferences();

        if (ended) {
            // Prepare the message
            FullscreenActivity.myToastMessage = getResources().getString(R.string.edit_save) + " - " +
                    getResources().getString(R.string.ok);

            // Now tell the main page to refresh itself with this new song
            // Don't need to reload the XML as we already have all its values
            mListener.refreshAll();

            // Now dismiss this popup
            dismiss();
        }
    }

    private class seekBarListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            String newtext = getResources().getString(R.string.notset);
            temposlider = edit_song_tempo.getProgress()+39;
            if (temposlider>39) {
                newtext = temposlider+" "+getResources().getString(R.string.bpm);
            }
            tempo_text.setText(newtext);
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {}
    }

    public static void prepareBlankSongXML() {
        // Prepare the new XML file
        String myNEWXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        myNEWXML += "<song>\n";
        myNEWXML += "  <title>" + FullscreenActivity.songfilename + "</title>\n";
        myNEWXML += "  <author></author>\n";
        myNEWXML += "  <copyright></copyright>\n";
        myNEWXML += "  <presentation></presentation>\n";
        myNEWXML += "  <hymn_number></hymn_number>\n";
        myNEWXML += "  <capo print=\"\"></capo>\n";
        myNEWXML += "  <tempo></tempo>\n";
        myNEWXML += "  <time_sig></time_sig>\n";
        myNEWXML += "  <duration></duration>\n";
        myNEWXML += "  <predelay></predelay>\n";
        myNEWXML += "  <ccli></ccli>\n";
        myNEWXML += "  <theme></theme>\n";
        myNEWXML += "  <alttheme></alttheme>\n";
        myNEWXML += "  <user1></user1>\n";
        myNEWXML += "  <user2></user2>\n";
        myNEWXML += "  <user3></user3>\n";
        myNEWXML += "  <key></key>\n";
        myNEWXML += "  <aka></aka>\n";
        myNEWXML += "  <key_line></key_line>\n";
        myNEWXML += "  <books></books>\n";
        myNEWXML += "  <midi></midi>\n";
        myNEWXML += "  <midi_index></midi_index>\n";
        myNEWXML += "  <pitch></pitch>\n";
        myNEWXML += "  <restrictions></restrictions>\n";
        myNEWXML += "  <notes></notes>\n";
        myNEWXML += "  <lyrics>[V]\n</lyrics>\n";
        myNEWXML += "  <linked_songs></linked_songs>\n";
        myNEWXML += "  <pad_file></pad_file>\n";
        myNEWXML += "  <custom_chords></custom_chords>\n";
        myNEWXML += "  <link_youtube></link_youtube>\n";
        myNEWXML += "  <link_web></link_web>\n";
        myNEWXML += "  <link_audio></link_audio>\n";
        myNEWXML += "  <loop_audio>false</loop_audio>\n";
        myNEWXML += "  <link_other></link_other>\n";
        myNEWXML += "  <abcnotation></abcnotation>\n";
        myNEWXML += "</song>";
        FullscreenActivity.mynewXML = myNEWXML;
    }

    public static void prepareSongXML() {
        // Prepare the new XML file

        if (FullscreenActivity.mEncoding==null || FullscreenActivity.mEncoding.equals("")) {
            FullscreenActivity.mEncoding = "UTF-8";
        }
        String myNEWXML = "<?xml version=\"1.0\" encoding=\""+FullscreenActivity.mEncoding+"\"?>\n";
        myNEWXML += "<song>\n";
        myNEWXML += "  <title>" + parseToHTMLEntities(FullscreenActivity.mTitle.toString()) + "</title>\n";
        myNEWXML += "  <author>" + parseToHTMLEntities(FullscreenActivity.mAuthor.toString()) + "</author>\n";
        myNEWXML += "  <copyright>" + parseToHTMLEntities(FullscreenActivity.mCopyright.toString()) + "</copyright>\n";
        myNEWXML += "  <presentation>" + parseToHTMLEntities(FullscreenActivity.mPresentation) + "</presentation>\n";
        myNEWXML += "  <hymn_number>" + parseToHTMLEntities(FullscreenActivity.mHymnNumber) + "</hymn_number>\n";
        myNEWXML += "  <capo print=\"" + parseToHTMLEntities(FullscreenActivity.mCapoPrint) + "\">" + parseToHTMLEntities(FullscreenActivity.mCapo) + "</capo>\n";
        myNEWXML += "  <tempo>" + parseToHTMLEntities(FullscreenActivity.mTempo) + "</tempo>\n";
        myNEWXML += "  <time_sig>" + parseToHTMLEntities(FullscreenActivity.mTimeSig) + "</time_sig>\n";
        myNEWXML += "  <duration>" + parseToHTMLEntities(FullscreenActivity.mDuration) + "</duration>\n";
        myNEWXML += "  <predelay>" + parseToHTMLEntities(FullscreenActivity.mPreDelay) + "</predelay>\n";
        myNEWXML += "  <ccli>" + parseToHTMLEntities(FullscreenActivity.mCCLI) + "</ccli>\n";
        myNEWXML += "  <theme>" + parseToHTMLEntities(FullscreenActivity.mTheme) + "</theme>\n";
        myNEWXML += "  <alttheme>" + parseToHTMLEntities(FullscreenActivity.mAltTheme) + "</alttheme>\n";
        myNEWXML += "  <user1>" + parseToHTMLEntities(FullscreenActivity.mUser1) + "</user1>\n";
        myNEWXML += "  <user2>" + parseToHTMLEntities(FullscreenActivity.mUser2) + "</user2>\n";
        myNEWXML += "  <user3>" + parseToHTMLEntities(FullscreenActivity.mUser3) + "</user3>\n";
        myNEWXML += "  <key>" + parseToHTMLEntities(FullscreenActivity.mKey) + "</key>\n";
        myNEWXML += "  <aka>" + parseToHTMLEntities(FullscreenActivity.mAka) + "</aka>\n";
        myNEWXML += "  <key_line>" + parseToHTMLEntities(FullscreenActivity.mKeyLine) + "</key_line>\n";
        myNEWXML += "  <books>" + parseToHTMLEntities(FullscreenActivity.mBooks) + "</books>\n";
        myNEWXML += "  <midi>" + parseToHTMLEntities(FullscreenActivity.mMidi) + "</midi>\n";
        myNEWXML += "  <midi_index>" + parseToHTMLEntities(FullscreenActivity.mMidiIndex) + "</midi_index>\n";
        myNEWXML += "  <pitch>" + parseToHTMLEntities(FullscreenActivity.mPitch) + "</pitch>\n";
        myNEWXML += "  <restrictions>" + parseToHTMLEntities(FullscreenActivity.mRestrictions) + "</restrictions>\n";
        myNEWXML += "  <notes>" + parseToHTMLEntities(FullscreenActivity.mNotes) + "</notes>\n";
        myNEWXML += "  <lyrics>" + parseToHTMLEntities(FullscreenActivity.mLyrics) + "</lyrics>\n";
        myNEWXML += "  <linked_songs>" + parseToHTMLEntities(FullscreenActivity.mLinkedSongs) + "</linked_songs>\n";
        myNEWXML += "  <pad_file>" + parseToHTMLEntities(FullscreenActivity.mPadFile) + "</pad_file>\n";
        myNEWXML += "  <custom_chords>" + parseToHTMLEntities(FullscreenActivity.mCustomChords) + "</custom_chords>\n";
        myNEWXML += "  <link_youtube>" + parseToHTMLEntities(FullscreenActivity.mLinkYouTube) + "</link_youtube>\n";
        myNEWXML += "  <link_web>" + parseToHTMLEntities(FullscreenActivity.mLinkWeb) + "</link_web>\n";
        myNEWXML += "  <link_audio>" + parseToHTMLEntities(FullscreenActivity.mLinkAudio) + "</link_audio>\n";
        myNEWXML += "  <loop_audio>" + parseToHTMLEntities(FullscreenActivity.mLoopAudio) + "</loop_audio>\n";
        myNEWXML += "  <link_other>" + parseToHTMLEntities(FullscreenActivity.mLinkOther) + "</link_other>\n";
        myNEWXML += "  <abcnotation>" + parseToHTMLEntities(FullscreenActivity.mNotation) + "</abcnotation>\n";

        if (!FullscreenActivity.mExtraStuff1.isEmpty()) {
            myNEWXML += "  " + FullscreenActivity.mExtraStuff1 + "\n";
        }
        if (!FullscreenActivity.mExtraStuff2.isEmpty()) {
            myNEWXML += "  " + FullscreenActivity.mExtraStuff2 + "\n";
        }
        myNEWXML += "</song>";

        FullscreenActivity.mynewXML = myNEWXML;
    }

    public static void justSaveSongXML() throws IOException {
        // Only do this if the title isn't welcome to opensongapp!
        if (!FullscreenActivity.mTitle.equals("Welcome to OpenSongApp")) {
            // Now write the modified song
            String filename;
            if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername) || FullscreenActivity.whichSongFolder.isEmpty()) {
                filename = FullscreenActivity.dir + "/" + FullscreenActivity.songfilename;
            } else {
                filename = FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.songfilename;
            }

            FileOutputStream overWrite = new FileOutputStream(filename, false);
            overWrite.write(FullscreenActivity.mynewXML.getBytes());
            overWrite.flush();
            overWrite.close();
        }
    }

    public static String parseToHTMLEntities(String val) {
        if (val==null) {
            val = "";
        }
        // Make sure all vals are unencoded to start with
        // Now HTML encode everything
        val = val.replace("<","&lt;");
        val = val.replace(">","&gt;");
        val = val.replace("&","&amp;");
        val = val.replace("&#39;","'");

        return val;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}