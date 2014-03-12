package concurrent.examples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;

import concurrent.util.Forker;
import concurrent.util.Joiner;

/**
 * A simple string reverser based on split-merger.
 * 
 * @author U0121670
 */
public class StringReversingUsingForkJoiner implements Forker<String, String>,
        Joiner<String, String> {
    /**
     *@serialField mergeResult a merge container
     */
    final List<String> mergeResult = new ArrayList<String>();

    /**
     * split method
     * 
     * @param request
     *            - a input
     * @param completionService
     *            for split
     */
    public void fork(String request,
            final CompletionService<String> completionService)
            throws ExecutionException {
        String reversedRequest = new StringBuffer(request.toString()).reverse()
                .toString();
        for (int i = 0; i < reversedRequest.length(); i++) {
            TestCallable callable = new TestCallable(String
                    .valueOf(reversedRequest.charAt(i)));
            completionService.submit(callable);
        }
    }

    /**
     * Merger's clear
     */
    public void clear() {
        mergeResult.clear();
    }

    /**
     * Merger's getResult
     */
    public String getResult() throws ExecutionException {
        Collections.sort(mergeResult);
        StringBuffer buff = new StringBuffer();
        for (Object value : mergeResult)
            buff.append(value);
        return buff.reverse().toString();
    }

    /**
     * merge an interim result
     */
    public void join(String result) throws ExecutionException {
        mergeResult.add(result);
    }
}

class TestCallable implements Callable<String> {
    String value;

    public TestCallable(String value) {
        this.value = value;
    }

    public String call() throws Exception {
        return value;
    }
}
