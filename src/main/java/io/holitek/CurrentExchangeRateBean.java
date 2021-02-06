package io.holitek;

import java.util.HashMap;
import java.util.Map;

public class CurrentExchangeRateBean {

    private String buildID = "";
    private Map<String, Double> exchangeRateMap = new HashMap<>();

    public void setBuildID(String buildID) { this.buildID = buildID; }
    public String getBuildID() { return this.buildID; }

    public void setExchangeRateMap(String exchangeRateJson) {
        // do stuff
    }

    public void getExchangeRatesAsJson() {
        // do stuff
    }

    public void getExchangeRateFor(String key) {
        // do stuff
    }

    public void clearExchangeRateMap() { exchangeRateMap.clear(); }



}
