package io.holitek.finance_company_x;


import com.jayway.jsonpath.JsonPath;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.Introspector;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;


public class DataFileProcessor implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(DataFileProcessor.class);

    public static final String NAMESPACE_KEY = Introspector.decapitalize(DataFileProcessor.class.getSimpleName());


    @Override
    public void process(Exchange exchange) throws Exception {

        String directory = (String)exchange.getMessage()
                                           .getHeader(CurrencyDataPollingConsumerRoute.DATA_DIRECTORY_HEADER_KEY);

        String buildIdFileAsJson = (String)exchange.getMessage().getBody();
        LOG.info("buildIdFileAsJson is: {}", buildIdFileAsJson);
        // this will throw exception if key not found
        String dataFileName = JsonPath.read(buildIdFileAsJson, "$.Filename");

        Path dataFilePath = Paths.get(directory, dataFileName);
        if (Files.exists(dataFilePath) == false) {
            LOG.warn("could not find dataFilePath file {}. no action taken", dataFilePath);
        } else {
            String buildIdJson = new String(Files.readAllBytes(dataFilePath));
            exchange.getMessage().setBody(buildIdJson);
        }


    }

}


//        if (buildFileNameOptional.isEmpty() || fileExists(directory, buildFileNameOptional.get()) == false) {
//                LOG.error("buildID_filename property can't be resolved");
//                } else {
//                Path filePath = Paths.get(directory, buildFileNameOptional.get());
//                String buildIdJson = new String(Files.readAllBytes(filePath));
//                try {
//                new ObjectMapper().readTree(buildIdJson);
//                exchange.getMessage().setBody(buildIdJson);
//                } catch (IOException e) {
//                LOG.error("buildID file contents are not valid JSON");
//                }
//                }