import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.temboo.Library.Google.Calendar.*;
import com.temboo.Library.Google.Calendar.GetAllCalendars.GetAllCalendarsInputSet;
import com.temboo.Library.Google.Calendar.GetAllCalendars.GetAllCalendarsResultSet;
import com.temboo.Library.Google.OAuth.*;
import com.temboo.Library.Google.OAuth.InitializeOAuth.*;
import com.temboo.Library.Google.OAuth.FinalizeOAuth.*;
import com.temboo.core.TembooSession;
import com.temboo.outputs.Google.Calendar.GoogleCalendarListEntry;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class WhereCanWeMeet {

	private TembooSession session;
	private static String accessToken;
	static Scanner input = new Scanner(System.in);
	private String jsonObjStr; 
	
	
	
	public void oAuth(int users) throws Exception {
		
		for(int i=0; i<users; i++){
		session = new TembooSession("nishthad", "CS280", "95b93854-6f4c-4143-a");
		
		InitializeOAuth initializeOAuthChoreo = new InitializeOAuth(session);
		InitializeOAuthInputSet initializeOAuthInputs = initializeOAuthChoreo.newInputSet();
		
		initializeOAuthInputs.set_AppKeyName("CS280");
		initializeOAuthInputs.set_AccountName("nishthad");
		initializeOAuthInputs.set_AppKeyValue("95b93854-6f4c-4143-a");
		initializeOAuthInputs.set_ClientID("349103510715.apps.googleusercontent.com");
		initializeOAuthInputs.set_Scope("https://www.googleapis.com/auth/calendar");

		
		InitializeOAuthResultSet initializeOAuthResults = initializeOAuthChoreo.execute(initializeOAuthInputs);
		
		System.out.println("User "+ (i+1) + ", please go to the following URL and click Accept");
		System.out.println(initializeOAuthResults.get_AuthorizationURL());
		
		System.out.println("Hit A once you have Accepted");
		if(input.next().equals("a")){
			FinalizeOAuth finalizeOAuthChoreo = new FinalizeOAuth(session);
			FinalizeOAuthInputSet finalizeOAuthInputs = finalizeOAuthChoreo.newInputSet();
			finalizeOAuthInputs.setCredential("GCal");
			
			String callbackID = initializeOAuthResults.get_CallbackID();
			
			finalizeOAuthInputs.set_CallbackID(callbackID);
			FinalizeOAuthResultSet finalizeOAuthResults = finalizeOAuthChoreo.execute(finalizeOAuthInputs);
			
			accessToken = finalizeOAuthResults.get_AccessToken();	
		
		}
		System.out.println("Access Token: " + accessToken);
		getCal(accessToken);
		}
		
		
	}
	
	public void getCal(String accessToken) throws Exception {
		
		System.out.println("Hit c to continue");
			if(input.next().equals("c")){
			GetAllCalendars getAllCalendarsChoreo = new GetAllCalendars(session);
			GetAllCalendarsInputSet getAllCalendarsInputs = getAllCalendarsChoreo.newInputSet();
			
			getAllCalendarsInputs.set_AccessToken (accessToken);
			
			GetAllCalendarsResultSet getAllCalendarsResults = getAllCalendarsChoreo.execute(getAllCalendarsInputs);
			
			GoogleCalendarListEntry[] myCalendars = getAllCalendarsResults.getCalendars();
			
			for(int i=0; i<myCalendars.length; i++){
			
			System.out.println(myCalendars[i].getId());
			}
			
			String baseURL= "https://www.googleapis.com/calendar/v3/freeBusy?key=AIzaSyArFvHjqA3qQe14PYosnTJcHZHhZ4QrE90";
			//String URLParams = timeMin=
			jsonObjStr = "{ \n \"timeMin\": \"2013-07-01T12:00:00-04:00\", \n \"timeMax\": \"2013-07-09T12:00:00-04:00\", \n \"timeZone\": \"(GMT -4:00)\", \n \"items\": [ \n { \"id\":\""+ myCalendars[2].getId()+ "\" \n}\n]\n}";
			//System.out.println(jsonObjStr);
			
			postReq(baseURL, jsonObjStr, myCalendars);
			
			}
	}
	
	public static void postReq (String request, String json, GoogleCalendarListEntry[] myCalendars) throws Exception
	{
		URL url = new URL(request); 
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();           
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setInstanceFollowRedirects(false); 
		connection.setRequestMethod("POST"); 
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setRequestProperty("Authorization", "Bearer "+ accessToken);

		connection.setRequestProperty("Content-Length", "" + Integer.toString(json.getBytes().length));
		connection.setUseCaches (false);

		DataOutputStream wr = new DataOutputStream(connection.getOutputStream ());
		wr.writeBytes(json);
		wr.flush();
		wr.close();
		
		JsonParser jParser = new JsonParser();
		JsonElement root = jParser.parse(new InputStreamReader((InputStream) connection.getContent()));
		
		JsonArray startEnd = root.getAsJsonObject().get("calendars").getAsJsonObject().get(myCalendars[2].getId()).getAsJsonObject().get("busy").getAsJsonArray(); 
		
		for(int i=0; i<startEnd.size(); i++) {
			
			JsonObject st = startEnd.get(i).getAsJsonObject();
			String startTime = st.get("start").getAsString();
			System.out.println("start time: " + startTime);
			String endTime = st.get("end").getAsString();
			System.out.println("end time: "+ endTime);
	
		}

		connection.disconnect();
		
	}

	public void userInfo() throws Exception{
	
		System.out.println("Please enter the number of people for the meeting");
		Integer numUsers = input.nextInt();
		
		oAuth(numUsers);
		
		System.out.println("Please enter the date range within which you would like to meet ");
		String dateRange = input.next();
		
		
	}
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		WhereCanWeMeet test = new WhereCanWeMeet();
		test.userInfo();


	}

}
