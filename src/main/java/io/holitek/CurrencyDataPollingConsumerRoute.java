package io.holitek;

import org.apache.camel.builder.RouteBuilder;

public class CurrencyDataPollingConsumerRoute extends RouteBuilder {

    private static final String POLLING_CONSUMER =
            "{{route_from_template}}" + "{{polling_consumer_period}}";

    private static final String BUILDFILE_URI =
            "{{polling_consumer_URI_template}}" + "{{buildID_filename}}";


    @Override
    public void configure() throws Exception {
        from(POLLING_CONSUMER)
                //.to(BUILDFILE_URI)
                // grab current build ID
                // choice on current build ID and build ID in bean
                    // if buildID delta - update currentexchangeratebean-er-ino
            .log("${body}");
    }
}
