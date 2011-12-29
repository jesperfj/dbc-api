package heroku.dbcapi;

public class Env {
	public static final String FORCE_COREDB_URL  = System.getenv("FORCE_COREDB_URL");
	public static final String LINK_OAUTH_KEY    = System.getenv("LINK_OAUTH_KEY");
	public static final String LINK_OAUTH_SECRET = System.getenv("LINK_OAUTH_SECRET");

	// optional. If not present an internal cache will be used
	public static final String MEMCACHE_USERNAME = System.getenv("MEMCACHE_USERNAME");
	public static final String MEMCACHE_PASSWORD = System.getenv("MEMCACHE_PASSWORD");
	public static final String MEMCACHE_SERVERS  = System.getenv("MEMCACHE_SERVERS");

}
