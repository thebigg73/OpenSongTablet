package com.garethevans.church.opensongtablet.importsongs;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.SongParserInterface;
import com.garethevans.church.opensongtablet.interfaces.SongProvider;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseSongProvider implements SongProvider {
    protected final Context context;
    protected final ExecutorService executor = Executors.newSingleThreadExecutor();
    protected final Handler handler;
    private final String TAG = "BaseSongProvider";

    public BaseSongProvider(Context context) {
        this.context = context;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            handler = Handler.createAsync(Looper.getMainLooper());
        } else {
            handler = new Handler(Looper.getMainLooper());
        }
    }

    protected abstract String getSearchUrl(String query) throws Exception;
    protected abstract List<SongMatch> parseSearchResults(Document doc) throws Exception;

    // Pass the context down when instantiating the parser
    protected abstract SongParserInterface getParser(Context context);

    @Override
    public void searchSongs(String query, SongProvider.OnSearchResultsListener listener) {
        executor.execute(() -> {
            // Create the list once and modify its contents (do not reassign with '=')
            final List<SongMatch> matches = new ArrayList<>();
            try {
                String url = getSearchUrl(query);
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                        .timeout(10000)
                        .get();

                // Log the title or length of the document to ensure we actually got content back
                Log.d("SongProvider", "Fetched URL: " + url + " | Doc Title: " + doc.title() + " | HTML Length: " + doc.html().length());

                List<SongMatch> results = parseSearchResults(doc);
                if (results != null) {
                    matches.addAll(results); // Modifying the list, not reassigning the variable!
                }
            } catch (Exception e) {
                Log.e(TAG, "Search error", e);
            }
            handler.post(() -> listener.onResultsLoaded(matches));
        });
    }

    @Override
    public void fetchAndParseSong(String url, Song targetSong, MainActivityInterface mainActivityInterface, OnSongParsedListener listener) {
        Log.d(TAG,"fetchAndParseSong()");
        executor.execute(() -> {
            // Use a temporary local variable for the result
            Song finalSong = targetSong;
            try {
                String html = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                        .timeout(15000)
                        .get()
                        .html();

                Log.d(TAG,"fetchAndParseSong() html returned");

                finalSong = getParser(context).processContent(mainActivityInterface, targetSong, html);
                Log.d(TAG,"fetchAndParseSong() finalSong retrieved");

            } catch (Exception e) {
                Log.e(getName(), "Fetch and parse error", e);
            }

            // finalSong is now effectively final because it isn't reassigned inside the lambda
            Song resultSong = finalSong;
            handler.post(() -> listener.onSongParsed(resultSong));
        });
    }
}