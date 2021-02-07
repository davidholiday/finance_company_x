package io.holitek.finance_company_x;


import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

import static io.holitek.finance_company_x.BuildIdFileProcessor.NEW_BUILD_ID_HEADER_KEY;
import static io.holitek.finance_company_x.ExchangeRateBean.CURRENT_BUILD_ID_HEADER_KEY;
import static io.holitek.finance_company_x.DataFileProcessor.DATA_FILE_CONTENTS_HEADER_KEY;


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

    @Override
    public void configure() throws Exception {

        from(POLLING_CONSUMER)
            // attempt to load buildID file
            .setHeader(DATA_DIRECTORY_HEADER_KEY, simple(DATA_DIRECTORY))
            .to(BUILD_ID_FILE_PROCESSOR)
            // grab the current exchange rate data from the container bean
            .to(EXCHANGE_RATE_BEAN)
            .log(LoggingLevel.INFO, "exchange headers are ${headers}")
            // compare new to current buildID, taking action only on delta
            .choice()
                .when(header(NEW_BUILD_ID_HEADER_KEY).isNotEqualTo(header(CURRENT_BUILD_ID_HEADER_KEY)))
                    .to(DATA_FILE_PROCESSOR)
                    .to(EXCHANGE_RATE_BEAN +
                            "?method=setExchangeRates(" +
                                "${headers." + NEW_BUILD_ID_HEADER_KEY + "}," +
                                "${headers." + DATA_FILE_CONTENTS_HEADER_KEY + "}" +
                            ")"
                    )
                .endChoice()
            .otherwise()
                .log(LoggingLevel.INFO, "taking no action, build IDs are same")
            .end();
    }
}
