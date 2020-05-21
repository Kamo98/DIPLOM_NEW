package ru.vkr.vkr.domain;
// Java-программа генерирует случайную буквенно-цифровую строку
// используя метод Math.random ()

public class RandomString {
    // функция для генерации случайной строки длиной n
    public static String getAlphaNumericString(int n) {
        // выбрал символ случайный из этой строки
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";
        // создаем StringBuffer размером AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {
            // генерируем случайное число между
            // 0 переменной длины AlphaNumericString
            int index = (int) (AlphaNumericString.length() * Math.random());
            // добавляем символ один за другим в конец sb
            sb.append(AlphaNumericString.charAt(index));
        }
        return sb.toString();
    }
}