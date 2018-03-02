package rpc;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import entity.Event;


///  writeHTTPResponse
public class RpcHelper {
    // Writes a JSONObject to http response.
	public static void writeJSONArray(HttpServletResponse response, JSONArray array) {
		try {
			response.setContentType("application/json");
			response.addHeader("Access-Control-Allow-Origin", "*");
			PrintWriter out = response.getWriter();
			out.print(array);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Writes a JSONArray to http response.
	public static void writeJSONObject(HttpServletResponse response, JSONObject obj) {
		try {
			response.setContentType("application/json");
			response.addHeader("Access-Control-Allow-Origin", "*");
			PrintWriter out = response.getWriter();
			out.print(obj);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// read a JSONObject about favorate events
	public static JSONObject readJSONObject(HttpServletRequest request) {
		// string builder vs string buffer
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			reader.close();
			return new JSONObject(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	  // Converts a list of Event objects to JSONArray.
	  public static JSONArray getJSONArray(List<Event> Events) {
	    JSONArray result = new JSONArray();
	    try {
	      for (Event Event : Events) {
	        result.put(Event.toJSONObject());
	      }
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return result;
	  }

}
