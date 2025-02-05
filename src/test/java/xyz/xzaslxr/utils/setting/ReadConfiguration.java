package xyz.xzaslxr.utils.setting;


/**
 * 读取静态分析后得到的配置文件，该配置文件格式为 json 格式，
 * 从而获取PropertyTree、Source、Sink、Edges等信息。
 * <p></p>
 * @author fe1w0
 * @version 1.0
*/
public interface ReadConfiguration {
    <ConfigurationClass> ConfigurationClass readConfiguration(String configurationPath, ConfigurationClass Object);
}