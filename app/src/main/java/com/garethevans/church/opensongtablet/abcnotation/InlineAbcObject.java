package com.garethevans.church.opensongtablet.abcnotation;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.caverock.androidsvg.SVG;
import com.garethevans.church.opensongtablet.customviews.InlineAbcWebView;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class InlineAbcObject {

    // This holds the information for the ABC svg used for ImageView and WebView drawings
    private final String TAG = "InlineAbcObject";
    private final MainActivityInterface mainActivityInterface;
    private String abcSvgText, abcInlineText;
    private int abcWidth, abcHeight, abcItem;
    private boolean abcMeasured = false, isPopup = false, isPDF = false, isPresentation = false;
    private InlineAbcWebView inlineAbcWebView;
    private ImageView inlineAbcImageView;
    private final Context c;
    private final int backgroundColor, abcContainingItem;
    private String mainColor, chordColor;
    private InlineAbcWebViewTagObject inlineAbcWebViewTagObject;

    public InlineAbcObject(Context c, String abcInlineText, int abcContainingItem, int backgroundColor) {
        // Each object has a single associated webView (is only used in the pre-measure view)
        // Once it has been rendered, we convert it to an imageView
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;

        // If the abcInlineText is null, we use the songAbc as we must be editing or showing popup
        if (abcInlineText==null) {
            // Use the abcNotation version as it returns a basic start point if songAbc is null/empty
            this.abcInlineText = mainActivityInterface.getAbcNotation().getSongAbc();

        } else {
            this.abcInlineText = abcInlineText;
        }

        this.abcContainingItem = abcContainingItem;
        inlineAbcWebView = new InlineAbcWebView(c);
        inlineAbcWebViewTagObject = new InlineAbcWebViewTagObject();

        this.backgroundColor = backgroundColor;
        mainColor = mainActivityInterface.getMyThemeColors().getHexFromIntNoAlpha(mainActivityInterface.getMyThemeColors().getLyricsTextColor());
        chordColor = mainActivityInterface.getMyThemeColors().getHexFromIntNoAlpha(mainActivityInterface.getMyThemeColors().getLyricsChordsColor());
    }

    // The setters
    public void setAbcSvgText(String abcSvgText) {
        this.abcSvgText = abcSvgText;
    }
    public void setAbcWidth(int abcWidth) {
        this.abcWidth = abcWidth;
    }
    public void setAbcHeight(int abcHeight) {
        this.abcHeight = abcHeight;
    }
    public void setAbcItem(int abcItem) {
        this.abcItem = abcItem;
    }
    public void setAbcMeasured(boolean abcMeasured) {
        this.abcMeasured = abcMeasured;
    }
    public void setAbcInlineText(String abcInlineText) {
        this.abcInlineText = abcInlineText;
    }
    public void setIsPDF(boolean isPDF) {
        this.isPDF = isPDF;
    }
    public void setIsPresentation(boolean isPresentation) {
        this.isPresentation = isPresentation;
    }
    public void setMainColor(String mainColor) {
        this.mainColor = mainColor;
    }
    public void setChordColor(String chordColor) {
        this.chordColor = chordColor;
    }

    // The getters
    public int getAbcWidth() {
        return abcWidth;
    }
    public int getAbcHeight() {
        return abcHeight;
    }
    public int getAbcItem() {
        return abcItem;
    }
    public int getAbcContainingItem() {
        return abcContainingItem;
    }
    public boolean getAbcMeasured() {
        return abcMeasured;
    }
    public boolean getIsInline() {
        return abcInlineText!=null && abcInlineText.trim().startsWith(mainActivityInterface.getAbcNotation().getInlineAbcLineIndicator());
    }
    public String getJSReadyAbcText() {
        String jsReadyAbcText = abcInlineText.trim().replace(mainActivityInterface.getAbcNotation().getInlineAbcLineIndicator(),"");
        jsReadyAbcText = jsReadyAbcText.replace("\\n", "__NEWLINE__");
        return Uri.encode(jsReadyAbcText, "UTF-8").replace("'", "&apos;");
    }

    // This uses an XML set InlineWebView as the view for this object
    public void setInlineAbcWebView(InlineAbcWebView inlineAbcWebView) {
        this.inlineAbcWebView = inlineAbcWebView;
    }

    // This returns a new WebView for the object.  Only one parent allowed
    public InlineAbcWebView getInlineAbcWebView() {
        // We override the default WebView class so we can dispatch touch events
        if (inlineAbcWebView==null) {
            inlineAbcWebView = new InlineAbcWebView(c);
        }
        inlineAbcWebView.setVisibility(View.GONE);
        inlineAbcWebView.post(() -> {
            inlineAbcWebView.setBackgroundColor(backgroundColor);
            inlineAbcWebView.setWebChromeClient(new WebChromeClient() {
                @Override
                public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                    return super.onConsoleMessage(consoleMessage);
                }
            });
            inlineAbcWebView.setWebViewClient(new MyWebViewClient(getIsInline()));
            inlineAbcWebView.loadUrl("file:///android_asset/ABC/abc.html");
        });
        return inlineAbcWebView;
    }

    // This creates a new ImageView for the object (so it can be used in multiple parents)
    public ImageView getInlineAbcImageView(boolean generateNewView) {
        // Here we decide if we want a new ImageView object, or the one linked to this WebView
        // New ImageView objects can be used for duplicating song views on secondary screens
        // Make sure we have a default ImageView ready

        inlineAbcWebView.setVisibility(View.GONE);

        // Now either return the default ImageView or create a new one that can be drawn
        if (generateNewView) {
            ImageView newImageView = new ImageView(c);
            newImageView.setBackgroundColor(backgroundColor);
            drawTheImageView(newImageView);
            setImageViewTags(newImageView);
            return newImageView;

        } else {
            if (inlineAbcImageView==null) {
                inlineAbcImageView = new ImageView(c);
                inlineAbcImageView.setBackgroundColor(backgroundColor);
            }
            setImageViewTags(inlineAbcImageView);
            return inlineAbcImageView;
        }
    }

    private void setImageViewTags(ImageView thisImageView) {
        inlineAbcWebViewTagObject = new InlineAbcWebViewTagObject();
        inlineAbcWebViewTagObject.setContainingViewNumber(abcContainingItem);
        inlineAbcWebViewTagObject.setObjectNumber(abcItem);
        thisImageView.setTag(inlineAbcWebViewTagObject);
    }

    public void drawTheImageView(ImageView thisImageView) {
        if (abcSvgText!=null) {
            if (abcSvgText.contains("<svg")) {
                abcSvgText = abcSvgText.substring(abcSvgText.indexOf("<svg"));
            }
            abcSvgText = abcSvgText.replace("</div>","");

            try {
                //abcSvgText = abcSvgText.replace("#FFFFFF","#000000");
                SVG svg = SVG.getFromString(swapSvgTextColors());
                svg.setDocumentWidth(abcWidth);
                svg.setDocumentHeight(abcHeight);
                Drawable drawable = new PictureDrawable(svg.renderToPicture());

                // Make sure the sizes are as expected
                if (isPDF) {
                    thisImageView.setLayoutParams(new ViewGroup.LayoutParams(abcWidth, abcHeight));
                } else {
                    thisImageView.setLayoutParams(new LinearLayout.LayoutParams(abcWidth, abcHeight));
                }
                thisImageView.setCropToPadding(false);
                thisImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                thisImageView.setImageDrawable(drawable);
                inlineAbcWebView.setVisibility(View.GONE);
                thisImageView.setVisibility(View.VISIBLE);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String swapSvgTextColors() {
        String tempText = abcSvgText;
        if (isPDF) {
            // Replace the main color
            tempText = tempText.replace(mainColor, mainActivityInterface.getMyThemeColors().getHexFromIntNoAlpha(mainActivityInterface.getMyThemeColors().getPdfTextColor()));
            // Replace the chord color
            tempText = tempText.replace(chordColor, mainActivityInterface.getMyThemeColors().getHexFromIntNoAlpha(mainActivityInterface.getMyThemeColors().getPdfChordsColor()));
        } else if (isPresentation) {
            // Replace the main color
            tempText = tempText.replace(mainColor, mainActivityInterface.getMyThemeColors().getHexFromIntNoAlpha(mainActivityInterface.getMyThemeColors().getPresoFontColor()));
            // Replace the chord color
            tempText = tempText.replace(chordColor, mainActivityInterface.getMyThemeColors().getHexFromIntNoAlpha(mainActivityInterface.getMyThemeColors().getPresoChordColor()));
        }
        return tempText;
    }


    // Listener for the webview drawing the abc score
    private class MyWebViewClient extends WebViewClient {

        boolean isInline;
        MyWebViewClient(boolean isInline) {
            this.isInline = isInline;
        }

        @Override
        public void onPageFinished(WebView webView, String url) {
            super.onPageFinished(webView, url);
            updateContent();
        }
    }

    public void setIsPopup(boolean isPopup) {
        this.isPopup = isPopup;
    }
    public boolean getIsPopup() {
        return isPopup;
    }

    public void updateContent() {
        inlineAbcWebView.post(() -> {
            if (getIsInline()) {
                inlineAbcWebView.evaluateJavascript("javascript:setWidth(" + mainActivityInterface.getAbcNotation().getAbcInlineWidth() + ");", null);
                inlineAbcWebView.evaluateJavascript("javascript:setWebView(" + abcItem + ");", null);
                inlineAbcWebView.evaluateJavascript("javascript:setMainColor('" + mainActivityInterface.getMyThemeColors().getNonAlphaHexColorFromInt(mainActivityInterface.getMyThemeColors().getLyricsTextColor()) + "');", null);
                inlineAbcWebView.evaluateJavascript("javascript:setChordColor('" + mainActivityInterface.getMyThemeColors().getNonAlphaHexColorFromInt(mainActivityInterface.getMyThemeColors().getLyricsChordsColor()) + "');", null);
                inlineAbcWebView.evaluateJavascript("javascript:displayOnly();", null);

            } else if (isPopup) {
                // PopUp ABC window
                inlineAbcWebView.evaluateJavascript("javascript:setWidth(" + mainActivityInterface.getAbcNotation().getAbcInlineWidth() + ");", null);
                inlineAbcWebView.evaluateJavascript("javascript:setWebView(0);", null);
                inlineAbcWebView.evaluateJavascript("javascript:setMainColor('"+mainActivityInterface.getMyThemeColors().getNonAlphaHexColorFromInt(mainActivityInterface.getMyThemeColors().getAbcPopupTextColor())+"');", null);
                inlineAbcWebView.evaluateJavascript("javascript:setChordColor('"+mainActivityInterface.getMyThemeColors().getNonAlphaHexColorFromInt(mainActivityInterface.getMyThemeColors().getAbcPopupTextColor())+"');", null);
                inlineAbcWebView.evaluateJavascript("javascript:displayOnly();", null);

            } else {
                // ABC editor fragment
                inlineAbcWebView.evaluateJavascript("javascript:setWidth(" + mainActivityInterface.getAbcNotation().getAbcInlineWidth() + ");", null);
                inlineAbcWebView.evaluateJavascript("javascript:setWebView(0);", null);
                inlineAbcWebView.evaluateJavascript("javascript:setMainColor('#000000');", null);
                inlineAbcWebView.evaluateJavascript("javascript:setChordColor('#000000');", null);
                inlineAbcWebView.evaluateJavascript("javascript:displayAndEdit();", null);
            }

            // This sends the final information to start the rendering process
            // The default settings
            inlineAbcWebView.evaluateJavascript("javascript:setIsPopup("+isPopup+");", null);
            inlineAbcWebView.evaluateJavascript("javascript:setResponsive('resize');", null);
            inlineAbcWebView.evaluateJavascript("javascript:updateABC('" + getJSReadyAbcText() + "');", null);
            inlineAbcWebView.evaluateJavascript("javascript:setHideTab(" + !mainActivityInterface.getAbcNotation().getAbcIncludeTab() + ");", null);
            inlineAbcWebView.evaluateJavascript("javascript:setTranspose(" + mainActivityInterface.getAbcNotation().getSongAbcTranspose() + ");", null);
            inlineAbcWebView.evaluateJavascript("javascript:setInstrument('" + mainActivityInterface.getAbcNotation().getAbcIntrumentTabForABCJS() + "');", null);
            String[] strings = mainActivityInterface.getAbcNotation().getAbcInstrumentTuningABCJS();
            inlineAbcWebView.evaluateJavascript("javascript:setTuning('" + strings[6] + "','" + strings[5] + "','" + strings[4] + "','" + strings[3] + "','" + strings[2] + "','" + strings[1] + "');", null);
            inlineAbcWebView.evaluateJavascript("javascript:setLabel('" + mainActivityInterface.getAbcNotation().getAbcInstrumentLabelABCJS() + "');", null);

            inlineAbcWebView.evaluateJavascript("javascript:initEditor()", null);
        });
    }

}
