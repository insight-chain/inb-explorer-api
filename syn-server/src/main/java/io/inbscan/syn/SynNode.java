package io.inbscan.syn;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.inject.Inject;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import io.inbscan.SynServerConfig;
import io.inbscan.chain.InbChainService;
import io.inbscan.dto.node.NodeDTO;
import io.inbscan.dto.node.NodeInfo;
import io.inbscan.model.tables.pojos.Node;
import io.inbscan.model.tables.records.NodeRecord;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static io.inbscan.model.tables.Node.NODE;

public class SynNode {

    private DSLContext dslContext;
    private SynServerConfig config;
    private InbChainService inbChainService;

    private static final int NODE_PING_TIMEOUT = 5000;

    private static final Logger logger = LoggerFactory.getLogger(SynNode.class);

    @Inject
    public SynNode(DSLContext dslContext, SynServerConfig config, InbChainService inbChainService) {
        this.dslContext = dslContext;
        this.config = config;
        this.inbChainService = inbChainService;
    }

    public void syncNodes() throws InterruptedException {

        JSONArray jsonArray = inbChainService.getCandidateNodesInfo().getJSONArray("result");
        if(jsonArray != null & jsonArray.size() != 0) {
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject nodeObj = JSONArray.parseObject(jsonArray.get(i).toString());
                JSONObject nodeInfoObj = nodeObj.getJSONObject("NodeInfo");
                Node node = this.dslContext.select().from(NODE).where(NODE.NODE_ID.eq(nodeInfoObj.getString("id"))).fetchOneInto(Node.class);
                if(node==null) {
                    logger.info("syn node start" + nodeObj.getString("Address"));
                    this.dslContext.insertInto(NODE)
                            .set(NODE.HOST, nodeInfoObj.getString("ip"))
                            .set(NODE.PORT, Integer.valueOf(nodeInfoObj.getString("port")))
                            .set(NODE.DATE_CREATED, Timestamp.valueOf(LocalDateTime.now()))
                            .set(NODE.COUNTRY, nodeInfoObj.getString("nation"))
                            .set(NODE.IMAGE, nodeInfoObj.getString("image"))
                            .set(NODE.WEB_SITE, nodeInfoObj.getString("website"))
                            .set(NODE.ADDRESS, nodeInfoObj.getString("address"))
                            .set(NODE.CITY, nodeInfoObj.getString("city"))
                            .set(NODE.NAME, nodeInfoObj.getString("name"))
                            .set(NODE.NODE_ID, nodeInfoObj.getString("id"))
                            .set(NODE.VOTE_NUMBER, ULong.valueOf(nodeObj.getInteger("Stake")))
                            .set(NODE.EMAIL, nodeInfoObj.getString("email"))
                            .set(NODE.TYPE, 2)
                            .execute();
                }
            }

            JSONArray json = inbChainService.getSuperNodesInfo().getJSONArray("result");
            if(json.size()!=0){
                for (int i = 0; i < json.size(); i++) {
                    JSONObject superNodeObj = JSONArray.parseObject(jsonArray.get(i).toString());
                    JSONObject superNodeInfoObj = superNodeObj.getJSONObject("NodeInfo");
                    Node node = this.dslContext.select().from(NODE).where(NODE.NODE_ID.eq(superNodeInfoObj.getString("id"))).fetchOneInto(Node.class);
                    if(node!=null){
                        this.dslContext.update(NODE)
                            .set(NODE.TYPE, 1)
                            .where(NODE.ID.eq(node.getId()))
                            .execute();
                    }
                }

            }

//            for (NodeDTO superNode : listSuperNode) {
//                Node node = this.dslContext.select().from(NODE).where(NODE.NODE_ID.eq(superNode.getId())).fetchOneInto(Node.class);
//                if (node != null) {
//                    this.dslContext.update(NODE)
//                            .set(NODE.TYPE, 1)
//                            .where(NODE.ID.eq(node.getId()))
//                            .execute();
//
//            }
        }

//		for(String ip:config.getSeedNodes()) {
//			TronFullNodeCli cli = new TronFullNodeCli(ip, true);
//			try {
//				for(Node node:cli.getAllNodes()){
//					nodesMap.put(node.getAddress().getHost().toStringUtf8()+":"+node.getAddress().getPort(), node);
//				}
//			}catch(Throwable e) {
//				logger.error("Could get nodes list from {}",ip,e);
//			}finally {
//				cli.shutdown();
//			}
//
//
//
//		}
//
//        List<Node> nodes = new ArrayList<>(nodesMap.values());
//        Collections.shuffle(nodes);
//
//        for (Node node : nodes) {
//
//            boolean isUp = pingHost(node.getAddress().getHost().toStringUtf8(), node.getAddress().getPort(), NODE_PING_TIMEOUT);
//
//            Timestamp lastUp = Timestamp.valueOf(LocalDateTime.now());
//
//            if (!isUp) {
//                lastUp = null;
//            }
//
//            CityResponse geoRes = getGeoData(node.getAddress().getHost().toStringUtf8());
//
//            this.dslContext.insertInto(NODE)
//                    .set(NODE.HOST, node.getAddress().getHost().toStringUtf8())
//                    .set(NODE.PORT, node.getAddress().getPort())
//                    .set(NODE.DATE_CREATED, Timestamp.valueOf(LocalDateTime.now()))
//                    .set(NODE.LAST_UPDATED, Timestamp.valueOf(LocalDateTime.now()))
//                    .set(NODE.COUNTRY, geoRes != null && geoRes.getCountry() != null ? geoRes.getCountry().getName() : null)
//                    .set(NODE.COUNTRY_CODE, geoRes != null && geoRes.getCountry() != null ? geoRes.getCountry().getIsoCode() : null)
//                    .set(NODE.CITY, geoRes != null && geoRes.getCity() != null ? geoRes.getCity().getName() : null)
//                    .set(NODE.LONGITUDE, geoRes != null && geoRes.getLocation() != null ? new BigDecimal(geoRes.getLocation().getLongitude()) : null)
//                    .set(NODE.LATITUDE, geoRes != null && geoRes.getLocation() != null ? new BigDecimal(geoRes.getLocation().getLatitude()) : null)
//                    .set(NODE.LAST_UP, lastUp)
//                    .set(NODE.UP, isUp ? (byte) 1 : (byte) 0)
//                    .onDuplicateKeyUpdate()
//                    .set(NODE.UP, isUp ? (byte) 1 : (byte) 0)
//                    .execute();
//
//
//        }


    }


    public void updateNodesStatus() {

        List<NodeRecord> dbNodes = this.dslContext.select(NODE.fields()).from(NODE).orderBy(NODE.LAST_UPDATED.asc()).limit(50).fetchInto(NodeRecord.class);


        for (NodeRecord dbNode : dbNodes) {

            boolean isUp = pingHost(dbNode.getHost(), dbNode.getPort(), NODE_PING_TIMEOUT);

            dbNode.setUp(isUp ? (byte) 1 : (byte) 0);

            if (isUp) {
                dbNode.setLastUp(Timestamp.valueOf(LocalDateTime.now()));
            }

            dbNode.setLastUpdated(Timestamp.valueOf(LocalDateTime.now()));

            this.dslContext.executeUpdate(dbNode);
        }

    }

    public void removeDownNodes() {
        Timestamp delay = Timestamp.valueOf(LocalDateTime.now().minusHours(1));

        this.dslContext.deleteFrom(NODE)
                .where(NODE.UP.eq((byte) 0)
                        .and(NODE.LAST_UP.lt(delay)))
                .execute();


        this.dslContext.deleteFrom(NODE)
                .where(NODE.UP.eq((byte) 0)
                        .and(NODE.LAST_UP.isNull())
                        .and(NODE.DATE_CREATED.lt(delay))
                )

                .execute();
    }


    public void updateNodesLocalizationData() throws UnirestException {

        List<NodeRecord> dbNodes = this.dslContext.select(NODE.fields()).from(NODE).where(NODE.COUNTRY.isNull()).limit(100).fetchInto(NodeRecord.class);

        List<Query> updates = new ArrayList<>();

        for (NodeRecord node : dbNodes) {

            CityResponse geoRes = getGeoData(node.getHost());

            if (geoRes != null) {

                updates.add(DSL.update(NODE)
                        .set(NODE.COUNTRY, geoRes.getCountry() != null ? geoRes.getCountry().getName() : null)
                        .set(NODE.COUNTRY_CODE, geoRes.getCountry() != null ? geoRes.getCountry().getIsoCode() : null)
                        .set(NODE.CITY, geoRes.getCity() != null ? geoRes.getCity().getName() : null)
                        .set(NODE.LONGITUDE, geoRes.getLocation() != null ? new BigDecimal(geoRes.getLocation().getLongitude()) : null)
                        .set(NODE.LATITUDE, geoRes.getLocation() != null ? new BigDecimal(geoRes.getLocation().getLatitude()) : null)
                        .where(NODE.ID.eq(node.getId())));

            }

        }

        this.dslContext.batch(updates).execute();

    }


    public static boolean pingHost(String host, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private CityResponse getGeoData(String ip) {

        try {
            String dbLocation = this.config.getGeoDbPath();
            File database = new File(dbLocation);
            DatabaseReader dbReader = new DatabaseReader.Builder(database).build();

            InetAddress ipAddress = InetAddress.getByName(ip);
            return dbReader.city(ipAddress);
        } catch (Exception e) {
            return null;
        }
    }


}
