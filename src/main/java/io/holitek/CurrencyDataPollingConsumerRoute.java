package io.holitek;

import org.apache.camel.builder.RouteBuilder;

public class CurrencyDataPollingConsumerRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("{{currency_data_polling_consumer_from}}")
            .bean("myBean", "hello")
            .log("${body}")
            .bean("myBean", "bye")
            .log("${body}");
    }
}
