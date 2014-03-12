package concurrent.examples;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;

import concurrent.util.Forker;
import concurrent.util.Joiner;
public class FileSizerUsingForkJoiner implements Forker<File, Long>,
        Joiner<Long, Long> {
    Long totalSize = 0L;

    public void fork(File request,
            final CompletionService<Long> completionService)
            throws ExecutionException {
        File file = request;
        File[] files = file.listFiles();
        for (final File f : files)
            if (f.isDirectory())
                fork(f, completionService); // recursive splitting
            else
                completionService.submit(new Callable<Long>() {
                    public Long call() throws Exception {
                        return f.length();
                    }
                });
    }
    public void clear() {
        totalSize = 0L;
    }

    public Long getResult() throws ExecutionException {
        return totalSize;
    }

    public void join(Long result) throws ExecutionException {
        totalSize += result;
    }
}
