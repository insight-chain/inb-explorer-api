package io.inbscan.job;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.inbscan.model.tables.SyncNode;
import io.inbscan.SynServerConfig;
import org.jooby.quartz.Scheduled;
import org.jooq.DSLContext;
import org.quartz.DisallowConcurrentExecution;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Singleton
@DisallowConcurrentExecution
public class SynNodeSynJob {

	private SynServerConfig config;
	private DSLContext dslContext;

	@Inject
	public SynNodeSynJob(SynServerConfig config, DSLContext dslContext){
		this.config = config;
		this.dslContext = dslContext;
	}


	@Scheduled("10s")
	public void ping() {

		this.dslContext.insertInto(SyncNode.SYNC_NODE)
		.set(SyncNode.SYNC_NODE.NODE_ID, this.config.getNodeId())
		.onDuplicateKeyUpdate()
		.set(SyncNode.SYNC_NODE.PING, Timestamp.valueOf(LocalDateTime.now()))
		.execute()


		;

	}
	
}

