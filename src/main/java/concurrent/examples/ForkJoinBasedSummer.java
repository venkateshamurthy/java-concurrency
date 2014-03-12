package concurrent.examples;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ForkJoinBasedSummer extends RecursiveTask<Long> {
    static final ForkJoinPool fjPool = new ForkJoinPool();

    static final int SEQUENTIAL_THRESHOLD = 5000;

    int low;
    int high;
    int[] array;

    ForkJoinBasedSummer(int[] arr, int lo, int hi) {
        array = arr;
        low   = lo;
        high  = hi;
    }
    @Override
    protected Long compute() {
        if(high - low <= SEQUENTIAL_THRESHOLD) {
            long sum = 0;
            for(int i=low; i < high; ++i) 
                sum += array[i];
            return sum;
         } else {
            int mid = low + (high - low) / 2;
            ForkJoinBasedSummer left  = new ForkJoinBasedSummer(array, low, mid);
            ForkJoinBasedSummer right = new ForkJoinBasedSummer(array, mid, high);
            left.fork();
            long rightAns = right.compute();
            long leftAns  = left.join();
            return leftAns + rightAns;
         }
     }

     static long sumArray(int[] array) {
         return ForkJoinBasedSummer.fjPool.invoke(new ForkJoinBasedSummer(array,0,array.length));
     }
}