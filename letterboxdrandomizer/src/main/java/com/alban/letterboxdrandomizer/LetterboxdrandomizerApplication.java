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
	//private static final Set<String> global_usernames = new HashSet<>();

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
		//return movie.get(new Random().nextInt(movie.size()));
		Map<String, String> random_movie = movie.get(new Random().nextInt(movie.size()));
		return fetch_movie_details(random_movie.get("link"));
	}

	@PostMapping("/get-random-movie-from-user")
	/**
	 * A class to only fetch a random movie from a single user directly
	 * @param username - String
	 * @return randomizer (Map) that selects one random movie in the list
	 */
	public Map<String, String> get_random_movie_from_user(@RequestParam String username) {
		List<Map<String, String>> watchlist = get_watchlist(username);

		if (watchlist.isEmpty()) {
			return Collections.singletonMap("error", "no movies found");
		}
		Map<String, String> randomizer = watchlist.get(new Random().nextInt(watchlist.size()));
		return randomizer;
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

		List<Map<String, String>> watchlist = get_watchlist(username);

		if (watchlist.isEmpty()) {
			return "Failed to fetch " + username + "'s watchlist.";
		}

		global_watchlist.addAll(watchlist);
		//global_usernames.add(username);
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
	 * A class to fetch a user's watchlist through web scraping
	 * @param username - String
	 * @return new ArrayList with the list including all watchlist movies
	 */
	public List<Map<String, String>> get_watchlist(String username) {
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
					String link = "https://letterboxd.com" + movie.select("div").attr("data-target-link");

					Map<String,String> film = new HashMap<>();
					film.put("title", title);
					film.put("link", link);

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
		System.out.println(movie_list);
		return new ArrayList<>(movie_list);
	}

	/**
	 * A class to fetch a user's custom link through web scraping
	 * @param link - String
	 * @return new ArrayList with the list including all custom / user link movies
	 */
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

}
