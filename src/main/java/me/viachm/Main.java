package me.viachm;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Main {

    private static final String BASE_URL = "http://loveread.ec/read_book.php";
    private static final String FILE_NAME = "book.txt";
    private static Integer bookId = 70180;

    public static void main(String[] args) {
        try (FileOutputStream out = new FileOutputStream(FILE_NAME)) {
            System.out.println("Starting book download...");
            int pagesNumber = getPagesNumber(getPageUrl(BASE_URL, bookId, 1));
            for (int i = 1; i <= pagesNumber; i++) {
                String url = getPageUrl(BASE_URL, bookId, i);
                String text = parsePage(url);
                out.write(text.getBytes());
            }

            System.out.println("Successfully downloaded a book.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String parsePage(String url) {
        StringBuilder text = new StringBuilder();

        try {
            System.out.println("Processing url " + url);
            Document document = Jsoup.connect(url).get();
            Elements linksOnPage = document.getElementsByTag("div").not(".footer").select("p");

            linksOnPage.forEach(element ->  {
                text.append(element.text());
                text.append("\n");
            });
        } catch (IOException e) {
            System.err.println("Error while page parsing for url '" + url + "': " + e.getMessage());
        }

        return text.toString();
    }

    private static String getPageUrl(String baseUrl, Integer bookId, Integer pageId) {
        return baseUrl + "?id=" + bookId + "&p=" + pageId;
    }

    private static int getPagesNumber(String url) {
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
}
