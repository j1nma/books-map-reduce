package ar.edu.itba.server;

import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.singletonList;

/**
 * Member node entry point class.
 */
public class Server {
	private final static Logger LOGGER = LoggerFactory.getLogger(Server.class);
	
	public static void main(String[] args) {
		LOGGER.info("Starting member node...");
		
		// prepare Hazelcast cluster
		Hazelcast.newHazelcastInstance();
		Hazelcast.newHazelcastInstance();
		Hazelcast.newHazelcastInstance();
	}
}