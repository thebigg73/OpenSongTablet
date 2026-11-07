package com.garethevans.church.opensongtablet.stickynotes;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.core.graphics.ColorUtils;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MyFloatingActionButton;
import com.garethevans.church.opensongtablet.customviews.MyMaterialEditText;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StickyNoteUtils {

    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private final ArrayList<StickyNote> stickyNotes;
    private final ArrayList<StickyPopUp> stickyPopUps;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "StickyNoteUtils";

    public StickyNoteUtils(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        stickyNotes = new ArrayList<>();
        stickyPopUps = new ArrayList<>();
    }

    // Matches the new metadata header format
    private final Pattern NEW_META_PATTERN = Pattern.compile(
            "^\\[x:\\s*(-?\\d+),\\s*y:\\s*(-?\\d+)(?:,\\s*override:\\s*([a-zA-Z0-9_]+))?,\\s*color:\\s*([1-4])\\]"
    );

    // Matches legacy position string: "SN:120x250;" anywhere in a string
    private final Pattern LEGACY_POS_PATTERN = Pattern.compile("SN:(-?\\d+)x(-?\\d+);?");

    /**
     * Parses sticky notes, automatically migrating legacy notes if needed.
     *
     * @param song        Song object
     */
    public void parseAndMigrate(Song song) {

        stickyNotes.clear();

        // If no song, just end now
        if (song == null) {
            return;
        }

        // New version (v7.0.4), sticky notes and position, override are in getNotes()
        String notes = song.getNotes()==null ? "" : song.getNotes();
        // Older versions saved overrides and v7.0.3 saved positions in user fields
        String user1 = song.getUser1()==null ? "" : song.getUser1();
        String user2 = song.getUser2()==null ? "" : song.getUser2();
        String user3 = song.getUser3()==null ? "" : song.getUser3();

        notes = notes.trim();

        // If we don't have notes, just end now
        if (notes.isEmpty()) {
            return;
        }

        // 1. Check if this is ALREADY in the new multi-note format
        int index = 0;
        if (notes.startsWith("[")) {
            String[] chunks = notes.split("\\r?\\n---\\r?\\n");
            for (String chunk : chunks) {
                chunk = chunk.trim();
                if (chunk.isEmpty()) continue;

                Matcher matcher = NEW_META_PATTERN.matcher(chunk);
                if (matcher.find()) {
                    int x = Integer.parseInt(Objects.requireNonNull(matcher.group(1)));
                    int y = Integer.parseInt(Objects.requireNonNull(matcher.group(2)));
                    String override = matcher.group(3);
                    int color = Integer.parseInt(Objects.requireNonNull(matcher.group(4)));

                    String text = chunk.substring(matcher.end()).trim();
                    stickyNotes.add(new StickyNote(index, x, y, override, color, text));
                    index ++;
                }
            }
            // End here as these notes are fine
            if (!stickyNotes.isEmpty()) {
                checkIndexes();
                return;
            }
        }

        // 2. LEGACY MIGRATION PATH
        // If we reached here, notesField is either old plain text or empty.
        // Extract old override string ("sticky_on" / "sticky_off") from user fields or notes
        String override = extractLegacyOverride(user1, user2, user3);

        // Extract old position (SN:XxY;) from notes or user fields
        int[] pos = extractLegacyPosition(user1, user2, user3);

        int screenWidth = c.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = c.getResources().getDisplayMetrics().heightPixels;
        int stickyWidth = mainActivityInterface.getPreferences().getMyPreferenceInt("stickyWidth", 400);

        int maxX = screenWidth - stickyWidth - 32;
        int maxY = screenHeight - 32;

        int x, y;
        if (pos != null) {
            x = Math.min(pos[0], maxX);
            y = Math.min(pos[1], maxY);
        } else {
            // Default position: Top Right of screen
            x = maxX;
            y = (int) ((float) mainActivityInterface.getToolbar().getActionBarHeight(mainActivityInterface.needActionBar())*1.2f);
        }

        // Remove old values stored in the user fields and save the song
        if (override!=null || pos!=null) {
            song.setUser1(cleanLegacyText(user1));
            song.setUser2(cleanLegacyText(user1));
            song.setUser3(cleanLegacyText(user3));
            mainActivityInterface.getSaveSong().updateSong(song,false);
        }

        // Create the single legacy note as a new StickyNote object (Color defaults to 1)
        stickyNotes.add(new StickyNote(0, x, y, override, 1, notes));
    }

    // --- Helper Extraction Methods ---

    private String extractLegacyOverride(String... fields) {
        for (String field : fields) {
            if (field == null) continue;
            if (field.contains("sticky_on")) return "sticky_on";
            if (field.contains("sticky_off")) return "sticky_off";
        }
        return null;
    }

    private int[] extractLegacyPosition(String... fields) {
        for (String field : fields) {
            if (field == null) continue;
            Matcher matcher = LEGACY_POS_PATTERN.matcher(field);
            if (matcher.find()) {
                try {
                    int x = Integer.parseInt(Objects.requireNonNull(matcher.group(1)));
                    int y = Integer.parseInt(Objects.requireNonNull(matcher.group(2)));
                    return new int[]{x, y};
                } catch (NumberFormatException ignored) {}
            }
        }
        return null; // Signals to use top-right default
    }

    private String cleanLegacyText(String text) {
        if (text == null) return "";
        // Strip out old position tokens and override strings if they were saved inside <notes>
        return text.replaceAll("SN:-?\\d+x-?\\d+;?", "")
                .replace("sticky_on", "")
                .replace("sticky_off", "")
                .trim();
    }

    public void updateStickyNote(StickyNote stickyNote) {
        // The sticky note will have an id (the order)
        if (stickyNotes.size()>stickyNote.getIndex()) {
            stickyNotes.set(stickyNote.getIndex(), stickyNote);
        }

        mainActivityInterface.getSong().setNotes(serializeStickyNotes());
        mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(),false);
    }

    public void prepareStickyPopUps(View viewHolder) {
        parseAndMigrate(mainActivityInterface.getSong());

        // Clear the array of stickyPopUps
        stickyPopUps.clear();

        if (!stickyNotes.isEmpty()) {
            boolean stickyAuto = mainActivityInterface.getPreferences().
                    getMyPreferenceBoolean("stickyAuto", true);

            checkIndexes();

            for (StickyNote stickyNote : stickyNotes) {
                if (stickyNote.getText()!=null && !stickyNote.getText().isEmpty()) {
                    StickyPopUp stickyPopUp = new StickyPopUp(c, stickyNote);
                    // If we want this to show, do it
                    if ((stickyAuto && !stickyNote.overrideOff()) || stickyNote.overrideOn()) {
                        stickyPopUp.floatSticky(viewHolder, false);
                    }
                    // Add it to the array
                    stickyPopUps.add(stickyPopUp);
                }
            }
        }
    }
    public ArrayList<StickyNote> getStickyNotes() {
        checkIndexes();
        return stickyNotes;
    }
    public ArrayList<StickyPopUp> getStickyPopUps() {
        return stickyPopUps;
    }

    public void destroyStickyPopUps() {
        checkIndexes();

        for (StickyPopUp stickyPopUp : stickyPopUps) {
            if (stickyPopUp.isShowing()) {
                mainActivityInterface.getMainHandler().post(stickyPopUp::closeSticky);
            }
            stickyPopUp.destroyPopup();
        }
        //stickyNotes.clear();
        stickyPopUps.clear();
    }

    public void toggleStickyPopUps(View holderView) {
        checkIndexes();

        // Go through all sticky popups and determine if any are showing
        boolean anyShowing = false;
        for (StickyPopUp stickyPopUp : stickyPopUps) {
            if (stickyPopUp.isShowing()) {
                anyShowing = true;
                break;
            }
        }

        // If no sticky popups are showing, show them all
        if (!anyShowing) {
            for (StickyPopUp stickyPopUp : stickyPopUps) {
                // Close sticky if showing
                if (!stickyPopUp.isShowing()) {
                    stickyPopUp.floatSticky(holderView, true);
                }
            }
        } else {
            // Some are showing, so hide them all
            for (StickyPopUp stickyPopUp : stickyPopUps) {
                if (stickyPopUp.isShowing()) {
                    stickyPopUp.closeSticky();
                }
            }
        }
    }

    /**
     * Serializes an entire list of StickyNote objects into a single string
     * ready to be saved into the song's <notes> XML field.
     */
    public String serializeStickyNotes() {
        if (stickyNotes.isEmpty()) {
            return "";
        }

        checkIndexes();
        checkIdenticalPlaces();

        List<String> serializedChunks = new ArrayList<>();
        for (StickyNote note : stickyNotes) {
            // Skip completely empty notes if you don't want to save blank entries
            if (note != null && (note.getText() != null && !note.getText().trim().isEmpty())) {
                serializedChunks.add(note.serialize());
            }
        }

        // Join individual sticky note blocks using your requested separator
        return String.join("\n---\n", serializedChunks);
    }

    /**
     * Helper to serialize current editor list and save back to tempSong notes.
     */
    public void syncStickyNotesToTempSong() {
        checkIndexes();
        String serialized = serializeStickyNotes();
        if (mainActivityInterface.getTempSong() != null) {
            mainActivityInterface.getTempSong().setNotes(serialized);
        }
    }

    private void checkIndexes() {
        for (int i=0; i<stickyNotes.size(); i++) {
            stickyNotes.get(i).setIndex(i);
        }
    }

    public void checkIdenticalPlaces() {
        if (stickyNotes.size() <= 1) {
            return;
        }

        int screenHeight = c.getResources().getDisplayMetrics().heightPixels;
        int offsetStep = 64; // Pixels to shift by if a collision is found
        int bottomMargin = 100; // Keep some space above the very bottom of the screen

        for (int i = 0; i < stickyNotes.size(); i++) {
            StickyNote current = stickyNotes.get(i);

            // Compare against all previous notes to check for duplicates
            boolean collisionFound;
            do {
                collisionFound = false;
                for (int j = 0; j < i; j++) {
                    StickyNote previous = stickyNotes.get(j);

                    if (current.getX() == previous.getX() && current.getY() == previous.getY()) {
                        collisionFound = true;
                        // Shift down
                        int newY = current.getY() + offsetStep;
                        // Shift left
                        int newX = current.getX() - offsetStep;

                        // Boundary check: if it goes past the screen bottom, loop it back near the top or clamp it
                        if (newY + bottomMargin > screenHeight) {
                            newY = (int) ((float) mainActivityInterface.getToolbar().getActionBarHeight(mainActivityInterface.needActionBar()) * 1.2f) + (i * 20);
                        }
                        if (newX < 0) {
                            newX = 0;
                        }

                        current.setY(newY);
                        current.setX(newX);
                        break; // Re-check against all previous notes with the new position
                    }
                }
            } while (collisionFound);
        }
    }

    public void updateOverrides(String what) {
        for (StickyNote stickyNote : stickyNotes) {
            stickyNote.setOverride(what);
        }
    }

    public boolean hasStickyOnOverride() {
        boolean stickyOn = false;
        boolean stickyOff = false;
        for (StickyNote stickyNote : stickyNotes) {
            if (stickyNote.overrideOff()) {
                stickyOff = true;
            }
            if (stickyNote.overrideOn()) {
                stickyOn = true;
            }
        }

        // If we have both (we shouldn't), set them all to override on
        if (stickyOn && stickyOff) {
            for (StickyNote stickyNote : stickyNotes) {
                stickyNote.setOverride("sticky_on");
            }
        }
        return stickyOn;
    }
    public boolean hasStickyOffOverride() {
        for (StickyNote stickyNote : stickyNotes) {
            if (stickyNote.overrideOff()) {
                return true;
            }
        }
        return false;
    }

    public void updateStickyAlphas(float val) {
        // We need to do this for each stickyBackground color
        int stickyBgColor1 = mainActivityInterface.getMyThemeColors().getStickyBackgroundColor();
        int stickyBgColor2 = mainActivityInterface.getMyThemeColors().getStickyBackgroundColor2();
        int stickyBgColor3 = mainActivityInterface.getMyThemeColors().getStickyBackgroundColor3();
        int stickyBgColor4 = mainActivityInterface.getMyThemeColors().getStickyBackgroundColor4();

        int rgb1   = stickyBgColor1 & 0x00FFFFFF;
        int rgb2   = stickyBgColor2 & 0x00FFFFFF;
        int rgb3   = stickyBgColor3 & 0x00FFFFFF;
        int rgb4   = stickyBgColor4 & 0x00FFFFFF;

        int color1 = ColorUtils.setAlphaComponent(rgb1,(int)(val*255f));
        int color2 = ColorUtils.setAlphaComponent(rgb2,(int)(val*255f));
        int color3 = ColorUtils.setAlphaComponent(rgb3,(int)(val*255f));
        int color4 = ColorUtils.setAlphaComponent(rgb4,(int)(val*255f));

        mainActivityInterface.getMyThemeColors().setStickyBackgroundColor(color1);
        mainActivityInterface.getMyThemeColors().setStickyBackgroundColor2(color2);
        mainActivityInterface.getMyThemeColors().setStickyBackgroundColor3(color3);
        mainActivityInterface.getMyThemeColors().setStickyBackgroundColor4(color4);

        String theme = mainActivityInterface.getMyThemeColors().getThemeName();
        mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"_stickyBackgroundColor",color1);
        mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"_stickyBackgroundColor2",color2);
        mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"_stickyBackgroundColor3",color3);
        mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"_stickyBackgroundColor4",color4);
    }

    // Used in both the edit song fragment and the edit sticky note fragment
    public void populateStickyNoteEditorViews(Context c, LinearLayout stickyNotesContainer) {
        mainActivityInterface.getMainHandler().post(() -> {
            if (stickyNotesContainer == null || c == null) return;

            stickyNotesContainer.removeAllViews();

            // Resolve color values
            int color1 = mainActivityInterface.getMyThemeColors().getColorOnly(
                    mainActivityInterface.getMyThemeColors().getStickyBackgroundColor());
            int color2 = mainActivityInterface.getMyThemeColors().getColorOnly(
                    mainActivityInterface.getMyThemeColors().getStickyBackgroundColor2());
            int color3 = mainActivityInterface.getMyThemeColors().getColorOnly(
                    mainActivityInterface.getMyThemeColors().getStickyBackgroundColor3());
            int color4 = mainActivityInterface.getMyThemeColors().getColorOnly(
                    mainActivityInterface.getMyThemeColors().getStickyBackgroundColor4());

            LayoutInflater inflater = LayoutInflater.from(c);

            for (int i = 0; i < stickyNotes.size(); i++) {
                final StickyNote note = stickyNotes.get(i);
                View rowView = inflater.inflate(R.layout.view_sticky_note_item, stickyNotesContainer, false);

                MyMaterialEditText editContent = rowView.findViewById(R.id.editNoteContent);
                mainActivityInterface.getProcessSong().editBoxToMultiline(editContent);
                mainActivityInterface.getProcessSong().stretchEditBoxToLines(editContent, 3);

                MyFloatingActionButton deleteButton = rowView.findViewById(R.id.deleteBtn);

                MyFloatingActionButton btn1 = rowView.findViewById(R.id.btnColor1);
                MyFloatingActionButton btn2 = rowView.findViewById(R.id.btnColor2);
                MyFloatingActionButton btn3 = rowView.findViewById(R.id.btnColor3);
                MyFloatingActionButton btn4 = rowView.findViewById(R.id.btnColor4);

                editContent.setText(note.getText());

                // Set background tint for each color button
                btn1.setFABButtonColor(color1);
                btn2.setFABButtonColor(color2);
                btn3.setFABButtonColor(color3);
                btn4.setFABButtonColor(color4);

                // Helper lambda to refresh check icons based on the selected color
                Runnable updateColorSelectionUI = () -> {
                    int selectedColor = note.getColor();
                    btn1.setImageResource(selectedColor == 1 ? R.drawable.check : 0);
                    btn2.setImageResource(selectedColor == 2 ? R.drawable.check : 0);
                    btn3.setImageResource(selectedColor == 3 ? R.drawable.check : 0);
                    btn4.setImageResource(selectedColor == 4 ? R.drawable.check : 0);

                    // Ensure check icon uses a clear dark tint for high visibility
                    int tickColor = mainActivityInterface.getMyThemeColors().getColorOnly(
                            mainActivityInterface.getMyThemeColors().getStickyTextColor());

                    btn1.setFABIconColor(tickColor);
                    btn2.setFABIconColor(tickColor);
                    btn3.setFABIconColor(tickColor);
                    btn4.setFABIconColor(tickColor);
                };

                // Initialize state
                updateColorSelectionUI.run();

                // Click listeners for color selection
                btn1.setOnClickListener(v -> {
                    note.setColor(1);
                    updateColorSelectionUI.run();
                    syncStickyNotesToTempSong();
                });
                btn2.setOnClickListener(v -> {
                    note.setColor(2);
                    updateColorSelectionUI.run();
                    syncStickyNotesToTempSong();
                });
                btn3.setOnClickListener(v -> {
                    note.setColor(3);
                    updateColorSelectionUI.run();
                    syncStickyNotesToTempSong();
                });
                btn4.setOnClickListener(v -> {
                    note.setColor(4);
                    updateColorSelectionUI.run();
                    syncStickyNotesToTempSong();
                });

                // Text change listener
                editContent.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        note.setText(s != null ? s.toString() : "");
                        syncStickyNotesToTempSong();
                    }
                });

                // Delete note listener (safely remove by index and re-render)
                deleteButton.setOnClickListener(v -> {
                    Log.d(TAG, "Current serialized: " + serializeStickyNotes());

                    Log.d(TAG, "Deleting note at index: " + note.getIndex());
                    if (note.getIndex() >= 0 && note.getIndex() < stickyNotes.size()) {
                        stickyNotes.remove(note.getIndex());
                    }
                    syncStickyNotesToTempSong();
                    populateStickyNoteEditorViews(c, stickyNotesContainer);
                });

                stickyNotesContainer.addView(rowView);
            }
        });
    }
}