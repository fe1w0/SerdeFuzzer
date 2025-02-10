package xyz.xzaslxr.utils.generator;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import edu.berkeley.cs.jqf.fuzz.guidance.TimeoutException;
import sun.misc.Unsafe;
import xyz.xzaslxr.utils.setting.PropertyTreeNode;
import xyz.xzaslxr.utils.setting.ReadConfiguration;
import xyz.xzaslxr.utils.setting.ReadPropertyTreeConfigure;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

import static xyz.xzaslxr.driver.SerdeFuzzerDriver.*;

public class ByteArrayInputStreamGenerator extends Generator<ByteArrayInputStream> {

    public static ArrayList<String> validRandomFieldClasses = new ArrayList<String>(
            Arrays.asList(
                    "java.lang.Integer"));

    // Random number 的值域: [0, 100)
    public static Integer maxNumber = 100;

    public static short minShort = 0;

    public static short maxShort = 100;

    public static short MAX_STRING_LENGTH = 20;

    public static int MAX_ARRAY_LENGTH = 10;

    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * 实例化 className
     * 
     * @param className
     * @return
     * @param <className>
     */
    public static <className> Object objectInstance(String className) throws Exception {
        className instantiatedObject = (className) new Object();
        try {
            if (className.equals("java.lang.Class")) {
                return Class.forName(className);
            } else if (className.equals("java.lang.Object")) {
                return new SerdeClass();
            } else if (className.equals("java.util.EmptySet")) {
                return Collections.EMPTY_SET;
            } else if (className.equals("java.util.EmptyList")) {
                return Collections.EMPTY_LIST;
            } else if (className.equals("java.util.EmptyMap")) {
                return Collections.EMPTY_MAP;
            }

            Class<?> targetClass = Class.forName(className, true, fuzzClassLoader);

            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            Unsafe unsafe = (Unsafe) f.get(null);

            instantiatedObject = (className) unsafe.allocateInstance(targetClass);
        } catch (Exception | ExceptionInInitializerError e) {
                e.printStackTrace();
        }
        return instantiatedObject;
    }

