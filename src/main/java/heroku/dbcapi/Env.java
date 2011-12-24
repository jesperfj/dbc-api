package heroku.dbcapi;

public class Env {
	public static final String FORCE_COREDB_URL  = System.getenv("FORCE_COREDB_URL");
	public static final String LINK_OAUTH_KEY    = System.getenv("LINK_OAUTH_KEY");
	public static final String LINK_OAUTH_SECRET = System.getenv("LINK_OAUTH_SECRET");
}
