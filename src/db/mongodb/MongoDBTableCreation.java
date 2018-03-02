package db.mongodb;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;

import db.mongodb.MongoDBUtilt;

public class MongoDBTableCreation {
	// Run as Java application to create MongoDB collections with index.
	public static void main(String[] args) {
		MongoClient mongoClient = new MongoClient();
		MongoDatabase db = mongoClient.getDatabase(MongoDBUtilt.DB_NAME);

		// drop old collections
		db.getCollection( "users" ).drop();
		db.getCollection( "events" ).drop();

		// create new collections & insert fake user
		db.getCollection("users").insertOne( new Document().append("user_id","1111")
				.append("password", "3229c1097c00d497a0fd282d586be050")
				);
		IndexOptions indexOptions = new IndexOptions().unique(true);
		db.getCollection("users").createIndex( new Document("user_id", 1), indexOptions);
		db.getCollection("events").createIndex( new Document("event_id", 1), indexOptions);

		mongoClient.close();
		System.out.println("Import is done successfully.");
	}
}