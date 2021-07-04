package com.garethevans.church.opensongtablet.songsandsetsmenu;

public class CreateNewSet {

/*
    boolean doCreation(Context c, MainActivityInterface mainActivityInterface,
                       Song thisSong) {

        StringBuilder sb = new StringBuilder();

        // Only do this if the current set isn't empty
        if (mainActivityInterface.getCurrentSet().getCurrentSet() != null &&
                mainActivityInterface.getCurrentSet().getCurrentSet().size() > 0) {
            // Check all arrays are the same size!!
            mainActivityInterface.getSetActions().checkArraysMatch(c, mainActivityInterface);
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n").
                    append("<set name=\"").
                    append(mainActivityInterface.getProcessSong().parseToHTMLEntities(mainActivityInterface.getCurrentSet().getSetName())).
                    append("\">\n<slide_groups>\n");

            for (int x = 0; x < mainActivityInterface.getCurrentSet().getCurrentSet().size(); x++) {
                String path = mainActivityInterface.getCurrentSet().getCurrentSet_Folder().get(x);
                // If the path isn't empty, add a forward slash to the end
                if (!path.isEmpty()) {
                    path = path + "/";
                }
                String name = mainActivityInterface.getCurrentSet().getCurrentSet_Filename().get(x);
                boolean isImage = name.contains("**" + c.getString(R.string.image));
                boolean isVariation = name.contains("**" + c.getString(R.string.variation));
                boolean isScripture = name.contains("**" + c.getString(R.string.scripture));
                boolean isSlide = name.contains("**" + c.getString(R.string.slide));
                boolean isNote = name.contains("**" + c.getString(R.string.note));

                if (isImage) {
                    // Adding an image
                    Song tempSong = getTempSong(c,mainActivityInterface,thisSong,"../Images/_cache", name);
                    sb.append(buildImage(c,mainActivityInterface,tempSong));

                } else if (isScripture) {
                    // Adding a scripture
                    Song tempSong = getTempSong(c,mainActivityInterface,thisSong, "../Scripture/_cache", name);
                    sb.append(buildScripture(mainActivityInterface,tempSong));

                } else if (isVariation) {
                    // Adding a variation
                    Song tempSong = getTempSong(c,mainActivityInterface,thisSong, "../Variations", name);
                    sb.append(buildVariation(c,mainActivityInterface,tempSong));

                } else if (isSlide) {
                    // Adding a slide
                    Song tempSong = getTempSong(c,mainActivityInterface,thisSong, "../Slides/_cache", name);
                    sb.append(buildSlide(mainActivityInterface,tempSong));

                } else if (isNote) {
                    // Adding a note
                    Song tempSong = getTempSong(c,mainActivityInterface,thisSong, "../Notes/_cache", name);
                    sb.append(buildNote(c,mainActivityInterface,tempSong));
                } else {
                    // Adding a song
                    sb.append(buildSong(mainActivityInterface,path,name));

                }
            }
            sb.append("</slide_groups>\n</set>");

            mainActivityInterface.getCurrentSet().setCurrentSetXML(sb.toString());

            // Write the string to the file
            Uri uri = mainActivityInterface.getStorageAccess().getUriForItem(c, mainActivityInterface, "Sets", "", mainActivityInterface.getCurrentSet().getSetFile());

            // Check the uri exists for the outputstream to be valid
            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(c, mainActivityInterface, uri, null, "Sets", "", mainActivityInterface.getCurrentSet().getSetFile());

            OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(c,uri);
            if (mainActivityInterface.getStorageAccess().writeFileFromString(mainActivityInterface.getCurrentSet().getCurrentSetXML(),outputStream)) {
                // Update the last loaded set now it is saved.
                mainActivityInterface.getPreferences().setMyPreferenceString(c,"setCurrentBeforeEdits",mainActivityInterface.getPreferences().getMyPreferenceString(c,"setCurrent",""));
            }

            mainActivityInterface.getPreferences().setMyPreferenceString(c,"setCurrentLastName",mainActivityInterface.getCurrentSet().getSetFile());
            return true;
        } else {
            return false;
        }
    }
*/



        /*

            for (int x = 0; x < curren; x++) {




                } else if (songparts[0].contains("**"+c.getResources().getString(R.string.scripture)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.variation)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.image)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.slide)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.note))) {
                    // Adding a scripture


                } else if (songparts[0].contains("**"+c.getResources().getString(R.string.slide)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.variation)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.image)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.note)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.scripture))) {


                } else if (songparts[0].contains("**"+c.getResources().getString(R.string.note)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.variation)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.image)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.slide)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.scripture))) {
                    // Adding a custom note





                } else if (songparts[0].contains("**"+c.getResources().getString(R.string.variation)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.note)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.image)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.slide)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.scripture))) {
                    // Adding a variation


                } else if (songparts[0].contains("**"+c.getResources().getString(R.string.image)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.variation)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.note)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.slide)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.scripture))) {

            }


    }
*/

}
