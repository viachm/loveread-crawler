package me.viachm;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileOutputStream;
import java.io.IOException;

public class Main {

    private static final String BASE_URL = "http://loveread.ec/read_book.php";
    private static final String FILE_NAME = "book.txt";
    private static Integer numberOfPages = 58;
    private static Integer bookId = 70180;

    public static void main(String[] args) {
        try (FileOutputStream out = new FileOutputStream(FILE_NAME)) {
            System.out.println("Starting book download...");
            for (int i = 1; i <= numberOfPages; i++) {
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
            Elements linksOnPage = document.select("p").not("style");

            for (Element element : linksOnPage) {
                text.append(element.text());
                text.append("\n");
            }
        } catch (IOException e) {
            System.err.println("Error while page parsing for url '" + url + "': " + e.getMessage());
        }

        return text.toString();
    }

    private static String getPageUrl(String baseUrl, Integer bookId, Integer pageId) {
        return baseUrl + "?id=" + bookId + "&p=" + pageId;
    }
}
