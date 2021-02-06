package io.holitek;


import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;

import org.junit.jupiter.api.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


public class CurrentExchangeRateBeanTest extends CamelTestSupport {
    private static final Logger LOG = LoggerFactory.getLogger(CurrentExchangeRateBeanTest.class);

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


        buildIdContents = context().getPropertiesComponent()
                                   .resolveProperty("buildID.contents")
                                   .orElse("");

        dataFileContents = context().getPropertiesComponent()
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
    @DisplayName("checks default exchange behavior")
    public void testHappyPath() throws Exception {
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

        String expectedBuildID = "foo";
        boolean successFlag = currentExchangeRateBean.setBuildID(expectedBuildID);
        Assertions.assertTrue(successFlag);

        String actualBuildID = currentExchangeRateBean.getBuildID();
        Assertions.assertEquals(expectedBuildID, actualBuildID, "we should get back what we put in!");
    }

    @Test
    @DisplayName("checks buildID setter prevents empty String from being set ")
    public void testSetBuildIdErrorPathEmptyString() {

        // setup
        //
        String expectedBuildID = "foo";
        CurrentExchangeRateBean currentExchangeRateBean = (
                CurrentExchangeRateBean) context().getRegistry().lookupByName(CurrentExchangeRateBean.NAMESPACE_KEY);

        // this way (vs junit way) because this is to validate programmer assumption that expected condition exists.
        assert currentExchangeRateBean.setBuildID(expectedBuildID);


        // test
        //
        boolean successFlag = currentExchangeRateBean.setBuildID("");
        Assertions.assertFalse(successFlag);

        String actualBuildID = currentExchangeRateBean.getBuildID();
        Assertions.assertEquals(
                expectedBuildID,
                actualBuildID,
                "buildID value should not have changed on setBuildID fault"
        );
    }

    @Test
    @DisplayName("checks buildID setter prevents null String from being set ")
    public void testSetBuildIdErrorPathNullString() {

        // setup
        //
        String expectedBuildID = "foo";
        CurrentExchangeRateBean currentExchangeRateBean = (
                CurrentExchangeRateBean) context().getRegistry().lookupByName(CurrentExchangeRateBean.NAMESPACE_KEY);

        // this way (vs junit way) because this is to validate programmer assumption that expected condition exists.
        assert currentExchangeRateBean.setBuildID(expectedBuildID);


        // test
        //
        boolean successFlag = currentExchangeRateBean.setBuildID(null);
        Assertions.assertFalse(successFlag);

        String actualBuildID = currentExchangeRateBean.getBuildID();
        Assertions.assertEquals(
                expectedBuildID,
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
        boolean successFlag = currentExchangeRateBean.setExchangeRateMap(dataFileContents);
        Assertions.assertTrue(successFlag, "method expected to report operation was success");

        //
        Map<String, Double> expectedMapResults = Map.of("CAD_USD", 0.98, "FR_USD", 0.9);
        Map<String, Double> actualMapResults = currentExchangeRateBean.getExchangeRateMap();
        Assertions.assertEquals(expectedMapResults, actualMapResults, "what went in is not what came out");

        //
        String expectedJsonResults = dataFileContents;
        String actualJsonResults = currentExchangeRateBean.getExchangeRatesAsJson();
        Assertions.assertEquals(expectedJsonResults, actualJsonResults);
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

        assert currentExchangeRateBean.setBuildID("foo");
        assert currentExchangeRateBean.setExchangeRateMap(dataFileContents);

        assert currentExchangeRateBean.getBuildID().equals("foo");
        assert currentExchangeRateBean.getExchangeRateMap().size() == 2;
        assert currentExchangeRateBean.getExchangeRatesAsJson().equals(dataFileContents);

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
}



























