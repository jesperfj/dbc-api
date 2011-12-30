package heroku.dbcapi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.ConnectionFactoryBuilder.Protocol;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.auth.AuthDescriptor;
import net.spy.memcached.auth.PlainCallbackHandler;

import com.force.api.ApiSession;

public class SessionCache {

	private final static Logger LOGGER = Logger.getLogger(SessionCache.class.getName());
	
	MemcachedClient mc;
	Map<String,ApiSession> local;
	
	public SessionCache() {
		if(Env.MEMCACHE_SERVERS!=null) {
			ConnectionFactoryBuilder factoryBuilder = new ConnectionFactoryBuilder();
			ConnectionFactory cf = null;
			if(Env.MEMCACHE_USERNAME!=null) {
				AuthDescriptor ad = new AuthDescriptor(
						new String[]{"PLAIN"}, 
						new PlainCallbackHandler(Env.MEMCACHE_USERNAME, Env.MEMCACHE_PASSWORD));
				cf = factoryBuilder.setProtocol(Protocol.BINARY).setAuthDescriptor(ad).build();
			} else {
				cf = factoryBuilder.setProtocol(Protocol.BINARY).build();
			}

			try {
				mc = new MemcachedClient(cf, Collections.singletonList(new InetSocketAddress(Env.MEMCACHE_SERVERS, 11211)));
				LOGGER.log(Level.INFO, "Configured Memcached");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			local = new HashMap<String,ApiSession>();
			LOGGER.log(Level.INFO, "No memcached found. Using a local hashmap instead");
		}
	}
	
	public ApiSession get(String apiKey, String database) {
		if(mc!=null) {
			return (ApiSession) mc.get(apiKey+":"+database);
		} else {
			return local.get(apiKey+":"+database);
		}
	}
	
	public void add(String apiKey, String database, ApiSession session) {
		if(mc!=null) {
			mc.add(apiKey+":"+database, 3600, session);
		} else {
			local.put(apiKey+":"+database, session);
		}
	}
	
	public Map<SocketAddress,Map<String,String>> stats() {
		if(mc!=null) {
			return mc.getStats();
		} else {
			return null;
		}
	}
	
}
