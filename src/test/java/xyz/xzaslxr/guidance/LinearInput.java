package xyz.xzaslxr.guidance;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import static xyz.xzaslxr.guidance.ChainsCoverageGuidance.*;

/**
 * @author fe1w0
 * @date 2023/9/10 16:06
 * @Project SerdeFuzzer
 */
public class LinearInput extends Input<Integer> {

    /** A list of byte values (0-255) ordered by their index. */
    protected ArrayList<Integer> values;

    /** The number of bytes requested so far */
    protected int requested = 0;

    public LinearInput() {
        super();
        this.values = new ArrayList<>();
    }

    public LinearInput(LinearInput other) {
        super(other);
        this.values = new ArrayList<>(other.values);
    }

    @Override
    public int getOrGenerateFresh(Integer key, Random random) {
        // Otherwise, make sure we are requesting just beyond the end-of-list
        // assert (key == values.size());
        if (key != requested) {
            throw new IllegalStateException(String.format("Bytes from linear input out of order. " +
                    "Size = %d, Key = %d", values.size(), key));
        }

        // Don't generate over the limit
        if (requested >= MAX_INPUT_SIZE) {
            return -1;
        }

        // If it exists in the list, return it
        if (key < values.size()) {
            requested++;
            // infoLog("Returning old byte at key=%d, total requested=%d", key, requested);
            return values.get(key);
        }

        // 当结束时，生成一个 -1
        // Handle end of stream
        if (GENERATE_EOF_WHEN_OUT) {
            return -1;
        } else {
            // Just generate a random input
            int val = random.nextInt(256);
            values.add(val);
            requested++;
            // infoLog("Generating fresh byte at key=%d, total requested=%d", key,
            // requested);
            return val;
        }
    }

    @Override
    public int size() {
        return values.size();
    }

    /**
     *
     * Truncates the input list to remove values that were never actually requested.
     * <p>
     * Although this operation mutates the underlying object, the effect should
     * not be externally visible (at least as long as the test executions are
     * deterministic).
     * </p>
     */
    @Override
    public void gc() {
        // Remove elements beyond "requested"
        values = new ArrayList<>(values.subList(0, requested));
        values.trimToSize();

        // c
        if (values.isEmpty()) {
            throw new IllegalArgumentException(
                    "Input is either empty or nothing was requested from the input generator.");
        }
    }

    @Override
    public Input fuzz(Random random) {
        // Clone this input to create initial version of new child
        LinearInput newInput = new LinearInput(this);

        // Stack a bunch of mutations
        int numMutations = sampleGeometric(random, MEAN_MUTATION_COUNT);
        newInput.desc += ",havoc:" + numMutations;

        boolean setToZero = random.nextDouble() < 0.1; // one out of 10 times

        for (int mutation = 1; mutation <= numMutations; mutation++) {

            // Select a random offset and size
            int offset = random.nextInt(newInput.values.size());
            int mutationSize = sampleGeometric(random, MEAN_MUTATION_SIZE);

            desc += String.format(":%d@%d", offset, mutationSize);

            // Mutate a contiguous set of bytes from offset
            for (int i = offset; i < offset + mutationSize; i++) {
                // Don't go past end of list
                if (i >= newInput.values.size()) {
                    break;
                }

                // Otherwise, apply a random mutation
                int mutatedValue = setToZero ? 0 : random.nextInt(256);
                newInput.values.set(i, mutatedValue);
            }
        }

        return newInput;
    }

    @Override
    public Iterator<Integer> iterator() {
        return values.iterator();
    }
}