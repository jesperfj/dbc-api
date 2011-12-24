package heroku.dbcapi.resources;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

@Path("/tool")
public class Tool {

	@GET
	@Produces("text/plain")
	@Path("/dbc")
	public StreamingOutput dbc() {
		return sendFile("files/dbc");
	}

	@GET
	@Produces("text/plain")
	@Path("/dbc-commands")
	public StreamingOutput dbcCommands() {
		return sendFile("files/dbc-commands");
	}

	private StreamingOutput sendFile(final String path) {
		return new StreamingOutput() {

			public void write(OutputStream output) throws IOException, WebApplicationException {
				FileInputStream fin = new FileInputStream(path);
				byte[] buf = new byte[4000];
				int n = 0;
				while ((n = fin.read(buf)) != -1) {
					output.write(buf, 0, n);
				}
			}
		};
	}

	private String MD5(String md5) {
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			byte[] array = md.digest(md5.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
		}
		return null;
	}
}
