package concurrent.util;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;

public interface Forker<Task, FinalResult> {
	void fork(Task task, CompletionService<FinalResult> completionService)
	throws ExecutionException;
}