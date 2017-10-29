package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

public class PopUpPDFToTextFragment extends DialogFragment {
    // This is called when user tries to extract pdf to text

    static PopUpPDFToTextFragment newInstance() {
        PopUpPDFToTextFragment frag;
        frag = new PopUpPDFToTextFragment();
        return frag;
    }

    View V;
    String foundText = "";
    String foundCopyright = "";
    String foundAuthor = "";
    String foundCCLI = "";
    String foundTitle = "";
    String foundKey = "";
    String foundTempo = "";
    String foundTimeSig = "";

    public interface MyInterface {
        void refreshAll();
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
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        V = inflater.inflate(R.layout.popup_pdftotext, container, false);

        TextView title = (TextView) V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.pdftotext_extract));
        final FloatingActionButton closeMe = (FloatingActionButton) V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                cancelEdit();
            }
        });
        final FloatingActionButton saveMe = (FloatingActionButton) V.findViewById(R.id.saveMe);
        saveMe.setVisibility(View.GONE);

        // Initialise the basic views
        TextView pdftotext_found = (TextView) V.findViewById(R.id.pdftotext_found);
        pdftotext_found.setTypeface(Typeface.MONOSPACE);
        Button doextractbutton = (Button) V.findViewById(R.id.doextractbutton);
        doextractbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveEdit();
            }
        });

        getPDFExtractedText();
        pdftotext_found.setText(foundText);

        return V;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

    public void cancelEdit() {
        try {
            dismiss();
        } catch (Exception e) {
            Log.d("d","Error dismissing");
        }
    }

    public void saveEdit() {
        PopUpEditSongFragment.prepareBlankSongXML();
        // Replace the default lyrics with the ones we've found
        FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("[V]\n",foundText);

        // Replace the title line
        if (foundTitle.equals("")) {
            // Use the filename and remove the .pdf
            FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace(".pdf","_from_pdf");
            FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace(".PDF","_from_pdf");
        } else {
            FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("<title>" + FullscreenActivity.songfilename + "</title>",
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

        // Change the file name
        FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace(".pdf","_from_pdf");
        FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace(".PDF","_from_pdf");
        FullscreenActivity.songfilename = FullscreenActivity.songfilename.replace(".pdf","_from_pdf");
        FullscreenActivity.songfilename = FullscreenActivity.songfilename.replace(".PDF","_from_pdf");
        try {
            PopUpEditSongFragment.justSaveSongXML();
            if (mListener!=null) {
                FullscreenActivity.myToastMessage = "";
                mListener.refreshAll();
                dismiss();
            }
        } catch (Exception e) {
            Log.d("d","Error saving");
        }
    }

    public void getPDFExtractedText() {
        String parsedText="";
        try {
            PdfReader reader = new PdfReader(FullscreenActivity.file.toString());
            int n = reader.getNumberOfPages();
            for (int i = 1; i<=n ; i++) {
                String text = detectAndImproveLine(PdfTextExtractor.getTextFromPage(reader, i));
                parsedText = parsedText + text  +"\n"; //Extracting the content from the different pages
            }
            reader.close();
        } catch (Exception e) {
            Log.d("d","Error extracting text");
        }
        foundText = parsedText;
    }

    public String detectAndImproveLine(String alltext) {
        String fixedtext = "";
        // Split the text into lines
        String[] lines = alltext.split("\n");
        for (String s:lines) {
            String c = s.toLowerCase(FullscreenActivity.locale).trim();
            String verse_tag = getActivity().getString(R.string.tag_verse).toLowerCase(FullscreenActivity.locale);
            String chorus_tag = getActivity().getString(R.string.tag_chorus).toLowerCase(FullscreenActivity.locale);
            String prechorus_tag = getActivity().getString(R.string.tag_prechorus).toLowerCase(FullscreenActivity.locale);
            String bridge_tag = getActivity().getString(R.string.tag_bridge).toLowerCase(FullscreenActivity.locale);

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
                Log.d("d","foundCCLI="+foundCCLI);
                // Now remove this line
                s = "";

            } else if (c.contains("key -")) {
                // If this is the 2nd line and the 1st line isn't blank, the first line will be the title
                if (lines.length>1 && lines[1].equals(s) && !lines[0].equals("")) {
                    foundTitle = lines[0].trim();
                    Log.d("d","foundTitle="+foundTitle);
                }
                // Likely coming from a song select pdf file line - add to the key
                s = s.replace("Key","");
                s = s.replace("-","");
                foundKey = s.trim();
                Log.d("d","foundKey="+foundKey);
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
                    Log.d("d","foundAuthor="+foundAuthor);
                    String[] bits = s.split("\\|");
                    if (bits.length>0) {
                        s = s.replace(bits[0],"");
                        s = s.replace("|","");
                        bits[0] = bits[0].replace("Tempo","");
                        bits[0] = bits[0].replace("-","");
                        foundTempo = bits[0].trim();
                        Log.d("d","foundTempo="+foundTempo);
                    }
                    if (bits.length>1) {
                        s = s.replace(bits[1],"");
                        bits[1] = bits[1].replace("Time","");
                        bits[1] = bits[1].replace("-","");
                        foundTimeSig = bits[1].trim();
                        Log.d("d","foundTimeSig="+foundTimeSig);
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
            Log.d("d", "s=" + s);
            fixedtext = fixedtext + s + "\n";
        }
        return fixedtext.trim();
    }
}
