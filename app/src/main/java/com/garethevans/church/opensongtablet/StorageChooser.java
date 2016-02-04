package com.garethevans.church.opensongtablet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class StorageChooser extends AppCompatActivity implements PopUpDirectoryChooserFragment.MyInterface {

    // Try to determine internal or external storage
	// If no preference has yet been set, prompt the user.
	//boolean intStorageExists = false;
	boolean extStorageExists = false;
	boolean defStorageExists = false;
	boolean otherStorageExists = false;
	//boolean storageIsValid = true;
	String secStorage = System.getenv("SECONDARY_STORAGE");
	String defStorage = Environment.getExternalStorageDirectory().getAbsolutePath();
	public static File customStorageLoc = Environment.getExternalStorageDirectory();
	@SuppressLint("SdCardPath")
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
	RadioGroup radioGroup;
	RadioButton intStorageButton;
	RadioButton extStorageButton;
    RadioButton otherStorageButton;
    Button changeCustom;
	File intStorCheck;
	File extStorCheck;
    File otherStorCheck;
	String numeral = "1";
	static View copyAssets;
	static View docopy;
	static View dowipe;
	File dir;
	File band;
	File various;
	private static final int requestStorage = 0;
	private View mLayout;
    public boolean storageGranted = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set the screen and title
		setContentView(R.layout.choose_storage);

        mLayout = findViewById(R.id.page);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Storage permission has not been granted.
            requestStoragePermission();

        } else {
            storageGranted = true;
        }

        Preferences.loadPreferences();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

		radioGroup = (RadioGroup) findViewById(R.id.storageOptions);
		intStorageButton = (RadioButton) findViewById(R.id.intStorage);
		extStorageButton = (RadioButton) findViewById(R.id.extStorage);
        otherStorageButton = (RadioButton) findViewById(R.id.otherStorage);
        changeCustom = (Button) findViewById(R.id.editCustomStorage);
		copyAssets = findViewById(R.id.copyProgressBar);
		docopy = findViewById(R.id.copySongs);
		dowipe = findViewById(R.id.wipeSongs);

        String text = getResources().getString(R.string.custom) + "\n(" + defStorage + ")";
		otherStorageButton.setText(text);

		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.intStorage:
                        numeral = "1";
                        break;
                    case R.id.extStorage:
                        numeral = "2";
                        break;
                    case R.id.otherStorage:
                        numeral = "3";
                        break;
                }
            }
        });

        changeCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOtherStorage();
            }
        });

		// Decide if internal and external storage storage exists
		if (secStorage!=null) {
			if (secStorage.contains(":")) {
				secStorage = secStorage.substring(0,secStorage.indexOf(":"));
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


		// If user has set their storage preference, set the appropriate radio button
		switch (FullscreenActivity.prefStorage) {
			case "int":
				intStorageButton.setChecked(true);
				extStorageButton.setChecked(false);
				otherStorageButton.setChecked(false);
				numeral = "1";
				break;
			case "ext":
				intStorageButton.setChecked(false);
				extStorageButton.setChecked(true);
				otherStorageButton.setChecked(false);
				numeral = "2";
				break;
			case "other":
				intStorageButton.setChecked(false);
				extStorageButton.setChecked(false);
				otherStorageButton.setChecked(true);
				numeral = "3";
				break;
		}

        otherStorCheck = new File(FullscreenActivity.customStorage);
        if (!FullscreenActivity.customStorage.isEmpty() && otherStorCheck.exists() && otherStorCheck.isDirectory()) {
            customStorageLoc = otherStorCheck;
            String textother = getResources().getString(R.string.custom) + "\n(" + customStorageLoc.getAbsolutePath() + ")";
            otherStorageButton.setText(textother);
            otherStorageExists = true;
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
			if (FullscreenActivity.prefStorage.equals("ext")) {
				extStorageButton.setChecked(true);
				otherStorageButton.setChecked(false);
			} else {
				extStorageButton.setChecked(false);
				otherStorageButton.setChecked(true);
			}
			intStorageButton.setAlpha(0.4f);
			String radiotext = getResources().getString(R.string.storage_int) + " - " + getResources().getString(R.string.storage_notavailable);
			intStorageButton.setText(radiotext);
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
            String inttext = getResources().getString(R.string.storage_int) + "\n(" + defStorage + "/documents)\n" + getResources().getString(R.string.storage_space) + " - " + freespace + " MB";
			intStorageButton.setText(inttext);
		}
		if (!extStorageExists || !extStorCheck.canWrite()) {
			extStorageButton.setClickable(false);
			extStorageButton.setAlpha(0.4f);
			if (FullscreenActivity.prefStorage.equals("int")) {
				intStorageButton.setChecked(true);
				otherStorageButton.setChecked(false);
			} else {
				intStorageButton.setChecked(false);
				otherStorageButton.setChecked(true);
			}
			String exttext = getResources().getString(R.string.storage_ext) + "\n" + getResources().getString(R.string.storage_notavailable);
			extStorageButton.setText(exttext);
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
            String exttext2 = getResources().getString(R.string.storage_ext) + "\n(" + secStorage + "/documents)\n" + getResources().getString(R.string.storage_space) + " - " + freespace + " MB";
			extStorageButton.setText(exttext2);
		}
	}

    public void onBackPressed() {
        if (FullscreenActivity.prefStorage.equals("ext") || FullscreenActivity.prefStorage.equals("int") || FullscreenActivity.prefStorage.equals("other")) {
            Intent intent_stop = new Intent();
            intent_stop.setClass(this, FullscreenActivity.class);
            startActivity(intent_stop);
            super.onBackPressed();
        } else {
            FullscreenActivity.myToastMessage = getResources().getString(R.string.storage_choose);
            ShowToast.showToast(StorageChooser.this);
        }
    }

    public void getOtherStorage() {
        if (storageGranted) {
            DialogFragment newFragment = PopUpDirectoryChooserFragment.newInstance();
            Bundle args = new Bundle();
            args.putString("type", "folder");
            newFragment.setArguments(args);
            newFragment.show(getFragmentManager(), "dialog");
        } else {
            requestStoragePermission();
        }
    }

	public void saveStorageLocation(View view) {
		//Rewrite the shared preference
		switch (numeral) {
			case "2":
				FullscreenActivity.prefStorage = "ext";
				break;
			case "3":
				FullscreenActivity.prefStorage = "other";
				break;
			default:
				FullscreenActivity.prefStorage = "int";
				break;
		}
		Preferences.savePreferences();
		Intent intent_stop = new Intent();
		intent_stop.setClass(this, FullscreenActivity.class);
		startActivity(intent_stop);
		finish();
	}

	public boolean deleteDirectory(File path) {
		if (storageGranted) {
            if (path.exists()) {
                File[] files = path.listFiles();
                if (files == null) {
                    return true;
                }
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        boolean dodelete = file.delete();
                        if (!dodelete) {
                            Log.d("StorageChooser", "Failed to delete file");
                        }
                    }
                }
            }
            return (path.delete());
        } else {
            requestStoragePermission();
            return false;
        }
	}

	public void wipeSongs(View view) {
        if (storageGranted) {
            // Wipe the user's song folder and reset it by calling script
            // Do an 'are you sure' prompt first
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            // Yes button clicked
                            // Go through files and delete them
                            switch (numeral) {
                                case "2":
                                    // Delete external directory
                                    if (extStorageExists) {
                                        File delPath = new File(secStorage + "/documents/OpenSong/Songs");
                                        if (delPath.exists()) {
                                            deleteDirectory(delPath);
                                        }
                                    }
                                    break;
                                case "3":
                                    // Delete custom directory
                                    if (otherStorageExists) {
                                        File delPath = new File(customStorageLoc + "/OpenSong/Songs");
                                        if (delPath.exists()) {
                                            deleteDirectory(delPath);
                                        }
                                    }
                                    break;
                                default:
                                    // Delete internal directory
                                    if (defStorageExists) {
                                        File delPath = new File(defStorage + "/documents/OpenSong/Songs");
                                        if (delPath.exists()) {
                                            deleteDirectory(delPath);
                                        }
                                    }
                                    break;
                            }

                            // Make the folder again!
                            // Easiest way is to restart the application as intent
                            switch (numeral) {
                                case "2":
                                    // Create external directory
                                    if (extStorageExists) {
                                        File createPath = new File(secStorage + "/documents/OpenSong/Songs");
                                        boolean domkdirs = createPath.mkdirs();
                                        if (!domkdirs) {
                                            Log.d("StorageChooser", "Failed to create songs folder");
                                        }

                                        // Copy Love Everlasting since folder is empty.
                                        AssetManager assetManager = getAssets();
                                        InputStream in;
                                        OutputStream out;
                                        try {
                                            String filename = "Love everlasting";
                                            in = assetManager.open("Songs" + File.separator + filename);
                                            File outFile = new File(createPath, filename);
                                            out = new FileOutputStream(outFile);
                                            copyFile(in, out);
                                            in.close();
                                            out.flush();
                                            out.close();
                                        } catch (IOException e) {
                                            Log.e("tag", "Failed to copy asset file: " + "Love everlasting", e);
                                        }
                                    }
                                    break;
                                case "3":
                                    // Create custom directory
                                    if (otherStorageExists) {
                                        File createPath = new File(customStorageLoc + "/OpenSong/Songs");
                                        boolean domkdirs = createPath.mkdirs();
                                        if (!domkdirs) {
                                            Log.d("StorageChooser", "Failed to create songs folder");
                                        }

                                        // Copy Love Everlasting since folder is empty.
                                        AssetManager assetManager = getAssets();
                                        InputStream in;
                                        OutputStream out;
                                        try {
                                            String filename = "Love everlasting";
                                            in = assetManager.open("Songs" + File.separator + filename);
                                            File outFile = new File(createPath, filename);
                                            out = new FileOutputStream(outFile);
                                            copyFile(in, out);
                                            in.close();
                                            out.flush();
                                            out.close();
                                        } catch (IOException e) {
                                            Log.e("tag", "Failed to copy asset file: " + "Love everlasting", e);
                                        }
                                    }
                                    break;
                                default:
                                    // Create internal directory
                                    if (defStorageExists) {
                                        File createPath = new File(defStorage + "/documents/OpenSong/Songs");
                                        boolean domkdirs = createPath.mkdirs();
                                        if (!domkdirs) {
                                            Log.d("StorageChooser", "Failed to create songs folder");
                                        }
                                        // Copy Love Everlasting since folder is empty.
                                        AssetManager assetManager = getAssets();
                                        InputStream in;
                                        OutputStream out;
                                        try {
                                            String filename = "Love everlasting";
                                            in = assetManager.open("Songs" + File.separator + filename);
                                            File outFile = new File(createPath, filename);
                                            out = new FileOutputStream(outFile);
                                            copyFile(in, out);
                                            in.close();
                                            out.flush();
                                            out.close();
                                        } catch (IOException e) {
                                            Log.e("tag", "Failed to copy asset file: " + "Love everlasting", e);
                                        }
                                    }
                                    break;
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
        } else {
            requestStoragePermission();
        }
	}

	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}

	public void copymyassets(View view) {
        if (storageGranted) {
            copyAssets.setVisibility(View.VISIBLE);
            FullscreenActivity.myToastMessage = getResources().getString(R.string.installingsongs);
            ShowToast.showToast(StorageChooser.this);

            // Do the stuff async to stop the app slowing down
            CopyAssetsTask task = new CopyAssetsTask();
            task.execute();
        } else {
            requestStoragePermission();
        }
	}

	@Override
	public void updateCustomStorage() {
		// Called from the popup custom directory fragment saving
        if (customStorageLoc.exists() && customStorageLoc.isDirectory() && customStorageLoc.canWrite()) {
            FullscreenActivity.customStorage = customStorageLoc.getAbsolutePath();
            String textother = getResources().getString(R.string.custom) + "\n(" + customStorageLoc.getAbsolutePath() + ")";
            otherStorageButton.setText(textother);
            otherStorageExists = true;
        }
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
			// Check the Band Directory exists
			// External
			switch (numeral) {
				case "2":
					if (extStorCheck.exists()) {
						band = new File(secStorage + "/documents/OpenSong/Songs/Band");
						various = new File(secStorage + "/documents/OpenSong/Songs/Various");
						if (!band.exists()) {
							boolean domkdirs = band.mkdirs();
							if (!domkdirs) {
								Log.d("StorageChooser", "Failed to create Band folder");
							}
						}
						if (!various.exists()) {
							boolean domkdirs = various.mkdirs();
							if (!domkdirs) {
								Log.d("StorageChooser", "Failed to create Various folder");
							}
						}
						dir = new File(secStorage + "/documents/OpenSong/Songs");
					}
					break;
				case "3":
					if (otherStorCheck.exists()) {
						band = new File(customStorageLoc + "/OpenSong/Songs/Band");
						various = new File(customStorageLoc + "/OpenSong/Songs/Various");
						if (!band.exists()) {
							boolean domkdirs = band.mkdirs();
							if (!domkdirs) {
								Log.d("StorageChooser", "Failed to create Band folder");
							}
						}
						if (!various.exists()) {
							boolean domkdirs = various.mkdirs();
							if (!domkdirs) {
								Log.d("StorageChooser", "Failed to create Various folder");
							}
						}
						dir = new File(customStorageLoc + "/OpenSong/Songs");
					}
					break;
				default:
					// Internal
					if (intStorCheck.exists()) {
						band = new File(defStorage + "/documents/OpenSong/Songs/Band");
						various = new File(defStorage + "/documents/OpenSong/Songs/Various");
						if (!band.exists()) {
							boolean domkdirs = band.mkdirs();
							if (!domkdirs) {
								Log.d("StorageChooser", "Failed to create Band folder");
							}
						}
						if (!various.exists()) {
							boolean domkdirs = various.mkdirs();
							if (!domkdirs) {
								Log.d("StorageChooser", "Failed to create Various folder");
							}
						}
						dir = new File(defStorage + "/documents/OpenSong/Songs");
					}
					break;
			}

			AssetManager assetManager = getAssets();
			String[] files = null;
			try {
				files = assetManager.list("Songs");
			} catch (IOException e) {
				Log.e("tag", "Failed to get asset file list.", e);
			}

            if (files!=null) {
                for (String filename : files) {
                    InputStream in;
                    OutputStream out;
                    try {
                        File checkFile = new File(dir + "/" + filename);
                        if (!checkFile.exists()) {
                            in = assetManager.open("Songs" + File.separator + filename);
                            File outFile = new File(dir, filename);
                            out = new FileOutputStream(outFile);
                            copyFile(in, out);
                            in.close();
                            out.flush();
                            out.close();
                        }
                    } catch (IOException e) {
                        Log.e("tag", "Failed to copy asset file: " + filename, e);
                    }
                }
            }

			String[] files_band = null;
			try {
				files_band = assetManager.list("Songs"+File.separator+"Band");
			} catch (IOException e) {
				Log.e("tag", "Failed to get asset file list.", e);
			}

            if (files_band!=null) {
                for (String filename : files_band) {
                    InputStream in;
                    OutputStream out;
                    try {
                        File checkFile = new File(dir + "/Band/" + filename);
                        if (!checkFile.exists()) {
                            in = assetManager.open("Songs" + File.separator + "Band" + File.separator + filename);
                            File outFile = new File(band, filename);
                            out = new FileOutputStream(outFile);
                            copyFile(in, out);
                            in.close();
                            out.flush();
                            out.close();
                        }
                    } catch (IOException e) {
                        Log.e("tag", "Failed to copy asset file: " + filename, e);
                    }
                }
            }

			String[] files_various = null;
			try {
				files_various = assetManager.list("Songs"+File.separator+"Various");
			} catch (IOException e) {
				Log.e("tag", "Failed to get asset file list.", e);
			}

            if (files_various!=null) {
                for (String filename : files_various) {
                    InputStream in;
                    OutputStream out;
                    try {
                        File checkFile = new File(dir + "/Various/" + filename);
                        if (!checkFile.exists()) {
                            in = assetManager.open("Songs" + File.separator + "Various" + File.separator + filename);
                            File outFile = new File(various, filename);
                            out = new FileOutputStream(outFile);
                            copyFile(in, out);
                            in.close();
                            out.flush();
                            out.close();
                        }
                    } catch (IOException e) {
                        Log.e("tag", "Failed to copy asset file: " + filename, e);
                    }
                }
            }
			return null;
		}
	}

    // The permission requests
    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Snackbar.make(mLayout, R.string.storage_rationale,
                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(StorageChooser.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestStorage);
                }
            }).show();
        } else {
            // Storage permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestStorage);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == requestStorage) {
            storageGranted = grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            Log.d("d", "onRequestPermissionResult\nstorageGranted=" + storageGranted);

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public static void createDirectory(File folder) {
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                Log.d("e","Error creating directory - "+folder);
            }
        }
    }
}