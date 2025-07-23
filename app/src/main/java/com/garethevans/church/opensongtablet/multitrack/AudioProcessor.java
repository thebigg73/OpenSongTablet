package com.garethevans.church.opensongtablet.multitrack;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import com.garethevans.church.opensongtablet.customviews.MyMaterialTextView;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class AudioProcessor {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG="AudioDecoder";
    private final MainActivityInterface mainActivityInterface;
    private final Context c;
    private final ArrayList<String> audioFiles = new ArrayList<>();
    private final ArrayList<String> filesNeedingConversion = new ArrayList<>();
    private boolean trackInfoExists = false;
    private Uri multiTrackFolderUri = null;

    public AudioProcessor(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
    }

    public void decodeToPCM(Context c, String what, Uri inputUri, Uri outputUri, MyMaterialTextView progressText) {
        MediaExtractor extractor = new MediaExtractor();
        // Show the file we are processing
        if (progressText!=null) {
            progressText.post(() -> progressText.setText(what));
        }

        int fileSizeBytes = (int) (mainActivityInterface.getStorageAccess().getFileSizeFromUri(inputUri) * 1024f);
        String percentage;
        try {
            extractor.setDataSource(c, inputUri, null);

            // Find and select audio track
            int trackIndex = -1;
            MediaFormat format = null;
            for (int i = 0; i < extractor.getTrackCount(); i++) {
                format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("audio/")) {
                    trackIndex = i;
                    break;
                }
            }

            if (trackIndex < 0) {
                Log.d(TAG, "No audio track found in " + inputUri);
                return;
            }

            extractor.selectTrack(trackIndex);

            MediaCodec codec = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME));
            codec.configure(format, null, null, 0);
            codec.start();

            ByteBuffer[] inputBuffers = codec.getInputBuffers();
            ByteBuffer[] outputBuffers = codec.getOutputBuffers();
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

            OutputStream pcmOutputStream = mainActivityInterface.getStorageAccess().getOutputStream(outputUri);
            if (pcmOutputStream!=null) {
                boolean sawInputEOS = false;
                boolean sawOutputEOS = false;

                int bytesRead = 0;
                while (!sawOutputEOS) {
                    // Feed input
                    if (!sawInputEOS) {
                        int inputBufferIndex = codec.dequeueInputBuffer(10000);
                        if (inputBufferIndex >= 0) {
                            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                            int sampleSize = extractor.readSampleData(inputBuffer, 0);
                            if (sampleSize < 0) {
                                codec.queueInputBuffer(inputBufferIndex, 0, 0, 0,
                                        MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                                sawInputEOS = true;
                            } else {
                                long presentationTimeUs = extractor.getSampleTime();
                                codec.queueInputBuffer(inputBufferIndex, 0, sampleSize,
                                        presentationTimeUs, 0);
                                extractor.advance();
                            }
                            bytesRead += sampleSize;
                            percentage = (Math.round(((float)bytesRead/(float)fileSizeBytes)*100)) + "%";

                            if (progressText!=null) {
                                final String percentageNew = percentage;
                                progressText.post(() -> progressText.setHint(percentageNew));
                            }
                        }
                    }

                    // Drain output
                    int outputBufferIndex = codec.dequeueOutputBuffer(info, 10000);
                    if (outputBufferIndex >= 0) {
                        ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                        byte[] chunk = new byte[info.size];
                        outputBuffer.get(chunk);
                        outputBuffer.clear();

                        if (chunk.length > 0) {
                            pcmOutputStream.write(chunk);
                        }

                        codec.releaseOutputBuffer(outputBufferIndex, false);

                        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            sawOutputEOS = true;
                        }
                    }
                }

                pcmOutputStream.close();
                codec.stop();
                codec.release();
                extractor.release();

                // We can now remove the original file
                mainActivityInterface.getStorageAccess().deleteFile(inputUri);
            }

        } catch (Exception e) {
            Log.e(TAG, "Decoding failed", e);
        }
    }

    public void indexAudioFiles(Uri multiTrackFolderUri) {
        // Get a note of all of the files in the folder
        ArrayList<String> filesInMultiTrackFolder = mainActivityInterface.getStorageAccess().listFilesAtUri(multiTrackFolderUri);

        // Go through these files in this folder and note any that are audio and any that need conversion
        audioFiles.clear();
        filesNeedingConversion.clear();
        trackInfoExists = false;

        try (MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever()) {
            for (String filename : filesInMultiTrackFolder) {
                if (mainActivityInterface.getStorageAccess().isSpecificFileExtension("audio", filename)) {
                    Uri uri = getAppendedUri(multiTrackFolderUri,filename);
                    if (!filename.toLowerCase().endsWith(".wav") && uri!=null) {
                        mediaMetadataRetriever.setDataSource(c, uri);
                        String mimeType = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
                        if (mimeType.contains("audio/")) {
                            audioFiles.add(filename);
                            if (!mimeType.equals("audio/wav")) {
                                filesNeedingConversion.add(filename);
                            }
                        }
                    } else {
                        // This wav file should be accepted
                        audioFiles.add(filename);
                    }
                } else if (filename.equals(mainActivityInterface.getMultiTrackPlayer().trackInfoFilename)) {
                    trackInfoExists = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getFilesNeedingConversion() {
        return filesNeedingConversion;
    }
    public ArrayList<String> getAudioFiles() {
        return audioFiles;
    }
    public boolean getTrackInfoExists() {
        return trackInfoExists;
    }

    public Uri getAppendedUri(Uri baseUri, String appendedFilename) {
        return Uri.parse(baseUri + "%2F" + appendedFilename);
    }


    public Uri getMultiTrackSongFolderUri() {
        // This is the default (OpenSong/Multitrack/Song name/)
        multiTrackFolderUri = mainActivityInterface.getStorageAccess().getUriForItem("Multitrack",getExpectedMultiTrackFolder(),null);
        return multiTrackFolderUri;
    }
    public String getExpectedMultiTrackFolder() {
        String expectedFolder = mainActivityInterface.getSong().getFilename();
        if (expectedFolder!=null && expectedFolder.contains(".")) {
            expectedFolder = expectedFolder.substring(0,expectedFolder.lastIndexOf("."));
        }
        return expectedFolder;
    }
    public Uri getMultiTrackFolderUri() {
        return multiTrackFolderUri;
    }

    // The actual multitrack folder we are using
    public void setMultiTrackFolderUri(Uri multiTrackFolderUri) {
        this.multiTrackFolderUri = multiTrackFolderUri;
    }
}
