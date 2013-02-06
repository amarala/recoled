package cowrat.recoled.client;

import com.sun.media.jsdt.*;

public class NetworkedClient implements Client {

	protected String name;

	public NetworkedClient(String name) {
		this.name = name;
	}

	public Object authenticate(AuthenticationInfo info) {
		System.err.println("ChatClient: authenticate.");
		return(null);
	}

	public String getName() {
		return(name);
	}
}
