package com.garethevans.church.opensongtablet.export;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsExportBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class ExportFragment extends Fragment {

    // TODO The app isn't setting isSong/isPDF/isImage
    // This means the correct export methods don't quite work
    // Currently everything is in the 'else' statements

    private SettingsExportBinding myView;
    private MainActivityInterface mainActivityInterface;
    private ArrayList<Uri> uris;
    private Uri uri;
    private final String TAG = "ExportFragment";
    private final MutableLiveData<Boolean> listen = new MutableLiveData<>();
    private ArrayList<View> sectionViewsPDF;
    private ArrayList<Integer> sectionViewWidthsPDF, sectionViewHeightsPDF;
    private LinearLayout headerLayoutPDF;
    private int headerLayoutWidth, headerLayoutHeight;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsExportBinding.inflate(inflater,container,false);

        mainActivityInterface.updateToolbar(getString(R.string.export));

        // Some options are hidden by default and only visible if we have a proper OpenSong song
        showUsable();

        myView.shareButton.setOnClickListener(v -> prepareExport());

        return myView.getRoot();
    }


    private void showUsable() {
        // By default everything is hidden.  Only make the correct ones visible
        if (mainActivityInterface.getSong().getFiletype().equals("PDF")) {
            myView.pdf.setVisibility(View.VISIBLE);
            myView.pdf.setChecked(true);

        } else if (mainActivityInterface.getSong().getFiletype().equals("IMG")) {
            myView.image.setVisibility(View.VISIBLE);
            myView.image.setChecked(true);

        } else if (mainActivityInterface.getSong().getFiletype().equals("XML")){
            // Must be a song!
            myView.pdf.setVisibility(View.VISIBLE);
            myView.openSong.setVisibility(View.VISIBLE);
            myView.openSongApp.setVisibility(View.VISIBLE);
            myView.onSong.setVisibility(View.VISIBLE);
            myView.chordPro.setVisibility(View.VISIBLE);
            myView.text.setVisibility(View.VISIBLE);
            myView.pdf.setChecked(true);
        }

        // Make sure the progress bar is hidden
        myView.progressBar.setVisibility(View.GONE);
    }

    private void prepareExport() {
        // If we are exporting a song, we do this once, if not, we do it for each song in the set
        myView.progressBar.setVisibility(View.VISIBLE);
        myView.shareButton.setEnabled(false);

        if (mainActivityInterface.getWhattodo().equals("exportset")) {
            uris.add(null);

        } else {
            if (myView.pdf.isChecked()) {
                if (mainActivityInterface.getSong().getFiletype().equals("PDF")) {
                    // We are just copying an existing PDF
                    initiateShare("application/pdf");

                } else {
                    // Create PDF song on the fly
                    listen.setValue(false); //Initilize with a value
                    createOnTheFly(requireContext(), mainActivityInterface, mainActivityInterface.getSong());
                    listen.observe(getViewLifecycleOwner(), isDone -> {
                        Log.d(TAG, "Received notification isDone=" + isDone);
                        if (isDone) {
                            listen.removeObservers(getViewLifecycleOwner());
                            initiateShare("application/pdf");
                        }
                    });
                }

            } else if (myView.onSong.isChecked() || myView.chordPro.isChecked() || myView.text.isChecked()) {
                initiateShare("text/plain");

            } else if (myView.openSong.isChecked() || myView.openSongApp.isChecked()) {
                initiateShare("text/xml");

            } else if (myView.image.isChecked()) {
                initiateShare("image/*");

            }
        }

    }

    private void initiateShare(String type) {
        Log.d(TAG,"Initiate share started");
        String exportFilename = "";
        String content = null;

        // Sharing a song should initiate the CCLI Log of printed (value 6)
        if (mainActivityInterface.getPreferences().getMyPreferenceBoolean(requireContext(),"ccliAutomaticLogging",false)) {
            mainActivityInterface.getCCLILog().addEntry(requireContext(),mainActivityInterface,mainActivityInterface.getSong(),"6");
        }

        if ((mainActivityInterface.getSong().getFiletype().equals("PDF") && type.equals("application/pdf") && myView.pdf.isChecked()) ||
                (mainActivityInterface.getSong().getFiletype().equals("IMG") && type.equals("image/*") && myView.image.isChecked())) {
            // This is for PDF or image songs  Only allow export in the same format
            uri = getActualFile();
            exportFilename = mainActivityInterface.getSong().getFilename();

        } else if (mainActivityInterface.getSong().getFiletype().equals("XML")){
            // These options are for OpenSong songs
            if (type.equals("application/pdf")) {
                exportFilename = getExportFilename(".pdf");

                // Create a PDF on the fly
                uri = mainActivityInterface.getMakePDF().createTextPDF(requireContext(), mainActivityInterface,
                        mainActivityInterface.getSong(), sectionViewsPDF, sectionViewWidthsPDF,
                        sectionViewHeightsPDF, headerLayoutPDF, headerLayoutWidth, headerLayoutHeight, exportFilename);

            } else if (type.equals("text/xml") && myView.openSongApp.isChecked()) {
                // Make a copy of the file as an .ost
                exportFilename = getExportFilename(".ost");
                uri = copyOpenSongApp();

            } else if (type.equals("text/xml") && myView.openSong.isChecked()) {
                // Make a copy of the file as is
                uri = getActualFile();
                exportFilename = getExportFilename("");

            } else if (type.equals("text/plain") && myView.onSong.isChecked()) {
                // Convert the song to OnSong format and save it
                content = mainActivityInterface.getPrepareFormats().getSongAsOnSong(requireContext(),
                        mainActivityInterface, mainActivityInterface.getSong());
                uri = getExportUri(".onsong");
                OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(requireContext(), uri);
                mainActivityInterface.getStorageAccess().writeFileFromString(content, outputStream);
                exportFilename = getExportFilename(".onsong");

            } else if (type.equals("text/plain") && myView.chordPro.isChecked()) {
                // Convert the song to ChordPro format and save it
                content = mainActivityInterface.getPrepareFormats().getSongAsChoPro(requireContext(),
                        mainActivityInterface, mainActivityInterface.getSong());
                uri = getExportUri(".cho");
                OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(requireContext(), uri);
                mainActivityInterface.getStorageAccess().writeFileFromString(content, outputStream);
                exportFilename = getExportFilename(".cho");

            } else if (type.equals("text/plain") && myView.text.isChecked()) {
                // Convert the song to text format and save it
                content = mainActivityInterface.getPrepareFormats().getSongAsText(mainActivityInterface.getSong());
                uri = getExportUri(".txt");
                OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(requireContext(), uri);
                mainActivityInterface.getStorageAccess().writeFileFromString(content, outputStream);
                exportFilename = getExportFilename(".txt");

            }

        }

        Log.d(TAG,"exportFilename="+exportFilename);

        Intent intent = mainActivityInterface.getExportActions().setShareIntent(exportFilename,exportFilename,content,type,uri);
        startActivity(Intent.createChooser(intent,getString(R.string.export_current_song)));
        myView.progressBar.setVisibility(View.GONE);
        myView.shareButton.setEnabled(true);
    }

    private Uri getActualFile() {
        return mainActivityInterface.getStorageAccess().getUriForItem(requireContext(),
                mainActivityInterface, "Songs",
                mainActivityInterface.getSong().getFolder(), mainActivityInterface.getSong().getFilename());
    }
    private Uri copyOpenSongApp() {
        uri = getExportUri(".ost");
        InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(requireContext(),getActualFile());
        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(requireContext(),uri);
        mainActivityInterface.getStorageAccess().copyFile(inputStream,outputStream);
        return uri;
    }
    private String getExportFilename(String extension) {
        return mainActivityInterface.getSong().getFolder().replace("/","_") + "_" +
                mainActivityInterface.getSong().getFilename().replace("/","_") + extension;
    }
    private Uri getExportUri(String extension) {
        String exportFilename = getExportFilename(extension);
        uri = mainActivityInterface.getStorageAccess().getUriForItem(requireContext(),
                mainActivityInterface,"Export","",
                exportFilename);

        // If the output file exists, delete it first (so we sort of overwrite)
        if (mainActivityInterface.getStorageAccess().uriExists(requireContext(),uri)) {
            mainActivityInterface.getStorageAccess().deleteFile(requireContext(),uri);
        }

        // Now create a blank file ready to assign an outputStream
        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(requireContext(),
                mainActivityInterface,uri,null,"Export","",
                exportFilename);

        return uri;
    }

    // We can create nice views on the fly here by processing the Song, then populating the views
    private void createOnTheFly(Context c, MainActivityInterface mainActivityInterface, Song thisSong) {
        // Make sure any current headers/sections are wiped
        initialiseViews();

        // Now start preparing the views.  First up the header
        createOnTheFlyHeader(c,mainActivityInterface,thisSong);
    }
    private void initialiseViews() {
        sectionViewsPDF = new ArrayList<>();
        sectionViewWidthsPDF = new ArrayList<>();
        sectionViewHeightsPDF = new ArrayList<>();
        headerLayoutPDF = new LinearLayout(requireContext());
        myView.hiddenSections.removeAllViews();
        myView.hiddenHeader.removeAllViews();
    }
    private void createOnTheFlyHeader(Context c, MainActivityInterface mainActivityInterface, Song thisSong) {
        // Get the song sheet header
        // Once this has drawn, move to the next stage of the song sections
        float scaleComments = mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"scaleComments",0.8f);
        Log.d(TAG,"About to make the header");

        ViewTreeObserver headerVTO = myView.hiddenHeader.getViewTreeObserver();
        headerVTO.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.d(TAG,"Header is ready");
                myView.hiddenHeader.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                headerLayoutWidth = myView.hiddenHeader.getMeasuredWidth();
                headerLayoutHeight = myView.hiddenHeader.getMeasuredHeight();
                Log.d(TAG,"Header is ready at "+myView.hiddenHeader.getMeasuredWidth()+"x"+myView.hiddenHeader.getMeasuredHeight());
                myView.hiddenHeader.removeAllViews();
                createOnTheFlySections(c,mainActivityInterface,thisSong,scaleComments);
            }
        });

        // Now draw it here for measuring via the VTO
        headerLayoutPDF = mainActivityInterface.getSongSheetHeaders().getSongSheet(c,mainActivityInterface,thisSong,
                scaleComments,true);
        myView.hiddenHeader.addView(headerLayoutPDF);
    }
    private void createOnTheFlySections(Context c, MainActivityInterface mainActivityInterface, Song thisSong, float scaleComments) {
        Log.d(TAG,"About to make the sections");

        boolean trimSections = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"trimSections",true);
        boolean trimLines = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"trimLines",true);
        boolean addSectionSpace = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"addSectionSpace",true);
        boolean displayBoldChordsHeadings = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"displayBoldChordsHeadings",false);
        float lineSpacing = mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"lineSpacing",0.1f);
        float scaleHeadings = mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"scaleHeadings",0.6f);
        float scaleChords = mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"scaleChords",0.8f);

        // Create the content for the section views.
        sectionViewsPDF = mainActivityInterface.getProcessSong().
                setSongInLayout(c,mainActivityInterface, trimSections, addSectionSpace,
                        trimLines, lineSpacing, scaleHeadings, scaleChords, scaleComments,
                        thisSong.getLyrics(),displayBoldChordsHeadings,true);

        // Now we have the views, add them to the temp layout and set up a view tree listener to measure
        ViewTreeObserver sectionsVTO = myView.hiddenSections.getViewTreeObserver();
        sectionsVTO.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // The views are ready so lets measure them after clearing this listener
                Log.d(TAG,"Sections are ready");

                // If all the views are there, we can start measuring
                if (myView.hiddenSections.getChildCount()==sectionViewsPDF.size()) {
                    myView.hiddenSections.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    for (int x=0; x<myView.hiddenSections.getChildCount(); x++) {
                        View view = myView.hiddenSections.getChildAt(x);
                        int width = view.getMeasuredWidth();
                        int height = view.getMeasuredHeight();
                        Log.d(TAG,width+" x "+height);
                        sectionViewWidthsPDF.add(width);
                        sectionViewHeightsPDF.add(height);
                    }
                    // Now detach from this view (can only be shown in one layout)
                    myView.hiddenSections.removeAllViews();

                    // Now trigger the next step of preparing the pdf from the views created on the fly
                    listen.setValue(true);
                }
            }
        });
        // Add the section views and this will trigger the VTO
        for (int x=0; x<sectionViewsPDF.size(); x++) {
            myView.hiddenSections.addView(sectionViewsPDF.get(x));
        }
    }
}
