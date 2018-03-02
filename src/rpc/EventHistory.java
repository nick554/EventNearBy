package rpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Event;

/**
 * Servlet implementation class EventHistory
 */
@WebServlet("/history")
public class EventHistory extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public EventHistory() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// allow access only if session exists
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.setStatus(403);
			return;
		}
		String userId = session.getAttribute("user_id").toString();
		
		JSONArray array = new JSONArray();

		DBConnection conn = DBConnectionFactory.getDBConnection();
		Set<Event> events = conn.getFavoriteEvents(userId);
		for (Event event : events) {
			JSONObject obj = event.toJSONObject();
			try {
				obj.append("favorite", true);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			array.put(obj);
		}
		RpcHelper.writeJSONArray(response, array);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			// allow access only if session exists
			HttpSession session = request.getSession(false);
			if (session == null) {
				response.setStatus(403);
				return;
			}
			String userId = session.getAttribute("user_id").toString();
			
			JSONObject input = RpcHelper.readJSONObject(request);
			JSONArray array = input.getJSONArray("favorite");
			List<String> eventIds = new ArrayList<>();
			for (int i = 0; i < array.length(); i++) {
				String eventId = array.get(i).toString();
				eventIds.add(eventId);
			}

			DBConnection conn = DBConnectionFactory.getDBConnection();
			conn.setFavoriteEvents(userId, eventIds);

			RpcHelper.writeJSONObject(response, new JSONObject().put("result", "SUCCESS"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			// allow access only if session exists
			HttpSession session = request.getSession(false);
			if (session == null) {
				response.setStatus(403);
				return;
			}
			String userId = session.getAttribute("user_id").toString();
			
			JSONObject input = RpcHelper.readJSONObject(request);
			JSONArray array = input.getJSONArray("favorite");
			List<String> eventIds = new ArrayList<>();
			for (int i = 0; i < array.length(); i++) {
				String eventId = array.get(i).toString();
				eventIds.add(eventId);
			}

			DBConnection conn = DBConnectionFactory.getDBConnection();
			conn.unsetFavoriteEvents(userId, eventIds);

			RpcHelper.writeJSONObject(response, new JSONObject().put("result", "SUCCESS"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
