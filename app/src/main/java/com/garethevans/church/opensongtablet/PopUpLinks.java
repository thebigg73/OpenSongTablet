package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class PopUpLinks extends DialogFragment {

    ImageButton linkYouTube_ImageButton;
    ImageButton linkWeb_ImageButton;
    ImageButton linkAudio_ImageButton;
    ImageButton linkOther_ImageButton;
    Button linkClose;
    Button linkSave;
    EditText linkYouTube_EditText;
    EditText linkWeb_EditText;
    EditText linkAudio_EditText;
    EditText linkOther_EditText;
    ImageButton linkYouTubeClear_ImageButton;
    ImageButton linkWebClear_ImageButton;
    ImageButton linkAudioClear_ImageButton;
    ImageButton linkOtherClear_ImageButton;

    static PopUpLinks newInstance() {
        PopUpLinks frag;
        frag = new PopUpLinks();
        return frag;
    }

    public interface MyInterface {
        void refreshAll();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(getActivity().getResources().getString(R.string.link));

        final View V = inflater.inflate(R.layout.popup_links, container, false);

        // Initialise the views
        linkYouTube_ImageButton = (ImageButton) V.findViewById(R.id.linkYouTube_ImageButton);
        linkWeb_ImageButton = (ImageButton) V.findViewById(R.id.linkWeb_ImageButton);
        linkAudio_ImageButton = (ImageButton) V.findViewById(R.id.linkAudio_ImageButton);
        linkOther_ImageButton = (ImageButton) V.findViewById(R.id.linkOther_ImageButton);
        linkYouTube_EditText = (EditText) V.findViewById(R.id.linkYouTube_EditText);
        linkWeb_EditText = (EditText) V.findViewById(R.id.linkWeb_EditText);
        linkAudio_EditText = (EditText) V.findViewById(R.id.linkAudio_EditText);
        linkOther_EditText = (EditText) V.findViewById(R.id.linkOther_EditText);
        linkYouTubeClear_ImageButton = (ImageButton) V.findViewById(R.id.linkYouTubeClear_ImageButton);
        linkWebClear_ImageButton = (ImageButton) V.findViewById(R.id.linkWebClear_ImageButton);
        linkAudioClear_ImageButton = (ImageButton) V.findViewById(R.id.linkAudioClear_ImageButton);
        linkOtherClear_ImageButton = (ImageButton) V.findViewById(R.id.linkOtherClear_ImageButton);
        linkClose = (Button) V.findViewById(R.id.linkClose);
        linkSave = (Button) V.findViewById(R.id.linkSave);

        // Put any links in to the text fields
        linkYouTube_EditText.setText(FullscreenActivity.mLinkYouTube);
        linkWeb_EditText.setText(FullscreenActivity.mLinkWeb);
        linkAudio_EditText.setText(FullscreenActivity.mLinkAudio);
        linkOther_EditText.setText(FullscreenActivity.mLinkOther);

        // Set listeners to clear the fields
        linkYouTubeClear_ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linkYouTube_EditText.setText("");
            }
        });
        linkWebClear_ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linkWeb_EditText.setText("");
            }
        });
        linkAudioClear_ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linkAudio_EditText.setText("");
            }
        });
        linkOtherClear_ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linkOther_EditText.setText("");
            }
        });

        // Listen for user clicking on EditText that shouldn't really be editable
        // This is because I want a file browser/picker to fill the text in
        linkAudio_EditText.setFocusable(false);
        linkAudio_EditText.setFocusableInTouchMode(false);
        linkOther_EditText.setFocusable(false);
        linkOther_EditText.setFocusableInTouchMode(false);

        linkAudio_EditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("file/");
                try {
                    startActivityForResult(i, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                    FullscreenActivity.myToastMessage = getResources().getString(R.string.no_filemanager);
                    ShowToast.showToast(getActivity());
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.estrongs.android.pop")));
                    }
                    catch (Exception anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.estrongs.android.pop")));
                    }
                }
            }
        });
        linkOther_EditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("file/");
                try {
                    startActivityForResult(i, 1);
                } catch (Exception e) {
                    e.printStackTrace();
                    FullscreenActivity.myToastMessage = getResources().getString(R.string.no_filemanager);
                    ShowToast.showToast(getActivity());
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.estrongs.android.pop")));
                    }
                    catch (Exception anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.estrongs.android.pop")));
                    }
                }
            }
        });

        // Set up button actions
        linkClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        linkSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the values from the page
                FullscreenActivity.mLinkYouTube = linkYouTube_EditText.getText().toString();
                FullscreenActivity.mLinkWeb = linkWeb_EditText.getText().toString();
                FullscreenActivity.mLinkAudio = linkAudio_EditText.getText().toString();
                FullscreenActivity.mLinkOther = linkOther_EditText.getText().toString();

                // Now resave the song with these new links
                EditSong.prepareSongXML();
                try {
                    EditSong.justSaveSongXML();
                    dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        linkYouTube_ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(linkYouTube_EditText.getText().toString())));
                } catch (Exception e) {
                    e.printStackTrace();
                    FullscreenActivity.myToastMessage = getResources().getString(R.string.error_notset);
                    ShowToast.showToast(getActivity());
                }
            }
        });
        linkWeb_ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(linkWeb_EditText.getText().toString())));
                } catch (Exception e) {
                    e.printStackTrace();
                    FullscreenActivity.myToastMessage = getResources().getString(R.string.error_notset);
                    ShowToast.showToast(getActivity());
                }
            }
        });
        linkAudio_ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(linkAudio_EditText.getText().toString()));
                try {
                    startActivity(i);
                } catch (Exception e) {
                    e.printStackTrace();
                    FullscreenActivity.myToastMessage = getResources().getString(R.string.no_audioplayer);
                    ShowToast.showToast(getActivity());
                }
            }
        });
        linkOther_ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(linkOther_EditText.getText().toString()));
                try {
                    startActivity(i);
                } catch (Exception e) {
                    e.printStackTrace();
                    FullscreenActivity.myToastMessage = getResources().getString(R.string.file_type_unknown);
                    ShowToast.showToast(getActivity());
                }
            }
        });

        return V;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        if (intent!=null) {
            Uri uri = intent.getData();
            if (requestCode==0) {
                // Audio
                linkAudio_EditText.setText(uri.toString());
            } else if (requestCode==1) {
                // Document
                linkOther_EditText.setText(uri.toString());
            }
        }
    }
}
