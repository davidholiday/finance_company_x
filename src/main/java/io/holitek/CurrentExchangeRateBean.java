package io.holitek;


import java.beans.Introspector;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * cache bean that stores and provides means to access current exchange rates
 */
public class CurrentExchangeRateBean {

    private static final Logger LOG = LoggerFactory.getLogger(CurrentExchangeRateBean.class);

    public static final String NAMESPACE_KEY = Introspector.decapitalize(CurrentExchangeRateBean.class.getSimpleName());
    public static final String BUILD_ID_HEADER_KEY = "buildID";

    private String buildID = new String();
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
     * clears all exchange rates from bean
     */
    public void clearExchangeRateMap() { exchangeRateMap.clear(); }


    /**
     * replaces whatever exchange rates are currently stored with rates provided by caller.
     *
     * @param exchangeRateJson
     * @return boolean indicating whether or not import was successful
     */
    public boolean setExchangeRateMap(String exchangeRateJson) {
        boolean faultFlag = false;

        try {
            Map<String, Double> result = objectMapper.readValue(
                        exchangeRateJson,
                        new TypeReference<Map<String, Double>>() {}
                    );

            exchangeRateMap = result;
        } catch (JsonProcessingException e) {
            LOG.error("something went wrong importing exchangeRateJson", e);
            faultFlag = true;
        }

        return faultFlag;
    }


    /**
     * default message handler. sets message header to current buildID. sets message body to Json map of
     * exchange rates.
     *
     * @param exchange
     */
    public void getExchangeRatesAsJson(Exchange exchange) {
        String buildID = getBuildID();
        exchange.getIn().setHeader(BUILD_ID_HEADER_KEY, buildID);

        String exchangeRatesAsJson = getExchangeRatesAsJson();
        exchange.getIn().setBody(exchangeRatesAsJson);
    }


    /**
     *
     * @return
     */
    public String getExchangeRatesAsJson() {
        String rv = new String();

        try {
            rv = objectMapper.writeValueAsString(exchangeRateMap);
        } catch (JsonProcessingException e) {
            LOG.error("something went wrong serializing cached exchange data into json", e);
        }

        return rv;
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
