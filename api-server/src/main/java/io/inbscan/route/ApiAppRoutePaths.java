package io.inbscan.route;

public interface ApiAppRoutePaths {

	// API
	public static final String API = "/api";
	
	public static interface V1{
		
		public static final String VERSION = "/v1";
		
		//ACCOUNT
		public static final String ACCOUNT = VERSION+"/account";
		public static final String ACCOUNT_INFO = ACCOUNT+"/info";
		public static final String ACCOUNT_EXISTS = ACCOUNT+"/exists";
		public static final String ACCOUNT_TOKENS = ACCOUNT+"/tokens";
		public static final String ACCOUNT_TRANSFERS_IN = ACCOUNT+"/transfersIn";
		public static final String ACCOUNT_TRANSFERS_OUT = ACCOUNT+"/transfersOut";
		public static final String ACCOUNT_TOKENPARTICIPATIONS = ACCOUNT+"/tokenParticipations";
		public static final String ACCOUNT_VOTES_ALL = ACCOUNT+"/votes";
		public static final String ACCOUNT_VOTERS = ACCOUNT+"/voters";
		public static final String ACCOUNT_LIVE_VOTES = ACCOUNT+"/liveVotes";
		public static final String ACCOUNT_FREEZE_ALL = ACCOUNT+"/freeze";
		public static final String ACCOUNT_WITHDRAWAL = ACCOUNT+"/withdrawal";
		public static final String ACCOUNT_SEARCH = ACCOUNT+"/search";
		public static final String ACCOUNT_SEARCH_TRANSFERS_ = ACCOUNT+"/search/transfers";

		
		//WITNESS
		public static final String WITNESS = VERSION+"/witness";		
		public static final String WITNESS_ALL = WITNESS+"/all";
		
		//TOKEN
		public static final String TOKEN = VERSION+"/token";
		public static final String TOKEN_TRANSFERS = TOKEN+"/transfers";
		public static final String TOKEN_PARTICIPANTS = TOKEN+"/participants";
		public static final String TOKEN_HOLDERS = TOKEN+"/holders";
		
		//VOTES
		public static final String VOTE_ROUND = VERSION+"/vote/:round";
		public static final String VOTE_ROUNDS = VERSION+"/vote/rounds";
		public static final String VOTE_ROUND_STATS = VERSION+"/vote/stats/:maxRound";
		public static final String VOTE_ROUND_VOTES = VERSION+"/vote/:round/:address";
		public static final String VOTE_LIVE_VOTES = VERSION+"/vote/live/:address";
		public static final String VOTE_LIVE = VERSION+"/vote/live";		
		public static final String VOTE_LIVE_TOTAL = VERSION+"/vote/live/total";
		
		//TRON
		public static final String TRON = VERSION+"/tron";
		public static final String TRON_BROADCAST = TRON+"/broadcast";
		
		
		//BLOCK
		public static final String BLOCK = VERSION+"/block";
		public static final String BLOCK_INFO = VERSION+"/block/info";
		public static final String BLOCK_LIST = VERSION+"/blocks";
		public static final String BLOCK_LATEST = BLOCK+"/latest";
		public static final String BLOCK_CREATE = BLOCK+"/create";
		public static final String BLOCK_SEARCH = BLOCK+"/search";
		public static final String BLOCK_SEARCH_TRANSFERS = BLOCK+"/search/transfers";
		public static final String NODE_INFO = VERSION+"/node/info";

		//TRANSACTION
		public static final String TRANSACTIONS = VERSION+"/transactions";
		public static final String TRANSACTIONS_WALLET = VERSION+"/transactions/wallet";
		public static final String TRANSACTION_SEARCH = VERSION+"/transaction/search";
		public static final String TRANSACTION_SEND = VERSION+"/transaction/send";


		
	}
	
}
