package xyz.xzaslxr.utils.coverage;

import edu.berkeley.cs.jqf.fuzz.util.*;
import edu.berkeley.cs.jqf.instrument.tracing.events.*;
import org.eclipse.collections.api.iterator.IntIterator;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import xyz.xzaslxr.guidance.ChainsCoverageGuidance;

import java.util.Map;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fe1w0
 * @date 2023/9/11 10:22
 * @Project SerdeFuzzer
 */
public class ChainsCoverage extends Coverage {

    /** The size of the coverage map. */
    public static int COVERAGE_MAP_SIZE = (1 << 16) - 1; // Minus one to reduce collisions

    /** The coverage counts for each edge. */
    private final Counter counter = new NonZeroCachingCounter(COVERAGE_MAP_SIZE);

    /**
     * The hashCode counts for the method name of each edge.
     */
    private final Counter chainsCodeCounter = new NonZeroCachingCounter(COVERAGE_MAP_SIZE);

    public Map<Integer, String> getChainPaths() {
        if (this.chainPaths.isEmpty()) {
            return new ChainsCoverage(ChainsCoverageGuidance.chainPaths).chainPaths;
        } else {
            return this.chainPaths;
        }
    }

    protected Map<Integer, String> chainPaths = new ConcurrentHashMap<>();

    public ChainsCoverage() {
        super();
    }

    private int idx(int key) {
        return Hashing.hash(key, COVERAGE_MAP_SIZE);
    }

    public ChainsCoverage(Map<Integer, String> chainPaths) {
        super();

        for ( Map.Entry entry : chainPaths.entrySet()) {
            this.chainPaths.put(idx((Integer) entry.getKey()), (String) entry.getValue());
        }
    }

    public Counter getChainsCodeCounter() {
        return chainsCodeCounter;
    }


    @Override
    public ChainsCoverage copy() {
        ChainsCoverage ret = new ChainsCoverage();

        for (int idx = 0; idx < COVERAGE_MAP_SIZE; idx++) {
            ret.counter.setAtIndex(idx, this.counter.getAtIndex(idx));
        }

        for (int idx = 0; idx < COVERAGE_MAP_SIZE; idx++) {
            ret.chainsCodeCounter.setAtIndex(idx, this.chainsCodeCounter.getAtIndex(idx));
        }

        ret.chainPaths = this.chainPaths;

        return ret;
    }

    @Override
    public int size() {
        return COVERAGE_MAP_SIZE;
    }


    /**
     * <p>
     * handleEvent会执行applyVisitor，applyVisitor将会调用TraceEventVisitor中各类visitor函数，
     * 如对于 CallEvent，会调用 visitCallEvent。
     * </p>
     * 
     * @param e
     */
    @Override
    public void handleEvent(TraceEvent e) {
        super.handleEvent(e);
    }

    /**
     * 处理 BranchEvent
     *
     * @param b
     */
    @Override
    public void visitBranchEvent(BranchEvent b) {
        super.visitBranchEvent(b);
        if (this.chainPaths.containsKey(idx(getEventMethodName(b).hashCode()))) {
            chainsCodeCounter.increment(getEventMethodName(b).hashCode());
        }
    }

    /**
     * 处理 CallEvent
     *
     * @param e
     */
    @Override
    public void visitCallEvent(CallEvent e) {
        super.visitCallEvent(e);

        // hashCodeCounter 中添加 invokedMethodName.hashCode
        if (this.chainPaths.containsKey(idx(getEventMethodName(e).hashCode()))) {
            chainsCodeCounter.increment(getEventMethodName(e).hashCode());
        }
    }

    @Override
    public void visitReturnEvent(ReturnEvent r) {
        super.visitReturnEvent(r);
        counter.increment(r.getIid());

        if (this.chainPaths.containsKey(idx(getEventMethodName(r).hashCode()))) {
            chainsCodeCounter.increment(getEventMethodName(r).hashCode());
        }
    }

    @Override
    public int getNonZeroCount() {
        return counter.getNonZeroSize();
    }

    @Override
    public IntList getCovered() {
        return counter.getNonZeroIndices();
    }


    /**
     * 对于 CallEvent，获取getInvokedMethodName；
     * 对于 BranchEvent， 获取 getContainingMethodName；
     * 其他，返回null
     * 
     * @param traceEvent
     * @return
     */
    public String getEventMethodName(TraceEvent traceEvent) {
        if (traceEvent instanceof CallEvent) {
            return ((CallEvent) traceEvent).getInvokedMethodName();
        } else if (traceEvent instanceof BranchEvent) {
            return traceEvent.getContainingClass() + "#" + (traceEvent).getContainingMethodName() + traceEvent.getContainingMethodDesc();
        } else if (traceEvent instanceof ReturnEvent) {
            return traceEvent.getContainingClass() + "#" + (traceEvent).getContainingMethodName() + traceEvent.getContainingMethodDesc();
        } else {
            return null;
        }
    }

    /**
     * @return int
     */
    public int getNonZeroChainsCount() {
        return chainsCodeCounter.getNonZeroSize();
    }

    /**
     * 获得 非0的空间
     *
     * @return IntList
     */
    public IntList getChainsCovered() {
        return chainsCodeCounter.getNonZeroIndices();
    }


