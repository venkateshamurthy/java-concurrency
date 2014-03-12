package concurrent.example;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import concurrent.util.RequestOrderExecutorCompletionService;

public class ExecutorCompletionServiceLoops {
    static final int                        POOLSIZE = 100;
    static final ExecutorService            pool     = Executors.newFixedThreadPool(POOLSIZE);
    static final CompletionService<Integer> ecs      = new ExecutorCompletionService<Integer>(pool);
    static final CompletionService<Integer> requestOrderCS   = new RequestOrderExecutorCompletionService<Integer>(pool);
    static boolean                          print    = false;

    public static void main(String[] args) throws Exception {
        int max = 8;
        int base = 10000;
        if (args.length > 0) max = Integer.parseInt(args[0]);
        System.out.println("Warmup...Run with ExecutorCompletionService");
        oneTest(base, ecs);
        Thread.sleep(100);
        print = true;
        for (int i = 1; i <= max; i += i + 1 >>> 1) {
            System.out.print("n: " + i * base);
            oneTest(i * base, ecs);
            Thread.sleep(100);
        }
        System.out.println("Warmup...Run with RequestOrderCompletionService");
        oneTest(base, requestOrderCS);
        Thread.sleep(100);
        print = true;
        for (int i = 1; i <= max; i += i + 1 >>> 1) {
            System.out.print("n: " + i * base);
            oneTest(i * base, requestOrderCS);
            Thread.sleep(100);
        }
        pool.shutdown();
    }

    static class Task implements Callable<Integer> {
        public Integer call() {
            int l = System.identityHashCode(this);
            l = LoopHelpers.compute2(l);
            int s = LoopHelpers.compute1(l);
            l = LoopHelpers.compute2(l);
            s += LoopHelpers.compute1(l);
            return new Integer(s);
        }
    }

    static class Producer implements Runnable {
        final CompletionService<Integer> cs;
        final int                        iters;

        Producer(CompletionService<Integer> ecs, int i) {
            cs = ecs;
            iters = i;
        }

        public void run() {
            for (int i = 0; i < iters; ++i)
                cs.submit(new Task());
        }
    }

    static void oneTest(int iters, CompletionService<Integer> cs) throws Exception {
        long startTime = System.nanoTime();
        new Thread(new Producer(cs, iters)).start();
        int r = 0;
        for (int i = 0; i < iters; ++i)
            r += cs.take().get().intValue();
        long elapsed = System.nanoTime() - startTime;
        long tpi = elapsed / iters;
        if (print) System.out.println("\t: " + LoopHelpers.rightJustify(tpi) + " ns per task");
        if (r == 0) // avoid overoptimization
            System.out.println("useless result: " + r);
    }
}
