package xyz.xzaslxr.utils.setting;

public class ConfigurationJson {


    /**
     * chainId，用于描述当前任务
     */
    private int chainId;

    /**
    * sourceMethodName， 用于描述当前Chain的入口点函数信息
     */
    private String sourceMethodName;

    /**
     *  sinkMethodName， 用于描述当前Chain Fuzzing的目标
     */
    private String sinkMethodName;

    /**
     * edges，描述edges信息，包含 fromMethod -{Call}-> toMethod 的调用信息。
     */
    // private HashMap<String, String> edges;

    /**
     * distanceEdges, 描述 fromMethod -{Call} - {DistanceToSource}-> toMethod 的调用信息。
     * Todo: 在2.0中，需要支持 distance 信息，即当前edge 涉及到的 BasicBlock 与 Source 的距离信息
     */
    // private HashMap<Integer, HashMap<String, String>> distanceEdges;

    private PropertyTreeNode propertyTreeNode;
}
