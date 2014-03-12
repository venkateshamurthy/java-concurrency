package concurrent.example;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Arrays;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import concurrent.examples.DefaultForkJoinImpl;
import concurrent.examples.DirectoryGrepUsingForkJoiner;
import concurrent.examples.FileSizerUsingForkJoiner;
import concurrent.examples.StringReversingUsingForkJoiner;
import concurrent.util.BoundedTransactionalCompletionService;
import concurrent.util.RequestOrderExecutorCompletionService;
import concurrent.util.SimpleForkJoiner;
import concurrent.util.SimpleThreadFactory;
import concurrent.util.SimpleTransactionalCompletionService;
import concurrent.util.TransactionalCompletionService;

public class ForkJoinTests extends BaseTestCase {
    private static final String SEARCH_STRING      = "CompletionService";
    private static final String INPUT_FOLDER       = System
                                                           .getProperty(
                                                                   "INPUT_FOLDER",
                                                                   "c:/vmurthy/workspace");
    static String[]             expectedStringArray;
    ExecutorService             e                  = null;
    TransactionalCompletionService   completionService  = null;
    SimpleForkJoiner          forkJoiner = null;

    @Override
    @Before
    public void setUp() {
    	//SimpleThreadFactory.setDaemon(true);
        e = Executors.newCachedThreadPool(SimpleThreadFactory.DEFAULT);
        completionService = new SimpleTransactionalCompletionService(
                new ExecutorCompletionService(e));
        forkJoiner = new SimpleForkJoiner(
                completionService);
    }

