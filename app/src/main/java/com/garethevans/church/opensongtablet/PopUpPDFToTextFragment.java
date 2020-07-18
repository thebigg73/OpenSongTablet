/*
package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.garethevans.church.opensongtablet.OLD_TO_DELETE._CustomAnimations;
import com.garethevans.church.opensongtablet.OLD_TO_DELETE._PopUpSizeAndAlpha;
import com.garethevans.church.opensongtablet.OLD_TO_DELETE._ShowToast;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.InputStream;
import java.util.Objects;

public class PopUpPDFToTextFragment extends DialogFragment {
    // This is called when user tries to extract pdf to text

    static PopUpPDFToTextFragment newInstance() {
        PopUpPDFToTextFragment frag;
        frag = new PopUpPDFToTextFragment();
        return frag;
    }

    private String foundText = "";
    private String foundCopyright = "";
    private String foundAuthor = "";
    private String foundCCLI = "";
    private String foundTitle = "";
    private String foundKey = "";
    private String foundTempo = "";
    private String foundTimeSig = "";
    private StorageAccess storageAccess;
    private _Preferences preferences;

    public interface MyInterface {
        void refreshAll();
        void allowPDFEditViaExternal();
        void openFragment();
    }

    private MyInterface mListener;

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (MyInterface) activity;
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        View v = inflater.inflate(R.layout.popup_pdftotext, container, false);

        TextView title = v.findViewById(R.id.dialogtitle);
        title.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.pdftotext_extract));
        final FloatingActionButton closeMe = v.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                cancelEdit();
            }
        });
        final FloatingActionButton saveMe = v.findViewById(R.id.saveMe);
        saveMe.hide();

        storageAccess = new StorageAccess();
        preferences = new _Preferences();

        // Initialise the basic views
        TextView pdftotext_found = v.findViewById(R.id.pdftotext_found);
        pdftotext_found.setTypeface(Typeface.MONOSPACE);
        Button externalPDF = v.findViewById(R.id.externalPDF);
        Button doextractbutton = v.findViewById(R.id.doextractbutton);
        Button justeditbutton = v.findViewById(R.id.justeditbutton);
        justeditbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StaticVariables.whattodo = "editsongpdf";
                if (mListener!=null) {
                    try {
                        mListener.openFragment();
                        dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        doextractbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveEdit();
            }
        });

        externalPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    mListener.allowPDFEditViaExternal();
                    try {
                        dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        getPDFExtractedText();
        pdftotext_found.setText(foundText);

        _PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return v;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

    private void cancelEdit() {
        try {
            dismiss();
        } catch (Exception e) {
            Log.d("d","Error dismissing");
        }
    }

    private void saveEdit() {
        PopUpEditSongFragment.prepareBlankSongXML();
        // Replace the default lyrics with the ones we've found
        FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("[V]\n",foundText);

        // Replace the title line
        if (foundTitle.equals("")) {
            // Use the filename and remove the .pdf
            FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace(".pdf","_from_pdf");
            FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace(".PDF","_from_pdf");
        } else {
            FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("<title>" + StaticVariables.songfilename + "</title>",
                    "<title>" + foundTitle + "</title>");
        }

        // Replace the author line
        FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("<author></author>",
                "<author>"+foundAuthor+"</author>");

        // Replace the copyright line
        FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("<copyright></copyright>",
                "<copyright>"+foundCopyright+"</copyright>");

        // Replace the copyright line
        FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("<copyright></copyright>",
                "<copyright>"+foundCopyright+"</copyright>");

        // Replace the tempo line
        FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("<tempo></tempo>",
                "<tempo>"+foundTempo+"</tempo>");

        // Replace the time signature line
        FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("<timesig></timesig>",
                "<timesig>"+foundTimeSig+"</timesig>");

        // Replace the key line
        FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("<key></key>",
                "<key>"+foundKey+"</key>");

        // Replace the ccli line
        FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("<ccli></ccli>",
                "<ccli>"+foundCCLI+"</ccli>");

        Log.d("d","mynewXML="+FullscreenActivity.mynewXML);

        // Change the file name
        FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace(".pdf","_from_pdf");
        FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace(".PDF","_from_pdf");
        StaticVariables.songfilename = StaticVariables.songfilename.replace(".pdf","_from_pdf");
        StaticVariables.songfilename = StaticVariables.songfilename.replace(".PDF","_from_pdf");

        FullscreenActivity.isPDF = false;
        FullscreenActivity.isImage = false;

        try {
            if (mListener!=null) {

                PopUpEditSongFragment.justSaveSongXML(getActivity(), preferences);
                */
