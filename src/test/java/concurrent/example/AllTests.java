package concurrent.example;

import org.junit.runner.RunWith;

import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( { SynchronizedVsLockTest.class,TestProducerConsumer.class, TransactionalCompletionServiceTest.class, BoundedTransactionalCompletionServiceTest.class,
        ExecutorCompletionServiceTest.class, RequestOrderExecutorCompletionServiceTest.class,
        BoundedCompletionServiceTest.class  })
public class AllTests {
    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite = new junit.framework.TestSuite();
        suite.addTest(TransactionalCompletionServiceTest.suite());
        suite.addTest(BoundedTransactionalCompletionServiceTest.suite());
        suite.addTest(ExecutorCompletionServiceTest.suite());
        suite.addTest(RequestOrderExecutorCompletionServiceTest.suite());
        suite.addTest(BoundedCompletionServiceTest.suite());
        return suite;
    }
}
