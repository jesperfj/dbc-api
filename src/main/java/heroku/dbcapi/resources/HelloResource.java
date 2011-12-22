package heroku.dbcapi.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.inamik.utils.SimpleTableFormatter;
import com.inamik.utils.TableFormatter;

@Path("/hello")
public class HelloResource {

    @GET
    @Produces("text/plain")
    public String handleGreeting() {
		TableFormatter tf = new SimpleTableFormatter(true) // true = show border
		.nextRow()
			.nextCell()
				.addLine("Hello World !")
			.nextCell()
				.addLine("Hello Again")
		.nextRow()
			.nextCell()
				.addLine("Still there?")
			.nextCell()
				.addLine("That's good!");
		StringBuilder b = new StringBuilder();
		for(String s : tf.getFormattedTable()) {
			b.append(s+"\n");
		}
		return b.toString();
    }
    
}
