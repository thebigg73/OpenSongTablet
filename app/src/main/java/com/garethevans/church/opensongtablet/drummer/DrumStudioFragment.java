package com.garethevans.church.opensongtablet.drummer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.gson.Gson;

public class DrumStudioFragment extends Fragment {

    private static final String TAG = "DrumStudioFragment";
    private WebView webView;
    private static final String STUDIO_URL = "https://thebigg73.github.io/OpenSongAppDrummer/index.html"; // Replace with your hosted URL
    private MainActivityInterface mainActivityInterface;
    private String deeplink_drummer_settings="";
    private ValueCallback<Uri[]> filePathCallback;
    private ActivityResultLauncher<Intent> fileChooserLauncher;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        prepareStrings();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register the activity result launcher for picking JSON files
        fileChooserLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (filePathCallback == null) return;

                    Uri[] results = null;
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        String dataString = result.getData().getDataString();
                        if (dataString != null) {
                            results = new Uri[]{Uri.parse(dataString)};
                        }
                    }

                    filePathCallback.onReceiveValue(results);
                    filePathCallback = null;
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_drum_studio, container, false); // Make sure you have this layout with a WebView

        webView = view.findViewById(R.id.webView);

        prepareStrings();

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webView.clearCache(true);
        webView.clearHistory();

        WebView.setWebContentsDebuggingEnabled(true);

        // 1. Register your existing storage interface so the web studio can save directly to OpenSong/Drummer/
        webView.addJavascriptInterface(new AppJavaScriptInterface(), "mainActivityInterface");

        // 💡 ADD THIS WebChromeClient to intercept <input type="file"> and open the file explorer
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (DrumStudioFragment.this.filePathCallback != null) {
                    DrumStudioFragment.this.filePathCallback.onReceiveValue(null);
                }
                DrumStudioFragment.this.filePathCallback = filePathCallback;

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/json"); // Filter for JSON files

                try {
                    fileChooserLauncher.launch(Intent.createChooser(intent, "Select Drum Pattern JSON"));
                } catch (Exception e) {
                    DrumStudioFragment.this.filePathCallback = null;
                    return false;
                }
                return true;
            }
        });

        // 2. Set up WebViewClient to inject the current pattern when the page loads
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // 1. Inject the pattern JSON
                injectCurrentPattern();

                // 2. Inject the active tempo (e.g. 120)
                int currentTempo = mainActivityInterface.getDrumViewModel().getThisBpm();
                webView.evaluateJavascript("if (window.loadAppTempo) { window.loadAppTempo(" + currentTempo + "); }", null);


                // 3. Inject the current drummer filename
                // Strip out the .json extension (preserving original case)
                String fullFileName = mainActivityInterface.getDrumViewModel().getTempDrumFileName();
                if (fullFileName==null || fullFileName.isEmpty()) {
                    fullFileName = mainActivityInterface.getSong().getTitle();
                }
                String nameWithoutExt = fullFileName.endsWith(".json") || fullFileName.endsWith(".JSON")
                        ? fullFileName.substring(0, fullFileName.length() - 5)
                        : fullFileName;

                // Strip out trailing time signatures or version numbers (e.g., _4_4 or _v2_4_4) while keeping exact case
                String cleanName = nameWithoutExt.replaceAll("(_v\\d+)?(_\\d+)+$", "");
                webView.evaluateJavascript("if (window.loadAppFilename) { window.loadAppFilename('" + cleanName + "'); }", null);

                // 3. Inject the active kit ('acoustic' or 'percussion')
                String currentKit = mainActivityInterface.getDrumViewModel().getTempDrumKit(); // returns "acoustic" or "percussion"
                if (currentKit==null) {
                    currentKit = "Acoustic";
                }
                currentKit = currentKit.toLowerCase();
                webView.evaluateJavascript("if (window.loadAppKit) { window.loadAppKit('" + currentKit + "'); }", null);
            }
        });

        // 3. Load the studio web page
        webView.loadUrl(STUDIO_URL);

        return view;
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            deeplink_drummer_settings = getString(R.string.deeplink_drummer_settings);
        }
    }
    /**
     * Serializes the user's active drum pattern object from your app to JSON
     * and injects it directly into the web studio via the global loadAppPattern function.
     */
    private void injectCurrentPattern() {
        try {
            // 1. Get your LiveData object from your ViewModel or repository
            androidx.lifecycle.MutableLiveData<com.garethevans.church.opensongtablet.drummer.DrumPatternJson> liveDataPattern =
                    mainActivityInterface.getDrumViewModel().getCurrentPattern(); // Adjust to your actual getter

            if (liveDataPattern == null || liveDataPattern.getValue() == null) {
                Log.d(TAG, "No active pattern value found in LiveData yet.");
                return;
            }

            // 2. Extract the actual data payload using .getValue()
            com.garethevans.church.opensongtablet.drummer.DrumPatternJson actualPatternData = liveDataPattern.getValue();

            // 3. Serialize the actual clean data object to JSON
            Gson gson = new Gson();
            String jsonString = gson.toJson(actualPatternData);

            Log.d(TAG, "Successfully serialized clean JSON length: " + jsonString.length());

            // 4. Escape and inject into the WebView
            String escapedJson = jsonString.replace("\\", "\\\\").replace("'", "\\'");
            String jsCommand = "if (window.loadAppPattern) { window.loadAppPattern('" + escapedJson + "'); }";

            webView.evaluateJavascript(jsCommand, null);

        } catch (Exception e) {
            Log.e(TAG, "Failed to inject drum pattern into web studio", e);
        }
    }

    /**
     * Your existing JavaScript Interface that exposes storage access methods to the WebView.
     */
    public class AppJavaScriptInterface {

        @JavascriptInterface
        public void savePatternToFile(String fileName, String textContent, String kitType) {
            try {
                Log.d(TAG, "Intercepted download. Filename: " + fileName + ", Kit: " + kitType);

                // 1. Save the pattern file using your storage utility
                mainActivityInterface.getStorageAccess().writeFileFromString(
                        "Drummer",       // folder
                        "",        // subfolder
                        fileName,         // filename passed from web studio
                        textContent,      // JSON text content
                        false             // append existing
                );

                // 2. Parse the textContent directly into a DrumPatternJson object
                Gson gson = new Gson();
                DrumPatternJson updatedPattern = gson.fromJson(textContent, DrumPatternJson.class);

                // 3. Directly update ViewModel and Drummer engine to avoid disk-read lag
                if (updatedPattern != null) {
                    DrumViewModel viewModel = mainActivityInterface.getDrumViewModel();
                    viewModel.setDrumPatternJson(updatedPattern);
                    viewModel.getCurrentPattern().postValue(updatedPattern);

                    if (viewModel.getDrummer() != null) {
                        viewModel.getDrummer().setPattern(updatedPattern);
                        viewModel.getDrummer().updateActiveMap();
                    }
                }

                // 4. Update the app's preferred kit
                if ("percussion".equalsIgnoreCase(kitType)) {
                    mainActivityInterface.getDrumViewModel().setTempDrumKit("Percussion");
                    mainActivityInterface.getDrummer().setDrummerStyle("Percussion");
                } else {
                    mainActivityInterface.getDrumViewModel().setTempDrumKit("Percussion");
                    mainActivityInterface.getDrummer().setDrummerStyle("Percussion");
                }

                // 5. Update song references & save song (WITHOUT reloading the drummer file)
                if (updatedPattern!=null && updatedPattern.getName()!=null) {
                    mainActivityInterface.getDrumViewModel().setTempDrumFileName(updatedPattern.getName());
                }

                Log.d(TAG, "Successfully saved and applied pattern for " + fileName);

                // Because we don't save to the song yet, we need the sequencer to load in this temp file
                // We need to set a boolean that we have received values from the studio
                mainActivityInterface.getDrumViewModel().setTempDrumsReceived(true);

                // 6. Navigate back to settings
                mainActivityInterface.getMainHandler().post(() -> mainActivityInterface.navigateToFragment(deeplink_drummer_settings, -1));

            } catch (Exception e) {
                Log.e(TAG, "Error writing pattern string to file", e);
            }
        }
    }

    @Override
    public void onDestroyView() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroyView();
    }
}