package com.garethevans.church.opensongtablet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class SettingsActivity extends Activity {
	// This class covers the splash screen and main settings page
	// Users then have the option to move into the FullscreenActivity

	// Let's define the variables needed for the Settings Page.
	Handler delayfadeinredraw;
	//View importOnSong;
	SQLiteDatabase db;
	//String[] backUpFiles;
	//String backupchosen = "";
	File[] int_myFiles;
	File[] ext_myFiles;
	//File[] mySets;
	int int_numofsongfiles;
	int ext_numofsongfiles;
	int numofsetfiles;
	int showSplashVersion; // Show splash on start up first time only if lower than current version

	static int version;
	static SharedPreferences myPreferences;
	static String prefStorage;

	// Try to determine internal or external storage
	String secStorage = System.getenv("SECONDARY_STORAGE");
	String defStorage = Environment.getExternalStorageDirectory().getAbsolutePath();
	String[] secStorageOptions = {"/mnt/emmc/",
			"/FAT",
			"/Removable/MicroSD",
			"/Removable/SD",
			"/data/sdext2",
			"/sdcard/sd",
			"/mnt/flash",
			"/mnt/sdcard/tflash",
			"/mnt/nand",
			"/mnt/external1",
			"/mnt/sdcard-ext",
			"/mnt/extsd",
			"/mnt/sdcard2",
			"/mnt/sdcard/sdcard1",
			"/mnt/sdcard/sdcard2",
			"/mnt/sdcard/ext_sd",
			"/mnt/sdcard/_ExternalSD",
			"/mnt/sdcard/external_sd",
			"/mnt/sdcard/SD_CARD",
			"/mnt/sdcard/removable_sdcard",
			"/mnt/sdcard/external_sdcard",	 
			"/mnt/sdcard/extStorages/SdCard",
			"/mnt/ext_card",
			"/mnt/extern_sd",
			"/mnt/ext_sdcard",
			"/mnt/ext_sd",
			"/mnt/external_sd",
			"/mnt/external_sdcard",
	"/mnt/extSdCard"};
	boolean intStorageExists = false;
	boolean extStorageExists = false;
	boolean defStorageExists = false;

	boolean storageIsValid = true;

	File int_root = android.os.Environment.getExternalStorageDirectory();
	File int_dir = new File(int_root.getAbsolutePath() + "/documents/OpenSong/Songs");
	File int_homedir = new File(int_root.getAbsolutePath() + "/documents/OpenSong");
	File int_dir_band = new File(int_root.getAbsolutePath() + "/documents/OpenSong/Songs/Band");
	File int_dir_various = new File(int_root.getAbsolutePath() + "/documents/OpenSong/Songs/Various");
	File int_dirsets = new File(int_root.getAbsolutePath() + "/documents/OpenSong/Sets");
	File int_dirpads = new File(int_root.getAbsolutePath() + "/documents/OpenSong/Pads");
	File int_dirbgs = new File(int_root.getAbsolutePath() + "/documents/OpenSong/Backgrounds");
	File int_dironsong = new File(int_root.getAbsolutePath() + "/documents/OpenSong/Songs/OnSong");
	File int_defbgimage = new File(int_root.getAbsolutePath() + "/documents/OpenSong/Backgrounds/ost_bg.png");
	File int_deflogoimage = new File(int_root.getAbsolutePath() + "/documents/OpenSong/Backgrounds/ost_logo.png");
	File int_dirbibles = new File(int_root.getAbsolutePath() + "/documents/OpenSong/OpenSong Scripture");
	File int_dirbibleverses = new File(int_root.getAbsolutePath() + "/documents/OpenSong/OpenSong Scripture/_cache");
	File int_dircustomslides = new File(int_root.getAbsolutePath() + "/documents/OpenSong/Slides/_cache");
	File int_dircustomnotes = new File(int_root.getAbsolutePath() + "/documents/OpenSong/Notes/_cache");

	File ext_root;
	File ext_dir;
	File ext_homedir;
	File ext_dir_band;
	File ext_dir_various;
	File ext_dirsets;
	File ext_dirpads;
	File ext_dirbgs;
	File ext_dironsong;
	File ext_defbgimage;
	File ext_deflogoimage;
	File ext_dirbibles;
	File ext_dirbibleverses;
	File ext_dircustomslides;
	File ext_dircustomnotes;


	// This class is called when the application first opens.
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		version = 0;		
		// Decide if user has already seen the splash screen
		PackageInfo pInfo;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			version = pInfo.versionCode;
		} catch (NameNotFoundException e1) {
			e1.printStackTrace();
		}
		myPreferences = getSharedPreferences("mysettings",MODE_PRIVATE);
		showSplashVersion = myPreferences.getInt("showSplashVersion", version);

		if (secStorage!=null) {
			if (secStorage.contains(":")) {
				secStorage = secStorage.substring(0,secStorage.indexOf(":"));
			}
			if (secStorage.contains("storage")) {
				// Valid external SD card directory
			}
		} else {
			// Lets look for alternative secondary storage positions
			for (String secStorageOption : secStorageOptions) {
				File testaltsecstorage = new File(secStorageOption);
				if (testaltsecstorage.exists() && testaltsecstorage.canWrite()) {
					secStorage = secStorageOption;
				}
			}
		}

		// If secondary and default storage are the same thing, hide secStorage
		if (defStorage.equals(secStorage)) {
			secStorage = null;
		}

		// I want folders to be ready on internal and external storage (if available)
		File intStorCheck = new File(defStorage);
		if (intStorCheck.exists()) {
			defStorageExists = true;
		}
		if (secStorage!=null) {
			File extStorCheck = new File(secStorage);
			if (extStorCheck.exists() && extStorCheck.canWrite()) {
				extStorageExists = true;
				ext_root = extStorCheck;
				ext_dir = new File(ext_root.getAbsolutePath() + "/documents/OpenSong/Songs");
				ext_homedir = new File(ext_root.getAbsolutePath() + "/documents/OpenSong");
				ext_dir_band = new File(ext_root.getAbsolutePath() + "/documents/OpenSong/Songs/Band");
				ext_dir_various = new File(ext_root.getAbsolutePath() + "/documents/OpenSong/Songs/Various");
				ext_dirsets = new File(ext_root.getAbsolutePath() + "/documents/OpenSong/Sets");
				ext_dirpads = new File(ext_root.getAbsolutePath() + "/documents/OpenSong/Pads");
				ext_dirbgs = new File(ext_root.getAbsolutePath() + "/documents/OpenSong/Backgrounds");
				ext_dironsong = new File(ext_root.getAbsolutePath() + "/documents/OpenSong/Songs/OnSong");
				ext_defbgimage = new File(ext_root.getAbsolutePath() + "/documents/OpenSong/Backgrounds/ost_bg.png");
				ext_deflogoimage = new File(ext_root.getAbsolutePath() + "/documents/OpenSong/Backgrounds/ost_logo.png");
				ext_dirbibles = new File(ext_root.getAbsolutePath() + "/documents/OpenSong/OpenSong Scripture");
				ext_dirbibleverses = new File(ext_root.getAbsolutePath() + "/documents/OpenSong/OpenSong Scripture/_cache");
				ext_dircustomslides = new File(ext_root.getAbsolutePath() + "/documents/OpenSong/Slides/_cache");
				ext_dircustomnotes = new File(ext_root.getAbsolutePath() + "/documents/OpenSong/Notes/_cache");
			}
		}

		setContentView(R.layout.activity_logosplash);		

		// Check the OpenSong Songs Directory exists
		// Internal
		if (defStorageExists) {if (!int_dir.exists()) {int_dir.mkdirs();}}
		// External
		if (extStorageExists) {if (!ext_dir.exists()) {ext_dir.mkdirs();}}

		// Sets directory
		// Internal
		if (defStorageExists) {if (!int_dirsets.exists()) {int_dirsets.mkdirs();}}
		// External
		if (extStorageExists) {if (!ext_dirsets.exists()) {ext_dirsets.mkdirs();}}

		// Pads directory
		// Internal
		if (defStorageExists) {if (!int_dirpads.exists()) {int_dirpads.mkdirs();}}
		// External
		if (extStorageExists) {if (!ext_dirpads.exists()) {ext_dirpads.mkdirs();}}


		// Scripture/Bible directory
		// Internal
		if (defStorageExists) {if (!int_dirbibles.exists()) {int_dirbibles.mkdirs();}}
		// External
		if (extStorageExists) {if (!ext_dirbibles.exists()) {ext_dirbibles.mkdirs();}}


		// Check the OpenSong Scripture _cache Directory exists
		// Internal
		if (defStorageExists) {if (!int_dirbibleverses.exists()) {int_dirbibleverses.mkdirs();}}
		// External
		if (extStorageExists) {if (!ext_dirbibleverses.exists()) {ext_dirbibleverses.mkdirs();}}

		// Check the Slides _cache Directory exists
		// Internal
		if (defStorageExists) {if (!int_dircustomslides.exists()) {int_dircustomslides.mkdirs();}}
		// External
		if (extStorageExists) {if (!ext_dircustomslides.exists()) {ext_dircustomslides.mkdirs();}}

		// Check the Notes _cache Directory exists
		// Internal
		if (defStorageExists) {if (!int_dircustomnotes.exists()) {int_dircustomnotes.mkdirs();}}
		// External
		if (extStorageExists) {if (!ext_dircustomnotes.exists()) {ext_dircustomnotes.mkdirs();}}

		// Check the OpenSong Backgrounds Directory exists
		// Internal
		if (defStorageExists) {if (!int_dirbgs.exists()) {int_dirbgs.mkdirs();}}
		// External
		if (extStorageExists) {if (!ext_dirbgs.exists()) {ext_dirbgs.mkdirs();}}

		// Look for default background image
		AssetManager assetManager_bg = getAssets();
		InputStream in;
		OutputStream out;
		String filename = "ost_bg.png";
		// Internal
		if (defStorageExists) {
			if (!int_defbgimage.exists()) {
				try {
					in = assetManager_bg.open("backgrounds/"+filename);
					File outFile = new File(int_dirbgs, filename);
					out = new FileOutputStream(outFile);
					copyFile(in, out);
					in.close();
					in = null;
					out.flush();
					out.close();
					out = null;
				} catch (IOException e) {
					Log.e("tag","Failed to copy asset file: " + "ost_bg.png", e);
				}
			}	
		}
		// External
		if (extStorageExists) {
			if (!ext_defbgimage.exists()) {
				try {
					in = assetManager_bg.open("backgrounds/"+filename);
					File outFile = new File(ext_dirbgs, filename);
					out = new FileOutputStream(outFile);
					copyFile(in, out);
					in.close();
					in = null;
					out.flush();
					out.close();
					out = null;
				} catch (IOException e) {
					Log.e("tag","Failed to copy asset file: " + "ost_bg.png", e);
				}
			}
		}

		// Look for default logo image
		AssetManager assetManager_logo = getAssets();
		InputStream in_logo;
		OutputStream out_logo;
		String filename_logo = "ost_logo.png";
		// Internal
		if (defStorageExists) {
			if (!int_deflogoimage.exists()) {
				try {
					in_logo = assetManager_logo.open("backgrounds/"+filename_logo);
					File outFileLogo = new File(int_dirbgs, filename_logo);
					out_logo = new FileOutputStream(outFileLogo);
					copyFile(in_logo, out_logo);
					in_logo.close();
					in_logo = null;
					out_logo.flush();
					out_logo.close();
					out_logo = null;
				} catch (IOException e) {
					Log.e("tag","Failed to copy asset file: " + "ost_logo.png", e);
				}
			}
		}
		// External
		if (extStorageExists) {
			if (!ext_deflogoimage.exists()) {
				try {
					in_logo = assetManager_logo.open("backgrounds/"+filename_logo);
					File outFileLogo = new File(ext_dirbgs, filename_logo);
					out_logo = new FileOutputStream(outFileLogo);
					copyFile(in_logo, out_logo);
					in_logo.close();
					in_logo = null;
					out_logo.flush();
					out_logo.close();
					out_logo = null;
				} catch (IOException e) {
					Log.e("tag","Failed to copy asset file: " + "ost_logo.png", e);
				}
			}
		}

		AssetManager assetManager_song = getAssets();
		InputStream in_song;
		OutputStream out_song;
		String filename_song = "Love everlasting";
		if (defStorageExists) {
			int_myFiles = int_dir.listFiles();
			if (int_myFiles != null && int_myFiles.length>0) {
				int_numofsongfiles = int_myFiles.length;			
			} else {
				int_numofsongfiles = 0;
			}
			if (int_numofsongfiles <= 0) {
				// Copy Love Everlasting since folder is empty.
				try {
					in_song = assetManager_song.open("Songs"+File.separator+filename_song);		
					File outFile_song = new File(int_dir, filename_song);
					out_song = new FileOutputStream(outFile_song);
					copyFile(in_song, out_song);
					in_song.close();
					in_song = null;
					out_song.flush();
					out_song.close();
					out = null;
				} catch (IOException e) {
					Log.e("tag","Failed to copy asset file: " + "Love everlasting", e);
				}
			}
		}


		if (extStorageExists) {
			ext_myFiles = ext_dir.listFiles();
			if (ext_myFiles != null && ext_myFiles.length>0) {
				ext_numofsongfiles = ext_myFiles.length;			
			} else {
				ext_numofsongfiles = 0;
			}
			if (ext_numofsongfiles <= 0) {
				// Copy Love Everlasting since folder is empty.
				try {
					in_song = assetManager_song.open("Songs"+File.separator+filename_song);		
					File outFile_song = new File(ext_dir, filename_song);
					out_song = new FileOutputStream(outFile_song);
					copyFile(in_song, out_song);
					in_song.close();
					in_song = null;
					out_song.flush();
					out_song.close();
					out = null;
				} catch (IOException e) {
					Log.e("tag",
							"Failed to copy asset file: " + "Love everlasting", e);
				}
			}
		}

		// Wait 2000ms before either showing the introduction page or the main app
		delayfadeinredraw = new Handler();
		delayfadeinredraw.postDelayed(new Runnable() {
			@Override
			public void run() {
				// This bit then redirects the user to the main app if they've got the newest version
				// This now happens AFTER a file/folder check has happened!
				if (showSplashVersion>version) {
					//User version is bigger than current - this means they've seen the splash
					showSplashVersion = version+1;
					//Rewrite the shared preference
					Editor editor = myPreferences.edit();
					editor.putInt("showSplashVersion", showSplashVersion);
					editor.apply();
					gotothesongs(null);
					return;
				} else {
					//Set the showSplashVersion to the next level - it will only show on next update
					showSplashVersion = version+1;
					//Rewrite the shared preference
					Editor editor = myPreferences.edit();
					editor.putInt("showSplashVersion", showSplashVersion);
					editor.apply();
				}
				setContentView(R.layout.activity_splashscreen);

				PackageInfo pinfo = null;
				try {
					pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
				} catch (NameNotFoundException e1) {
					e1.printStackTrace();
				}
				int versionNumber = pinfo.versionCode;
				String versionName = pinfo.versionName;

				TextView showVersion = (TextView) findViewById(R.id.version);
				showVersion.setText("V"+versionName+" ("+versionNumber+")");

			}
		}, 2000); // 2000ms delay

	}

	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}


	public void gotothesongs(View view) {
		Intent intent = new Intent();
		intent.setClass(this, FullscreenActivity.class);
		startActivity(intent);
		finish();
	}

	public void webLink(View view) {
		String url = "http://www.opensong.org";
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i);
	}

	public void latestUpdates(View view) {
		String url = "https://sites.google.com/site/opensongtabletmusicviewer/latest-updates";
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i);
	}

	public void dropSync(View view) {
		String url = "https://play.google.com/store/apps/details?id=com.ttxapps.dropsync";
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i);

	}

}