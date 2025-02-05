package instrument;

import instrument.utils.JarModifier;
import org.tinylog.configuration.Configuration;
import picocli.CommandLine;
import org.tinylog.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static instrument.utils.CSVReader.sinksReader;


/**
 * @author fe1w0
 * @date 2023/10/9 16:54
 * @Project instrument
 * @Description Taking from Serhybrid
 */

@CommandLine.Command(name = "instrument.Instrument", version = "instrument.Instrument 1.0")
public class Transform implements Runnable {

    @CommandLine.Option(names = {"-i", "--input-file"}, required = true, description = "设置被插桩的Jar文件")
    public static String targetFile;

    @CommandLine.Option(names = {"-s", "--sink-method-file"}, required = true, description = "从sink.csv文件中读取需要被插桩的函数")
    public static String sinkMethodFile;

    @CommandLine.Option(names = {"-o", "--output-file"}, required = true, description = "插桩后的文件")
    public static String outputFile;

    @CommandLine.Option(names = {"-l", "--log"}, description = "日志文件")
    public static String logFile;


    public static void main(String[] args) {
        int exitCode = new CommandLine(new Transform()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        if (logFile != null) {
            Map<String, String> logConfig = new HashMap<>();
            logConfig.put("writer.file", logFile);
            logConfig.put("writer", "file");
            logConfig.put("writer.level", "info");
            logConfig.put("writer.format", "{date: HH:mm:ss.SSS} {class}.{method} {level}: {message}");
            Configuration.replace(logConfig);
        }

        List<String> sinksList = new ArrayList<>();

        try {
            sinksList = sinksReader(sinkMethodFile);
        } catch (IOException e) {
            Logger.error(e.getMessage());
        }

        // Todo: 从 CSV 文件中读取 Sinks 信息
        try {
            JarModifier.change(targetFile, outputFile, sinksList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Logger.info("Finish Instrument.");
    }
}
