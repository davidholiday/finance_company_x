package io.holitek.finance_company_x;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * for common helpers - here to keep the code DRY
 */
public class Helpers {

    /**
     *
     * @param directory
     * @param filename
     * @return
     */
    public static boolean fileExists(String directory, String filename) {
        Path path = Paths.get(directory, filename);
        return Files.exists(path);
    }

}
