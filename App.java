//student roll number :202511181


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Set;

public class App {

    private static final int MAX_BOOKS = 20;
    private static final String TOP_BOOKS_URL = "https://www.gutenberg.org/browse/scores/top";
    private static final String SEARCH_BASE_URL = "https://www.gutenberg.org/ebooks/search/?query=";

    // Fixed-size array holding at least 20 books (no ArrayList)
    private static final String[] readingListTitles = new String[MAX_BOOKS];
    private static final String[] readingListAuthors = new String[MAX_BOOKS];
    private static final String[] readingListUrls = new String[MAX_BOOKS];
    private static int readingListCount = 0;

    // Store the most recently fetched/searched books so users can easily pick one by number
    private static String[] lastFetchedTitles = new String[0];
    private static String[] lastFetchedAuthors = new String[0];
    private static String[] lastFetchedUrls = new String[0];

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int choice;

        do {
            printMenu();
            choice = readInt(scanner, "Enter your choice: ");

            switch (choice) {
                case 1 -> exploreBooks();
                case 2 -> {
                    System.out.print("Enter book title to search: ");
                    String titleQuery = scanner.nextLine().trim();
                    if (titleQuery.isEmpty()) {
                        System.out.println("Search keyword cannot be empty.\n");
                    } else {
                        searchBook(titleQuery);
                    }
                }
                case 3 -> {
                    System.out.print("Enter author name to search: ");
                    String authorQuery = scanner.nextLine().trim();
                    if (authorQuery.isEmpty()) {
                        System.out.println("Author name cannot be empty.\n");
                    } else {
                        searchBook(authorQuery, true);
                    }
                }
                case 4 -> addBook(scanner);
                case 5 -> viewReadingList();
                case 6 -> removeBook(scanner);
                case 7 -> displayBookDetails(scanner);
                case 0 -> System.out.println("Exiting Gutenberg Book Explorer. Goodbye!");
                default -> System.out.println("Invalid choice. Please enter a number from 0 to 7.\n");
            }
        } while (choice != 0);

