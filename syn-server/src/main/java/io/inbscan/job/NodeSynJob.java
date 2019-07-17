package io.inbscan.job;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.inbscan.SynServerConfig;
import io.inbscan.syn.SynNode;
import org.jooby.quartz.Scheduled;
import org.quartz.DisallowConcurrentExecution;

@Singleton
@DisallowConcurrentExecution
public class NodeSynJob {

	private SynNode synNode;
	private SynServerConfig config;

	@Inject
	public NodeSynJob(SynNode synNode, SynServerConfig config) {
		this.synNode = synNode;
		this.config = config;
	}
//
	@Scheduled("1d")
	public void addNewNodes() throws InterruptedException{

		if (!this.config.isNodesJobEnabled()) {
			return;
		}

		this.synNode.syncNodes();


	}

}
