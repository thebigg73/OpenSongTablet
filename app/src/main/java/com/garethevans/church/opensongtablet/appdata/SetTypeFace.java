package com.garethevans.church.opensongtablet.appdata;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import androidx.core.provider.FontRequest;
import androidx.core.provider.FontsContractCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

public class SetTypeFace {
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "SetTypeFace";
    private final Context c;
    private final MainActivityInterface mainActivityInterface;

    // The fonts used in the app
    private Typeface lyricFont;
    private Typeface chordFont;
    private Typeface presoFont;
    private Typeface presoInfoFont;
    private Typeface stickyFont;
    private Typeface monoFont;

    private String lyricFontName, chordFontName, monoFontName;

    public SetTypeFace(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
    }

    // Set the fonts
    public void setLyricFont(String lyricFontName, Typeface lyricFont) {
        this.lyricFontName = lyricFontName;
        this.lyricFont = lyricFont;
    }
    public void setChordFont(String chordFontName, Typeface chordFont) {
        this.chordFontName = chordFontName;
        this.chordFont = chordFont;
    }
    public void setPresoFont(Typeface presoFont) {
        this.presoFont = presoFont;
    }
    public void setPresoInfoFont(Typeface presoInfoFont) {
        this.presoInfoFont = presoInfoFont;
    }
    public void setStickyFont(Typeface stickyFont) {
        this.stickyFont = stickyFont;
    }
    public void setMonoFont(Typeface monoFont) {
        this.monoFont = monoFont;
    }

    // Get the fonts
    public Typeface getLyricFont() {
        return lyricFont;
    }
    public Typeface getChordFont() {
        return chordFont;
    }
    public Typeface getPresoFont() {
        return presoFont;
    }
    public Typeface getPresoInfoFont() {
        return presoInfoFont;
    }
    public Typeface getStickyFont() {
        return stickyFont;
    }
    public Typeface getMonoFont() {
        return monoFont;
    }

    // Set the fonts used from preferences
    public void setUpAppFonts(Handler lyricFontHandler,
                              Handler chordFontHandler, Handler stickyFontHandler,
                              Handler presoFontHandler, Handler presoInfoFontHandler) {

        // Load up the user preferences
        lyricFontName = mainActivityInterface.getPreferences().getMyPreferenceString("fontLyric", "Lato");
        chordFontName = mainActivityInterface.getPreferences().getMyPreferenceString("fontChord", "Lato");
        monoFontName = "RobotoMono";
        String fontSticky = mainActivityInterface.getPreferences().getMyPreferenceString("fontSticky", "Lato");
        String fontPreso = mainActivityInterface.getPreferences().getMyPreferenceString("fontPreso", "Lato");
        String fontPresoInfo = mainActivityInterface.getPreferences().getMyPreferenceString("fontPresoInfo", "Lato");

        // Set the values  (if we don't have play services, use the bundled fonts)
        // The reason is that KiKat devices don't load the Google Font resource automatically (it requires manually selecting it).
        changeFont("fontLyric",lyricFontName,lyricFontHandler);
        changeFont("fontChord",chordFontName,chordFontHandler);
        changeFont("fontPreso",fontPreso,presoFontHandler);
        changeFont("fontPresoInfo",fontPresoInfo,presoInfoFontHandler);
        changeFont("fontSticky",fontSticky,stickyFontHandler);
        setMonoFont(Typeface.createFromAsset(c.getAssets(),"font/robotomono.ttf"));
    }

