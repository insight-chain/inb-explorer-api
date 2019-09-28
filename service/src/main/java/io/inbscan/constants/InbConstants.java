package io.inbscan.constants;

import com.google.inject.Inject;
import com.typesafe.config.Config;

import java.util.List;

public class InbConstants {

    private Config config;

    @Inject
    public InbConstants(Config config){
        this.config=config;
    }

//    public final static String URL = "http://127.0.0.1:8080";
//    public final static String URL = "http://192.168.1.183:6001";
    public final static String URL = "http://192.168.1.184:6001";
//    public final static String URL = "http://192.168.1.182:6002";

    //xiang
//    public final static String URL = "http://192.168.1.85:8545";

    //yu
//    public final static String URL = "http://192.168.1.118:6001";

    public final static String INBTOTALSUPPLY  = "0x9510000000000000000000000000000000000000";
    public final static String MORTGAGENETINB  = "0x9530000000000000000000000000000000000000";

//    public final static String MORTGAGENETINB  = "0x953000000000000000000000000000000000000000";

    public List<String> getNodes(){
        return this.config.getStringList("inb.fullnode");
    }
}
