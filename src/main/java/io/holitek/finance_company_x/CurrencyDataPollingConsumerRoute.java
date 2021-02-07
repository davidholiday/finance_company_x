package io.holitek.finance_company_x;


import org.apache.camel.builder.RouteBuilder;


public class CurrencyDataPollingConsumerRoute extends RouteBuilder {

    private static final String POLLING_CONSUMER = "{{polling_consumer_uri_template}}" + "{{route_from_period}}";
    private static final String DATA_DIRECTORY = "{{data_directory}}";
    private static final String BUILD_ID_FILE_PROCESSOR = "{{buildIdFileProcessor}}";

    public static final String DATA_DIRECTORY_HEADER_KEY = "dataDirectory";


    @Override
    public void configure() throws Exception {

        from(POLLING_CONSUMER)
            .setHeader(DATA_DIRECTORY_HEADER_KEY, simple(DATA_DIRECTORY))
            .to(BUILD_ID_FILE_PROCESSOR)
            .log("${body}");
    }
}