    @BeforeClass
    public static void beforeClass() {
        try {
            System.out.println("Building up expected data for all tests...");
            expectedStringArray = new DirectoryGrepUsingForkJoiner(SEARCH_STRING,
                    new FileFilter() {
                        public boolean accept(File pathname) {
                            return pathname.getName().endsWith("java");
                        }
                    }).createExpectedGrepResultArray(new File(INPUT_FOLDER));
            System.out
                    .println("Expected data build up done...will now start executing tests");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    @After
    public void tearDown() {
        completionService.endTransaction();
        forkJoiner.shutdown();
        joinPool(e);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void forkJoinReverseString() throws Exception {
        String searchParam = "abcdefg";
        StringReversingUsingForkJoiner stringReverser = new StringReversingUsingForkJoiner();
        String result = (String) forkJoiner.execute(searchParam,
                stringReverser, stringReverser);
        Assert.assertEquals(new StringBuffer(searchParam).reverse().toString(),
                result);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = ExecutionException.class)
    public void forkJoinErrorCheckingSplitterException()
            throws ExecutionException {
        String searchParam = "abcdefg";
        DefaultForkJoinImpl<String, Void, Void> errorSPM = new DefaultForkJoinImpl<String, Void, Void>() {
            @Override
            public void fork(String request, CompletionService<Void> cs)
                    throws ExecutionException {
                throw new ExecutionException("Forker is in error", null);
            }
        };
        Void result = (Void) forkJoiner.execute(searchParam, errorSPM,
                errorSPM);
        System.out.println("Final result=" + result);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = ExecutionException.class)
    public void forkJoinErrorCheckingMergeException()
            throws ExecutionException {
        String searchParam = "abcdefg";
        DefaultForkJoinImpl<String, String, Void> errorSPM = new DefaultForkJoinImpl<String, String, Void>() {
            @Override
            public void fork(String request, CompletionService<String> cs)
                    throws ExecutionException {
                cs.submit(new StringTask());
            }

            @Override
            public void join(String str) throws ExecutionException {
                throw new ExecutionException("Joining is error", null);
            }
        };
        Void result = (Void) forkJoiner.execute(searchParam, errorSPM,
                errorSPM);
        System.out.println("Final result=" + result);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = ExecutionException.class)
    public void forkJoinErrorCheckingMergeResultException()
            throws ExecutionException {
        String searchParam = "abcdefg";
        DefaultForkJoinImpl<String, String, String> errorSPM = new DefaultForkJoinImpl<String, String, String>() {
            @Override
            public void fork(String request, CompletionService<String> cs)
                    throws ExecutionException {
                cs.submit(new StringTask());
            }

            @Override
            public void join(String str) throws ExecutionException {
            }

            @Override
            public String getResult() throws ExecutionException {
                throw new ExecutionException("Joined result", null);
            }
        };
        Void result = (Void) forkJoiner.execute(searchParam, errorSPM,
                errorSPM);
        System.out.println("Final result=" + result);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFileSizesInDirectories() throws ExecutionException {
        FileSizerUsingForkJoiner fileSizerSPM = new FileSizerUsingForkJoiner();
        File inputFolder = new File(INPUT_FOLDER);
        if (!inputFolder.exists()) return;
        // long t1 = System.nanoTime();
        // long exactSize = FileUtils.sizeOfDirectory(inputFolder);
        long t2 = System.nanoTime();
        Long computedSize = (Long) forkJoiner.execute(inputFolder,
                fileSizerSPM, fileSizerSPM);
        long t3 = System.nanoTime();
        System.out
                .println(/* "FileUtils.size(time)=" + (t2 - t1) + */" SplitMergeBased.Size(time)="
                        + (t3 - t2));
        // Assert.assertEquals(exactSize, computedSize.longValue());
        System.out.println("Total directory size=" + computedSize);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGrepDirectory() throws ExecutionException, IOException {
        final File inputFolder = new File(INPUT_FOLDER);
        if (!inputFolder.exists()) return;
        FileFilter filter = new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().endsWith("java");
            }
        };
        DirectoryGrepUsingForkJoiner directoryGrepSPM = new DirectoryGrepUsingForkJoiner(
                SEARCH_STRING, filter);
        // Get the expected object
        long t1 = System.nanoTime();
        String[] exactStringArray = expectedStringArray;// directoryGrepSPM.createExpectedGrepResultArray(inputFolder);
        long t2 = System.nanoTime();
        // compute using split merge
        String[] computedStringArray = (String[]) forkJoiner.execute(
                inputFolder, directoryGrepSPM, directoryGrepSPM);
        long t3 = System.nanoTime();
        assertArrayEqualsAfterSorting(exactStringArray, computedStringArray);
        for (String s : computedStringArray)
            System.out.print(s);
        System.out.println("Grep.grepDirectory(time)=" + (t2 - t1)
                + " ForkJoinBasedGrep(time)=" + (t3 - t2));
    }

    /**
     * A Custom assertion method for comparing array equals after sorting them.
     * The object arrays must contain objects that have implemented Comparator
     * interface
     * 
     * @param c1
     * @param c2
     */
    private static void assertArrayEqualsAfterSorting(Object[] c1, Object[] c2) {
        Arrays.sort(c1);
        Arrays.sort(c2);
        Assert.assertArrayEquals(c1, c2);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGrepDirectoryUsingTransactionalRequestOrderCS() throws ExecutionException,
            IOException {
        final File inputFolder = new File(INPUT_FOLDER);
        if (!inputFolder.exists()) return;
        FileFilter filter = new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().endsWith("java");
            }
        };
        completionService = new SimpleTransactionalCompletionService(
                new RequestOrderExecutorCompletionService(e));
        forkJoiner = new SimpleForkJoiner(
                completionService);
        DirectoryGrepUsingForkJoiner directoryGrepSPM = new DirectoryGrepUsingForkJoiner(
                SEARCH_STRING, filter);
        // Get the expected object
        long t1 = System.nanoTime();
        String[] exactStringArray = expectedStringArray;// directoryGrepSPM.createExpectedGrepResultArray(inputFolder);
        long t2 = System.nanoTime();
        // compute using split merge
        String[] computedStringArray = (String[]) forkJoiner.execute(
                inputFolder, directoryGrepSPM, directoryGrepSPM);
        long t3 = System.nanoTime();
        assertArrayEqualsAfterSorting(exactStringArray, computedStringArray);
        System.out.println("Grep.grepDirectory(time)=" + (t2 - t1)
                + " ForkJoinBasedGrep(time)=" + (t3 - t2));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGrepDirectoryUsingBoundedTransactionalRequestOrderCS()
            throws ExecutionException, IOException {
        // Some test method specific inputs
        final File inputFolder = new File(INPUT_FOLDER);
        if (!inputFolder.exists()) return;
        final FileFilter filter = new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().endsWith("java");
            }
        };
        completionService = new BoundedTransactionalCompletionService(
                new SimpleTransactionalCompletionService(
                        new RequestOrderExecutorCompletionService(e)), 5);
        forkJoiner = new SimpleForkJoiner(
                completionService);
        DirectoryGrepUsingForkJoiner directoryGrepSPM = new DirectoryGrepUsingForkJoiner(
                SEARCH_STRING, filter);
        // Get the expected object
        long t1 = System.nanoTime();
        String[] exactStringArray = expectedStringArray;// directoryGrepSPM.createExpectedGrepResultArray(inputFolder);
        long t2 = System.nanoTime();
        // compute using split merge
        String[] computedStringArray = (String[]) forkJoiner.execute(
                inputFolder, directoryGrepSPM, directoryGrepSPM);
        long t3 = System.nanoTime();
        assertArrayEqualsAfterSorting(exactStringArray, computedStringArray);
        System.out.println("Grep.grepDirectory(time)=" + (t2 - t1)
                + " ForkJoinBasedGrep(time)=" + (t3 - t2));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGrepDirectoryUsingBoundedTransactionalCS()
            throws ExecutionException, IOException {
        final File inputFolder = new File(INPUT_FOLDER);
        if (!inputFolder.exists()) return;
        final FileFilter filter = new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().endsWith("java");
            }
        };
        completionService = new BoundedTransactionalCompletionService(
                new SimpleTransactionalCompletionService(
                        new RequestOrderExecutorCompletionService(e)), 5);
        forkJoiner = new SimpleForkJoiner(
                completionService);
        DirectoryGrepUsingForkJoiner directoryGrepSPM = new DirectoryGrepUsingForkJoiner(
                SEARCH_STRING, filter);
        // Get the expected object
        long t1 = System.nanoTime();
        String[] exactStringArray = expectedStringArray;// directoryGrepSPM.createExpectedGrepResultArray(inputFolder);
        long t2 = System.nanoTime();
        // compute using split merge
        String[] computedStringArray = (String[]) forkJoiner.execute(
                inputFolder, directoryGrepSPM, directoryGrepSPM);
        long t3 = System.nanoTime();
        // Here u need to use after sort only since executor cs puts itout of
        // order
        assertArrayEqualsAfterSorting(exactStringArray, computedStringArray);
        System.out.println("Grep.grepDirectory(time)=" + (t2 - t1)
                + " ForkJoinBasedGrep(time)=" + (t3 - t2));
    }

