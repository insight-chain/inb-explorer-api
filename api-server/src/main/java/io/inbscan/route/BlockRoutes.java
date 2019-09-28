package io.inbscan.route;

import com.google.inject.Inject;
import io.inbscan.dto.ListModel;
import io.inbscan.dto.block.BlockChainDto;
import io.inbscan.dto.block.BlockCriteriaDTO;
import io.inbscan.dto.block.BlockDTO;
import io.inbscan.dto.node.NodeCriteriaDTO;
import io.inbscan.model.tables.pojos.Node;
import io.inbscan.service.BlockService;
import org.jooby.Request;
import org.jooby.Response;
import org.jooby.mvc.GET;
import org.jooby.mvc.Path;

import java.util.Optional;


public class BlockRoutes {

	private BlockService blockService;

	@Inject
	public BlockRoutes(BlockService blockService) {
		this.blockService = blockService;
	}
	
	/**
	 * Get the latest available block
	 * @param req
	 * @param res
	 * @return
	 * @throws Throwable
	 */
	@GET
	@Path(ApiAppRoutePaths.V1.BLOCK_LATEST)
	public BlockDTO broadcastTransaction(Request req, Response res) throws Throwable {
		req.param("toto");
	    
		return this.blockService.getLastBlock();
		
	}

	@GET
	@Path(ApiAppRoutePaths.V1.BLOCK_LIST)
	public ListModel<BlockDTO, BlockCriteriaDTO> listBlocks(Optional<Integer> page, Optional<Integer> limit) throws Throwable {

		BlockCriteriaDTO criteria = new BlockCriteriaDTO();

		criteria.setLimit(limit.orElse(20));
		criteria.setPage(page.orElse(1));

		return this.blockService.listBlocks(criteria);

	}

	@GET
	@Path(ApiAppRoutePaths.V1.BLOCK_CREATE)
	public void createBlock(){

		this.blockService.createBlock();
	}
//
	@GET
	@Path(ApiAppRoutePaths.V1.BLOCK_SEARCH)
	public BlockDTO searchBlock(Integer blockNumber){
		return this.blockService.getBlockByNum(blockNumber);
	}


	@GET
	@Path(ApiAppRoutePaths.V1.BLOCK_INFO)
	public BlockChainDto blockInfo(){
		return this.blockService.getBlockChainInfo();
	}

	@GET
	@Path(ApiAppRoutePaths.V1.NODE_INFO)
	public ListModel<Node, NodeCriteriaDTO> NodeInfo(Optional<Integer> page, Optional<Integer> limit,Optional<String> address){
		NodeCriteriaDTO criteria = new NodeCriteriaDTO();
		criteria.setLimit(limit.orElse(200));
		criteria.setPage(page.orElse(1));
		criteria.setAddress(address.orElse(null));
		return this.blockService.getNodeInfo(criteria);
	}
//
//	@GET
//	@Path(ApiAppRoutePaths.V1.BLOCK_SEARCH_TRANSFERS)
//	public BlockDTO searchBlockTransfers(Integer blockNumber){
//		BLockCriteria bLockCriteria = new BLockCriteria();
//		bLockCriteria.setBlockNum(blockNumber);
//		return this.blockService.getBlock(bLockCriteria);
//	}
}
