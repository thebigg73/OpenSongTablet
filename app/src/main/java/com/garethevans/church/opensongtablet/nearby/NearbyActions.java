package com.garethevans.church.opensongtablet.nearby;

import android.app.Activity;
import android.content.Context;

public class NearbyActions {

    // This class is the communicator head class for Nearby actions
    // Tasks are delegated to helper classes
    private NearbyConnectionManager nearbyConnectionManager;
    private NearbySendPayloads nearbySendPayloads;
    private NearbyReceivePayloads nearbyReceivePayloads;
    private NearbyTransferRecords nearbyTransferRecords;
    private NearbyMessages nearbyMessages;
    private SyncNearbyFragment syncNearbyFragment = null;
    private NearbyLogs nearbyLogs;

    private final Activity activity;
    private final Context c;

    // Common strings
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "NearbyActions";

    // Autoscroll and scrolling identifiers in bytes payloads
    public String scrollByTag = "___scrollby___";
    public String scrollToTag = "___scrollto___";
    public String autoscrollPause = "___autoscrollpause___";
    public String autoscrollIncrease = "___autoscrollincrease___";
    public String autoscrollStart = "___autoscrollstart___";
    public String autoscrollStop = "___autoscrollstop___";
    public String autoscrollDecrease = "___autoscrolldecrease___";

    // Song section identifiers in bytes payloads
    public String sectionTag = "___section___";

    public String profiles = "___profiles___";
    public String songTag = "___song___";
    public String fileTag = "___file___";
    public String endpointSplit = "__";
    public String sets = "___sets___";
    public String messageTag = "___message___";

    // Synchronisation identifiers in bytes payloads
    public String syncRequestInfo = "___syncinforequest___";
    public String syncProcessingInfo = "___syncprocessinginfo___";
    public String syncReturnedInfo = "___syncreturnedinfo___";
    public String syncRequestContent = "___synccontent___";
    public String syncProcessingContent = "___syncprocessingcontent___";
    public String syncReturnedContent = "___syncreturnedcontent___";
    public String syncRequestDenied = "___syncrequestdenied___";
    public String sharableObjectFile = "nearbyShareableList.json";
    public String requestSongsFile = "nearbyRequestSongs.json";
    public String requestSetsFile = "nearbyRequestSets.json";
    public String requestProfilesFile = "nearbyRequestProfiles.json";
    public String contentZipSongs = "syncSongsContent.zip";
    public String contentZipSets = "syncSetsContent.zip";
    public String contentZipProfiles = "syncProfilesContent.zip";
    public String currentSetFile = "currentSet.xml";

    public NearbyActions(Activity activity, Context c) {
        this.activity = activity;
        this.c = c;

        // Initialise the helper classes
        getNearbyConnectionManagement();
        getNearbySendPayloads();
        getNearbyReceivePayloads();
        getNearbyLogs();
        getNearbyTransferRecords();
        getNearbyMessages();
    }

    public NearbyLogs getNearbyLogs() {
        if (nearbyLogs == null) {
            nearbyLogs = new NearbyLogs(c);
        }
        return nearbyLogs;
    }
    public NearbyConnectionManager getNearbyConnectionManagement() {
        if (nearbyConnectionManager == null) {
            nearbyConnectionManager = new NearbyConnectionManager(activity, c, this);
        }
        return nearbyConnectionManager;
    }
    public NearbySendPayloads getNearbySendPayloads() {
        if (nearbySendPayloads == null) {
            nearbySendPayloads = new NearbySendPayloads(activity, c, this);
        }
        return nearbySendPayloads;
    }
    public NearbyReceivePayloads getNearbyReceivePayloads() {
        if (nearbyReceivePayloads == null) {
            nearbyReceivePayloads = new NearbyReceivePayloads(c, this);
        }
        return nearbyReceivePayloads;
    }
    public NearbyTransferRecords getNearbyTransferRecords() {
        if (nearbyTransferRecords == null) {
            nearbyTransferRecords = new NearbyTransferRecords(c);
        }
        return nearbyTransferRecords;
    }
    public NearbyMessages getNearbyMessages() {
        if (nearbyMessages == null) {
            nearbyMessages = new NearbyMessages(c);
        }
        return nearbyMessages;
    }

    // Deal with synchronising devices
    // Firstly, keep a reference to the syncNearybFragment (for sending info back)
    public void setSyncNearbyFragment(SyncNearbyFragment syncNearbyFragment) {
        this.syncNearbyFragment = syncNearbyFragment;
    }
    public SyncNearbyFragment getSyncNearbyFragment() {
        return syncNearbyFragment;
    }

}
