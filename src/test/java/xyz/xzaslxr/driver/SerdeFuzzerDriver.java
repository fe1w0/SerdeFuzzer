package xyz.xzaslxr.driver;

import edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance;
import edu.berkeley.cs.jqf.fuzz.guidance.Guidance;
import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;
import edu.berkeley.cs.jqf.instrument.InstrumentingClassLoader;
import org.junit.runner.Result;
import picocli.CommandLine;
import xyz.xzaslxr.fuzzing.SerdeFuzzerTest;
import xyz.xzaslxr.guidance.ChainsCoverageGuidance;
import xyz.xzaslxr.guidance.ReproGuidance;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.*;

/**
 * <p>
 * This Class is a driver to run SerdeFuzzer-DirectedGuidance, and inspired by
 * this article
 * <a href="https://github.com/rohanpadhye/JQF/issues/102">GuidedFuzzing.run
 * does not collect coverage when run a second time in same program</a>
 * 
 * @author <a href="https://github.com/fe1w0"> fe1w0</a>
 */
@CommandLine.Command(name = "SerdeFuzzer", version = "SerdeFuzzer 1.0", mixinStandardHelpOptions = true)
public class SerdeFuzzerDriver implements Runnable {

    @CommandLine.Option(names = { "-f",
            "--target-file" }, required = true, description = "TargetFile: TargetFile will be included by InstrumentedClassLoader, only one Jar file is supported.")
    public static String fuzzTargetFile;

    @CommandLine.Option(names = { "-o",
            "--output-directory" }, required = true, description = "outputDirectory: includes poc.ser(under report mode), no-poc.ser(under report mode) and target/fuzz-report(under fuzz and chains modes).")
    public static String outputDirectoryName;

    // Todo: 支持多种 tree.json 输入，并可以分配不同的能力
    @CommandLine.Option(names = { "-c",
            "--config-directory" }, description = "configDirectory: include tree.json and paths.csv.")
    public static String configDirectory;

    @CommandLine.Option(names = { "-r",
            "--report-input-directory" }, description = "should point to fuzz output files, and only used in report mode.")
    public String inputFilePath;

    @CommandLine.Option(names = { "-t",
            "--timeout" }, defaultValue = "10s", description = "Maximum allowable execution time, default value is 10s.")
    public String fuzzTime = "10s";

    @CommandLine.Option(names = {
            "--skip-exception" }, description = "See: https://github.com/rohanpadhye/JQF/issues/196, allow SerdeFuzzer to catch FuzzException.")
    public boolean isSkipException = false;

    @CommandLine.Option(names = { "-m",
            "--fuzz-mode" }, defaultValue = "chains", description = "FuzzMode: report, fuzz and chains.")
    public String fuzzMode = "chains";

    @CommandLine.Option(names = { "--trials" }, description = "The maximum number of tests allowed.")
    public Long trials = 10_000L;

    public static ClassLoader fuzzClassLoader = null;

    public static List<String> getArtifacts(String targetDirectory) throws IOException {

        return new ArrayList<>(Collections.singletonList(targetDirectory));
    }

    public static void setUpClassLoader(String fuzzTargetDirectory) throws IOException {

        // Todo: 优化 setUpClassLoader ，支持 多Jar
        List<String> classpathElements = getArtifacts(fuzzTargetDirectory);

        ClassLoader appClassLoader = ClassLoader.getSystemClassLoader();

        fuzzClassLoader = new InstrumentingClassLoader(
                classpathElements.toArray(new String[0]),
                appClassLoader);
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new SerdeFuzzerDriver()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        String chainsConfigPath = configDirectory + "/paths.csv";

        String testClassName = SerdeFuzzerTest.class.getName();

        String testMethodName = null;
        if (fuzzMode.equals("fuzz") || fuzzMode.equals("chains")) {
            testMethodName = "fuzz";
        } else if (fuzzMode.equals("report")) {
            testMethodName = "reportFuzz";
            System.setProperty("jqf.repro.logUniqueBranches", "true");
        }

        // 设置 isSkipException
        if (!isSkipException) {
            System.setProperty("jqf.failOnDeclaredExceptions", "true");
        } else {
            System.setProperty("jqf.failOnDeclaredExceptions", "false");
        }

        System.setProperty("jqf.logCoverage", "true");

        String logCoverage = outputDirectoryName + "/coverage.out";
        System.setProperty("logCoverageOutput", logCoverage);

        // 设置 ClassLoader
        try {
            setUpClassLoader(fuzzTargetFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 需要检查输出文件夹的有效性
        File outputDirectory = new File(outputDirectoryName);
        File seedDirectories = null;

        Random random = new Random();
        try {
            // Load the guidance
            String title = testClassName + "#" + testMethodName;

            Duration fuzzDuration = Duration.parse("PT" + fuzzTime);

            Guidance guidance = null;

            if (fuzzMode.equals("fuzz")) {
                guidance = new ZestGuidance(title, fuzzDuration, trials, outputDirectory, seedDirectories, random);
            } else if (fuzzMode.equals("report")) {
                if (inputFilePath == null) {
                    throw new IOException("not set inputFilePath");
                }
                File inputFile = new File(inputFilePath);
                guidance = new ReproGuidance(inputFile, null);
            } else if (fuzzMode.equals("chains")) {
                guidance = new ChainsCoverageGuidance(title, fuzzDuration, trials, outputDirectory, seedDirectories,
                        random, chainsConfigPath);
            }

            // Run the Junit test
            Result res = GuidedFuzzing.run(testClassName, testMethodName, fuzzClassLoader, guidance, System.out);

            if (guidance instanceof ZestGuidance) {
                if (Boolean.getBoolean("jqf.logCoverage")) {
                    System.out.printf("Covered %d edges.%n",
                            ((ZestGuidance) guidance).getTotalCoverage().getNonZeroCount());
                }
            } else if (guidance instanceof ChainsCoverageGuidance) {
                if (Boolean.getBoolean("jqf.logCoverage")) {
                    System.out.printf("Covered %d edges.%n",
                            ((ChainsCoverageGuidance) guidance).getChainsCoverage().getNonZeroCount());
                }
            } else if (guidance instanceof ReproGuidance) {
                Set<String> coverageSet = ((ReproGuidance) guidance).getBranchesCovered();
                assert (coverageSet != null);
                SortedSet<String> sortedCoverage = new TreeSet<>(coverageSet);
                try (PrintWriter covOut = new PrintWriter(new File(logCoverage))) {
                    for (String b : sortedCoverage) {
                        covOut.println(b);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (Boolean.getBoolean("jqf.ei.EXIT_ON_CRASH") && !res.wasSuccessful()) {
                System.exit(3);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
