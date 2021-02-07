package io.holitek.finance_company_x;


import com.jayway.jsonpath.JsonPath;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;

import org.junit.jupiter.api.*;

import java.nio.file.Path;


public class CurrencyDataPollingConsumerRouteTest extends CamelTestSupport {

    private String buildIdFileContents;
    private String buildID;
    private String dataFileContents;

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
                .bind(ExchangeRateBean.NAMESPACE_KEY, new ExchangeRateBean());

        // this will allow us to dynamically resolve a target directory for the route to find build and data files
        String routeTestDataRelativeDirectory = context().getPropertiesComponent()
                                                         .resolveProperty("route_data_directory")
                                                         .orElse("");

        assert (routeTestDataRelativeDirectory.isEmpty() == false);
        Path routeTestDataPath = Path.of(System.getProperty("user.dir"), routeTestDataRelativeDirectory);
        // camel will fall back to system and env vars if one hasn't been defined via properties file(s)
        System.setProperty("data_directory", routeTestDataPath.toString());


        // for validation during tests...
        String buildIdContents = context().getPropertiesComponent()
                .resolveProperty("buildID.contents")
                .orElse("");

        assert (buildIdContents.isEmpty() == false);
        this.buildIdFileContents = buildIdContents;

        // this will go boom on parsing error
        String parsedBuildID = JsonPath.read(buildIdContents, "$.buildID");
        assert (parsedBuildID.isEmpty() == false);
        this.buildID = parsedBuildID;


        // DATAFILE CONTENTS
        String dataFileContents = context().getPropertiesComponent()
                .resolveProperty("dataFile.contents")
                .orElse("");

        assert (dataFileContents.isEmpty() == false);
        this.dataFileContents = dataFileContents;



        // simbora!
        context().start();
    }

    @AfterEach
    void afterEach() { context().stop(); }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new CurrencyDataPollingConsumerRoute();
    }


    //
    // tests

    @Test
    @DisplayName("checks that the route properly updates the exchange rate bean")
    public void testCurrencyDataPollingConsumerHappyPath() throws Exception {

        // grab bean for before/after inspection
        ExchangeRateBean exchangeRateBean = template.getCamelContext()
                                                    .getRegistry()
                                                    .lookupByNameAndType(
                                                        ExchangeRateBean.NAMESPACE_KEY,
                                                        ExchangeRateBean.class
                                                    );

        // bean should be unpopulated
        String expectedBuildID = ExchangeRateBean.DEFAULT_BUILD_ID;
        String actualBuildID = exchangeRateBean.getBuildID();
        Assertions.assertEquals(expectedBuildID, actualBuildID, "bean should report default buildID");

        String expectedExchangeRates = "{}";
        String actualExchangeRates = exchangeRateBean.getExchangeRatesAsJson();
        Assertions.assertEquals(
                expectedExchangeRates,
                actualExchangeRates,
                "bean should report default (empty) exchange rate json"
        );


        // kick off route
        template.sendBody("direct:start", "");


        // currency exchange bean should now be populated
        expectedBuildID = buildID;
        actualBuildID = exchangeRateBean.getBuildID();
        Assertions.assertEquals(expectedBuildID, actualBuildID, "bean should report updated buildID");

        expectedExchangeRates = dataFileContents;
        actualExchangeRates = exchangeRateBean.getExchangeRatesAsJson();
        Assertions.assertEquals(
                expectedExchangeRates,
                actualExchangeRates,
                "bean should report updated exchange rates"
        );


        // send another message
        template.sendBody("direct:start", "");


        // currency exchange bean state should not have changed
        expectedBuildID = buildID;
        actualBuildID = exchangeRateBean.getBuildID();
        Assertions.assertEquals(
                expectedBuildID,
                actualBuildID,
                "buildID value should not have changed upon second message with same data in data directory"
        );

        expectedExchangeRates = dataFileContents;
        actualExchangeRates = exchangeRateBean.getExchangeRatesAsJson();
        Assertions.assertEquals(
                expectedExchangeRates,
                actualExchangeRates,
                "exchange rate json value should not have changed upon second message with same data in data directory"
        );

    }

}
