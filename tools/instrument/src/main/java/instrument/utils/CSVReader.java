package instrument.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fe1w0
 * @date 2023/10/11 13:37
 * @Project instrument
 */
public class CSVReader {


    public static List<String> sinksReader(String configurationPath) throws IOException {
        List<String> sinks = new ArrayList<String>();
        CSVParser parser = CSVParser.parse(new File(configurationPath), Charset.defaultCharset(), CSVFormat.newFormat('\t'));

        for (CSVRecord record : parser) {
            String jqfMethod = convertDescription(record.values());
            sinks.add(jqfMethod);
        }
        return sinks;
    }

    private static String convertDescription (String[] values) {
        if (values.length == 3) {

            String result = values[0].replaceAll("\\.", "/")
                    + "#" + values[1] + "#" + values[2];
            Logger.info("Prepare Instrumented Sinks: {}", result);
            return result;
        } else {
            return null;
        }
    }

}
