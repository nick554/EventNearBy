package db;

import java.util.List;
import java.util.Set;

import entity.Event;

public interface DBConnection {
	/**
	 * Close the connection.
	 */
	public void close();

	/**
	 * Insert the favorite events for a user.
	 * 
	 * @param userId
	 * @param eventIds
	 */
	public void setFavoriteEvents(String userId, List<String> eventIds);

	/**
	 * Delete the favorite events for a user.
	 * 
	 * @param userId
	 * @param eventIds
	 */
	public void unsetFavoriteEvents(String userId, List<String> eventIds);

	/**
	 * Get the favorite event id for a user.
	 * 
	 * @param userId
	 * @return eventIds
	 */
	public Set<String> getFavoriteEventIds(String userId);

	/**
	 * Get the favorite events for a user.
	 * 
	 * @param userId
	 * @return events
	 */
	public Set<Event> getFavoriteEvents(String userId);

	/**
	 * Gets categories based on event id
	 * 
	 * @param eventId
	 * @return set of categories
	 */
	public Set<String> getCategories(String eventId);

	/**
	 * Search events near a geolocation and a term (optional).
	 * 
	 * @param userId
	 * @param lat
	 * @param lon
	 * @param term
	 *            (Nullable)
	 * @return list of events
	 */
	public List<Event> searchEvents(/*String userId,*/ double lat, double lon, String term);

	/**
	 * Save event into db.
	 * 
	 * @param event
	 */
	public void saveEvent(Event event);

	/**
	 * Get full name of a user. (This is not needed for main course, just for demo
	 * and extension).
	 * 
	 * @param userId
	 * @return full name of the user
	 */
	public String[] getFullname(String userId);

	/**
	 * Return whether the credential is correct. 
	 * 
	 * @param userId
	 * @param password
	 * @return boolean
	 */
	public boolean verifyLogin(String userId, String password);
	
	/**
	 * Return whether the UserId exists. 
	 * 
	 * @param userId
	 * @return boolean
	 */
	public boolean addUser(String userId, String password);	
}
