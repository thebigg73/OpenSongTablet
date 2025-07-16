package com.garethevans.church.opensongtablet.nearby;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.collection.SimpleArrayMap;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.gms.nearby.connection.Payload;

public class NearbyTransferRecords {

    // This class keeps track of incoming and outgoing transfers
    // It should only allow each message to be sent or received once

    public NearbyTransferRecords(Context c) {
        this.mainActivityInterface = (MainActivityInterface) c;
    }

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "NearbyTransferRecords";
    private final MainActivityInterface mainActivityInterface;

    // Incoming payloads (what has been received)
    private final SimpleArrayMap<Long, Payload> incomingPayloads = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, NearbyJson> incomingFileInformation = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, Boolean> incomingDealtWith = new SimpleArrayMap<>();
    // Outgoing payloads (what has been sent)
    private final SimpleArrayMap<Long, Payload> outgoingPayloads = new SimpleArrayMap<>();

    // The URI of the last file received
    private Uri lastFileReceivedURI = null;

    private final int delayToRemove = 15000;

    // Check if we have already received these payloads/nearbyJsons
    public boolean getAlreadyReceivedPayload(Payload payload) {
        return incomingPayloads.containsKey(payload.getId());
    }
    public boolean getAlreadyReceivedFilePayload(long id) {
        Payload payload = incomingPayloads.get(id);
        return payload!=null && payload.getType()==Payload.Type.FILE;
    }
    public Payload getAlreadyReceivedPayload(long id) {
        return incomingPayloads.get(id);
    }
    public boolean getAlreadyReceivedFileInformation(NearbyJson nearbyJson) {
        return incomingFileInformation.containsKey(nearbyJson.getId());
    }
    public NearbyJson getAlreadyReceivedFileInformation(Long id) {
        return incomingFileInformation.get(id);
    }
    public void addIncomingDealtWith(long id, Boolean dealtWith) {
        incomingDealtWith.put(id, dealtWith);
    }

    // Check if we have already sent these payloads/nearbyJsons
    public boolean getNotAlreadySentPayload(Payload payload) {
        return !outgoingPayloads.containsKey(payload.getId());
    }

    // Add the payloads/nearbyJsons to our arrays
    public void addAlreadyReceivedPayload(Payload payload) {
        try {
            incomingPayloads.put(payload.getId(), payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void addAlreadyReceivedFileInformation(NearbyJson nearbyJson) {
        incomingFileInformation.put(nearbyJson.getId(), nearbyJson);
    }
    public void addAlreadySentPayload(Payload payload) {
        outgoingPayloads.put(payload.getId(), payload);
    }

    // Remove the incoming payloads/nearbyJsons from our arrays to recover memory (dealt with)
    // These are done as delayed handlers (10 seconds)
    public void removeAlreadyReceivedPayload(long id) {
        mainActivityInterface.getMainHandler().postDelayed(() -> {
            try {
                if (incomingPayloads.containsKey(id)) {
                    incomingPayloads.remove(id);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        },delayToRemove);
    }
    public void removeAlreadyReceivedFileInformation(Long id) {
        if (id!=null) {
            mainActivityInterface.getMainHandler().postDelayed(() -> {
                try {
                    if (incomingFileInformation.containsKey(id)) {
                        incomingFileInformation.remove(id);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, delayToRemove);
        }
    }

    // Remove the outgoing payloads/nearbyJsons from our arrays to recover memory (dealt with)
    // These are done as delayed handlers (10 seconds)
    public void removeAlreadySentPayload(long id) {
        mainActivityInterface.getMainHandler().postDelayed(() -> {
            try {
                if (outgoingPayloads.containsKey(id)) {
                    outgoingPayloads.remove(id);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        },delayToRemove);
    }
    public void removeAlreadyDealtWith(long id) {
        mainActivityInterface.getMainHandler().postDelayed(() -> {
            try {
                if (incomingDealtWith.containsKey(id)) {
                    incomingDealtWith.remove(id);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        },delayToRemove);
    }

    public void setLastFileReceivedURI(Uri lastFileReceivedURI) {
        Log.d(TAG,"Setting the last file received Uri as:"+lastFileReceivedURI);
        this.lastFileReceivedURI = lastFileReceivedURI;
    }
    public Uri getLastFileReceivedURI() {
        return lastFileReceivedURI;
    }
}
