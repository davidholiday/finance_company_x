package io.holitek.finance_company_x;


import org.apache.camel.main.Main;


/**
 * bootstraps the app
 */
public final class App {

    private App() { }

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.configure().addRoutesBuilder(CurrencyDataPollingConsumerRoute.class);
        main.run(args);
    }

}
