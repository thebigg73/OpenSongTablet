package com.garethevans.church.opensongtablet.animation;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import com.garethevans.church.opensongtablet.R;

import java.util.ArrayList;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class ShowCase {

    public MaterialShowcaseView.Builder singleShowCase(Activity c, View target, String dismisstext_ornull, String information) {
        if (dismisstext_ornull==null) {
            dismisstext_ornull = c.getResources().getString(R.string.got_it);
        }
        Log.d("d","Trying to showcase");
        return new MaterialShowcaseView.Builder(c)
                .setTarget(target)
                .setDismissText(dismisstext_ornull)
                .setContentText(information)
                .setDelay(500) // optional but starting animations immediately in onCreate can make them choppy
                .setDismissOnTouch(true);
    }

    public void sequenceShowCase (Activity c, ArrayList<View> targets, ArrayList<String> dismisstexts_ornulls,
                           ArrayList<String> information, String showcaseid) {
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view
        config.setRenderOverNavigationBar(true);
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(c, showcaseid);
        sequence.setConfig(config);

        for (int i=0; i<targets.size(); i++) {
            if (dismisstexts_ornulls.get(i)==null) {
                dismisstexts_ornulls.set(i,c.getResources().getString(R.string.got_it));
            }
            if (targets.get(i)!=null) {
                sequence.addSequenceItem(singleShowCase(c, targets.get(i), dismisstexts_ornulls.get(i), information.get(i)).build());
            }
        }
        if (sequence!=null) {
            sequence.start();
        }
    }
}
