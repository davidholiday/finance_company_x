package io.holitek.finance_company_x;


import com.jayway.jsonpath.JsonPath;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;

import org.junit.jupiter.api.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class ExchangeRateBeanTest extends CamelTestSupport {
    private static final Logger LOG = LoggerFactory.getLogger(ExchangeRateBeanTest.class);

    private String buildID;
    private String buildIdContents;
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


        this.buildIdContents = context().getPropertiesComponent()
                                        .resolveProperty("buildID.contents")
                                        .orElse("");

        this.buildID = JsonPath.read(this.buildIdContents, "$.buildID");

        this.dataFileContents = context().getPropertiesComponent()
                                         .resolveProperty("dataFile.contents")
                                         .orElse("");


        context().getRegistry()
                 .bind(ExchangeRateBean.NAMESPACE_KEY, new ExchangeRateBean());

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
                        .bean(ExchangeRateBean.NAMESPACE_KEY)
                        .to("mock:result");
            }
        };
    }


    //
    // tests

    @Test
    @DisplayName("checks that bean in default state properly handles exchange messages ")
    public void testExchangeHandlerHappyPath() throws Exception {
        getMockEndpoint("mock:result").expectedHeaderReceived(ExchangeRateBean.BUILD_ID_HEADER_KEY, "");
        getMockEndpoint("mock:result").expectedBodiesReceived("{}");
        template.sendBody("direct:start", "");
        assertMockEndpointsSatisfied();
    }

    @Test
    @DisplayName("checks happy path behavior of buildID setter")
    public void testSetBuildIdHappyPath() {
        ExchangeRateBean exchangeRateBean = (
                ExchangeRateBean) context().getRegistry().lookupByName(ExchangeRateBean.NAMESPACE_KEY);

        boolean successFlag = exchangeRateBean.setBuildID(this.buildID);
        Assertions.assertTrue(successFlag);

        String actualBuildID = exchangeRateBean.getBuildID();
        Assertions.assertEquals(this.buildID, actualBuildID, "we should get back what we put in!");
    }

    @Test
    @DisplayName("checks buildID setter prevents empty String from being set ")
    public void testSetBuildIdErrorPathEmptyString() {

        // setup
        //
        ExchangeRateBean exchangeRateBean = (
                ExchangeRateBean) context().getRegistry().lookupByName(ExchangeRateBean.NAMESPACE_KEY);

        // this way (vs junit way) because this is to validate programmer assumption that expected condition exists.
        assert exchangeRateBean.setBuildID(this.buildID);


        // test
        //
        boolean successFlag = exchangeRateBean.setBuildID("");
        Assertions.assertFalse(successFlag);

        String actualBuildID = exchangeRateBean.getBuildID();
        Assertions.assertEquals(
                this.buildID,
                actualBuildID,
                "buildID value should not have changed on setBuildID fault"
        );
    }

    @Test
    @DisplayName("checks buildID setter prevents null String from being set ")
    public void testSetBuildIdErrorPathNullString() {

        // setup
        //
        ExchangeRateBean exchangeRateBean = (
                ExchangeRateBean) context().getRegistry().lookupByName(ExchangeRateBean.NAMESPACE_KEY);

        assert exchangeRateBean.setBuildID(this.buildID);


        // test
        //
        boolean successFlag = exchangeRateBean.setBuildID(null);
        Assertions.assertFalse(successFlag);

        String actualBuildID = exchangeRateBean.getBuildID();
        Assertions.assertEquals(
                this.buildID,
                actualBuildID,
                "buildID value should not have changed on setBuildID fault"
        );
    }

    @Test
    @DisplayName("checks default behavior for getExchangeRateMap ")
    public void testGetExchangeRateMapDefault() {
        ExchangeRateBean exchangeRateBean = (
                ExchangeRateBean) context().getRegistry().lookupByName(ExchangeRateBean.NAMESPACE_KEY);

        Map<String, Double> expectedResults = new HashMap<>();
        Map<String, Double> actualResults = exchangeRateBean.getExchangeRateMap();
        Assertions.assertEquals(
                expectedResults,
                actualResults,
                "expected empty map from newly created CurrentExchangeRateBean"
        );
    }

    @Test
    @DisplayName("checks default behavior for getExchangeRateJson ")
    public void testGetExchangeRateAsJsonDefault() {
        ExchangeRateBean exchangeRateBean = (
                ExchangeRateBean) context().getRegistry().lookupByName(ExchangeRateBean.NAMESPACE_KEY);

        String expectedResults = "{}";
        String actualResults = exchangeRateBean.getExchangeRatesAsJson();
        Assertions.assertEquals(
                expectedResults,
                actualResults,
                "expected empty json from newly created CurrentExchangeRateBean"
        );
    }

    @Test
    @DisplayName("checks behavior for exchangeRateMap getters and setters by ensuring they report the same data")
    public void testSetAndGetExchangeRateMap() {
        ExchangeRateBean exchangeRateBean = (
                ExchangeRateBean) context().getRegistry().lookupByName(ExchangeRateBean.NAMESPACE_KEY);

        //
        boolean successFlag = exchangeRateBean.setExchangeRateMap(this.dataFileContents);
        Assertions.assertTrue(successFlag, "method expected to report operation was success");

        //
        Map<String, Double> expectedMapResults = Map.of("CAD_USD", 0.98, "FR_USD", 0.9);
        Map<String, Double> actualMapResults = exchangeRateBean.getExchangeRateMap();
        Assertions.assertEquals(expectedMapResults, actualMapResults, "what went in is not what came out");

        //
        String expectedJsonResults = this.dataFileContents;
        String actualJsonResults = exchangeRateBean.getExchangeRatesAsJson();
        Assertions.assertEquals(expectedJsonResults, actualJsonResults);
    }

    @Test
    @DisplayName("checks that a request for an exchange rate that doesn't exist returns empty optional")
    public void testGetExchangeRateForNonExistentKey() {
        ExchangeRateBean exchangeRateBean = (
                ExchangeRateBean) context().getRegistry().lookupByName(ExchangeRateBean.NAMESPACE_KEY);

        assert exchangeRateBean.getExchangeRateMap().isEmpty();

        Optional<Double> actualResult = exchangeRateBean.getExchangeRateFor("floopieDonkWagonTheThird");
        Assertions.assertTrue(
                actualResult.isEmpty(),
                "bean should return empty optional for exchange rate key not present in the exchange rate map"
        );
    }

    @Test
    @DisplayName("checks that a request for an exchange rate that exists returns it wrapped in an optional")
    public void testGetExchangeRateForExistentKey() {
        ExchangeRateBean exchangeRateBean = (
                ExchangeRateBean) context().getRegistry().lookupByName(ExchangeRateBean.NAMESPACE_KEY);

        // setup
        //
        String expectedKey = "CAD_USD";
        Double expectedValue = 0.98;
        assert exchangeRateBean.getExchangeRateMap().isEmpty();

        assert exchangeRateBean.setExchangeRateMap(this.dataFileContents);

        assert exchangeRateBean.getExchangeRateMap().containsKey(expectedKey);
        assert exchangeRateBean.getExchangeRateMap().get(expectedKey).equals(expectedValue);

        // test
        //
        Optional<Double> actualResult = exchangeRateBean.getExchangeRateFor(expectedKey);

        Assertions.assertFalse(
                actualResult.isEmpty(),
                "method should not return an empty Optional in response to a request for something that " +
                         "is in exchangeRate map"
        );


        Assertions.assertEquals(
                expectedValue,
                actualResult.get(),
                "method returned an unexpected value"
        );
    }

    @Test
    @DisplayName("checks that the reset button resets everything it is supposed to")
    public void testResetBean() {
        ExchangeRateBean exchangeRateBean = (
                ExchangeRateBean) context().getRegistry().lookupByName(ExchangeRateBean.NAMESPACE_KEY);

        // setup
        //
        assert exchangeRateBean.getExchangeRateMap().isEmpty();
        assert exchangeRateBean.getBuildID().isEmpty();

        assert exchangeRateBean.setBuildID(this.buildID);
        assert exchangeRateBean.setExchangeRateMap(this.dataFileContents);

        assert exchangeRateBean.getBuildID().equals(this.buildID);
        assert exchangeRateBean.getExchangeRateMap().size() == 2;
        assert exchangeRateBean.getExchangeRatesAsJson().equals(this.dataFileContents);

        // test
        //
        exchangeRateBean.resetBean();

        //
        String expectedBuildId = "";
        String actualBuildId = exchangeRateBean.getBuildID();
        Assertions.assertEquals(
                expectedBuildId,
                actualBuildId,
                "buildID should reset to empty string on bean reset"
        );

        //
        int expectedMapSize = 0;
        int actualMapSize = exchangeRateBean.getExchangeRateMap().size();
        Assertions.assertEquals(
                expectedMapSize,
                actualMapSize,
                "exchangeRateMap size should be zero on bean reset"
        );

        //
        String expectedExchangeRateJson = "{}";
        String actualExchangeRateJson = exchangeRateBean.getExchangeRatesAsJson();
        Assertions.assertEquals(
                expectedExchangeRateJson,
                actualExchangeRateJson,
                "exchangeRateMap, and therefore exchangeRateJson, should be empty on bean reset"
        );

    }

    @Test
    @DisplayName("checks happy path for setExchangeRates")
    public void testSetExchangeRates() {
        ExchangeRateBean exchangeRateBean = (
                ExchangeRateBean) context().getRegistry().lookupByName(ExchangeRateBean.NAMESPACE_KEY);

        // setup
        //
        assert exchangeRateBean.getExchangeRateMap().isEmpty();
        assert exchangeRateBean.getBuildID().isEmpty();


        // tests
        //

        //
        boolean successFlag = exchangeRateBean.setExchangeRates(this.buildID, this.dataFileContents);
        Assertions.assertTrue(successFlag, "method was expected to report successful completion of operation");

        String actualBuildID = exchangeRateBean.getBuildID();
        Assertions.assertEquals(
                this.buildID,
                actualBuildID,
                "bean is returning an unexpected value for buildID"
        );

        //
        Map<String, Double> expectedMap = Map.of("CAD_USD", 0.98, "FR_USD", 0.9);
        Map<String, Double> actualMap = exchangeRateBean.getExchangeRateMap();
        Assertions.assertEquals(
                expectedMap,
                actualMap,
                "bean is returning an unexpected value for exchangeRateMap"
        );

        //
        String actualJsonString = exchangeRateBean.getExchangeRatesAsJson();
        Assertions.assertEquals(
                this.dataFileContents,
                actualJsonString,
                "bean is returning an unexpected value for exchangeRateJson"
        );

    }

    @Test
    @DisplayName("checks that attempting to set exchange rates with a bad buildID results in a reset bean")
    public void testSetExchangeRatesBadBuildId() {
        ExchangeRateBean exchangeRateBean = (
                ExchangeRateBean) context().getRegistry().lookupByName(ExchangeRateBean.NAMESPACE_KEY);

        // setup
        //
        assert exchangeRateBean.getExchangeRateMap().isEmpty();
        assert exchangeRateBean.getBuildID().isEmpty();


        // tests
        //

        //
        boolean successFlag = exchangeRateBean.setExchangeRates("", this.dataFileContents);
        Assertions.assertFalse(successFlag, "method was expected to report failed attempt at operation");

        String actualBuildID = exchangeRateBean.getBuildID();
        Assertions.assertEquals(
                "",
                actualBuildID,
                "bean is returning an unexpected value for buildID"
        );

        //;
        Map<String, Double> actualMap = exchangeRateBean.getExchangeRateMap();
        Assertions.assertTrue(
                actualMap.isEmpty(),
                "bean is returning a populated exchangeRateMap when it shouldn't be"
        );

        //
        String actualJsonString = exchangeRateBean.getExchangeRatesAsJson();
        Assertions.assertEquals(
                "{}",
                actualJsonString,
                "bean is returning an unexpected value for exchangeRateJson"
        );

    }

    @Test
    @DisplayName("checks that attempting to set exchange rates with a bad json data string results in a reset bean")
    public void testSetExchangeRatesBadJsonString() {
        ExchangeRateBean exchangeRateBean = (
                ExchangeRateBean) context().getRegistry().lookupByName(ExchangeRateBean.NAMESPACE_KEY);

        // setup
        //
        assert exchangeRateBean.getExchangeRateMap().isEmpty();
        assert exchangeRateBean.getBuildID().isEmpty();


        // tests
        //

        //
        boolean successFlag = exchangeRateBean.setExchangeRates(buildID, "");
        Assertions.assertFalse(successFlag, "method was expected to report failed attempt at operation");

        String actualBuildID = exchangeRateBean.getBuildID();
        Assertions.assertEquals(
                "",
                actualBuildID,
                "bean is returning an unexpected value for buildID"
        );

        //;
        Map<String, Double> actualMap = exchangeRateBean.getExchangeRateMap();
        Assertions.assertTrue(
                actualMap.isEmpty(),
                "bean is returning a populated exchangeRateMap when it shouldn't be"
        );

        //
        String actualJsonString = exchangeRateBean.getExchangeRatesAsJson();
        Assertions.assertEquals(
                "{}",
                actualJsonString,
                "bean is returning an unexpected value for exchangeRateJson"
        );

    }

}



























