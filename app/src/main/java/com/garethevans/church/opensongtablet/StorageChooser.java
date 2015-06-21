package com.garethevans.church.opensongtablet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class StorageChooser extends Activity {

	// Try to determine internal or external storage
	// If no preference has yet been set, prompt the user.
	boolean intStorageExists = false;
	boolean extStorageExists = false;
	boolean defStorageExists = false;
	boolean storageIsValid = true;
	String secStorage = System.getenv("SECONDARY_STORAGE");
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
	String defStorage = Environment.getExternalStorageDirectory().getAbsolutePath().toString();
	RadioGroup radioGroup;
	RadioButton intStorageButton;
	RadioButton extStorageButton;
	File intStorCheck;
	File extStorCheck;
	String numeral = "1";
	static View copyAssets;
	static View docopy;
	static View dowipe;
	File dir;
	File band;
	File various;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set the screen and title
		setContentView(R.layout.choose_storage);

		Preferences.loadPreferences();

		getActionBar().setTitle(getResources().getString(R.string.app_name));
		radioGroup = (RadioGroup) findViewById(R.id.storageOptions);
		intStorageButton = (RadioButton) findViewById(R.id.intStorage);
		extStorageButton = (RadioButton) findViewById(R.id.extStorage);
		copyAssets = (ProgressBar) findViewById(R.id.copyProgressBar);
		docopy = (Button) findViewById(R.id.copySongs);
		dowipe = (Button) findViewById(R.id.wipeSongs);

		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				switch (checkedId) {
				case R.id.intStorage:
					numeral = "1";
					break;
				case R.id.extStorage:
					numeral = "2";
					break;
				}
			}
		});

		// Decide if internal and external storage storage exists
		if (secStorage!=null) {
			if (secStorage.contains(":")) {
				secStorage = secStorage.substring(0,secStorage.indexOf(":"));
			}
			if (secStorage.contains("storage")) {
				// Valid external SD card directory
			}

		} else {
			// Lets look for alternative secondary storage positions
			for (int z=0;z<secStorageOptions.length;z++) {
				File testaltsecstorage = new File(secStorageOptions[z]);
				if (testaltsecstorage.exists() && testaltsecstorage.canWrite()) {
					secStorage = secStorageOptions[z];
				}
			}
		}

		// If secondary and default storage are the same thing, hide secStorage
		if (defStorage.equals(secStorage)) {
			secStorage = null;
		}


		// If user has set their storage preference, set the appropriate radio button
		if (FullscreenActivity.prefStorage.equals("int")) {
			intStorageButton.setChecked(true);
			extStorageButton.setChecked(false);
			numeral = "1";
		} else if (FullscreenActivity.prefStorage.equals("ext")) {
			intStorageButton.setChecked(false);
			extStorageButton.setChecked(true);
			numeral = "2";
		}

		// If external storage isn't found, disable this radiobutton
		intStorCheck = new File(defStorage);
		if (intStorCheck.exists()) {
			defStorageExists = true;
		}
		if (secStorage!=null) {
			extStorCheck = new File(secStorage);
			if (extStorCheck.exists()) {
				extStorageExists = true;
			}
		}

		if (!defStorageExists) {
			intStorageButton.setClickable(false);
			intStorageButton.setChecked(false);
			extStorageButton.setChecked(true);
			intStorageButton.setAlpha(0.4f);
			intStorageButton.setText(getResources().getString(R.string.storage_int) + " - " + getResources().getString(R.string.storage_notavailable));
		} else {
			// Try to get free space
			String freespace = "?";
			if (intStorCheck.exists()) {
				long temp = intStorCheck.getFreeSpace();
				if (temp>0) {
					int num = (int) ((float)temp/(float)1000000);
					freespace = "" + num;
				}
			}
			intStorageButton.setText(getResources().getString(R.string.storage_int) + "\n(" + defStorage + "/documents/OpenSong)\n" + getResources().getString(R.string.storage_space) + " - " + freespace + " MB");
		}
		if (!extStorageExists || !extStorCheck.canWrite()) {
			extStorageButton.setClickable(false);
			extStorageButton.setAlpha(0.4f);
			extStorageButton.setChecked(false);
			intStorageButton.setChecked(true);
			extStorageButton.setText(getResources().getString(R.string.storage_ext) + "\n" + getResources().getString(R.string.storage_notavailable));
		} else {
			// Try to get free space
			String freespace = "?";
			if (extStorCheck.exists()) {
				long temp = extStorCheck.getFreeSpace();
				if (temp>0) {
					int num = (int) ((float)temp/(float)1000000);
					freespace = "" + num;
				}
			}
			extStorageButton.setText(getResources().getString(R.string.storage_ext) + "\n(" + secStorage + "/documents/OpenSong)\n" + getResources().getString(R.string.storage_space) + " - " + freespace + " MB");
		}
	}	

	public void onBackPressed() {
        if (FullscreenActivity.prefStorage.equals("ext") || FullscreenActivity.prefStorage.equals("int")) {
            Intent intent_stop = new Intent();
            intent_stop.setClass(this, FullscreenActivity.class);
            startActivity(intent_stop);
            super.onBackPressed();
        } else {
            FullscreenActivity.myToastMessage = getResources().getString(R.string.storage_choose);
            ShowToast.showToast(StorageChooser.this);
        }
    }

	public void saveStorageLocation (View view) {
		//Rewrite the shared preference
		if (numeral.equals("2")) {
			FullscreenActivity.prefStorage = "ext";
		} else {
			FullscreenActivity.prefStorage = "int";
		}
		Preferences.savePreferences();
		Intent intent_stop = new Intent();
		intent_stop.setClass(this, FullscreenActivity.class);
		startActivity(intent_stop);
		finish();
	}

	public static boolean deleteDirectory(File path) {
		if(path.exists() ) {
			File[] files = path.listFiles();
			if (files == null) {
				return true;
			}
			for(int i=0; i<files.length; i++) {
				if(files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return( path.delete() );
	}

	public void wipeSongs(View view) {
		// Wipe the user's song folder and reset it by calling script
		// Do an 'are you sure' prompt first
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					// Yes button clicked
					// Go through files and delete them
					if (numeral.equals("2")) {
						// Delete external directory
						if (extStorageExists) {
							File delPath = new File(secStorage + "/documents/OpenSong/Songs");
							if (delPath.exists()) {
								deleteDirectory(delPath);
							}
						}
					} else {
						// Delete internal directory
						if (defStorageExists) {
							File delPath = new File(defStorage + "/documents/OpenSong/Songs");
							if (delPath.exists()) {
								deleteDirectory(delPath);
							}
						}
					}

					// Make the folder again!
					// Easiest way is to restart the application as intent
					if (numeral.equals("2")) {
						// Create external directory
						if (extStorageExists) {
							File createPath = new File(secStorage + "/documents/OpenSong/Songs");
							createPath.mkdirs();
							// Copy Love Everlasting since folder is empty.
							AssetManager assetManager = getAssets();
							InputStream in = null;
							OutputStream out = null;
							try {
								String filename = "Love everlasting";
								in = assetManager.open("Songs"+File.separator+filename);
								File outFile = new File(createPath, filename);
								out = new FileOutputStream(outFile);
								copyFile(in, out);
								in.close();
								in = null;
								out.flush();
								out.close();
								out = null;
							} catch (IOException e) {
								Log.e("tag","Failed to copy asset file: " + "Love everlasting", e);
							}
						}
					} else {
						// Create internal directory
						if (defStorageExists) {
							File createPath = new File(defStorage + "/documents/OpenSong/Songs");
							createPath.mkdirs();
							// Copy Love Everlasting since folder is empty.
							AssetManager assetManager = getAssets();
							InputStream in = null;
							OutputStream out = null;
							try {
								String filename = "Love everlasting";
								in = assetManager.open("Songs"+File.separator+filename);
								File outFile = new File(createPath, filename);
								out = new FileOutputStream(outFile);
								copyFile(in, out);
								in.close();
								in = null;
								out.flush();
								out.close();
								out = null;
							} catch (IOException e) {
								Log.e("tag","Failed to copy asset file: " + "Love everlasting", e);
							}
						}
					}
					// Tell them that the job has been done
					FullscreenActivity.myToastMessage = getResources().getString(R.string.songfoldercreate);
					ShowToast.showToast(StorageChooser.this);
					saveStorageLocation(dowipe);
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					// No button clicked
					break;
				}


			}
		};
		AlertDialog.Builder builder = new AlertDialog.Builder(StorageChooser.this);
		builder.setMessage(
				getResources().getString(R.string.areyousure))
				.setPositiveButton(
						getResources().getString(R.string.yes),
						dialogClickListener)
						.setNegativeButton(
								getResources().getString(R.string.no),
								dialogClickListener).show();

	}

	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}

	public void copymyassets(View view) {
		copyAssets.setVisibility(View.VISIBLE);
		FullscreenActivity.myToastMessage = getResources().getString(R.string.installingsongs);
		ShowToast.showToast(StorageChooser.this);

		// Do the stuff async to stop the app slowing down
		CopyAssetsTask task = new CopyAssetsTask();
		task.execute();
	}

	private class CopyAssetsTask extends AsyncTask<String, Void, String> {
		@Override
		protected void onPostExecute(String result)  {
			copyAssets.setVisibility(View.INVISIBLE);

			FullscreenActivity.myToastMessage = getResources().getString(R.string.assetcopydone);
			ShowToast.showToast(StorageChooser.this);
			saveStorageLocation(docopy);
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			// Check the Band Directory exists
			// External
			if (numeral.equals("2")) {
				if (extStorCheck.exists()) {
					band = new File(secStorage+"/documents/OpenSong/Songs/Band");
					various = new File(secStorage+"/documents/OpenSong/Songs/Various");
					if (!band.exists()) {
						band.mkdirs();
					}
					if (!various.exists()) {
						various.mkdirs();
					}
					dir = new File(secStorage+"/documents/OpenSong/Songs"); 
				}
			} else {
				// Internal
				if (intStorCheck.exists()) {
					band = new File(defStorage+"/documents/OpenSong/Songs/Band");
					various = new File(defStorage+"/documents/OpenSong/Songs/Various");
					if (!band.exists()) {
						band.mkdirs();
					}
					if (!various.exists()) {
						various.mkdirs();
					}
					dir = new File(defStorage+"/documents/OpenSong/Songs"); 
				}
			}

			AssetManager assetManager = getAssets();
			String[] files = null;
			try {
				files = assetManager.list("Songs");
			} catch (IOException e) {
				Log.e("tag", "Failed to get asset file list.", e);
			}

			for (String filename : files) {
				InputStream in = null;
				OutputStream out = null;
				try {
                    File checkFile = new File(dir+"/"+filename);
                    if (!checkFile.exists()) {
                        in = assetManager.open("Songs" + File.separator + filename);
                        File outFile = new File(dir, filename);
                        out = new FileOutputStream(outFile);
                        copyFile(in, out);
                        in.close();
                        in = null;
                        out.flush();
                        out.close();
                        out = null;
                    }
				} catch (IOException e) {
					Log.e("tag", "Failed to copy asset file: " + filename, e);
				}
			}

			String[] files_band = null;
			try {
				files_band = assetManager.list("Songs"+File.separator+"Band");
			} catch (IOException e) {
				Log.e("tag", "Failed to get asset file list.", e);
			}

			for (String filename : files_band) {
				InputStream in = null;
				OutputStream out = null;
				try {
                    File checkFile = new File(dir+"/Band/"+filename);
                    if (!checkFile.exists()) {
                        in = assetManager.open("Songs" + File.separator + "Band" + File.separator + filename);
                        File outFile = new File(band, filename);
                        out = new FileOutputStream(outFile);
                        copyFile(in, out);
                        in.close();
                        in = null;
                        out.flush();
                        out.close();
                        out = null;
                    }
				} catch (IOException e) {
					Log.e("tag", "Failed to copy asset file: " + filename, e);
				}
			}

			String[] files_various = null;
			try {
				files_various = assetManager.list("Songs"+File.separator+"Various");
			} catch (IOException e) {
				Log.e("tag", "Failed to get asset file list.", e);
			}

			for (String filename : files_various) {
				InputStream in = null;
				OutputStream out = null;
				try {
                    File checkFile = new File(dir+"/Various/"+filename);
                    if (!checkFile.exists()) {
                        in = assetManager.open("Songs" + File.separator + "Various" + File.separator + filename);
                        File outFile = new File(various, filename);
                        out = new FileOutputStream(outFile);
                        copyFile(in, out);
                        in.close();
                        in = null;
                        out.flush();
                        out.close();
                        out = null;
                    }
				} catch (IOException e) {
					Log.e("tag", "Failed to copy asset file: " + filename, e);
				}
			}
			return null;
		}
	}
}