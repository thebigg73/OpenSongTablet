package com.garethevans.church.opensongtablet.animation;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;

import com.garethevans.church.opensongtablet.R;

import java.util.ArrayList;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class ShowCase {

    @SuppressWarnings("unused")
    private final String TAG = "ShowCase";
    private String ok="";
    private int showcaseColor, white;

    public ShowCase(Context c){
        if (c!=null) {
            ok = c.getString(R.string.okay);
            showcaseColor = c.getResources().getColor(R.color.showcaseColor);
            white = c.getResources().getColor(R.color.white);
        }
    }

    public boolean singleShowCase(Activity c, View target, String dismiss, String info, boolean rect, String id) {
        if (c!=null && target!=null) {
            return singleShowCaseBuilder(c, target, dismiss, info, rect, id).setMaskColour(showcaseColor).setContentTextColor(white).build().show(c);
        } else {
            return false;
        }
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
            dismisstext_ornull = ok;
        }

        final int COLOR_MASK = showcaseColor;
        final int COLOR_TEXT = white;

        MaterialShowcaseView.Builder mscb = new MaterialShowcaseView.Builder(c)
                .setTarget(target)
                .setDismissText(dismisstext_ornull)
                .setContentText(information)
                .setDelay(500) // optional but starting animations immediately in onCreate can make them choppy
                .renderOverNavigationBar()
                .setMaskColour(COLOR_MASK)
                .setContentTextColor(COLOR_TEXT)
                .setDismissOnTouch(true);
        if (id!=null) {
            mscb = mscb.singleUse(id);
        }
        if (rect) {
            mscb = mscb.withRectangleShape();
        }
        return mscb;
    }

    public void sequenceShowCase (Activity c, ArrayList<View> targets, ArrayList<String> dismisstexts_ornulls,
                                  ArrayList<String> information, ArrayList<Boolean> rects, String showcaseid) {
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(800); // 800ms between each showcase view
        config.setRenderOverNavigationBar(true);
        final int COLOR_MASK = showcaseColor;
        final int COLOR_TEXT = white;
        config.setMaskColor(COLOR_MASK);
        config.setContentTextColor(COLOR_TEXT);
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(c, showcaseid);
        sequence.setConfig(config);

        if (dismisstexts_ornulls==null) {
            dismisstexts_ornulls = new ArrayList<>();
            for (int z=0; z<targets.size(); z++) {
                dismisstexts_ornulls.add(ok);
            }
        }
        if (rects==null) {
            rects = new ArrayList<>();
            for (int z=0; z<targets.size(); z++) {
                rects.add(true);
            }
        }

        for (int i=0; i<targets.size(); i++) {
            if (dismisstexts_ornulls.get(i)==null) {
                dismisstexts_ornulls.set(i,ok);
            }

            if (targets.get(i)!=null) {
                sequence.addSequenceItem(singleShowCaseBuilder(c, targets.get(i), dismisstexts_ornulls.get(i),
                        information.get(i), rects.get(i),null).build());
            }
        }
        try {
            sequence.start();
        } catch (Exception e) {
            Log.d("ShowCase","Error:"+e);
        }
    }

    public void resetShowcase(Context c, String what) {
        if (what==null) {
            MaterialShowcaseView.resetAll(c);
        } else {
            MaterialShowcaseView.resetSingleUse(c, what);
        }
    }
}
