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
 * if present - will load the json contents of the buildID file into the message header
 */
public class BuildIdFileProcessor implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(BuildIdFileProcessor.class);

    public static final String NAMESPACE_KEY = Introspector.decapitalize(BuildIdFileProcessor.class.getSimpleName());

    public static final String BUILD_ID_FILE_CONTENTS_HEADER_KEY = "buildIdJson";


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
        } else {
            String buildIdJson = new String(Files.readAllBytes(filePath));
            exchange.getMessage().setHeader(
                    BUILD_ID_FILE_CONTENTS_HEADER_KEY,
                    buildIdJson
            );
        }
    }
}