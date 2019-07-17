package io.inbscan.route;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.inbscan.dto.ListModel;
import io.inbscan.dto.ListResult;
import io.inbscan.dto.account.AccountCriteria;
import io.inbscan.dto.account.Accountdto;
import io.inbscan.dto.transaction.TransactionCriteria;
import io.inbscan.dto.transaction.TransactionDTO;
import io.inbscan.dto.transaction.TransferModel;
import io.inbscan.service.AccountService;
import io.inbscan.service.TransactionService;
import org.jooby.mvc.GET;
import org.jooby.mvc.Path;

import java.util.Optional;

@Singleton
public class AccountRoutes {

	private AccountService accountService;
	private TransactionService transactionService;
	
	@Inject
	public AccountRoutes(AccountService accountService, TransactionService transactionService) {
		this.accountService = accountService;
		this.transactionService = transactionService;
	}

	@GET
	@Path(ApiAppRoutePaths.V1.ACCOUNT_TRANSFERS_IN)
	public ListResult<TransferModel, AccountCriteria> transfers(String address, Optional<Integer> limit, Optional<Integer> page) {

		AccountCriteria criteria = new AccountCriteria();

		criteria.setAddress(address);
		criteria.setLimit(limit.orElse(20));
		criteria.setPage(page.orElse(1));

		return this.accountService.listTransfersIn(criteria);
	}

	/**
	 * Get basic informations on account
	 * @param address
	 * @return {@link Accountdto}
	 * @throws Throwable
	 */
	@GET
	@Path(ApiAppRoutePaths.V1.ACCOUNT_SEARCH)
	public Accountdto accountInfo(String address) throws Throwable {
		AccountCriteria accountCriteria = new AccountCriteria();
		accountCriteria.setAddress(address);
		return this.accountService.getActByAddress(accountCriteria);
//		return this.accountInfoService.getAccountByAddress(new AccountDetailCriteriaDTO(address));
	}



	@GET
	@Path(ApiAppRoutePaths.V1.ACCOUNT_TRANSFERS_OUT)	
	public ListResult<TransferModel, AccountCriteria> transfersOut(String address,Optional<Boolean> trx,Optional<Integer> limit,Optional<Integer> page) {
		
		AccountCriteria criteria = new AccountCriteria();
		
		criteria.setAddress(address);
		criteria.setLimit(limit.orElse(20));
		criteria.setTrx(trx.orElse(false));
		criteria.setPage(page.orElse(1));
		
		return this.accountService.listTransfersOut(criteria);
	}



	@GET
	@Path(ApiAppRoutePaths.V1.ACCOUNT_SEARCH_TRANSFERS_)
	public ListModel<TransactionDTO, TransactionCriteria> listTransactionsWallet(Optional<String> address, Optional<Integer> page, Optional<Integer> limit) {

		TransactionCriteria criteria = new TransactionCriteria();
		criteria.setLimit(limit.orElse(20));
		criteria.setPage(page.orElse(1));
		criteria.setAddress(address.orElse(null));
		return this.transactionService.getTransactionsForWallet(criteria);

	}

	/* FIXME
	 * DISABLED
	@GET
	@Path(ApiAppRoutePaths.V1.ACCOUNT_FREEZE_ALL)	
	public ListResult<FrozenBalanceModel, AccountCriteria> freezeAll(String address,Optional<Integer> limit,Optional<Integer> page) {
		
		AccountCriteria criteria = new AccountCriteria();
		
		criteria.setAddress(address);
		criteria.setLimit(limit.orElse(20));
		criteria.setPage(page.orElse(1));
		
		return this.accountService.listFrozenBalance(criteria);
	}
	*/
	
	
	
}
