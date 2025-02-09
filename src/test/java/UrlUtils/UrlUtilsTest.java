package UrlUtils;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Map;

public class UrlUtilsTest {
    private String userId;
    private String longUrl;

    @BeforeEach
    void setUp() {
        userId = UrlUtils.generateUserId();
        longUrl = "http://yandex.ru";
    }

    @Test
    void testShortenURL() {
        String shortUrl = UrlUtils.shortenURL(longUrl, userId);
        assertNotNull(shortUrl, "Короткая ссылка не должна быть null");
        assertTrue(shortUrl.startsWith("clck.ru/"), "Короткая ссылка должна начинаться с clck.ru/");
        String shortCode = shortUrl.replace("clck.ru/", "");
        assertEquals(8, shortCode.length(), "Длина короткого кода должна быть 8 символов");
        assertEquals(longUrl, UrlUtils.getLongURL(shortUrl), "Длинный URL должен соответствовать исходному");
    }

    @Test
    void testShortenURLWithClickLimit() {
        int clickLimit = 3;
        String shortUrl = UrlUtils.shortenURL(longUrl, clickLimit, userId);
        assertNotNull(shortUrl, "Короткая ссылка не должна быть null");
        assertTrue(shortUrl.startsWith("clck.ru/"), "Короткая ссылка должна начинаться с clck.ru/");

        assertEquals(clickLimit, UrlUtils.getRemainingClicks(shortUrl.replace("clck.ru/", "")),
                "Начальное количество кликов должно соответствовать лимиту");

        // Проверка подсчета кликов
        for (int i = 0; i < clickLimit - 1; i++) {
            assertEquals(longUrl, UrlUtils.getLongURL(shortUrl), "URL должен быть доступен");
            assertEquals(clickLimit - (i + 1), UrlUtils.getRemainingClicks(shortUrl.replace("clck.ru/", "")),
                    "Количество оставшихся кликов должно уменьшаться");
        }

        // Должен вернуть null после превышения лимита
        assertNull(UrlUtils.getLongURL(shortUrl), "URL должен быть недоступен после превышения лимита");
    }

    @Test
    void testUserManagement() {
        String userName = "ТестовыйПользователь";
        String userId = UrlUtils.createUser(userName);
        assertNotNull(userId, "ID пользователя не должен быть null");

        assertEquals(userName, UrlUtils.getUserName(userId), "Имя пользователя должно соответствовать");
        assertTrue(UrlUtils.userExists(userName), "Пользователь должен существовать");
        assertEquals(userId, UrlUtils.getUserIdByName(userName), "ID пользователя должен соответствовать");

        Map<String, String> allUsers = UrlUtils.getAllUsers();
        assertTrue(allUsers.containsKey(userId), "Пользователь должен быть в списке всех пользователей");
        assertEquals(userName, allUsers.get(userId), "Имя пользователя должно соответствовать в списке");
    }

    @Test
    void testUrlOwnership() {
        String shortUrl = UrlUtils.shortenURL(longUrl, userId);
        String shortCode = shortUrl.replace("clck.ru/", "");
        assertTrue(UrlUtils.isUrlOwner(shortCode, userId), "Пользователь должен быть владельцем URL");
        assertFalse(UrlUtils.isUrlOwner(shortCode, "неверныйUserId"),
                "Неверный пользователь не должен быть владельцем URL");
    }

    @Test
    void testDeleteUrl() {
        String shortUrl = UrlUtils.shortenURL(longUrl, userId);
        String shortCode = shortUrl.substring(shortUrl.lastIndexOf("/") + 1);

        // Проверка удаления неверным пользователем
        assertFalse(UrlUtils.deleteUrl(shortCode, "неверныйUserId"),
                "Неверный пользователь не должен иметь возможность удалить URL");
        assertTrue(UrlUtils.isUrlActive(shortCode), "URL должен оставаться активным");

        // Проверка удаления правильным пользователем
        assertTrue(UrlUtils.deleteUrl(shortCode, userId), "Владелец должен иметь возможность удалить URL");
        assertFalse(UrlUtils.isUrlActive(shortCode), "URL не должен быть активным после удаления");
        assertNull(UrlUtils.getLongURL(shortUrl), "URL не должен быть доступен после удаления");
    }

    @Test
    void testUrlExpiration() {
        String shortUrl = UrlUtils.shortenURL(longUrl, userId);
        String shortCode = shortUrl.replace("clck.ru/", "");
        assertTrue(UrlUtils.isUrlActive(shortCode), "URL должен быть активным");

        // Примечание: В реальном тестовом окружении следует использовать
        // библиотеку для манипуляции временем, чтобы протестировать истечение срока
    }

    @Test
    void testRemainingClicks() {
        int clickLimit = 5;
        String shortUrl = UrlUtils.shortenURL(longUrl, clickLimit, userId);
        String shortCode = shortUrl.replace("clck.ru/", "");

        assertEquals(clickLimit, UrlUtils.getRemainingClicks(shortCode),
                "Начальное количество кликов должно соответствовать лимиту");

        String longUrlResult = UrlUtils.getLongURL(shortUrl); // Использовать один раз
        assertNotNull(longUrlResult, "URL должен быть доступен");
        assertEquals(clickLimit - 1, UrlUtils.getRemainingClicks(shortCode),
                "Количество оставшихся кликов должно уменьшиться на 1");
    }
}
