package io.holitek.finance_company_x;


import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;


/**
 *
 */
public class CurrencyDataPollingConsumerRoute extends RouteBuilder {

    private static final String POLLING_CONSUMER = "{{polling_consumer_uri_template}}" + "{{route_from_period}}";
    private static final String DATA_DIRECTORY = "{{data_directory}}";
    private static final String BUILD_ID_FILE_PROCESSOR = "{{buildID_file_processor}}";
    private static final String EXCHANGE_RATE_BEAN = "{{exchange_rate_bean}}";

    public static final String DATA_DIRECTORY_HEADER_KEY = "dataDirectory";
    public static final String NEW_BUILD_ID_HEADER_KEY = "newBuildID";

    @Override
    public void configure() throws Exception {

        from(POLLING_CONSUMER)
            .setHeader(DATA_DIRECTORY_HEADER_KEY, simple(DATA_DIRECTORY))
            .to(BUILD_ID_FILE_PROCESSOR)
            // set buildID to either what's in the file or to an empty string (default for ExchangeRateBean)
            .choice()
                .when().jsonpath("$.buildID", true)
                    .setHeader(NEW_BUILD_ID_HEADER_KEY).jsonpath("$.buildID")
                .endChoice()
            .otherwise()
                .setHeader(NEW_BUILD_ID_HEADER_KEY, simple(ExchangeRateBean.DEFAULT_BUILD_ID))
            .end()
            .log(LoggingLevel.INFO, "new buildID is: ${headers." + NEW_BUILD_ID_HEADER_KEY + "}")
            // now grab the current exchange rate data from the container bean
            .to(EXCHANGE_RATE_BEAN)
            .log(LoggingLevel.INFO, "current buildID is: ${headers." + ExchangeRateBean.BUILD_ID_HEADER_KEY + "}")
            // compare new to current buildID, taking action only on delta
            .choice()
                .when(header(NEW_BUILD_ID_HEADER_KEY).isNotEqualTo(header(ExchangeRateBean.BUILD_ID_HEADER_KEY)))
                    .log("PUT CALL TO DATAFILEPROCESSOR HERE")
                    //TODO set bean values here
                .endChoice()
            .otherwise()
                .log(LoggingLevel.INFO, "taking no action, build IDs are same")
            .end()



        ;


        // check build ID against bean
        // if build id is different, goto datafileprocessor
        // else exit

    }
}
