package UrlUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UrlUtils {
  private static Map<String, String> urlMap = new HashMap<>();
  private static Map<String, String> userNames = new HashMap<>();
  private static Map<String, Integer> clickCountMap = new HashMap<>();
  private static Map<String, Integer> clickLimitMap = new HashMap<>();
  private static Map<String, Long> expirationMap = new HashMap<>();
  private static Map<String, String> urlOwnerMap = new HashMap<>();
  private static final int DEFAULT_CLICK_LIMIT = 5;
  private static final long DEFAULT_EXPIRATION_TIME = 24 * 60 * 60 * 1000;

  public static String shortenURL(String longUrl, String userId) {
    String shortCode = generateShortCode();
    urlMap.put(shortCode, longUrl);
    clickCountMap.put(shortCode, 0);
    clickLimitMap.put(shortCode, DEFAULT_CLICK_LIMIT);
    expirationMap.put(shortCode, System.currentTimeMillis() + DEFAULT_EXPIRATION_TIME);
    urlOwnerMap.put(shortCode, userId);
    return formatShortUrl(shortCode);
  }

  public static String formatShortUrl(String shortCode) {
    return "clck.ru/" + shortCode;
  }

  public static String shortenURL(String longUrl, int clickLimit, String userId) {
    String shortCode = generateShortCode();
    urlMap.put(shortCode, longUrl);
    clickCountMap.put(shortCode, 0);
    clickLimitMap.put(shortCode, clickLimit);
    expirationMap.put(shortCode, System.currentTimeMillis() + DEFAULT_EXPIRATION_TIME);
    urlOwnerMap.put(shortCode, userId);
    return formatShortUrl(shortCode);
  }

  private static String generateShortCode() {
    return UUID.randomUUID().toString().substring(0, 8);
  }

  public static String generateUserId() {
    return UUID.randomUUID().toString();
  }

  public static String getLongURL(String shortCode) {
    // Убираем префикс "clck.ru/" если он есть
    shortCode = shortCode.replace("clck.ru/", "");
    if (!isUrlActive(shortCode)) {
      return null;
    }
    String longUrl = urlMap.get(shortCode);
    int currentClicks = clickCountMap.getOrDefault(shortCode, 0);
    clickCountMap.put(shortCode, currentClicks + 1);
    if (getRemainingClicks(shortCode) <= 0) {
      removeUrl(shortCode);
      return null;
    }
    return longUrl;
  }

  public static boolean isUrlActive(String shortCode) {
    if (!urlMap.containsKey(shortCode)) {
      return false;
    }
    long expirationTime = expirationMap.getOrDefault(shortCode, 0L);
    if (System.currentTimeMillis() > expirationTime) {
      removeUrl(shortCode);
      return false;
    }
    int currentClicks = clickCountMap.getOrDefault(shortCode, 0);
    int limit = clickLimitMap.getOrDefault(shortCode, DEFAULT_CLICK_LIMIT);
    return currentClicks < limit;
  }

  public static boolean deleteUrl(String shortCode, String userId) {
    if (!isUrlOwner(shortCode, userId)) {
      return false;
    }
    if (!urlMap.containsKey(shortCode)) {
      return false;
    }
    removeUrl(shortCode);
    return true;
  }

  public static boolean isUrlOwner(String shortCode, String userId) {
    return userId != null && userId.equals(urlOwnerMap.get(shortCode));
  }

  public static int getRemainingClicks(String shortCode) {
    if (!urlMap.containsKey(shortCode)) {
      return 0;
    }
    int currentClicks = clickCountMap.getOrDefault(shortCode, 0);
    int limit = clickLimitMap.getOrDefault(shortCode, DEFAULT_CLICK_LIMIT);
    return Math.max(0, limit - currentClicks);
  }

  private static void removeUrl(String shortCode) {
    urlMap.remove(shortCode);
    clickCountMap.remove(shortCode);
    clickLimitMap.remove(shortCode);
    expirationMap.remove(shortCode);
    urlOwnerMap.remove(shortCode);
  }

  public static String createUser(String userName) {
    String userId = generateUserId();
    userNames.put(userId, userName);
    return userId;
  }

  public static String getUserName(String userId) {
    return userNames.get(userId);
  }

  public static Map<String, String> getAllUsers() {
    return new HashMap<>(userNames);
  }

  public static boolean userExists(String userName) {
    return userNames.containsValue(userName);
  }

  public static String getUserIdByName(String userName) {
    return userNames.entrySet()
            .stream()
            .filter(entry -> entry.getValue().equals(userName))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
  }
}