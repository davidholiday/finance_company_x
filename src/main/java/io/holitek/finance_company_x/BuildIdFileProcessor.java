package io.holitek.finance_company_x;


import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.Introspector;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Optional;


/**
 *
 */
public class BuildIdFileProcessor implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(BuildIdFileProcessor.class);

    public static final String NAMESPACE_KEY = Introspector.decapitalize(BuildIdFileProcessor.class.getSimpleName());

    @Override
    public void process(Exchange exchange) throws Exception {


        String directory = (String)exchange.getMessage()
                                           .getHeader(CurrencyDataPollingConsumerRoute.DATA_DIRECTORY_HEADER_KEY);



        Optional<String> buildFileNameOptional = exchange.getContext()
                                                         .getPropertiesComponent()
                                                         .resolveProperty("buildID_filename");

        if (buildFileNameOptional.isEmpty()) {
            throw new IllegalStateException("property buildID_filename must not be emtpy");
        }

        Path filePath = Paths.get(directory, buildFileNameOptional.get());

        if (Files.exists(filePath) == false) {
            throw new IllegalStateException("can not find buildID file " + filePath);
        }

    }
}
