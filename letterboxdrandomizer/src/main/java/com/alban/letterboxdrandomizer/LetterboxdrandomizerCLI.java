package com.alban.letterboxdrandomizer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LetterboxdrandomizerCLI {
    private static final Set<Map<String, String>> global_watchlist = ConcurrentHashMap.newKeySet();

    // Terminal colors
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static void main(String[] args) {
        System.out.println(ANSI_BLUE + "----------| Letterboxd Randomizer |----------\n" + ANSI_RESET);

        display_menu();

    }

    public static void display_menu() {
        System.out.println("1. Search watchlist(s)\n2. Search custom list\n");

        Scanner scanner = new Scanner(System.in);
        int input = scanner.nextInt();

        switch (input) {
            case 1:
                watchlistRandomizer();
                break;
            case 2:
                customlistRandomizer();
                break;
            default:
                System.out.println("Invalid input");
        }
    }

    public static void customlistRandomizer() {
        Scanner scanner = new Scanner(System.in);
        String input;

        System.out.println("\nEnter url of user link");
        input = scanner.nextLine().trim();

        List<Map<String, String>> get_list = get_custom_list(input);
        System.out.println(get_list);

        System.out.println(ANSI_YELLOW + "******** RESULTS ********\n" + ANSI_RESET +
                "Random movie: " + get_random(get_list) + ANSI_YELLOW + "\n*************************" + ANSI_RESET);


    }

    public static void watchlistRandomizer() {
        Scanner scanner = new Scanner(System.in);
        ArrayList<String> user_list = new ArrayList<>();
        String input;

        while (true) {
            System.out.println("\nEnter username ('q' or Enter to finish writing)");
            input = scanner.nextLine().trim();

            if (input.isEmpty() || input.equalsIgnoreCase("q")) {
                break;
            }

            user_list.add(input);
            System.out.println(add_user(input));
        }

        if (!user_list.isEmpty()) {
            System.out.println("-------------------------\nTotal movies in global list: " + global_watchlist.size() + "\n-------------------------");

            System.out.println(ANSI_YELLOW + "******** RESULTS ********\n" + ANSI_RESET +
                    "Random movie: " + get_random_movie_from_global() + ANSI_YELLOW + "\n*************************" + ANSI_RESET);
        }
    }

    public static String add_user(String username) {
        List<Map<String, String>> watchlist = get_watchlist(username);

        if (watchlist.isEmpty()) {
            return ANSI_RED + "Failed to fetch " + username + "'s watchlist." + ANSI_RESET;
        }

        global_watchlist.addAll(watchlist);
        return ANSI_GREEN + "User " + username + " added." + ANSI_RESET;
    }

    public static Map<String, String> get_random_movie_from_global() {
        if (global_watchlist.isEmpty()) {
            return Collections.singletonMap(ANSI_RED + "error", "empty list" + ANSI_RESET);
        }

        List<Map<String, String>> movie = new ArrayList<>(global_watchlist);
        return movie.get(new Random().nextInt(movie.size()));
    }

    public static Map<String, String> get_random(List<Map<String, String>> link) {
        if (link.isEmpty()) {
            return Collections.singletonMap("error", "empty list");
        }

        List<Map<String, String>> selected_movie = new ArrayList<>(link);
        return selected_movie.get(new Random().nextInt(selected_movie.size()));
    }

    public static List<Map<String, String>> get_custom_list(String link) {
        Set<Map<String, String>> movie_list = new HashSet<>();
        boolean hasNextPage = true;
        int pages = 1;

        System.out.println("Fetching movie list: " + link);

        while (hasNextPage) {
            try {
                Document doc = Jsoup.connect(link + "page/" + pages + "/").userAgent("Mozilla/5.0").get();
                Elements movies = doc.select("li.poster-container");

                if (movies.isEmpty()) {
                    System.out.println("List is empty");
                    break;
                }

                for (Element movie : movies) {
                    String title = movie.select("img").attr("alt");
                    Map<String, String> film = new HashMap<>();

                    film.put("title", title);

                    movie_list.add(film);
                }

                Elements pagination = doc.select("a.next");
                if (pagination.isEmpty()) {
                    break;
                }
                pages++;

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Movies: " + movie_list);
        return new ArrayList<>(movie_list);
    }

    public static List<Map<String, String>> get_watchlist(String username) {
        String url = "https://letterboxd.com/" + username + "/watchlist/";
        Set<Map<String, String>> movie_list = new HashSet<>();
        boolean hasNextPage = true;
        int pages = 1;

        System.out.println("Fetching: " + url + "\n");

        while (hasNextPage) {
            try {
                Document doc = Jsoup.connect(url + "page/" + pages + "/").userAgent("Mozilla/5.0").get();
                Elements movies = doc.select("li.poster-container");

                if (movies.isEmpty()) {
                    System.out.println("User " + username + " has no movies.");
                    break;
                }

                for (Element movie : movies) {
                    String title = movie.select("img").attr("alt");

                    Map<String,String> film = new HashMap<>();
                    film.put("title", title);

                    movie_list.add(film);
                }

                Elements pagination = doc.select("a.next");
                if (pagination.isEmpty()) {
                    break;
                }
                pages++;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Movies: " + movie_list);
        return new ArrayList<>(movie_list);
    }
}
