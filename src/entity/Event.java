package entity;

import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Event {
	private String eventId;
	private String name;
	private double rating;
	private String address;
	private Set<String> categories;
	private String imageUrl;
	private String url;
	private double distance;

	public String getEventId() {
		return eventId;
	}
	public String getName() {
		return name;
	}
	public double getRating() {
		return rating;
	}
	public String getAddress() {
		return address;
	}
	public Set<String> getCategories() {
		return categories;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public String getUrl() {
		return url;
	}
	public double getDistance() {
		return distance;
	}
	
	private Event( EventBuilder event ) {
		this.eventId = event.eventId;
		this.name = event.name;
		this.rating = event.rating;
		this.address = event.address;
		this.categories = event.categories;
		this.imageUrl = event.imageUrl;
		this.url = event.url;
		this.distance = event.distance;
		}
	
	public static class EventBuilder {
		private String eventId;
		private String name;
		private double rating;
		private String address;
		private Set<String> categories;
		private String imageUrl;
		private String url;
		private double distance;
		
		public EventBuilder setEventId(String eventId) {
			this.eventId = eventId;
			return this;
		}
		public EventBuilder setName(String name) {
			this.name = name;
			return this;
		}
		public EventBuilder setRating(double rating) {
			this.rating = rating;
			return this;
		}
		public EventBuilder setAddress(String address) {
			this.address = address;
			return this;
		}
		public EventBuilder setCategories(Set<String> categories) {
			this.categories = categories;
			return this;
		}
		public EventBuilder setImageUrl(String imageUrl) {
			this.imageUrl = imageUrl;
			return this;
		}
		public EventBuilder setUrl(String url) {
			this.url = url;
			return this;
		}
		public EventBuilder setDistance(double distance) {
			this.distance = distance;
			return this;
		}
		
		public Event build() {
			return new Event( this );
		}
	}
	
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("item_id", eventId);
			obj.put("name", name);
			obj.put("rating", rating);
			obj.put("address", address);
			obj.put("categories", new JSONArray(categories));
			obj.put("image_url", imageUrl);
			obj.put("url", url);
			obj.put("distance", distance);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((eventId == null) ? 0 : eventId.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Event other = (Event) obj;
		if (eventId == null) {
			if (other.eventId != null)
				return false;
		} else if (!eventId.equals(other.eventId))
			return false;
		return true;
	}
	
}
