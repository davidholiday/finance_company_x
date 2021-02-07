package io.holitek.finance_company_x;


import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;


/**
 * business logic that polls a given directory for exchange rate files and, on delta, updates exchange rate data
 * stored in memory
 */
public class CurrencyDataPollingConsumerRoute extends RouteBuilder {

    // property placeholders that will be resolved by camel when the route is created
    private static final String POLLING_CONSUMER = "{{polling_consumer_uri_template}}" + "{{route_from_period}}";
    private static final String DATA_DIRECTORY = "{{data_directory}}";
    private static final String BUILD_ID_FILE_PROCESSOR = "{{buildID_file_processor}}";
    private static final String DATA_FILE_PROCESSOR = "{{datafile_processor}}";
    private static final String EXCHANGE_RATE_BEAN = "{{exchange_rate_bean}}";

    // keys for data put into the message header
    public static final String DATA_DIRECTORY_HEADER_KEY = "dataDirectory";
    public static final String NEW_BUILD_ID_HEADER_KEY = "newBuildID";

    @Override
    public void configure() throws Exception {

        from(POLLING_CONSUMER)

            // attempt to load buildID file
            .setHeader(DATA_DIRECTORY_HEADER_KEY, simple(DATA_DIRECTORY))
            .to(BUILD_ID_FILE_PROCESSOR)

             // eject if buildIdFileProcessor returns empty json
             .choice()
                 .when(body().isEqualTo("{}"))
                    .stop()
             .end()

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
                    .to(DATA_FILE_PROCESSOR)
                .endChoice()
            .otherwise()
                .log(LoggingLevel.INFO, "taking no action, build IDs are same")
            .end();

    }
}
