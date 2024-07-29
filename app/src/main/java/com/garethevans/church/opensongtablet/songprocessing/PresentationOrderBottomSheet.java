package com.garethevans.church.opensongtablet.songprocessing;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.gridlayout.widget.GridLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.BottomSheetEditSongOrderBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.RecyclerInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

public class PresentationOrderBottomSheet extends BottomSheetDialogFragment implements RecyclerInterface {
    private BottomSheetEditSongOrderBinding myView;
    private MainActivityInterface mainActivityInterface;
    private final Fragment callingFragment;
    private final String fragName;
    private PresentationOrderAdapter presentationOrderAdapter;
    private final String TAG = "PresOrderBottomSheet";

    public PresentationOrderBottomSheet() {
        // Default constructor required to avoid re-instantiation failures
        // Just close the bottom sheet
        callingFragment = null;
        fragName = "";
        dismiss();
    }

    PresentationOrderBottomSheet(Fragment callingFragment, String fragName) {
        this.callingFragment = callingFragment;
        this.fragName = fragName;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialog1 -> {
            FrameLayout bottomSheet = ((BottomSheetDialog) dialog1).findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                BottomSheetBehavior.from(bottomSheet).setDraggable(false);
            }
        });
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = requireActivity().getWindow();
        if (w != null) {
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetEditSongOrderBinding.inflate(inflater, container, false);

        myView.dialogHeading.setClose(this);

        // Prepare views
        prepareViews();

        return myView.getRoot();
    }

    private void prepareViews() {
        // Update the recycler view
        if (callingFragment!=null) {
            presentationOrderAdapter = new PresentationOrderAdapter(requireContext(), this, mainActivityInterface,
                    callingFragment, fragName, this);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
            linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
            myView.currentSections.setLayoutManager(new LinearLayoutManager(requireContext()));
            myView.currentSections.setAdapter(presentationOrderAdapter);
            ItemTouchHelper.Callback callback = new PresoOrderItemTouchHelper(presentationOrderAdapter);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
            itemTouchHelper.attachToRecyclerView(myView.currentSections);

            // The adapter above sorted the available headings.
            // Now create buttons to add them
            for (String heading : mainActivityInterface.getTempSong().getSongSectionHeadings()) {
                MaterialButton button = new MaterialButton(requireContext());
                button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorSecondary)));
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.setMargins(12, 6, 6, 12);
                params.setGravity(Gravity.CENTER_HORIZONTAL);
                button.setLayoutParams(params);
                button.setText(heading);
                button.setOnClickListener(v -> addSection(heading));
                myView.sectionButtons.addView(button);
            }
            myView.deletePresOrder.setOnClickListener(v -> {
                presentationOrderAdapter.reset();
                checkViewsToShow();
            });

            // Check if we should show the recycler or the not set view
            checkViewsToShow();
        } else {
            dismiss();
        }
    }

    private void addSection(String sectionToAdd) {
        Log.d(TAG, "sectionToAdd: " + sectionToAdd);
        presentationOrderAdapter.onItemAdded(sectionToAdd);
        checkViewsToShow();
    }

    private void checkViewsToShow() {
        if (mainActivityInterface.getTempSong().getPresentationorder()==null ||
                mainActivityInterface.getTempSong().getPresentationorder().isEmpty()) {
            myView.currentSections.setVisibility(View.GONE);
            myView.deletePresOrder.setVisibility(View.GONE);
            myView.currentSectionsTextView.setHint(getString(R.string.is_not_set));
        } else {
            myView.currentSectionsTextView.setHint(getString(R.string.presentation_order_info));
            myView.currentSections.setVisibility(View.VISIBLE);
            myView.deletePresOrder.setVisibility((View.VISIBLE));
        }
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        // Notification that an item has moved.
        Log.d(TAG,"Item moved from "+fromPosition+" to "+toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        // Notification that an item has been removed
        Log.d(TAG,"Item removed from "+position);
        checkViewsToShow();
    }

    public void showWarning(String warning) {
        if (warning!=null && !warning.isEmpty()) {
            myView.warningText.setText(warning);
            myView.warningText.setVisibility(View.VISIBLE);
        } else {
            myView.warningText.setVisibility(View.GONE);
        }
    }
}
