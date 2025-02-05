package xyz.xzaslxr.utils.setting;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fe1w0
 * @date 2023/9/8 16:18
 * @Project SerdeFuzzer
 */
public class ChainPaths {

    List<String> paths;

    public ChainPaths() {
        this.paths = new ArrayList<String>();
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }
}
