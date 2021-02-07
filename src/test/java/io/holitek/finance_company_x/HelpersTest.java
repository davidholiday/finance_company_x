package io.holitek.finance_company_x;


import org.apache.camel.test.junit5.CamelTestSupport;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;


public class HelpersTest extends CamelTestSupport {
    private static final Logger LOG = LoggerFactory.getLogger(HelpersTest.class);

    private static final String TEMP_FILE_NAME = "test-temp-file";
    private static final String TEMP_FILE_EXTENSION = ".txt";

    private File tempFile;

    @BeforeEach
    void beforeEach() throws IOException {
        this.tempFile = File.createTempFile(TEMP_FILE_NAME, TEMP_FILE_EXTENSION);
        tempFile.deleteOnExit();
    }


    @Test
    @DisplayName("helper should report 'true' for file that is present on filesystem")
    public void testFileExistsHappyPath() {
        String directory = tempFile.getParent();
        String filename = tempFile.getName();

        boolean expectedResult = true;
        boolean actualResult = Helpers.fileExists(directory, filename);
        Assertions.assertEquals(expectedResult, actualResult, "method should report true when file exists");
    }

    @Test
    @DisplayName("helper should report 'false' for file that is not present on filesystem")
    public void testFileExistsFileNotFound() {
        boolean expectedResult = false;
        boolean actualResult = Helpers.fileExists("I-do-not-exist", "but-then-who-wrote-this");
        Assertions.assertEquals(expectedResult, actualResult, "method should report true when file exists");
    }


}
