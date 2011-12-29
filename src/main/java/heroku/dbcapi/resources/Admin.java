package heroku.dbcapi.resources;

import heroku.dbcapi.Svc;

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketAddress;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import com.inamik.utils.SimpleTableFormatter;
import com.inamik.utils.TableFormatter;
@Path("/admin")
public class Admin {

	@GET
	@Path("/cache")
	@Produces("text/plain")
	public StreamingOutput cacheStats() {
		return new StreamingOutput() {
			public void write(OutputStream out) throws IOException, WebApplicationException {
				Map<SocketAddress,Map<String,String>> stats = Svc.cache.stats();
				if(stats==null) {
					out.write("No cache stats found\n".getBytes("UTF-8"));
					return;
				}

				for(SocketAddress addr : stats.keySet()) {
					
					out.write(new String("Stats for "+addr+"\n\n").getBytes("UTF-8"));
					TableFormatter tf = new SimpleTableFormatter(false).nextRow();
					tf.nextCell().addLine("    Key").addLine("    ---");
					tf.nextCell().addLine("Value").addLine("-----");
					for(String key : stats.get(addr).keySet()) {
						tf.nextRow()
							.nextCell().addLine("    "+key)
							.nextCell().addLine(stats.get(addr).get(key));
					}
					
					for(String line : tf.getFormattedTable()) {
						out.write(line.getBytes("UTF-8"));
						out.write('\n');
					}
				}

			}
		};
	}

}
