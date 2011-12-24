package heroku.dbcapi.resources;

import heroku.dbcapi.DatabaseLink;
import heroku.dbcapi.Env;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import com.force.api.ApiConfig;
import com.force.api.ApiSession;
import com.force.api.Auth;
import com.force.api.DescribeGlobal;
import com.force.api.DescribeSObject;
import com.force.api.ForceApi;
import com.force.api.QueryResult;
import com.inamik.utils.SimpleTableFormatter;
import com.inamik.utils.TableFormatter;

@Path("/data/{database}")
public class RESTServices {

	static ForceApi coredb = new ForceApi(new ApiConfig().setForceURL(Env.FORCE_COREDB_URL));
	static ApiConfig CONFIG = new ApiConfig().setClientId(Env.LINK_OAUTH_KEY).setClientSecret(Env.LINK_OAUTH_SECRET);
	
	// hack
	static Map<String,ApiSession> apiKeyToAccessToken = new HashMap<String,ApiSession>();

	@GET
	@Path("/query")
	@Produces("text/plain")
	@SuppressWarnings("rawtypes")
	public String query(@QueryParam("q") String q, @PathParam("database") String database, @HeaderParam("Authorization") String apiKey) {
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

	@GET
	@Path("/sobjects/")
	@Produces("text/plain")
	public StreamingOutput describeGlobal(@PathParam("database") String database, @HeaderParam("Authorization") String apiKey) {
		ApiSession session = apiKeyToAccessToken.get(apiKey+":"+database);
		if(session == null) {
			String refreshToken = coredb.query("SELECT token__c FROM database__c WHERE name='"+database+"' AND developer__r.apiKey__c='"+apiKey+"'", DatabaseLink.class).getRecords().get(0).getToken();
			session = Auth.refreshOauthTokenFlow(CONFIG, refreshToken);
			apiKeyToAccessToken.put(apiKey+":"+database, session);
		}
		ForceApi targetdb = new ForceApi(new ApiConfig(), session);
		System.out.println("Getting info for "+database);
		DescribeGlobal dg = targetdb.describeGlobal();
		final TableFormatter tf = new SimpleTableFormatter(false).nextRow();
		tf.nextCell().addLine("Name").addLine("----");
		tf.nextCell().addLine("Label").addLine("-----");
		for (DescribeSObject ds : dg.getSObjects()) {
			tf.nextRow()
				.nextCell().addLine(ds.getName()+"  ")
				.nextCell().addLine(ds.getLabel());
		}

		return new StreamingOutput() {
			public void write(OutputStream output) throws IOException, WebApplicationException {
				for(String s: tf.getFormattedTable()) {
					output.write((s+"\n").getBytes());
				}
			}
		};
		
	}

	@GET
	@Path("/sobjects/{sobject}")
	@Produces("text/plain")
	public StreamingOutput describeSObject(@PathParam("database") String database, @PathParam("sobject") String sobject, @HeaderParam("Authorization") String apiKey) {
		ApiSession session = apiKeyToAccessToken.get(apiKey+":"+database);
		if(session == null) {
			String refreshToken = coredb.query("SELECT token__c FROM database__c WHERE name='"+database+"' AND developer__r.apiKey__c='"+apiKey+"'", DatabaseLink.class).getRecords().get(0).getToken();
			session = Auth.refreshOauthTokenFlow(CONFIG, refreshToken);
			apiKeyToAccessToken.put(apiKey+":"+database, session);
		}
		ForceApi targetdb = new ForceApi(new ApiConfig(), session);
		System.out.println("Getting info for "+database);
		final DescribeSObject ds = targetdb.describeSObject(sobject);
		final TableFormatter tf = new SimpleTableFormatter(false).nextRow();
		tf.nextCell().addLine("Name").addLine("----");
		tf.nextCell().addLine("Type").addLine("----");
		for (DescribeSObject.Field f : ds.getFields()) {
			tf.nextRow()
				.nextCell().addLine(f.getName()+"  ")
				.nextCell().addLine(f.getType());
		}

		return new StreamingOutput() {
			public void write(OutputStream output) throws IOException, WebApplicationException {
				output.write(new String("Fields for SObject: "+ds.getName()+"\n\n").getBytes());
				for(String s: tf.getFormattedTable()) {
					output.write((s+"\n").getBytes());
				}
			}
		};
		
	}

}
