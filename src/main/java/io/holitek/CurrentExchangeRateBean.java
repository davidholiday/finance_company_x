package io.holitek;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
public class CurrentExchangeRateBean {

    private static final Logger LOG = LoggerFactory.getLogger(CurrentExchangeRateBean.class);

    private String buildID = "";
    private Map<String, Double> exchangeRateMap = new HashMap<>();
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     *
     * @param buildID
     */
    public void setBuildID(String buildID) { this.buildID = buildID; }

    /**
     *
     * @return
     */
    public String getBuildID() { return this.buildID; }

    /**
     *
     */
    public void clearExchangeRateMap() { exchangeRateMap.clear(); }

    /**
     *
     * @param exchangeRateJson
     */
    public boolean setExchangeRateMap(String exchangeRateJson) {
        boolean faultFlag = false;

        try {
            Map<String, Double> result = objectMapper.readValue(
                        exchangeRateJson,
                        new TypeReference<Map<String, Double>>() {}
                    );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            faultFlag = true;
        }

        return faultFlag;
    }

    /**
     *
     * @return
     */
    public Optional<String> getExchangeRatesAsJson() {
        Optional<String> returnOptional;

        try {
            String exchangeRateJson = objectMapper.writeValueAsString(exchangeRateMap);
            returnOptional = Optional.of(exchangeRateJson);
        } catch (JsonProcessingException e) {
            returnOptional = Optional.empty();
            LOG.error("something went wrong serializing cached exchange data into json", e);
        }

        return returnOptional;
    }

    /**
     *
     * @param key
     * @return
     */
    public Optional<Double> getExchangeRateFor(String key) {
        Optional<Double> returnOptional;

        if (exchangeRateMap.containsKey(key)) {
            Double exchangeRate = exchangeRateMap.get(key);
            returnOptional = Optional.of(exchangeRate);
        } else {
            returnOptional = Optional.empty();
        }

        return returnOptional;
    }

}
