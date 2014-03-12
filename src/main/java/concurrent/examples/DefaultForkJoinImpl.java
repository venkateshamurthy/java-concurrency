package concurrent.examples;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;

import concurrent.util.Forker;
import concurrent.util.Joiner;

public class DefaultForkJoinImpl<R, V, T> implements Forker<R, V>, Joiner<V, T> {
    public void fork(R request, CompletionService<V> completionService)
            throws ExecutionException {
    }

    public void clear() {
    }

    public T getResult() throws ExecutionException {
        return null;
    }

    public void join(V callableResult) throws ExecutionException {
    }
}
