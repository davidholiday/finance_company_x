package io.holitek.finance_company_x;


import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import com.jayway.jsonpath.Option;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.Introspector;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;


import static io.holitek.finance_company_x.Helpers.fileExists;

import static io.holitek.finance_company_x.BuildIdFileProcessor.BUILD_ID_FILE_CONTENTS_HEADER_KEY;


/**
 * handles parsing of data files into exchange header
 */
public class DataFileProcessor implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(DataFileProcessor.class);

    public static final String NAMESPACE_KEY = Introspector.decapitalize(DataFileProcessor.class.getSimpleName());

    public static final String DATA_FILE_CONTENTS_HEADER_KEY = "dataFileContents";

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

        String buildIdFileAsJson = (String)exchange.getMessage().getHeader(BUILD_ID_FILE_CONTENTS_HEADER_KEY);
LOG.info(directory);
        Optional<String> dataFileNameOptional = Optional.ofNullable(
                JsonPath.using(jsonPathConf).parse(buildIdFileAsJson).read("$.FileName")
        );

        // update exchange rates iff data file exists and is valid json
        // TODO add more validation of dataFileContentsJson as needed...
        if (dataFileNameOptional.isEmpty() || fileExists(directory, dataFileNameOptional.get()) == false) {
            LOG.error("data file can't can't be parsed from buildID file contents");
        } else {
            Path dataFilePath = Paths.get(directory, dataFileNameOptional.get());
            String dataFileContentsJson = new String(Files.readAllBytes(dataFilePath));
            exchange.getMessage().setHeader(DATA_FILE_CONTENTS_HEADER_KEY, dataFileContentsJson);
        }

    }

}
