package fr.be.your.self.backend.config.root;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.google.api.client.util.Value;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import fr.be.your.self.backend.cache.CacheManager;
import fr.be.your.self.util.StringUtils;

@Configuration
@ComponentScan(basePackages = { "fr.be.your.self.backend.cache" })
public class ClusterConfig {
	@Value("${cluster.local.port}")
	private int localPort;
	
	@Value("${cluster.master}")
	private boolean isMaster;
	
	@Value("${cluster.members}")
	private String clusterMembers;
	
	@Bean
	public CacheManager cacheManager() {
		return new CacheManager(hazelcastInstance());
	}
	
	/*
	@Bean
	public ClusterManager clusterManager() {
		return new ClusterManager(hazelcastInstance(), isMaster);
	}
	*/
	
	@Bean
	public HazelcastInstance hazelcastInstance() {
		NetworkConfig networkConfig = new NetworkConfig();
		networkConfig.setPort(localPort);
        networkConfig.setPortAutoIncrement(true);
        
		if (!StringUtils.isNullOrSpace(clusterMembers)) {
			List<String> members = Arrays.asList(clusterMembers.split(","));
			
			TcpIpConfig tcpIpConfig = new TcpIpConfig();
	        tcpIpConfig.setEnabled(true);
	        tcpIpConfig.setMembers(members);
	        
	        MulticastConfig multicastConfig = new MulticastConfig();
	        multicastConfig.setEnabled(false);
	        
	        JoinConfig joinConfig = new JoinConfig();
	        joinConfig.setTcpIpConfig(tcpIpConfig);
	        joinConfig.setMulticastConfig(multicastConfig);
	        
	        networkConfig.setJoin(joinConfig);
		}
		
		Config config = new Config();
		config.setNetworkConfig(networkConfig);
		
		//ListenerConfig memberListener = new ListenerConfig(new ClusterMembershipListener());
		//config.addListenerConfig(memberListener);
		
		return Hazelcast.newHazelcastInstance(config);
	};
}
