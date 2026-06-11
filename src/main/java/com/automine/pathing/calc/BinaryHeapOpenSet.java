package com.automine.pathing.calc;

/**
 * A binary min-heap keyed on {@link PathNode#combinedCost}, with O(log n) insert,
 * remove-min and decrease-key. Each node caches its heap index so decrease-key is cheap —
 * this is the same structure Baritone uses to keep A* fast over millions of nodes.
 */
public final class BinaryHeapOpenSet {

    private PathNode[] heap;
    private int size;

    public BinaryHeapOpenSet() {
        this(1024);
    }

    public BinaryHeapOpenSet(int initialCapacity) {
        this.heap = new PathNode[initialCapacity];
        this.size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void insert(PathNode node) {
        if (size >= heap.length - 1) {
            grow();
        }
        size++;
        heap[size] = node;
        node.heapPosition = size;
        siftUp(size);
    }

    /** Re-establish heap order after {@code node.combinedCost} decreased. */
    public void update(PathNode node) {
        siftUp(node.heapPosition);
    }

    public PathNode removeLowest() {
        PathNode result = heap[1];
        result.heapPosition = -1;
        PathNode last = heap[size];
        heap[size] = null;
        size--;
        if (size > 0) {
            heap[1] = last;
            last.heapPosition = 1;
            siftDown(1);
        }
        return result;
    }

    private void siftUp(int index) {
        PathNode node = heap[index];
        double cost = node.combinedCost;
        while (index > 1) {
            int parent = index >>> 1;
            PathNode parentNode = heap[parent];
            if (cost >= parentNode.combinedCost) {
                break;
            }
            heap[index] = parentNode;
            parentNode.heapPosition = index;
            index = parent;
        }
        heap[index] = node;
        node.heapPosition = index;
    }

    private void siftDown(int index) {
        PathNode node = heap[index];
        double cost = node.combinedCost;
        int half = size >>> 1;
        while (index <= half) {
            int child = index << 1;
            PathNode childNode = heap[child];
            int right = child + 1;
            if (right <= size && heap[right].combinedCost < childNode.combinedCost) {
                child = right;
                childNode = heap[right];
            }
            if (cost <= childNode.combinedCost) {
                break;
            }
            heap[index] = childNode;
            childNode.heapPosition = index;
            index = child;
        }
        heap[index] = node;
        node.heapPosition = index;
    }

    private void grow() {
        PathNode[] bigger = new PathNode[heap.length * 2];
        System.arraycopy(heap, 0, bigger, 0, heap.length);
        heap = bigger;
    }
}
