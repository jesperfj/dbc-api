package heroku.dbcapi.resources;

import heroku.dbcapi.DatabaseLink;
import heroku.dbcapi.Svc;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.force.api.QueryResult;
import com.inamik.utils.SimpleTableFormatter;
import com.inamik.utils.TableFormatter;

@Path("/databases")
public class Databases {

	@GET
	@Produces("text/plain")
	public String dblist(@HeaderParam("Authorization") String apiKey) {
		QueryResult<DatabaseLink> qr = Svc.coredb.query(
				"SELECT name, username__c, instance__c "+
				"FROM database__c WHERE developer__r.apiKey__c='"+apiKey+"'", DatabaseLink.class);

		TableFormatter tf = new SimpleTableFormatter(false).nextRow();
		tf.nextRow()
			.nextCell().addLine("Name").addLine("----")
			.nextCell().addLine("User").addLine("----")
			.nextCell().addLine("Instance").addLine("--------");

		for(DatabaseLink d : qr.getRecords()) {
			tf.nextRow()
				.nextCell().addLine(d.getName()+"  ")
				.nextCell().addLine(d.getUsername()+"  ")
				.nextCell().addLine(d.getInstance());
		}
		
		StringBuilder b = new StringBuilder();
		for(String s : tf.getFormattedTable()) {
			b.append("    "+s+"\n");
		}
		return b.toString();
	}

}
