package db.mongodb;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

import db.DBConnection;
import entity.Event;
import entity.Event.EventBuilder;
import external.TicketMasterAPI;

import static com.mongodb.client.model.Filters.eq;

public class MongoDBConnection implements DBConnection {
	private MongoClient mongoClient;
	private MongoDatabase db;

	public MongoDBConnection() {
		// Connects to local mongodb server.
		mongoClient = new MongoClient();
		db = mongoClient.getDatabase(MongoDBUtilt.DB_NAME);
	}

	@Override
	public void close() {
		if (mongoClient != null) {
			mongoClient.close();
		}		
	}

	@Override
	public void setFavoriteEvents(String userId, List<String> eventIds) {
		db.getCollection("users").updateOne(new Document("user_id", userId),
				new Document("$push", new Document("favorite", new Document("$each", eventIds))));		
	}

	@Override
	public void unsetFavoriteEvents(String userId, List<String> eventIds) {
		db.getCollection("users").updateOne(new Document("user_id", userId),
				new Document("$pullAll", new Document("favorite", eventIds)));		
	}

	@Override
	public Set<String> getFavoriteEventIds(String userId) {
		Set<String> favoriteEvents = new HashSet<String>();
		FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id", userId));
		if (iterable.first().containsKey("favorite")) {
			@SuppressWarnings("unchecked")
			List<String> list = (List<String>) iterable.first().get("favorite");
			favoriteEvents.addAll(list);
		}
		return favoriteEvents;
	}

	@Override
	public Set<Event> getFavoriteEvents(String userId) {
		Set<String> eventIds = getFavoriteEventIds(userId);
		Set<Event> favoriteEvents = new HashSet<>();
		for (String eventId : eventIds) {
			FindIterable<Document> iterable = db.getCollection("events").find(eq("event_id", eventId));
			if (iterable.first() != null) {
				Document doc = iterable.first();
				EventBuilder builder = new EventBuilder()
						.setEventId(doc.getString("event_id"))  	.setName(doc.getString("name"))
						.setRating(doc.getDouble("rating"))		.setAddress(doc.getString("address"))
						.setImageUrl(doc.getString("image_url")) .setUrl(doc.getString("url"))
						.setDistance(doc.getDouble("distance"))	.setCategories(getCategories(eventId));

				favoriteEvents.add(builder.build());
			}
		}
		return favoriteEvents;
	}

	@Override
	public Set<String> getCategories(String eventId) {
		Set<String> categories = new HashSet<>();
		FindIterable<Document> iterable = db.getCollection("events").find(eq("event_id", eventId));

		@SuppressWarnings("unchecked")
		List<String> list = (List<String>) iterable.first().get("categories");
		if (list != null) {
			categories.addAll(list);
		}
		return categories;
	}

	@Override
	public List<Event> searchEvents(double lat, double lon, String term) {
		List<Event> events = TicketMasterAPI.search(lat, lon, term);
		for (Event event : events) {
			// Save the event into our own db.
			saveEvent(event);
		}
		return events;
	}

	@Override
	public void saveEvent(Event event) {
		// the query look like db.getCollection("events").find(new Document().append("event_id",
		// event.getEventId())) but the java drive provides you a clearer way to do this.

		FindIterable<Document> iterable = db.getCollection("events").find(eq("event_id", event.getEventId()));

		if (iterable.first() == null) {
			db.getCollection("events")
					.insertOne(new Document().append("event_id", event.getEventId()).append("name", event.getName())
							.append("rating", event.getRating())			.append("address", event.getAddress())
							.append("image_url", event.getImageUrl())		.append("url", event.getUrl())
							.append("categories", event.getCategories())	.append("distance", event.getDistance()));
		}
		
	}

	@Override
	public boolean verifyLogin(String userId, String password) {
		FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id", userId));
		return (iterable.first() != null) && (iterable.first().getString("password").equals(password));
	}

	@Override
	public String[] getFullname(String userId) {
		FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id", userId));
		Document document = iterable.first();
		String[] name = new String[2];
		name[0] = document.getString("first_name");
		name[1] = document.getString("last_name");
		return name;
	}

	@Override
	public boolean addUser(String userId, String password) {
		FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id", userId));
		if ( iterable.first() == null ) {
			db.getCollection( "users" ).insertOne( new Document().append("userid", userId).append("password", password));
			return false;
		}
		return true;
	}

}
