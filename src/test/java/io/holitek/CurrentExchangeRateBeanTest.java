package io.holitek;


import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;

import org.junit.jupiter.api.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CurrentExchangeRateBeanTest extends CamelTestSupport {
    private static final Logger LOG = LoggerFactory.getLogger(CurrentExchangeRateBeanTest.class);

    //
    // test setup and configuration

    // tells the test runner that we'll start and stop the camel context manually. this ensures the camel context
    // doesn't start before we've set up the camel registry and routes.
    @Override
    public boolean isUseAdviceWith() { return true; }

    @BeforeEach
    void beforeEach() {
        context().getRegistry().bind(CurrentExchangeRateBean.NAMESPACE_KEY, new CurrentExchangeRateBean());
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
    @DisplayName("checks default behavior")
    public void testHappyPath() throws Exception {
        getMockEndpoint("mock:result").expectedHeaderReceived(CurrentExchangeRateBean.BUILD_ID_HEADER_KEY, "");
        getMockEndpoint("mock:result").expectedBodiesReceived("{}");
        template.sendBody("direct:start", "");
        assertMockEndpointsSatisfied();
    }

    @Test
    @DisplayName("checks happy path behavior of buildID setter")
    public void testSetBuildIdHappyPath() throws Exception {
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
    public void testSetBuildIdErrorPathEmptyString() throws Exception {

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


}



























