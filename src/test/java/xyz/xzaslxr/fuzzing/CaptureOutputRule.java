package xyz.xzaslxr.fuzzing;

/**
 * @author fe1w0
 * @date 2025/2/3 15:08
 * @Project SerdeFuzzer
 */
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class CaptureOutputRule implements TestRule {
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                // 将 System.out 重定向到 ByteArrayOutputStream
                System.setOut(new PrintStream(outputStreamCaptor));
                try {
                    base.evaluate(); // 执行测试方法
                } finally {
                    System.setOut(originalOut);
                    System.out.println(outputStreamCaptor.toString());
                }
            }
        };
    }

    // 获取捕获的输出
    public String getCapturedOutput() {
        return outputStreamCaptor.toString();
    }
}
