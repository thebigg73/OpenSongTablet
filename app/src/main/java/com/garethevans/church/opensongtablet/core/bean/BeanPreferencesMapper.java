package com.garethevans.church.opensongtablet.core.bean;

import android.content.SharedPreferences;

import com.garethevans.church.opensongtablet.FullscreenActivity;
import com.garethevans.church.opensongtablet.core.property.BooleanProperty;
import com.garethevans.church.opensongtablet.core.property.BeanProperty;
import com.garethevans.church.opensongtablet.core.property.FloatProperty;
import com.garethevans.church.opensongtablet.core.property.IntProperty;
import com.garethevans.church.opensongtablet.core.property.Property;
import com.garethevans.church.opensongtablet.core.property.StringProperty;

public class BeanPreferencesMapper extends BeanMapper {

    private SharedPreferences preferences;

    public BeanPreferencesMapper(Bean bean, SharedPreferences preferences) {
        super(bean);
        this.preferences = preferences;
    }

    public void load() {
        load(getBean());
    }

    private void load(Bean bean) {
        for (Property<?> p : bean.getProperties()) {
            String name = p.getName();
            if (p instanceof IntProperty) {
                ((IntProperty) p).set(this.preferences.getInt(name, 0));
            } else if (p instanceof BooleanProperty) {
                ((BooleanProperty) p).set(this.preferences.getBoolean(name, false));
            } else if (p instanceof StringProperty) {
                ((StringProperty) p).setValue(this.preferences.getString(name, ""));
            } else if (p instanceof FloatProperty) {
                ((FloatProperty) p).set(this.preferences.getFloat(name, 0));
            } else if (p instanceof BeanProperty) {
                Bean childBean = ((BeanProperty) p).getValue();
                if (childBean != null) {
                    load(childBean);
                }
            } else {
                throw new IllegalStateException("Unsupported property type!");
            }
        }
    }

    public void save() {
        try {
            SharedPreferences.Editor editor = this.preferences.edit();
            save(editor);
            editor.apply();
        } catch (Exception e) {
            // Error saving.  Normally happens if app was closed before this happens
            e.printStackTrace();
            // Try restarting the app
            FullscreenActivity.restart(FullscreenActivity.mContext);
        }
    }

    public void save(SharedPreferences.Editor preferences) {

        save(getBean(), preferences);
    }

    public void save(Bean bean, SharedPreferences.Editor preferences) {

        for (Property<?> p : bean.getProperties()) {
            String name = p.getName();
            if (p instanceof IntProperty) {
                preferences.putInt(name, ((IntProperty) p).get());
            } else if (p instanceof BooleanProperty) {
                preferences.putBoolean(name, ((BooleanProperty) p).get());
            } else if (p instanceof StringProperty) {
                preferences.putString(name, ((StringProperty) p).getValue());
            } else if (p instanceof FloatProperty) {
                preferences.putFloat(name, ((FloatProperty) p).getValue());
            } else if (p instanceof BeanProperty) {
                Bean childBean = ((BeanProperty) p).getValue();
                if (childBean != null) {
                    save(childBean, preferences);
                }
            } else {
                throw new IllegalStateException("Unsupported property type!");
            }
        }
    }

}
