package io.holitek.finance_company_x;


import org.apache.camel.BindToRegistry;


/**
 * lets us, among other things, register named beans to the camel registry before the camel context fires up
 */
public class AppConfig {

    /**
     * it's important that we register the bean in this way rather than define the bean by classname. the reason is the
     * bean will need the ability dynamically alter the contents of the exchange message and, when defined by classname,
     * that doesn't seem to be possible as all of that gets resolved and set in stone at runetime. womp womp.
     *
     * @return
     */
    @BindToRegistry
    public ExchangeRateBean exchangeRateBean() { return new ExchangeRateBean(); }

}
