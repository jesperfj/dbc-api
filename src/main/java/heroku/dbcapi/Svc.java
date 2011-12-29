package heroku.dbcapi;

import com.force.api.ApiConfig;
import com.force.api.ForceApi;

public class Svc {
	static public final ForceApi coredb = new ForceApi(new ApiConfig().setForceURL(Env.FORCE_COREDB_URL));
	static public final TokenCache cache = new TokenCache();
}
