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


public class BuildIdFileProcessorTest extends CamelTestSupport {

    private static final Logger LOG = LoggerFactory.getLogger(BuildIdFileProcessorTest.class);

    private File dataDirectory;
    private String buildIdFileContents;
    private String buildID;

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
                .bind(BuildIdFileProcessor.NAMESPACE_KEY, new BuildIdFileProcessor());

        // at runtime, the route the BuildFileProcessor is a part of will set the data directory in the exchange header.
        //   this will enable us to simulate that for testing
        String buildIdRelativeDirectory = context().getPropertiesComponent()
                                              .resolveProperty("buildID.directory")
                                              .orElse("");

        assert (buildIdRelativeDirectory.isEmpty() == false);
        Path buildIdPath = Path.of(System.getProperty("user.dir"), buildIdRelativeDirectory);
        this.dataDirectory = buildIdPath.toFile();

        //
        String buildIdContents = context().getPropertiesComponent()
                                          .resolveProperty("buildID.contents")
                                          .orElse("");

        assert (buildIdContents.isEmpty() == false);
        this.buildIdFileContents = buildIdContents;

        // this will go boom on parsing error
        String parsedBuildID = JsonPath.read(buildIdContents, "$.buildID");
        assert (parsedBuildID.isEmpty() == false);
        this.buildID = parsedBuildID;

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
                        .bean(BuildIdFileProcessor.NAMESPACE_KEY)
                        .to("mock:result");
            }
        };
    }


    //
    // tests

    @Test
    @DisplayName("checks that the processor properly parses a well formed buildID file")
    public void testExchangeHandlerHappyPath() throws Exception {

        //
        getMockEndpoint("mock:result").expectedHeaderReceived(
                BuildIdFileProcessor.NEW_BUILD_ID_HEADER_KEY,
                this.buildID
        );

        getMockEndpoint("mock:result").expectedHeaderReceived(
                BuildIdFileProcessor.BUILD_ID_FILE_CONTENTS_HEADER_KEY,
                this.buildIdFileContents
        );

        getMockEndpoint("mock:result").expectedBodiesReceived("");

        //
        setbuildIdFilenameHeader("buildID.file.good");

        template.sendBodyAndHeader(
                "direct:start",
                "",
                CurrencyDataPollingConsumerRoute.DATA_DIRECTORY_HEADER_KEY, this.dataDirectory.toString()
        );

        assertMockEndpointsSatisfied();
    }


    /**
     * populates a JVM system property with the build filename appropriate for a given test. the property key the
     * processor is looking for isn't set in the test properties file, so the JVM value will be used instead. doing this
     * because for some reason the folks who make camel decided mucking with properties directly at runtime is a 'no'.
     *
     * @param propertyName
     */
    private void setbuildIdFilenameHeader(String propertyName) {
        String buildIdFilename = context().getPropertiesComponent()
                                          .resolveProperty(propertyName)
                                          .orElse("");

        assert (buildIdFilename.isEmpty() == false);

        System.setProperty("buildID_filename", buildIdFilename);

    }

}







