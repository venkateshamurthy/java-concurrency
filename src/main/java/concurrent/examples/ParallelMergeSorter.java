package concurrent.examples;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

import lombok.ToString;


@SuppressWarnings("serial")
@ToString
public class ParallelMergeSorter {

  public static void main(String [] args) throws InterruptedException {
    Random random = new Random();
    long[][] times= new long[50][3];
    for(int i=0;i<times.length;i++,TimeUnit.MILLISECONDS.sleep(1)) {
      int[] array = random.ints(100000,1,1000_000).toArray();
      int result[] = new int[array.length];
      int[] copy = Arrays.copyOf(array, array.length);
      
      long t2=System.nanoTime();
      ForkJoinPool.commonPool().invoke(new Sorter(array, result));
      long t3=System.nanoTime();
      Arrays.sort(copy);
      long t1=System.nanoTime();
      times[i][0]=Arrays.equals(result, copy)?1:0;
      times[i][1]=t1-t3;
      times[i][2]=t3-t2;
    }
    boolean equals = Arrays.stream(times).map(t->t[0]==1?true:false).reduce(true, (a,b)->a&&b);
    long serialAvg = Arrays.stream(times).map(t->t[1]).reduce(0L, (a,b)->a+b)/times.length;
    long parallelAvg = Arrays.stream(times).map(t->t[2]).reduce(0L, (a,b)->a+b)/times.length;
    System.out.println("Equals= "+equals+" serial(ms):"+serialAvg/1000_000+" parallel(ms):"+parallelAvg/1000_000);
  }

  /**
   * <pre>
   *  P-MERGE(T, p1, r1, p2, r2, A, p3)
      1 n1 = r1 - p1 + 1
      2 n2 = r2 - p2 + 1
      3 if n1 < n2 // ensure that n1 >= n2
      4    exchange p1 with p2
      5    exchange r1 with r2
      6    exchange n1 with n2
      7 if n1 == 0 // both empty?
      8    return
      9 else q1 = (p1 + r1)/2
     10      q2 = BINARY-SEARCH(T[q1], T, p2, r2)
     11      q3 = p3 + (q1 - p1) + (q2 - p2)
     12      A[q3] = T[q1]
     13      spawn P-MERGE(T, p1, q1 - 1, p2, q2 - 1, A, p3)
     14      P-MERGE(T, q1 + 1, r1, q2, r2, A, q3 + 1)
     15      sync
   * </pre>
   * 
   * @author murthyv
   */
  static class Merger extends RecursiveAction {
    final int[] T, A;
    int p1, r1, p2, r2, p3;
    
    public Merger(int[] T, int[] A) {
      this(T, 0, ((T.length-1)/2),((T.length-1)/2)+1,T.length - 1, A, 0);
    }
  
    public Merger(int[] T, int p1, int r1, int p2, int r2, int[] A, int p3) {
      super();
      this.T = T;
      this.p1 = p1;
      this.r1 = r1;
      this.p2 = p2;
      this.r2 = r2;

      this.A = A;
      this.p3 = p3;
    }
  
    public void compute() {
      int n1=r1-p1+1;
      int n2=r2-p2+1;
      if (n1 < n2) {// ensure that n1 >= n2
        if(p1!=p2) {p1 = p1 ^ p2; p2 = p1 ^ p2; p1 = p1 ^ p2;}
        if(r1!=r2) {r1 = r1 ^ r2; r2 = r1 ^ r2; r1 = r1 ^ r2;}
        if(n1!=n2) {n1 = n1 ^ n2; n2 = n1 ^ n2; n1 = n1 ^ n2;}
      }
      if (n1==0)// both empty? why? because n1>=n2 therefore n2 also must be <=0
        return;
      
      int q1 = p1+(r1-p1)/2;
      int q2 = Arrays.binarySearch(T, p2, r2+1, T[q1]); q2 = q2 < 0 ? ~q2 : q2;
      int q3 = p3 + (q1 - p1) + (q2 - p2);
      A[q3] = T[q1];
      ForkJoinTask<Void> left = new Merger(T, p1, q1-1, p2, q2-1, A, p3).fork();
      new Merger(T, q1+1, r1, q2, r2, A, q3 + 1).compute();
      left.join();
    }
  }
  
  /**
   * <pre>
    P-MERGE-SORT(A, p, r, B, s)
    1 n = r - p + 1
    2 if n == 1
    3    B[s] = A[p]
    4 else let T [1::n] be a new array
    5    q = (p + r)/2
    6    q`= q - p + 1
    7    spawn P-MERGE-SORT(A, p, q, T, 1)
    8    P-MERGE-SORT(A, q + 1, r, T, q` + 1)
    9    sync
    10   P-MERGE(T, 1, q`, q1 + 1, n, B, s)
  </pre>
  */
  static class Sorter extends RecursiveAction {
    final int[] A, B;
    final int p, r;
    final boolean srcToDest;
    
    public Sorter(int[] a, int[] b) {
      this(a,0,a.length-1,b,true);
    }
    
    public Sorter(int[] a, int p, int r, int[] b, boolean srcToDest) {
      super();
      A = a;
      this.p = p;
      this.r = r;
      B = b;
      this.srcToDest=srcToDest;
    }
    
    @Override
    protected void compute() {
      if(p>r)
        return;
      
      if(r-p>10) {
        int q= p+(r-p)/2;
        ForkJoinTask<Void>  left =
            new Sorter(A, p,   q, B, !srcToDest).fork();
            new Sorter(A, q+1, r, B, !srcToDest).compute();
        left.join();
        new Merger(srcToDest?A:B, p, q, q+1, r, srcToDest?B:A, p).compute();
      } else {
        System.arraycopy(srcToDest?A:B, p, srcToDest?B:A, p, r+1-p);
        Arrays.sort(srcToDest?B:A, p, r+1);
      }
    }
  }
}
