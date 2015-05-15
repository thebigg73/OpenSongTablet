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
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import java.util.ArrayList;

public class PopUpEditSetFragment extends DialogFragment {

    static PopUpEditSetFragment newInstance() {
        PopUpEditSetFragment frag;
        frag = new PopUpEditSetFragment();
        return frag;
    }

    public interface MyInterface {
        void refreshAll();
    }

    private MyInterface mListener;

    @Override
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View V = inflater.inflate(R.layout.popup_editset, container, false);

        getDialog().setTitle(getActivity().getResources().getString(R.string.set_edit_title));

        String tempCheck = "";
        String tempCheckThis;

        ArrayList<String>mCurrentSetList = new ArrayList<>();

        for (int i = 0; i < FullscreenActivity.mSetList.length; ++i) {
            // Need to ensure that we don't use the same name twice.
            // Add a $_duplicate_$ to the end
            tempCheckThis = FullscreenActivity.mSetList[i];
            while (tempCheck.contains("$**_"+tempCheckThis+"_**$")) {
                tempCheckThis = tempCheckThis + " *";
            }
            tempCheck = tempCheck + "$**_" + tempCheckThis + "_**$";
            mCurrentSetList.add(tempCheckThis);
        }

        StableArrayAdapter adapter = new StableArrayAdapter(getActivity(), R.layout.text_view, mCurrentSetList);
        DynamicListView listView = (DynamicListView) V.findViewById(R.id.listview);

        listView.setSetList(mCurrentSetList);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        Button set_save = (Button) V.findViewById(R.id.set_save);
        set_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSave();
            }
        });

        Button set_cancel = (Button) V.findViewById(R.id.set_cancel);
        set_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return V;
    }

    public void doSave() {
        DynamicListView.doTheSave();
        Preferences.savePreferences();

        // Tell the listener to do something
        mListener.refreshAll();
        dismiss();
    }
}