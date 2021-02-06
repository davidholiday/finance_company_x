package io.holitek;


import java.beans.Introspector;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.camel.Exchange;

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


    // PUBLIC
    //

    /**
     * will attempt to set exchange rates given buildID and json payload. on fault, bean will reset itself to prevent
     * risk of reporting corrupted or malformed data.
     *
     * @param buildID
     * @return
     */
    public boolean setExchangeRates(String buildID, String exchangeRateJson) {
        boolean setBuildIdSuccessFlag = setBuildID(buildID);
        boolean setExchangeRateMapSuccessFlag = setExchangeRateMap(exchangeRateJson);
        boolean successFlag = setBuildIdSuccessFlag && setExchangeRateMapSuccessFlag;
        if (successFlag == false) {
            LOG.error("something went wrong setting exchange rates. " +
                      "setBuildIdSuccessFlag was: {}  setExchangeRateMapSuccessFlag was: {}",
                    setBuildIdSuccessFlag,
                    setExchangeRateMapSuccessFlag
            );

            LOG.info("bean may be corrupted - resetting ...");
            resetBean();
        }

        return successFlag;
    }

    /**
     * resets all bean variables to their default values
     */
    public void resetBean() {
        buildID = ""; // purposefully not using the setter...
        exchangeRateMap.clear();
    }

    /**
     *
     * @return
     */
    public String getBuildID() { return this.buildID; }

    /**
     *
     * @return
     */
    public Map<String, Double> getExchangeRateMap() {
        Map<String, Double> exchangeRateMapClone = new HashMap<>();
        exchangeRateMapClone.putAll(this.exchangeRateMap);
        return exchangeRateMapClone;
    }

    /**
     * default message handler. sets message header to current buildID. sets message body to Json map of
     * exchange rates.
     *
     * @param exchange
     */
    public void getExchangeRatesAsJson(Exchange exchange) {
        String buildID = getBuildID();
        String exchangeRatesAsJson = getExchangeRatesAsJson();

        assert (buildID.equals("") && exchangeRatesAsJson.equals("{}") == false) == false;

        exchange.getIn().setHeader(BUILD_ID_HEADER_KEY, buildID);

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


    // PACKAGE PROTECTED
    //

    /**
     * setter for buildID will prevent caller from setting empty value. the reason is it's possible to have a buildID
     * that points to an empty data set but it shouldn't be possible for there to be a populated data set that doesn't
     * have a unique buildID. the data came *from* somewhere and absent that context, it's meaningless.
     *
     * @param buildID
     */
    boolean setBuildID(String buildID) {
        boolean successFlag = true;
        if (buildID == null || buildID.isEmpty()) {
            successFlag = false;
        } else {
            this.buildID = buildID;
        }

        return successFlag;
    }

    /**
     * replaces whatever exchange rates are currently stored with rates provided by caller.
     *
     * @param exchangeRateJson
     * @return boolean indicating whether or not import was successful
     */
    boolean setExchangeRateMap(String exchangeRateJson) {
        boolean successFlag = true;

        try {
            Map<String, Double> result = objectMapper.readValue(
                        exchangeRateJson,
                        new TypeReference<Map<String, Double>>() {}
                    );

            exchangeRateMap = result;
        } catch (JsonProcessingException e) {
            LOG.error("something went wrong importing exchangeRateJson", e);
            successFlag = false;
        }

        return successFlag;
    }

}
