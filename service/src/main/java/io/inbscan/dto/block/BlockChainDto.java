package io.inbscan.dto.block;

public class BlockChainDto {

    private Long   id;
    private Long   latestBlockNum;
    private Long   transactionNum;
    private Long   addressNum;
    private Long   irreversibleBlockNum;
    private Integer currentTps;
    private Integer highestTps;
    private Double  inbTotalSupply;
    private Double  votedInb;
    private Double  mortgageNetInb;
    private Integer totalNet;
    private Double  mortgageCpuInb;
    private Integer totalCpu;
    private String  currentProducer;
    private String  nextProducer;
    private Integer currentNetConsumed;
    private Integer netLimit;
    private Integer currentCpuConsumed;
    private Integer cpuLimit;
    private Double  inbCurrentPrice;
    private Double  inbSupply;
    private Integer dappNum;
    private Long   transactionNumLastDay;
    private Long   activeAddressNumLastDay;
    private Long   newAddressNumLastDay;
    private Integer superNodeNum;
    private Integer nodeNum;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLatestBlockNum() {
        return latestBlockNum;
    }

    public void setLatestBlockNum(Long latestBlockNum) {
        this.latestBlockNum = latestBlockNum;
    }

    public Long getTransactionNum() {
        return transactionNum;
    }

    public void setTransactionNum(Long transactionNum) {
        this.transactionNum = transactionNum;
    }

    public Long getAddressNum() {
        return addressNum;
    }

    public void setAddressNum(Long addressNum) {
        this.addressNum = addressNum;
    }

    public Long getIrreversibleBlockNum() {
        return irreversibleBlockNum;
    }

    public void setIrreversibleBlockNum(Long irreversibleBlockNum) {
        this.irreversibleBlockNum = irreversibleBlockNum;
    }

    public Integer getCurrentTps() {
        return currentTps;
    }

    public void setCurrentTps(Integer currentTps) {
        this.currentTps = currentTps;
    }

    public Integer getHighestTps() {
        return highestTps;
    }

    public void setHighestTps(Integer highestTps) {
        this.highestTps = highestTps;
    }

    public Double getInbTotalSupply() {
        return inbTotalSupply;
    }

    public void setInbTotalSupply(Double inbTotalSupply) {
        this.inbTotalSupply = inbTotalSupply;
    }

    public Double getVotedInb() {
        return votedInb;
    }

    public void setVotedInb(Double votedInb) {
        this.votedInb = votedInb;
    }

    public Double getMortgageNetInb() {
        return mortgageNetInb;
    }

    public void setMortgageNetInb(Double mortgageNetInb) {
        this.mortgageNetInb = mortgageNetInb;
    }

    public Integer getTotalNet() {
        return totalNet;
    }

    public void setTotalNet(Integer totalNet) {
        this.totalNet = totalNet;
    }

    public Double getMortgageCpuInb() {
        return mortgageCpuInb;
    }

    public void setMortgageCpuInb(Double mortgageCpuInb) {
        this.mortgageCpuInb = mortgageCpuInb;
    }

    public Integer getTotalCpu() {
        return totalCpu;
    }

    public void setTotalCpu(Integer totalCpu) {
        this.totalCpu = totalCpu;
    }

    public String getCurrentProducer() {
        return currentProducer;
    }

    public void setCurrentProducer(String currentProducer) {
        this.currentProducer = currentProducer;
    }

    public String getNextProducer() {
        return nextProducer;
    }

    public void setNextProducer(String nextProducer) {
        this.nextProducer = nextProducer;
    }

    public Integer getCurrentNetConsumed() {
        return currentNetConsumed;
    }

    public void setCurrentNetConsumed(Integer currentNetConsumed) {
        this.currentNetConsumed = currentNetConsumed;
    }

    public Integer getNetLimit() {
        return netLimit;
    }

    public void setNetLimit(Integer netLimit) {
        this.netLimit = netLimit;
    }

    public Integer getCurrentCpuConsumed() {
        return currentCpuConsumed;
    }

    public void setCurrentCpuConsumed(Integer currentCpuConsumed) {
        this.currentCpuConsumed = currentCpuConsumed;
    }

    public Integer getCpuLimit() {
        return cpuLimit;
    }

    public void setCpuLimit(Integer cpuLimit) {
        this.cpuLimit = cpuLimit;
    }

    public Double getInbCurrentPrice() {
        return inbCurrentPrice;
    }

    public void setInbCurrentPrice(Double inbCurrentPrice) {
        this.inbCurrentPrice = inbCurrentPrice;
    }

    public Double getInbSupply() {
        return inbSupply;
    }

    public void setInbSupply(Double inbSupply) {
        this.inbSupply = inbSupply;
    }

    public Integer getDappNum() {
        return dappNum;
    }

    public void setDappNum(Integer dappNum) {
        this.dappNum = dappNum;
    }

    public Long getTransactionNumLastDay() {
        return transactionNumLastDay;
    }

    public void setTransactionNumLastDay(Long transactionNumLastDay) {
        this.transactionNumLastDay = transactionNumLastDay;
    }

    public Long getActiveAddressNumLastDay() {
        return activeAddressNumLastDay;
    }

    public void setActiveAddressNumLastDay(Long activeAddressNumLastDay) {
        this.activeAddressNumLastDay = activeAddressNumLastDay;
    }

    public Long getNewAddressNumLastDay() {
        return newAddressNumLastDay;
    }

    public void setNewAddressNumLastDay(Long newAddressNumLastDay) {
        this.newAddressNumLastDay = newAddressNumLastDay;
    }

    public Integer getSuperNodeNum() {
        return superNodeNum;
    }

    public void setSuperNodeNum(Integer superNodeNum) {
        this.superNodeNum = superNodeNum;
    }

    public Integer getNodeNum() {
        return nodeNum;
    }

    public void setNodeNum(Integer nodeNum) {
        this.nodeNum = nodeNum;
    }
}
