package com.alban.letterboxdrandomizer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
@RestController
@RequestMapping("/")
public class LetterboxdrandomizerApplication {
	private static final Set<Map<String, String>> global_watchlist = ConcurrentHashMap.newKeySet();
	private static final Map<String, Set<Map<String, String>>> user_watchlists = new ConcurrentHashMap<>();
	private static final List<Map<String, String>> global_popular = new ArrayList<>(); // avoid scraping every time

	public static void main(String[] args) {
		SpringApplication.run(LetterboxdrandomizerApplication.class, args);
	}

	@CrossOrigin(origins = "http://localhost:3000")
	@GetMapping("/randomizer")
	// A class that selects a random movie from the list: global_watchlist
	public Map<String, String> randomizer() {
		return get_random_movie_from_global();
	}

	/**
	 * A class that selects a random movie from the list: global_watchlist
	 * @return fetch_movie_details() : function to get info about the selected random movie
	 */
	public Map<String, String> get_random_movie_from_global() {
		if (global_watchlist.isEmpty()) {
			return Collections.singletonMap("error", "empty list");
		}

		List<Map<String, String>> movie = new ArrayList<>(global_watchlist);
		Map<String, String> random_movie = movie.get(new Random().nextInt(movie.size()));
		return fetch_movie_details(random_movie.get("link"));
	}

	@CrossOrigin(origins = "http://localhost:3000")
	@GetMapping("/get_random_popular")
	/**
	 * A class that selects a random movie by fetching the popular top 50 pages of all time
	 * @return fetch_movie_details() : function to get info about the selected random movie
	 */
	public Map<String, String> get_random_popular() {
		String popular = "https://letterboxd.com/films/ajax/popular/";

		// avoid web-scraping every time
		if (global_popular.isEmpty()) {
			List<Map<String, String>> get_list = fetchList(popular);
			global_popular.addAll(get_list);
		}

		Map<String, String> random_movie = global_popular.get(new Random().nextInt(global_popular.size()));
		return fetch_movie_details(random_movie.get("link"));
	}

	@CrossOrigin(origins = "http://localhost:3000")
	@PostMapping("/get_random_from_cList")
	/**
	 * A class that selects a random movie from a custom list url
	 * @param url - String
	 * @return fetch_movie_details() : function to get info about the selected random movie
	 */
	public Map<String, String> get_random_from_cList(@RequestParam String url) {
		if (url.isEmpty()) {
			return Collections.singletonMap("error", "empty list");
		}

		List<Map<String, String>> get_list = fetchList(url);

		Map<String, String> random_movie = get_list.get(new Random().nextInt(get_list.size()));
		return fetch_movie_details(random_movie.get("link"));
	}

	/**
	 * A class that fetches information about the selected movie through scraping
	 * @param url - String
	 * @return movie_details (HashMap) with details about the selected movie
	 */
	private Map<String, String> fetch_movie_details(String url) {
		Map<String, String> movie_details = new HashMap<>();

		try {
			Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0").get();
			movie_details.put("title", doc.select("h1.headline-1").text());
			movie_details.put("year", doc.select("div.releaseyear").text());
			movie_details.put("director", doc.select("span.directorlist").text());
			String length_text = String.valueOf(doc.select("p.text-link").text().split(" ")[0]);
			movie_details.put("length", length_text);
			movie_details.put("link", url);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return movie_details;
	}

	@CrossOrigin(origins = "http://localhost:3000")
	@PostMapping("/add-user")
	/**
	 * A class that adds an arbitrary user and fetches their watchlist
	 * @param username - String
	 * @return the username that's been added to the list
	 */
	public String add_user(@RequestParam String username) {

		if (user_watchlists.containsKey(username)) {
			return username + " already added.";
		}

		List<Map<String, String>> watchlist = fetchList(username);

		if (watchlist.isEmpty()) {
			return "Failed to fetch " + username + "'s watchlist.";
		}

		global_watchlist.addAll(watchlist);
		user_watchlists.put(username, new HashSet<>(watchlist));
		return "User " + username + " added.";
	}

	@CrossOrigin(origins = "http://localhost:3000")
	@DeleteMapping("/delete-user")
	/**
	 * A class to delete an arbitrary user and their watchlist from the list
	 * @param username - String
	 * @return the username that's been deleted from the list
	 */
	public String delete_user(@RequestParam String username) {
		if (user_watchlists.isEmpty()) {
			return "There are no usernames in the list.";
		}
		if (!user_watchlists.containsKey(username)) {
			return "Username " + username + " not found.";
		}

		Set<Map<String, String>> user_watchlist = user_watchlists.remove(username);
		global_watchlist.removeAll(user_watchlist);

		return "User " + username + "'s watchlist has been removed.";
	}
	@CrossOrigin(origins = "http://localhost:3000")
	@GetMapping("/get-users")
	/**
	 * A class to get all users
	 * @return all users (key values in dict/map)
	 */
	public Set<String> get_users() {
		return user_watchlists.keySet();
	}

	/**
	 * A class to scrape and fetch movies from either users' watchlist, custom list or all-time popular page (limit 50 pages)
	 * @param input - String
	 * @return new ArrayList with the list including all custom / user link movies
	 */
	public List<Map<String, String>> fetchList(String input) {
		String username = "";
		String link = "";

		if (input.contains("https://") && (input.contains("/list/") || input.contains("/popular/"))) {
			link = input;
			System.out.println("Link: " + link);
		}
		else {
			username = input;
		}

		String user_url = "https://letterboxd.com/" + username + "/watchlist/";
		Set<Map<String, String>> movie_list = new HashSet<>();
		boolean hasNextPage = true;
		int pages = 1;

		if (!username.isEmpty()) {
			System.out.println("Fetching: " + user_url + "\n");
		}

		if (!link.isEmpty()) {
			System.out.println("Fetching: " + link + "\n");
		}

		while (hasNextPage) {
			try {
				Document doc = null;

				if (link.isEmpty()) {
					doc = Jsoup.connect(user_url + "page/" + pages + "/").userAgent("Mozilla/5.0").get();
				}
				else if (link.contains("/popular/")) {
					doc = Jsoup.connect(link + "page/" + pages + "/?esiAllowFilters=true").userAgent("Mozilla/5.0").get();
				}
				else
				{
					doc = Jsoup.connect(link + "page/" + pages + "/").userAgent("Mozilla/5.0").get();
				}
				Elements movies = doc.select("li.poster-container");

				if (movies.isEmpty()) {
					System.out.println("User " + username + " has no movies.");
					break;
				}

				for (Element movie : movies) {
					String title = movie.select("img").attr("alt");
					String url = "https://letterboxd.com" + movie.select("div").attr("data-target-link");

					Map<String,String> film = new HashMap<>();
					film.put("title", title);
					film.put("link", url);

					movie_list.add(film);
				}

				Elements pagination = doc.select("a.next");
				if (link.contains("/popular/") && pages == 50) {
					break;
				} else if (pagination.isEmpty()) {
					break;
				}
				pages++;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		System.out.println(movie_list);
		return new ArrayList<>(movie_list);
	}

}
