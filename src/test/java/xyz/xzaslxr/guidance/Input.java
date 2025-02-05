package xyz.xzaslxr.guidance;

/**
 * @author fe1w0
 * @date 2023/9/10 16:05
 * @Project SerdeFuzzer
 */
// -------- Chains Coverage Guidance Input Class --------

import edu.berkeley.cs.jqf.fuzz.util.Coverage;
import edu.berkeley.cs.jqf.fuzz.util.ICoverage;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

import java.io.File;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Math.ceil;
import static java.lang.Math.log;

/**
 * 与 ZestGuidance 一样都需要创建一个新的Input Class，
 * Input Class 用于将 K 类映射到字节，以用于 ChainsCoverageGuidance#getInput()。
 * <p>
 * Learning from <code>ZestGuidance$Input</code>
 * </p>
 */
public abstract class Input<K> implements Iterable<Integer> {

    /**
     * The file where this input is saved.
     *
     * <p>
     * This field is null for inputs that are not saved.
     * </p>
     */
    File saveFile = null;

    /**
     * An ID for a saved input.
     *
     * <p>
     * This field is -1 for inputs that are not saved.
     * </p>
     */
    int id;

    /**
     * Whether this input is favored.
     */
    boolean favored;

    /**
     * The description for this input.
     *
     * <p>
     * This field is modified by the construction and mutation
     * operations.
     * </p>
     */
    String desc;

    /**
     * The run coverage for this input, if the input is saved.
     * <p>
     * 需要注意的是，ChainsCoverageGuidance 中的 SCoverage 应当是字符串或者字符串的hashcode。
     * </p>
     * <p>
     * This field is null for inputs that are not saved.
     * </p>
     */
    ICoverage coverage = null;

    /**
     * The number of non-zero elements in `coverage`.
     *
     * <p>
     * 非零分支的input个数。
     * </p>
     *
     * <p>
     * This field is -1 for inputs that are not saved.
     * </p>
     *
     * <p>
     * </p>
     * When this field is non-negative, the information is
     * redundant (can be computed using {@link Coverage#getNonZeroCount()}),
     * but we store it here for performance reasons.
     * </p>
     */
    int nonZeroCoverage = -1;

    /**
     * The number of mutant children spawned from this input that
     * were saved.
     *
     * <p>
     * 该Input产生的突变种子个数。
     * </p>
     *
     * <p>
     * This field is -1 for inputs that are not saved.
     * </p>
     */
    int offspring = -1;

    /**
     * The set of coverage keys for which this input is
     * responsible.
     *
     * <p>
     * This field is null for inputs that are not saved.
     * </p>
     *
     * <p>
     * Each coverage key appears in the responsibility set
     * of exactly one saved input, and all covered keys appear
     * in at least some responsibility set. Hence, this list
     * needs to be kept in-sync with {@link }.
     * </p>
     */
    IntHashSet responsibilities = null;

    /**
     * chainsMap 用于优化程序和处理程序信息。
     * Key 为 ChainMethod 的hashcode, Value 是 ChainMethod 的字符串。
     */
    ConcurrentHashMap<Integer, String> chainsMap = null;

    /**
     * Create an empty input.
     */
    public Input() {
        desc = "random";
    }

    /**
     * Create a copy of an existing input.
     *
     * @param toClone the input map to clone
     */
    public Input(Input toClone) {
        desc = String.format("src:%06d", toClone.id);
    }

    public abstract int getOrGenerateFresh(K key, Random random);

    public abstract int size();

    public abstract Input fuzz(Random random);

    public abstract void gc();

    /**
     * Sets this input to be favored for fuzzing.
     */
    public void setFavored() {
        favored = true;
    }

    /**
     * Returns whether this input should be favored for fuzzing.
     *
     * <p>
     * An input is favored if it is responsible for covering
     * at least one branch.
     * </p>
     *
     * @return whether or not this input is favored
     */
    public boolean isFavored() {
        return favored;
    }

    /**
     * Sample from a geometric distribution with given mean.
     *
     * Utility method used in implementing mutation operations.
     *
     * @param random a pseudo-random number generator
     * @param mean   the mean of the distribution
     * @return a randomly sampled value
     */
    public static int sampleGeometric(Random random, double mean) {
        double p = 1 / mean;
        double uniform = random.nextDouble();
        return (int) ceil(log(1 - uniform) / log(1 - p));
    }
}