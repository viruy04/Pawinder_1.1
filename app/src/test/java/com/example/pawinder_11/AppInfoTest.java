
package com.example.pawinder_11;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.pawinder_11.utils.AppInfo;

public class AppInfoTest {

    @Test
    public void formatContainsVersionShaAndBuildType() {
        String info = AppInfo.format();

        assertTrue(info.startsWith(
                BuildConfig.VERSION_NAME + "+" + BuildConfig.GIT_SHA
        ));

        assertTrue(info.endsWith(
                " (" + BuildConfig.BUILD_TYPE + ")"
        ));

        String expected = BuildConfig.VERSION_NAME
                + "+"
                + BuildConfig.GIT_SHA
                + (BuildConfig.GIT_DIRTY ? "-dirty" : "")
                + " ("
                + BuildConfig.BUILD_TYPE
                + ")";

        assertEquals(expected, info);
    }
}
