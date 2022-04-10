package com.garethevans.church.opensongtablet.preferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public class ProfileActions {

    private final String TAG = "ProfileActions";
    private final Context c;
    private final MainActivityInterface mainActivityInterface;

    public ProfileActions(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
    }
    // Deal with loading and saving the profiles
    public boolean loadProfile(Uri uri) {
        // This is uses to copy the external file on top of the application preferences

        InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(uri);

        try {
            XmlPullParserFactory factory;
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp;
            xpp = factory.newPullParser();
            xpp.setInput(inputStream, "utf-8");
            int eventType;

            // Extract all of the stuff we need
            eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && !xpp.getName().equals("map")) {
                    String type = xpp.getName();
                    String key = "";
                    String value = "";
                    if (type.equals("boolean") || type.equals("string") || type.equals("int") || type.equals("float")) {
                        // This is an new preference file which has entries like <int name="key" value="1" />
                        if (xpp.getAttributeCount()>0) {
                            key = xpp.getAttributeValue(0);
                        }
                        if (type.equals("string")) {
                            try {
                                value = xpp.nextText();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (xpp.getAttributeCount()>1) {
                            value = xpp.getAttributeValue(1);
                        }
                    } else {
                        // This is an older preference file which has entries like <key>value</key>
                        key = xpp.getName();
                        try {
                            value = xpp.nextText();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // Now try to guess the type!!!
                        // Assume text
                        type = "string";
                        if (value.equals("") || value.isEmpty()) {
                            type = "string";
                        } else if (value.equals("true") || value.equals("false")) {
                            type = "boolean";
                        } else if (value.endsWith("f")) {
                            // Could be a float
                            try {
                                Float.parseFloat(value);
                                type = "float";
                            } catch (Exception e) {
                                Log.d(TAG, key+" isn't a float!");
                            }
                        } else {
                            // Could be an int
                            try {
                                Integer.parseInt(value);
                                type = "int";
                            } catch (Exception e) {
                                Log.d(TAG, key+" isn't a int!");
                            }
                        }
                    }

                    if (key!=null && !key.isEmpty()) {
                        switch (type) {
                            case "boolean":
                                try {
                                    mainActivityInterface.getPreferences().setMyPreferenceBoolean(key, value.equals("true"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "string":
                                try {
                                    if (!key.equals("uriTree") && !key.equals("uriTreeHome")) {
                                        // Don't overwrite our storage location reference!!
                                        mainActivityInterface.getPreferences().setMyPreferenceString(key, value);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "int":
                                try {
                                    mainActivityInterface.getPreferences().setMyPreferenceInt(key, Integer.parseInt(value));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "float":
                                try {
                                    mainActivityInterface.getPreferences().setMyPreferenceFloat(key, Float.parseFloat(value));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                    }
                }
                try {
                    eventType = xpp.next();
                } catch (Exception e) {
                    Log.d(TAG,"Finished");
                }
            }
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean saveProfile(Uri uri) {
        boolean result = true;  // Returns true on success.  Catches throw to false
        try {
            // This is used to copy the current preferences xml file to the chosen name / location
            // Check the file exists, if not create it
            if (!mainActivityInterface.getStorageAccess().uriExists(uri)) {
                String name = uri.getLastPathSegment();
                mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, uri, null, "Profiles", "", name);
            }

            // Different versions of Android save the preferences in different locations.
            Uri prefsFile = getPrefsFile();

            InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(prefsFile);
            OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(uri);

            mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream);
        } catch (Exception e) {
            Log.d(TAG,"Error saving");
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    @SuppressLint("SdCardPath")
    private Uri getPrefsFile() {
        Uri uri;
        File root;

        //Try the Samsung version
        root = new File("/dbdata/databases/" + c.getPackageName() + "/shared_prefs/");
        uri = Uri.fromFile(root);

        // If not there, try the default
        if (uri==null || !mainActivityInterface.getStorageAccess().uriExists(uri)) {
            // Use the default method
            root = new File("/data/data/" + c.getPackageName() + "/shared_prefs/CurrentPreferences.xml");
            uri = Uri.fromFile(root);
        }
        return uri;
    }

    public void resetPreferences() {
        SharedPreferences.Editor editor = mainActivityInterface.getPreferences().getSharedPref().edit();
        editor.clear();
        editor.apply();
    }
}
