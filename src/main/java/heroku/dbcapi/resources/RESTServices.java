package heroku.dbcapi.resources;

import heroku.dbcapi.DatabaseLink;
import heroku.dbcapi.Env;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.force.api.ApiConfig;
import com.force.api.ApiSession;
import com.force.api.Auth;
import com.force.api.ForceApi;
import com.force.api.QueryResult;
import com.inamik.utils.SimpleTableFormatter;
import com.inamik.utils.TableFormatter;

@Path("/data")
public class RESTServices {

	static ForceApi coredb = new ForceApi(new ApiConfig().setForceURL(Env.FORCE_COREDB_URL));
	static ApiConfig CONFIG = new ApiConfig().setClientId(Env.LINK_OAUTH_KEY).setClientSecret(Env.LINK_OAUTH_SECRET);
	
	// hack
	static Map<String,ApiSession> apiKeyToAccessToken = new HashMap<String,ApiSession>();

	@GET
	@Path("/query")
	@Produces("text/plain")
	@SuppressWarnings("rawtypes")
	public String query(@QueryParam("q") String q, @QueryParam("database") String database, @HeaderParam("Authorization") String apiKey) {
		System.out.println("query: "+q);
		ApiSession session = apiKeyToAccessToken.get(apiKey+":"+database);
		if(session == null) {
			String refreshToken = coredb.query("SELECT token__c FROM database__c WHERE name='"+database+"' AND developer__r.apiKey__c='"+apiKey+"'", DatabaseLink.class).getRecords().get(0).getToken();
			session = Auth.refreshOauthTokenFlow(CONFIG, refreshToken);
			apiKeyToAccessToken.put(apiKey+":"+database, session);
		}
		ForceApi targetdb = new ForceApi(new ApiConfig(), session);

		QueryResult<Map> qr = targetdb.query(q, Map.class);
		List<String> fields = new ArrayList<String>();
		for(Object key : qr.getRecords().get(0).keySet()) {
			if(!key.equals("attributes")) {
				fields.add(key.toString());
			}
		}
		
		TableFormatter tf = new SimpleTableFormatter(true).nextRow();
		for(String f : fields) {
			tf.nextCell().addLine(f);
		}
		for(Map rec : qr.getRecords()) {
			tf.nextRow();
			for(String f : fields) {
				tf.nextCell().addLine(rec.get(f).toString());
			}
		}
		StringBuilder b = new StringBuilder();
		for(String s : tf.getFormattedTable()) {
			b.append(s+"\n");
		}
		return b.toString();
	}

}
