package external;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.Event;
import entity.Event.EventBuilder;

public class TicketMasterAPI {
	private static final String URL = "https://app.ticketmaster.com/discovery/v2/events.json";
	private static final String DEFAULT_TERM = ""; // no restriction
	private static final String API_KEY = "RKyGMnCH9SJ9PivposNpyomdeJwaCs4A";
	private static final int RADIUS = 50;
	
	private static JSONObject getVenue( JSONObject event ) throws JSONException {
		if ( ! event.isNull( "_embedded" ) ) {
			JSONObject embedded = event.getJSONObject( "_embedded" );
			if ( !embedded.isNull( "venues" ) ) {
				JSONArray venues = embedded.getJSONArray( "venues" );
				if ( venues.length() > 0 ) {
					return venues.getJSONObject( 0 );
				}
			}
		}
		return null;
	}

	private static String getImageUrl( JSONObject event ) throws JSONException {
		if( !event.isNull( "images" ) ) {
			JSONArray array = event.getJSONArray( "images" );
			for( int i = 0; i < array.length(); i++ ) {
				JSONObject image = array.getJSONObject( i );
				if( !image.isNull( "url" ) ) {
					return image.getString( "url" );
				}
			}
		}
		return null;
	}

	private static Set<String> getCategories(JSONObject event) throws JSONException {
		if ( ! event.isNull( "classifications" ) ) {
			JSONArray classifications = event.getJSONArray( "classifications" );
			Set< String > categories = new HashSet<>();
			for ( int i = 0; i < classifications.length(); i++ ) {
				JSONObject classification = classifications.getJSONObject( i );
				if ( ! classification.isNull( "segment" ) ) {
					JSONObject segment = classification.getJSONObject( "segment" );
					if ( !segment.isNull( "name" ) ) {
						String name = segment.getString( "name" );
						categories.add( name );
					}
				}
			}
			return categories;
		}
		return null;
	}

	private static List< Event > getEventList( JSONArray events ) throws JSONException {
		List< Event > EventList = new ArrayList<>();
		for ( int i = 0; i < events.length(); i++ ) {
			JSONObject JSONEvent = events.getJSONObject(i);
			// build Event obj
			EventBuilder newEvent = new EventBuilder();
			if ( !JSONEvent.isNull("name") ) {
				newEvent.setName( JSONEvent.getString( "name" ) );
			}
			if ( !JSONEvent.isNull( "id" ) ) {
				newEvent.setEventId( JSONEvent.getString( "id" ) );
			}
			if ( !JSONEvent.isNull( "url" ) ) {
				newEvent.setUrl( JSONEvent.getString( "url" ) );
			}
			if (!JSONEvent.isNull("distance")) {
				newEvent.setDistance(JSONEvent.getDouble("distance"));
			}
			
			JSONObject venue = getVenue( JSONEvent );
			if (venue != null) {
				StringBuilder sb = new StringBuilder();
				if (!venue.isNull("address")) {
					JSONObject address = venue.getJSONObject("address");
					if (!address.isNull("line1")) {
						sb.append(address.getString("line1"));
					}
					if (!address.isNull("line2")) {
						sb.append(address.getString("line2"));
					}
					if (!address.isNull("line3")) {
						sb.append(address.getString("line3"));
					}
					sb.append(",");
				}
				if (!venue.isNull("city")) {
					JSONObject city = venue.getJSONObject("city");
					if (!city.isNull("name")) {
						sb.append(city.getString("name"));
					}
				}
				newEvent.setAddress(sb.toString());
			}

			
			newEvent.setImageUrl( getImageUrl( JSONEvent ) );
			newEvent.setCategories( getCategories( JSONEvent ) );
			
			Event event = newEvent.build();
			EventList.add( event );
		}
		return EventList;
	}
	
	public static List< Event > search(double lat, double lon, String term) {
		// Convert lat/lon to geo hash
		String geoHash = GeoHash.encodeGeohash(lat, lon, 8);
		// Encode term in url
		if (term == null) {
			term = DEFAULT_TERM;
		}
		try {
			term = URLEncoder.encode(term, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}

		// TicketMaster Requirement
		String query = String.format("apikey=%s&geoPoint=%s&keyword=%s&radius=%s", API_KEY, geoHash, term, RADIUS);
		try {
			// Open a HTTP connection between your Java application and TicketMaster based on url
			HttpURLConnection connection = (HttpURLConnection) new URL(URL + "?" + query).openConnection();
			// Set requrest method to GET
			connection.setRequestMethod("GET");

			int responseCode = connection.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + URL + "?" + query);
			System.out.println("Response Code : " + responseCode);
			// Read response body to get events data
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			StringBuilder response = new StringBuilder();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			JSONObject obj = new JSONObject(response.toString());
			if (obj.isNull("_embedded")) {
				return new ArrayList<>();
			}
			JSONObject embedded = obj.getJSONObject("_embedded");
			JSONArray events = embedded.getJSONArray("events");
			return getEventList( events );
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

	private static void queryAPI(double lat, double lon) {
		List< Event > eventList = search(lat, lon, null);
		try {
			for (Event event : eventList) {
				JSONObject jsonObject = event.toJSONObject();
				System.out.println(jsonObject);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		// Mountain View, CA
		// TicketMasterAPI.queryAPI(37.38, -122.08);
		// Houston, TX
		TicketMasterAPI.queryAPI(29.682684, -95.295410);
	}
}

