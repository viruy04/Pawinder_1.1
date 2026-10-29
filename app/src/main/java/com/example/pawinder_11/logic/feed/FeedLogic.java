package com.example.pawinder_11.logic.feed;

import java.util.List;

/**
 * Логика ленты, выделенная из FeedAdapter и FeedFragment (без Android, сети и Glide),
 * чтобы её можно было проверять модульными тестами на JVM.
 * Контракт: docs/pz02/feed-contract (раздел 2 отчёта).
 */
public final class FeedLogic {

    public static final String LOAD_ERROR_MESSAGE = "Не удалось загрузить ленту";
    public static final String NETWORK_ERROR_PREFIX = "Ошибка сети";

    private FeedLogic() {
    }

    /**
     * Адрес фото для загрузки.
     * Предусловие: baseUrl задан без завершающего символа "/".
     * null или пустая (после trim) строка -> null (нужна заглушка);
     * строка, начинающаяся с "/" (после trim) -> baseUrl + строка;
     * иначе строка возвращается без изменений (после trim).
     */
    public static String resolvePhotoUrl(String photoUrl, String baseUrl) {
        if (photoUrl == null) {
            return null;
        }
        String url = photoUrl.trim();
        if (url.isEmpty()) {
            return null;
        }
        if (url.startsWith("/")) {
            return baseUrl + url;
        }
        return url;
    }

    /**
     * Можно ли применить ответ сервера к ленте:
     * только успешный ответ с непустым телом (пустой список допустим).
     */
    public static boolean isUsableResponse(boolean successful, List<?> body) {
        return successful && body != null;
    }

    /**
     * Сообщение при сбое сети: "Ошибка сети: <текст>"; если текста нет
     * (null, пусто) или передан null -> "Ошибка сети".
     */
    public static String networkErrorMessage(Throwable t) {
        String message = (t == null) ? null : t.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return NETWORK_ERROR_PREFIX;
        }
        return NETWORK_ERROR_PREFIX + ": " + message;
    }
}