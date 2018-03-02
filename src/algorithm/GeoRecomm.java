package algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Event;

public class GeoRecomm {

	public static List<Event> recommendEvents(String userId, double lat, double lon) {
		List<Event> recommendedEvents = new ArrayList<>();
		DBConnection conn = DBConnectionFactory.getDBConnection();

		// step 1 Get all favorited events
		Set<String> favoriteEvents = conn.getFavoriteEventIds(userId);

		// step 2 Get all categories of favorited events, sort by count
		Map<String, Integer> allCategories = new HashMap<>(); // step 2
		for (String event : favoriteEvents) {
			Set<String> categories = conn.getCategories(event); // db queries
			for (String category : categories) {
				if (allCategories.containsKey(category)) {
					allCategories.put(category, allCategories.get(category) + 1);
				} else {
					allCategories.put(category, 1);
				}
			}
		}

		List<Entry<String, Integer>> categoryList = new ArrayList<Entry<String, Integer>>(allCategories.entrySet());
		Collections.sort(categoryList, new Comparator<Entry<String, Integer>>() {
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return Integer.compare(o2.getValue(), o1.getValue());
			}
		});

		// Step 3, do search based on category, filter out favorited events, sort by distance
		Set<Event> visitedEvents = new HashSet<>();
		for (Entry<String, Integer> category : categoryList) {
			List<Event> events = conn.searchEvents(lat, lon, category.getKey());
			List<Event> filteredEvents = new ArrayList<>();
			for (Event event : events) {
				if (!favoriteEvents.contains(event.getEventId()) && ! visitedEvents.contains(event)) {
					filteredEvents.add(event);
				}
			}
			Collections.sort(filteredEvents, new Comparator<Event>() {
				@Override
				public int compare(Event item1, Event item2) {
					// return the increasing order of distance.
					return Double.compare(item1.getDistance(), item2.getDistance());
				}
			});
			visitedEvents.addAll(events);
			recommendedEvents.addAll(filteredEvents);
		}

		return recommendedEvents;
	}
}
