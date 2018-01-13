package me.viachm;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {

    private static final String BASE_URL = "http://loveread.ec/read_book.php";
    private static final String FILE_NAME = "book.txt";
    private static Integer bookId = 1759;

    public static void main(String[] args) {
        System.out.println("Starting book download...");
        Integer numberOfPages = getNumberOfPages(getPageUrl(BASE_URL, bookId, 1));
        ExecutorService executor = Executors.newFixedThreadPool(numberOfPages);

        try {
            List<Callable<String>> crawlers = getPageCrawlers(BASE_URL, numberOfPages);
            List<Future<String>> pages = executor.invokeAll(crawlers);

            writePagesToFile(FILE_NAME, pages);
            System.out.println("Successfully downloaded a book.");
        } catch (InterruptedException e) {
            System.out.println("Error while book downloading: " + e);
        } finally {
            executor.shutdown();
        }
    }

    private static String getPageUrl(String baseUrl, Integer bookId, Integer pageId) {
        return baseUrl + "?id=" + bookId + "&p=" + pageId;
    }

    private static int getNumberOfPages(String url) {
        Set<Integer> pages = new HashSet<>();

        try {
            Document document = Jsoup.connect(url).get();
            Elements pageLinks = document.getElementsByClass("navigation").select("a");
            pageLinks.forEach(pageLink -> {
                String text = pageLink.text();
                try {
                    Integer pageNumber = Integer.parseInt(text);
                    pages.add(pageNumber);
                } catch(NumberFormatException ignored) {
                }
            });

        } catch (IOException e) {
            System.err.println("Error while pages number parsing for url '" + url + "': " + e.getMessage());
        }

        return Collections.max(pages);
    }

    private static List<Callable<String>> getPageCrawlers(String baseUrl, Integer numberOfPages) {
        List<Callable<String>> crawlers = new ArrayList<>();

        for (int i = 1; i <= numberOfPages; i++) {
            String url = getPageUrl(baseUrl, bookId, i);
            crawlers.add(() -> {
                StringBuilder text = new StringBuilder();

                try {
                    System.out.println("Processing url " + url);
                    Document document = Jsoup.connect(url).get();
                    Elements paragraphs = document.select("p.MsoNormal").not("style");

                    for (Element paragraph : paragraphs) {
                        text.append(paragraph.text());
                        text.append("\n");
                    }
                } catch (IOException e) {
                    System.err.println("Error while page parsing for url '" + url + "': " + e.getMessage());
                }

                return text.toString();
            });
        }

        return crawlers;
    }

    private static void writePagesToFile(String fileName, List<Future<String>> pages) {
        try (FileOutputStream out = new FileOutputStream(fileName)) {
            pages.forEach(page -> {
                try {
                    String text = page.get();
                    out.write(text.getBytes());
                } catch (InterruptedException | IOException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            System.out.println("Error while writing pages to file: " + e);
        }
    }

}
