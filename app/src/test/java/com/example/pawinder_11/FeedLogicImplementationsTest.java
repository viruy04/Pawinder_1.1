
package com.example.pawinder_11;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.pawinder_11.logic.feed.AlternativeFeedLogic;
import com.example.pawinder_11.logic.feed.DefaultFeedLogic;
import com.example.pawinder_11.logic.feed.FeedLogicContract;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class FeedLogicImplementationsTest {

    private static final String BASE = "https://example.test";

    private final FeedLogicContract[] implementations = {
            new DefaultFeedLogic(),
            new AlternativeFeedLogic()
    };

    @Test
    public void resolvePhotoUrl_normalBoundaryAndInvalidInputs() {
        for (FeedLogicContract logic : implementations) {
            assertNull(logic.resolvePhotoUrl(null, BASE));
            assertNull(logic.resolvePhotoUrl("", BASE));
            assertNull(logic.resolvePhotoUrl("   ", BASE));

            assertEquals(BASE + "/images/rex.jpg",
                    logic.resolvePhotoUrl("/images/rex.jpg", BASE));

            assertEquals(BASE + "/",
                    logic.resolvePhotoUrl("/", BASE));

            assertEquals("https://cdn.example.com/a.jpg",
                    logic.resolvePhotoUrl("https://cdn.example.com/a.jpg", BASE));

            assertEquals("images/rex.jpg",
                    logic.resolvePhotoUrl("images/rex.jpg", BASE));

            assertEquals(BASE + "/images/rex.jpg",
                    logic.resolvePhotoUrl("  /images/rex.jpg  ", BASE));
        }
    }

    @Test
    public void isUsableResponse_normalBoundaryAndInvalidInputs() {
        for (FeedLogicContract logic : implementations) {
            List<String> body = Collections.singletonList("pet");

            assertTrue(logic.isUsableResponse(true, body));
            assertTrue(logic.isUsableResponse(true, Collections.emptyList()));

            assertFalse(logic.isUsableResponse(true, null));
            assertFalse(logic.isUsableResponse(false, body));
            assertFalse(logic.isUsableResponse(false, null));
        }
    }

    @Test
    public void networkErrorMessage_normalBoundaryAndInvalidInputs() {
        for (FeedLogicContract logic : implementations) {
            assertEquals("Ошибка сети: timeout",
                    logic.networkErrorMessage(new RuntimeException("timeout")));

            assertEquals("Ошибка сети",
                    logic.networkErrorMessage(new RuntimeException()));

            assertEquals("Ошибка сети",
                    logic.networkErrorMessage(new RuntimeException("  ")));

            assertEquals("Ошибка сети",
                    logic.networkErrorMessage(null));
        }
    }
}
