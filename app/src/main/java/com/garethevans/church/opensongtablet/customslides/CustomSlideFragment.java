package com.garethevans.church.opensongtablet.customslides;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsCustomSlideBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.TextInputBottomSheet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class CustomSlideFragment extends Fragment {

    private SettingsCustomSlideBinding myView;
    private MainActivityInterface mainActivityInterface;
    private ActivityResultLauncher<Intent> addImagesLauncher;
    private final String TAG = "CustomSlideFragment";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsCustomSlideBinding.inflate(inflater, container, false);
        mainActivityInterface.updateToolbar(getString(R.string.custom_slide));
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        // Set up listeners
        setupListeners();

        // Set up views (do these after listeners so we trigger them)
        setupViews();

        // Set up launchers;
        setupLaunchers();

        // Set up the activity launcher for file selection
        return myView.getRoot();
    }

    private void setupViews() {
        mainActivityInterface.getProcessSong().editBoxToMultiline(myView.content);
        mainActivityInterface.getProcessSong().stretchEditBoxToLines(myView.content,8);
        switch (mainActivityInterface.getCustomSlide().getCreateType()) {
            case "note":
            default:
                myView.customNote.setChecked(true);
                break;
            case "slide":
                myView.customSlide.setChecked(true);
                break;
            case "image":
                myView.customImageSlide.setChecked(true);
                break;
        }
        myView.loopSlides.setChecked(mainActivityInterface.getCustomSlide().getCreateLoop());
        myView.title.setText(mainActivityInterface.getCustomSlide().getCreateTitle());
        myView.addReusable.setChecked(mainActivityInterface.getCustomSlide().getCreateReusable());
        myView.content.setText(mainActivityInterface.getCustomSlide().getCreateContent());
    }

    private void setupListeners() {
        // Add a new page to the content
        myView.addPage.setOnClickListener(v -> {
            if (mainActivityInterface.getCustomSlide().getCreateType().equals("image")) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("image/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                addImagesLauncher.launch(intent);
            } else {
                String currentText = "";
                if (myView.content.getText() != null) {
                    currentText = myView.content.getText().toString();
                }
                // Add the new line text
                currentText = currentText + "\n---\n";
                myView.content.setText(currentText);
                myView.content.requestFocus();
                myView.content.setSelection(currentText.length());
            }
        });
        myView.loadReusable.setOnClickListener(v -> loadReusable());
        // The switches
        myView.addReusable.setOnCheckedChangeListener((compoundButton, b) -> mainActivityInterface.getCustomSlide().setCreateReusable(b));
        myView.loopSlides.setOnCheckedChangeListener((compoundButton, b) -> mainActivityInterface.getCustomSlide().setCreateLoop(b));
        // The slide type
        myView.customNote.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                showViews(false,false,true,false);
                mainActivityInterface.getCustomSlide().setCreateType("note");
            }
        });
        myView.customSlide.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                showViews(true,true,true,false);
                mainActivityInterface.getCustomSlide().setCreateType("slide");
            }
        });
        myView.customImageSlide.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                showViews(true,true,false,true);
                mainActivityInterface.getCustomSlide().setCreateType("image");
                buildImageRows();
            }
        });
        // Add to the set
        myView.addToSet.setOnClickListener(v -> addToSet());
        // Text listeners
        myView.title.addTextChangedListener(new MyTextWatcher("title"));
        myView.content.addTextChangedListener(new MyTextWatcher("content"));
        myView.time.addTextChangedListener(new MyTextWatcher("time"));

        myView.nestedScrollView.setExtendedFabToAnimate(myView.addToSet);
    }

    @SuppressLint("WrongConstant")
    private void setupLaunchers() {
        // Initialise the launcher
        addImagesLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                try {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri contentUri = data.getData();
                        // If this is a localised (i.e. inside OpenSong folder), we don't need to take the permissions
                        // There is a limit of 128-512 permissions allowed (depending on Android version).
                        String localisedUri = mainActivityInterface.getStorageAccess().fixUriToLocal(contentUri);
                        if (!localisedUri.contains("../OpenSong/")) {
                            ContentResolver resolver = requireActivity().getContentResolver();
                            resolver.takePersistableUriPermission(contentUri, data.getFlags()
                                    & Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }
                        // Add item to the list
                        mainActivityInterface.getCustomSlide().setCreateImages(mainActivityInterface.getCustomSlide().getCreateImages()+"\n"+localisedUri);
                        addRow(Uri.parse(localisedUri));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void showViews(boolean showTime, boolean showLoop, boolean showContent, boolean showImageContent) {
        showView(myView.time,showTime);
        showView(myView.loopSlides,showLoop);
        showView(myView.content,showContent);
        showView(myView.slideImageTable,showImageContent);
    }

    private void showView(View view, boolean showView) {
        if (showView) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    private void addToSet() {
        ArrayList<String> newSlide = new ArrayList<>();
        // Slide type
        newSlide.add(mainActivityInterface.getCustomSlide().getCreateType());
        // Slide title
        newSlide.add(mainActivityInterface.getCustomSlide().getCreateTitle());
        // Slide content
        newSlide.add(mainActivityInterface.getCustomSlide().getCreateContent());

        // Now the additional stuff for custom slide/image slide
        switch (mainActivityInterface.getCustomSlide().getCreateType()) {
            case "slide":
            case "image":
                newSlide.add(mainActivityInterface.getCustomSlide().getCreateTime());
                newSlide.add(mainActivityInterface.getCustomSlide().getCreateLoop()+"");
                newSlide.add(mainActivityInterface.getCustomSlide().getCreateImages());
                break;
        }

        mainActivityInterface.getCustomSlide().buildCustomSlide(requireContext(),mainActivityInterface,newSlide);
        mainActivityInterface.getCustomSlide().addItemToSet(requireContext(), mainActivityInterface,true);
    }

    private class MyTextWatcher implements TextWatcher {

        private final String which;

        MyTextWatcher(String which) {
            this.which = which;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void afterTextChanged(Editable editable) {
            switch (which) {
                case "title":
                    mainActivityInterface.getCustomSlide().setCreateTitle(editable.toString());
                    break;
                case "content":
                    mainActivityInterface.getCustomSlide().setCreateContent(editable.toString());
                    break;
                case "time":
                    mainActivityInterface.getCustomSlide().setCreateTime(editable.toString());
                    break;
            }
        }
    }

    private void buildImageRows() {
        String[] images = mainActivityInterface.getCustomSlide().getCreateImages().split("\n");
        myView.slideImageTable.removeAllViews();
        for (String image:images) {
            Uri uri = Uri.parse(image);
            addRow(uri);
        }
    }

    private void addRow(Uri uri) {
        if (uri != null && uri.getPath() != null && !uri.getPath().isEmpty()) {
            Log.d(TAG,"uri="+uri);
            try {
                // Prepare the tag - use the file name and base 64 encode it to make it safe
                byte[] data = uri.getPath().getBytes(StandardCharsets.UTF_8);
                String tag = Base64.encodeToString(data, Base64.DEFAULT);
                TableRow row = (TableRow) getLayoutInflater().inflate(R.layout.view_slide_image_row,null,false);
                row.setTag(tag);
                TextView filename = row.findViewById(R.id.uriEncoded);
                filename.setText(uri.toString());
                ImageView thumbnail = row.findViewById(R.id.image);
                Bitmap ThumbImage;
                BitmapDrawable bd;

                if (uri.getPath().startsWith("../OpenSong/")) {
                    uri = mainActivityInterface.getStorageAccess().fixLocalisedUri(requireContext(),mainActivityInterface,uri.getPath());
                }
                if (!mainActivityInterface.getStorageAccess().uriExists(requireContext(), uri)) {
                    Drawable notfound = ContextCompat.getDrawable(requireContext(),R.drawable.notfound);
                    thumbnail.setImageDrawable(notfound);
                } else {
                    InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(requireContext(), uri);
                    ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeStream(inputStream), dpToPx(200), dpToPx(150));
                    bd = new BitmapDrawable(getResources(), ThumbImage);
                    thumbnail.setImageDrawable(bd);
                }

                FloatingActionButton delete = row.findViewById(R.id.delete);
                delete.setTag(tag + "_delete");
                delete.setOnClickListener(v -> {
                    String rowtag = v.getTag().toString();
                    rowtag = rowtag.replace("_delete", "");
                    try {
                        if (getView() != null) {
                            TableRow tr = getView().findViewWithTag(rowtag);
                            myView.slideImageTable.removeView(tr);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                myView.slideImageTable.addView(row);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadReusable() {
        // List the files in the current folder
        String folder = "";
        switch (mainActivityInterface.getCustomSlide().getCreateType()) {
            case "note":
                folder = "Notes";
                break;
            case "slide":
                folder = "Slides";
                break;
            case "image":
                folder = "Images";
                break;
        }
        ArrayList<String> filesFound = mainActivityInterface.getStorageAccess().listFilesInFolder(requireContext(),mainActivityInterface,folder,"");
        TextInputBottomSheet textInputBottomSheet = new TextInputBottomSheet(this,"CustomSlideFragment",getString(R.string.load_reusable),getString(R.string.file_chooser),null,null,"",filesFound);
        textInputBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"textInputBottomSheet");
    }

    private int dpToPx(int dp) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) Math.ceil(dp * scale);
    }
}
