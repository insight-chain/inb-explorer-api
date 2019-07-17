package io.inbscan;

import io.inbscan.job.AccountSynJob;
import io.inbscan.job.BlockSynJob;
import io.inbscan.job.NodeSynJob;
import io.inbscan.job.SynNodeSynJob;
import org.jooby.Jooby;
import org.jooby.flyway.Flywaydb;
import org.jooby.jdbc.Jdbc;
import org.jooby.jooq.jOOQ;
import org.jooby.json.Jackson;
import org.jooby.quartz.Quartz;
import org.jooq.DSLContext;
import org.jooq.conf.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SynServer extends Jooby {
	
	private static final Logger logger = LoggerFactory.getLogger(SynServer.class);
	
	{
		use(new SynServerModule());


		use(new Jdbc());
		use(new jOOQ());

		use(new Jackson());
		use(new Flywaydb());

		
		use(new Quartz(AccountSynJob.class, SynNodeSynJob.class, BlockSynJob.class, NodeSynJob.class));

		
		onStart(registry -> {
			
			SynServerConfig config = registry.require(SynServerConfig.class);
			
			logger.info("=> Sync node up [id = {}]",config.getNodeId());
			
			DSLContext dslContext = registry.require(DSLContext.class);

			Settings settings = new Settings();
			settings.setRenderSchema(false);

			dslContext.configuration().set(settings);

		});
		
		onStop((registry)->{
			
		});
		

		get("/", (req, res) -> {

			res.send("");
		});

	}

	public static void main(final String[] args) {
		run(SynServer::new, args);
	}

}
