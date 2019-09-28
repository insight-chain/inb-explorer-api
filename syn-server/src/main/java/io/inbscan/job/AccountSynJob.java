package io.inbscan.job;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.inbscan.SynServerConfig;
import io.inbscan.exception.ServiceException;
import io.inbscan.service.AccountService;
import io.inbscan.syn.SynAccount;
import org.jooby.quartz.Scheduled;
import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@DisallowConcurrentExecution
public class AccountSynJob {

	private AccountService accountService;
	private SynAccount synAccount;
	private SynServerConfig config;
	private static final Logger logger = LoggerFactory.getLogger(AccountSynJob.class);
	@Inject
	public AccountSynJob( AccountService accountService, SynServerConfig config, SynAccount synAccount) {
		this.accountService = accountService;
		this.synAccount = synAccount;
		this.config = config;
	}

	@Scheduled("10ms")
	public void syncAccount() throws ServiceException {

		if (!this.config.isAccountJobEnabled()) {
			return;
		}

		this.synAccount.syncAccounts();

	}
//
//	@Scheduled("20ms")
//	public void syncAccountVote() throws ServiceException {
//
//		if (!this.config.isAccountJobEnabled()) {
//			return;
//		}
//
//		this.accountSyncService.syncAccountVote();
//
//	}

//	@Scheduled("30ms")
//	public void syncAccountResync() throws ServiceException {
//
//		if (!this.config.isAccountJobEnabled()) {
//			return;
//		}
//
//		this.synAccount.syncAccountResync();
//
//	}

}
