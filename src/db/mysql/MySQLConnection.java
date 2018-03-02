package db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import db.DBConnection;
import entity.Event;
import entity.Event.EventBuilder;
import external.TicketMasterAPI;

public class MySQLConnection implements DBConnection{
	private Connection conn;

	public MySQLConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(MySQLUtilt.URL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}		
	}

	@Override
	//"INSERT IGNORE INTO history (user_id, event_id) VALUES (?, ?)"
	public void setFavoriteEvents(String userId, List<String> eventIds) {
		if (conn == null) {
			return;
		}
		String query = "INSERT IGNORE INTO history (user_id, event_id) VALUES (?, ?)";
		try {
			PreparedStatement statement = conn.prepareStatement(query);
			for (String eventId : eventIds) {
				statement.setString(1, userId);
				statement.setString(2, eventId);
				statement.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void unsetFavoriteEvents(String userId, List<String> eventIds) {
		if (conn == null) {
			return;
		}
		String query = "DELETE FROM history WHERE user_id = ? and event_id = ?";
		try {
			PreparedStatement statement = conn.prepareStatement(query);
			for (String eventId : eventIds) {
				statement.setString(1, userId);
				statement.setString(2, eventId);
				statement.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Set<String> getFavoriteEventIds(String userId) {
		if (conn == null) {
			return new HashSet<>();
		}
		Set<String> favoriteEvents = new HashSet<>();
		try {
			String sql = "SELECT event_id from history WHERE user_id = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				String eventId = rs.getString("event_id");
				favoriteEvents.add(eventId);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return favoriteEvents;
	}

	@Override
	public Set<Event> getFavoriteEvents(String userId) {
		if (conn == null) {
			return new HashSet<>();
		}
		Set<String> eventIds = getFavoriteEventIds(userId);
		Set<Event> favoriteEvents = new HashSet<>();
		try {
			for (String eventId : eventIds) {
				String sql = "SELECT * from events WHERE event_id = ? ";
				PreparedStatement statement = conn.prepareStatement(sql);
				statement.setString(1, eventId);
				ResultSet rs = statement.executeQuery();
				EventBuilder builder = new EventBuilder();

				// Because eventId is unique and given one event id there should
				// have only one result returned.
				if (rs.next()) {
					builder.setEventId(rs.getString("event_id"));
					builder.setName(rs.getString("name"));
					builder.setRating(rs.getDouble("rating"));
					builder.setAddress(rs.getString("address"));
					builder.setImageUrl(rs.getString("image_url"));
					builder.setUrl(rs.getString("url"));
					builder.setCategories(getCategories(eventId));
				}
				favoriteEvents.add(builder.build());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return favoriteEvents;
	}

	@Override
	public Set<String> getCategories(String eventId) {
		if (conn == null) {
			return null;
		}
		Set<String> categories = new HashSet<>();
		try {
			String sql = "SELECT category from categories WHERE event_id = ? ";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, eventId);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				categories.add(rs.getString("category"));
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
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
		if (conn == null) {
			return;
		}
		try {
			// First, insert into events table
			String sql = "INSERT IGNORE INTO events VALUES (?,?,?,?,?,?,?)";

			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, event.getEventId());
			statement.setString(2, event.getName());
			statement.setDouble(3, event.getRating());
			statement.setString(4, event.getAddress());
			statement.setString(5, event.getImageUrl());
			statement.setString(6, event.getUrl());
			statement.setDouble(7, event.getDistance());
			statement.execute();

			// Second, update categories table for each category.
			sql = "INSERT IGNORE INTO categories VALUES (?,?)";
			for (String category : event.getCategories()) {
				statement = conn.prepareStatement(sql);
				statement.setString(1, event.getEventId());
				statement.setString(2, category);
				statement.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public String[] getFullname(String userId) {
		if (conn == null) {
			return null;
		}
		String[] name = {"",""};
		try {
			String sql = "SELECT first_name, last_name from users WHERE user_id = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				name[0] = rs.getString("first_name");
				name[1] = rs.getString("last_name");
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return name;
	}

	@Override
	public boolean verifyLogin(String userId, String password) {
		if (conn == null) {
			return false;
		}
		try {
			String sql = "SELECT user_id from users WHERE user_id = ? and password = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, password);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				return true;
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return false;
	}

	@Override
	public boolean addUser(String userId, String password) {
		if (conn == null) {
			return false;
		}
		try {
			String sql = "SELECT user_id from users WHERE user_id = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				return true;
			} else {
				String sqlWrite = "INSERT INTO users (user_id, password) VALUES (?,?)";
				PreparedStatement statementWrite = conn.prepareStatement(sqlWrite);
				statementWrite.setString(1, userId);
				statementWrite.setString(2, password);
				statementWrite.execute();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return false;
	}

}
