package xyz.xzaslxr.utils.coverage;

import edu.berkeley.cs.jqf.fuzz.util.*;
import edu.berkeley.cs.jqf.instrument.tracing.events.*;
import org.eclipse.collections.api.iterator.IntIterator;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;

import java.util.Map;
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

    protected Map<Integer, String> chainPaths = new ConcurrentHashMap<>();

    public ChainsCoverage() {
    }

    public ChainsCoverage(Map<Integer, String> chainPaths) {
        this.chainPaths = chainPaths;
    }

    public Counter getChainsCodeCounter() {
        return chainsCodeCounter;
    }

    /**
     * <p>
     * handleEvent会执行applyVisitor，applyVisitor将会调用TraceEventVisitor中各类visitor函数，
     * 如对于 CallEvent，会调用 visitCallEvent。
     * </p>
     * 
     * @param e
     */
    public void handleEvent(TraceEvent e) {
        e.applyVisitor(this);
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
            return ((BranchEvent) traceEvent).getContainingMethodName();
        } else {
            return null;
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
        if (this.chainPaths.containsKey(getEventMethodName(e).hashCode())) {
            chainsCodeCounter.increment(getEventMethodName(e).hashCode());
        }
    }

    /**
     * 处理 BranchEvent
     * 
     * @param b
     */
    @Override
    public void visitBranchEvent(BranchEvent b) {
        super.visitBranchEvent(b);

        // hashCodeCounter 中添加 invokedMethodName.hashCode
        if (this.chainPaths.containsKey(getEventMethodName(b).hashCode())) {
            this.chainsCodeCounter.increment(getEventMethodName(b).hashCode());
        }
    }

    /**
     * this对象为: runCoverage，需要计算出触发的新函数
     * 
     * @param baseline
     * @return
     */
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
                int after = before | hob(runCoverage.getChainsCodeCounter().getAtIndex(idx));
                if (after != before) {
                    this.chainsCodeCounter.setAtIndex(idx, after);
                    changed = true;
                }
            }
        }
        return changed;
    }

    /**
     * 获得 非0的空间大小
     * 
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
}
