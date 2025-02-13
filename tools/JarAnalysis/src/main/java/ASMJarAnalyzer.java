import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.*;
import java.net.URLClassLoader;
import java.util.*;
import java.net.URL;
import java.io.File;

public class ASMJarAnalyzer {

    // 解析 JAR 文件并提取方法签名
    public static void processMethodChain(String jarPath, String[] methodNames) {
        try {
            File jarFile = new File(jarPath);
            URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { jarFile.toURI().toURL() });

            // 遍历每个方法，提取类名和方法签名
            for (String methodName : methodNames) {
                String[] parts = methodName.trim().split("\\.");
                if (parts.length > 1) {
                    String className = String.join(".", Arrays.copyOf(parts, parts.length - 1));
                    // 去掉括号
                    String method = parts[parts.length - 1].replace("()", "");
                    // 加载类并解析方法签名
                    ArrayList<String> signatures = getMethodSignature(classLoader, className, method);
                    if (signatures.size() > 0) {
                        for (String signature : signatures) {
                            System.out.println((className + "\t" + method + "\t" + signature));
                        }
                    } else {
                        System.out.println("方法 " + method + " 未找到" + "，在类名：" + className);
                    }
                } else {
                    System.out.println("无效的输入方法名：" + methodName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 通过 ASM 加载 JAR 文件并提取方法签名
    public static ArrayList getMethodSignature(ClassLoader classLoader, String className, String methodName)
            throws IOException, ClassNotFoundException {
        ArrayList<String> mns = new ArrayList<>();
        InputStream in = classLoader.getResourceAsStream(className.replace('.', '/') + ".class");
        ClassReader classReader = new ClassReader(in);
        ClassNode cn = new ClassNode();
        classReader.accept(cn, 0);
        for (MethodNode mn : cn.methods) {
            // System.out.println(cn.name + "\t" + mn.name + "\t" + mn.desc);
            if (methodName.equals(mn.name)) {
                mns.add(mn.desc);
            }
        }
        return mns;
    }

    public static void main(String[] args) {
        // 输入 JAR 文件路径和目标方法链
        String jarPath = "/Users/fe1w0/Project/SoftWareAnalysis/Fuzzing/SerdeFuzzer/DataSet/cc7/jar/commons-collections-3.1.jar"; // 替换为实际的
                                                                                                                                  // JAR
                                                                                                                                  // 文件路径

        // jarPath =
        // "/Users/fe1w0/Project/SoftWareAnalysis/Fuzzing/SerdeFuzzer/DataSet/clojureNew/jar/clojure-1.12.0-alpha5.jar";

        String inputFile = "/Users/fe1w0/Project/SoftWareAnalysis/Fuzzing/SerdeFuzzer/DataSet/cc7/conf/paths.txt";

        inputFile = "/Users/fe1w0/Project/SoftWareAnalysis/Fuzzing/SerdeFuzzer/DataSet/rome/conf/paths.txt";
        jarPath = "/Users/fe1w0/Project/SoftWareAnalysis/Fuzzing/SerdeFuzzer/DataSet/rome/jar/rome-1.0.jar";

        inputFile = "/Users/fe1w0/Project/SoftWareAnalysis/Fuzzing/SerdeFuzzer/DataSet/clojure/conf/paths.txt";
        jarPath = "/Users/fe1w0/Project/SoftWareAnalysis/Fuzzing/SerdeFuzzer/DataSet/clojure/jar/clojure-1.8.0.jar";

        // inputFile = "/Users/fe1w0/Project/SoftWareAnalysis/Fuzzing/SerdeFuzzer/DataSet/cc5/conf/paths.txt";
        // jarPath = "/Users/fe1w0/Project/SoftWareAnalysis/Fuzzing/SerdeFuzzer/DataSet/cc5/jar/commons-collections-3.1.jar";

        String[] methodNames = new String[0];

        // 读取 inputFile, 每一行是一个方法,添加至 methodNames
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            methodNames = lines.toArray(new String[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 处理方法链
        processMethodChain(jarPath, methodNames);
    }
}
