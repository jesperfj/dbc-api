package heroku.dbcapi.resources;

import heroku.dbcapi.DatabaseLink;
import heroku.dbcapi.Env;
import heroku.dbcapi.Svc;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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

	static ApiConfig CONFIG = new ApiConfig().setClientId(Env.LINK_OAUTH_KEY).setClientSecret(Env.LINK_OAUTH_SECRET);
	private final static Logger LOGGER = Logger.getLogger(RESTServices.class.getName());

	String apiKey;
	String database;
	ForceApi targetdb;

	public RESTServices(@PathParam("database") String database, @HeaderParam("Authorization") String apiKey) {
		this.database = database;
		this.apiKey = apiKey;
		ApiSession session = Svc.cache.get(apiKey,database);
		if(session == null) {
			String refreshToken = Svc.coredb.query("SELECT token__c FROM database__c WHERE name='"+database+"' AND developer__r.apiKey__c='"+apiKey+"'", DatabaseLink.class).getRecords().get(0).getToken();
			session = Auth.refreshOauthTokenFlow(CONFIG, refreshToken);
			Svc.cache.add(apiKey, database, session);
		}
		targetdb = new ForceApi(new ApiConfig(), session);
	}
	
	@GET
	@Path("/query")
	@Produces("text/plain")
	@SuppressWarnings("rawtypes")
	public String query(@QueryParam("q") String q) {
		LOGGER.log(Level.INFO, "query: "+q);
		QueryResult<Map> qr = targetdb.query(q, Map.class);
		List<String> fields = new ArrayList<String>();
		for(Object key : qr.getRecords().get(0).keySet()) {
			if(!key.equals("attributes")) {
				fields.add(key.toString());
			}
		}
		
		TableFormatter tf = new SimpleTableFormatter(false).nextRow();
		for(String f : fields) {
			char[] underline = new char[f.length()];
			for(int i=0;i<underline.length;i++) { underline[i] = '-'; }
			tf.nextCell().addLine(f).addLine(new String(underline));
		}
		for(Map rec : qr.getRecords()) {
			tf.nextRow();
			for(String f : fields) {
				String val = null;
				if (rec.get(f) instanceof Map) {
					// relationship traversal
					for(Object k : ((Map) rec.get(f)).keySet()) {
						if(!k.equals("attributes")) {
							val = ((Map) rec.get(f)).get(k).toString();
							break;
						}
					}
				} else {
					val = rec.get(f)!=null ? rec.get(f).toString() : "";
				}
				tf.nextCell().addLine(val+"  ");
			}
		}
		StringBuilder b = new StringBuilder();
		for(String s : tf.getFormattedTable()) {
			b.append("    "+s+"\n");
		}
		return b.toString();
	}

	@GET
	@Path("/sobjects/")
	@Produces("text/plain")
	public StreamingOutput describeGlobal() {
		LOGGER.log(Level.INFO,"Getting info for "+database);
		DescribeGlobal dg = targetdb.describeGlobal();
		final TableFormatter tf = new SimpleTableFormatter(false).nextRow();
		tf.nextCell().addLine("    Name  ").addLine("----");
		tf.nextCell().addLine("Label").addLine("-----");
		for (DescribeSObject ds : dg.getSObjects()) {
			tf.nextRow()
				.nextCell().addLine("    "+ds.getName()+"  ")
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
	public StreamingOutput describeSObject(@PathParam("sobject") String sobject) {
		LOGGER.log(Level.INFO,"Getting info for "+database+" for sobject "+sobject);
		final DescribeSObject ds = targetdb.describeSObject(sobject);
		final TableFormatter tf = new SimpleTableFormatter(false).nextRow();
		tf.nextCell().addLine("    Name  ").addLine("----");
		tf.nextCell().addLine("Type").addLine("----");
		for (DescribeSObject.Field f : ds.getFields()) {
			tf.nextRow()
				.nextCell().addLine("    "+f.getName()+"  ")
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

	@GET
	@Path("/sobjects/{sobject}")
	@Produces("application/java")
	public StreamingOutput describeSObjectAsJava(@PathParam("sobject") String sobject, final @QueryParam("package") String pkg) {
		LOGGER.log(Level.INFO,"Getting Java class for sobject "+sobject+" in database "+database);
		final DescribeSObject ds = targetdb.describeSObject(sobject);

		return new StreamingOutput() {
			public void write(OutputStream output) throws IOException, WebApplicationException {
				PojoCodeGenerator gen = new PojoCodeGenerator();
				gen.generateCode(output, ds, new ApiConfig().getApiVersion(), pkg);
			}
		};

	}

}
