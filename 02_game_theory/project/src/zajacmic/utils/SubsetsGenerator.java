package zajacmic.utils;

import java.util.BitSet;
import java.util.Iterator;

public class SubsetsGenerator implements Iterator<BitSet> {
    private int n,k;
    private BitSet set;
    private int[] stack;
    private int stackPointer = 0;
    private boolean pendingSet = false;
    private boolean returnInitSet;

    public SubsetsGenerator(int n) {
        this.n = n;
        this.stack = new int[n];
        this.set = new BitSet(n);
    }

    public void reset(int k) {
        stackPointer = 0;
        set.clear();

        this.k = k;

        pendingSet = true;
        returnInitSet = true;
    }

    @Override
    public boolean hasNext() {
	    return pendingSet;
    }

    @Override
    public BitSet next() {

        if (returnInitSet) {

            for (int i = 0; i < k; i++) {
                set.set(i);
                stack[stackPointer++] = i;
            }

            returnInitSet = false;

        } else {

            int m = n;
            int toPush = 0;

            while ((stack[stackPointer - 1] + 1) == m) {
                set.clear(stack[stackPointer - 1]);
                toPush++;
                m--;
                stackPointer--;
            }

            set.clear(stack[stackPointer - 1]);
            stack[stackPointer - 1]++;
            set.set(stack[stackPointer - 1]);

            for (int i = 0, j = stack[stackPointer - 1] + 1; i < toPush; i++, j++) {
                stack[stackPointer] = j;
                set.set(j);
                stackPointer++;
            }
        }

        pendingSet = (stack[0] != (n - k));

        return set;
    }

    @Override
    public void remove() {
        System.out.println("Not implemented. Hire some Chinese guys or something ...");
    }
}
