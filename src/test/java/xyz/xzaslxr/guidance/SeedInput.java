package xyz.xzaslxr.guidance;

import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;

import java.io.*;
import java.util.Random;

/**
 * @author fe1w0
 * @date 2023/9/10 16:13
 * @Project SerdeFuzzer
 */
public class SeedInput extends LinearInput {
    final File seedFile;
    final InputStream in;

    public SeedInput(File seedFile) throws IOException {
        super();
        this.seedFile = seedFile;
        this.in = new BufferedInputStream(new FileInputStream(seedFile));
        this.desc = "seed";
    }

    @Override
    public int getOrGenerateFresh(Integer key, Random random) {
        int value;
        try {
            value = in.read();
        } catch (IOException e) {
            throw new GuidanceException("Error reading from seed file: " + seedFile.getName(), e);

        }

        // assert (key == values.size())
        if (key != values.size() && value != -1) {
            throw new IllegalStateException(String.format("Bytes from seed out of order. " +
                    "Size = %d, Key = %d", values.size(), key));
        }

        if (value >= 0) {
            requested++;
            values.add(value);
        }

        // If value is -1, then it is returned (as EOF) but not added to the list
        return value;
    }

    /**
     *
     */
    @Override
    public void gc() {
        super.gc();
        try {
            in.close();
        } catch (IOException e) {
            throw new GuidanceException("Error closing seed file:" + seedFile.getName(), e);
        }
    }
}
