package nanali.test_basic;

import java.util.*;

/**
 * @author lina
 * @date 2019-04-09 14:25
 */

public class test_topK<E extends Comparable> {
    private PriorityQueue<E> queue;
    private int maxSize; //堆的最大容量

    public test_topK(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalStateException();
        }
        this.maxSize = maxSize;
        this.queue = new PriorityQueue<>(maxSize, new Comparator<E>() {
            @Override
            public int compare(E o1, E o2) {
                // 最大堆用o2 - o1，最小堆用o1 - o2
                return (o1.compareTo(o2));
            }
        });
    }

    public void add(E e) {
        if (queue.size() < maxSize) {
            queue.add(e);
        } else {
            E peek = queue.peek();
            if (e.compareTo(peek) > 0) {
                queue.poll();
                queue.add(e);
            }
        }
    }

    public List<E> sortedList() {
        List<E> list = new ArrayList<>(queue);
        Collections.sort(list);
        return list;
    }

    public static void main(String[] args) {
        int[] array = {4, 5, 1, 6, 2, 7, 3, 8};
        test_topK pq = new test_topK(4);
        for (int n : array) {
            pq.add(n);
        }
        System.out.println(pq.sortedList());
    }
}