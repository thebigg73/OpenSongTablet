package com.garethevans.church.opensongtablet.core.config;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DummyPreferences implements SharedPreferences.Editor, SharedPreferences {

    private final Map<String, Object> map;

    public DummyPreferences() {
        this(new HashMap<String, Object>());
    }

    public DummyPreferences(Map<String, Object> map) {
        super();
        this.map = map;
    }

    @Override
    public SharedPreferences.Editor putString(String s, @Nullable String s1) {
        this.map.put(s, s1);
        return this;
    }

    @Override
    public SharedPreferences.Editor putStringSet(String s, @Nullable Set<String> set) {
        this.map.put(s, set);
        return this;
    }

    @Override
    public SharedPreferences.Editor putInt(String s, int i) {
        this.map.put(s, Integer.valueOf(i));
        return this;
    }

    @Override
    public SharedPreferences.Editor putLong(String s, long l) {
        this.map.put(s, Long.valueOf(l));
        return this;
    }

    @Override
    public SharedPreferences.Editor putFloat(String s, float v) {
        this.map.put(s, Float.valueOf(v));
        return this;
    }

    @Override
    public SharedPreferences.Editor putBoolean(String s, boolean b) {
        this.map.put(s, Boolean.valueOf(b));
        return this;
    }

    @Override
    public SharedPreferences.Editor remove(String s) {
        this.map.remove(s);
        return this;
    }

    @Override
    public SharedPreferences.Editor clear() {
        this.map.clear();
        return this;
    }

    @Override
    public boolean commit() {
        return true;
    }

    @Override
    public void apply() {

    }

    @Override
    public Map<String, ?> getAll() {
        return new HashMap<>(this.map);
    }

    public <T> T get(String s, T defaultValue) {

        T value = (T) this.map.get(s);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }

    @Nullable
    @Override
    public String getString(String s, @Nullable String s1) {
        return get(s, s1);
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String s, @Nullable Set<String> set) {
        return get(s, set);
    }

    @Override
    public int getInt(String s, int i) {
        return get(s, Integer.valueOf(i));
    }

    @Override
    public long getLong(String s, long l) {
        return get(s, Long.valueOf(l));
    }

    @Override
    public float getFloat(String s, float v) {
        return get(s, Float.valueOf(v));
    }

    @Override
    public boolean getBoolean(String s, boolean b) {
        return get(s, Boolean.valueOf(b));
    }

    @Override
    public boolean contains(String s) {
        return this.map.containsKey(s);
    }

    @Override
    public Editor edit() {
        return this;
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {

    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {

    }
}
