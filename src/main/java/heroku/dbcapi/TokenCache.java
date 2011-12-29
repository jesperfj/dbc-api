package heroku.dbcapi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.ConnectionFactoryBuilder.Protocol;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.auth.AuthDescriptor;
import net.spy.memcached.auth.PlainCallbackHandler;

public class TokenCache {

	MemcachedClient mc;
	Map<String,String> local;
	
	public TokenCache() {
		if(Env.MEMCACHE_USERNAME!=null) {
			AuthDescriptor ad = new AuthDescriptor(new String[]{"PLAIN"}, new PlainCallbackHandler(Env.MEMCACHE_USERNAME, Env.MEMCACHE_PASSWORD));
			ConnectionFactoryBuilder factoryBuilder = new ConnectionFactoryBuilder();
			ConnectionFactory cf = factoryBuilder.setProtocol(Protocol.BINARY).setAuthDescriptor(ad).build();

			try {
				mc = new MemcachedClient(cf, Collections.singletonList(new InetSocketAddress(Env.MEMCACHE_SERVERS, 11211)));
				mc.add("test", 0, "testData");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			local = new HashMap<String,String>();
		}
	}
	
	public String get(String apiKey, String database) {
		if(mc!=null) {
			return (String) mc.get(apiKey+":"+database);
		} else {
			return local.get(apiKey+":"+database);
		}
	}
	
	public void add(String apiKey, String database, String accessToken) {
		if(mc!=null) {
			mc.add(apiKey+":"+database, 3600, accessToken);
		} else {
			local.put(apiKey+":"+database, accessToken);
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
