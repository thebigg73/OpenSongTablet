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

    public void singleShowCase(Activity c, View target, String dismiss, String info, boolean rect, String id) {
        singleShowCaseBuilder(c,target,dismiss,info,rect,id).build().show(c);
    }

    public MaterialShowcaseView.Builder getSingleShowCaseBuilderForListener(Activity c, View target,
                                                                            String dismiss, String info,
                                                                            boolean rect, String id) {
        return singleShowCaseBuilder(c,target,dismiss,info,rect,id);
    }

    public MaterialShowcaseView.Builder singleShowCaseBuilder(Activity c, View target,
                                                              String dismisstext_ornull,
                                                              String information,
                                                              boolean rect, String id) {
        if (dismisstext_ornull==null) {
            dismisstext_ornull = c.getResources().getString(android.R.string.ok);
        }

        MaterialShowcaseView.Builder mscb = new MaterialShowcaseView.Builder(c)
                .setTarget(target)
                .setDismissText(dismisstext_ornull)
                .setContentText(information)
                .setDelay(500) // optional but starting animations immediately in onCreate can make them choppy
                .renderOverNavigationBar()
                .setMaskColour(c.getResources().getColor(R.color.showcaseColor))
                .setDismissOnTouch(true);
        if (id!=null) {
            mscb = mscb.singleUse(id);
        }
        if (rect) {
            mscb = mscb.withRectangleShape();
        }
        return mscb;
    }

    public MaterialShowcaseSequence sequenceShowCase (Activity c, ArrayList<View> targets, ArrayList<String> dismisstexts_ornulls,
                           ArrayList<String> information, ArrayList<Boolean> rects, String showcaseid) {
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view
        config.setRenderOverNavigationBar(true);
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(c, showcaseid);
        sequence.setConfig(config);

        if (dismisstexts_ornulls==null) {
            dismisstexts_ornulls = new ArrayList<>();
            for (int z=0; z<targets.size(); z++) {
                dismisstexts_ornulls.add(null);
            }
        }

        for (int i=0; i<targets.size(); i++) {
            if (dismisstexts_ornulls.get(i)==null) {
                dismisstexts_ornulls.set(i,c.getResources().getString(android.R.string.ok));
            }
            if (targets.get(i)!=null) {
                sequence.addSequenceItem(singleShowCaseBuilder(c, targets.get(i), dismisstexts_ornulls.get(i),
                        information.get(i), rects.get(i),null).build());
            }
        }
        try {
            sequence.start();
            return sequence;
        } catch (Exception e) {
            Log.d("ShowCase","Error:"+e);
            return null;
        }
    }
}
