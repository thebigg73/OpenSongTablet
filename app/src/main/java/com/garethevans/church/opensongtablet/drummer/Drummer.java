package com.garethevans.church.opensongtablet.drummer;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Drummer {

    // This class is to emulate a drum machine that you can play along with.
    // There are basic midi files available to begin with, but ultimately the user can create their own!
    // Also looking at using Oboe to access low latency audio



    // TODO
    // Make the main beat and the variation beat lots of bars long (no slip each bar)
    // Every half bar check for the next part
    // If requesting a fill during first half of bar, play the second half bar fill
    // If requesting a fill during the second half of bar, play the full bar fill
    // If requesting a change to variation/main during first half of bar, play the second half bar fill then change
    // If requesting a change to variation/main during second half of bar, play the full bar fill then change
    // If requesting the end in the first half of the bar, play the second half bar fill then the end crash
    // If requesting the end in the second half of the bar, play a full bar fill then the end crash

    // Have to be aware of different timings
    // 4/4 - First half 1-2, Second half 3-4
    // 3/4 - First half = Second half = 1-3
    // etc.



    // TODO make app check for Drummer folder creation on start
    // TODO copy across my basic files

    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private final String TAG = "Drummer";

    // The names of the MIDI parts
    private final String intro = "intro";
    private final String main_beat = "main_beat";
    private final String main_fill_1 = "main_fill_1";
    private final String main_fill_2 = "main_fill_2";
    private final String main_start = "main_start";
    private final String variation_beat = "variation_beat";
    private final String variation_fill_1 = "variation_fill_1";
    private final String variation_fill_2 = "variation_fill_2";
    private final String variation_start = "variation_start";

    // The uris for the parts
    private Uri introUri, mainBeatUri, mainStartUri, mainFill1Uri, mainFill2Uri,
            variationBeatUri, variationStartUri, variationFill1Uri, variationFill2Uri;

    // Each part has a mediaPlayer to reduce delays in loading parts
    private MediaPlayer introMediaPlayer, mainStartMediaPlayer, mainBeatMediaPlayer, mainFill1MediaPlayer, mainFill2MediaPlayer,
            variationStartMediaPlayer, variationBeatMediaPlayer, variationFill1MediaPlayer, variationFill2MediaPlayer;

    // Booleans to check if each part is ready
    private boolean introReady, mainStartReady = false, mainBeatReady = false, mainFill1Ready = false, mainFill2Ready = false,
            variationStartReady = false, variationBeatReady = false, variationFill1Ready = false, variationFill2Ready = false;

    // For the looping of the MediaPlayer
    int songBPM = 120;
    int beats = 4;
    String currentPart = null;
    String nextPart="intro";
    Handler mediaPlayerHandler = new Handler();
    private int loopTimer = 0;
    private long dueStartTime = 0;

    // Stuff for the MIDI file parsing
    // --- MIDI Chunk Markers ---
    private static final byte[] MTHD_MARKER = "MThd".getBytes();
    private static final byte[] MTRK_MARKER = "MTrk".getBytes();

    // Tempo and Time Signature Meta-Events
    private static final byte STATUS_META = (byte) 0xFF;
    private static final byte META_SET_TEMPO = (byte) 0x51;
    private static final byte META_TIME_SIG = (byte) 0x58;
    private static final byte TEMPO_LENGTH = (byte) 0x03;
    private static final byte TIME_SIG_LENGTH = (byte) 0x04;


    // Initialise the class and get a MainActivityInterface reference
    public Drummer(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
    }

    // This is called each time a different song or drum folder is selected
    public void setupDrums(String folder) {
        // Reset all values as we might have a new song
        resetAllValues();

        // Prepare the drumUris from the OpenSong/Drummer folder
        // The folder is the drum signature and style chosen e.g. 4_4_basic
        // This checks for validity and fixes null values
        getUserDrumUris(folder);

        // We need the main_beat.
        if (mainBeatUri!=null) {
            // Copy each part into the app specific folder, but change the tempo to match the song
            modifyDrumParts();

            // Create new mediaPlayers
            createNewMediaPlayers();

        } else {
            // If no main_beat.mid, we assume no drum files have been found
            Log.d(TAG, "No drum files found");
        }
    }

    // Whenever we start a new song, reset all values to null/empty
    private void resetAllValues() {
        // Initialise the uris
        introUri = null;
        mainBeatUri = null;
        mainStartUri = null;
        mainFill1Uri = null;
        mainFill2Uri = null;
        variationBeatUri = null;
        variationStartUri = null;
        variationFill1Uri = null;
        variationFill2Uri = null;

        // State that nothing is ready
        introReady = false;
        mainBeatReady = false;
        mainStartReady = false;
        mainFill1Ready = false;
        mainFill2Ready = false;
        variationBeatReady = false;
        variationStartReady = false;
        variationFill1Ready = false;
        variationFill2Ready = false;

        // Null the mediaPlayers
        introMediaPlayer = null;
        mainBeatMediaPlayer = null;
        mainStartMediaPlayer = null;
        mainFill1MediaPlayer = null;
        mainFill2MediaPlayer = null;
        variationBeatMediaPlayer = null;
        variationStartMediaPlayer = null;
        variationFill1MediaPlayer = null;
        variationFill2MediaPlayer = null;
    }

    // Get the user drum uris found in OpenSong/Drummer/[FOLDER]
    // We need a main_beat.mid file
    // Other accepted files are main_start, main_fill_1, main_fill_2, variation_beat, variation_start, variation_fill_1, variation_fill_2
    private void getUserDrumUris(String folder) {
        if (folder!=null && !folder.isEmpty()) {
            introUri = mainActivityInterface.getStorageAccess().getUriForItem("Drummer",folder,intro+".mid");
            mainBeatUri = mainActivityInterface.getStorageAccess().getUriForItem("Drummer",folder,main_beat+".mid");
            mainStartUri = mainActivityInterface.getStorageAccess().getUriForItem("Drummer",folder,main_start+".mid");
            mainFill1Uri = mainActivityInterface.getStorageAccess().getUriForItem("Drummer",folder,main_fill_1+".mid");
            mainFill2Uri = mainActivityInterface.getStorageAccess().getUriForItem("Drummer",folder,main_fill_2+".mid");
            variationBeatUri = mainActivityInterface.getStorageAccess().getUriForItem("Drummer",folder,variation_beat+".mid");
            variationStartUri = mainActivityInterface.getStorageAccess().getUriForItem("Drummer",folder,variation_start+".mid");
            variationFill1Uri = mainActivityInterface.getStorageAccess().getUriForItem("Drummer",folder,variation_fill_1+".mid");
            variationFill2Uri = mainActivityInterface.getStorageAccess().getUriForItem("Drummer",folder,variation_fill_2+".mid");

            if (!mainActivityInterface.getStorageAccess().uriExists(mainBeatUri)) {
                // Reset all values to null as we should not proceed
                resetAllValues();

            } else {
                // Either return a valid uri, or the mainBeatUri
                introUri = introUri==null || !mainActivityInterface.getStorageAccess().uriExists(introUri) ? mainBeatUri : introUri;
                mainStartUri = mainStartUri==null || !mainActivityInterface.getStorageAccess().uriExists(mainStartUri) ? mainBeatUri : mainStartUri;
                mainFill1Uri = mainFill1Uri==null || !mainActivityInterface.getStorageAccess().uriExists(mainFill1Uri) ? mainBeatUri : mainFill1Uri;
                mainFill2Uri = mainFill2Uri==null || !mainActivityInterface.getStorageAccess().uriExists(mainFill2Uri) ? mainBeatUri : mainFill2Uri;
                variationBeatUri = variationBeatUri==null || !mainActivityInterface.getStorageAccess().uriExists(variationBeatUri) ? mainBeatUri : variationBeatUri;
                variationFill1Uri = variationFill1Uri==null || !mainActivityInterface.getStorageAccess().uriExists(variationFill1Uri) ? mainBeatUri : variationFill1Uri;
                variationFill2Uri = variationFill2Uri==null || !mainActivityInterface.getStorageAccess().uriExists(variationFill2Uri) ? mainBeatUri : variationFill2Uri;
                variationStartUri = variationStartUri==null || !mainActivityInterface.getStorageAccess().uriExists(variationStartUri) ? mainBeatUri : variationStartUri;
            }
        }
    }

    // Edit the original MIDI files replacing the tempo and then save to the app private storage
    private void modifyDrumParts() {
        if (mainBeatUri!=null) {
            // Default 120bpm in case it isn't set in the song
            songBPM = 120;
            if (mainActivityInterface.getSong().getTempo() != null &&
                    !mainActivityInterface.getSong().getTempo().replaceAll("\\D", "").isEmpty()) {
                songBPM = Integer.parseInt(mainActivityInterface.getSong().getTempo().replaceAll("\\D", ""));
            }
            // Now make a copy of the parts with the new tempo in the app private storage
            introUri = copyModifiedDrumPart(introUri,intro);
            mainBeatUri = copyModifiedDrumPart(mainBeatUri, main_beat);
            mainStartUri = copyModifiedDrumPart(mainStartUri, main_start);
            mainFill1Uri = copyModifiedDrumPart(mainFill1Uri, main_fill_1);
            mainFill2Uri = copyModifiedDrumPart(mainFill2Uri, main_fill_2);
            variationBeatUri = copyModifiedDrumPart(variationBeatUri, variation_beat);
            variationStartUri = copyModifiedDrumPart(variationStartUri, variation_start);
            variationFill1Uri = copyModifiedDrumPart(variationFill1Uri, variation_fill_1);
            variationFill2Uri = copyModifiedDrumPart(variationFill2Uri, variation_fill_2);
        }
    }
    // Return the newUri to replace the original one (original file is untouched)
    private Uri copyModifiedDrumPart(Uri inputUri, String fileName) {
        if (inputUri!=null) {
            try {
                // If the process is successful, we return the newUri, otherwise the original inputUri
                return changeTempo(inputUri,fileName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return null;
        }
        return null;
    }

    // This will be used to adjust the tempo of MIDI files
    public Uri changeTempo(Uri inputUri, String fileName) throws IOException {
        // Use a buffered approach to read all bytes from the InputStream
        InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(inputUri);
        File outputFile = mainActivityInterface.getStorageAccess().getAppSpecificFile("Midi", "", fileName+".mid");

        byte[] fileBytes = null;
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            int nRead;
            byte[] data = new byte[4096]; // 4KB buffer
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            fileBytes = buffer.toByteArray();
            if (fileBytes.length < 14) {
                // Insufficient data for a valid MIDI header
                fileBytes = null;
            }
            inputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (fileBytes != null) {

            // --- 1. HEADER CHUNK PARSING ---
            if (!Arrays.equals(Arrays.copyOfRange(fileBytes, 0, 4), MTHD_MARKER)) {
                // MIDI file header marker 'MThd' not found.
                // Return the unchanged inputUri
                return inputUri;
            }

            // Read Time Division (Ticks Per Quarter Note, TPQN) at bytes 12 & 13
            int ticksPerQuarterNote = ((fileBytes[12] & 0xFF) << 8) | (fileBytes[13] & 0xFF);
            if ((fileBytes[12] & 0x80) != 0) {
                Log.d(TAG, "SMPTE time division format is not supported for clipping");
                // Return the unchanged inputUri
                return inputUri;
            }

            // --- 2. TEMPO & TIME SIGNATURE IDENTIFICATION AND MODIFICATION (Track 1) ---
            int trackDataStart = 0;
            int trackDataLength = 0;
            int timeSigNumerator = 4; // Default to 4 (4/4 time)
            int track1End = -1; // End position of MTrk 1 + its 8-byte header

            // Find the start of the FIRST MTrk chunk data (Track 1)
            for (int i = 14; i < fileBytes.length - 8; i++) {
                if (Arrays.equals(Arrays.copyOfRange(fileBytes, i, i + 4), MTRK_MARKER)) {
                    // Found MTrk 1. Next 4 bytes are the length.
                    trackDataLength = ((fileBytes[i + 4] & 0xFF) << 24) |
                            ((fileBytes[i + 5] & 0xFF) << 16) |
                            ((fileBytes[i + 6] & 0xFF) << 8) |
                            (fileBytes[i + 7] & 0xFF);
                    trackDataStart = i + 8; // Start of the actual MIDI events
                    track1End = i + 8 + trackDataLength; // End of Track 1 (MTrk + Length + Data)
                    break;
                }
            }

            if (trackDataStart == 0) {
                Log.d(TAG, "MTrk marker not found in the file");
                // Return the unchanged inputUri
                return inputUri;
            }

            // A. TEMPO CHANGE (In-place modification on Track 1, from trackDataStart up to track1End)
            int newUsPerQn = 60_000_000 / songBPM;
            byte t1 = (byte) ((newUsPerQn >> 16) & 0xFF);
            byte t2 = (byte) ((newUsPerQn >> 8) & 0xFF);
            byte t3 = (byte) (newUsPerQn & 0xFF);
            boolean tempoChanged = false;

            for (int i = trackDataStart; i < track1End - 5; i++) {
                // Search for FF 51 03 (Set Tempo Meta-Event)
                if (fileBytes[i] == STATUS_META &&
                        fileBytes[i + 1] == META_SET_TEMPO &&
                        fileBytes[i + 2] == TEMPO_LENGTH) {

                    fileBytes[i + 3] = t1;
                    fileBytes[i + 4] = t2;
                    fileBytes[i + 5] = t3;
                    Log.d(TAG, "Tempo successfully changed to " + songBPM + " BPM at offset " + (i + 3));
                    tempoChanged = true;
                }

                // B. TIME SIGNATURE IDENTIFICATION (Only need the numerator)
                if (fileBytes[i] == STATUS_META &&
                        fileBytes[i + 1] == META_TIME_SIG &&
                        fileBytes[i + 2] == TIME_SIG_LENGTH) {

                    timeSigNumerator = fileBytes[i + 3] & 0xFF;
                    beats = timeSigNumerator;
                    int denominatorLog = fileBytes[i + 4] & 0xFF;
                    int denominator = 1 << denominatorLog;

                    Log.d(TAG, "Time Signature found: " + timeSigNumerator + "/" + denominator);
                }
            }

            if (!tempoChanged) {
                Log.d(TAG, "Set Tempo Meta-Event (FF 51 03) not found. Tempo not changed.");
            }

            // --- 3. CLIPPING TRACK DATA TO 1 BAR (Track 2) ---
            // Calculate the maximum tick value (exclusive) for 1 full bar
            final int BAR_TICKS_LIMIT = ticksPerQuarterNote * timeSigNumerator;

            // Find the start of the SECOND MTrk chunk (Track 2)
            int track2Start = -1;
            for (int i = track1End; i < fileBytes.length - 8; i++) {
                if (Arrays.equals(Arrays.copyOfRange(fileBytes, i, i + 4), MTRK_MARKER)) {
                    // Found MTrk 2. Next 4 bytes are the length.
                    trackDataLength = ((fileBytes[i + 4] & 0xFF) << 24) |
                            ((fileBytes[i + 5] & 0xFF) << 16) |
                            ((fileBytes[i + 6] & 0xFF) << 8) |
                            (fileBytes[i + 7] & 0xFF);
                    trackDataStart = i + 8; // Start of Track 2 events
                    track2Start = i; // Store the MTrk 2 marker start position
                    break;
                }
            }

            if (track2Start == -1) {
                Log.d(TAG, "MTrk 2 marker not found. Cannot clip music data.");
                // Return the unchanged inputUri
                return inputUri;
            }

            int currentOffset = trackDataStart;
            int absoluteTime = 0; // Absolute time of the *last successfully processed* event
            byte runningStatus = 0; // The last MIDI status byte seen

            List<Byte> newTrackEvents = new ArrayList<>();
            boolean clipPerformed = false; // Flag to check if the clipping logic was executed

            while (currentOffset < trackDataStart + trackDataLength) {

                int[] offsetRef = {currentOffset};
                int deltaTime = 0;

                try {
                    deltaTime = readVLQ(fileBytes, offsetRef);
                } catch (IOException e) {
                    Log.d(TAG, "Premature end of track data at offset 0x" + String.format("%06X", currentOffset));
                    break;
                }
                currentOffset = offsetRef[0]; // currentOffset is now the start of the event bytes

                int nextAbsoluteTime = absoluteTime + deltaTime;

                // --- CLIPPING LOGIC ---
                // If the next event starts AT or AFTER the bar boundary, we must clip.
                if (nextAbsoluteTime >= BAR_TICKS_LIMIT) {

                    // --- WORKAROUND FOR GAPLESS LOOPING (Delay EOT by 1 tick) ---
                    clipPerformed = true; // Clipping was performed

                    // 1. Calculate the delta time needed to reach the BAR_TICKS_LIMIT exactly
                    int silenceDelta = BAR_TICKS_LIMIT - absoluteTime;

                    // 2. Add 1 extra tick to the delta time. This places the EOT one tick past the loop point.
                    // This prevents the sequencer from hitting EOT too early and pausing.
                    int deltaToEOT = silenceDelta + 1;

                    // Add the End-of-Track event (FF 2F 00)
                    writeVLQ(deltaToEOT, newTrackEvents); // Delta Time is now 1 tick past the boundary
                    newTrackEvents.add(STATUS_META);
                    newTrackEvents.add((byte) 0x2F);
                    newTrackEvents.add((byte) 0x00);

                    break; // Stop processing further events
                }

                // If we are here, the event is valid and starts before the clip limit.
                absoluteTime = nextAbsoluteTime;

                // Write the valid delta time (VLQ encoded)
                writeVLQ(deltaTime, newTrackEvents);

                // Read Status/Event bytes
                byte statusByte = fileBytes[currentOffset];
                boolean statusBytePresent = false;

                if ((statusByte & 0x80) != 0) {
                    // It's a full status byte (not Running Status)
                    runningStatus = statusByte;
                    currentOffset++; // Move past the status byte to the first data byte
                    statusBytePresent = true;
                }
                // currentOffset now points to the first data byte (or the first meta/sysex byte)

                int effectiveStatus = runningStatus & 0xFF;

                if (effectiveStatus == 0xFF) { // Meta-event - (FF)
                    byte metaType = fileBytes[currentOffset];
                    byte metaLength = fileBytes[currentOffset + 1];
                    int totalMetaLength = metaLength + 2; // + Type + Length bytes

                    // Write the FF, Type, Length
                    newTrackEvents.add(STATUS_META);
                    newTrackEvents.add(metaType);
                    newTrackEvents.add(metaLength);

                    // Write the Data bytes. Start 2 bytes after currentOffset (skipping type and length).
                    for (int j = 0; j < metaLength; j++) {
                        newTrackEvents.add(fileBytes[currentOffset + 2 + j]);
                    }

                    currentOffset += totalMetaLength; // Advance past Type, Length, and Data


                } else if (effectiveStatus == 0xF0) { // System Exclusive - F0
                    // currentOffset points to the first data byte after the F0 status

                    newTrackEvents.add((byte) 0xF0);

                    int sysexStart = currentOffset; // Start of payload (first byte after F0)
                    int sysexEnd = -1;

                    // Search for the end marker (0xF7)
                    // We must clip the search to the original track length to avoid OOB
                    for (int k = sysexStart; k < trackDataStart + trackDataLength; k++) {
                        if (fileBytes[k] == (byte) 0xF7) {
                            sysexEnd = k;
                            break;
                        }
                    }

                    if (sysexEnd != -1) {
                        for (int k = sysexStart; k <= sysexEnd; k++) {
                            newTrackEvents.add(fileBytes[k]);
                        }
                        currentOffset = sysexEnd + 1; // Advance past F7
                        runningStatus = 0; // Reset running status after Sysex
                        // System.out.println("[SYSEX] End F7 found.");
                    } else {
                        Log.d(TAG, "Error: Unterminated System Exclusive message found at tick: " + absoluteTime);
                        break;
                    }

                } else if (effectiveStatus >= 0xF1 && effectiveStatus <= 0xF7) { // System Common Messages (F1-F7)

                    // System.out.printf("[SYSTEM] Status: 0x%02X\n", effectiveStatus);
                    newTrackEvents.add(runningStatus); // F1-F7 must be written explicitly

                    int messageLength = 0;
                    if (effectiveStatus == 0xF2) {
                        messageLength = 2; // Song Position Pointer (2 data bytes)
                    } else if (effectiveStatus == 0xF1 || effectiveStatus == 0xF3) {
                        messageLength = 1; // Time Code Quarter Frame, Song Select (1 data byte)
                    }

                    for (int j = 0; j < messageLength; j++) {
                        newTrackEvents.add(fileBytes[currentOffset + j]);
                    }
                    currentOffset += messageLength; // Advance past data bytes
                    runningStatus = 0; // Reset running status after System Common messages

                } else if (effectiveStatus >= 0x80 && effectiveStatus <= 0xEF) { // Voice Message (8x to Ex)

                    int command = effectiveStatus & 0xF0;

                    // Status Byte is written ONLY if it was explicitly present
                    if (statusBytePresent) {
                        newTrackEvents.add(statusByte);
                    }

                    // Determine length of the message
                    int messageLength;
                    // C0-DF (Program Change, Channel Pressure) have 1 data byte
                    if (command == 0xC0 || command == 0xD0) {
                        messageLength = 1;
                    } else {
                        // 8x, 9x, Ax, Bx, Ex have 2 data bytes
                        messageLength = 2;
                    }

                    // Write the data bytes
                    for (int j = 0; j < messageLength; j++) {
                        newTrackEvents.add(fileBytes[currentOffset + j]);
                    }
                    currentOffset += messageLength; // Advance past data bytes
                } else {
                    Log.d(TAG, "Error: Unhandled or misaligned byte found at tick: " + absoluteTime + ", byte: 0x" + String.format("%02X", fileBytes[currentOffset] & 0xFF));
                    currentOffset++;
                    runningStatus = 0; // Clear running status to attempt resync
                }
            }

            // --- 4. DIAGNOSTICS CHECK ---
            if (!clipPerformed) {
                // If the track completed naturally, we still need an EOT marker.
                // Check if the last event was already EOT (FF 2F 00). If not, append it.
                if (newTrackEvents.size() < 3 ||
                        newTrackEvents.get(newTrackEvents.size() - 3) != STATUS_META ||
                        newTrackEvents.get(newTrackEvents.size() - 2) != (byte) 0x2F ||
                        newTrackEvents.get(newTrackEvents.size() - 1) != (byte) 0x00) {

                    writeVLQ(0, newTrackEvents); // Delta Time 0
                    newTrackEvents.add(STATUS_META);
                    newTrackEvents.add((byte) 0x2F);
                    newTrackEvents.add((byte) 0x00);
                }
            }

            // --- 5. REASSEMBLE THE MIDI FILE ---
            // track2Start is the position of the MTrk marker for Track 2
            int headerAndTrack1End = track2Start;

            int newTrackDataLength = newTrackEvents.size();

            // Construct the new track length bytes (4 bytes, big-endian)
            byte[] newLengthBytes = new byte[4];
            newLengthBytes[0] = (byte) ((newTrackDataLength >> 24) & 0xFF);
            newLengthBytes[1] = (byte) ((newTrackDataLength >> 16) & 0xFF);
            newLengthBytes[2] = (byte) ((newTrackDataLength >> 8) & 0xFF);
            newLengthBytes[3] = (byte) (newTrackDataLength & 0xFF);

            // Create the final byte array: [Header] [MTrk 1] [MTrk 2 Marker] [New Length] [New Clipped Data]
            ByteArrayOutputStream finalStream = new ByteArrayOutputStream();

            // 1. Write Header Chunk and Track 1 (the metadata track)
            finalStream.write(fileBytes, 0, headerAndTrack1End);

            // 2. Write MTrk Marker for the clipped track (MTrk 2)
            finalStream.write(MTRK_MARKER);

            // 3. Write New Length
            finalStream.write(newLengthBytes);

            // 4. Write New Clipped Track Data
            for (byte b : newTrackEvents) {
                finalStream.write(b);
            }

            // Write the final result to the output file
            Uri outputUri = Uri.fromFile(outputFile);
            mainActivityInterface.getStorageAccess().getOutputStream(outputUri);

            FileOutputStream fos = new FileOutputStream(outputFile);
            fos.write(finalStream.toByteArray());
            fos.close();

            Log.d(TAG, "Clipped MIDI file successfully written to: " + outputFile.getAbsolutePath());
            return Uri.fromFile(outputFile);
        }
        return inputUri;
    }
    private int readVLQ(byte[] bytes, int[] offset) throws IOException {
        int value = 0;
        int currentOffset = offset[0];

        for (int i = 0; i < 4; i++) {
            if (currentOffset >= bytes.length) {
                throw new EOFException("Unexpected end of file while reading VLQ.");
            }
            byte b = bytes[currentOffset++];
            // Accumulate the lower 7 bits of the byte
            value = (value << 7) | (b & 0x7F);

            // If the MSB (most significant bit, 0x80) is 0, this is the last byte of the VLQ.
            if ((b & 0x80) == 0) {
                offset[0] = currentOffset; // Update the external offset reference
                return value;
            }
        }
        throw new IOException("VLQ exceeds 4 bytes (unexpected format).");
    }
    private void writeVLQ(int value, List<Byte> outputList) {
        if (value < 0) throw new IllegalArgumentException("VLQ value cannot be negative.");

        // If the value is small, it's just one byte
        if (value == 0) {
            outputList.add((byte) 0x00);
            return;
        }

        // Buffer to hold the VLQ bytes (up to 4 bytes max for a 32-bit int)
        byte[] buffer = new byte[4];
        int bufferIndex = 0;

        do {
            int currentByte = value & 0x7F; // Get the lowest 7 bits
            value >>>= 7;

            // Set the continuation bit (0x80) on all but the last byte
            if (bufferIndex > 0) {
                currentByte |= 0x80;
            }
            buffer[bufferIndex++] = (byte) currentByte;
        } while (value > 0);

        // Write the bytes in the correct order (big-endian/most significant first)
        for (int i = bufferIndex - 1; i >= 0; i--) {
            outputList.add(buffer[i]);
        }
    }

    // Create new mediaPlayers ready for the content
    private void createNewMediaPlayers() {
        // Initialise the media players
        introMediaPlayer = new MediaPlayer();
        mainStartMediaPlayer = new MediaPlayer();
        mainBeatMediaPlayer = new MediaPlayer();
        mainFill1MediaPlayer = new MediaPlayer();
        mainFill2MediaPlayer = new MediaPlayer();
        variationStartMediaPlayer = new MediaPlayer();
        variationBeatMediaPlayer = new MediaPlayer();
        variationFill1MediaPlayer = new MediaPlayer();
        variationFill2MediaPlayer = new MediaPlayer();

        // Set the looping to manual, so we can force a restarting after time
        // IMPORTANT: setLooping MUST be false for manual control
        // I don't use the built in loop as hanging sounds delays the loop
        introMediaPlayer.setLooping(false);
        mainBeatMediaPlayer.setLooping(false);
        mainStartMediaPlayer.setLooping(false);
        mainFill1MediaPlayer.setLooping(false);
        mainFill2MediaPlayer.setLooping(false);
        variationBeatMediaPlayer.setLooping(false);
        variationStartMediaPlayer.setLooping(false);
        variationFill1MediaPlayer.setLooping(false);
        variationFill2MediaPlayer.setLooping(false);

        // Set a listener to show that each beat is ready
        introMediaPlayer.setOnPreparedListener(mediaPlayer -> introReady = true);
        mainBeatMediaPlayer.setOnPreparedListener(mediaPlayer -> mainBeatReady = true);
        mainStartMediaPlayer.setOnPreparedListener(mediaPlayer -> mainStartReady = true);
        mainFill1MediaPlayer.setOnPreparedListener(mediaPlayer -> mainFill1Ready = true);
        mainFill2MediaPlayer.setOnPreparedListener(mediaPlayer -> mainFill2Ready = true);
        variationBeatMediaPlayer.setOnPreparedListener(mediaPlayer -> variationBeatReady = true);
        variationStartMediaPlayer.setOnPreparedListener(mediaPlayer -> variationStartReady = true);
        variationFill1MediaPlayer.setOnPreparedListener(mediaPlayer -> variationFill1Ready = true);
        variationFill2MediaPlayer.setOnPreparedListener(mediaPlayer -> variationFill2Ready = true);

        // Set the source and prepareAsync each media player
        initialiseMediaPlayer(introUri, introMediaPlayer);
        initialiseMediaPlayer(mainBeatUri, mainBeatMediaPlayer);
        initialiseMediaPlayer(mainStartUri, mainStartMediaPlayer);
        initialiseMediaPlayer(mainFill1Uri, mainFill1MediaPlayer);
        initialiseMediaPlayer(mainFill2Uri, mainFill2MediaPlayer);
        initialiseMediaPlayer(variationBeatUri, variationBeatMediaPlayer);
        initialiseMediaPlayer(variationStartUri, variationStartMediaPlayer);
        initialiseMediaPlayer(variationFill1Uri, variationFill1MediaPlayer);
        initialiseMediaPlayer(variationFill2Uri, variationFill2MediaPlayer);
    }
    // Set the individual mediaPlayers source and prepareAsync
    private void initialiseMediaPlayer(Uri uri, MediaPlayer mediaPlayer) {
        if (uri!=null && mediaPlayer!=null) {
            try {
                mediaPlayer.setDataSource(c, uri);
                mediaPlayer.prepareAsync();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Check all of the mediaPlayers are ready
    private boolean checkReady() {
        return introReady && mainStartReady && mainBeatReady && mainFill1Ready && mainFill2Ready &&
                variationStartReady && variationBeatReady && variationFill1Ready && variationFill2Ready;
    }

    public void startDrummer() {
        Log.d(TAG,"checkReady():"+checkReady());
        if (checkReady()) {
            mediaPlayerHandler.removeCallbacks(loopRunnable);
            // Start with the intro beat, then play the start beat, then the normal beat
            currentPart = intro;
            nextPart = main_start;
            startLooping();
        }
    }

    public void startLooping() {
        try {
            // START THE METRONOME
            //mainActivityInterface.getMetronome().stopMetronome();
            //mainActivityInterface.getMetronome().startMetronome();

            Log.d(TAG,"start this part:"+currentPart+"  introUri:"+introUri);
            loopTimer = getLoopDuration();
            dueStartTime = System.currentTimeMillis() + loopTimer;

            // 2. Start Playback
            switch (currentPart) {
                case intro:
                    introMediaPlayer.start();
                    break;
                case main_beat:
                    mainBeatMediaPlayer.start();
                    break;
                case main_start:
                    mainStartMediaPlayer.start();
                    break;
                case main_fill_1:
                    mainFill1MediaPlayer.start();
                    break;
                case main_fill_2:
                    mainFill2MediaPlayer.start();
                    break;
                case variation_start:
                    variationStartMediaPlayer.start();
                    break;
                case variation_beat:
                    variationBeatMediaPlayer.start();
                    break;
                case variation_fill_1:
                    variationFill1MediaPlayer.start();
                    break;
                case variation_fill_2:
                    variationFill2MediaPlayer.start();
                    break;
            }

            Log.d("MIDI_PLAYER", "Playback started.");

            // 3. Schedule the first loop operation.
            // Start the handler thread which will repeatedly seek the player.
            if (loopTimer == 0) {
                loopTimer = getLoopDuration();
            }
            dueStartTime = System.currentTimeMillis() + loopTimer;
            mediaPlayerHandler.postDelayed(loopRunnable, loopTimer);

        } catch (Exception e) {
            Log.e("MIDI_PLAYER", "Error starting media player", e);
        }
    }


    // Runnable that executes the seek operation for looping
    private final Runnable loopRunnable = new Runnable() {
        @Override
        public void run() {
            // Because there may have been a timing glitch, move the seek position to account for the different
            long currentTime = System.currentTimeMillis();
            Log.d(TAG,"currentTime:"+currentTime+"  dueStartTime:"+dueStartTime);
            int slipTime = 0;
            if (currentTime>dueStartTime) {
                slipTime = (int)(currentTime - dueStartTime);
            }
            Log.d(TAG,"runnable currentPart:"+currentPart+"  nextPart:"+nextPart+"  slipTime:"+slipTime);
            // If we are continuing with the same part, just reset the seek time to 0ms
            if (currentPart!=null && currentPart.equals(nextPart)) {
                // Just start again at time 0
                switch (currentPart) {
                    case intro:
                        introMediaPlayer.seekTo(0);
                        nextPart = main_start;
                        break;
                    case main_beat:
                        mainBeatMediaPlayer.seekTo(0);
                        nextPart = main_beat;
                        break;
                    case main_start:
                        mainStartMediaPlayer.seekTo(0);
                        nextPart = main_beat;
                        break;
                    case main_fill_1:
                        mainFill1MediaPlayer.seekTo(0);
                        nextPart = main_start;
                        break;
                    case main_fill_2:
                        mainFill2MediaPlayer.seekTo(0);
                        nextPart = main_start;
                        break;
                    case variation_start:
                        variationStartMediaPlayer.seekTo(0);
                        nextPart = variation_beat;
                        break;
                    case variation_beat:
                        variationBeatMediaPlayer.seekTo(0);
                        nextPart = variation_beat;
                        break;
                    case variation_fill_1:
                        variationFill1MediaPlayer.seekTo(0);
                        nextPart = variation_start;
                        break;
                    case variation_fill_2:
                        variationFill2MediaPlayer.seekTo(0);
                        nextPart = variation_start;
                        break;
                }

            } else {
                final String whatWasPlaying = currentPart;

                // Start the next part (we will stop the current part as soon as possible afterwards
                switch (nextPart) {
                    case intro:
                        startThisMediaPlayer(introMediaPlayer);
                        currentPart = intro;
                        nextPart = main_start;
                        break;
                    case main_beat:
                        startThisMediaPlayer(mainBeatMediaPlayer);
                        currentPart = main_beat;
                        nextPart = main_beat;
                        break;
                    case main_start:
                        startThisMediaPlayer(mainStartMediaPlayer);
                        currentPart = main_start;
                        nextPart = main_beat;
                        break;
                    case main_fill_1:
                        startThisMediaPlayer(mainFill1MediaPlayer);
                        currentPart = main_fill_1;
                        nextPart = main_start;
                        break;
                    case main_fill_2:
                        startThisMediaPlayer(mainFill2MediaPlayer);
                        currentPart = main_fill_2;
                        nextPart = variation_start;
                        break;
                    case variation_start:
                        startThisMediaPlayer(variationStartMediaPlayer);
                        currentPart = variation_start;
                        nextPart = variation_beat;
                        break;
                    case variation_beat:
                        startThisMediaPlayer(variationBeatMediaPlayer);
                        currentPart = variation_beat;
                        nextPart = variation_beat;
                        break;
                    case variation_fill_1:
                        startThisMediaPlayer(variationFill1MediaPlayer);
                        currentPart = variation_fill_1;
                        nextPart = variation_start;
                        break;
                    case variation_fill_2:
                        startThisMediaPlayer(variationFill2MediaPlayer);
                        currentPart = variation_fill_2;
                        nextPart = main_start;
                        break;
                }


                // Now stop the bit that was playing
                if (whatWasPlaying!=null) {
                    // The next part is also set
                    switch (whatWasPlaying) {
                        case intro:
                            stopThisMediaPlayer(introMediaPlayer);
                            break;
                        case main_start:
                            stopThisMediaPlayer(mainStartMediaPlayer);
                            break;
                        case main_beat:
                            stopThisMediaPlayer(mainBeatMediaPlayer);
                            break;
                        case main_fill_1:
                            stopThisMediaPlayer(mainFill1MediaPlayer);
                            break;
                        case main_fill_2:
                            stopThisMediaPlayer(mainFill2MediaPlayer);
                            break;
                        case variation_start:
                            stopThisMediaPlayer(variationStartMediaPlayer);
                            break;
                        case variation_beat:
                            stopThisMediaPlayer(variationBeatMediaPlayer);
                            break;
                        case variation_fill_1:
                            stopThisMediaPlayer(variationFill1MediaPlayer);
                            break;
                        case variation_fill_2:
                            stopThisMediaPlayer(variationFill2MediaPlayer);
                            break;
                    }
                }

            }

            Log.d(TAG,"end of runnable currentPart:"+currentPart+"  nextPart:"+nextPart);

            // Reschedule the same Runnable to run after the loop duration
            loopTimer = getLoopDuration();
            dueStartTime = currentTime + loopTimer - slipTime;
            mediaPlayerHandler.postDelayed(this, loopTimer-slipTime);
        }
    };

    private void stopThisMediaPlayer(MediaPlayer mediaPlayer) {
        if (mediaPlayer!=null && mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(0);
            mediaPlayer.pause();
        }
    }

    private void startThisMediaPlayer(MediaPlayer mediaPlayer) {
        if (mediaPlayer!=null) {
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
        }
    }

    // Stop the drums after the current bar
    public void stopAll() {
        // Stop the metronome first
        //mainActivityInterface.getMetronome().stopMetronome();

        mediaPlayerHandler.removeCallbacks(loopRunnable);
        stopMediaPlayer(mainBeatMediaPlayer);
        stopMediaPlayer(mainStartMediaPlayer);
        stopMediaPlayer(mainFill1MediaPlayer);
        stopMediaPlayer(mainFill2MediaPlayer);
        stopMediaPlayer(variationBeatMediaPlayer);
        stopMediaPlayer(variationStartMediaPlayer);
        stopMediaPlayer(variationFill1MediaPlayer);
        stopMediaPlayer(variationFill2MediaPlayer);
    }

    private void stopMediaPlayer(MediaPlayer mp) {
        if (mp!=null && mp.isPlaying()) {
            mp.setLooping(false);
        }
    }


    private int getLoopDuration() {
        return (60000/songBPM) * beats;
    }


    public void setNextPart(String nextPart) {
        this.nextPart = nextPart;
    }

    // Called when the user exits the app
    public void endDrummer() {
        stopAll();
        resetAllValues();
    }



    // Old stuff
    //private MediaPlayer part1, part1Fill1, part1Fill2, part1Fill3, part2, part2Fill1, part2Fill2, part2Fill3;

    //private String bpm_string, bpm_hex, timesig_string, timesig_hex;
    //private int bpm_int;

    // Drum voices recognised
    /*private final int bass_drum = 35;
    private final int rim_shot = 37;
    private final int snare_drum = 38;
    private final int low_floor_tom = 41;
    private final int high_floor_tom = 43;
    private final int low_tom = 45;
    private final int low_mid_tom = 47;
    private final int high_mid_tom = 48;
    private final int high_tom = 50;
    private final int hat_closed = 42;
    private final int hat_pedal = 44;
    private final int hat_open = 46;
    private final int crash_1 = 49;
    private final int crash_2 = 57;
    private final int ride_1 = 51;
    private final int ride_2 = 59;
    private final int ride_bell = 53;
    private final int splash = 55;*/

    // Drum volumes based on dynamics
    /*private final int level_ppp = 16;
    private final int level_pp = 33;
    private final int level_p = 49;
    private final int level_mp = 64;
    private final int level_mf = 80;
    private final int level_f = 96;
    private final int level_ff = 112;
    private final int level_fff = 127;

    private final String note_on = "99 ";
    private final int beat_4 = 32;
    private final int half_4 = 16;
    private final int quarter_4 = 8;
    private final int eigth_4 = 4;
    private final int sixteenth_4 = 2;
*/


    /*
    1  -  2  -  3  -  4  - ..
    0 16 16 16 16 16 16 16 16

    B           B
          S           S
    H  H  H  H  H  H  H  H
     */
    /*private String simple_1_4_4() {
        // Make a simple midi file
        String bass = note_on + intToHex(bass_drum) + intToHex(level_ff);
        String snare = note_on + intToHex(snare_drum) + intToHex(level_ff);
        String hatff = note_on + intToHex(hat_closed) + intToHex(level_ff);
        String hatf = note_on + intToHex(hat_closed) + intToHex(level_f);

        String events = bpm_hex + timesig_hex +
                intToHex(0) + bass + intToHex(0) + hatff +
                intToHex(beat_4*2) + hatf +
                intToHex(beat_4*2) + snare + intToHex(0) + hatf +
                intToHex(beat_4*2) + hatf +
                intToHex(beat_4*2) + bass + intToHex(0) + hatff +
                intToHex(beat_4*2) + hatf +
                intToHex(beat_4*2) + snare + intToHex(0) + hatff +
                intToHex(beat_4*2) + hatf +
                intToHex(beat_4*2) + mainActivityInterface.getMidi().getAllOff() +
                mainActivityInterface.getMidi().getMidiFileTrackOut();

        int size = events.split(" ").length;

        return mainActivityInterface.getMidi().getMidiFileHeader() +
                mainActivityInterface.getMidi().getMidiFileTrackHeader() +
                intToHex(size) + events;
    }
*/



    /*public void setupSongValues() {
        // Set the tempo
        setTempo();
        // Set the time signature
        setTimeSig();

        String drums = simple_1_4_4();
        File drumFile = mainActivityInterface.getStorageAccess().getAppSpecificFile("Midi","","simple_1_4_4.mid");
        //File drumFile = new File(c.getExternalFilesDir("Midi"),"simple_1_4_4.mid");
        try (FileOutputStream fileOutputStream = new FileOutputStream(drumFile,false)) {
            fileOutputStream.write(mainActivityInterface.getMidi().returnBytesFromHexText(drums));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (part1!=null) {
            part1.release();
            part1 = null;
        }
        part1 = new MediaPlayer();
        part1.setLooping(true);
        part1.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                part1.start();
            }
        });
        Uri uri = Uri.parse(drumFile.getPath());
        try {
            part1.setDataSource(c, uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
        part1.prepareAsync();

        Log.d(TAG,"simple_1_4_4:"+simple_1_4_4());
    }*/

    /*private void setTempo() {
        bpm_string = mainActivityInterface.getSong().getTempo();
        if (bpm_string!=null && !bpm_string.isEmpty()) {
            bpm_int = Integer.parseInt(bpm_string);
        } else {
            bpm_string = "120";
            bpm_int = 120;
        }
        bpm_hex = mainActivityInterface.getMidi().getTempoByteString(bpm_int);

        Log.d(TAG,"bpm_string:"+bpm_string+"  bpm_int:"+bpm_int+"  bpm_hex:"+bpm_hex);
    }*/

    /*private void setTimeSig() {
        timesig_string = mainActivityInterface.getMetronome().fixInvalidTimeSignature(mainActivityInterface.getSong().getTimesig(),false);
        if (timesig_string==null || timesig_string.isEmpty()) {
            // Assume 4/4
            timesig_string = "4/4";
        }
        timesig_hex = mainActivityInterface.getMidi().getTimeSigByteString(timesig_string);
        Log.d(TAG,"timesig_string:"+timesig_string+"  timesig_hex:"+timesig_hex);
    }

    private String intToHex(int value) {
        return String.format("%02X",value) + " ";
    }


    */
}
