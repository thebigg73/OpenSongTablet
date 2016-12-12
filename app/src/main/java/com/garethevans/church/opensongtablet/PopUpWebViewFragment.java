package com.garethevans.church.opensongtablet;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

public class PopUpWebViewFragment extends DialogFragment {

    static PopUpWebViewFragment newInstance() {
        PopUpWebViewFragment frag;
        frag = new PopUpWebViewFragment();
        return frag;
    }

    WebView webview;
    TextView textview;
    Button closebutton;

    public void onStart() {
        super.onStart();

        // safety check
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
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
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_webview, container, false);
        webview = (WebView) V.findViewById(R.id.webview);
        textview = (TextView) V.findViewById(R.id.textview);
        closebutton = (Button) V.findViewById(R.id.closebutton);

        closebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        if (FullscreenActivity.whattodo.equals("errorlog")) {
            getDialog().setTitle(getActivity().getResources().getString(R.string.search_log));
            webview.setVisibility(View.GONE);
            textview.setVisibility(View.VISIBLE);
            textview.setText(FullscreenActivity.indexlog);
        }
        return V;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }


}
