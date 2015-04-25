/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * This application creates a listview where the ordering of the data set
 * can be modified in response to user touch events.
 *
 * An item in the listview is selected via a long press event and is then
 * moved around by tracking and following the movement of the user's finger.
 * When the item is released, it animates to its new position within the listview.
 */
public class ListViewDraggingAnimation extends Activity {

	boolean PresentMode = false; 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        PresentMode = getIntent().getBooleanExtra("PresentMode", false);
        
        setContentView(R.layout.activity_list_view);

        String tempCheck = "";
        String tempCheckThis = "";
        ArrayList<String>mCurrentSetList = new ArrayList<String>();
        for (int i = 0; i < FullscreenActivity.mSetList.length; ++i) {
        	// Need to ensure that we don't use the same name twice.
        	// Add a $_duplicate_$ to the end
        	tempCheckThis = FullscreenActivity.mSetList[i];
        	while (tempCheck.contains("$**_"+tempCheckThis+"_**$")) {
        		tempCheckThis = tempCheckThis + "$_duplicate_$";
        	}
        	tempCheck = tempCheck + "$**_" + tempCheckThis + "_**$";
        	//mCurrentSetList.add(FullscreenActivity.mSetList[i]);
        	mCurrentSetList.add(tempCheckThis);
        	tempCheckThis = "";
        	
        }

        StableArrayAdapter adapter = new StableArrayAdapter(this, R.layout.text_view, mCurrentSetList);
        DynamicListView listView = (DynamicListView) findViewById(R.id.listview);

        listView.setSetList(mCurrentSetList);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }
    
	@Override
	public void onBackPressed() {
		Intent viewsong = new Intent();

		if (PresentMode) {
			viewsong.setClass(ListViewDraggingAnimation.this, PresentMode.class);			
			startActivity(viewsong);
			this.finish();
		} else {
			viewsong.setClass(ListViewDraggingAnimation.this, FullscreenActivity.class);			
			startActivity(viewsong);
			this.finish();
		}
		

	}

    public void doSave(View v) {
    	DynamicListView.doTheSave();
    	Preferences.savePreferences();
		Intent editset = new Intent();
		invalidateOptionsMenu();
		if (PresentMode) {
			editset.setClass(ListViewDraggingAnimation.this, PresentMode.class);			
			startActivity(editset);
			this.finish();
		} else {
			editset.setClass(ListViewDraggingAnimation.this, FullscreenActivity.class);			
			startActivity(editset);
			this.finish();
		}

    }
}
