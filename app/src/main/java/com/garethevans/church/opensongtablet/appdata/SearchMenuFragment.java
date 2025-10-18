package com.garethevans.church.opensongtablet.appdata;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsSearchMenuBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class SearchMenuFragment extends Fragment {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "SearchMenuFragment";
    private MainActivityInterface mainActivityInterface;
    private SettingsSearchMenuBinding myView;
    private SearchSettingsAdapter searchSettingsAdapter;
    private String title="";
    private final String webSearchAddress = "https://www.opensongapp.com/_/search?universe=classic&scope=site&showCloudSearchTab=false&query=";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public @org.jetbrains.annotations.Nullable View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = SettingsSearchMenuBinding.inflate(inflater, container, false);

        myView.getRoot().setBackgroundColor(mainActivityInterface.getPalette().background);

        prepareStrings();
        setupViews();
        setupListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            title = getString(R.string.search_settings);
        }
    }
    private void setupViews() {
        if (getContext()!=null) {
            mainActivityInterface.updateToolbar(title);
            myView.displayedItems.setLayoutManager(new LinearLayoutManager(getContext()));
            searchSettingsAdapter = new SearchSettingsAdapter(getContext(),this);
            myView.displayedItems.setAdapter(searchSettingsAdapter);
            myView.searchBox.setEnabled(true);
            myView.searchBox.post(() -> {
                myView.searchBox.requestFocus();
                mainActivityInterface.getWindowFlags().showKeyboard();
            });
        }
    }

    private void setupListeners() {
        myView.searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {}

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (searchSettingsAdapter!=null) {
                    searchSettingsAdapter.filterAndRank(myView.searchBox.getText().toString());
                }
            }
        });
        myView.searchOnline.setOnClickListener(view -> mainActivityInterface.openDocument(webSearchAddress+myView.searchBox.getText().toString()));
    }
}
