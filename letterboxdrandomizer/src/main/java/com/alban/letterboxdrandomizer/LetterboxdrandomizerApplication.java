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
		//return movie.get(new Random().nextInt(movie.size()));
		Map<String, String> random_movie = movie.get(new Random().nextInt(movie.size()));
		return fetch_movie_details(random_movie.get("link"));
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

}
