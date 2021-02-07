package io.holitek.finance_company_x;


import com.jayway.jsonpath.JsonPath;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;


public class DataFileProcessorTest extends CamelTestSupport {

    private static final Logger LOG = LoggerFactory.getLogger(BuildIdFileProcessorTest.class);

    private File dataDirectory;
    private String dataFileContents;
    private String buildIdFileContents;
    private String buildIdContentsNoFilename;
    private String buildIdContentsBadFilename;


    //
    // test setup and configuration

    // tells the test runner that we'll start and stop the camel context manually. this ensures the camel context
    // doesn't start before we've set up the camel registry and routes.
    @Override
    public boolean isUseAdviceWith() { return true; }

    @BeforeEach
    void beforeEach() {
        context().getPropertiesComponent()
                 .setLocation("classpath:application.test.properties");

        context().getRegistry()
                 .bind(DataFileProcessor.NAMESPACE_KEY, new DataFileProcessor());

        // at runtime, the route the BuildFileProcessor is a part of will set the data directory in the exchange header.
        //   this will enable us to simulate that for testing
        String buildIdRelativeDirectory = context().getPropertiesComponent()
                                                   .resolveProperty("dataFile.directory")
                                                   .orElse("");

        assert (buildIdRelativeDirectory.isEmpty() == false);
        Path buildIdPath = Path.of(System.getProperty("user.dir"), buildIdRelativeDirectory);
        this.dataDirectory = buildIdPath.toFile();


        // DATAFILE CONTENTS
        String dataFileContents = context().getPropertiesComponent()
                                           .resolveProperty("dataFile.contents")
                                           .orElse("");

        assert (dataFileContents.isEmpty() == false);
        this.dataFileContents = dataFileContents;


        // BUILD_ID CONTENTS
        String buildIdContents = context().getPropertiesComponent()
                                          .resolveProperty("buildID.contents")
                                          .orElse("");

        assert (buildIdContents.isEmpty() == false);
        this.buildIdFileContents = buildIdContents;


        // BUILD_ID CONTENTS NO FILENAME
        String buildIdContentsNoFilename = context().getPropertiesComponent()
                                                    .resolveProperty("buildID.contents.no-filename")
                                                    .orElse("");

        assert (buildIdContentsNoFilename.isEmpty() == false);
        this.buildIdContentsNoFilename = buildIdContentsNoFilename;


        // BUILD_ID CONTENTS BAD FILENAME
        String buildIdContentsBadFilename = context().getPropertiesComponent()
                                                     .resolveProperty("buildID.contents.bad-filename")
                                                     .orElse("");

        assert (buildIdContentsBadFilename.isEmpty() == false);
        this.buildIdContentsBadFilename = buildIdContentsBadFilename;


        // simbora!
        context().start();
    }

    @AfterEach
    void afterEach() { context().stop(); }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:start")
                        .bean(DataFileProcessor.NAMESPACE_KEY)
                        .to("mock:result");
            }
        };
    }


    //
    // tests

    @Test
    @DisplayName("checks that the processor properly parses a well formed buildID file contents ")
    public void testDataFileProcessorHappyPath() throws Exception {

        // set expectations of output
        getMockEndpoint("mock:result").expectedHeaderReceived(
                DataFileProcessor.DATA_FILE_CONTENTS_HEADER_KEY,
                this.dataFileContents
        );

        getMockEndpoint("mock:result").expectedBodiesReceived("");


        Map<String, Object> exchangeHeaders = Map.of(
                BuildIdFileProcessor.BUILD_ID_FILE_CONTENTS_HEADER_KEY, this.buildIdFileContents,
                CurrencyDataPollingConsumerRoute.DATA_DIRECTORY_HEADER_KEY, this.dataDirectory.toString()
        );

        // do the thing
        template.sendBodyAndHeaders(
                "direct:start",
                "",
                exchangeHeaders
        );

        assertMockEndpointsSatisfied();
    }

    @Test
    @DisplayName("checks that the processor takes no action if the filename can't be parsed from buildID file contents")
    public void testDataFileProcessorNoFilenameInBuildIdFile() throws Exception {

        // set expectations of output
        getMockEndpoint("mock:result").expectedHeaderReceived(
                DataFileProcessor.DATA_FILE_CONTENTS_HEADER_KEY,
                this.dataFileContents
        );


        Map<String, Object> exchangeHeaders = Map.of(
                BuildIdFileProcessor.BUILD_ID_FILE_CONTENTS_HEADER_KEY, this.buildIdContentsNoFilename,
                CurrencyDataPollingConsumerRoute.DATA_DIRECTORY_HEADER_KEY, this.dataDirectory.toString()
        );

        // do the thing
        template.sendBodyAndHeaders(
                "direct:start",
                "",
                exchangeHeaders
        );

        getMockEndpoint("mock:result").assertIsNotSatisfied();
    }

    @Test
    @DisplayName("checks that the processor takes no action if the file from buildID file can't be found")
    public void testDataFileProcessorBadFilenameInBuildIdFile() throws Exception {

        // set expectations of output
        getMockEndpoint("mock:result").expectedHeaderReceived(
                DataFileProcessor.DATA_FILE_CONTENTS_HEADER_KEY,
                this.dataFileContents
        );


        Map<String, Object> exchangeHeaders = Map.of(
                BuildIdFileProcessor.BUILD_ID_FILE_CONTENTS_HEADER_KEY, this.buildIdContentsBadFilename,
                CurrencyDataPollingConsumerRoute.DATA_DIRECTORY_HEADER_KEY, this.dataDirectory.toString()
        );

        // do the thing
        template.sendBodyAndHeaders(
                "direct:start",
                "",
                exchangeHeaders
        );

        getMockEndpoint("mock:result").assertIsNotSatisfied();
    }

}









