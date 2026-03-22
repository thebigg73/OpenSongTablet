package com.garethevans.church.opensongtablet.webserver;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.BottomSheetCommon;
import com.garethevans.church.opensongtablet.customviews.MyFloatingActionButton;
import com.garethevans.church.opensongtablet.customviews.MyMaterialEditText;
import com.garethevans.church.opensongtablet.databinding.BottomSheetWebserverMessagesBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class WebServerMessagesBottomSheet extends BottomSheetCommon {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "WebServerMessagesBS";
    private BottomSheetWebserverMessagesBinding myView;
    private MainActivityInterface mainActivityInterface;
    private String title="", webserver_website="", webserver_message="";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = BottomSheetWebserverMessagesBinding.inflate(inflater, null, false);

        return myView.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialise the 'close' floatingactionbutton
        myView.dialogHeading.setClose(this);

        prepareStrings();

        myView.dialogHeading.setWebHelp(mainActivityInterface, webserver_website);

        setupViews();
        setListeners();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            webserver_website = getString(R.string.website_web_server);
            webserver_message = getString(R.string.message);
        }
    }
    private void setupViews() {
        updateWebServerMessage(myView.webServerMessage1,1);
        updateWebServerMessage(myView.webServerMessage2,2);
        updateWebServerMessage(myView.webServerMessage3,3);
        updateWebServerMessage(myView.webServerMessage4,4);
        updateWebServerMessage(myView.webServerMessage5,5);
    }

    private void setListeners() {
        setWebServerListeners(myView.webServerMessage1, myView.webServerMessage1Send, 1);
        setWebServerListeners(myView.webServerMessage2, myView.webServerMessage2Send, 2);
        setWebServerListeners(myView.webServerMessage3, myView.webServerMessage3Send, 3);
        setWebServerListeners(myView.webServerMessage4, myView.webServerMessage4Send, 4);
        setWebServerListeners(myView.webServerMessage5, myView.webServerMessage5Send, 5);
    }

    private void updateWebServerMessage(MyMaterialEditText whichView, int messageNumber) {
        whichView.setText(mainActivityInterface.getWebServer().getWebServerMessage(messageNumber));
        String hintText = webserver_message + " " + messageNumber;
        whichView.setHint(hintText);
    }

    private void setWebServerListeners(MyMaterialEditText myMaterialEditText, MyFloatingActionButton myFloatingActionButton, int messageNumber) {
        myMaterialEditText.addTextChangedListener(new MyTextWatcher(messageNumber));
        myFloatingActionButton.setOnClickListener(view -> {
            mainActivityInterface.getWebServer().sendWebServerMessage(messageNumber);
            mainActivityInterface.getNearbyActions().getNearbySendPayloads().sendWebServerMessage(messageNumber);
        });
    }

    private class MyTextWatcher implements TextWatcher {
        private final int messageNumber;

        MyTextWatcher(int messageNumber) {
            this.messageNumber = messageNumber;
        }

        @Override
        public void afterTextChanged(Editable editable) {
            mainActivityInterface.getWebServer().setWebServerMessage(messageNumber, editable.toString());
            mainActivityInterface.getPreferences().setMyPreferenceString("webServerMessage"+messageNumber, editable.toString());
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
    }
}
