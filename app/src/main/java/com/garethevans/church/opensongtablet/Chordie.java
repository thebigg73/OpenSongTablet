package com.garethevans.church.opensongtablet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Chordie extends Activity{
	
	@SuppressWarnings("unused")
	private Menu menu;
	static String foldername;
	static String result;
	static String response;
	static String weblink;
	static String webSource;
	static WebView chordieWeb;
	static String resultposted;
	static String whatfolderselected="(MAIN)";
	ProgressBar progressbar;
	static String[] availableFolders;
	AlertDialog.Builder dialogBuilder;
	String filenametosave;
	String resultfinal;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		Bundle bundle = getIntent().getExtras();
		String thissearch = bundle.getString("thissearch");
		String engine = bundle.getString("engine");
		
		if (engine.equals("chordie")) {
			weblink = "http://www.chordie.com/?np=0&ps=100&wf=531&ul=.pro&ul=.cho&ul=.crd&ul=.tab&chordie=ref&q="+thissearch;
		} else if (engine.equals("ultimate-guitar")) {
			weblink = "http://www.ultimate-guitar.com/search.php?page=1&tab_type_group=text&app_name=ugt&order=myweight&type=300&title="+thissearch;
		}
		

		setContentView(R.layout.chordie_preview);
		
		chordieWeb = (WebView) findViewById(R.id.webView1);
		
		chordieWeb.setWebViewClient(new WebViewClient() {
	        @Override
	        public boolean shouldOverrideUrlLoading(WebView view, String url) {
	            return false;
	        }
	    });
		chordieWeb.getSettings().getJavaScriptEnabled();
		chordieWeb.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		chordieWeb.loadUrl(weblink);
	}
	
	@Override
	public void onBackPressed() {
		// Make the back button go back in the Browser history
		chordieWeb.goBack();
	}

	public void closesearch(View view) {		
		Intent viewsong = new Intent(this, FullscreenActivity.class);
		startActivity(viewsong);
		finish();
		return;
	}
	
	
	
	public void grabchordpro(View view) {
		// Need to run a async task to grab html text
		progressbar = (ProgressBar) findViewById(R.id.progressbar);
		progressbar.setVisibility(View.VISIBLE);
		WebView checkChordieWeb = (WebView) findViewById(R.id.webView1);
		weblink = checkChordieWeb.getUrl();
		String message = getResources().getText(R.string.chordproprogress).toString();
		Toast toast = Toast.makeText(Chordie.this,message, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
		
		DownloadWebPageTask task = new DownloadWebPageTask();
	    task.execute(new String[] { weblink });
	}

	 private class DownloadWebPageTask extends AsyncTask<String, Void, String> {
		    @Override
		    protected String doInBackground(String... urls) {
		      response = "";
		      for (@SuppressWarnings("unused") String url : urls) {
		        DefaultHttpClient client = new DefaultHttpClient();
		        HttpGet httpGet = new HttpGet(weblink);
		        try {
		          HttpResponse execute = client.execute(httpGet);
		          InputStream content = execute.getEntity().getContent();

		          BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
		          String s = "";
		          while ((s = buffer.readLine()) != null) {
		            response += "\n" + s;
		          }

		        } catch (Exception e) {
		          e.printStackTrace();
		        }
		      }
		      return response;
		    }

		    public void makeNewFolder() {
		    	AlertDialog.Builder alert = new AlertDialog.Builder(Chordie.this);

		    	alert.setTitle(getResources().getText(R.string.newfolder).toString());
		    	alert.setMessage(getResources().getText(R.string.newfoldername).toString());

		    	// Set an EditText view to get user input 
		    	final EditText input = new EditText(Chordie.this);
		    	alert.setView(input);

		    	alert.setPositiveButton(getResources().getText(R.string.ok).toString(), new DialogInterface.OnClickListener() {
		    	@Override
				public void onClick(DialogInterface dialog, int whichButton) {
		    	  String value = input.getText().toString();
		    	  // Check if folder already exists
		    	  int folderexists = -1;
		    	  for (int z=0;z<availableFolders.length;z++) {
		    		  if (availableFolders[z].equals(value)) {
			    		// Folder already exists.  Tell user and return
		    			Toast toasterror = Toast.makeText(Chordie.this,getResources().getText(R.string.folderexists).toString(), Toast.LENGTH_SHORT);
			    		toasterror.setGravity(Gravity.CENTER, 0, 0);
			    		toasterror.show();
		    			folderexists = z;
		    			whatfolderselected = value;
		    			FullscreenActivity.whichSongFolder = value;
		    			onPostExecute(response);
		    		  }
		    	  }
		    	  if (folderexists == -1) {
		    		  // Create this folder and set it as the selected one.
		    		  File foldertocreate = new File (FullscreenActivity.dir + "/" + value);
		    		  boolean success = true;
		    		  if (!foldertocreate.exists()) {
		    		      success = foldertocreate.mkdir();
		    		  }
		    		  if (success) {
			    			Toast toastsuccess = Toast.makeText(Chordie.this,getResources().getText(R.string.createfoldersuccess).toString(), Toast.LENGTH_SHORT);
			    			toastsuccess.setGravity(Gravity.CENTER, 0, 0);
			    			toastsuccess.show();
			    			whatfolderselected = value;
			    			FullscreenActivity.whichSongFolder = value;
			    			onPostExecute(response);
		    		  } else {
			    			Toast toasterror2 = Toast.makeText(Chordie.this,getResources().getText(R.string.createfoldererror).toString(), Toast.LENGTH_SHORT);
				    		toasterror2.setGravity(Gravity.CENTER, 0, 0);
				    		toasterror2.show();
			    			onPostExecute(response);
		    		  }
		    	  }

		    	  }
		    	});

		    	alert.setNegativeButton(getResources().getText(R.string.cancel).toString(), new DialogInterface.OnClickListener() {
		    	  @Override
				public void onClick(DialogInterface dialog, int whichButton) {
		    	    // Cancelled.
		    	  }
		    	});

		    	alert.show();
		    }
		    
		    @Override
		    protected void onPostExecute(String result)  {
		    	String resultposted = result;
		    	final String resultfinal;
				//Now look to see if the webcontent has the ChordPro text in it
		    	Toast toast;
				if (result.contains("<form id=\"chopro\"")) {

					progressbar.setVisibility(View.INVISIBLE);
					// Find the position of the start of this section
					int startpos = resultposted.indexOf("<form id=\"chopro\"");
					if (startpos<0) {
						startpos=0;
					}
					// Remove everything before this position
					resultposted = resultposted.substring(startpos);
					// Find where the actual ChordPro bit begins
					startpos = resultposted.indexOf("value=\"");
					if (startpos<0) {
						startpos=0;
					}
					// Remove everything before this position
					resultposted = resultposted.substring(startpos+7);
					
					// Find the position of the end of the form
					int endpos = resultposted.indexOf("\">");
					if (endpos<0) {
						endpos = resultposted.length();
					}
					resultposted = resultposted.substring(0,endpos);

					//Replace all \r with \n
					resultposted = resultposted.replace("\r","\n");
					resultposted = resultposted.replace("\'","'");
					
					resultfinal = resultposted;
					// Ask the user to specify the folder to save the file into			
					// Get a list of folders available
					// First set the browsing directory back to the main one
					FullscreenActivity.dir = new File(FullscreenActivity.root.getAbsolutePath()+"/documents/OpenSong/Songs");
				    String currentFolder = FullscreenActivity.whichSongFolder;
					FullscreenActivity.whichSongFolder = FullscreenActivity.mainfoldername;
					ListSongFiles.listSongs();
					availableFolders = FullscreenActivity.mSongFolderNames;
					
					// This bit gives the user a prompt to create a new song
					dialogBuilder = new AlertDialog.Builder(Chordie.this);
				    LinearLayout titleLayout = new LinearLayout(Chordie.this);
				    titleLayout.setOrientation(LinearLayout.VERTICAL);
				    TextView m_titleView = new TextView(Chordie.this);
				    m_titleView.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
				    m_titleView.setTextAppearance(Chordie.this, android.R.style.TextAppearance_Large);
				    m_titleView.setTextColor( Chordie.this.getResources().getColor(android.R.color.white) );
				    m_titleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
				    m_titleView.setText(getResources().getString(R.string.choosefolder));

				    Button newDirButton = new Button(Chordie.this);
				    newDirButton.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
				    newDirButton.setText(getResources().getString(R.string.newfolder));
				    newDirButton.setOnClickListener(new View.OnClickListener() {			
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							makeNewFolder();
						}
					});
				    
				    titleLayout.addView(m_titleView);
				    titleLayout.addView(newDirButton);

				    dialogBuilder.setCustomTitle(titleLayout);
				    dialogBuilder.setCancelable(false);
				    
				    // Get current folder
				    int numfolders = availableFolders.length;
				    //By default the folder is set to the main one
				    int folderposition = 0;
				    for (int z=0;z<numfolders;z++) {
				    	if (availableFolders[z].toString().equals(currentFolder)) {
				    		// Set this as the folder
				    		folderposition = z;
				    		whatfolderselected = currentFolder;
				    	}
				    }
				    
				    dialogBuilder.setSingleChoiceItems(availableFolders, folderposition,  new DialogInterface.OnClickListener() {

				    	   @Override
				    	   public void onClick(DialogInterface arg0, int arg1) {
				    	    whatfolderselected = availableFolders[arg1];
				    	   }
				    	  });
				    
				    dialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
																	
									FileOutputStream newFile;
									String filenameandlocation;
									
									try {
										if (whatfolderselected.equals("(MAIN)")) {
											filenameandlocation = FullscreenActivity.dir + "/"
													+ "chordie_import.chopro";
										} else {
											filenameandlocation = FullscreenActivity.dir + "/"
													+ whatfolderselected + "/chordie_import.chopro";
											FullscreenActivity.whichSongFolder = whatfolderselected;
										}
										newFile = new FileOutputStream(filenameandlocation, false);
										newFile.write(resultfinal.getBytes());
										newFile.flush();
										newFile.close();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

									FullscreenActivity.songfilename = "chordie_import.chopro";
									Preferences.savePreferences();
									Intent viewsong = new Intent(Chordie.this, FullscreenActivity.class);
									startActivity(viewsong);
									finish();
									}
								});

				    dialogBuilder.setNegativeButton(getResources().getString(R.string.cancel),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub	
								}				    
				    });
				    dialogBuilder.show();
								    
				} else if (result.contains("<div class=\"tb_ct\">")) {
					// From ultimate guitar
					
					progressbar.setVisibility(View.INVISIBLE);

					// Try to find the title
					int startpos = resultposted.indexOf("song:");
					if (startpos<0) {
						startpos=0;
					}
					// Remove everything before this position
					String title_resultposted = resultposted.substring(startpos);

					int endpos = title_resultposted.indexOf(",\n");
					if (endpos<0) {
						endpos=0;
					}
					//Bit with song title is in here hopefully
					if (endpos>5) {
						filenametosave = title_resultposted.substring(5, endpos);
						filenametosave = filenametosave.replace("\"", "");
						filenametosave = filenametosave.trim();
					} else {
						filenametosave = "*temp*";
					}
					
					// Find the position of the start of this section
					startpos = resultposted.indexOf("<div class=\"tb_ct\">");
					if (startpos<0) {
						startpos=0;
					}
					// Remove everything before this position
					resultposted = resultposted.substring(startpos);
					

					// Find the position of the start of this section
					startpos = resultposted.indexOf("<div class=\"tb_ct\">");
					if (startpos<0) {
						startpos=0;
					}
					// Remove everything before this position
					resultposted = resultposted.substring(startpos);
					
					// Find the ultimate guitar promo text start
					startpos = resultposted.indexOf("<pre class=\"print-visible\">");
					if (startpos<0) {
						startpos=0;
					}
					// Remove everything before this position
					resultposted = resultposted.substring(startpos+27);

					// Find the text start
					startpos = resultposted.indexOf("<pre>");
					if (startpos<0) {
						startpos=0;
					}
					// Remove everything before this position
					resultposted = resultposted.substring(startpos+5);

					// Find the position of the end of the form
					endpos = resultposted.indexOf("</pre>");
					if (endpos<0) {
						endpos = resultposted.length();
					}
					resultposted = resultposted.substring(0,endpos);

					//Replace all \r with \n
					resultposted = resultposted.replace("\r","\n");
					resultposted = resultposted.replace("\'","'");
					
					// Split into lines
					String[] templines = resultposted.split("\n");
					// Go through each line and look for chord lines
					// These have <span> in them
					int numlines = templines.length;
					String newtext = "";
					for (int q=0;q<numlines;q++) {
						if (templines[q].contains("<span>")) {
							// Identify chord lines
							templines[q] = "."+templines[q];
						}
						if (templines[q].indexOf("[")!=0 && templines[q].indexOf(".")!=0) {
							// Identify lyrics lines
							templines[q] = " " + templines[q];
						}
						newtext = newtext + templines[q] + "\n";
					}
					
					// Ok remove all html tags
					newtext = newtext.replace("<span>","");
					newtext = newtext.replace("</span>","");
					newtext = newtext.replace("<i>","");
					newtext = newtext.replace("</i>","");
					newtext = newtext.replace("<b>","");
					newtext = newtext.replace("</b>","");
					
					resultfinal = "<song>\n<title>" + filenametosave
							+ "</title>\n<author></author>\n<copyright></copyright>\n<lyrics>[]\n"
							+ newtext
							+ "</lyrics>\n</song>";

					// Success if this far - prompt for save
					// Ask the user to specify the folder to save the file into			
					// Get a list of folders available
					// First set the browsing directory back to the main one
					FullscreenActivity.dir = new File(FullscreenActivity.root.getAbsolutePath()+"/documents/OpenSong/Songs");
				    String currentFolder = FullscreenActivity.whichSongFolder;
					FullscreenActivity.whichSongFolder = FullscreenActivity.mainfoldername;
					ListSongFiles.listSongs();
					availableFolders = FullscreenActivity.mSongFolderNames;
					
					// This bit gives the user a prompt to create a new song
					dialogBuilder = new AlertDialog.Builder(Chordie.this);
				    LinearLayout titleLayout = new LinearLayout(Chordie.this);
				    titleLayout.setOrientation(LinearLayout.VERTICAL);
				    TextView m_titleView = new TextView(Chordie.this);
				    m_titleView.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
				    m_titleView.setTextAppearance(Chordie.this, android.R.style.TextAppearance_Large);
				    m_titleView.setTextColor( Chordie.this.getResources().getColor(android.R.color.white) );
				    m_titleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
				    m_titleView.setText(getResources().getString(R.string.choosefolder));

				    Button newDirButton = new Button(Chordie.this);
				    newDirButton.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
				    newDirButton.setText(getResources().getString(R.string.newfolder));
				    newDirButton.setOnClickListener(new View.OnClickListener() {			
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							makeNewFolder();
						}
					});
				    
				    titleLayout.addView(m_titleView);
				    titleLayout.addView(newDirButton);

				    dialogBuilder.setCustomTitle(titleLayout);
				    dialogBuilder.setCancelable(false);
				    
				    // Get current folder
				    int numfolders = availableFolders.length;
				    //By default the folder is set to the main one
				    int folderposition = 0;
				    for (int z=0;z<numfolders;z++) {
				    	if (availableFolders[z].toString().equals(currentFolder)) {
				    		// Set this as the folder
				    		folderposition = z;
				    		whatfolderselected = currentFolder;
				    	}
				    }
				    
				    dialogBuilder.setSingleChoiceItems(availableFolders, folderposition,  new DialogInterface.OnClickListener() {

				    	   @Override
				    	   public void onClick(DialogInterface arg0, int arg1) {
				    	    whatfolderselected = availableFolders[arg1];
				    	   }
				    	  });
				    
				    dialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
																	
									FileOutputStream newFile;
									String filenameandlocation;
									
									try {
										if (whatfolderselected.equals("(MAIN)")) {
											filenameandlocation = FullscreenActivity.dir + "/"
													+ filenametosave;
										} else {
											filenameandlocation = FullscreenActivity.dir + "/"
													+ whatfolderselected + "/"+filenametosave;
											FullscreenActivity.whichSongFolder = whatfolderselected;
										}
										newFile = new FileOutputStream(filenameandlocation, false);
										newFile.write(resultfinal.getBytes());
										newFile.flush();
										newFile.close();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

									FullscreenActivity.songfilename = filenametosave;
									Preferences.savePreferences();
									Intent viewsong = new Intent(Chordie.this, FullscreenActivity.class);
									startActivity(viewsong);
									finish();
									}
								});

				    dialogBuilder.setNegativeButton(getResources().getString(R.string.cancel),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub	
								}				    
				    });
				    dialogBuilder.show();

					
					
				} else {
			    	String message;
					message = getResources().getText(R.string.chordpro_false).toString();
					toast = Toast.makeText(Chordie.this,message, Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					progressbar.setVisibility(View.INVISIBLE);

				}
		  }	
	 }	 
}