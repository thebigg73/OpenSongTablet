package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class BibleGateway extends Activity{

    public static String response = "";

    public static void grabBibleText(Context c, String weblink) {
        DownloadWebTextTask task = new DownloadWebTextTask(c);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,weblink);
    }

    private static class DownloadWebTextTask extends AsyncTask<String, Void, String> {
        @SuppressLint("StaticFieldLeak")
        Context c;

        DownloadWebTextTask (Context context) {
            c = context;
        }
        @Override
        protected String doInBackground(String... addresses) {
            response = "";
            for (String address:addresses) {
                URL url;
                HttpURLConnection urlConnection = null;
                try {
                    url = new URL(address);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream in = urlConnection.getInputStream();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
                    String s;
                    while ((s = buffer.readLine()) != null) {
                        response += "\n" + s;
/*
                        if (s.contains("<meta name=\"twitter:title\" content=\"")) {
                            gottitle=true;
                        }
                        if (s.contains("<meta property=\"og:description\" content=\"")) {
                            gotscripture=true;
                        }
*/

                        // OVERRIDE THIS BIT FOR NOW WHILE I TEST FULL SCRIPTURE EXTRACT
/*
                        if (s.contains("<meta property=\"al:ios:url\"") || (gottitle && gotscripture)) {
                            // Force s to be null as we've got all we need!
                            break;
                        }
*/
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
            return response;
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void onPostExecute(String result)  {
            String scripture;
            String scripture_title = "";

            // TEST THE FULLY EXTRACTED SCRIPTURE (FULLER THAN HEADER)
            String newbit = result;

            // Find the start and end of the scripture bit
            int startoffull = newbit.indexOf("<sup class=\"versenum\">");
            int endoffull   = newbit.indexOf("<div class=\"crossrefs hidden\">");

            if (endoffull>startoffull && startoffull>0 && endoffull>0) {
                newbit = newbit.substring(startoffull,endoffull);
            } else {
                Log.d("d","Error getting scripture");
                FullscreenActivity.myToastMessage = c.getResources().getString(R.string.error_missingsection);
                ShowToast.showToast(c);
            }

            newbit = Html.fromHtml(newbit).toString();
            newbit = newbit.replace("<p>","");
            newbit = newbit.replace("</p>","");
            //newbit = newbit.replace("\n","");

            //Now look to see if the webcontent has the desired text in it
            if (result.contains("og:description")) {
                // Find the position of the start of this section
                // Get the scripture
/*
                int script_startpos = result.indexOf("og:description\" content=\"")+25;
                int script_endpos = result.indexOf("\"/>",script_startpos);
                try {
                    scripture = result.substring(script_startpos,script_endpos);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("d","Error getting scripture");
                    FullscreenActivity.myToastMessage = FullscreenActivity.error_missingsection;
                    ShowToast.showToast(context);
                }
*/

                // Get the title
                int title_startpos = result.indexOf("<meta name=\"twitter:title\" content=\"")+36;
                int title_endpos   = result.indexOf("\" />",title_startpos);

                try {
                    scripture_title = result.substring(title_startpos,title_endpos);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("d","Error getting scripture title");
                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.error_missingsection);
                    ShowToast.showToast(c);
                }

                // Make the scripture more readable by making a line break at the start of the word after 40 chars
                // First split the scripture into an array of words
                //String[] scripturewords = scripture.split(" ");
                String[] scripturewords = newbit.split(" ");

                String currentline="";
                ArrayList<String> newimprovedscripture = new ArrayList<>();

                for (String words:scripturewords) {
                    if (currentline.length()<40) {
                        currentline = currentline + " " + words;
                    } else {
                        newimprovedscripture.add(currentline.trim());
                        currentline = words;
                    }
                }
                newimprovedscripture.add(currentline);

                scripture = "";
                int newslideneeded = 0;
                for (int z=0;z<newimprovedscripture.size();z++) {
                    scripture = scripture + "\n" + newimprovedscripture.get(z);
                    newslideneeded ++;
                    // Every 6 lines, start a new slide
                    if (newslideneeded > 5) {
                        scripture = scripture + "\n---";
                        newslideneeded = 0;
                    }
                }

                scripture = scripture.trim();

                // Send these back to the popupcustomslide creator window
                FullscreenActivity.scripture_title = scripture_title;
                FullscreenActivity.scripture_verse = scripture;

                PopUpCustomSlideFragment.addScripture();

            } else {
                FullscreenActivity.myToastMessage = c.getResources().getString(R.string.error_missingsection);
                ShowToast.showToast(c);
            }
        }
    }
}