        scanner.close();
    }

    private static void printMenu() {
        System.out.println("====================================");
        System.out.println("GUTENBERG BOOK EXPLORER");
        System.out.println("1. Explore Books");
        System.out.println("2. Search by Title");
        System.out.println("3. Search by Author");
        System.out.println("4. Add Book to Reading List");
        System.out.println("5. View Reading List");
        System.out.println("6. Remove Book");
        System.out.println("7. Display Book Details");
        System.out.println("0. Exit");
        System.out.println("====================================");
    }

    // 1. Explore top books from Project Gutenberg
    public static void exploreBooks() {
        System.out.println("\nFetching top books from Project Gutenberg...");
        try {
            Document doc = Jsoup.connect(TOP_BOOKS_URL)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            extractBooksFromDocument(doc, 15);
            displayFetchedResults("Top Project Gutenberg Books");
        } catch (Exception e) {
            System.out.println("Connection error while exploring books: " + e.getMessage() + "\n");
        }
    }

    // 2. Overloaded searchBook (1): Search by title
    public static void searchBook(String title) {
        searchBook(title, false);
    }

    // 3. Overloaded searchBook (2): Search by author when true, otherwise by title
    public static void searchBook(String keyword, boolean searchByAuthor) {
        String modeLabel = searchByAuthor ? "Author" : "Title";
        System.out.println("\nSearching Project Gutenberg by " + modeLabel + " for: \"" + keyword + "\"... ");

        try {
            String encodedQuery = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String searchUrl = SEARCH_BASE_URL + encodedQuery;

            Document doc = Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            extractBooksFromSearchOrCatalog(doc, keyword, searchByAuthor, 15);
            displayFetchedResults("Search Results (" + modeLabel + ": " + keyword + ")");
        } catch (Exception e) {
            System.out.println("Connection error while searching books: " + e.getMessage() + "\n");
        }
    }

    // 4. Add a selected book from the last results into the fixed-size array
    public static void addBook(Scanner scanner) {
        if (readingListCount >= MAX_BOOKS) {
            System.out.println("Reading list is full (maximum " + MAX_BOOKS + " books). Remove a book first.\n");
            return;
        }

        if (lastFetchedTitles.length == 0) {
            System.out.println("No recent books available to select. Run 'Explore Books' or a search first.\n");
            return;
        }

        displayFetchedResults("Available Books");
        int index = readInt(scanner, "Enter the book number to add (1-" + lastFetchedTitles.length + ", or 0 to cancel): ");

        if (index == 0) {
            System.out.println("Cancelled.\n");
            return;
        }

        if (index < 1 || index > lastFetchedTitles.length) {
            System.out.println("Invalid book selection.\n");
            return;
        }

        String selectedTitle = lastFetchedTitles[index - 1];
        String selectedAuthor = lastFetchedAuthors[index - 1];
        String selectedUrl = lastFetchedUrls[index - 1];

        for (int i = 0; i < readingListCount; i++) {
            if (readingListUrls[i].equalsIgnoreCase(selectedUrl)) {
                System.out.println("This book is already in your reading list.\n");
                return;
            }
        }

        readingListTitles[readingListCount] = selectedTitle;
        readingListAuthors[readingListCount] = selectedAuthor;
        readingListUrls[readingListCount] = selectedUrl;
        readingListCount++;

        System.out.println("Added \"" + selectedTitle + "\" to your reading list (" + readingListCount + "/" + MAX_BOOKS + ").\n");
    }

    // 5. View all saved books using a for loop
    public static void viewReadingList() {
        System.out.println("\n--- Personal Reading List (" + readingListCount + "/" + MAX_BOOKS + ") ---");
        if (readingListCount == 0) {
            System.out.println("Your reading list is currently empty.\n");
            return;
        }

        for (int i = 0; i < readingListCount; i++) {
            System.out.println((i + 1) + ". " + readingListTitles[i] + " 鈥� " + readingListAuthors[i]);
        }
        System.out.println();
    }

    // 6. Remove a book from the reading list and shift elements to keep array compact
    public static void removeBook(Scanner scanner) {
        if (readingListCount == 0) {
            System.out.println("Your reading list is empty. Nothing to remove.\n");
            return;
        }

        viewReadingList();
        int index = readInt(scanner, "Enter the number of the book to remove (1-" + readingListCount + ", or 0 to cancel): ");

        if (index == 0) {
            System.out.println("Removal cancelled.\n");
            return;
        }

        if (index < 1 || index > readingListCount) {
            System.out.println("Invalid book number.\n");
            return;
        }

        String removedTitle = readingListTitles[index - 1];

        for (int i = index - 1; i < readingListCount - 1; i++) {
            readingListTitles[i] = readingListTitles[i + 1];
            readingListAuthors[i] = readingListAuthors[i + 1];
            readingListUrls[i] = readingListUrls[i + 1];
        }

        readingListTitles[readingListCount - 1] = null;
        readingListAuthors[readingListCount - 1] = null;
        readingListUrls[readingListCount - 1] = null;
        readingListCount--;

        System.out.println("Removed \"" + removedTitle + "\" from your reading list.\n");
    }

    // 7. Display full details (title, author, URL) for a book
    public static void displayBookDetails(Scanner scanner) {
        System.out.println("\nView details from:");
        System.out.println("1. Personal Reading List");
        System.out.println("2. Last Explored / Searched Results");
        int sourceChoice = readInt(scanner, "Choose source (1 or 2): ");

        if (sourceChoice == 1) {
            if (readingListCount == 0) {
                System.out.println("Reading list is empty.\n");
                return;
            }
            viewReadingList();
            int idx = readInt(scanner, "Enter book number: ");
            if (idx < 1 || idx > readingListCount) {
                System.out.println("Invalid book number.\n");
                return;
            }
            printSingleBookDetails(readingListTitles[idx - 1], readingListAuthors[idx - 1], readingListUrls[idx - 1]);
        } else if (sourceChoice == 2) {
            if (lastFetchedTitles.length == 0) {
                System.out.println("No explored or searched books available yet.\n");
                return;
            }
            displayFetchedResults("Last Results");
            int idx = readInt(scanner, "Enter book number: ");
            if (idx < 1 || idx > lastFetchedTitles.length) {
                System.out.println("Invalid book number.\n");
                return;
            }
            printSingleBookDetails(lastFetchedTitles[idx - 1], lastFetchedAuthors[idx - 1], lastFetchedUrls[idx - 1]);
        } else {
            System.out.println("Invalid option.\n");
        }
    }

    private static void printSingleBookDetails(String title, String author, String url) {
        System.out.println("\n--- Book Details ---");
        System.out.println("Title : " + title);
        System.out.println("Author: " + author);
        System.out.println("URL   : " + url);
        System.out.println("--------------------\n");
    }

    private static void extractBooksFromDocument(Document doc, int limit) {
        Elements links = doc.select("a[href^=/ebooks/]");
        Set<String> seenUrls = new LinkedHashSet<>();

        String[] tempTitles = new String[limit];
        String[] tempAuthors = new String[limit];
        String[] tempUrls = new String[limit];
        int found = 0;

        for (Element link : links) {
            String href = link.attr("href").trim();
            if (!isNumericEbookHref(href)) {
                continue;
            }

            String absUrl = link.absUrl("href");
            if (seenUrls.contains(absUrl)) {
                continue;
            }

            String rawText = link.text().trim();
            if (rawText.isEmpty() || rawText.toLowerCase().contains("next page")) {
                continue;
            }

            seenUrls.add(absUrl);
            tempTitles[found] = parseTitle(rawText);
            tempAuthors[found] = parseAuthor(link, rawText);
            tempUrls[found] = absUrl;
            found++;

            if (found >= limit) {
                break;
            }
        }

        storeFetchedResults(tempTitles, tempAuthors, tempUrls, found);
    }

    private static void extractBooksFromSearchOrCatalog(Document doc, String keyword, boolean searchByAuthor, int limit) {
        Elements bookItems = doc.select("li.booklink");
        Set<String> seenUrls = new LinkedHashSet<>();

        String[] tempTitles = new String[limit];
        String[] tempAuthors = new String[limit];
        String[] tempUrls = new String[limit];
        int found = 0;
        String lowerKeyword = keyword.toLowerCase();

        for (Element item : bookItems) {
            Element link = item.selectFirst("a[href^=/ebooks/]");
            if (link == null) {
                continue;
            }
            String href = link.attr("href").trim();
            if (!isNumericEbookHref(href)) {
                continue;
            }

            String absUrl = link.absUrl("href");
            if (seenUrls.contains(absUrl)) {
                continue;
            }

            Element titleEl = item.selectFirst(".title");
            Element subtitleEl = item.selectFirst(".subtitle");
            String title = (titleEl != null && !titleEl.text().isBlank())
                    ? titleEl.text().trim()
                    : parseTitle(link.text().trim());

            String author = (subtitleEl != null && !subtitleEl.text().isBlank())
                    ? subtitleEl.text().trim()
                    : "Unknown Author";

            boolean matches = searchByAuthor
                    ? author.toLowerCase().contains(lowerKeyword)
                    : (title.toLowerCase().contains(lowerKeyword) || link.text().toLowerCase().contains(lowerKeyword));

            if (!matches) {
                continue;
            }

            seenUrls.add(absUrl);
            tempTitles[found] = title;
            tempAuthors[found] = author;
            tempUrls[found] = absUrl;
            found++;

            if (found >= limit) {
                break;
            }
        }

        if (found == 0) {
            Elements links = doc.select("a[href^=/ebooks/]");
            for (Element link : links) {
                String href = link.attr("href").trim();
                if (!isNumericEbookHref(href)) {
                    continue;
                }
                String absUrl = link.absUrl("href");
                if (seenUrls.contains(absUrl)) {
                    continue;
                }
                String text = link.text().trim();
                if (text.isEmpty() || !text.toLowerCase().contains(lowerKeyword)) {
                    continue;
                }
                seenUrls.add(absUrl);
                tempTitles[found] = parseTitle(text);
                tempAuthors[found] = parseAuthor(link, text);
                tempUrls[found] = absUrl;
                found++;
                if (found >= limit) {
                    break;
                }
            }
        }

        storeFetchedResults(tempTitles, tempAuthors, tempUrls, found);
    }

    private static boolean isNumericEbookHref(String href) {
        return href.matches("^/ebooks/\\d+/?$");
    }

    private static String parseTitle(String rawText) {
        int byIndex = rawText.indexOf(" by ");
        if (byIndex > 0) {
            return rawText.substring(0, byIndex).trim();
        }
        return rawText;
    }

    private static String parseAuthor(Element link, String rawText) {
        int byIndex = rawText.indexOf(" by ");
        if (byIndex > 0) {
            String afterBy = rawText.substring(byIndex + 4).trim();
            return afterBy.replaceAll("\\s*\\(\\d+\\)$", "").trim();
        }
        Element parent = link.parent();
        if (parent != null) {
            String parentText = parent.text().trim();
            int parentBy = parentText.indexOf(" by ");
            if (parentBy > 0) {
                return parentText.substring(parentBy + 4).replaceAll("\\s*\\(\\d+\\)$", "").trim();
            }
        }
        return "Unknown Author";
    }

    private static void storeFetchedResults(String[] titles, String[] authors, String[] urls, int count) {
        lastFetchedTitles = new String[count];
        lastFetchedAuthors = new String[count];
        lastFetchedUrls = new String[count];

        for (int i = 0; i < count; i++) {
            lastFetchedTitles[i] = titles[i];
            lastFetchedAuthors[i] = authors[i];
            lastFetchedUrls[i] = urls[i];
        }
    }

    private static void displayFetchedResults(String heading) {
        System.out.println("\n--- " + heading + " ---");
        if (lastFetchedTitles.length == 0) {
            System.out.println("No matching books found.\n");
            return;
        }

        for (int i = 0; i < lastFetchedTitles.length; i++) {
            System.out.println((i + 1) + ". " + lastFetchedTitles[i] + " | Author: " + lastFetchedAuthors[i]);
        }
        System.out.println();
    }

    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }
            }
