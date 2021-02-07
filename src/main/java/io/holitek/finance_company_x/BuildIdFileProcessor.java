package io.holitek.finance_company_x;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
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

    public static final String NEW_BUILD_ID_HEADER_KEY = "newBuildID";


    /**
     *
     * @param exchange
     * @throws Exception
     */
    @Override
    public void process(Exchange exchange) throws Exception {

        // this to prevent the json parser from going boom when a key isn't found
        Configuration jsonPathConf = Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS);

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
            // this will go boom if the file does not contain valid json or if expected key doesn't exist

            Optional<String> buildIdOptional = Optional.ofNullable(
                    JsonPath.using(jsonPathConf).parse(buildIdJson).read( "$.buildID")
            );

            String buildID = buildIdOptional.isEmpty() ? ExchangeRateBean.DEFAULT_BUILD_ID : buildIdOptional.get();
            exchange.getMessage().setHeader(NEW_BUILD_ID_HEADER_KEY, buildID);
            exchange.getMessage().setHeader(BUILD_ID_FILE_CONTENTS_HEADER_KEY, buildIdJson);

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
