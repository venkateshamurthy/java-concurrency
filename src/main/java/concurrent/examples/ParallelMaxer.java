package concurrent.examples;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

class ParallelMaxer {

  List<Student> students;

  ParallelMaxer(List<Student> list) {
    this.students = list;
  }

  double find() {
    MaxFinderTask mft = new MaxFinderTask(students);
    ForkJoinPool.commonPool().invoke(mft);
    return mft.result;
  }

  class MaxFinderTask extends RecursiveAction {
    protected static final int SEQUENTIAL_THRESHOLD = 5;

    List<Student> students;
    double result;

    public MaxFinderTask(List<Student> list) {
      this.students = list;
    }

    protected void compute() {
      int n = students.size();
      if (n < SEQUENTIAL_THRESHOLD) {
        result = students.stream().map(s -> s.score).reduce(Double.MIN_VALUE, (a, b) -> Math.max(a, b));
      } else {
        int m = n / 2;
        MaxFinderTask left = new MaxFinderTask(students.subList(0, m));
        MaxFinderTask right = new MaxFinderTask(students.subList(m, n));
        left.fork();
        right.compute();
        left.join();
        result = Math.max(left.result, right.result);
      }
    }
  }

  class Student {
    String name;
    int gradYear;
    double score;
  }
}


class IncrementTask extends RecursiveAction {
  final long[] array; final int lo, hi;
  IncrementTask(long[] array, int lo, int hi) {
    this.array = array; this.lo = lo; this.hi = hi;
  }
  protected void compute() {
    if (hi - lo < 10) {
      for (int i = lo; i < hi; ++i)
        array[i]++;
    }
    else {
      int mid = (lo + hi) >>> 1;
      invokeAll(new IncrementTask(array, lo, mid),
                new IncrementTask(array, mid, hi));
    }
  }
}