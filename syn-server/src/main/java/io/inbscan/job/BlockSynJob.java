package io.inbscan.job;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.inbscan.SynServerConfig;
import io.inbscan.service.BlockService;
import io.inbscan.exception.ServiceException;
import io.inbscan.syn.SynBlock;
import org.jooby.quartz.Scheduled;
import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@DisallowConcurrentExecution
public class BlockSynJob {

	private SynBlock synBlock;
	private BlockService blockService;
	private SynServerConfig config;
	private static final Logger logger = LoggerFactory.getLogger(BlockSynJob.class);



	@Inject
	public BlockSynJob(SynBlock synBlock, BlockService blockService, SynServerConfig config) {
		this.synBlock = synBlock;
		this.blockService = blockService;
		this.config = config;
	}
	
	//@Scheduled(ChainConstant.BLOCK_PRODUCED_INTERVAL+"ms")
	public void validateBlockChainSync() throws ServiceException {

		Long lastBlockNum = blockService.getlastNumber();
//		Long lastBlockNum = fullNodeClient.getLastBlock().getBlockHeader().getRawData().getNumber();
		
		logger.info("current block:"+lastBlockNum);
		if (this.synBlock.isInitialSync()) {
			logger.info("Initial import ... that might take a moment, grab a coffe ...");
			this.synBlock.createInitSync(lastBlockNum);
		}else {
			this.synBlock.validateBlockChainSync(lastBlockNum);
		}


		
		this.synBlock.syncNodeBlocks();
	}

	@Scheduled("2000ms")
	public void syncFullNodeBlocks() throws ServiceException {
		
		if (!this.config.isBlockJobEnabled()) {
			return;
		}

		Long lastBlockNum = blockService.getlastNumber();
		logger.info("current full node block:"+lastBlockNum);
		
		if (this.synBlock.isInitialSync()) {
			logger.info("Initial import ... that might take a moment, grab a coffe ...");
		}


		this.synBlock.syncNodeFull(lastBlockNum);
		
	}

	
	
}
