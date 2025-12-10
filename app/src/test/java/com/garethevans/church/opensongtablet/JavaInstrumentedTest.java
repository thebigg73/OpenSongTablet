package com.garethevans.church.opensongtablet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.util.Log;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test, which will execute on an Android device.
 */
@RunWith(AndroidJUnit4.class)
public class JavaInstrumentedTest {

    private MainActivityInterface mainActivityInterface;
    private final String TAG = "Test";

    // 1. Add the ActivityScenarioRule for your MainActivity
    // This rule will launch MainActivity before each test
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    // 2. Create a @Before method to get the interface reference
    // This method runs before every @Test method
    @Before
    public void setUp() {
        // Use the rule to get a reference to the running activity
        activityRule.getScenario().onActivity(activity -> {
            // This code runs on the UI thread, safely accessing the activity
            mainActivityInterface = activity;
        });
    }

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.garethevans.church.opensongtablet", appContext.getPackageName());

        // You no longer need to get the interface here, it's already set up in @Before
        assertNotNull("MainActivityInterface should not be null", mainActivityInterface);
    }

    @Test
    public void testProcessString() {
        // The mainActivityInterface is already available here because @Before ran first.
        assertNotNull("MainActivityInterface is null, check setUp()", mainActivityInterface);

        // Arrange: Set up the input for your test
        String inputString = "hello world";
        String expectedOutput = "hello world";

        // Act: Call the method you want to test
        // Let's assume processString is a method on your MainActivityInterface for this example
        // String actualOutput = mainActivityInterface.processString(inputString);
        String actualOutput = processString(inputString); // Or call it directly if it's a local test helper


        // Assert: Check if the result is what you expected
        assertEquals(expectedOutput, actualOutput);
        Log.d(TAG, "Test passed! The processed string is: " + actualOutput);
    }

    // Helper method for the test
    public String processString(String thisString) {
        Log.d(TAG,"thisString:"+thisString);
        return thisString;
    }
}
