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


public class CurrentExchangeRateBeanTest extends CamelTestSupport {
    private static final Logger LOG = LoggerFactory.getLogger(CurrentExchangeRateBeanTest.class);

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
                 .bind(CurrentExchangeRateBean.NAMESPACE_KEY, new CurrentExchangeRateBean());

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
                        .bean(CurrentExchangeRateBean.NAMESPACE_KEY)
                        .to("mock:result");
            }
        };
    }


    //
    // tests

    @Test
    @DisplayName("checks that bean in default state properly handles exchange messages ")
    public void testExchangeHandlerHappyPath() throws Exception {
        getMockEndpoint("mock:result").expectedHeaderReceived(CurrentExchangeRateBean.BUILD_ID_HEADER_KEY, "");
        getMockEndpoint("mock:result").expectedBodiesReceived("{}");
        template.sendBody("direct:start", "");
        assertMockEndpointsSatisfied();
    }

    @Test
    @DisplayName("checks happy path behavior of buildID setter")
    public void testSetBuildIdHappyPath() {
        CurrentExchangeRateBean currentExchangeRateBean = (
                CurrentExchangeRateBean) context().getRegistry().lookupByName(CurrentExchangeRateBean.NAMESPACE_KEY);

        boolean successFlag = currentExchangeRateBean.setBuildID(this.buildID);
        Assertions.assertTrue(successFlag);

        String actualBuildID = currentExchangeRateBean.getBuildID();
        Assertions.assertEquals(this.buildID, actualBuildID, "we should get back what we put in!");
    }

    @Test
    @DisplayName("checks buildID setter prevents empty String from being set ")
    public void testSetBuildIdErrorPathEmptyString() {

        // setup
        //
        CurrentExchangeRateBean currentExchangeRateBean = (
                CurrentExchangeRateBean) context().getRegistry().lookupByName(CurrentExchangeRateBean.NAMESPACE_KEY);

        // this way (vs junit way) because this is to validate programmer assumption that expected condition exists.
        assert currentExchangeRateBean.setBuildID(this.buildID);


        // test
        //
        boolean successFlag = currentExchangeRateBean.setBuildID("");
        Assertions.assertFalse(successFlag);

        String actualBuildID = currentExchangeRateBean.getBuildID();
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
        CurrentExchangeRateBean currentExchangeRateBean = (
                CurrentExchangeRateBean) context().getRegistry().lookupByName(CurrentExchangeRateBean.NAMESPACE_KEY);

        assert currentExchangeRateBean.setBuildID(this.buildID);


        // test
        //
        boolean successFlag = currentExchangeRateBean.setBuildID(null);
        Assertions.assertFalse(successFlag);

        String actualBuildID = currentExchangeRateBean.getBuildID();
        Assertions.assertEquals(
                this.buildID,
                actualBuildID,
                "buildID value should not have changed on setBuildID fault"
        );
    }

    @Test
    @DisplayName("checks default behavior for getExchangeRateMap ")
    public void testGetExchangeRateMapDefault() {
        CurrentExchangeRateBean currentExchangeRateBean = (
                CurrentExchangeRateBean) context().getRegistry().lookupByName(CurrentExchangeRateBean.NAMESPACE_KEY);

        Map<String, Double> expectedResults = new HashMap<>();
        Map<String, Double> actualResults = currentExchangeRateBean.getExchangeRateMap();
        Assertions.assertEquals(
                expectedResults,
                actualResults,
                "expected empty map from newly created CurrentExchangeRateBean"
        );
    }

    @Test
    @DisplayName("checks default behavior for getExchangeRateJson ")
    public void testGetExchangeRateAsJsonDefault() {
        CurrentExchangeRateBean currentExchangeRateBean = (
                CurrentExchangeRateBean) context().getRegistry().lookupByName(CurrentExchangeRateBean.NAMESPACE_KEY);

        String expectedResults = "{}";
        String actualResults = currentExchangeRateBean.getExchangeRatesAsJson();
        Assertions.assertEquals(
                expectedResults,
                actualResults,
                "expected empty json from newly created CurrentExchangeRateBean"
        );
    }

    @Test
    @DisplayName("checks behavior for exchangeRateMap getters and setters by ensuring they report the same data")
    public void testSetAndGetExchangeRateMap() {
        CurrentExchangeRateBean currentExchangeRateBean = (
                CurrentExchangeRateBean) context().getRegistry().lookupByName(CurrentExchangeRateBean.NAMESPACE_KEY);

        //
        boolean successFlag = currentExchangeRateBean.setExchangeRateMap(this.dataFileContents);
        Assertions.assertTrue(successFlag, "method expected to report operation was success");

        //
        Map<String, Double> expectedMapResults = Map.of("CAD_USD", 0.98, "FR_USD", 0.9);
        Map<String, Double> actualMapResults = currentExchangeRateBean.getExchangeRateMap();
        Assertions.assertEquals(expectedMapResults, actualMapResults, "what went in is not what came out");

        //
        String expectedJsonResults = this.dataFileContents;
        String actualJsonResults = currentExchangeRateBean.getExchangeRatesAsJson();
        Assertions.assertEquals(expectedJsonResults, actualJsonResults);
    }

    @Test
    @DisplayName("checks that a request for an exchange rate that doesn't exist returns empty optional")
    public void testGetExchangeRateForNonExistentKey() {
        CurrentExchangeRateBean currentExchangeRateBean = (
                CurrentExchangeRateBean) context().getRegistry().lookupByName(CurrentExchangeRateBean.NAMESPACE_KEY);

        assert currentExchangeRateBean.getExchangeRateMap().isEmpty();

        Optional<Double> actualResult = currentExchangeRateBean.getExchangeRateFor("floopieDonkWagonTheThird");
        Assertions.assertTrue(
                actualResult.isEmpty(),
                "bean should return empty optional for exchange rate key not present in the exchange rate map"
        );
    }

    @Test
    @DisplayName("checks that a request for an exchange rate that exists returns it wrapped in an optional")
    public void testGetExchangeRateForExistentKey() {
        CurrentExchangeRateBean currentExchangeRateBean = (
                CurrentExchangeRateBean) context().getRegistry().lookupByName(CurrentExchangeRateBean.NAMESPACE_KEY);

        // setup
        //
        String expectedKey = "CAD_USD";
        Double expectedValue = 0.98;
        assert currentExchangeRateBean.getExchangeRateMap().isEmpty();

        assert currentExchangeRateBean.setExchangeRateMap(this.dataFileContents);

        assert currentExchangeRateBean.getExchangeRateMap().containsKey(expectedKey);
        assert currentExchangeRateBean.getExchangeRateMap().get(expectedKey).equals(expectedValue);

        // test
        //
        Optional<Double> actualResult = currentExchangeRateBean.getExchangeRateFor(expectedKey);

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
        CurrentExchangeRateBean currentExchangeRateBean = (
                CurrentExchangeRateBean) context().getRegistry().lookupByName(CurrentExchangeRateBean.NAMESPACE_KEY);

        // setup
        //
        assert currentExchangeRateBean.getExchangeRateMap().isEmpty();
        assert currentExchangeRateBean.getBuildID().isEmpty();

        assert currentExchangeRateBean.setBuildID(this.buildID);
        assert currentExchangeRateBean.setExchangeRateMap(this.dataFileContents);

        assert currentExchangeRateBean.getBuildID().equals(this.buildID);
        assert currentExchangeRateBean.getExchangeRateMap().size() == 2;
        assert currentExchangeRateBean.getExchangeRatesAsJson().equals(this.dataFileContents);

        // test
        //
        currentExchangeRateBean.resetBean();

        //
        String expectedBuildId = "";
        String actualBuildId = currentExchangeRateBean.getBuildID();
        Assertions.assertEquals(
                expectedBuildId,
                actualBuildId,
                "buildID should reset to empty string on bean reset"
        );

        //
        int expectedMapSize = 0;
        int actualMapSize = currentExchangeRateBean.getExchangeRateMap().size();
        Assertions.assertEquals(
                expectedMapSize,
                actualMapSize,
                "exchangeRateMap size should be zero on bean reset"
        );

        //
        String expectedExchangeRateJson = "{}";
        String actualExchangeRateJson = currentExchangeRateBean.getExchangeRatesAsJson();
        Assertions.assertEquals(
                expectedExchangeRateJson,
                actualExchangeRateJson,
                "exchangeRateMap, and therefore exchangeRateJson, should be empty on bean reset"
        );

    }

    @Test
    @DisplayName("checks happy path for setExchangeRates")
    public void testSetExchangeRates() {
        CurrentExchangeRateBean currentExchangeRateBean = (
                CurrentExchangeRateBean) context().getRegistry().lookupByName(CurrentExchangeRateBean.NAMESPACE_KEY);

        // setup
        //
        assert currentExchangeRateBean.getExchangeRateMap().isEmpty();
        assert currentExchangeRateBean.getBuildID().isEmpty();


        // tests
        //

        //
        boolean successFlag = currentExchangeRateBean.setExchangeRates(this.buildID, this.dataFileContents);
        Assertions.assertTrue(successFlag, "method was expected to report successful completion of operation");

        String actualBuildID = currentExchangeRateBean.getBuildID();
        Assertions.assertEquals(
                this.buildID,
                actualBuildID,
                "bean is returning an unexpected value for buildID"
        );

        //
        Map<String, Double> expectedMap = Map.of("CAD_USD", 0.98, "FR_USD", 0.9);
        Map<String, Double> actualMap = currentExchangeRateBean.getExchangeRateMap();
        Assertions.assertEquals(
                expectedMap,
                actualMap,
                "bean is returning an unexpected value for exchangeRateMap"
        );

        //
        String actualJsonString = currentExchangeRateBean.getExchangeRatesAsJson();
        Assertions.assertEquals(
                this.dataFileContents,
                actualJsonString,
                "bean is returning an unexpected value for exchangeRateJson"
        );

    }

    @Test
    @DisplayName("checks that attempting to set exchange rates with a bad buildID results in a reset bean")
    public void testSetExchangeRatesBadBuildId() {
        CurrentExchangeRateBean currentExchangeRateBean = (
                CurrentExchangeRateBean) context().getRegistry().lookupByName(CurrentExchangeRateBean.NAMESPACE_KEY);

        // setup
        //
        assert currentExchangeRateBean.getExchangeRateMap().isEmpty();
        assert currentExchangeRateBean.getBuildID().isEmpty();


        // tests
        //

        //
        boolean successFlag = currentExchangeRateBean.setExchangeRates("", this.dataFileContents);
        Assertions.assertFalse(successFlag, "method was expected to report failed attempt at operation");

        String actualBuildID = currentExchangeRateBean.getBuildID();
        Assertions.assertEquals(
                "",
                actualBuildID,
                "bean is returning an unexpected value for buildID"
        );

        //;
        Map<String, Double> actualMap = currentExchangeRateBean.getExchangeRateMap();
        Assertions.assertTrue(
                actualMap.isEmpty(),
                "bean is returning a populated exchangeRateMap when it shouldn't be"
        );

        //
        String actualJsonString = currentExchangeRateBean.getExchangeRatesAsJson();
        Assertions.assertEquals(
                "{}",
                actualJsonString,
                "bean is returning an unexpected value for exchangeRateJson"
        );

    }

    @Test
    @DisplayName("checks that attempting to set exchange rates with a bad json data string results in a reset bean")
    public void testSetExchangeRatesBadJsonString() {
        CurrentExchangeRateBean currentExchangeRateBean = (
                CurrentExchangeRateBean) context().getRegistry().lookupByName(CurrentExchangeRateBean.NAMESPACE_KEY);

        // setup
        //
        assert currentExchangeRateBean.getExchangeRateMap().isEmpty();
        assert currentExchangeRateBean.getBuildID().isEmpty();


        // tests
        //

        //
        boolean successFlag = currentExchangeRateBean.setExchangeRates(buildID, "");
        Assertions.assertFalse(successFlag, "method was expected to report failed attempt at operation");

        String actualBuildID = currentExchangeRateBean.getBuildID();
        Assertions.assertEquals(
                "",
                actualBuildID,
                "bean is returning an unexpected value for buildID"
        );

        //;
        Map<String, Double> actualMap = currentExchangeRateBean.getExchangeRateMap();
        Assertions.assertTrue(
                actualMap.isEmpty(),
                "bean is returning a populated exchangeRateMap when it shouldn't be"
        );

        //
        String actualJsonString = currentExchangeRateBean.getExchangeRatesAsJson();
        Assertions.assertEquals(
                "{}",
                actualJsonString,
                "bean is returning an unexpected value for exchangeRateJson"
        );

    }

}



























