package com.garethevans.church.opensongtablet.nearby;

import android.os.Handler;

import androidx.collection.SimpleArrayMap;

import com.google.android.gms.nearby.connection.Payload;

public class NearbyTransferRecords {

    // This class keeps track of incoming and outgoing transfers
    // It should only allow each message to be sent or received once

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "NearbyTransferRecords";

    // Incoming payloads (what has been received)
    private final SimpleArrayMap<Long, Payload> incomingPayloads = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, NearbyJson> incomingFileInformation = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, Boolean> incomingDealtWith = new SimpleArrayMap<>();
    // Outgoing payloads (what has been sent)
    private final SimpleArrayMap<Long, Payload> outgoingPayloads = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, NearbyJson> outgoingFileInformation = new SimpleArrayMap<>();

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
    public boolean getIncomingDealtWith(long id) {
        return incomingDealtWith.containsKey(id) && Boolean.TRUE.equals(incomingDealtWith.get(id));
    }
    public void addIncomingDealtWith(long id, Boolean dealtWith) {
        incomingDealtWith.put(id, dealtWith);
    }


    // Check if we have already sent these payloads/nearbyJsons
    public boolean getAlreadySentPayload(Payload payload) {
        return outgoingPayloads.containsKey(payload.getId());
    }
    public boolean getAlreadySentFileInformation(NearbyJson nearbyJson) {
        return outgoingFileInformation.containsKey(nearbyJson.getId());
    }


    // Add the payloads/nearbyJsons to our arrays
    public void addAlreadyReceivedPayload(Payload payload) {
        incomingPayloads.put(payload.getId(), payload);
    }
    public void addAlreadyReceivedFileInformation(NearbyJson nearbyJson) {
        incomingFileInformation.put(nearbyJson.getId(), nearbyJson);
    }
    public void addAlreadySentPayload(Payload payload) {
        outgoingPayloads.put(payload.getId(), payload);
    }
    public void addAlreadySentFileInformation(NearbyJson nearbyJson) {
        outgoingFileInformation.put(nearbyJson.getId(), nearbyJson);
    }


    // Remove the incoming payloads/nearbyJsons from our arrays to recover memory (dealt with)
    // These are done as delayed handlers (10 seconds)
    public void removeAlreadyReceivedPayload(long id) {
        new Handler().postDelayed(() -> {
            try {
                if (incomingPayloads != null && incomingPayloads.containsKey(id)) {
                    incomingPayloads.remove(id);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        },delayToRemove);
    }
    public void removeAlreadyReceivedFileInformation(Long id) {
        if (id!=null) {
            new Handler().postDelayed(() -> {
                try {
                    if (incomingFileInformation != null && incomingFileInformation.containsKey(id)) {
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
        new Handler().postDelayed(() -> {
            try {
                if (outgoingPayloads != null && outgoingPayloads.containsKey(id)) {
                    outgoingPayloads.remove(id);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        },delayToRemove);
    }
    public void removeAlreadySentFileInformation(Long id) {
        if (id!=null) {
            new Handler().postDelayed(() -> {
                try {
                    if (outgoingFileInformation != null && outgoingFileInformation.containsKey(id)) {
                        outgoingFileInformation.remove(id);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, delayToRemove);
        }
    }
    public void removeAlreadyDealtWith(long id) {
        new Handler().postDelayed(() -> {
            try {
                if (incomingDealtWith != null && incomingDealtWith.containsKey(id)) {
                    incomingDealtWith.remove(id);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        },delayToRemove);
    }


/*
    //private String payloadReceivedIds = "";
    //private SimpleArrayMap<Long, String> fileNewLocation = new SimpleArrayMap<>();



    public String getPayloadTransferIds() {
        return payloadTransferIds;
    }
    public void setPayloadTransferIds(String payloadTransferIds) {
        this.payloadTransferIds = payloadTransferIds;
    }
    public void addPayloadTransferIds(Long payloadTransferId) {
        this.payloadTransferIds += " " + payloadTransferId + " ";
    }
    public void cancelTransferIds() {
        // IV - Used to cancel earlier transfer Ids
        if (payloadTransferIds != null && !payloadTransferIds.isEmpty()) {
            String[] ids = payloadTransferIds.trim().split(" ");
            payloadTransferIds = "";
            for (String Id : ids) {
                Nearby.getConnectionsClient(activity).cancelPayload(Long.parseLong((Id.trim())));
            }
            incomingFilePayloads = new SimpleArrayMap<>();
            fileNewLocation = new SimpleArrayMap<>();
        }
    }
    public String getPayloadReceivedIds() {
        return payloadReceivedIds;
    }
    public void setPayloadReceivedIds(String payloadReceivedIds) {
        this.payloadReceivedIds = payloadReceivedIds;
    }
    public void addPayloadReceivedIds(Long payloadReceivedId) {
        this.payloadReceivedIds += " " + payloadReceivedId + " ";
    }
    public boolean payloadReceivedIdsContains(NearbyJson nearbyJson) {
        return nearbyJson!=null && nearbyJson.getId()!=null && payloadReceivedIds.contains(String.valueOf(nearbyJson.getId()));
    }
    public Payload getPreviousPayload() {
        return previousPayload;
    }
    public void setPreviousPayload(Payload previousPayload) {
        this.previousPayload = previousPayload;
    }

    public SimpleArrayMap<Long, Payload> getIncomingFilePayloads() {
        return incomingFilePayloads;
    }
    public SimpleArrayMap<Long, String> getFileNewLocation() {
        return fileNewLocation;
    }

    public SimpleArrayMap<Long, Payload> getIncomingPayloads() {
        return incomingPayloads;
    }
    public SimpleArrayMap<Long, NearbyJson> getIncomingFileInformation() {
        return incomingFileInformation;
    }*/
}
