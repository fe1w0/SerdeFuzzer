package xyz.xzaslxr.utils.setting;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;


public class ReadChainPathsConfigure implements ReadConfiguration{

    @Override
    public <ConfigurationClass> ConfigurationClass readConfiguration(String configurationPath, ConfigurationClass object) {
        ChainPaths chainPaths = new ChainPaths();

        if (object.getClass() == ChainPaths.class) {

            try{
                CSVParser parser = CSVParser.parse(new File(configurationPath), Charset.defaultCharset(), CSVFormat.newFormat('\t'));

                for (CSVRecord record : parser) {
                    String jqfMethod = convertDescription(record.values());
                    chainPaths.paths.add(jqfMethod);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return (ConfigurationClass) chainPaths;
        }
        return (ConfigurationClass) chainPaths;
    }

    /**
     * 转换格式，将 doop 上的格式转为 jvm 上的 描述符，以用于 guidance 上的 directed 算法
     * sources.dynamic.Reflect	handleMethod	(Ljava/lang/String;)V
     *     ->
     * sources/dynamic/Reflect#handleMethod(Ljava/lang/String;)V
     * </p>
     * @param doopDescription,  doop 上的函数签名与描述
     * @return String
     */
    public static String convertDescription(String[] doopDescription){
        if (doopDescription.length == 3) {
            return doopDescription[0].replaceAll("\\.", "/")
                    + "#" + doopDescription[1] + doopDescription[2];
        } else {
            return null;
        }
    }


    /**
     * Testing
     * @param args
     */
    public static void main(String[] args) {
        String file = "DataSet/paths.csv";
        ReadChainPathsConfigure reader = new ReadChainPathsConfigure();

        ChainPaths chainPaths = reader.readConfiguration(file, new ChainPaths());

        System.out.println(chainPaths.paths);

    }
}