    @Override
    public IntList computeNewCoverage(ICoverage baseline) {
        IntArrayList newCoverage = new IntArrayList();

        IntList baseNonZero = this.counter.getNonZeroIndices();
        IntIterator iter = baseNonZero.intIterator();
        while (iter.hasNext()) {
            int idx = iter.next();
            if (baseline.getCounter().getAtIndex(idx) == 0) {
                newCoverage.add(idx);
            }
        }
        return newCoverage;
    }

    public IntList computeNewCoveredChainsPath(ICoverage baseline) {
        IntArrayList newCoverage = new IntArrayList();

        IntList baseNonZero = this.chainsCodeCounter.getNonZeroIndices();
        IntIterator iter = baseNonZero.intIterator();
        while (iter.hasNext()) {
            int idx = iter.next();
            if (((ChainsCoverage) baseline).getChainsCodeCounter().getAtIndex(idx) == 0) {
                newCoverage.add(idx);
            }
        }
        return newCoverage;
    }

    /**
     * 清理 counter 和 chainsCodeCounter
     */
    @Override
    public void clear() {
        this.counter.clear();
        this.chainsCodeCounter.clear();
    }

    // ----------- Cache -----------

    private static final int[] HOB_CACHE = new int[1024];


    /* Computes the highest order bit */
    private static int computeHob(int num) {
        if (num == 0)
            return 0;

        int ret = 1;

        while ((num >>= 1) != 0)
            ret <<= 1;

        return ret;
    }

    /** Populates the HOB cache. */
    static {
        for (int i = 0; i < HOB_CACHE.length; i++) {
            HOB_CACHE[i] = computeHob(i);
        }
    }

    /** Returns the highest order bit (perhaps using the cache) */
    private static int hob(int num) {
        if (num < HOB_CACHE.length) {
            return HOB_CACHE[num];
        } else {
            return computeHob(num);
        }
    }

    // ----------- Cache Chains -----------

    private static final int[] HOB_CACHE_Chains = new int[1024];


    /* Computes the highest order bit */
    private static int computeHobChains(int num) {
        if (num == 0)
            return 0;

        int ret = 1;

        while ((num >>= 1) != 0)
            ret <<= 1;

        return ret;
    }

    /** Populates the HOB cache Chains. */
    static {
        for (int i = 0; i < HOB_CACHE_Chains.length; i++) {
            HOB_CACHE_Chains[i] = computeHobChains(i);
        }
    }

    /** Returns the highest order bit (perhaps using the cache) */
    private static int hob_chains(int num) {
        if (num < HOB_CACHE_Chains.length) {
            return HOB_CACHE_Chains[num];
        } else {
            return computeHobChains(num);
        }
    }


    @Override
    public boolean updateBits(ICoverage that) {
        boolean changed = false;
        if (that.getCounter().hasNonZeros()) {
            for (int idx = 0; idx < COVERAGE_MAP_SIZE; idx++) {
                int before = this.counter.getAtIndex(idx);
                int after = before | hob(that.getCounter().getAtIndex(idx));
                if (after != before) {
                    this.counter.setAtIndex(idx, after);
                    changed = true;
                }
            }
        }
        return changed;
    }


    /**
     * Todo: 存在问题，当前 before = 1 && after = 0
     *
     * 根据当前的runCoverage, 刷新 ChainsCoverage
     *
     * @param runCoverage
     * @return
     */
    public boolean updateChainsBits(ChainsCoverage runCoverage) {
        boolean changed = false;
        if (runCoverage.getChainsCodeCounter().hasNonZeros()) {
            for (int idx = 0; idx < COVERAGE_MAP_SIZE; idx++) {
                int before = this.chainsCodeCounter.getAtIndex(idx);
                int after = before | hob_chains(runCoverage.getChainsCodeCounter().getAtIndex(idx));
                if (after != before) {
                    this.chainsCodeCounter.setAtIndex(idx, after);
                    changed = true;
                }
            }
        }
        return changed;
    }

    /** Returns a hash code of the edge counts in the coverage map. */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public int nonZeroHashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        // return super.toString();
        StringBuffer sb = new StringBuffer();

        if (this.counter.hasNonZeros()){
            sb.append("Coverage counts: \n");
            for (int i = 0; i < counter.size(); i++) {
                if (counter.getAtIndex(i) == 0) {
                    continue;
                }
                sb.append(i);
                sb.append("->");
                sb.append(counter.getAtIndex(i));
                sb.append('\n');
            }
        }

        if (this.chainsCodeCounter.hasNonZeros()){
            if (this.chainPaths.isEmpty()) {
                this.chainPaths = new ChainsCoverage(ChainsCoverageGuidance.chainPaths).chainPaths;
            }
            sb.append("Coverage Chains counts: \n");
            for (int i = 0; i < chainsCodeCounter.size(); i++) {
                if (chainsCodeCounter.getAtIndex(i) == 0) {
                    continue;
                }
                sb.append(i);
                sb.append("->");
                sb.append(this.chainPaths.get(i));
                sb.append('\n');
            }
        }

        return sb.toString();
    }

}