    static class ueh implements UncaughtExceptionHandler {
        Exception ex;

        public void uncaughtException(Thread t, Throwable e) {
            ex = (Exception) e;
        }

        public Exception ex() {
            return ex;
        }
    }

    @SuppressWarnings("unchecked")
    @Test(expected = ExecutionException.class)
    public void testCancelOnGrepDirectoryUsingBoundedTransactionalCS()
            throws ExecutionException, IOException {
        final File inputFolder = new File(INPUT_FOLDER);
        if (!inputFolder.exists())
            throw new ExecutionException(
                    "Test for cancelGrep failed due to Directory not found",
                    null);
        final FileFilter filter = new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().endsWith("java");
            }
        };
        completionService = new BoundedTransactionalCompletionService(
                new SimpleTransactionalCompletionService(
                        new RequestOrderExecutorCompletionService(e)), 5);
        forkJoiner = new SimpleForkJoiner(
                completionService);
        DirectoryGrepUsingForkJoiner directoryGrepSPM = new DirectoryGrepUsingForkJoiner(
                SEARCH_STRING, filter);
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                forkJoiner.cancel();
            }
        };
        ueh u = new ueh();
        // Get the expected object
        Thread.setDefaultUncaughtExceptionHandler(u);
        t.start();
        long t2 = System.nanoTime();
        String[] computedStringArray = (String[]) forkJoiner.execute(
                inputFolder, directoryGrepSPM, directoryGrepSPM);
        long t3 = System.nanoTime();
        System.out.println("ueh=" + u.ex());
        for (String s : computedStringArray)
            System.out.print(s);
        // Here u need to use after sort only since executor cs puts itout of
        // order
        System.out.println(" ForkJoinBasedGrep(time)=" + (t3 - t2));
    }

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(ForkJoinTests.class);
    }
}
