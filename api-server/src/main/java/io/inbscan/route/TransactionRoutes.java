package io.inbscan.route;

import com.google.inject.Inject;
import io.inbscan.dto.ListModel;
import io.inbscan.dto.transaction.TransactionCriteria;
import io.inbscan.dto.transaction.TransactionDTO;
import io.inbscan.dto.transaction.TransactionModel;
import io.inbscan.service.TransactionService;
import org.jooby.mvc.GET;
import org.jooby.mvc.Path;

import java.util.Optional;


public class TransactionRoutes {

	private TransactionService txService;

	@Inject
	public TransactionRoutes(TransactionService txService) {
		this.txService= txService;
	}
	

	@GET
	@Path(ApiAppRoutePaths.V1.TRANSACTIONS)
	public ListModel<TransactionModel, TransactionCriteria> listTransactions(Optional<Integer> block, Optional<Integer> page, Optional<Integer> limit) throws Throwable {
		
		TransactionCriteria criteria = new TransactionCriteria();
		criteria.setLimit(limit.orElse(20));
		criteria.setPage(page.orElse(1));
		criteria.setBlock(block.orElse(null));
		
		return this.txService.listTransactions(criteria);
		
	}


	@GET
	@Path(ApiAppRoutePaths.V1.TRANSACTION_SEARCH)
	public TransactionModel getTrxByHash(String hash) {

		TransactionCriteria criteria = new TransactionCriteria();
		criteria.setHash(hash);
		return this.txService.getTxByHash(criteria.getHash());

	}


	@GET
	@Path(ApiAppRoutePaths.V1.TRANSACTIONS_WALLET)
	public ListModel<TransactionDTO,TransactionCriteria> listTransactionsWallet(Optional<String> address, Optional<Integer> page, Optional<Integer> limit) {

		TransactionCriteria criteria = new TransactionCriteria();
		criteria.setLimit(limit.orElse(20));
		criteria.setPage(page.orElse(1));
		criteria.setAddress(address.orElse(null));
		return this.txService.getTransactionsForWallet(criteria);

	}

    @GET
    @Path(ApiAppRoutePaths.V1.TRANSACTION_SEND)
    public String sendTransaction(String toAddress) {

         return this.txService.sendTransaction(toAddress);

    }
}
