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
	private static final Set<String> global_usernames = new HashSet<>();

	public static void main(String[] args) {
		SpringApplication.run(LetterboxdrandomizerApplication.class, args);
	}

	@CrossOrigin(origins = "http://localhost:3000")
	@GetMapping("/randomizer")
	public Map<String, String> randomizer() {
		return get_random_movie_from_global();
	}
	public Map<String, String> get_random_movie_from_global() {
		if (global_watchlist.isEmpty()) {
			return Collections.singletonMap("error", "empty list");
		}

		List<Map<String, String>> movie = new ArrayList<>(global_watchlist);
		return movie.get(new Random().nextInt(movie.size()));
	}

	@PostMapping("/get-random-movie-from-user")
	public Map<String, String> get_random_movie_from_user(@RequestParam String username) {
		List<Map<String, String>> watchlist = get_watchlist(username);

		if (watchlist.isEmpty()) {
			return Collections.singletonMap("error", "no movies found");
		}
		Map<String, String> randomizer = watchlist.get(new Random().nextInt(watchlist.size()));
		return randomizer;
	}
	@CrossOrigin(origins = "http://localhost:3000")
	@PostMapping("/add-user")
	public String add_user(@RequestParam String username) {
		
		if (global_usernames.contains(username)) {
			return username + " already added.";
		}

		List<Map<String, String>> watchlist = get_watchlist(username);

		if (watchlist.isEmpty()) {
			return "Failed to fetch " + username + "'s watchlist.";
		}

		global_watchlist.addAll(watchlist);
		global_usernames.add(username);
		return "User " + username + " added.";
	}

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
		return new ArrayList<>(movie_list);
	}

}
