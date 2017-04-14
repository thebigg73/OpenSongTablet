package com.garethevans.church.opensongtablet;

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
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ExportPreparer extends Activity {

	static String setxml = "";
	static String settext = "";
	static String songtext = "";
	static String song_title = "";
	static String song_author = "";
	static String song_copyright = "";
	static String song_hymnnumber = "";
	static String song_key = "";
	static String song_lyrics_withchords = "";
	static String song_lyrics_withoutchords = "";
	static File songfile = null;
    static ArrayList<String> filesinset = new ArrayList<>();
	static ArrayList<String> filesinset_ost = new ArrayList<>();
    static String FILE = FullscreenActivity.homedir + "/Images/_cache/" + FullscreenActivity.songfilename +".pdf";
    static Image image;
    static byte[] bArray;
    static Backup_Create backup_create;
    static Backup_Create_Selected backup_create_selected;
    static Context context;
    static Activity activity;
    static Intent emailIntent;
    static String folderstoexport = "";
    static ZipOutputStream outSelected;

	public static void songParser() throws XmlPullParserException, IOException {

		songfile = null;
		if (!FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
            songfile = new File(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.songfilename);
        } else {
			songfile = new File(FullscreenActivity.dir + "/" + FullscreenActivity.songfilename);
		}
		getSongData();
		songtext = "";
		if (!song_title.isEmpty()) {
			songtext = songtext + song_title + "\n";
		} else {
			songtext = songtext + songfile.toString();
		}
		if (!song_author.isEmpty()) {
			songtext = songtext + song_author + "\n";
		}
		if (!song_copyright.isEmpty()) {
			songtext = songtext + song_copyright + "\n";
		}
		songtext = songtext + "\n\n\n" + song_lyrics_withchords;
		songtext = songtext + "\n\n\n\n_______________________________\n\n\n\n" + song_lyrics_withoutchords;

		FullscreenActivity.emailtext = songtext;
	}

	public static boolean setParser() throws IOException, XmlPullParserException {

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
						//filesinset_ost.add(xpp.getAttributeValue(0));
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
						settext = settext + FullscreenActivity.song + " : " + song_title;
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
						settext = settext + FullscreenActivity.scripture.toUpperCase(Locale.getDefault()) + " : " + LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null,"name")) + "\n";

					} else if (xpp.getAttributeValue(null,"type").equals("custom")) {
                        // Decide if this is a note or a slide
                        if (xpp.getAttributeValue(null,"name").contains("# " + FullscreenActivity.text_note + " # - ")) {
                            String nametemp = LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null,"name"));
                            nametemp = nametemp.replace("# " + FullscreenActivity.note + " # - ","");
                            settext = settext + FullscreenActivity.note.toUpperCase(Locale.getDefault()) + " : " + nametemp + "\n";
                        } else {
                            settext = settext + FullscreenActivity.slide.toUpperCase(Locale.getDefault()) + " : " + LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null, "name")) + "\n";
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
                        settext = settext + FullscreenActivity.image.toUpperCase(Locale.getDefault()) + " : " + imgname + "\n";
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

	public static void getSongData() throws XmlPullParserException, IOException {
		// Parse the song xml.
		// Grab the title, author, lyrics_withchords, lyrics_withoutchords, copyright, hymnnumber, key

		// Initialise all the xml tags a song should have that we want
		String songxml = "";
		song_title = "";
		song_author = "";
		song_lyrics_withchords = "";
		song_lyrics_withoutchords = "";
		song_copyright = "";
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
			//song_title = songfile.toString();
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
				} else if (xppSong.getName().equals("copyright")) {
					song_copyright = LoadXML.parseFromHTMLEntities(xppSong.nextText());
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

	public static Intent exportSong(Context c, Bitmap bmp) {
        File saved_image_file = new File(
                FullscreenActivity.homedir + "/Images/_cache/" + FullscreenActivity.songfilename + ".png");
        if (saved_image_file.exists())
            if (!saved_image_file.delete()) {
                Log.d("d","error removing temp image file");
            }
        try {
            FileOutputStream out = new FileOutputStream(saved_image_file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Run the script that generates the email text which has the set details in it.
        try {
            songParser();
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }

        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_TITLE, FullscreenActivity.songfilename);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, FullscreenActivity.songfilename);
        emailIntent.putExtra(Intent.EXTRA_TEXT, FullscreenActivity.emailtext);
        FullscreenActivity.emailtext = "";
        String songlocation = FullscreenActivity.dir + "/";
        String tolocation = FullscreenActivity.homedir + "/Notes/_cache/";
        Uri uri;
        if (!FullscreenActivity.dir.toString().contains("/" + FullscreenActivity.whichSongFolder + "/")
                && !FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
            uri = Uri.fromFile(new File(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.songfilename));
            songlocation = songlocation + FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.songfilename;
            tolocation = tolocation + "/" + FullscreenActivity.songfilename + ".ost";
        } else {
            uri = Uri.fromFile(new File(FullscreenActivity.dir + "/" + FullscreenActivity.songfilename));
            songlocation = songlocation + FullscreenActivity.songfilename;
            tolocation = tolocation + "/" + FullscreenActivity.songfilename + ".ost";
        }

        Uri uri2 = Uri.fromFile(saved_image_file);
        Uri uri3 = null;
        // Also add an .ost version of the file
        try {
            FileInputStream in = new FileInputStream(new File(songlocation));
            FileOutputStream out = new FileOutputStream(new File(tolocation));

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();

            // write the output file (You have now copied the file)
            out.flush();
            out.close();

            uri3 = Uri.fromFile(new File(tolocation));

        } catch (Exception e) {
            // Error
            e.printStackTrace();
        }

        Uri uri4 = null;
        if (bmp==null) {
            FullscreenActivity.myToastMessage = c.getString (R.string.toobig);
            ShowToast.showToast(c);
        } else {
            makePDF(bmp);
        }
        try {
            uri4 = Uri.fromFile(new File(FILE));
        } catch (Exception e) {
            // Error
            e.printStackTrace();
        }


        ArrayList<Uri> uris = new ArrayList<>();
        if (uri != null) {
            uris.add(uri);
        }
        if (uri2 != null) {
            uris.add(uri2);
        }
        if (uri3 != null) {
            uris.add(uri3);
        }
        if (uri4 != null) {
            uris.add(uri4);
        }
        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        return emailIntent;
        // These .ost and .png files will be removed when a user loads a new set
    }

    public static Intent exportBackup(Context c, File f) {
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

    public static void makePDF (Bitmap bmp) {
        Document document = new Document();
        try {
            FILE = FullscreenActivity.homedir + "/Images/_cache/" + FullscreenActivity.songfilename +".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(FILE));
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

    public static void addImage(Document document, Bitmap bmp) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            bArray = stream.toByteArray();
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

    public static void createSelectedOSB(Context c) {
        activity = (Activity) c;
        context = c;
        if (backup_create!=null) {
            backup_create.cancel(true);
        }
        backup_create_selected = new Backup_Create_Selected();
        backup_create_selected.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    public static class Backup_Create_Selected extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            return makeBackupZipSelected();
        }

        @Override
        public void onPostExecute(String s) {
            File f = new File(s);
            FullscreenActivity.myToastMessage = context.getString(R.string.backup_success);
            ShowToast.showToast(context);
            emailIntent = exportBackup(context, f);
            activity.startActivityForResult(Intent.createChooser(emailIntent, context.getString(R.string.backup_info)), 12345);
        }
    }
    public static String makeBackupZipSelected() {
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
    static void addDirSelected(File dirObj) throws IOException {
        Log.d("d","dirObj="+dirObj.toString());
        if (dirObj.toString().contains("/"+FullscreenActivity.mainfoldername)) {
            dirObj = new File(FullscreenActivity.dir.toString());
        }
        File[] files = dirObj.listFiles();
        byte[] tmpBuf = new byte[1024];

        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                FileInputStream in = new FileInputStream(files[i].getAbsolutePath());
                System.out.println(" Adding: " + files[i].getAbsolutePath().replace(FullscreenActivity.dir.toString() + "/", ""));
                outSelected.putNextEntry(new ZipEntry((files[i].getAbsolutePath()).replace(FullscreenActivity.dir.toString() + "/", "")));
                int len;
                while ((len = in.read(tmpBuf)) > 0) {
                    outSelected.write(tmpBuf, 0, len);
                }
                outSelected.closeEntry();
                in.close();
            }
        }
    }





    public static void createOpenSongBackup(Context c) {
        context = c;
        activity = (Activity) context;
        if (backup_create!=null) {
            backup_create.cancel(true);
        }
        backup_create = new Backup_Create();
        backup_create.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    public static class Backup_Create extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            return makeBackupZip();
        }

        @Override
        public void onPostExecute(String s) {
            File f = new File(s);
            FullscreenActivity.myToastMessage = context.getString(R.string.backup_success);
            ShowToast.showToast(context);
            emailIntent = exportBackup(context, f);
            activity.startActivityForResult(Intent.createChooser(emailIntent, context.getString(R.string.backup_info)), 12345);
        }
    }
    public static String makeBackupZip() {
        // Get the date for the file
        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd", FullscreenActivity.locale);
        String formattedDate = df.format(c.getTime());
        String backup = FullscreenActivity.homedir + "/OpenSongBackup_" + formattedDate + ".osb";
        String songfolder = FullscreenActivity.dir.toString();
        try {
            zipDir(backup, songfolder);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return backup;
    }
    private static void zipDir(String zipFileName, String dir) throws Exception {
        File dirObj = new File(dir);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
        System.out.println("Creating : " + zipFileName);
        addDir(dirObj, out);
        out.close();
    }
    static void addDir(File dirObj, ZipOutputStream out) throws IOException {
        File[] files = dirObj.listFiles();
        byte[] tmpBuf = new byte[1024];

        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                Log.d("d","files["+i+"]="+files[i]);
                addDir(files[i], out);
                continue;
            }
            FileInputStream in = new FileInputStream(files[i].getAbsolutePath());
            System.out.println(" Adding: " + files[i].getAbsolutePath().replace(FullscreenActivity.dir.toString()+"/",""));
            out.putNextEntry(new ZipEntry((files[i].getAbsolutePath()).replace(FullscreenActivity.dir.toString()+"/","")));
            int len;
            while ((len = in.read(tmpBuf)) > 0) {
                out.write(tmpBuf, 0, len);
            }
            out.closeEntry();
            in.close();
        }
    }
}
