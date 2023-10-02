package org.example;


import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import  org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;


public class App {
    static String FILENAME;
    static String URL_AUTHOR;
    static boolean continueProcessing = true;
    public static void main( String[] args ) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Введите ссылку на автора: ");
            URL_AUTHOR = scanner.nextLine();

            if (isValidURL(URL_AUTHOR)) {
                break; // Выход из цикла, если введенная ссылка правильная
            } else {
                System.out.println("Вы ввели неправильную ссылку. Пожалуйста, введите ссылку, начинающуюся с 'https://rustih.ru/'.");
            }
        }

        System.out.println("Введите название файла, в котором вы хотите увидеть его стихи: ");
        Scanner scanner1 = new Scanner(System.in);
        FILENAME = scanner1.nextLine();

        ArrayList<String> linkList = findAllLinks();
        for(String s : linkList){
            parseToFile(s);
        }

        while (continueProcessing) {
            System.out.println("Вы хотите увидеть количество всех встречающихся слов у этого поэта? да/нет ");
            Scanner scanner2 = new Scanner(System.in);
            String answer = scanner2.nextLine();
            if (answer.equals("да")) {
                ArrayList<String> wordList = read();
                int countAllWords = wordList.size();
                System.out.println("Всего слов: " + countAllWords);
                Map<String, Integer> wordCount = getListWords(wordList);

                // Преобразуем мапу в список записей (entrySet)
                List<Map.Entry<String, Integer>> entryList = new ArrayList<>(wordCount.entrySet());

                // Сортируем список записей по значениям (value) в порядке убывания
                entryList.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

                // Выводим отсортированные результаты на консоль
                for (Map.Entry<String, Integer> entry : entryList) {
                    String word = entry.getKey();
                    int count = entry.getValue();
                    System.out.println(word + " || " + count + " раз");
                }
                continueProcessing = false;
            } else if (answer.equals("нет")) {
                System.out.println("END");
                continueProcessing = false;
            } else {
                System.out.println("Некорректный ответ. Пожалуйста, введите 'да' или 'нет'.");
            }
        }
    }

    // Возвращает все ссылки на стихи, найденные под каким-то автором
    // Например, если URL = https://rustih.ru/nikolaj-nekrasov , то найдутся Дедушка Мазай и зайцы, Родина и т.д.
    public static ArrayList<String> findAllLinks(){
        String url = URL_AUTHOR;

        // Уберите завершающий слеш, если он есть
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        String startUrl = url + "-";

        ArrayList<String> linkList = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(url).get();

            Elements links = doc.select("a");

            for(Element link : links){
                String linkHref = link.attr("href");
                if(linkHref.startsWith(startUrl)){
                    linkList.add(linkHref);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return linkList;
    }


    // Парсит содержание стиха из сайта с ссылкой urlStih и записывает его в FILENAME.txt
    public static void parseToFile(String urlStih){
        try {
            Document document = Jsoup.connect(urlStih).get();

            Elements targetElement = document.select("div.entry-content.poem-text");
            Elements spans = targetElement.select("h2");

            FileWriter fileWriter = new FileWriter(FILENAME, true);

            Elements par = spans.prevAll().select("p");
            for(int i = par.size() - 1; i > 0; i--){
                String text = par.get(i).text();
                fileWriter.write(text + "\n");
            }

            fileWriter.close();
        } catch (HttpStatusException e) {
            int statusCode = e.getStatusCode();
            if (statusCode == 503) {
                // Обработка случая, когда сервер вернул статус 503
                System.out.println("Сервер " + urlStih + " недоступен.");
            } else {
                // Обработка других HTTP-статусов
                System.out.println("Произошла ошибка при получении данных: " + statusCode);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    // Возвращает список из слов, содержащихся в FILENAME.txt
    public static ArrayList<String> read(){
        String filePath = FILENAME;
        ArrayList<String> wordList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))){

            String text;
            while ((text = reader.readLine()) != null){
                String[] words = text.split(" ");
                for(String word : words){
                    wordList.add(word);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return wordList;
    }

    // Возвращает количество раз, встреченных слова uniqueWord из wordList
    public static int getUniqueWords(ArrayList<String> wordList, String uniqueWord){
        int wordCount = 0;
        for(String word : wordList){
            word = word.replaceAll("[^a-zA-Zа-яА-Я]", "");
            if (word.equalsIgnoreCase(uniqueWord)) {
                wordCount++;
            }
        }
        return wordCount;
    }

    // Возвращает мапу, в которой содержится слова и их количество раз встреченных в тексте
    public static Map<String, Integer> getListWords(ArrayList<String> wordList){
        Map<String, Integer> wordCount = new HashMap();
        for(String word : wordList){
            word = word.replaceAll("[^a-zA-Zа-яА-Я]", "");
            word = word.toLowerCase();
            int count = wordCount.getOrDefault(word, 0);
            count++;
            wordCount.put(word, count);
        }
        return wordCount;
    }

    // Метод для проверки валидности ссылки
    private static boolean isValidURL(String url) {
        return url.startsWith("https://rustih.ru/");
    }
}
