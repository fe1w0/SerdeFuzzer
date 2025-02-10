package xyz.xzaslxr.fuzzing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assume.assumeFalse;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.runner.RunWith;

import com.pholser.junit.quickcheck.From;

import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import edu.berkeley.cs.jqf.fuzz.guidance.TimeoutException;
import static xyz.xzaslxr.driver.SerdeFuzzerDriver.fuzzClassLoader;
import static xyz.xzaslxr.driver.SerdeFuzzerDriver.outputDirectoryName;
import xyz.xzaslxr.utils.generator.ByteArrayInputStreamGenerator;

/**
 * SerdeFuzzer 用于Fuzzing libraries，主要与JQF进行交互。
 */

@RunWith(JQF.class)
public class SerdeFuzzerTest {

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    private final String magicWords = "FuzzChains@fe1w0";

    @Rule
    public CaptureOutputRule captureOutput = new CaptureOutputRule();

    /**
     * 修改 fuzzClassLoader，并对 SinkMethod 进行插桩
     */
    @BeforeClass
    public static void instrument() {
    }

    @Fuzz
    public void fuzz(@From(ByteArrayInputStreamGenerator.class) ByteArrayInputStream byteArrayInputStream)
            throws FuzzException {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream) {
                @Override
                public Class resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                    try {
                        return Class.forName(desc.getName(), true, fuzzClassLoader);
                    } catch (Exception e) {
                    }

                    return super.resolveClass(desc);
                }
            };
            // 反序列化
            objectInputStream.readObject();
            byteArrayInputStream.close();
            objectInputStream.close();
        } catch (TimeoutException e) {
            assumeFalse(false);
            throw new FuzzException("This Object is Timeout.");
        } catch (Exception e) {
            System.out.println("反序列化时发生异常: " + e.getMessage());
            // e.printStackTrace();
        }

        // 检验插桩， 若触发插桩，则 isExploitable = true;
        String capturedOutput = captureOutput.getCapturedOutput().trim();
        boolean isExploitable = capturedOutput.contains(magicWords);

        // Todo: 修改检测插桩的方式
        if (isExploitable) {
            // 对于 Poc 生成来说，需要 assumeFalse(false); 引导 POC 的生成
            assumeFalse(false);
            throw new FuzzException("This Object is Exploitable.");
        }
    }

    public String getJarFilePath() throws URISyntaxException {
        URL url = getClass().getProtectionDomain().getCodeSource().getLocation();
        URI uri = url.toURI();
        File jarFile = new File(uri);

        return jarFile.getAbsolutePath();
    }

    public void saveByteArrayInputStream(String filePath, ByteArrayInputStream byteArrayInputStream) {
        try {
            if (filePath.isEmpty()) {
                System.out.println("文件路径不能为空");
                return;
            }

            if (byteArrayInputStream.available() > 0) {
                System.out.println("有效");
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = byteArrayInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }

            byte[] byteArray = byteArrayOutputStream.toByteArray();

            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            fileOutputStream.write(byteArray);
            fileOutputStream.flush();

            byteArrayOutputStream.close();
            byteArrayInputStream.close();

            System.out.println("保存: " + filePath + " 成功");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public ByteArrayOutputStream copyByteArrayInputStream(ByteArrayInputStream byteArrayInputStream)
            throws IOException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // 定义一个缓存数组，临时存放读取的数组
            // 经过测试，4*1024是一个非常不错的数字，过大过小都会比较影响性能
            byte[] buffer = new byte[4096];
            int length;
            while ((length = byteArrayInputStream.read(buffer)) > -1) {
                baos.write(buffer, 0, length);
            }
            baos.flush();
            return baos;
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    /**
     * 与 fuzz 不同，reportFuzz 是用于fuzz得到的seed, 保存危险的Object
     * 
     * @param inputStream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Fuzz
    public void reportFuzz(@From(ByteArrayInputStreamGenerator.class) ByteArrayInputStream inputStream)
            throws Exception {
        String saveFilePath = outputDirectoryName + "/poc.ser";
        ByteArrayOutputStream copyOutputStream = copyByteArrayInputStream(inputStream);
        ByteArrayInputStream saveStream = new ByteArrayInputStream(copyOutputStream.toByteArray());
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(copyOutputStream.toByteArray());
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream) {
                @Override
                public Class resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                    try {
                        return Class.forName(desc.getName(), true, fuzzClassLoader);
                    } catch (Exception e) {
                        System.out.println("解析类时发生异常: " + e.getMessage());
                    }
                    return super.resolveClass(desc);
                }
            };

            // 反序列化
            objectInputStream.readObject();
            byteArrayInputStream.close();
            objectInputStream.close();
        } catch (TimeoutException e) {
            assumeFalse(false);
            throw new FuzzException("This Object is Timeout.");
        } catch (Exception e) {
            // 记录反序列化时的异常
            // System.out.println("反序列化时发生异常: " + e.getMessage());
        }

        String capturedOutput = captureOutput.getCapturedOutput().trim();
        boolean isExploitable = capturedOutput.contains(magicWords);

        if (isExploitable) {
            saveByteArrayInputStream(saveFilePath, saveStream);
            assumeFalse(false);
            throw new FuzzException("This Object is Exploitable.");
        } else {
            assumeFalse(true);
            saveFilePath = outputDirectoryName + "/no-poc.ser";
            saveByteArrayInputStream(saveFilePath, saveStream);
        }
    }
}