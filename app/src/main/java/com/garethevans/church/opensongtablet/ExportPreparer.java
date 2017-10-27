package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class ExportPreparer {

	private static String setxml = "";
	static String settext = "";
	private static String song_title = "";
	private static String song_author = "";
    private static String song_hymnnumber = "";
	private static String song_key = "";
    private static File songfile = null;
    private static ArrayList<String> filesinset = new ArrayList<>();
	private static ArrayList<String> filesinset_ost = new ArrayList<>();
    static Image image;
    //static Backup_Create backup_create;
    @SuppressLint("StaticFieldLeak")
    private static Backup_Create_Selected backup_create_selected;
    Context context;
    @SuppressLint("StaticFieldLeak")
    static Activity activity;
    static Intent emailIntent;
    static String folderstoexport = "";
    private static ZipOutputStream outSelected;

	private static boolean setParser(Context c) throws IOException, XmlPullParserException {

        settext = "";
        FullscreenActivity.exportsetfilenames.clear();
        FullscreenActivity.exportsetfilenames_ost.clear();
        filesinset.clear();
        filesinset_ost.clear();

		// First up, load the set
		File settoparse = new File(FullscreenActivity.dirsets + "/" + FullscreenActivity.settoload);
		if (!settoparse.isFile() || !settoparse.exists()) {
			return false;
		}

		try {
			FileInputStream inputStreamSet = new FileInputStream(settoparse);
			InputStreamReader streamReaderSet = new InputStreamReader(inputStreamSet);
			BufferedReader bufferedReaderSet = new BufferedReader(streamReaderSet);
			setxml = readTextFile(inputStreamSet);
			inputStreamSet.close();
			bufferedReaderSet.close();
			inputStreamSet.close(); // close the file
		} catch (Exception e) {
			e.printStackTrace();
		}

		XmlPullParserFactory factory;
		factory = XmlPullParserFactory.newInstance();

		factory.setNamespaceAware(true);
		XmlPullParser xpp;
		xpp = factory.newPullParser();

		xpp.setInput(new StringReader(setxml));
		int eventType;

		eventType = xpp.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				if (xpp.getName().equals("slide_group")) {
					if (xpp.getAttributeValue(null,"type").equals("song")) {
						songfile = null;
                        String thisline;
						songfile = new File(FullscreenActivity.homedir + "/Songs/" + LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null,"path")) + LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null,"name")));
						// Ensure there is a folder '/'
                        if (xpp.getAttributeValue(null,"path").equals("")) {
                            thisline = "/" + LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null,"name"));
                        } else {
                            thisline = LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null,"path")) + LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null,"name"));
                        }
						filesinset.add(thisline);
						filesinset_ost.add(thisline);

                        // Set the default values exported with the text for the set
                        song_title = LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null,"name"));
						song_author = "";
						song_hymnnumber = "";
						song_key = "";
                        // Now try to improve on this info
						if (songfile.exists() && songfile.isFile()) {
							// Read in the song title, author, copyright, hymnnumber, key
							getSongData();
						}
						settext = settext + song_title;
						if (!song_author.isEmpty()) {
							settext = settext + ", " + song_author;
						}
						if (!song_hymnnumber.isEmpty()) {
							settext = settext + ", #" + song_hymnnumber;
						}
						if (!song_key.isEmpty()) {
							settext = settext + " (" + song_key + ")";
						}
						settext = settext + "\n";
					} else if (xpp.getAttributeValue(null,"type").equals("scripture")) {
						settext = settext + LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null,"name")) + "\n";

					} else if (xpp.getAttributeValue(null,"type").equals("custom")) {
                        // Decide if this is a note or a slide
                        if (xpp.getAttributeValue(null,"name").contains("# " + c.getResources().getString(R.string.note) + " # - ")) {
                            String nametemp = LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null,"name"));
                            nametemp = nametemp.replace("# " + c.getResources().getString(R.string.note) + " # - ","");
                            settext = settext + nametemp + "\n";
                        } else {
                            settext = settext + LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null, "name")) + "\n";
                        }
					} else if (xpp.getAttributeValue(null,"type").equals("image")) {
                        // Go through the descriptions of each image and extract the absolute file locations
                        boolean allimagesdone = false;
                        ArrayList<String> theseimages = new ArrayList<>();
						String imgname;
						imgname = LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null,"name"));
                        while (!allimagesdone) { // Keep iterating unless the current eventType is the end of the document
                            if (eventType == XmlPullParser.START_TAG) {
                                if (xpp.getName().equals("description")) {
                                    xpp.next();
                                    theseimages.add(LoadXML.parseFromHTMLEntities(xpp.getText()));
                                    filesinset.add(LoadXML.parseFromHTMLEntities(xpp.getText()));
                                    filesinset_ost.add(LoadXML.parseFromHTMLEntities(xpp.getText()));
                                }

                            } else if (eventType == XmlPullParser.END_TAG) {
                                if (xpp.getName().equals("slide_group")) {
                                    allimagesdone = true;
                                }
                            }

                            eventType = xpp.next(); // Set the current event type from the return value of next()
                        }
                        // Go through each of these images and add a line for each one
                        settext = settext + imgname + "\n";
                        for (int im=0;im<theseimages.size();im++) {
                            settext = settext + "     - " + theseimages.get(im) + "\n";
                        }
					}
				}
			}
			eventType = xpp.next();		
		}

		// Send the settext back to the FullscreenActivity as emailtext
		FullscreenActivity.emailtext = settext;
        FullscreenActivity.exportsetfilenames = filesinset;
        FullscreenActivity.exportsetfilenames_ost = filesinset_ost;
        return true;
	}

	private static void getSongData() throws XmlPullParserException, IOException {
		// Parse the song xml.
		// Grab the title, author, lyrics_withchords, lyrics_withoutchords, copyright, hymnnumber, key

		// Initialise all the xml tags a song should have that we want
		String songxml = "";
		song_title = "";
		song_author = "";
        String song_lyrics_withchords = "";
        String song_lyrics_withoutchords = "";
        //String song_copyright = "";
		song_hymnnumber = "";
		song_key = "";

		try {
			FileInputStream inputStreamSong = new FileInputStream(songfile);
			InputStreamReader streamReaderSong = new InputStreamReader(inputStreamSong);
            BufferedReader bufferedReaderSong = new BufferedReader(streamReaderSong);
			songxml = readTextFile(inputStreamSong);
			inputStreamSong.close();
			bufferedReaderSong.close();
			inputStreamSong.close(); // close the file
		} catch (java.io.FileNotFoundException e) {
			// file doesn't exist
			//song_title = songfile.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//Change the line breaks and Slides to better match OpenSong
		songxml = songxml.replaceAll("\r\n", "\n");
		songxml = songxml.replaceAll("\r", "\n");
		songxml = songxml.replaceAll("\t", "    ");
		songxml = songxml.replaceAll("\\t", "    ");
		songxml = songxml.replaceAll("\f", "    ");
		songxml = songxml.replace("\r", "");
		songxml = songxml.replace("\t", "    ");
		songxml = songxml.replace("\b", "    ");
		songxml = songxml.replace("\f", "    ");
        songxml = songxml.replace("&#0;","");

		// Extract all of the key bits of the song
		XmlPullParserFactory factorySong;
		factorySong = XmlPullParserFactory.newInstance();

		factorySong.setNamespaceAware(true);
		XmlPullParser xppSong;
		xppSong = factorySong.newPullParser();

		xppSong.setInput(new StringReader(songxml));

		int eventType;
		eventType = xppSong.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				if (xppSong.getName().equals("author")) {
					song_author = LoadXML.parseFromHTMLEntities(xppSong.nextText());
				/*} else if (xppSong.getName().equals("copyright")) {
					song_copyright = LoadXML.parseFromHTMLEntities(xppSong.nextText());*/
				} else if (xppSong.getName().equals("title")) {
					song_title = LoadXML.parseFromHTMLEntities(xppSong.nextText());
				} else if (xppSong.getName().equals("lyrics")) {
					song_lyrics_withchords = LoadXML.parseFromHTMLEntities(xppSong.nextText());
				} else if (xppSong.getName().equals("hymn_number")) {
					song_hymnnumber = LoadXML.parseFromHTMLEntities(xppSong.nextText());
				} else if (xppSong.getName().equals("key")) {
					song_key = LoadXML.parseFromHTMLEntities(xppSong.nextText());
				}
			}
			eventType = xppSong.next();
		}
		// Remove the chord lines from the song lyrics
		String[] templyrics = song_lyrics_withchords.split("\n");
		// Only add the lines that don't start with a .
		int numlines = templyrics.length;
		if (numlines>0) {
            for (String templyric : templyrics) {
                if (!templyric.startsWith(".")) {
                    song_lyrics_withoutchords = song_lyrics_withoutchords + templyric + "\n";
                }
            }
		}
	}

	private static String readTextFile(InputStream inputStream) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		byte buf[] = new byte[1024];
		int len;
		try {
			while ((len = inputStream.read(buf)) != -1) {
				outputStream.write(buf, 0, len);
			}
			outputStream.close();
			inputStream.close();
		} catch (IOException e) {
            e.printStackTrace();
		}
		return outputStream.toString();
	}

	static Intent exportSet(Context c) {
        String nicename = FullscreenActivity.settoload;
        Uri text = null;
        Uri desktop = null;
        Uri osts = null;
        File newfile;

        // Prepare a txt version of the set.
        try {
            if (!setParser(c)) {
                Log.d("d","Problem parsing the set");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (FullscreenActivity.settoload.contains("__")) {
            String[] bits = FullscreenActivity.settoload.split("__");
            String category = "";
            String name = FullscreenActivity.settoload;
            if (bits[0]!=null && !bits[0].equals("")) {
                category = " (" + bits[0] + ")";
            }
            if (bits[1]!=null && !bits[1].equals("")) {
                name = bits[1];
            }
            nicename = name + category;
        }

        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, nicename);
        emailIntent.putExtra(Intent.EXTRA_TITLE, nicename);
        emailIntent.putExtra(Intent.EXTRA_TEXT, nicename + "\n\n" + FullscreenActivity.emailtext);
        // Check that the export directory exists
        File exportdir = new File(FullscreenActivity.homedir + "/Export");

        if (!exportdir.mkdirs()) {
            Log.d("d","Can't create");
        }

        File setfile  = new File(FullscreenActivity.dirsets + "/" + FullscreenActivity.settoload);
        File ostsfile = new File(exportdir + "/" + FullscreenActivity.settoload + ".osts");

        if (!setfile.exists() || !setfile.canRead()) {
            return null;
        }

        if (FullscreenActivity.exportText) {
            newfile = new File(exportdir, FullscreenActivity.settoload + ".txt");
            writeFile(FullscreenActivity.emailtext, newfile, "text", null);
            text = Uri.fromFile(newfile);
        }

        FullscreenActivity.emailtext = "";

        if (FullscreenActivity.exportDesktop) {
            desktop = Uri.fromFile(setfile);
        }

        if (FullscreenActivity.exportOpenSongAppSet) {
            // Copy the set file to an .osts file
            try {
                FileInputStream in = new FileInputStream(setfile);
                FileOutputStream out = new FileOutputStream(ostsfile);
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();

                // write the output file (You have now copied the file)
                out.flush();
                out.close();
                osts = Uri.fromFile(ostsfile);
            } catch (Exception e) {
                // Error
                e.printStackTrace();
            }
        }

        ArrayList<Uri> uris = new ArrayList<>();
        if (text!=null) {
            uris.add(text);
        }
        if (osts!=null) {
            uris.add(osts);
        }
        if (desktop!=null) {
            uris.add(desktop);
        }

        // Go through each song in the set and attach them
        // Also try to attach a copy of the song ending in .ost, as long as they aren't images
        if (FullscreenActivity.exportOpenSongApp) {
            for (int q = 0; q < FullscreenActivity.exportsetfilenames.size(); q++) {
                // Remove any subfolder from the exportsetfilenames_ost.get(q)
                String tempsong_ost = FullscreenActivity.exportsetfilenames_ost.get(q);
                tempsong_ost = tempsong_ost.substring(tempsong_ost.indexOf("/") + 1);
                File songtoload = new File(FullscreenActivity.dir + "/" + FullscreenActivity.exportsetfilenames.get(q));
                File ostsongcopy = new File(FullscreenActivity.homedir + "/Notes/_cache/" + tempsong_ost + ".ost");
                boolean isimage = false;
                if (songtoload.toString().endsWith(".jpg") || songtoload.toString().endsWith(".JPG") ||
                        songtoload.toString().endsWith(".jpeg") || songtoload.toString().endsWith(".JPEG") ||
                        songtoload.toString().endsWith(".gif") || songtoload.toString().endsWith(".GIF") ||
                        songtoload.toString().endsWith(".png") || songtoload.toString().endsWith(".PNG") ||
                        songtoload.toString().endsWith(".bmp") || songtoload.toString().endsWith(".BMP")) {
                    songtoload = new File(FullscreenActivity.exportsetfilenames.get(q));
                    isimage = true;
                }

                // Copy the song
                if (songtoload.exists()) {
                    try {
                        if (!isimage) {
                            FileInputStream in = new FileInputStream(songtoload);
                            FileOutputStream out = new FileOutputStream(ostsongcopy);

                            byte[] buffer = new byte[1024];
                            int read;
                            while ((read = in.read(buffer)) != -1) {
                                out.write(buffer, 0, read);
                            }
                            in.close();

                            // write the output file (You have now copied the file)
                            out.flush();
                            out.close();
                            Uri urisongs_ost = Uri.fromFile(ostsongcopy);
                            uris.add(urisongs_ost);
                        }
                    } catch (Exception e) {
                        // Error
                        e.printStackTrace();
                    }
                }
            }
        }
        if (FullscreenActivity.exportDesktop) {
            for (int q = 0; q < FullscreenActivity.exportsetfilenames.size(); q++) {
                File songtoload = new File(FullscreenActivity.dir + "/" + FullscreenActivity.exportsetfilenames.get(q));
                Uri urisongs = Uri.fromFile(songtoload);
                uris.add(urisongs);
            }
        }

        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        return emailIntent;
    }

	static Intent exportSong(Context c, Bitmap bmp) {
        // Prepare the appropriate attachments
        String emailcontent = "";
        Uri text = null;
        Uri ost = null;
        Uri desktop = null;
        Uri chopro = null;
        Uri onsong = null;
        Uri image = null;
        Uri pdf = null;
        File newfile;

        // Prepare a txt version of the song.
        prepareTextFile(c);
        // Check we have a directory to save these
        File exportdir = new File(FullscreenActivity.homedir + "/Export");

        if (!exportdir.mkdirs()) {
            Log.d("d","Can't create");
        }
        emailcontent += FullscreenActivity.exportText_String;
        if (FullscreenActivity.exportText) {
            newfile = new File(exportdir, FullscreenActivity.songfilename + ".txt");
            writeFile(FullscreenActivity.exportText_String, newfile, "text", null);
            text = Uri.fromFile(newfile);
        }

        if (FullscreenActivity.exportOpenSongApp) {
            // Prepare an ost version of the song.
            newfile = new File(exportdir, FullscreenActivity.songfilename + ".ost");
            File filetocopy;
            if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
                filetocopy = new File(FullscreenActivity.dir + "/" + FullscreenActivity.songfilename);
            } else {
                filetocopy =  new File(FullscreenActivity.dir + "/" +
                        FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.songfilename);
            }
            copyFile(filetocopy, newfile);
            ost = Uri.fromFile(newfile);
        }

        if (FullscreenActivity.exportDesktop) {
            // Prepare a desktop version of the song.
            newfile = new File(exportdir, FullscreenActivity.songfilename);
            File filetocopy;
            if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
                filetocopy = new File(FullscreenActivity.dir + "/" + FullscreenActivity.songfilename);
            } else {
                filetocopy =  new File(FullscreenActivity.dir + "/" +
                        FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.songfilename);
            }
            copyFile(filetocopy, newfile);
            desktop = Uri.fromFile(newfile);
        }

        if (FullscreenActivity.exportChordPro) {
            // Prepare a chordpro version of the song.
            newfile = new File(exportdir, FullscreenActivity.songfilename + ".chopro");
            prepareChordProFile(c);
            writeFile(FullscreenActivity.exportChordPro_String, newfile, "chopro", null);
            chopro = Uri.fromFile(newfile);
        }

        if (FullscreenActivity.exportOnSong) {
            // Prepare an onsong version of the song.
            newfile = new File(exportdir, FullscreenActivity.songfilename + ".onsong");
            prepareOnSongFile(c);
            writeFile(FullscreenActivity.exportOnSong_String, newfile, "onsong", null);
            onsong = Uri.fromFile(newfile);
        }

        if (FullscreenActivity.exportImage) {
            // Prepare an image/png version of the song.
            newfile = new File(exportdir, FullscreenActivity.songfilename + ".png");
            writeFile(FullscreenActivity.exportOnSong_String, newfile, "png", bmp);
            image = Uri.fromFile(newfile);
        }

        if (FullscreenActivity.exportPDF) {
            // Prepare a pdf version of the song.
            newfile = new File(exportdir, FullscreenActivity.songfilename + ".pdf");
            makePDF(bmp, newfile);
            //writeFile(c,FullscreenActivity.exportOnSong_String, newfile, "pdf", bmp);
            pdf = Uri.fromFile(newfile);
        }

        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_TITLE, FullscreenActivity.songfilename);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, FullscreenActivity.songfilename);
        emailIntent.putExtra(Intent.EXTRA_TEXT, emailcontent);
        FullscreenActivity.emailtext = "";

        // Add the attachments
        ArrayList<Uri> uris = new ArrayList<>();
        if (ost != null) {
            uris.add(ost);
        }
        if (text != null) {
            uris.add(text);
        }
        if (desktop != null) {
            uris.add(desktop);
        }
        if (chopro != null) {
            uris.add(chopro);
        }
        if (onsong != null) {
            uris.add(onsong);
        }
        if (image != null) {
            uris.add(image);
        }
        if (pdf != null) {
            uris.add(pdf);
        }
        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        return emailIntent;
    }

    static Intent exportBackup(Context c, File f) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_TITLE, c.getString(R.string.backup_info));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT,  c.getString(R.string.backup_info));
        emailIntent.putExtra(Intent.EXTRA_TEXT,  c.getString(R.string.backup_info));
        FullscreenActivity.emailtext = "";

        Uri uri = Uri.fromFile(f);
        ArrayList<Uri> uris = new ArrayList<>();
        uris.add(uri);

        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        return emailIntent;
    }

    private static void makePDF(Bitmap bmp, File file) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.addAuthor(FullscreenActivity.mAuthor.toString());
            document.addTitle(FullscreenActivity.mTitle.toString());
            document.addCreator("OpenSongApp");
            if (bmp!=null && bmp.getWidth()>bmp.getHeight()) {
                document.setPageSize(PageSize.A4.rotate());
            } else {
                document.setPageSize(PageSize.A4);
            }
            document.addTitle(FullscreenActivity.mTitle.toString());
            document.open();//document.add(new Header("Song title",FullscreenActivity.mTitle.toString()));
            BaseFont urName = BaseFont.createFont("assets/fonts/Lato-Reg.ttf", "UTF-8",BaseFont.EMBEDDED);
            Font TitleFontName  = new Font(urName, 14);
            Font AuthorFontName = new Font(urName, 10);
            document.add(new Paragraph(FullscreenActivity.mTitle.toString(),TitleFontName));
            document.add(new Paragraph(FullscreenActivity.mAuthor.toString(),AuthorFontName));
            addImage(document,bmp);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addImage(Document document, Bitmap bmp) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] bArray = stream.toByteArray();
            image = Image.getInstance(bArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
        float A4_width  = document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin() - 80;
        float A4_height = document.getPageSize().getHeight() - document.topMargin() - document.bottomMargin();
        int bmp_width   = bmp.getWidth();
        int bmp_height  = bmp.getHeight();
        // If width is bigger than height, then landscape it!

        float x_scale = A4_width/(float)bmp_width;
        float y_scale = A4_height/(float)bmp_height;
        float new_width;
        float new_height;

        if (x_scale>y_scale) {
            new_width  = bmp_width  * y_scale;
            new_height = bmp_height * y_scale;
        } else {
            new_width  = bmp_width  * x_scale;
            new_height = bmp_height * x_scale;
        }
        image.scaleAbsolute(new_width,new_height);
        image.scaleToFit(A4_width,A4_height);
        image.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_BOTTOM);
        //image.scaleToFit(PageSize.A4.getWidth(), PageSize.A4.getHeight());
        // image.scaleAbsolute(150f, 150f);
        try {
            document.add(image);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void createSelectedOSB(Context c) {
        activity = (Activity) c;
        if (backup_create_selected!=null) {
            backup_create_selected.cancel(true);
        }
        backup_create_selected = new Backup_Create_Selected(c);
        backup_create_selected.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    private static class Backup_Create_Selected extends AsyncTask<String, Void, String> {
        @SuppressLint("StaticFieldLeak")
        Context c;
        Backup_Create_Selected(Context context) {
            c = context;
        }
        @Override
        protected String doInBackground(String... strings) {
            return makeBackupZipSelected();
        }

        boolean cancelled = false;
        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        public void onPostExecute(String s) {
            if (!cancelled) {
                try {
                    File f = new File(s);
                    FullscreenActivity.myToastMessage = c.getString(R.string.backup_success);
                    ShowToast.showToast(c);
                    emailIntent = exportBackup(c, f);
                    activity.startActivityForResult(Intent.createChooser(emailIntent, c.getString(R.string.backup_info)), 12345);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    static String makeBackupZipSelected() {
        // Get the date for the file
        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd", FullscreenActivity.locale);
        String formattedDate = df.format(c.getTime());
        String backup = FullscreenActivity.homedir + "/OpenSongBackup_" + formattedDate + ".osb";
        String songfolder = FullscreenActivity.dir.toString();
        try {
            zipDirSelected(backup, songfolder);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return backup;
    }
    private static void zipDirSelected(String zipFileName, String dir) throws Exception {
        outSelected = new ZipOutputStream(new FileOutputStream(zipFileName));
        System.out.println("Creating : " + zipFileName);

        // Go through each of the selected folders and add them to the zip file
        String[] whichfolders = folderstoexport.split("__%%__");
        for (int i=0;i<whichfolders.length;i++) {
            if (!whichfolders[i].equals("")) {
                whichfolders[i] = whichfolders[i].replace("%__", "");
                whichfolders[i] = whichfolders[i].replace("__%", "");
                Log.d("d", "whichfolders[" + i + "]=" + whichfolders[i]);
                File dirObj = new File(dir + "/" + whichfolders[i]);
                addDirSelected(dirObj);
            }
        }
        outSelected.close();
    }
    private static void addDirSelected(File dirObj) throws IOException {
        Log.d("d","dirObj="+dirObj.toString());
        if (dirObj.toString().contains("/"+FullscreenActivity.mainfoldername)) {
            dirObj = new File(FullscreenActivity.dir.toString());
        }
        File[] files = dirObj.listFiles();
        byte[] tmpBuf = new byte[1024];

        for (File file : files) {
            if (file.isFile()) {
                FileInputStream in = new FileInputStream(file.getAbsolutePath());
                System.out.println(" Adding: " + file.getAbsolutePath().replace(FullscreenActivity.dir.toString() + "/", ""));
                outSelected.putNextEntry(new ZipEntry((file.getAbsolutePath()).replace(FullscreenActivity.dir.toString() + "/", "")));
                int len;
                while ((len = in.read(tmpBuf)) > 0) {
                    outSelected.write(tmpBuf, 0, len);
                }
                outSelected.closeEntry();
                in.close();
            }
        }
    }

    private static void prepareChordProFile(Context c) {
        // This converts an OpenSong file into a ChordPro file
        FullscreenActivity.exportChordPro_String = "";
        String s = "{ns}\n";
        s += "{t:"+FullscreenActivity.mTitle+"}\n";
        s += "{st:"+FullscreenActivity.mAuthor+"}\n\n";

        // Go through each song section and add the ChordPro formatted chords
        for (int f=0;f<FullscreenActivity.songSections.length;f++) {
            s += ProcessSong.songSectionChordPro(c, f, false);
        }

        s = s.replace("\n\n\n","\n\n");
        Log.d("d","ChordProSong = "+s);
        FullscreenActivity.exportChordPro_String = s;
    }
    private static void prepareOnSongFile(Context c) {
        // This converts an OpenSong file into a OnSong file
        FullscreenActivity.exportOnSong_String = "";
        String s = FullscreenActivity.mTitle+"\n";
        if (!FullscreenActivity.mAuthor.equals("")) {
            s += FullscreenActivity.mAuthor + "\n";
        }
        if (!FullscreenActivity.mCopyright.equals("")) {
            s += "Copyright: "+FullscreenActivity.mCopyright + "\n";
        }
        if (!FullscreenActivity.mKey.equals("")) {
            s += "Key: " + FullscreenActivity.mKey + "\n\n";
        }

        // Go through each song section and add the ChordPro formatted chords
        for (int f=0;f<FullscreenActivity.songSections.length;f++) {
            s += ProcessSong.songSectionChordPro(c, f, true);
        }

        s = s.replace("\n\n\n","\n\n");
        Log.d("d","OnSong = "+s);
        FullscreenActivity.exportOnSong_String = s;
    }
    private static void prepareTextFile(Context c) {
        // This converts an OpenSong file into a text file
        FullscreenActivity.exportText_String = "";
        String s = FullscreenActivity.mTitle+"\n";
        if (!FullscreenActivity.mAuthor.equals("")) {
            s += FullscreenActivity.mAuthor + "\n";
        }
        if (!FullscreenActivity.mCopyright.equals("")) {
            s += "Copyright: "+FullscreenActivity.mCopyright + "\n";
        }
        if (!FullscreenActivity.mKey.equals("")) {
            s += "Key: " + FullscreenActivity.mKey + "\n\n";
        }

        // Go through each song section and add the text trimmed lines
        for (int f=0;f<FullscreenActivity.songSections.length;f++) {
            s += ProcessSong.songSectionText(c, f);
        }

        s = s.replace("\n\n\n","\n\n");
        FullscreenActivity.exportText_String = s;
    }
    private static void writeFile(String s, File f, String what, Bitmap bmp) {
        if (what.equals("png")) {
            try {
                FileOutputStream out = new FileOutputStream(f);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                new FileOutputStream (new File(f.getAbsolutePath()), true);
                FileOutputStream fOut = new FileOutputStream(f);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fOut);
                outputStreamWriter.write(s);
                outputStreamWriter.close();
            } catch (Exception e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        }
    }
    private static void copyFile(File from, File to) {
        try {
            InputStream is=new FileInputStream(from);
            OutputStream os=new FileOutputStream(to);
            byte[] buff=new byte[1024];
            int len;
            while((len=is.read(buff))>0){
                os.write(buff,0,len);
            }
            is.close();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
