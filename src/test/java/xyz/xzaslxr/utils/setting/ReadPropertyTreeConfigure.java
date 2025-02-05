package xyz.xzaslxr.utils.setting;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class ReadPropertyTreeConfigure implements ReadConfiguration{

    @Override
    public <ConfigurationClass> ConfigurationClass readConfiguration(String configurationPath, ConfigurationClass object) {
        if (object.getClass() == PropertyTreeNode.class) {
            if (configurationPath != null) {

                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    File jsonFile = new File(configurationPath);

                    PropertyTreeNode root = objectMapper.readValue(jsonFile, PropertyTreeNode.class);

                    return (ConfigurationClass) root;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    /**
     * Testing
     * @param args
     */
    public static void main(String[] args) {
        String configurationPath = "DataSet/cc2/conf/tree.json";

        ReadConfiguration reader = new ReadPropertyTreeConfigure();
        PropertyTreeNode root = reader.readConfiguration(configurationPath, new PropertyTreeNode());

        System.out.println(root);
    }
}