package xyz.xzaslxr.utils.setting;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class PropertyTreeNode {

    /**
     * Node的label分为三类:
     * <p>1. ROOT </p>
     * <p>      ROOT 表示为根节点，不包含任何属性</p>
     * <p>2. ORDINARY</p>
     * <p>      ORDINARY 表示为一般属性</p>
     * <p>3. PRIORITY</p>
     * <p>      PRIORITY 表示为基本属性，类型包含数字、字符串</p>
     */
    private String label;

    private String className;

    private String fieldName;

    private String defaultValue;

    private List<PropertyTreeNode> fields;

    public PropertyTreeNode(String label, String className, String fieldName, String defaultValue, List<PropertyTreeNode> fields) {
        this.label = label;
        this.className = className;
        this.fieldName = fieldName;
        this.defaultValue = defaultValue;
        this.fields = fields;
    }

    public PropertyTreeNode(String label, String className, String fieldName, List<PropertyTreeNode> fields) {
        this.label = label;
        this.className = className;
        this.fieldName = fieldName;
        this.defaultValue = null;
        this.fields = fields;
    }

    /**
     * 初始化 PropertyTreeNode.
     * <p>label: "Root"</p>
     * @param label
     * @param className
     * @param fieldName
     */
    public PropertyTreeNode(String label, String className, String fieldName) {
        this.label = label;
        this.className = className;
        this.fieldName = fieldName;
        this.fields = new ArrayList<PropertyTreeNode>();
    }


    /**
     * 适用于 jackson 中 `fields = []` 的无参数构造，该 object 的所有成员变量都为`null`。
     */
    public PropertyTreeNode() {
        this.label = null;
        this.className = null;
        this.fieldName = null;
        this.fields = null;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getDefaultValue() { return defaultValue; }

    public void setDefaultValue(String defaultValue) {  this.defaultValue = defaultValue; }

    public List<PropertyTreeNode> getFields() { return fields; }

    public void setFields(List<PropertyTreeNode> fields) {
        this.fields = fields;
    }

    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = null;
        try {
            json = objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return json;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || getClass() != object.getClass())
            return false;
        PropertyTreeNode that = (PropertyTreeNode) object;
        return Objects.equals(label, that.label) && Objects.equals(className, that.className)
                && Objects.equals(defaultValue, that.defaultValue)
                && Objects.equals(fieldName, that.fieldName) && Objects.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, className, fieldName, defaultValue ,fields);
    }

    /**
     * 若该object的所有成员变量都为`null`，则认为该object为空。
     * @return
     */
    public boolean isEmpty() {
        if (this.className == null && this.fieldName == null
                && this.defaultValue == null
                && this.fields == null && this.label == null) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * Testing
     * @param args
     * @throws JsonProcessingException
     */
    public static void main(String[] args) throws JsonProcessingException {

        PropertyTreeNode sizeField = new PropertyTreeNode("PRIORITY", "java.lang.Integer", "size");

        List<PropertyTreeNode> tmpFieldList = new ArrayList<>();
        tmpFieldList.add(sizeField);

        PropertyTreeNode chainOne = new PropertyTreeNode("ORDINARY", "sources.demo.ExpOne", "chainOne", "40", tmpFieldList);

        PropertyTreeNode chainTwo = new PropertyTreeNode("ORDINARY", "sources.demo.ExpTwo", "chainTwo");

        List<PropertyTreeNode> newTmpFieldList = new ArrayList<>();
        newTmpFieldList.add(chainOne);
        newTmpFieldList.add(chainTwo);

        PropertyTreeNode root = new PropertyTreeNode("ROOT", "sources.serialize.UnsafeSerialize", null, null, newTmpFieldList);


        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(root);

        System.out.println(json);
    }
}
