package io.holitek.finance_company_x;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.Introspector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Optional;


/**
 * if present - will load the json contents of the buildID file into the message body. will except if build file
 * property is not specified. if specified file is not found, message body will be returned with empty json string
 */
public class BuildIdFileProcessor implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(BuildIdFileProcessor.class);

    public static final String NAMESPACE_KEY = Introspector.decapitalize(BuildIdFileProcessor.class.getSimpleName());

    public static final String BUILD_ID_FILE_CONTENTS_HEADER_KEY = "buildIdJson";


    /**
     *
     * @param exchange
     * @throws Exception
     */
    @Override
    public void process(Exchange exchange) throws Exception {
        // populate exchange body with default value of empty json string to keep the code DRY
        exchange.getMessage().setBody("{}");

        String directory = (String)exchange.getMessage()
                                           .getHeader(CurrencyDataPollingConsumerRoute.DATA_DIRECTORY_HEADER_KEY);

        Optional<String> buildFileNameOptional = exchange.getContext()
                                                         .getPropertiesComponent()
                                                         .resolveProperty("buildID_filename");

        // update message body iff buildID can be found and is valid JSON
        // TODO add more validation of buildID contents as needed...
        if (buildFileNameOptional.isEmpty() || fileExists(directory, buildFileNameOptional.get()) == false) {
            LOG.error("buildID_filename property can't be resolved");
        } else {
            Path filePath = Paths.get(directory, buildFileNameOptional.get());
            String buildIdJson = new String(Files.readAllBytes(filePath));
            try {
                new ObjectMapper().readTree(buildIdJson);
                exchange.getMessage().setBody(buildIdJson);
            } catch (IOException e) {
                LOG.error("buildID file contents are not valid JSON");
            }
        }
    }

    /**
     * helper to keep the code clean
     *
     * @param directory
     * @param filename
     * @return
     */
    private boolean fileExists(String directory, String filename) {
        Path path = Paths.get(directory, filename);
        return Files.exists(path);
    }

}
