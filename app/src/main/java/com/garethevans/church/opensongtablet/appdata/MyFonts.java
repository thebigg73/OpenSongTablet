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

public class MyFonts {
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "MyFonts";
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

    public MyFonts(Context c) {
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
                File fontFile = mainActivityInterface.getStorageAccess().getAppSpecificFile("Files","",actualName);
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
                Log.d(TAG,"onTypefaceRequestFailed("+reason+")");
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
        Log.d(TAG,"doSetDesiredFont("+which+", " + typeface+", "+ fontName+")");
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
                        String notworking = "Aboreto Abyssinica_SIL ADLaM_Display Afacad Agbalumo " +
                                "Agdasima Akatab Akaya_Kanadaka Akshar Albert_Sans Alexandria Alkalami " +
                                "Alkatra Allison Alumni_Sans Alumni_Sans_Collegiate_One Alumni_Sans_Inline_One " +
                                "Alumni_Sans_Pinstripe Amiri_Quran Andada_Pro Anek_Bangla Anek_Devanagari " +
                                "Anek_Gujarati Anek_Gurmukhi Anek_Kannada Anek_Latin Anek_Malayalam Anek_Odia " +
                                "Anek_Tamil Anek_Telugu Antonio Anuphan Anybody Aoboshi_One Are_You_Serious " +
                                "Aref_Ruqaa_Ink Arima Atkinson_Hyperlegible Azaret_Mono AR_One_Sans Aleo Angkor " +
                                "Asap_Condensed B612 B612_Mono Bai_Jamjuree BIZ_UDGothic BIZ_UDMincho " +
                                "BIZ_UDPGothic BIZ_UDPMincho Babylonica Bacasime_Antique Bagel_Fat_One " +
                                "Bakbak_One Ballet Baloo_2 Baloo_Bhaijaan_2 Baloo_Chettan_2 Baloo_Da_2 " +
                                "Baloo_Thambi_2 Be_Vietnam_Pro Beau_Rivage Benne Besley Bhu_Tuka_Expanded_One " +
                                "Barlow_Condensed Barlow_Semi_Condensed Barricecito Battambang " +
                                "Bayon Beth_Ellen BioRhyme_Expanded Blinker Bokor Buda Cabin_Condensed " +
                                "Birthstone Birthstone_Bounce Blaka Blaka_Hollow Blaka_Ink Bona_Nova " +
                                "Bonheur_Royale Borel Braah_One Brincolage_Grotesque Bruno_Ace Bruno_Ace_SC " +
                                "Cairo_Play Caprasimo Caramel Carattere Carlito Charis_SIL Cherish" +
                                "Cherry_Bomb_One Chivo_Mono Chokokutai Climate_Crisis Comforter Comforter_Brush " +
                                "Comme Corinthia Calligraffitti Chakre_Petch Charm Charmonman Chenla Coda_Caption " +
                                "Content Crimson_Pro DM_Sans DM_Serif_Display DM_Serif_Text Dangrek " +
                                "Dai_Banna_SIL Darumadrop_One Delicious_Handrawn Diphylleia Dongle Dyna_Puff " +
                                "Darker_Grotesque Encode_Sans_Condensed Encode_Sans_Expanded " +
                                "Edu_NSW_ACT_Foundation Edu_QLD_Beginner Edu_SA_Beginner Edu_TAS_Beginner " +
                                "Edu_VIC_WA_NT_Beginner Encode_Sans_SC Ephesis Estonia Explora " +
                                "Encode_Sans_Semi_Condensed Encode_Sans_Semi_Expanded Fahkwang " +
                                "Familjen_Grotesk Festive Figtree Finlandica Fleur_De_Leah Flow_Block " +
                                "Flow_Circular Flow_Rounded Foldit Fragment_Mono Fredoka Fuggles Fuzzy_Bubbles " +
                                "Farro Fasthand Fira_Code Freehand Gabarito Gajraj_One Gantari Gasoek_One " +
                                "Gemunu_Libre Genos Gentium_Book_Plus Gentium_Plus Geologica Georama " +
                                "Gideon_Roman Gloock Glory Gluten Golos_Text Gowun_Batang Gowun_Dodum " +
                                "Grandiflora_One Grape_Nuts Grenze Hanuman Grechen_Fuemen Grey_Qo Gulzar " +
                                "Gwendolyn Hahmlet Handjet Hanken_Grotesk Hedvig_Letters_Sans Headvig_Letters_Serif " +
                                "Hina_Mincho Hubballi Hurricane IBM_Plex_Sans_Arabic IBM_Plex_Sans_Devanagari " +
                                "IBM_Plex_Sans_Condensed IBM_Plex_Sans_Hebrew IBM_Plex_Sans_JP IBM_Plex_Sans_KR " +
                                "IBM_Plex_Sans_Thai IBM_Plex_Sans_Thai_Looped Imbue Imperial_Script " +
                                "Inclusive_Sans Ingrid_Darling Inspiration Instrument_Sans Instrument_Serif " +
                                "Inter_Tight Island_Moments Joan Kablammo Kaisei_Decol Kaisei_HarunoUmi " +
                                "Kaisei_Opti Kaisei_Tokumin Kalnia Kantumruy_Pro Karantina Kay_Pho_Du " +
                                "Kdam_Thmor_Pro Kings Kiwi_Maru Klee_One Koh_Santepheap Kolker_Brush " +
                                "Konkhmer_Sleokchher K2D Khmer KoHo Kodchasan Kosugi Kosugi_Maru Koulen Krub " +
                                "Lacquer Labrada Lavishly_Yours League_Gothic League_Spartan Libre_Barcode_EAN13_Text" +
                                "Libre_Barcode_128 Libre_Barcode_128_Text Libre_Barcode_39 Libre_Bodoni" +
                                "Libre_Barcode_39_Extended Libre_Barcode_39_Extended_Text Libre_Barcode_39_Text " +
                                "Libre_Caslon_Display Libre_Caslon_Text Literata Liu_Jian_Mao_Cao " +
                                "Licorice Linefont Lisu_Bosa Love_Light Lugrasimo Lumanosimo Lunasima" +
                                "Luxurious_Roman Luxurious_Script Long_Cang M_PLUS_1 M_PLUS_1p M_PLUS_1_Code " +
                                "M_PLUS_2 M_PLUS_Code_Latin M_PLUS_Rounded_1c Ma_Shan_Zheng Major_Mono_Display " +
                                "Marhey Martian_Mono Material_Icons Material_Icons_Outlined Material_Icons_Round " +
                                "Material_Icons_Sharp Material_Icons_Two_Tone Material_Symbols_Outlined " +
                                "Material_Symbols_Rounded Material_Symbols_Sharp Mea_Culpa Meow_Script " +
                                "Mali Markazi_Text Metal Molle Moul Moulpali Mingzat Mochiy_Pop_One " +
                                "Mochiy_Pop_P_One Mohave Moirai_One Monomaniac_One Montagu_Slab MonteCarlo " +
                                "Moo_Lah_Lah Mooli Moon_Dance Ms_Madi Murecho My_Soul Mynerve Nabla " +
                                "Narnoor Neonderthaw Newsreader Noto_Color_Emoji Noto_Emoji Noto_Kufi_Arabic " +
                                "Niramit Nokora Notable Noto_Music Noto_Naskh_Arabic Noto_Nastaliq_Urdu " +
                                "Noto_Sans_HK Noto_Sans_JP Noto_Sans_KR Noto_Sans_SC Noto_Sans_TC " +
                                "Noto_Serif_JP Noto_Serif_KR Noto_Serif_SC Noto_Serif_TC Noto_Rashi_Hebrew " +
                                "Open_Sans_Condensed Noto_Sans_Adlam Noto_Sans_Adlam_Unjoined Noto_Sans_Anatolian_Hieroglyphs " +
                                "Noto_Sans_Arabic Noto_Sans_Armenian Noto_Sans_Avestan Noto_Sans_Balinese " +
                                "Noto_Sans_Bamum Noto_Sans_Bassa_Vah Noto_Sans_Satak Noto_Traditional_Nushu " +
                                "Nuosu_SIL Ole Onest Oooh_Baby Orbit Orelega_One Outfit Padyakke_Expanded_One " +
                                "Palette_Mosaic Passions_Conflict Pathway_Extreme Petemoss Phudu Pixelify_Sans " +
                                "Orbitron Preahvihear Playfair Playpen_Sans Plus_Jakarta_Sans Poltawski_Nowy " +
                                "Praise Puppies_Play Qahiri Qwitcher_Grypen REM Radio_Canada Rampart_One " +
                                "Readex_Pro Red_Hat_Mono Red_Hat_Display Red_Hat_Text Redacted Redacted_Script " +
                                "Reen_Kufi_Fun Reen_Kufi_Ink Rethink_Sans Road_Rage Roboto_Condensed " +
                                "Roboto_Flex Roboto_Serif Rock_3D Ruwudu STIX_Two_Text Sassy_Frass " +
                                "Scheherazade_New Schibsted_Grotesk Send_Flowers Shalimar Shantell_Sans " +
                                "Saira_Condensed Saira_Extra_Condensed Saira_Semi_Condensed Saira_Stencil_One " +
                                "Sarabun Sawarabi_Gothic Sawarabi_Mincho Siemreap Single_Day Srisakdi " +
                                "Shippori_Antique Shippori_Antique_B1 Shizuru Sigmar Silkscreen Slackside_One " +
                                "Smooch Smooch_Sans Sofia_Sans Sofia_Sans_Condensed Sofia_Sans_Extra_Condensed " +
                                "Sofia_Sans_Semi_Condensed Solitreo Sometype_Mono Sono Source_Sans_3 " +
                                "Source_Serif_4 Splash Spline_Sans Spline_Sans_Mono Square_Peg Stick_No_Bills " +
                                "Staatliches Sunflower Suwannaphum Tai_Heritage_Pro Tapestry Taprom Thasadith " +
                                "Tektur Texturina The_Nautigal Tilt_Neon Tilt_Prism Tilt_Warp Tiro_Bangla " +
                                "Tiro_Devanagari_Hindi Tiro_Devanagari_Marathi Tiro_Devanagari_Sanskrit " +
                                "Tiro_Gurmukhi Tiro_Kannada Tiro_Tamil Tiro_Telugu Tourney Truculenta " +
                                "Tsukimi_Rounded Twinkle_Star Uchen Unbounded Updock Urbanist Ubuntu_Condensed " +
                                "UnifrakturCook VT323 Vazirmatn Victor_Mono Vina_Sans Vujahday_Script " +
                                "Water_Brush Waterfall Wavefont Whisper WindSong Wix_Madefor_Display " +
                                "Wix_Madefor_Text Yaldevi Yomogi Young_Serif Ysabeau Ysabeau_Infant " +
                                "Ysabeau_Office Ysabeau_SC Yuji_Boku Yuji_Hentaigana_Akari Yuji_Hentaigana_Akebono " +
                                "Yuji_Mai Yuji_Syuku Zen_Antique Zen_Dots Zen_Kaku_Gothic_Antique " +
                                "Zen_Kaku_Gothic_New Zen_Kurenaido Zen_Loop Zen_Maru_Gothic Zen_Old_Mincho " +
                                "Zen_Tokyo_Zoo ZCOOL_KuaiLe ZCOOL_QingKe_HuangYou ZCOOL_XiaoWei Zhi_Mhang_Xing ";

                        if (!notworking.contains(line.trim().replace(" ", "_") + " ") &&
                            !line.trim().startsWith("Noto Sans") && !line.trim().startsWith("Noto Serif") &&
                            !line.trim().startsWith("Rubik ")) {
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