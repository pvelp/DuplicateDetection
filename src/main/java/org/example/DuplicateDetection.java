package org.example;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.*;

public class DuplicateDetection {

    // **1. Канонизация текста**
    public static String canonizeText(String text) {
        // Приводим текст к нижнему регистру, удаляем лишние символы и пробелы
        return text.toLowerCase().replaceAll("[^a-zа-я0-9]", " ").replaceAll("\\s+", " ").trim();
    }

    // **2. Построение шинглов (например, 3-граммы)**
    public static List<String> generateShingles(String text, int shingleSize) {
        List<String> shingles = new ArrayList<>();
        for (int i = 0; i <= text.length() - shingleSize; i++) {
            shingles.add(text.substring(i, i + shingleSize));
        }
        return shingles;
    }

    // **3. Вычисление хэшей для шинглов на основе MinHash**
    public static int[] computeMinHashSignatures(Set<String> shingles, int numHashFunctions) {
        int[] signatures = new int[numHashFunctions];
        Arrays.fill(signatures, Integer.MAX_VALUE);

        for (String shingle : shingles) {
            for (int i = 0; i < numHashFunctions; i++) {
                int hash = hashFunction(shingle, i); // Хэшируем шингл для конкретной функции хэширования
                signatures[i] = Math.min(signatures[i], hash); // Выбираем минимальный хэш
            }
        }

        return signatures;
    }

    // Пример простой хэш-функции
    public static int hashFunction(String value, int seed) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update((seed + value).getBytes(StandardCharsets.UTF_8));
            return Arrays.hashCode(md.digest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // **4. Сравнение хэшей для определения схожести**
    public static double calculateSimilarity(int[] sig1, int[] sig2) {
        if (sig1.length != sig2.length)
            throw new IllegalArgumentException("Signatures length must be the same!");

        int matches = 0; // Количество совпадающих хэшей в сигнатурах
        for (int i = 0; i < sig1.length; i++) {
            if (sig1[i] == sig2[i]) {
                matches++;
            }
        }

        return (double) matches / sig1.length;
    }

    public static void main(String[] args) {
//        String text1 = "Пример текста для проверки дубликатов.";
//        String text2 = "Текст проверки похожий на первый пример.";
        try {
            // Чтение текстов из файлов
            String text1 = Files.readString(Paths.get("texts/text1.txt"), StandardCharsets.UTF_8);
            String text2 = Files.readString(Paths.get("texts/text2.txt"), StandardCharsets.UTF_8);
            String text3 = Files.readString(Paths.get("texts/text3.txt"), StandardCharsets.UTF_8);

            // Шаг 1: Канонизация текстов
            String canonicalText1 = canonizeText(text1);
            String canonicalText2 = canonizeText(text2);
            String canonicalText3 = canonizeText(text3);

            System.out.println("Канонизованный текст 1: " + canonicalText1);
            System.out.println("Канонизованный текст 2: " + canonicalText2);
            System.out.println("Канонизованный текст 3: " + canonicalText3);

            // Шаг 2: Генерация шинглов
            List<String> shingles1 = generateShingles(canonicalText1, 3); // 3-граммы
            List<String> shingles2 = generateShingles(canonicalText2, 3);
            List<String> shingles3 = generateShingles(canonicalText3, 3);

            Set<String> uniqueShingles1 = new HashSet<>(shingles1);
            Set<String> uniqueShingles2 = new HashSet<>(shingles2);
            Set<String> uniqueShingles3 = new HashSet<>(shingles3);

            System.out.println("Шинглы текст 1: " + uniqueShingles1);
            System.out.println("Шинглы текст 2: " + uniqueShingles2);
            System.out.println("Шинглы текст 3: " + uniqueShingles3);

            // Шаг 3: Вычисление хэшей MinHash
            int numHashFunctions = 100; // Количество хэш-функций
            int[] minHash1 = computeMinHashSignatures(uniqueShingles1, numHashFunctions);
            int[] minHash2 = computeMinHashSignatures(uniqueShingles2, numHashFunctions);
            int[] minHash3 = computeMinHashSignatures(uniqueShingles3, numHashFunctions);

            // Шаг 4: Сравнение хэшей
            double similarity = calculateSimilarity(minHash1, minHash2);
            System.out.println("Схожесть текстов 1 и 2: " + similarity);

            similarity = calculateSimilarity(minHash1, minHash3);
            System.out.println("Схожесть текстов 1 и 3: " + similarity);

            similarity = calculateSimilarity(minHash2, minHash3);
            System.out.println("Схожесть текстов 2 и 3: " + similarity);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
