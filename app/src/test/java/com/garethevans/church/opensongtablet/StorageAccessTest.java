package com.garethevans.church.opensongtablet;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StorageAccessTest {

    @Test
    public void safeFilename() {
        String input = "This is a <*bad*> /Filename\\ & who knows what it wull do!! # $ + \" ' { } : @ ";
        String output;
        String expected = "This is a bad Filename who knows what it wull do";

        StorageAccess storageAccess = new StorageAccess();
        output = storageAccess.safeFilename(input);

        assertEquals(expected,output);
    }
}