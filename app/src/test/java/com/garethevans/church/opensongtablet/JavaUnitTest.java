package com.garethevans.church.opensongtablet;

import android.content.ContentResolver;
import android.content.Context;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Example local unit test, which will execute on the development machine (host).
 */
@RunWith(MockitoJUnitRunner.class)
public class JavaUnitTest {

    private final String TAG = "JavaUnitTest";

    @Mock
    Context mMockContext;

    @Mock
    ContentResolver mMockContentResolver;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

}
