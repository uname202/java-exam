package Console;

import UrlUtils.UrlUtils;
import java.util.*;
import java.awt.Desktop;
import java.net.URI;

public class Console {
    private static String currentUserId = null;
    private static Scanner scanner = new Scanner(System.in);

    public static void start() {
        while (true) {
            if (currentUserId == null) {
                System.out.println("\n1. Создать нового пользователя");
                System.out.println("2. Сменить пользователя");
                System.out.println("3. Выход");
            } else {
                String currentUserName = UrlUtils.getUserName(currentUserId);
                System.out.println("\nТекущий пользователь: " + currentUserName);
                System.out.println("1. Создать короткую ссылку");
                System.out.println("2. Открыть короткую ссылку");
                System.out.println("3. Удалить ссылку");
                System.out.println("4. Сменить пользователя");
                System.out.println("5. Выход");
            }

            System.out.print("\nВведите ваш выбор: ");
            String choice = scanner.nextLine();

            if (currentUserId == null) {
                handleUnauthenticatedChoice(choice);
            } else {
                handleAuthenticatedChoice(choice);
            }
        }
    }

    private static void handleUnauthenticatedChoice(String choice) {
        switch (choice) {
            case "1":
                createNewUser();
                break;
            case "2":
                switchUser();
                break;
            case "3":
                System.exit(0);
                break;
            default:
                System.out.println("Invalid choice!");
        }
    }

    private static void handleAuthenticatedChoice(String choice) {
        switch (choice) {
            case "1":
                createShortUrl();
                break;
            case "2":
                openLongUrl();
                break;
            case "3":
                deleteUrl();
                break;
            case "4":
                switchUser();
                break;
            case "5":
                System.exit(0);
                break;
            default:
                System.out.println("Invalid choice!");
        }
    }

    private static void createNewUser() {
        System.out.print("Введите имя пользователя: ");
        String userName = scanner.nextLine();
        currentUserId = UrlUtils.createUser(userName);
        System.out.println("Пользователь успешно создан!");
    }

    private static void switchUser() {
        Map<String, String> users = UrlUtils.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("Нет пользователей! Пожалуйста, создайте пользователя сначала.");
            return;
        }

        System.out.println("\nДоступные пользователи:");
        for (String userName : users.values()) {
            System.out.println(userName);
        }

        System.out.print("\nВведите имя пользователя (если пользователь не существует, будет создан новый): ");
        String userName = scanner.nextLine();

        if (UrlUtils.userExists(userName)) {
            currentUserId = UrlUtils.getUserIdByName(userName);
            System.out.println("Переключено на пользователя: " + userName);
        } else {
            currentUserId = UrlUtils.createUser(userName);
            System.out.println("Пользователь успешно создан!");
        }
    }

    private static void createShortUrl() {
        System.out.print("Введите длинную ссылку: ");
        String longUrl = scanner.nextLine();
        System.out.print("Введите лимит кликов: ");
        String clickLimit = scanner.nextLine();
        int clickLimitInt = 5;
        try {
            clickLimitInt = Integer.parseInt(clickLimit);
        } catch (NumberFormatException e) {
            System.out.print("Не удалось распознать лимит кликов. Установлено значение по умолчанию: 5.");
        }
        String shortUrl = UrlUtils.shortenURL(longUrl, clickLimitInt, currentUserId);
        System.out.println("Короткая ссылка: " + shortUrl);
    }

    private static void openLongUrl() {
        System.out.print("Введите короткую ссылку: ");
        String shortUrl = scanner.nextLine();

        // Проверяем количество оставшихся кликов перед получением URL
        String shortCode = shortUrl.replace("clck.ru/", "");
        int remainingClicks = UrlUtils.getRemainingClicks(shortCode);

        if (remainingClicks <= 0) {
            System.out.println("Достигнут лимит кликов для этой ссылки!");
            return;
        }

        String longUrl = UrlUtils.getLongURL(shortUrl);
        if (longUrl == null) {
            System.out.println("Недействительная или просроченная ссылка!");
            return;
        }

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(longUrl));
                System.out.println("Осталось кликов: " + (remainingClicks - 1));
            } else {
                System.out.println("Открытие браузера не поддерживается в данной системе.");
                System.out.println("Длинная ссылка: " + longUrl);
            }
        } catch (Exception e) {
            System.out.println("Ошибка при открытии ссылки: " + e.getMessage());
            System.out.println("Длинная ссылка: " + longUrl);
        }
    }

    private static void deleteUrl() {
        System.out.println("\nВведите короткую ссылку для удаления (0 для отмены): ");
        String shortUrl = scanner.nextLine();

        if (!shortUrl.equals("0")) {
            if (UrlUtils.deleteUrl(shortUrl, currentUserId)) {
                System.out.println("Ссылка успешно удалена!");
            } else {
                System.out.println("Не удалось удалить ссылку или ссылка не найдена!");
            }
        }
    }
}