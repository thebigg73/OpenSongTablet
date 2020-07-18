/*
package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.garethevans.church.opensongtablet.OLD_TO_DELETE._CustomAnimations;
import com.garethevans.church.opensongtablet.OLD_TO_DELETE._PopUpSizeAndAlpha;
import com.garethevans.church.opensongtablet.OLD_TO_DELETE._ShowToast;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

// This deals with all aspects of the CCLI stuff

public class PopUpCCLIFragment extends DialogFragment {

    static PopUpCCLIFragment newInstance() {
        PopUpCCLIFragment frag;
        frag = new PopUpCCLIFragment();
        return frag;
    }

    public interface MyInterface {
        void prepareOptionMenu();
    }

    private static MyInterface mListener;

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        super.onAttach(activity);
    }

    private TextView title, churchNameTextView, licenceTextView, logTextView, resetText;
    private EditText churchNameEditText,licenceEditText;
    private WebView logWebView;
    private FloatingActionButton saveMe;
    private ArrayList<String> songfile;
    private ArrayList<String> song;
    private ArrayList<String> author;
    private ArrayList<String> copyright;
    private ArrayList<String> ccli;
    private ArrayList<String> date;
    private ArrayList<String> time;
    private ArrayList<String> action;
    private _Preferences preferences;

    static boolean createBlankXML(Context c, _Preferences preferences) {
        String blankXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<log></log>\n";
        StorageAccess storageAccess = new StorageAccess();
        Uri uri = storageAccess.getUriForItem(c, preferences, "Settings", "", "ActivityLog.xml");

        // Check the uri exists for the outputstream to be valid
        storageAccess.lollipopCreateFileForOutputStream(c, preferences, uri, null, "Settings", "", "ActivityLog.xml");

        OutputStream outputStream = storageAccess.getOutputStream(c, uri);
        return storageAccess.writeFileFromString(blankXML, outputStream);
    }




}*/