    // 递归查找字段
    public static Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        while (clazz != null) {
            try {
                // 尝试获取当前类中的字段
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                // 如果当前类没有该字段，检查父类
                clazz = clazz.getSuperclass();
            }
        }
        // 如果没有找到字段，抛出异常
        throw new NoSuchFieldException("Field '" + fieldName + "' not found in " + clazz + " hierarchy.");
    }

    /**
     * 通过反射的方式去 assign rootObject.[fieldName] = leafObject;
     * <p>
     * </p>
     * 其中 className 为 RootClass的 className, 用于 return 修改后的 rootObject。
     * 
     * @param className
     * @param rootObject
     * @param fieldName
     * @param leafObject
     * @return
     * @param <className>
     * @throws Exception
     */
    public static <className> Object setFieldTree(String className, Object rootObject, String fieldName,
            Object leafObject) {
        try {
            Class<className> rootClass = (Class<className>) Class.forName(className, true, fuzzClassLoader);

            Field rootField = findField(rootClass, fieldName);

            rootField.setAccessible(true);

            rootField.set(rootObject, leafObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rootObject;
    }

    public static Object getFieldFromObject(String className, Object object, String fieldName) {
        try {
            Class rootClass = Class.forName(className, true, fuzzClassLoader);

            Field rootField = rootClass.getDeclaredField(fieldName);
            rootField.setAccessible(true);

            Object field = rootField.get(object);

            return field;

        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public ByteArrayInputStreamGenerator() {
        // Register the type of objects that we can create
        super(ByteArrayInputStream.class);
    }

    public ByteArrayInputStream objectToByteArrayInputStream(Object object) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

            objectOutputStream.writeObject(object);
            objectOutputStream.flush();

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            return byteArrayInputStream;
        } catch (Exception e) {
            // e.printStackTrace();
        }
        return null;
    }

    /**
     * generateFromPropertyTree 递归过程中产生的临时结构
     */
    class IntermediateProduct {
        String fieldName;
        Object fieldObject;

        IntermediateProduct() {
            fieldName = null;
            fieldObject = null;
        }

        boolean isEmpty() {
            return fieldName == null && fieldObject == null;
        }

        boolean isRoot() {
            return fieldName == null && fieldObject != null;
        }
    }

    /**
     * Todo: 待完善，需要根据 tree.json 结构，生成对应的对象，实现基于 defaultValue 的随机化
     * 根据propertyTree生成对象，并设置Random，以用于Fuzzing
     * 
     * @param propertyTree
     * @return
     */
    public IntermediateProduct generateFromPropertyTree(SourceOfRandomness random, PropertyTreeNode propertyTree)
            throws Exception {

        IntermediateProduct iProduct = new IntermediateProduct();

        if (propertyTree.isEmpty()) {
            // 若 propertyTree 为空
            return null;
        }

        // 若 propertyTree 不为空，且 fields 为空
        if (propertyTree.getFields().isEmpty()) {
            String propertyFieldClassName = propertyTree.getClassName();

            iProduct.fieldName = propertyTree.getFieldName();

            String defaultValue = propertyTree.getDefaultValue();
            // 根据默认值，生成对应的 fieldObject
            if (defaultValue != null) {
                switch (propertyFieldClassName) {
                    case "java.lang.Integer":
                    case "int":
                        int value = Integer.parseInt(defaultValue);
                        iProduct.fieldObject = random.nextInt(value, value);
                        break;

                    case "long":
                    case "java.lang.Long":
                        long longValue = Long.parseLong(defaultValue);
                        iProduct.fieldObject = random.nextLong(longValue, longValue);
                        break;

                    case "short":
                    case "java.lang.Short":
                        short shortValue = Short.parseShort(defaultValue);
                        iProduct.fieldObject = random.nextShort(shortValue, shortValue);
                        break;

                    case "double":
                    case "java.lang.Double":
                        double doubleValue = Double.parseDouble(defaultValue);
                        iProduct.fieldObject = random.nextDouble(doubleValue, doubleValue);
                        break;

                    case "float":
                    case "java.lang.Float":
                        float floatValue = Float.parseFloat(defaultValue);
                        iProduct.fieldObject = random.nextFloat(floatValue, floatValue);
                        break;

                    case "byte":
                    case "java.lang.Byte":
                        byte byteValue = Byte.parseByte(defaultValue);
                        iProduct.fieldObject = random.nextByte(byteValue, byteValue);
                        break;

                    case "char":
                    case "java.lang.Character":
                        char charValue = defaultValue.charAt(0);
                        iProduct.fieldObject = random.nextChar(charValue, charValue);
                        break;

                    case "boolean":
                    case "java.lang.Boolean":
                        boolean booleanValue = Boolean.parseBoolean(defaultValue);
                        iProduct.fieldObject = random.nextBoolean();
                        break;
                    case "java.lang.String":
                        iProduct.fieldObject = defaultValue;
                        break;
                    default:
                        iProduct.fieldObject = objectInstance(propertyFieldClassName);
                        break;
                }
            } else {
                // 若不存在 默认值 和 fields，则根据类型生成随机对象
                switch (propertyFieldClassName) {
                    case "java.lang.Integer":
                    case "int":
                        iProduct.fieldObject = random.nextInt(maxNumber);
                        break;

                    case "long":
                    case "java.lang.Long":
                        iProduct.fieldObject = random.nextLong();
                        break;

                    case "short":
                    case "java.lang.Short":
                        iProduct.fieldObject = random.nextShort(minShort, maxShort);
                        break;

                    case "double":
                    case "java.lang.Double":
                        iProduct.fieldObject = random.nextDouble(0.0, (double) maxNumber);
                        break;

                    case "float":
                    case "java.lang.Float":
                        iProduct.fieldObject = random.nextFloat(0, 1.0f);
                        break;

                    case "byte":
                    case "java.lang.Byte":
                        iProduct.fieldObject = random.nextByte((byte) 0, (byte) 127);
                        break;

                    case "char":
                    case "java.lang.Character":
                        iProduct.fieldObject = random.nextChar(Character.MIN_VALUE, Character.MAX_VALUE);
                        break;

                    case "boolean":
                    case "java.lang.Boolean":
                        iProduct.fieldObject = random.nextBoolean();
                        break;

                    case "java.lang.String":
                        int stringLength = random.nextInt(MAX_STRING_LENGTH);
                        StringBuilder tmpString = new StringBuilder(new String());
                        for (int i = 0; i < stringLength; i++) {
                            // TODO: 需要优化
                            tmpString.append(random.nextChar(Character.MIN_VALUE, Character.MAX_VALUE));
                        }
                        iProduct.fieldObject = tmpString.toString();
                        break;

                    case "java.lang.Object":
                        iProduct.fieldObject = new SerdeClass();
                        break;

                    default:
                        // 若不存在 默认值 和 fields，则根据类型生成随机对象，包括数组类型
                        if (propertyFieldClassName.contains("[]")) {
                            int size = random.nextInt(MAX_ARRAY_LENGTH);
                            String arrayType = propertyFieldClassName.substring(0, propertyFieldClassName.indexOf("["));
                            Class componentType = Class.forName(arrayType, true, fuzzClassLoader);
                            Object array = Array.newInstance(componentType, size);
                            for (int i = 0; i < size; i++) {
                                Object element = objectInstance(arrayType);
                                Array.set(array, i, element);
                            }
                            iProduct.fieldObject = array;
                        } else {
                            iProduct.fieldObject = objectInstance(propertyFieldClassName);
                        }
                        break;
                }
            }
        } else {
            if (propertyTree.getClassName().contains("[]")) {
                // 生成数组
                String arrayType = propertyTree.getClassName().substring(0, propertyTree.getClassName().indexOf("["));
                int size = random.nextInt(MAX_ARRAY_LENGTH);

                int valueSize = propertyTree.getFields().size();

                Class componentType = Class.forName(arrayType, true, fuzzClassLoader);
                Object array = Array.newInstance(componentType, size);

                // 随机选择 value Type
                for (int i = 0; i < size; i++) {
                    int valueIndex = random.nextInt(valueSize);
                    PropertyTreeNode node = propertyTree.getFields().get(valueIndex);
                    IntermediateProduct tmpIProduct = generateFromPropertyTree(random, node);
                    Array.set(array, i, tmpIProduct.fieldObject);
                }

                iProduct.fieldName = propertyTree.getFieldName();
                iProduct.fieldObject = array;

            } else {
                // 若 propertyTree 的 fields 为非空

                String propertyFieldClassName = propertyTree.getClassName();
                String propertyFieldName = propertyTree.getFieldName();

                // 创建 root 对象
                Object root = objectInstance(propertyFieldClassName);

                // 设置 root 对象的变量
                for (PropertyTreeNode node : propertyTree.getFields()) {
                    IntermediateProduct tmpIProduct = generateFromPropertyTree(random, node);
                    setFieldTree(propertyFieldClassName, root,
                            tmpIProduct.fieldName, tmpIProduct.fieldObject);
                }

                // 设置 iProduct
                iProduct.fieldName = propertyFieldName;
                iProduct.fieldObject = root;
            }
        }

        if (!iProduct.isEmpty()) {
            return iProduct;
        } else {
            return null;
        }
    }

    @Override
    public ByteArrayInputStream generate(SourceOfRandomness random, GenerationStatus status) {
        // 获得 构造函数
        // 需要注意，SerdeFuzzer 只能根据json数据，知道涉及的class和 fields 是哪些。
        // 需要提供 下面处理:
        // 1. 根据 提供的 className classLoader 获得 class
        // 2. 得到 class 之后，newInstance 后 设置 field
        // 3. 自下(2-阶)而上，从 leaves 依次向上设置 class，直到 root 为止

        // 关闭 generate 过程中，可能会出现的 print
        PrintStream standardOut = System.out;
        ByteArrayOutputStream genOutputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(genOutputStreamCaptor));

        // 读取 PropertyTree 文件
        String configurationPath = configDirectory + "/tree.json";

        ReadConfiguration reader = new ReadPropertyTreeConfigure();
        PropertyTreeNode root = reader.readConfiguration(configurationPath, new PropertyTreeNode());

        IntermediateProduct iProduct = null;
        try {
            iProduct = generateFromPropertyTree(random, root);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Object rootObject = null;

        if (iProduct.isRoot()) {
            rootObject = iProduct.fieldObject;
        }

        // 序列化:
        // rootObject -> ObjectInputStream

        ByteArrayInputStream genByteArrayInputStream = objectToByteArrayInputStream(rootObject);

        // 关闭"屏蔽输出"功能
        System.setOut(standardOut);

        return genByteArrayInputStream;
    }

    static class SerdeClass implements Serializable {
        private static final long serialVersionUID = 22151214440L;
        private final Object object;

        SerdeClass() {
            this.object = null;
        }

        public SerdeClass(Object object) {
            this.object = object;
        }
    }

    public static void main(String[] args) {
        String configurationPath = "./DataSet/cc2/conf/tree.json";
        String fuzzTargetFile = "./DataSet/cc2/jar/i-commons-collections4-4.0.jar";

        configurationPath = "./DataSet/cc6/conf/tree.json";
        fuzzTargetFile = "./DataSet/cc6/jar/i-commons-collections-3.1.jar";

        // 设置 ClassLoader
        try {
            setUpClassLoader(fuzzTargetFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ReadConfiguration reader = new ReadPropertyTreeConfigure();
        PropertyTreeNode root = reader.readConfiguration(configurationPath, new PropertyTreeNode());

        System.out.println(root);
    }
}
