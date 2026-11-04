package com.example.pawinder_11;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.pawinder_11.logic.feed.FeedLogic;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Модульные тесты логики ленты (ПЗ №2). Ожидаемые значения взяты из контракта FeedLogic
 * и требований FR-05, FR-10, а не из запуска проверяемых методов.
 */
public class FeedLogicTest {

    private static final String BASE = "https://example.test";

    // ---- resolvePhotoUrl ----

    @Test
    public void tcFd01_nullUrl_returnsNull() {
        assertNull(FeedLogic.resolvePhotoUrl(null, BASE));
    }

    @Test
    public void tcFd02_emptyUrl_returnsNull() {
        assertNull(FeedLogic.resolvePhotoUrl("", BASE));
    }

    @Test
    public void tcFd03_blankUrl_returnsNull() {
        assertNull(FeedLogic.resolvePhotoUrl("   ", BASE));
    }

    @Test
    public void tcFd04_relativePath_getsBaseUrl() {
        assertEquals("https://example.test/images/rex.jpg",
                FeedLogic.resolvePhotoUrl("/images/rex.jpg", BASE));
    }

    @Test
    public void tcFd05_absoluteHttps_unchanged() {
        assertEquals("https://cdn.example.com/a.jpg",
                FeedLogic.resolvePhotoUrl("https://cdn.example.com/a.jpg", BASE));
    }

    @Test
    public void tcFd06_absoluteHttp_unchanged() {
        assertEquals("http://cdn.example.com/a.jpg",
                FeedLogic.resolvePhotoUrl("http://cdn.example.com/a.jpg", BASE));
    }

    @Test
    public void tcFd07_onlySlash_boundary() {
        assertEquals("https://example.test/", FeedLogic.resolvePhotoUrl("/", BASE));
    }

    @Test
    public void tcFd08_spacesAroundRelativePath_trimmedThenPrefixed() {
        assertEquals("https://example.test/images/rex.jpg",
                FeedLogic.resolvePhotoUrl("  /images/rex.jpg  ", BASE));
    }

    @Test
    public void tcFd09_pathWithoutLeadingSlash_unchanged() {
        assertEquals("images/rex.jpg", FeedLogic.resolvePhotoUrl("images/rex.jpg", BASE));
    }

    // ---- isUsableResponse (таблица решений) ----

    @Test
    public void tcFd10_successWithBody_usable() {
        List<String> body = new ArrayList<>();
        body.add("pet");
        assertTrue(FeedLogic.isUsableResponse(true, body));
    }

    @Test
    public void tcFd11_successWithEmptyList_usable() {
        assertTrue(FeedLogic.isUsableResponse(true, Collections.emptyList()));
    }

    @Test
    public void tcFd12_successWithoutBody_notUsable() {
        assertFalse(FeedLogic.isUsableResponse(true, null));
    }

    @Test
    public void tcFd13_failureWithBody_notUsable() {
        assertFalse(FeedLogic.isUsableResponse(false, Collections.singletonList("pet")));
    }

    @Test
    public void tcFd14_failureWithoutBody_notUsable() {
        assertFalse(FeedLogic.isUsableResponse(false, null));
    }

    // ---- networkErrorMessage ----

    @Test
    public void tcFd15_messagePresent_addedAfterPrefix() {
        assertEquals("Ошибка сети: timeout",
                FeedLogic.networkErrorMessage(new RuntimeException("timeout")));
    }

    @Test
    public void tcFd16_messageNull_prefixOnly() {
        assertEquals("Ошибка сети", FeedLogic.networkErrorMessage(new RuntimeException()));
    }

    @Test
    public void tcFd17_messageBlank_prefixOnly() {
        assertEquals("Ошибка сети", FeedLogic.networkErrorMessage(new RuntimeException("  ")));
    }

    @Test
    public void tcFd18_throwableNull_prefixOnly() {
        assertEquals("Ошибка сети", FeedLogic.networkErrorMessage(null));
    }
}