package io.inbscan;

import com.google.inject.Inject;
import com.typesafe.config.Config;

import java.util.List;
import java.util.Random;

public class SynServerConfig {

	private Config config;

	private int generatedNodeId;

	@Inject
	public SynServerConfig(Config config) {
		this.config = config;
		Random r = new Random();
		this.generatedNodeId = r.nextInt(1000000-1000)+1000;
	}
	
	
	public int getNodeId() {
		int id = this.config.getInt("node.id");
		return id==-1 ? this.generatedNodeId : id;
	}

	public boolean isNodesJobEnabled() {
		return this.config.getBoolean("jobs.nodes");
	}
	
	public int getSyncBatchSize() {
		return this.config.getInt("node.syncBatchSize");
	}
	
	public boolean isAccountJobEnabled() {
		return this.config.getBoolean("jobs.account");
	}

	public boolean isBlockJobEnabled() {
		return this.config.getBoolean("jobs.block");
	}

	public String getGeoDbPath() {
		return this.config.getString("geodb.path");
	}


}
