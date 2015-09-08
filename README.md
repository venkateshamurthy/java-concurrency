[![Coverage Status](https://coveralls.io/repos/venkateshamurthy/java-concurrency/badge.svg)](https://coveralls.io/r/venkateshamurthy/java-concurrency)

This project is all about my own experiments to understand java concurrency utilities.
Some libraries used of interest are lombok.

Package concurrent.util.contextual
==================================
This package has the following flavours to deal with passing a context associated with Runnable to a Thread in thread pool.

ContextalThreadPoolExecutor
---------------------------
It has other associated classes to wrap thread, runnable with contextual flavour. this basically sets a ContextualRunnable's context to a thread in the beforeExecute method.

A sample test use:

```java
    
    private static final String context = "User:murthy";
    @Test
    public void test() {
        ContextualThreadPoolExecutor<String> ctp= ContextualThreadPoolExecutor.newFixedThreadPool(10);
        Runnable runnable = new Runnable(){
            @Override
            public void run() {
                ContextualThread<String> ct;
                if((ct=ContextualThread.current()) !=null){
                    log.info("From {}.run method,expected-context:{} got-context:{}",ct.getName(),newContext,ct.getContext());
                    Assert.assertEquals("Context "+ct.getContext(),newContext, ct.getContext());    
                }
            }
        };
        //ctp.submit(runnable);
        ContextualRunnable<String> ctxRunnable = ContextualRunnable.make(newContext,runnable);
        ctp.submit(ctxRunnable);
        
        try {
            TimeUnit.SECONDS.sleep(2);
            ctp.shutdown();
            ctp.awaitTermination(-1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Assert.assertNull(ctxRunnable.getThrowable());
    }
```
MDCCallable and MDCRunnable
---------------------------
This assumes MDC from Slf4j as the means to capture per-thread specific context. Akin to memento; the context at the time of creation of runnable/callable will be set during execution by the thread and unset before the run/call finishes.

A sample test method reproduced from src/test/java.

```java

    @Slf4j
    public class MDCExecutorTest {
    ExecutorService es;
    Map<String, String> context = Collections.singletonMap("User", "Murthy");
    @Test
    public void testRunnable() {
        final CyclicBarrier cb = new CyclicBarrier(2); // since only one more
                                                        // thread apart from
                                                        // main
        Runnable runner = new Runnable() {
            @Override
            public void run() {
                log.info(MDC.getCopyOfContextMap().toString());
                try {
                    cb.await();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Assert.assertTrue(context.equals(MDC.getCopyOfContextMap()));
            }
        };
        MDCRunnable mdcRunner = MDCRunnable.of(context, runner);
        es.submit(mdcRunner);
        try {
            cb.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertNull(mdcRunner.getThrowable());
    }
    @Before
    public void before() {
        es = Executors.newFixedThreadPool(10);
    }

    @After
    public void after() {
        try {
            es.shutdown();
            es.awaitTermination(-1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
```