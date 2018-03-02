package db;

import db.mongodb.MongoDBConnection;
import db.mysql.MySQLConnection;

public class DBConnectionFactory {
	// Default DB
	private static final String DEFAULT_DB = "mongodb";

	// Create a DBConnection based on given db type.
	public static DBConnection getDBConnection(String db) {
		switch (db) {
		case "mysql":
			return new MySQLConnection();
		case "mongodb":
			return new MongoDBConnection();
		default:
			throw new IllegalArgumentException("Invalid DB name: " + db);
		}
	}

	//overload
	public static DBConnection getDBConnection() {
		return getDBConnection(DEFAULT_DB);
	}
}