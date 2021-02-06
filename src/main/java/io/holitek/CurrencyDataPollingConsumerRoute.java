package io.holitek;

import org.apache.camel.builder.RouteBuilder;

public class CurrencyDataPollingConsumerRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("{{currency_data_polling_consumer_from}}")
                // grab current build ID
                // choice on current build ID and build ID in bean
                    // if buildID delta - update currentexchangeratebean-er-ino
            .log("${body}");
    }
}
