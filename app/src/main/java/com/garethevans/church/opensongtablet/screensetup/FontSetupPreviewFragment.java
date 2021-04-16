package com.garethevans.church.opensongtablet.screensetup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsFontsPreviewBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

public class FontSetupPreviewFragment extends DialogFragment {

    private SettingsFontsPreviewBinding myView;
    private MainActivityInterface mainActivityInterface;

    private String sampleText;
    private ArrayList<String> fontNames, alphaList;
    private final Handler handler = new Handler();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsFontsPreviewBinding.inflate(inflater,container,false);

        mainActivityInterface.updateToolbar(getString(R.string.font_browse));

        sampleText = getString(R.string.lorem);

        // Run the webview setup
        myView.webView.getSettings().setJavaScriptEnabled(true);
        myView.webView.getSettings().setDomStorageEnabled(true);
        myView.webView.setWebChromeClient(new WebChromeClient());
        myView.webView.addJavascriptInterface(new WebAppInterface(), "Android");
        setupWebView("a");

        return myView.getRoot();
    }

    private void setupWebView(String ab) {
        new Thread(() -> {
            if (fontNames==null || fontNames.isEmpty()) {
                fontNames = mainActivityInterface.getMyFonts().getFontsFromGoogle();
                getAlphaList();
                requireActivity().runOnUiThread(this::prepareAlphaList);
            }
            String content = preparePageContent(ab);
            requireActivity().runOnUiThread(() -> prepareWebView(content));
        }).start();
    }

    public void prepareWebView(String content) {
        myView.webView.loadDataWithBaseURL("",content,"text/html","utf-8",null);
    }

    public void prepareAlphaList() {
        myView.sideIndex.removeAllViews();
        for (String ab:alphaList) {
            TextView textView = new TextView(getContext());
            textView.setTextSize(20.0f);
            textView.setText(ab);
            textView.setPadding(32,32,32,32);
            textView.setOnClickListener(v -> setupWebView(ab));
            myView.sideIndex.addView(textView);
        }
    }

    public class WebAppInterface {

        @JavascriptInterface
        public void processReturnValue(String fontname) {
            doSave(fontname);
        }
    }

    private void getAlphaList() {
        // Go through the list and get the unique alphabetical index
        alphaList = new ArrayList<>();
        for (String fontName:fontNames) {
            if (fontName!=null && fontName.length()>0 &&
                    !alphaList.contains(fontName.substring(0,1).toUpperCase(mainActivityInterface.getLocale()))) {
                alphaList.add(fontName.substring(0,1).toUpperCase(mainActivityInterface.getLocale()));
            }
        }
    }

    // Prepare the webpage based on alphabetical choices (button)
    private String preparePageContent(String ab) {

        StringBuilder links = new StringBuilder();
        StringBuilder rows = new StringBuilder();


        for (String fontName:fontNames) {
            if (fontName.toLowerCase(mainActivityInterface.getLocale()).startsWith(ab.toLowerCase(mainActivityInterface.getLocale()))) {
                links.append("<link rel=\"stylesheet\" href=\"https://fonts.googleapis.com/css2?family=")
                        .append(fontName.replaceAll("\\s", "+")).append("\">\n");
                rows.append("<tr onclick=\"getMyFont('").append(fontName.replaceAll("\\s", "+"))
                        .append("')\"><td>").append(fontName).append("</td><td style=\"font-family:'")
                        .append(fontName).append("';\">")
                        .append(sampleText).append("</td></tr>\n");
            }
        }

        return "<html>\n<head>\n" +
                "<script>\nfunction getMyFont(fnt) {\n" +
                "Android.processReturnValue(fnt);\n}\n</script>\n" +
                links + "\n" +
                "<style>\n" +
                "td {padding: 10px; text-align: left; border-bottom: 1px solid #ddd; font-size:18pt;}\n" +
                "tr:nth-child(even) {background-color: #f2f2f2;}\n" +
                "</style>" +
                "</head>\n<body>\n" +
                "<table>" + rows + "</table></body></html>";
    }

    private void doSave(String fontName) {
        fontName = fontName.replace("+"," ");
        mainActivityInterface.getMyFonts().changeFont(getContext(),mainActivityInterface,mainActivityInterface.getWhattodo(),fontName,handler);
        new Thread(() -> requireActivity().runOnUiThread(() -> {
            mainActivityInterface.popTheBackStack(R.id.fontSetupFragment,true);
            mainActivityInterface.navigateToFragment("opensongapp://settings/display/fonts",0);
            dismiss();
        })).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}