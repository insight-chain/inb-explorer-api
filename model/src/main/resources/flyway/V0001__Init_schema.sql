
--
-- Table structure for table `account`
--
DROP TABLE IF EXISTS `account`;
CREATE TABLE `account` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `account_name` varchar(250) DEFAULT NULL,
  `type` tinyint(4) NOT NULL,
  `address` varchar(45) NOT NULL,
  `balance` double NOT NULL DEFAULT '0',
  `usable` bigint(20) unsigned DEFAULT NULL,
  `used` bigint(20) unsigned DEFAULT NULL,
  `latest_withdraw_time` datetime(3) DEFAULT NULL,
  `create_time` datetime(3) DEFAULT NULL,
  `is_witness` tinyint(3) unsigned DEFAULT NULL,
  `is_committee` tinyint(3) unsigned DEFAULT NULL,
  `transfer_from_count` int(11) DEFAULT NULL,
  `transfer_to_count` int(11) DEFAULT NULL,
  `tokens_count` int(11) DEFAULT NULL,
  `redeem` double NOT NULL DEFAULT '0',
  `redeem_start_height` bigint(20) DEFAULT NULL,
  `vote_number`bigint(20) DEFAULT NULL,
  `last_receive_vote_award_height`  bigint(20) DEFAULT NULL,
  `regular` double NOT NULL DEFAULT '0',
  `mortgage` double DEFAULT NULL,
  `mortgage_height` bigint(20) DEFAULT NULL,
  `nonce` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `account_address_unique` (`address`),
  KEY `account_address_index` (`address`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


--
-- Table structure for table `block`
--
DROP TABLE IF EXISTS `block`;
CREATE TABLE `block` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `num` bigint(20) unsigned DEFAULT NULL,
  `hash` varchar(200) DEFAULT NULL,
  `parent_hash` varchar(200) NOT NULL,
  `txTrieRoot` varchar(64) DEFAULT NULL,
  `tx_count` int(10) unsigned NOT NULL,
  `timestamp` datetime(3) NOT NULL,
  `size` int(10) unsigned NOT NULL,
  `witness_id` bigint(20) unsigned DEFAULT NULL,
  `witness_address` varchar(164) DEFAULT NULL,
  `block_time` int(10) unsigned DEFAULT NULL,
  `confirmed` tinyint(1) NOT NULL DEFAULT '0',
  `reward` double unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `num_UNIQUE` (`num`),
  KEY `fk_block_witness_id_idx` (`witness_id`),
  KEY `block_witness_address_index` (`witness_address`),
  KEY `block_num_index` (`num`),
  KEY `block_hash_index` (`hash`),
  KEY `block_timestamp_index` (`timestamp`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


--
-- Table structure for table `block_chain`
--
DROP TABLE IF EXISTS `block_chain`;
CREATE TABLE `block_chain` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `latest_block_num` bigint(20) unsigned DEFAULT NULL,
  `transaction_num` bigint(20) unsigned NOT NULL DEFAULT '0',
  `address_num` bigint(20) unsigned NOT NULL DEFAULT '0',
  `irreversible_block_num` bigint(20) unsigned DEFAULT NULL,
  `current_tps` int(11) NOT NULL DEFAULT '0',
  `highest_tps` int(11) NOT NULL DEFAULT '0',
  `inb_total_supply` double unsigned DEFAULT NULL,
  `voted_inb` double unsigned DEFAULT NULL,
  `mortgage_net_inb` double unsigned DEFAULT NULL,
  `total_net` int(11) DEFAULT NULL,
  `mortgage_cpu_inb` double unsigned DEFAULT NULL,
  `total_cpu` int(11) DEFAULT NULL,
  `current_producer` varchar(128) DEFAULT NULL,
  `next_producer` varchar(128) DEFAULT NULL,
  `current_net_consumed` int(11) NOT NULL DEFAULT '0',
  `net_limit` int(11) NOT NULL DEFAULT '0',
  `current_cpu_consumed` int(11) DEFAULT NULL,
  `cpu_limit` int(11) DEFAULT NULL,
  `inb_current_price` double unsigned DEFAULT NULL,
  `inb_supply` double unsigned DEFAULT NULL,
  `dapp_num` int(11) DEFAULT NULL,
  `transaction_num_last_day` bigint(20) unsigned DEFAULT NULL,
  `active_address_num_last_day` bigint(20) unsigned DEFAULT NULL,
  `new_address_num_last_day` bigint(20) unsigned DEFAULT NULL,
  `super_node_num` int(11) DEFAULT NULL,
  `node_num` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;



--
-- Table structure for table `node`
--
DROP TABLE IF EXISTS `node`;
CREATE TABLE `node` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `host` varchar(40) NOT NULL,
  `port` varchar(32) NOT NULL,
  `up` tinyint(4) NOT NULL DEFAULT '0',
  `longitude` decimal(11,8) DEFAULT NULL,
  `latitude` decimal(10,8) DEFAULT NULL,
  `country_code` varchar(10) DEFAULT NULL,
  `country` varchar(45) DEFAULT NULL,
  `city` varchar(150) DEFAULT NULL,
  `last_updated` datetime DEFAULT NULL,
  `date_created` datetime NOT NULL,
  `last_up` datetime DEFAULT NULL,
  `name` varchar(256) DEFAULT NULL,
  `vote_ratio` float DEFAULT NULL,
  `image` varchar(256) DEFAULT NULL,
  `web_site` varchar(256) DEFAULT NULL,
  `address` varchar(256) DEFAULT NULL,
  `node_id` varchar(256) DEFAULT NULL,
  `email` varchar(64) DEFAULT NULL,
  `vote_number` bigint(20) unsigned DEFAULT NULL,
  `type` int(2) DEFAULT NULL,
  `intro` varchar(512) DEFAULT NULL,
  `data` varchar(512) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `node_unique` (`host`,`port`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `sync_account`
--
DROP TABLE IF EXISTS `sync_account`;
CREATE TABLE `sync_account` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `address` varchar(164) NOT NULL,
  `date_created` datetime DEFAULT NULL,
  `date_locked` datetime DEFAULT NULL,
  `origin` varchar(45) DEFAULT NULL,
  `tx_timestamp` datetime DEFAULT NULL,
  `node_id` int(10) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `address_unique` (`address`,`origin`),
  KEY `address_index` (`address`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;



--
-- Table structure for table `sync_block`
--
DROP TABLE IF EXISTS `sync_block`;
CREATE TABLE `sync_block` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `block_num` bigint(20) unsigned DEFAULT NULL,
  `date_start` datetime(3) DEFAULT NULL,
  `date_end` datetime(3) DEFAULT NULL,
  `date_locked` datetime(3) DEFAULT NULL,
  `node_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `block_num_UNIQUE` (`block_num`),
  KEY `block_num_index` (`block_num`),
  KEY `date_start_index` (`date_start`),
  KEY `date_end_index` (`date_end`),
  KEY `date_locked_index` (`date_locked`),
  KEY `node_id_index` (`node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;




--
-- Table structure for table `sync_node`
--
DROP TABLE IF EXISTS `sync_node`;
CREATE TABLE `sync_node` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `node_id` int(11) NOT NULL,
  `is_validating` varchar(45) NOT NULL DEFAULT '0',
  `ping` datetime DEFAULT NULL,
  `sync_start_full` bigint(20) DEFAULT NULL,
  `sync_end_full` bigint(20) DEFAULT NULL,
  `start_full_date` datetime DEFAULT NULL,
  `end_full_date` datetime DEFAULT NULL,
  `sync_start_solidity` bigint(20) DEFAULT NULL,
  `sync_end_solidity` bigint(20) DEFAULT NULL,
  `start_solidity_date` datetime DEFAULT NULL,
  `end_solidity_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `node_id_UNIQUE` (`node_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;




--
-- Table structure for table `transaction`
--
DROP TABLE IF EXISTS `transaction`;
CREATE TABLE `transaction` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `hash` varchar(200) NOT NULL,
  `timestamp` datetime DEFAULT NULL,
  `expiration` datetime DEFAULT NULL,
  `confirmed` tinyint(1) NOT NULL DEFAULT '0',
  `block_id` bigint(20) unsigned DEFAULT NULL,
  `block_num` bigint(20) unsigned DEFAULT NULL,
  `block_hash` varchar(200)  DEFAULT NULL,
  `from` varchar(45) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `bindwith` varchar(64) DEFAULT NULL,
  `input` varchar(5000) DEFAULT NULL,
  `status` varchar(64) DEFAULT NULL,
  `nonce` bigint(20) unsigned DEFAULT NULL,
  `index` double DEFAULT NULL,
  `gas` double unsigned DEFAULT NULL,
  `gas_price` double unsigned DEFAULT NULL,
  `tx_receipt_status` int(2) DEFAULT NULL,
  `contract_address` varchar(256) DEFAULT NULL,
  `cumulative_gas_used` double unsigned DEFAULT NULL,
  `gas_used` double unsigned DEFAULT NULL,
  `confirmations` varchar(256) DEFAULT NULL,
  `token_name` varchar(200) DEFAULT NULL,
  `token_address` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `hash_UNIQUE` (`hash`),
  KEY `fk_transaction_block_idx` (`block_id`),
  KEY `transaction_timestamp_index` (`timestamp`),
  KEY `transaction_hash_index` (`hash`),
  KEY `token_name_index` (`token_name`),
  KEY `token_address_index` (`token_address`),
  CONSTRAINT `fk_transaction_block` FOREIGN KEY (`block_id`) REFERENCES `block` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;




--
-- Table structure for table `transfer`
--
DROP TABLE IF EXISTS `transfer`;
CREATE TABLE `transfer` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `from` varchar(45) NOT NULL,
  `to` varchar(45) DEFAULT NULL,
  `amount` bigint(20) unsigned NOT NULL,
  `token` varchar(45) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `timestamp` datetime NOT NULL,
  `transaction_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `transfer_from_index` (`from`),
  KEY `transfer_to_index` (`to`),
  KEY `transfer_amount_index` (`amount`),
  KEY `transfer_token_index` (`token`),
  KEY `transfer_timestamp_index` (`timestamp`),
  KEY `fk_transfer_tx_id_idx` (`transaction_id`),
  CONSTRAINT `fk_transfer_tx_id` FOREIGN KEY (`transaction_id`) REFERENCES `transaction` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

--
-- Table structure for table `transaction_inline`
--
DROP TABLE IF EXISTS `transaction_log`;
CREATE TABLE `transaction_log` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `block_hash` varchar(200) DEFAULT NULL,
  `address` varchar(200) DEFAULT NULL,
  `inline_transaction_hash` varchar(200) DEFAULT NULL,
  `transaction_hash` varchar(200) DEFAULT NULL,
  `transaction_type` int(2) DEFAULT NULL,
  `transaction_index` int(2) DEFAULT NULL,
  `removed` int(2) DEFAULT NULL,
  `from` varchar(200) DEFAULT NULL,
  `to` varchar(200) DEFAULT NULL,
  `amount` double DEFAULT NULL,
  `timestamp` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `inline_from_index` (`from`),
  KEY `inline_to_index` (`to`),
  KEY `inline_timestamp_index` (`timestamp`),
  KEY `fk_transfer_tx_hash_idx` (`transaction_hash`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


--
-- Table structure for table `store`
--
DROP TABLE IF EXISTS `store`;
CREATE TABLE `store` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `address` varchar(200) DEFAULT NULL,
  `hash` varchar(200) DEFAULT NULL,
  `start_height` bigint(20) DEFAULT NULL,
  `last_received_height` bigint(20) DEFAULT NULL,
  `lock_height` bigint(20) DEFAULT NULL,
  `received` bigint(20) DEFAULT NULL,
  `amount` double DEFAULT NULL,
  `timestamp` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `store_address_index` (`address`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


--
-- Table structure for table `token`
--
DROP TABLE IF EXISTS `token`;
CREATE TABLE `token` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `address` varchar(200) DEFAULT NULL,
  `intro` varchar(200) DEFAULT NULL,
  `price` decimal DEFAULT NULL,
  `light_token_address` varchar(200) DEFAULT NULL,
  `issue_account_address` varchar(200) DEFAULT NULL,
  `owner_address` varchar(200) DEFAULT NULL,
  `issue_transaction_hash` varchar(200) DEFAULT NULL,
  `symbol` varchar(200) DEFAULT NULL,
  `name` varchar(200) DEFAULT NULL,
  `icon` varchar(200) DEFAULT NULL,
  `total_supply` double DEFAULT NULL,
  `total_circulation` double DEFAULT NULL,
  `total_market_cap` double DEFAULT NULL,
  `decimals` int(2) DEFAULT NULL,
  `state` int(2) DEFAULT NULL,
  `holder_number` int(11) DEFAULT NULL,
  `timestamp` datetime DEFAULT NULL,
  `web_site` varchar(200) DEFAULT NULL,
  `telegram` varchar(200) DEFAULT NULL,
  `facebook` varchar(200) DEFAULT NULL,
  `wechat` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE  KEY `token_address_index` (`address`),
  KEY `token_light_address_index` (`light_token_address`),
  KEY `token_issue_address_index` (`issue_account_address`),
  KEY `token_trans_index` (`issue_transaction_hash`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


--
-- Table structure for table `token_holder`
--
DROP TABLE IF EXISTS `token_holder`;
CREATE TABLE `token_holder` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `token_address` varchar(200) DEFAULT NULL,
  `token_symbol` varchar(200) DEFAULT NULL,
  `holder_address` varchar(200) DEFAULT NULL,
  `balance` double DEFAULT NULL,
  `timestamp` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `token_address_index` (`token_address`),
  KEY `token_symbol_index` (`token_symbol`),
  KEY `transaction_hash_index` (`holder_address`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;