    public void changeFont(String which, String fontName, Handler handler) {
        // Save the preferences
        mainActivityInterface.getPreferences().setMyPreferenceString(which,fontName);
        // Update the font
        if (fontName.startsWith("Fonts/")) {
            try {
                String actualName = fontName.replace("Fonts/", "");
                // We need the font to be in a file readable location - the app storage
                // Copy the chosen file here
                File dir = c.getExternalFilesDir("files");
                File fontFile = new File(dir, actualName);
                Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Fonts", "", actualName);
                InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(uri);
                FileOutputStream outputStream = new FileOutputStream(fontFile);
                mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream);
                Typeface typeface = Typeface.createFromFile(fontFile.getPath());
                doSetDesiredFont(which, typeface, fontName, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (!mainActivityInterface.getAlertChecks().getHasPlayServices()) {
            // Use the bundled lato font
            Typeface typeface = Typeface.createFromAsset(c.getAssets(),"font/lato.ttf");
            doSetDesiredFont(which, typeface, fontName, null);

        } else {
            getGoogleFont(fontName, which, null, handler);
        }
    }

    public void getGoogleFont(String fontName, String which, TextView textView, Handler handler) {
        FontRequest fontRequest = getFontRequest(fontName);
        FontsContractCompat.FontRequestCallback fontRequestCallback = getFontRequestCallback(fontName,which,textView);
        FontsContractCompat.requestFont(c,fontRequest,fontRequestCallback,handler);
    }

    private FontRequest getFontRequest(String fontnamechosen) {
        return new FontRequest("com.google.android.gms.fonts",
                "com.google.android.gms", fontnamechosen,
                R.array.com_google_android_gms_fonts_certs);
    }

    private FontsContractCompat.FontRequestCallback getFontRequestCallback(final String fontName,
                                                                           final String which,
                                                                           final TextView textView) {
        return new FontsContractCompat.FontRequestCallback() {
            @Override
            public void onTypefaceRetrieved(Typeface typeface) {
                // Set the desired font
                setDesiredFont(typeface,fontName);
            }

            @Override
            public void onTypefaceRequestFailed(int reason) {
                // Default to Lato
                Typeface typeface = Typeface.createFromAsset(c.getAssets(), "font/lato.ttf");
                setDesiredFont(typeface,"Lato");
            }

            private void setDesiredFont(Typeface typeface, String thisFont) {
                doSetDesiredFont(which,typeface,thisFont,textView);
            }
        };
    }

    private void doSetDesiredFont(String which, Typeface typeface, String fontName,TextView textView) {
        // Set the desired font
        switch (which) {
            case "fontLyric":
                setLyricFont(fontName,typeface);
                break;
            case "fontChord":
                setChordFont(fontName,typeface);
                break;
            case "fontSticky":
                setStickyFont(typeface);
                break;
            case "fontPreso":
                setPresoFont(typeface);
                break;
            case "fontPresoInfo":
                setPresoInfoFont(typeface);
                break;
        }
        mainActivityInterface.getPreferences().setMyPreferenceString(which,fontName);

        // If we are previewing the font, update the text (this will be null otherwise)
        if (textView != null) {
            textView.setTypeface(typeface);
        }
    }
    public ArrayList<String> bundledFonts() {
        ArrayList<String> f = new ArrayList<>();
        f.add("Lato");
        return f;
    }

    private boolean hasPlayServices() {
        if (mainActivityInterface.getAlertChecks().getIgnorePlayServicesWarning()) {
            return false;
        } else {
            return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(c) == ConnectionResult.SUCCESS;
        }
    }

    public Typeface getAppDefault() {
        return Typeface.createFromAsset(c.getAssets(), "font/lato.ttf");
    }
    public ArrayList<String> getFontsFromGoogle() {
        ArrayList<String> fontNames;
        String response = null;
        try {
            URL url = new URL("https://www.googleapis.com/webfonts/v1/webfonts?key=AIzaSyBKvCB1NnWwXGyGA7RTar0VQFCM3rdOE8k&sort=alpha");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                response = stringBuilder.toString();
            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        fontNames = new ArrayList<>();

        if (response == null || !hasPlayServices()) {
            // Set up the custom fonts - use my preferred Google font lists as local files no longer work!!!
            fontNames = bundledFonts();

        } else {
            // Split the returned JSON into lines
            String[] lines = response.split("\n");

            if (lines.length > 0) {
                for (String line : lines) {
                    if (line.contains("\"family\":")) {
                        line = line.replace("\"family\"", "");
                        line = line.replace(":", "");
                        line = line.replace("\"", "");
                        line = line.replace(",", "");
                        line = line.trim();

                        // Fonts that don't work (there are hundred that do, so don't include the ones that don't)
                        String notworking = "Aleo Angkor Asap_Condensed B612 B612_Mono Bai_Jamjuree " +
                                "Barlow_Condensed Barlow_Semi_Condensed Barricecito Battambang " +
                                "Bayon Beth_Ellen BioRhyme_Expanded Blinker Bokor Buda Cabin_Condensed " +
                                "Calligraffitti Chakre_Petch Charm Charmonman Chenla Coda_Caption " +
                                "Content Crimson_Pro DM_Sans DM_Serif_Display DM_Serif_Text Dangrek " +
                                "Darker_Grotesque Encode_Sans_Condensed Encode_Sans_Expanded " +
                                "Encode_Sans_Semi_Condensed Encode_Sans_Semi_Expanded Fahkwang " +
                                "Farro Fasthand Fira_Code Freehand Grenze Hanuman IBM_Plex_Sans_Condensed " +
                                "K2D Khmer KoHo Kodchasan Kosugi Kosugi_Maru Koulen Krub Lacquer " +
                                "Libre_Barcode_128 Libre_Barcode_128_Text Libre_Barcode_39 " +
                                "Libre_Barcode_39_Extended Libre_Barcode_39_Extended_Text Libre_Barcode_39_Text " +
                                "Libre_Caslon_Display Libre_Caslon_Text Literata Liu_Jian_Mao_Cao " +
                                "Long_Cang M_PLUS_1p M_PLUS_Rounded_1c Ma_Shan_Zheng Major_Mono_Display " +
                                "Mali Markazi_Text Metal Molle Moul Moulpali Niramit Nokora Notable " +
                                "Noto_Sans_HK Noto_Sans_JP Noto_Sans_KR Noto_Sans_SC Noto_Sans_TC " +
                                "Noto_Serif_JP Noto_Serif_KR Noto_Serif_SC Noto_Serif_TC Open_Sans_Condensed " +
                                "Orbitron Preahvihear Red_Hat_Display Red_Hat_Text Roboto_Condensed " +
                                "Saira_Condensed Saira_Extra_Condensed Saira_Semi_Condensed Saira_Stencil_One " +
                                "Sarabun Sawarabi_Gothic Sawarabi_Mincho Siemreap Single_Day Srisakdi " +
                                "Staatliches Sunflower Suwannaphum Taprom Thasadith Ubuntu_Condensed " +
                                "UnifrakturCook ZCOOL_KuaiLe ZCOOL_QingKe_HuangYou ZCOOL_XiaoWei Zhi_Mhang_Xing ";

                        if (!notworking.contains(line.trim().replace(" ", "_") + " ")) {
                            fontNames.add(line);
                        }
                    }
                }
            }
        }

        // Now add in any fonts stored in the user folder (for non Google Service users)
        ArrayList<String> userFonts = mainActivityInterface.getStorageAccess().listFilesInFolder("Fonts","");
        if (userFonts!=null && userFonts.size()>0) {
            Collections.sort(userFonts, Collections.reverseOrder());
            for (String userFont:userFonts) {
                fontNames.add(0, "Fonts/"+userFont);
            }
        }
        return fontNames;
    }

    public String getLyricFontName() {
        return lyricFontName;
    }
    public String getChordFontName() {
        return chordFontName;
    }
    public String getMonoFontName() {
        return monoFontName;
    }

}