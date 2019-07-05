package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;

import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class PopUpCustomSlideFragment extends DialogFragment {

    static PopUpCustomSlideFragment newInstance() {
        PopUpCustomSlideFragment frag;
        frag = new PopUpCustomSlideFragment();
        return frag;
    }

    public interface MyInterface {
        void addSlideToSet();
        void openFragment();
    }

    private MyInterface mListener;

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    private AsyncTask<Object,Void,String> update_fields;

    private Bible bibleC;
    Preferences preferences;

    // Declare views
    View V;
    private RadioGroup customRadioGroup;
    private RadioButton noteRadioButton, slideRadioButton, imageRadioButton, scriptureRadioButton;
    private TextView timeTextView;
    private TextView warningTextView;
    private EditText slideTitleEditText, slideContentEditText, timeEditText, bibleSearch, bibleVersion;
    private Button loadReusableButton, addPageButton, searchBibleGateway_Button, localBibleFile, grabVerse_Button;
    private CheckBox saveReusableCheckBox, loopCheckBox;
    private TableLayout slideImageTable;
    private LinearLayout reusable_LinearLayout, searchBible_LinearLayout;
    private RelativeLayout slideDetails_RelativeLayout;
    private WebView bibleGateway_WebView;
    private ProgressBar searchBible_progressBar;

    // Declare variables used
    private static String whattype = "note";
    StorageAccess storageAccess;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        // Initialise the helper classes
        storageAccess = new StorageAccess();
        bibleC = new Bible();
        preferences = new Preferences();

        V = inflater.inflate(R.layout.popup_customslidecreator, container, false);

        // Initialise the views
        initialiseTheViews();

        new Thread(new Runnable() {
            @Override
            public void run() {

                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        grabVerse_Button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                searchBible_progressBar.setVisibility(View.VISIBLE);
                                bibleGateway_WebView.setVisibility(View.GONE);
                                grabVerse_Button.setVisibility(View.GONE);
                                grabBibleText(bibleGateway_WebView.getUrl());
                            }
                        });

                        localBibleFile.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                FullscreenActivity.whattodo = "localbible";
                                if (mListener != null) {
                                    try {
                                        mListener.openFragment();
                                        dismiss();
                                    } catch (Exception e) {
                                        Log.d("d", "Error opening local bible");
                                    }
                                }
                            }
                        });
                        searchBibleGateway_Button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                searchBible_progressBar.setVisibility(View.VISIBLE);
                                bibleSearch.clearFocus();
                                bibleVersion.clearFocus();
                                searchBibleGateway_Button.requestFocus(); // Try to hide keyboard
                                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                if (imm!=null) {
                                    imm.hideSoftInputFromWindow(bibleSearch.getWindowToken(), 0);
                                    imm.hideSoftInputFromWindow(bibleVersion.getWindowToken(), 0);
                                }
                                setUpWebView();
                                searchBible();
                            }
                        });

                        if (FullscreenActivity.whattodo.contains("customreusable_")) {
                            updateFields();
                        } else {
                            // By default we want to make a brief note/placeholder
                            noteRadioButton.setChecked(true);
                            FullscreenActivity.whattodo = "customnote";
                            slideRadioButton.setChecked(false);
                            imageRadioButton.setChecked(false);
                            scriptureRadioButton.setChecked(false);
                            saveReusableCheckBox.setChecked(false);
                            switchViewToNote();
                        }

                        // Set button listeners
                        addPageButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (whattype.equals("slide")) {
                                    String newText = slideContentEditText.getText().toString().trim() + "\n---\n";
                                    newText = newText.trim() + "\n";
                                    slideContentEditText.setText(newText);
                                    slideContentEditText.setSelection(slideContentEditText.getText().length());
                                } else if (whattype.equals("image")) {
                                    // Call file browser
                                    Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                    i.setType("image/*");
                                    try {
                                        startActivityForResult(i, StaticVariables.REQUEST_IMAGE_CODE);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });

                        loadReusableButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // This reopens the choose backgrounds popupFragment
                                dismiss();
                                DialogFragment newFragment = PopUpFileChooseFragment.newInstance();
                                newFragment.show(getActivity().getSupportFragmentManager(), "dialog");
                            }
                        });
                        customRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(RadioGroup group, int checkedId) {
                                if (noteRadioButton.isChecked()) {
                                    switchViewToNote();
                                } else if (slideRadioButton.isChecked()) {
                                    switchViewToSlide();
                                } else if (scriptureRadioButton.isChecked()) {
                                    switchViewToScripture();
                                } else {
                                    switchViewToImage();
                                }
                            }
                        });
                    }
                });
            }
        }).start();

        PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog(), preferences);

        return V;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initialiseTheViews() {
        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.add_custom_slide));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe, getActivity());
                closeMe.setEnabled(false);
                dismiss();
            }
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(saveMe, getActivity());
                saveMe.setEnabled(false);
                doSave();
            }
        });

        // Initialise the basic views
        customRadioGroup = V.findViewById(R.id.customRadioGroup);
        noteRadioButton = V.findViewById(R.id.noteRadioButton);
        slideRadioButton = V.findViewById(R.id.slideRadioButton);
        imageRadioButton = V.findViewById(R.id.imageRadioButton);
        scriptureRadioButton = V.findViewById(R.id.scriptureRadioButton);
        slideTitleEditText = V.findViewById(R.id.slideTitleEditText);
        slideContentEditText = V.findViewById(R.id.slideContentEditText);
        addPageButton = V.findViewById(R.id.addPageButton);
        loadReusableButton = V.findViewById(R.id.loadReusableButton);
        saveReusableCheckBox = V.findViewById(R.id.saveReusableCheckBox);
        slideImageTable = V.findViewById(R.id.slideImageTable);
        loopCheckBox = V.findViewById(R.id.loopCheckBox);
        timeTextView = V.findViewById(R.id.timeTextView);
        timeEditText = V.findViewById(R.id.timeEditText);
        warningTextView = V.findViewById(R.id.warningTextView);
        reusable_LinearLayout = V.findViewById(R.id.reusable_LinearLayout);
        searchBible_LinearLayout = V.findViewById(R.id.searchBible_LinearLayout);
        slideDetails_RelativeLayout = V.findViewById(R.id.slideDetails_RelativeLayout);
        bibleSearch = V.findViewById(R.id.bibleSearch);
        bibleVersion = V.findViewById(R.id.bibleVersion);
        localBibleFile = V.findViewById(R.id.localBibleFile);
        searchBibleGateway_Button = V.findViewById(R.id.searchBibleGateway_Button);
        bibleGateway_WebView = V.findViewById(R.id.bibleGateway_WebView);
        grabVerse_Button = V.findViewById(R.id.grabVerse_Button);
        grabVerse_Button.setVisibility(View.GONE);
        searchBible_progressBar = V.findViewById(R.id.searchBible_progressBar);
        searchBible_progressBar.setVisibility(View.GONE);
        bibleGateway_WebView.setVisibility(View.GONE);

    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setUpWebView() {
        bibleGateway_WebView.getSettings().getJavaScriptEnabled();
        bibleGateway_WebView.getSettings().setJavaScriptEnabled(true);
        bibleGateway_WebView.getSettings().setDomStorageEnabled(true);
        bibleGateway_WebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        bibleGateway_WebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });
    }

    private void addScripture() {
        if (FullscreenActivity.scripture_title != null &&
                !FullscreenActivity.scripture_title.equals("") &&
                FullscreenActivity.scripture_verse != null &&
                !FullscreenActivity.scripture_verse.equals("")) {
            searchBible_progressBar.setVisibility(View.GONE);
            grabVerse_Button.setVisibility(View.GONE);
            slideTitleEditText.setText(FullscreenActivity.scripture_title);
            slideContentEditText.setText(FullscreenActivity.scripture_verse);
            reusable_LinearLayout.setVisibility(View.GONE);
            searchBible_LinearLayout.setVisibility(View.GONE);
            slideDetails_RelativeLayout.setVisibility(View.VISIBLE);
        }
    }

    private void switchViewToNote() {
        whattype = "note";
        FullscreenActivity.whattodo ="customnote";
        grabVerse_Button.setVisibility(View.GONE);
        reusable_LinearLayout.setVisibility(View.VISIBLE);
        searchBible_LinearLayout.setVisibility(View.GONE);
        slideDetails_RelativeLayout.setVisibility(View.VISIBLE);
        addPageButton.setVisibility(View.GONE);
        slideContentEditText.setVisibility(View.VISIBLE);
        slideImageTable.setVisibility(View.GONE);
        loopCheckBox.setVisibility(View.GONE);
        timeTextView.setVisibility(View.GONE);
        timeEditText.setVisibility(View.GONE);
        warningTextView.setVisibility(View.GONE);
    }

    private void switchViewToScripture() {
        whattype = "scripture";
        grabVerse_Button.setVisibility(View.GONE);
        searchBible_progressBar.setVisibility(View.GONE);
        FullscreenActivity.whattodo ="customscripture";
        reusable_LinearLayout.setVisibility(View.GONE);
        searchBible_LinearLayout.setVisibility(View.VISIBLE);
        slideDetails_RelativeLayout.setVisibility(View.GONE);
        addPageButton.setVisibility(View.GONE);
        slideContentEditText.setVisibility(View.VISIBLE);
        slideImageTable.setVisibility(View.GONE);
        loopCheckBox.setVisibility(View.GONE);
        timeTextView.setVisibility(View.GONE);
        timeEditText.setVisibility(View.GONE);
        warningTextView.setVisibility(View.GONE);
        bibleSearch.setVisibility(View.VISIBLE);
        bibleVersion.setVisibility(View.VISIBLE);
    }

    private void switchViewToSlide() {
        whattype = "slide";
        FullscreenActivity.whattodo ="customslide";
        grabVerse_Button.setVisibility(View.GONE);
        reusable_LinearLayout.setVisibility(View.VISIBLE);
        searchBible_LinearLayout.setVisibility(View.GONE);
        slideDetails_RelativeLayout.setVisibility(View.VISIBLE);
        addPageButton.setVisibility(View.VISIBLE);
        slideContentEditText.setVisibility(View.VISIBLE);
        slideImageTable.setVisibility(View.GONE);
        loopCheckBox.setVisibility(View.VISIBLE);
        timeTextView.setVisibility(View.VISIBLE);
        timeEditText.setVisibility(View.VISIBLE);
        warningTextView.setVisibility(View.GONE);
    }

    private void switchViewToImage() {
        whattype = "image";
        FullscreenActivity.whattodo ="customimage";
        grabVerse_Button.setVisibility(View.GONE);
        reusable_LinearLayout.setVisibility(View.VISIBLE);
        searchBible_LinearLayout.setVisibility(View.GONE);
        slideDetails_RelativeLayout.setVisibility(View.VISIBLE);
        addPageButton.setVisibility(View.VISIBLE);
        slideContentEditText.setVisibility(View.GONE);
        slideImageTable.setVisibility(View.VISIBLE);
        loopCheckBox.setVisibility(View.VISIBLE);
        timeTextView.setVisibility(View.VISIBLE);
        timeEditText.setVisibility(View.VISIBLE);
        warningTextView.setVisibility(View.VISIBLE);
    }

    public void doSave() {
        FullscreenActivity.noteorslide = whattype;
        StringBuilder text = new StringBuilder(slideContentEditText.getText().toString().trim());
        FullscreenActivity.customreusable = saveReusableCheckBox.isChecked();
        StringBuilder imagecontents;

        if (whattype.equals("image")) {
            imagecontents = new StringBuilder();
            // Go through images in list and extract the full location and the filename
            for (int r = 0; r < slideImageTable.getChildCount(); r++) {
                // Look for image file location
                if (slideImageTable.getChildAt(r) instanceof TableRow) {
                    TextView tv = (TextView) ((TableRow) slideImageTable.getChildAt(r)).getChildAt(0);
                    String tv_text = tv.getText().toString();
                    imagecontents.append(tv_text).append("\n");
                }
            }

            while (imagecontents.toString().contains("\n\n")) {
                imagecontents = new StringBuilder(imagecontents.toString().replace("\n\n", "\n"));
            }
            imagecontents = new StringBuilder(imagecontents.toString().trim());
            String[] individual_images = imagecontents.toString().split("\n");

            // Prepare the lyrics
            text = new StringBuilder();
            for (int t = 0; t < individual_images.length; t++) {
                text.append("[").append(Objects.requireNonNull(getActivity()).getResources().getString(R.string.image)).append("_").append(t + 1).append("]\n").append(individual_images[t]).append("\n\n");
            }
            text = new StringBuilder(text.toString().trim());

        } else {
            imagecontents = new StringBuilder();
        }
        FullscreenActivity.customslide_title = slideTitleEditText.getText().toString();
        FullscreenActivity.customslide_content = text.toString();
        FullscreenActivity.customimage_list = imagecontents.toString();
        FullscreenActivity.customimage_loop = "" + loopCheckBox.isChecked() + "";
        FullscreenActivity.customimage_time = timeEditText.getText().toString();
        // Check the slide has a title.  If not, use _
        if (FullscreenActivity.customslide_title == null || FullscreenActivity.customslide_title.equals("") || FullscreenActivity.customslide_title.isEmpty()) {
            FullscreenActivity.customslide_title = "_";
        }
        // Fix the title to remove / and . to make them safe for file names
        FullscreenActivity.customslide_title = FullscreenActivity.customslide_title.replace("/", "_");
        FullscreenActivity.customslide_title = FullscreenActivity.customslide_title.replace(".", "_");

        mListener.addSlideToSet();
        dismiss();
    }

    private void updateFields() {
        update_fields = new UpdateFields();
        try {
            update_fields.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            Log.d("d", "Error updating fields");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d("CustomSlideFragment","onActivityResult");
        if (intent!=null) {
            Uri uri = intent.getData();
            Log.d("onActivityResult","uri="+uri);

            if (requestCode==StaticVariables.REQUEST_IMAGE_CODE) {
                // Create a new row in the table
                // Each row has the file name, an image thumbnail and a delete button
                addRow(uri);
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void searchBible() {
        // Prepare the search strings
        String whattosearch = bibleSearch.getText().toString();
        String whatversion = bibleVersion.getText().toString();
        try {
            whattosearch = URLEncoder.encode(bibleSearch.getText().toString(), "UTF-8");
            whatversion = URLEncoder.encode(bibleVersion.getText().toString(), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        bibleGateway_WebView.getSettings().setJavaScriptEnabled(true);
        String webaddress = "https://www.biblegateway.com/quicksearch/?quicksearch="+whattosearch+"&qs_version="+whatversion;
        bibleGateway_WebView.loadUrl(webaddress);
        bibleGateway_WebView.setVisibility(View.VISIBLE);
        bibleGateway_WebView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                searchBible_progressBar.setVisibility(View.GONE);
                if (url.contains("passage")) {
                    grabVerse_Button.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void addRow(Uri uri) {
        if (uri != null && uri.getPath() != null) {
            try {
                // Prepare the tag - use the file name and base 64 encode it to make it safe
                byte[] data = uri.getPath().getBytes(StandardCharsets.UTF_8);
                String tag = Base64.encodeToString(data, Base64.DEFAULT);
                TableRow row = new TableRow(getActivity());
                TableLayout.LayoutParams layoutRow = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
                row.setLayoutParams(layoutRow);
                row.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);row.setTag(tag);
                Log.d("d", "row.getId()=" + row.getId() + "  row.getTag=" + row.getTag());
                TextView filename = new TextView(getActivity());
                filename.setText(uri.toString());
                filename.setTextSize(0.0f); // Make it take up no space (user doesn't need to see it).
                filename.setVisibility(View.GONE);
                ImageView thumbnail = new ImageView(getActivity());
                Bitmap ThumbImage;
                Resources res = getResources();
                BitmapDrawable bd;

                if (!storageAccess.uriExists(getActivity(), uri)) {
                    Drawable notfound = getResources().getDrawable(R.drawable.notfound);
                    thumbnail.setImageDrawable(notfound);
                } else {
                    InputStream inputStream = storageAccess.getInputStream(getActivity(), uri);
                    ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeStream(inputStream), 200, 150);
                    bd = new BitmapDrawable(res, ThumbImage);
                    thumbnail.setImageDrawable(bd);
                }
                //thumbnail.setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_black));
                //thumbnail.setImageDrawable(getResources().getDrawable(R.drawable.presenter_box_black));
                //thumbnail.setBackgroundResource(getResources().getDrawable(R.drawable.presenter_box_black,null));
                thumbnail.setMaxWidth(200);
                thumbnail.setMaxHeight(150);
                TableRow.LayoutParams layoutImage = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
                layoutImage.width = 200;
                layoutImage.height = 150;
                thumbnail.setLayoutParams(layoutImage);
                ImageButton delete = new ImageButton(getActivity());
                delete.setImageDrawable(getResources().getDrawable(R.drawable.ic_delete_white_36dp));
                delete.setTag(tag + "_delete");
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String rowtag = v.getTag().toString();
                        rowtag = rowtag.replace("_delete", "");
                        try {
                            if (getView() != null) {
                                TableRow tr = getView().findViewWithTag(rowtag);
                                Log.d("d", "tr.getId()=" + tr.getId() + "  tr.getTag=" + tr.getTag());
                                Log.d("d", "v.getId()=" + v.getId() + "  v.getTag=" + v.getTag());
                                slideImageTable.removeView(tr);
                                Log.d("PopUpCustomSlide", "Trying to remove row with tag " + rowtag);
                            }
                        } catch (Exception e) {
                            // oh well
                            Log.d("error", "No table row with this tag");
                        }
                    }
                });
                row.addView(filename);
                row.addView(thumbnail);
                row.addView(delete);
                slideImageTable.addView(row);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void grabBibleText(String weblink) {
        GrabBibleText grab_Bible_Text = new GrabBibleText(weblink);
        grab_Bible_Text.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @SuppressLint("StaticFieldLeak")
    private class GrabBibleText extends AsyncTask<Object, Void, String> {

        String weblink;
        StringBuilder sb;
        URL url;
        HttpURLConnection urlConnection = null;

        GrabBibleText(String s) {
            weblink = s;
        }

        @Override
        protected void onPreExecute() {
            sb = new StringBuilder();
            StaticVariables.myToastMessage = "";
        }

        @Override
        protected String doInBackground(Object... objects) {
            try {
                url = new URL(weblink);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
                String s;
                while ((s = buffer.readLine()) != null) {
                    sb.append(" ").append(s.trim());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            String scripture_title = "";
            String scripture;

            // TEST THE FULLY EXTRACTED SCRIPTURE (FULLER THAN HEADER)
            String result = sb.toString();
            String newbit = sb.toString();

            // Find the start and end of the scripture bit
            int startoffull = newbit.indexOf("<span class=\"passage-display-version\">");
            startoffull = newbit.indexOf("</span>",startoffull);
            int endoffull = newbit.indexOf("<div class=\"crossrefs hidden\">");

            if (endoffull > startoffull && startoffull > 0) {
                newbit = newbit.substring(startoffull, endoffull);
            } else {
                StaticVariables.myToastMessage = Objects.requireNonNull(getActivity()).getResources().getString(R.string.error_missingsection);
            }

            Log.d("CustomSlideFragment","newbit="+newbit);
            newbit = Html.fromHtml(newbit).toString();
            Log.d("CustomSlideFragment","newbit="+newbit);
            newbit = newbit.replace("<p>", "\n");
            newbit = newbit.replace("</p>", "");

            // Split into lines and trim them
            StringBuilder nl = new StringBuilder();
            String[] lines = newbit.split("\n");
            for (String b:lines) {
                nl.append(" ").append(b.trim());
            }

            //Now look to see if the webcontent has the desired text in it
            if (result.contains("og:description")) {

                // Get the title
                int title_startpos = result.indexOf("<meta name=\"twitter:title\" content=\"") + 36;
                int title_endpos = result.indexOf("\" />", title_startpos);

                try {
                    scripture_title = result.substring(title_startpos, title_endpos);
                } catch (Exception e) {
                    Log.d("Bible", "Error getting scripture title");
                    StaticVariables.myToastMessage = getString(R.string.error_missingsection);
                }

                // Make the scripture more readable by making a line break at the start of the word after 40 chars
                // First split the scripture into an array of words
                scripture = bibleC.shortenTheLines(newbit, 40, 6);

                // Send these back to the popupcustomslide creator window
                FullscreenActivity.scripture_title = scripture_title;
                FullscreenActivity.scripture_verse = scripture;

            } else {
                StaticVariables.myToastMessage = getResources().getString(R.string.error_missingsection);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (!StaticVariables.myToastMessage.equals("")) {
                ShowToast.showToast(getActivity());
            }

            // Try to update the views sent
            searchBible_progressBar.setVisibility(View.GONE);
            grabVerse_Button.setVisibility(View.GONE);
            slideTitleEditText.setText(FullscreenActivity.scripture_title);
            slideContentEditText.setText(FullscreenActivity.scripture_verse);
            reusable_LinearLayout.setVisibility(View.GONE);
            searchBible_LinearLayout.setVisibility(View.GONE);
            slideDetails_RelativeLayout.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class UpdateFields extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... objects) {
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            switch (FullscreenActivity.whattodo) {
                case "customreusable_note":
                    // Fill in the details
                    noteRadioButton.setChecked(true);
                    slideRadioButton.setChecked(false);
                    imageRadioButton.setChecked(false);
                    scriptureRadioButton.setChecked(false);
                    switchViewToNote();
                    slideTitleEditText.setText(FullscreenActivity.customslide_title);
                    slideContentEditText.setText(FullscreenActivity.customslide_content);
                    break;
                case "customreusable_scripture":
                    // Fill in the details
                    noteRadioButton.setChecked(false);
                    slideRadioButton.setChecked(false);
                    imageRadioButton.setChecked(false);
                    scriptureRadioButton.setChecked(true);
                    switchViewToScripture();
                    slideTitleEditText.setText(FullscreenActivity.customslide_title);
                    slideContentEditText.setText(FullscreenActivity.customslide_content);
                    if (FullscreenActivity.scripture_title != null && FullscreenActivity.scripture_verse != null) {
                        slideTitleEditText.setText(FullscreenActivity.scripture_verse);
                        slideContentEditText.setText(FullscreenActivity.scripture_verse);
                        addScripture();
                    }
                    break;
                case "customreusable_slide":
                    // Fill in the details
                    noteRadioButton.setChecked(false);
                    slideRadioButton.setChecked(true);
                    imageRadioButton.setChecked(false);
                    scriptureRadioButton.setChecked(false);
                    switchViewToSlide();
                    slideTitleEditText.setText(FullscreenActivity.customslide_title);
                    slideContentEditText.setText(FullscreenActivity.customslide_content);
                    timeEditText.setText(FullscreenActivity.customimage_time);
                    if (FullscreenActivity.customimage_loop.equals("true")) {
                        loopCheckBox.setChecked(true);
                    } else {
                        loopCheckBox.setChecked(false);
                    }
                    break;
                case "customreusable_image":
                    // Fill in the details
                    noteRadioButton.setChecked(false);
                    slideRadioButton.setChecked(false);
                    imageRadioButton.setChecked(true);
                    scriptureRadioButton.setChecked(false);
                    switchViewToImage();
                    slideTitleEditText.setText(FullscreenActivity.customslide_title);
                    slideContentEditText.setText("");
                    timeEditText.setText(FullscreenActivity.customimage_time);
                    if (FullscreenActivity.customimage_loop.equals("true")) {
                        loopCheckBox.setChecked(true);
                    } else {
                        loopCheckBox.setChecked(false);
                    }
                    // Now parse the list of images...
                    String[] imgs = FullscreenActivity.customimage_list.split("\n");
                    slideImageTable.removeAllViews();
                    for (String img : imgs) {
                        Uri uri = Uri.parse(img);
                        addRow(uri);
                    }
                    break;
            }
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (update_fields!=null) {
            update_fields.cancel(true);
        }
        this.dismiss();
    }

}