/*if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
                    NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper = new NonOpenSongSQLiteHelper(getActivity());
                    nonOpenSongSQLiteHelper.createBasicSong(getActivity(),storageAccess,preferences,StaticVariables.whichSongFolder,StaticVariables.songfilename);
                    NonOpenSongSQLite nonOpenSongSQLite = nonOpenSongSQLiteHelper.getSong(getActivity(),storageAccess,preferences,nonOpenSongSQLiteHelper.getSongId());
                    nonOpenSongSQLiteHelper.updateSong(getActivity(),storageAccess,preferences,nonOpenSongSQLite);
                } else {

                }*//*

                StaticVariables.myToastMessage = "";
                mListener.refreshAll();
                dismiss();
            }
        } catch (Exception e) {
            Log.d("d","Error saving");
            StaticVariables.myToastMessage = Objects.requireNonNull(getActivity()).getResources().getString(R.string.save) + " - " +
                    getActivity().getResources().getString(R.string.error);
            _ShowToast.showToast(getActivity());
        }
    }

    private void getPDFExtractedText() {
        StringBuilder parsedText= new StringBuilder();

        try {
            Uri uri = storageAccess.getUriForItem(getActivity(), preferences, "Songs", StaticVariables.whichSongFolder,
                    StaticVariables.songfilename);
            InputStream inputStream = storageAccess.getInputStream(getActivity(),uri);
            PdfReader reader = new PdfReader(inputStream);
            int n = reader.getNumberOfPages();
            for (int i = 1; i<=n ; i++) {
                String text = detectAndImproveLine(PdfTextExtractor.getTextFromPage(reader, i));
                parsedText.append(text).append("\n"); //Extracting the content from the different pages
            }
            reader.close();
        } catch (Exception e) {
            Log.d("d","Error extracting text");
        }
        foundText = PopUpEditSongFragment.parseToHTMLEntities(parsedText.toString());
    }

    private String detectAndImproveLine(String alltext) {
        StringBuilder fixedtext = new StringBuilder();
        // Split the text into lines
        String[] lines = alltext.split("\n");
        for (String s:lines) {
            String c = s.toLowerCase(StaticVariables.locale).trim();
            String verse_tag = Objects.requireNonNull(getActivity()).getString(R.string.tag_verse).toLowerCase(StaticVariables.locale);
            String chorus_tag = getActivity().getString(R.string.tag_chorus).toLowerCase(StaticVariables.locale);
            String prechorus_tag = getActivity().getString(R.string.tag_prechorus).toLowerCase(StaticVariables.locale);
            String bridge_tag = getActivity().getString(R.string.tag_bridge).toLowerCase(StaticVariables.locale);

            if ((c.startsWith("verse") || c.startsWith("chorus") || c.startsWith("bridge") ||
                    c.startsWith("intro") || c.startsWith("outro") || c.startsWith("ending") ||
                    c.startsWith("instr") || c.startsWith("interlude") || c.startsWith(verse_tag) ||
                    c.startsWith(chorus_tag) || c.startsWith(prechorus_tag) || c.startsWith(bridge_tag))
                    && c.length() < 20) {
                // Likely to be a heading....
                s = "[" + s.trim() + "]";

            } else if (c.startsWith("©") || c.contains("copyright") || c.startsWith("@") ||
                    c.startsWith("&copy") || c.startsWith("&#169;")) {
                // Likely coming from a song select pdf file line - add to the copyright
                foundCopyright = s.trim();
                // Remove this line
                s = "";

            } else if (c.startsWith("ccli licence") || c.startsWith("note: repro") ||
                    c.startsWith("for use solely")) {
                // Likely coming from a song select pdf file line - don't need this as app deals with it
                s = "";

            } else if (c.startsWith("ccli song")) {
                // Likely coming from a song select pdf file line - add to the ccli number
                s = s.replace("CCLI","");
                s = s.replace("Song","");
                s = s.replace("#","");
                foundCCLI = s.trim();
                // Now remove this line
                s = "";

            } else if (c.contains("key -")) {
                // If this is the 2nd line and the 1st line isn't blank, the first line will be the title
                if (lines.length>1 && lines[1].equals(s) && !lines[0].equals("")) {
                    foundTitle = lines[0].trim();
                }
                // Likely coming from a song select pdf file line - add to the key
                s = s.replace("Key","");
                s = s.replace("-","");
                foundKey = s.trim();
                // Now remove this line
                s = "";


            } else if (c.contains("tempo - ") || c.contains("time -")) {
                // Likely coming from a song select pdf file line - this line contains author, tempo and timesig
                int tempopos = s.indexOf("Tempo");
                if (tempopos>0) {
                    // Author is the stuff before this
                    foundAuthor = s.substring(0,tempopos);
                    // Remove this from the string
                    s = s.replace(foundAuthor,"");
                    foundAuthor = foundAuthor.trim();
                    String[] bits = s.split("\\|");
                    if (bits.length>0) {
                        s = s.replace(bits[0],"");
                        s = s.replace("|","");
                        bits[0] = bits[0].replace("Tempo","");
                        bits[0] = bits[0].replace("-","");
                        foundTempo = bits[0].trim();
                    }
                    if (bits.length>1) {
                        s = s.replace(bits[1],"");
                        bits[1] = bits[1].replace("Time","");
                        bits[1] = bits[1].replace("-","");
                        foundTimeSig = bits[1].trim();
                    }
                    // We can remove this line now if it is empty, if not keep the remainder as a comment
                    s = s.trim();
                    if (!s.equals("")) {
                        s = ";"+s+"\n";
                    }

                } else {
                    // Can't extract reliably, so add as a comment line
                    s = ";" + s;
                }

            } else if ((s.contains("7 ") || s.contains("m7 ") || s.contains("# ") || s.contains("#7") ||
                    s.contains("b7 ") || s.contains("#m") ||
                    s.contains("B ") || s.contains("Bb") || s.contains("C ") || s.contains("D ") ||
                    s.contains("Db") || s.contains("E ") || s.contains("Eb") || s.contains("F ") ||
                    s.contains("Gb") || s.contains("G ")) && s.replace(" ", "").trim().length() < 20) {
                // Likely to be a chord line...
                s = "." + s;
            } else if (s.replace(" ","").trim().length()<5) {
                // Likely to be a chord line as it isn't a heading and is very small!...
                s = "." + s;
            } else {
                s = " " + s;
            }
            fixedtext.append(s).append("\n");
        }
        return fixedtext.toString().trim();
    }

}*/
