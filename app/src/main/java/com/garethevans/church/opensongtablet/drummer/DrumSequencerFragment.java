package com.garethevans.church.opensongtablet.drummer;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.customviews.MyMaterialSimpleTextView;
import com.garethevans.church.opensongtablet.databinding.SettingsDrumSequencerBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

public class DrumSequencerFragment extends Fragment {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "DrumSequencerFragment";
    private MainActivityInterface mainActivityInterface;
    private SettingsDrumSequencerBinding myView;
    private SequencerAdapter adapter;
    private Drawable start, stop;
    private String drummer_string="", drummer_website="",
            drummer_main="", drummer_main_fill="", drummer_variation="", drummer_variation_fill="",
            drum_kit_acoustic="", drum_kit_percussion="";
    private final String[] timeSigs = new String[] {"3/4","4/4","5/4","6/8"};
    private String filename = "";
    // Add a flag to prevent listeners from firing during programmatic setup
    private boolean isRefreshingUI = false;
    // Track which view the user is currently touching
    final boolean[] isGridTouching = {false};
    final boolean[] isLabelTouching = {false};

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        mainActivityInterface.getDrumViewModel().getDrummer().setSequencerMode(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = SettingsDrumSequencerBinding.inflate(inflater, container, false);

        // Do everything below on a nonUI thread wherever possible
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            mainActivityInterface.getDrumViewModel().getDrummer().setSequencerMode(false);
            mainActivityInterface.getDrumViewModel().prepareSongValues(mainActivityInterface.getSong());
            mainActivityInterface.getDrumViewModel().getDrummer().setSequencerMode(true);
            String patternName = mainActivityInterface.getDrumViewModel().getDrumPatternJson().getName();
            if (patternName!=null && !patternName.isEmpty()) {
                filename = patternName;
            } else {
                filename = mainActivityInterface.getSong().getFilename();
            }

            // Prepare the strings
            prepareStrings();

            // Update the title
            mainActivityInterface.updateToolbar(drummer_string);
            mainActivityInterface.updateToolbarHelp(drummer_website);

            mainActivityInterface.getDrumViewModel().updateDrummerAndTimer();
            mainActivityInterface.getDrumViewModel().getDrummer().updateActiveMap();
        });

        return myView.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Post the heavy UI work to the end of the message queue
        // This allows the fragment transaction to finish smoothly first
        view.post(() -> {
            setupViews();
            setupIcons();
            setupLabels();
            setupListeners();
            setupGrid();
            setupBeatIndicators();
            setupObserve();
            updateGridSpan();
        });
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            drummer_string = getString(R.string.drummer);
            drummer_website = getString(R.string.website_drummer);
            drummer_main = getString(R.string.drummer_main);
            drummer_main_fill = getString(R.string.drummer_main_fill);
            drummer_variation = getString(R.string.drummer_variation);
            drummer_variation_fill = getString(R.string.drummer_variation_fill);
            drum_kit_acoustic = getString(R.string.drum_kit_acoustic);
            drum_kit_percussion = getString(R.string.drum_kit_percussion);
        }
    }



    private void setupViews() {
        ArrayList<String> tempos = new ArrayList<>();
        for (int x=40; x<=300; x++) {
            tempos.add(String.valueOf(x));
        }
        ArrayList<String> drummerParts = new ArrayList<>();
        drummerParts.add(drummer_main);
        drummerParts.add(drummer_main_fill);
        drummerParts.add(drummer_variation);
        drummerParts.add(drummer_variation_fill);

        ArrayList<String> drummerKits = new ArrayList<>();
        drummerKits.add(drum_kit_acoustic);
        drummerKits.add(drum_kit_percussion);

        if (getContext()!=null) {
            if (myView!=null) {
                ExposedDropDownArrayAdapter tempoAdapter = new ExposedDropDownArrayAdapter(getContext(),
                        myView.tempo,R.layout.view_exposed_dropdown_item,tempos);
                ExposedDropDownArrayAdapter timeSigAdapter = new ExposedDropDownArrayAdapter(getContext(),
                        myView.timeSignature, R.layout.view_exposed_dropdown_item, timeSigs);
                ExposedDropDownArrayAdapter drummerPartAdapter = new ExposedDropDownArrayAdapter(getContext(),
                        myView.drummerPart, R.layout.view_exposed_dropdown_item, drummerParts);
                ExposedDropDownArrayAdapter drumKitAdapter = new ExposedDropDownArrayAdapter(getContext(),
                    myView.drummerKit, R.layout.view_exposed_dropdown_item,drummerKits);
                myView.tempo.postDelayed(() -> {
                    myView.tempo.setAdapter(tempoAdapter);
                    myView.tempo.setText(String.valueOf(DrumCalculations.getFixedTempo(mainActivityInterface.getSong().getTempo(), true)),false);
                },100);
                myView.timeSignature.postDelayed(() -> {
                    myView.timeSignature.setAdapter(timeSigAdapter);
                    myView.timeSignature.setText(DrumCalculations.getFixedTimeSignatureString(mainActivityInterface.getSong().getTimesig(), true),false);
                },100);
                myView.drummerPart.postDelayed(() -> {
                    myView.drummerPart.setAdapter(drummerPartAdapter);

                    // CHANGE THIS: Don't just use 'drummer_main'
                    DrumSection active = mainActivityInterface.getDrumViewModel().getActiveSection().getValue();
                    if (active != null) {
                        // Map the enum back to your translated string list
                        myView.drummerPart.setText(getTranslationForSection(active),false);
                    } else {
                        myView.drummerPart.setText(drummer_main,false);
                    }
                }, 100);
                myView.drummerKit.postDelayed(() -> {
                    myView.drummerKit.setAdapter(drumKitAdapter);
                    myView.drummerKit.setText(mainActivityInterface.getDrumViewModel().getDrummer().getDrummerStyleFromXML(
                            mainActivityInterface.getDrumViewModel().getDrummer().getDrummerStyle()));
                },100);

                myView.filename.post(() -> myView.filename.setText(filename));

            }
        }
    }

    private String getTranslationForSection(DrumSection section) {
        if (section == null) return drummer_main;

        switch (section) {
            case VARIATION:
                return drummer_variation;
            case FILL_MAIN:
                return drummer_main_fill;
            case FILL_VARIATION:
                return drummer_variation_fill;
            case MAIN:
            default:
                return drummer_main;
        }
    }

    private void setupIcons() {
        if (getContext() != null) {
            stop = ContextCompat.getDrawable(getContext(), R.drawable.stop);
            start = ContextCompat.getDrawable(getContext(), R.drawable.play);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                stop.setTintList(ColorStateList.valueOf(mainActivityInterface.getPalette().textColor));
                stop.setTintList(ColorStateList.valueOf(mainActivityInterface.getPalette().textColor));
            }
        }
    }

    private void setupLabels() {
        if (getContext()!=null) {
        if (myView!=null) {
                myView.instrumentLabelContainer.post(() -> {
                    myView.instrumentLabelContainer.removeAllViews();

                    // 1. ADD A SPACER at the top to account for the beat indicators
                    // This spacer must match the height of your beatIndicatorContainer
                    View spacer = new View(getContext());
                    // Assuming beatIndicatorContainer is ~24-30dp, adjust height to match exactly
                    int spacerHeight = dpToPx(24);
                    spacer.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, spacerHeight));
                    myView.instrumentLabelContainer.addView(spacer);
                });

                // 2. Add instrument names as before

                for (int i = 0; i < mainActivityInterface.getDrumViewModel().getDrumSoundManager().getKit().getDrumParts().size(); i++) {
                    String partName = mainActivityInterface.getDrumViewModel().getDrumSoundManager().getKit().getDrumParts().get(i).getPartName();
                    String translation = mainActivityInterface.getDrumViewModel().getDrumSoundManager().getKit().getDrumParts().get(i).getPartTranslation();

                    if (getContext()!=null) {
                        MyMaterialSimpleTextView tv = new MyMaterialSimpleTextView(getContext());
                        tv.setText(translation);
                        tv.setTag(partName);
                        tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
                        tv.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(42))); // Match item height
                        tv.setPadding(0, 0, 16, 0);
                        myView.instrumentLabelContainer.post(() -> {
                            tv.setOnClickListener(view -> {
                                mainActivityInterface.getDrumViewModel().getDrumSoundManager().playDrum(
                                        mainActivityInterface.getDrumViewModel().getDrummer().getCajonPrefixIfNeeded() +
                                                view.getTag().toString(), 127);
                            });
                            myView.instrumentLabelContainer.addView(tv);
                        });
                    }
                }
            }
        }
    }

    private void setupBeatIndicators() {
        myView.beatIndicatorContainer.post(() ->
            myView.beatIndicatorContainer.removeAllViews());

        int totalBeats = mainActivityInterface.getDrumViewModel().getThisBeats();
        int stepsPerPulse = mainActivityInterface.getDrumViewModel().getThisStepsPerPulse();

        // Each "step" in your grid is 40dp wide + 2dp margin (from your xml)
        int stepWidthPx = dpToPx(42);

        if (getContext() != null) {
            for (int i = 1; i <= totalBeats; i++) {
                MyMaterialSimpleTextView tv = new MyMaterialSimpleTextView(getContext());
                tv.setText(String.valueOf(i));
                tv.setTextColor(mainActivityInterface.getPalette().textColor);
                tv.setGravity(Gravity.CENTER);

                // The width of the beat indicator should span all steps in that beat
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        stepWidthPx * stepsPerPulse,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                tv.setLayoutParams(params);

                myView.beatIndicatorContainer.post(()-> myView.beatIndicatorContainer.addView(tv));
            }
        }
    }

    private void setupObserve() {
        mainActivityInterface.getMainHandler().post(() -> {
            // 1. HIGH FREQUENCY: Only update the playhead highlight
            mainActivityInterface.getDrumViewModel().getCurrentStep().observe(getViewLifecycleOwner(), totalSteps -> {
                if (adapter != null && totalSteps != null) {
                    int stepsPerBar = mainActivityInterface.getDrumViewModel().getThisStepsPerBar();
                    adapter.updatePlayhead(totalSteps % stepsPerBar);
                }
            });

            // 2. MEDIUM FREQUENCY: Only fires when switching Intro/Main/Fill
            mainActivityInterface.getDrumViewModel().getActiveSection().observe(getViewLifecycleOwner(), section -> {
                if (adapter != null && section != null) {
                    // This calls notifyDataSetChanged() ONLY when the section actually changes
                    adapter.setSection(section);
                }
            });

            // 3. LOW FREQUENCY: Only fires when loading a new file
            mainActivityInterface.getDrumViewModel().getCurrentPattern().observe(getViewLifecycleOwner(), pattern -> {
                if (pattern == null || isRefreshingUI) return;

                String patternTimeSig = pattern.getBeats() + "/" + pattern.getDivisions();

                // Only update if the UI is actually showing the wrong thing
                if (!myView.timeSignature.getText().toString().equals(patternTimeSig)) {
                    isRefreshingUI = true;
                    myView.timeSignature.setText(patternTimeSig, false);
                    updateGridSpan();
                    isRefreshingUI = false;
                }
            });
        });
    }

    private void resetDrums(boolean emptyPattern, boolean addDefaultPattern) {
        // Stop the drummer first to pause the TimerEngine's step updates
        mainActivityInterface.getDrumViewModel().stopDrummer();

        if (emptyPattern) {
            mainActivityInterface.getDrumViewModel().buildEmptyPattern();
        }
        if (addDefaultPattern) {
            mainActivityInterface.getDrumViewModel().addDefaultPattern();
        }

        int beats = mainActivityInterface.getDrumViewModel().getThisBeats();
        int divisions = mainActivityInterface.getDrumViewModel().getThisDivisions();
        int bpm = mainActivityInterface.getDrumViewModel().getThisBpm();
        mainActivityInterface.getDrumViewModel().updateAllTimingValues(beats, divisions, bpm);
        mainActivityInterface.getDrumViewModel().setCurrentPattern(mainActivityInterface.getDrumViewModel().getDrumPatternJson());
        mainActivityInterface.getDrumViewModel().updateDrummerAndTimer();

        // updateGridSpan now safely detaches/reattaches the adapter
        updateGridSpan();
        setupBeatIndicators();
    }

    private void setupGrid() {
        int steps = mainActivityInterface.getDrumViewModel().getThisStepsPerBar();

        myView.sequencerRecyclerView.getRecycledViewPool().setMaxRecycledViews(0, 100);
        myView.sequencerRecyclerView.setHasFixedSize(true);

        // 1. RecyclerView (Grid) moves the Labels
        myView.sequencerRecyclerView.setOnTouchListener((v, event) -> {
            isGridTouching[0] = true;
            isLabelTouching[0] = false;
            return false; // Don't consume touch, let RV handle it
        });

        myView.sequencerRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // If grid is the leader, scroll labels to match
                if (isGridTouching[0]) {
                    myView.labelScrollView.scrollBy(0, dy);
                }
            }
        });

        // 2. NestedScrollView (Labels) moves the Grid
        myView.labelScrollView.setOnTouchListener((v, event) -> {
            isLabelTouching[0] = true;
            isGridTouching[0] = false;
            return false;
        });

        myView.labelScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldX, oldY) -> {
            // If labels are the leader, scroll grid to match
            if (isLabelTouching[0]) {
                int dy = scrollY - oldY;
                myView.sequencerRecyclerView.scrollBy(0, dy);
            }
        });

        myView.labelScrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        myView.sequencerRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        // 1. Only create the adapter once
        if (adapter == null && getContext() != null) {
            adapter = new SequencerAdapter(getContext());
            myView.sequencerRecyclerView.setAdapter(adapter);
        }

        // 2. Update the existing LayoutManager IMMEDIATELY (no .post())
        RecyclerView.LayoutManager lm = myView.sequencerRecyclerView.getLayoutManager();
        if (lm instanceof GridLayoutManager) {
            ((GridLayoutManager) lm).setSpanCount(steps);
        } else {
            myView.sequencerRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), steps));
        }

        // 3. Optimized settings
        myView.sequencerRecyclerView.setHasFixedSize(true);
        if (myView.sequencerRecyclerView.getItemAnimator() instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) myView.sequencerRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        }

        if (adapter != null) {
            adapter.refreshInstrumentCache();
        }
    }

    private void updateGridSpan() {
        // Get the values directly from the ViewModel to ensure they match the Adapter's math
        int totalSteps = mainActivityInterface.getDrumViewModel().getThisStepsPerBar();
        GridLayoutManager layoutManager = (GridLayoutManager) myView.sequencerRecyclerView.getLayoutManager();

        if (layoutManager != null) {
            // Force a total recycling of views to prevent "Scrap View" contamination
            myView.sequencerRecyclerView.setAdapter(null);
            layoutManager.setSpanCount(totalSteps);
            myView.sequencerRecyclerView.setAdapter(adapter);
        }

        if (adapter != null) {
            adapter.refreshInstrumentCache();
            adapter.notifyDataSetChanged();
        }
    }

    private void setupListeners() {
        myView.playStop.post(() -> myView.playStop.setOnClickListener(view -> {
                mainActivityInterface.getDrumViewModel().toggleDrummer();
                myView.playStop.setImageDrawable(mainActivityInterface.getDrumViewModel().getDrummer().getIsRunning() ? stop : start);
            }));
            myView.resetToDefault.post(() -> myView.resetToDefault.setOnClickListener(view -> resetDrums(true, true)));
            myView.clear.post(() -> myView.clear.setOnClickListener(view -> resetDrums(true, false)));
            myView.filename.post(() -> myView.filename.addTextChangedListener(new MyTextWatcher("name")));
            myView.timeSignature.post(() -> myView.timeSignature.addTextChangedListener(new MyTextWatcher("timesig")));
            myView.tempo.post(() -> myView.tempo.addTextChangedListener(new MyTextWatcher("tempo")));
            myView.load.post(() -> myView.load.setOnClickListener(view -> {
                String filename = myView.filename.getText() == null ? null : myView.filename.getText().toString();
                String timeSig = myView.timeSignature.getText() == null ? null : myView.timeSignature.getText().toString();
                DrummerFileBottomSheet bottomSheet = new DrummerFileBottomSheet(this, "load", filename, timeSig,null);
                bottomSheet.show(mainActivityInterface.getMyFragmentManager(), "DrummerFileBottomSheet");
            }));
            myView.save.post(() -> myView.save.setOnClickListener(view -> {
                String filename = myView.filename.getText() == null ? null : myView.filename.getText().toString();
                String timeSig = myView.timeSignature.getText() == null ? null : myView.timeSignature.getText().toString();
                DrummerFileBottomSheet bottomSheet = new DrummerFileBottomSheet(this, "save", filename, timeSig,null);
                bottomSheet.show(mainActivityInterface.getMyFragmentManager(), "DrummerFileBottomSheet");
            }));
            myView.assign.post(() -> myView.assign.setOnClickListener(view -> {
                String filename = myView.filename.getText() == null ? null : myView.filename.getText().toString();
                String timeSig = myView.timeSignature.getText() == null ? null : myView.timeSignature.getText().toString();
                String drummerKit = mainActivityInterface.getDrumViewModel().getDrummer().getDrummerStyleForSongXML(myView.drummerKit.getText().toString());
                DrummerFileBottomSheet bottomSheet = new DrummerFileBottomSheet(this, "assign", filename, timeSig, drummerKit);
                bottomSheet.show(mainActivityInterface.getMyFragmentManager(), "DrummerFileBottomSheet");
            }));
            myView.drummerPart.post(() -> myView.drummerPart.addTextChangedListener(new MyTextWatcher("drummerPart")));
            myView.drummerKit.post(() -> myView.drummerKit.addTextChangedListener(new MyTextWatcher("drummerKit")));
    }

    private class MyTextWatcher implements TextWatcher {

        private final String what;

        private MyTextWatcher(String what) {
            this.what = what;
        }

        @Override
        public void afterTextChanged(Editable editable) {}

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            int[] timeSig = null;
            if (myView.timeSignature.getText()!=null && !myView.timeSignature.getText().toString().isEmpty()) {
                timeSig = DrumCalculations.getFixedTimeSignature(myView.timeSignature.getText().toString());
            }
            int tempo = -1;
            if (myView.tempo.getText()!=null && !myView.tempo.getText().toString().isEmpty()) {
                tempo = Integer.parseInt(myView.tempo.getText().toString());
            }
            if (timeSig!=null && tempo!=-1) {
                mainActivityInterface.getDrumViewModel().updateAllTimingValues(timeSig[0], timeSig[1], tempo);
            }
            if (adapter!=null) {
                adapter.resetPlayhead();
            }
            switch (what) {
                case "timesig":
                    if (isRefreshingUI) return;

                    if (timeSig != null) {
                        // 1. Update the Model
                        mainActivityInterface.getDrumViewModel().updateAllTimingValues(timeSig[0], timeSig[1], tempo);
                        mainActivityInterface.getSong().setTimesig(charSequence.toString());

                        // 2. Clear the engine
                        mainActivityInterface.getDrumViewModel().stopDrummer();

                        // 3. Trigger the UI sync AFTER the timing values are set
                        resetDrums(true, true);
                    }
                    break;

                case "name":
                    if (myView.filename.isFocused()) {
                        filename = myView.filename.getText().toString();
                    }
                    break;

                case "tempo":
                    if (myView.tempo.isFocused()) {
                        mainActivityInterface.getDrumViewModel().stopDrummer();
                        resetDrums(false, false);
                    }
                    break;

                case "drummerPart":
                    if (isRefreshingUI) return;
                    mainActivityInterface.getDrumViewModel().stopDrummer();
                    String part = myView.drummerPart.getText().toString();
                    DrumSection section;
                    if (part.equals(drummer_main)) {
                        section = DrumSection.MAIN;
                    } else if (part.equals(drummer_main_fill)) {
                        section = DrumSection.FILL_MAIN;
                    } else if (part.equals(drummer_variation)) {
                        section = DrumSection.VARIATION;
                    } else {
                        section = DrumSection.FILL_VARIATION;
                    }
                    mainActivityInterface.getDrumViewModel().updateActiveSection(section);

                    if (adapter != null) {
                        adapter.setSection(section);
                    }
                    break;

                case "drummerKit":
                    String kit = myView.drummerKit.getText().toString();
                    mainActivityInterface.getDrumViewModel().getDrummer().setDrummerStyle(
                            mainActivityInterface.getDrumViewModel().getDrummer().getDrummerStyleForSongXML(kit));
                    break;

            }
        }
    }

    public void updateFilename(String filename) {
        this.filename = filename;
        myView.filename.setText(filename);
        mainActivityInterface.getDrumViewModel().getDrumPatternJson().setName(filename);
        if (mainActivityInterface.getDrumViewModel().getCurrentPattern().getValue() != null) {
            mainActivityInterface.getDrumViewModel().getCurrentPattern().getValue().setName(filename);
        }
    }

    public void updateViews() {
        DrumPatternJson drumPatternJson = mainActivityInterface.getDrumViewModel().getDrumPatternJson();
        mainActivityInterface.getMainHandler().post(() -> {
            if (myView != null) {
                myView.filename.setText(drumPatternJson.getName());
                myView.timeSignature.setText(drumPatternJson.getBeats() + "/" + drumPatternJson.getDivisions(),false);
            }
        });
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivityInterface.getDrumViewModel().getDrummer().setSequencerMode(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mainActivityInterface.getDrumViewModel().getDrummer().setSequencerMode(true);
    }
}