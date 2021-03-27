package com.garethevans.church.opensongtablet.nearby;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.autoscroll.AutoscrollActions;
import com.garethevans.church.opensongtablet.databinding.SettingsNearbyconnectionsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.TextInputDialogFragment;

public class NearbyConnectionsFragment extends Fragment {

    private SettingsNearbyconnectionsBinding myView;
    private MainActivityInterface mainActivityInterface;
    private NearbyConnections nearbyConnections;
    private AutoscrollActions autoscrollActions;
    private Preferences preferences;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        nearbyConnections = mainActivityInterface.getNearbyConnections(mainActivityInterface);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsNearbyconnectionsBinding.inflate(inflater,container,false);

        mainActivityInterface.updateToolbar(null,getString(R.string.settings) + " / " + getString(R.string.connections_connect));

        // Set the helpers
        setHelpers();

        // Update the views
        updateViews();

        // Set the listeners
        setListeners();

        return myView.getRoot();
    }

    private void setHelpers() {
        preferences = mainActivityInterface.getPreferences();
        autoscrollActions = mainActivityInterface.getAutoscrollActions();
        mainActivityInterface.registerFragment(this,"NearbyConnectionsFragment");
        mainActivityInterface.setNearbyOpen(true);
    }

    public void updateViews() {
        ((TextView) myView.deviceButton.findViewById(R.id.subText)).setText(nearbyConnections.getUserNickname());
        myView.actAsHost.setChecked(nearbyConnections.isHost);
        myView.receiveHostFiles.setChecked(nearbyConnections.receiveHostFiles);
        myView.keepHostFiles.setChecked(nearbyConnections.keepHostFiles);
        myView.enableNearby.setChecked(nearbyConnections.usingNearby);
        updateConnectionsLog();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivityInterface.setNearbyOpen(false);
    }

    public void updateConnectionsLog() {
        if (nearbyConnections.connectionLog ==null) {
            nearbyConnections.connectionLog = "";
        }
        ((TextView)myView.connectionsLog.findViewById(R.id.subText)).setText(nearbyConnections.connectionLog);
    }

    public void setListeners() {
        myView.deviceButton.setOnClickListener(v -> textInputDialog());
        myView.enableNearby.setOnCheckedChangeListener((buttonView, isChecked) -> {
            nearbyConnections.usingNearby = isChecked;
            if (isChecked) {
                if (nearbyConnections.isHost) {
                    nearbyConnections.startAdvertising(autoscrollActions);
                } else {
                    nearbyConnections.startDiscovery(autoscrollActions);
                }
            } else {
                // Beacause the host button can be switched on/off as well, when turning off, run both
                // These will catch errors
                nearbyConnections.stopDiscovery();
                nearbyConnections.stopAdvertising();
                nearbyConnections.turnOffNearby();
            }
        });
        myView.keepHostFiles.setOnCheckedChangeListener((buttonView, isChecked) -> nearbyConnections.keepHostFiles = isChecked);
        myView.receiveHostFiles.setOnCheckedChangeListener((buttonView, isChecked) -> nearbyConnections.receiveHostFiles = isChecked);
        myView.actAsHost.setOnCheckedChangeListener((buttonView, isChecked) -> nearbyConnections.isHost = isChecked);
        myView.connectionsLog.setOnClickListener(v -> {
            nearbyConnections.connectionLog = "";
            updateConnectionsLog();
        });
    }

    private void textInputDialog() {
        TextInputDialogFragment dialogFragment = new TextInputDialogFragment(preferences, this,
                "NearbyConnectionsFragment", getString(R.string.connections_device_name), getString(R.string.connections_device_name),
                "deviceName", nearbyConnections.deviceName);
        dialogFragment.show(requireActivity().getSupportFragmentManager(), "textInputFragment");
    }

    // Called from MainActivity after TextInputDialogFragment save
    public void updateValue(String which, String value) {
        if (which.equals("deviceName")) {
            ((TextView) myView.deviceButton.findViewById(R.id.subText)).setText(value);
            nearbyConnections.deviceName = value;
        }
    }
}


/*package com.garethevans.church.opensongtablet;

        import android.app.Dialog;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.view.Window;
        import android.widget.EditText;
        import android.widget.TextView;

        import androidx.annotation.NonNull;
        import androidx.fragment.app.DialogFragment;

        import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PopUpConnectFragment extends DialogFragment {

    static PopUpConnectFragment newInstance() {
        PopUpConnectFragment frag;
        frag = new PopUpConnectFragment();
        return frag;
    }

    public interface MyInterface {
        void prepareOptionMenu();
    }

    private static MyInterface mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        mListener = (MyInterface) context;
        super.onAttach(context);
    }

    private TextView title, deviceNameTextView;
    private EditText deviceNameEditText;
    private FloatingActionButton saveMe;
    private Preferences preferences;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.dismiss();
        }
        if (getDialog() == null) {
            dismiss();
        }

        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View V = inflater.inflate(R.layout.popup_connect, container, false);

        // Set the title based on the whattodo

        title = V.findViewById(R.id.dialogtitle);
        title.setText(getString(R.string.connections_connect));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe, getContext());
            closeMe.setEnabled(false);
            doSave();
        });
        saveMe = V.findViewById(R.id.saveMe);

        preferences = new Preferences();

        // Initialise the views
        deviceNameEditText = V.findViewById(R.id.deviceNameEditText);
        deviceNameTextView = V.findViewById(R.id.deviceNameTextView);
        // IV - Changed to DeviceId which is the pref used by getUserNickname()
        deviceNameEditText.setText(preferences.getMyPreferenceString(getContext(), "deviceId", ""));

        // Set up save/tick listener
        saveMe.setOnClickListener(view -> {
            String s = deviceNameEditText.getText().toString().trim();
            if (s.length() > 0) {
                preferences.setMyPreferenceString(getContext(), "deviceId", s);
                StaticVariables.deviceName = s;
            }
            doSave();
        });
        Dialog dialog = getDialog();
        if (dialog!=null && getContext()!=null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),dialog, preferences);
        }
        return V;
    }

    private void doSave() {
        if (mListener!=null) {
            mListener.prepareOptionMenu();
        }
        try {
            dismiss();
        } catch (Exception e) {
            Log.d("d","Error closing fragment");
        }
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        try {
            this.dismiss();
        } catch (Exception e) {
            Log.d("d","Error closing the fragment");
        }
    }
}*/
