package com.student;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class App { // Rename to "Main" if your file is named Main.java
    public static void main(String[] args) {
        try {
            Document doc = Jsoup.connect("https://gutenberg.org").get();
            System.out.println(doc.title());
        } catch (Exception e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
    }
}
