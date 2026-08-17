package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;

public class OpenChordsFolderUUIDLayout extends FrameLayout {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "OpenChordsUUIDLayout";
    private final LinearLayout uuidLinearLayout;
    private final MyMaterialEditText uuidItem;
    private final MyFloatingActionButton uuidItemLock, uuidItemShare;
    private String folderName = "";
    private String folderUUID = "";
    private boolean locked = true;


    public OpenChordsFolderUUIDLayout(@NonNull Context context) {
        this(context,null);
    }

    public OpenChordsFolderUUIDLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_openchords_uuid_layout, this);
        uuidLinearLayout = findViewById(R.id.uuidLinearLayout);
        uuidItem = findViewById(R.id.uuidItem);
        uuidItem.setEnabled(false);
        uuidItemLock = findViewById(R.id.uuidItemLock);
        uuidItemShare = findViewById(R.id.uuidItemShare);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OpenChordsFolderUUIDLayout);
        try {
            // 1. Safely handle android:text
            CharSequence text = a.getText(R.styleable.OpenChordsFolderUUIDLayout_android_text);
            if (text != null) {
                // So both actual and preview work
                folderUUID = text.toString();
                uuidItem.setText(folderUUID);
                setText(folderUUID);
            }

            // 2. Safely handle android:hint
            CharSequence hint = a.getText(R.styleable.OpenChordsFolderUUIDLayout_android_hint);
            if (hint != null) {
                // So both actual and preview work
                folderName = hint.toString();
                uuidItem.setHint(folderName);
                setHint(folderName);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            a.recycle();
        }
    }

    public void initialise(Context c, String uuid, String name) {
        setFolderUUID(uuid);
        setFolderName(name);

        Log.d(TAG,"intialise:"+name+"  "+uuid);
        // Handle Share Button Click
        uuidItemShare.setOnClickListener(view -> {
            if (locked) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, folderUUID);
                sendIntent.setType("text/plain");
                Intent shareIntent = Intent.createChooser(sendIntent, null);
                c.startActivity(shareIntent);
            }
        });

        // Handle Lock Button Click
        uuidItemLock.setOnClickListener(view -> {
            toggleLock();

            // Notify listener with the new lock state (returned to fragment
            if (listener != null) {
                listener.onLockClicked(locked);
            }
        });
    }

    public void toggleLock() {
        locked = !locked;
        uuidItem.setEnabled(!locked);
        uuidItemLock.setImageDrawable(locked ? R.drawable.lock:R.drawable.lock_open);
        uuidItemShare.setEnabled(locked);
        uuidItemShare.setAlpha(locked ? 1.0f:0.5f);
    }

    public boolean getLocked() {
        return locked;
    }

    public MyFloatingActionButton getUuidItemLock() {
        return uuidItemLock;
    }

    public MyFloatingActionButton getUuidItemShare() {
        return uuidItemShare;
    }

    public boolean checkUUIDValid() {
        String uuid = "";
        if (uuidItem.getText()!=null) {
            uuid = uuidItem.getText().toString();
        }
        if (uuid.isEmpty()) {
            return false;
        } else {
            java.util.regex.Pattern UUID_REGEX = java.util.regex.Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
            java.util.regex.Pattern CUSTOM_UUID_REGEX = java.util.regex.Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{6}$");
            return UUID_REGEX.matcher(uuid).matches() || CUSTOM_UUID_REGEX.matcher(uuid).matches();
        }
    }

    public String getFolderName() {
        return folderName;
    }
    public void setFolderName(String folderName) {
        this.folderName = folderName;
        uuidItem.setHint(folderName);
    }
    public String getFolderUUID() {
        return folderUUID;
    }
    public void setFolderUUID(String folderUUID) {
        this.folderUUID = folderUUID;
        uuidItem.setText(folderUUID);
    }
    public void setText(String text) {
        this.folderUUID = text;
        uuidItem.setText(text);
    }
    public void setHint(String hint) {
        this.folderName = hint;
        uuidItem.setHint(hint);
    }
    public String getText() {
        String text = null;
        if (uuidItem.getText()!=null) {
            text = uuidItem.getText().toString();
        }
        return text;
    }
    public String getHint() {
        String hint = null;
        if (uuidItem.getHint()!=null) {
            hint = uuidItem.getText().toString();
        }
        return hint;
    }


    // Define listener interface for both buttons
    public interface OnFolderActionListener {
        void onLockClicked(boolean isLocked);
    }

    private OnFolderActionListener listener;

    public void setOnFolderActionListener(OnFolderActionListener listener) {
        this.listener = listener;
    }
}
