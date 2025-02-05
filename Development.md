# coverage.out

Fuzzing成功构造可触发example.jar文件中的反序列化漏洞的程序输入(Program Input)
```
BranchEvent: (1065361410) sources/demo/ExpOne#check():16 [0]
CallEvent: (-1124061180) sources/serialize/UnsafeSerialize#readObject():39 --> sources/demo/Safe#safe(Lsources/demo/SafeClass;Lsources/demo/SafeClass;)V
CallEvent: (-1124061181) sources/serialize/UnsafeSerialize#readObject():38 --> sources/demo/Safe#<init>()V
CallEvent: (000000000) #<unknown>():0 --> sources/serialize/UnsafeSerialize#writeObject(Ljava/io/ObjectOutputStream;)V
CallEvent: (1065361412) sources/demo/ExpOne#check():19 --> sources/demo/ExpTwo#test(Ljava/lang/String;)V
CallEvent: (1333792770) sources/demo/Safe#<init>():12 --> sources/demo/SafeClass#<init>()V
CallEvent: (1333792771) sources/demo/Safe#<init>():13 --> sources/demo/SafeClass#<init>()V
CallEvent: (1333805057) sources/demo/Safe#safe():34 --> sources/demo/ExpOne#check(Lsources/demo/SafeClass;)V
CallEvent: (494936065) sources/dynamic/Reflect#handleMethod():10 --> sources/dynamic/Reflect#exec(Ljava/lang/String;)V
CallEvent: (494940161) sources/dynamic/Reflect#exec():14 --> sources/demo/EvilObject#<init>()V
CallEvent: (494940162) sources/dynamic/Reflect#exec():15 --> sources/demo/EvilObject#evil(Ljava/lang/String;)V
CallEvent: (956309508) sources/demo/ExpTwo#test():21 --> sources/dynamic/Reflect#<init>()V
CallEvent: (956309509) sources/demo/ExpTwo#test():22 --> sources/dynamic/Reflect#handleMethod(Ljava/lang/String;)V
```

# fromSourceCallGraph.csv
```
<Start Method>	<sources.serialize.UnsafeSerialize: void readObject(java.io.ObjectInputStream)>
<sources.demo.Safe: void safe(sources.demo.SafeClass,sources.demo.SafeClass)>	<sources.demo.ExpOne: void check(sources.demo.SafeClass)>
<sources.demo.Safe: void safe(sources.demo.SafeClass,sources.demo.SafeClass)>	<sources.demo.SafeClass: void check(sources.demo.SafeClass)>
<sources.demo.Safe: void safe(sources.demo.SafeClass,sources.demo.SafeClass)>	<sources.demo.SafeOne: void check(sources.demo.SafeClass)>
<sources.demo.ExpTwo: void test(java.lang.String)>	<java.lang.Class: java.lang.Class forName(java.lang.String)>
<sources.demo.ExpTwo: void test(java.lang.String)>	<java.lang.Class: java.lang.reflect.Method getMethod(java.lang.String,java.lang.Class[])>
<sources.demo.ExpTwo: void test(java.lang.String)>	<java.lang.Class: java.lang.Object newInstance()>
<sources.demo.ExpTwo: void test(java.lang.String)>	<sources.dynamic.Reflect: void handleMethod(java.lang.String)>
<sources.dynamic.Reflect: void handleMethod(java.lang.String)>	<sources.dynamic.Reflect: void exec(java.lang.String)>
<sources.dynamic.Reflect: void exec(java.lang.String)>	<sources.demo.EvilObject: void evil(java.lang.String)>
<sources.dynamic.Reflect: void exec(java.lang.String)>	<sources.demo.EvilObject: void <init>()>
<sources.demo.ExpOne: void check(sources.demo.SafeClass)>	<sources.demo.ExpTwo: void test(java.lang.String)>
<sources.demo.ExpOne: void check(sources.demo.SafeClass)>	<sources.demo.SafeClass: void test(java.lang.String)>
<sources.demo.ExpOne: void check(sources.demo.SafeClass)>	<sources.demo.SafeTwo: void test(java.lang.String)>
<sources.serialize.UnsafeSerialize: void readObject(java.io.ObjectInputStream)>	<sources.demo.Safe: void safe(sources.demo.SafeClass,sources.demo.SafeClass)>
<sources.serialize.UnsafeSerialize: void readObject(java.io.ObjectInputStream)>	<sources.demo.Safe: void <init>()>
<sources.serialize.UnsafeSerialize: void readObject(java.io.ObjectInputStream)>	<java.io.ObjectInputStream: void defaultReadObject()>
<sources.demo.Safe: void <init>()>	<java.lang.Object: void <init>()>
<sources.demo.Safe: void <init>()>	<sources.demo.SafeClass: void <init>()>
<sources.demo.SafeClass: void <init>()>	<java.lang.Object: void <init>()>
<sources.demo.EvilObject: void <init>()>	<java.lang.Object: void <init>()>
```

# SARIF