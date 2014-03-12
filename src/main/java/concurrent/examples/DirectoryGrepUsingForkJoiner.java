package concurrent.examples;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import concurrent.util.Forker;
import concurrent.util.Joiner;
public class DirectoryGrepUsingForkJoiner implements
        Forker<File, List<GrepResultObject>>,
        Joiner<List<GrepResultObject>, String[]> {
    final String     stringToSearch;
    final FileFilter javaFilesFilter;
    List<String>     resultList = new ArrayList<String>();

    public DirectoryGrepUsingForkJoiner(String string, FileFilter filter) {
        stringToSearch = string;
        javaFilesFilter = filter;
    }

    public void fork(File request,
            CompletionService<List<GrepResultObject>> completionService)
            throws ExecutionException {
        File file = request;
        File[] files = file.listFiles();
        try {
            for (final File f : files)
                if (f.isDirectory())
                    fork(f, completionService); // recursive splitting
                else
                    completionService
                            .submit(new Callable<List<GrepResultObject>>() {
                                public List<GrepResultObject> call() {
                                    try {
                                        return new Grep(stringToSearch).grep(f,
                                                javaFilesFilter);
                                    } catch (IOException e) {
                                        throw new RejectedExecutionException(
                                                "Grep interrupted by cancel", e);
                                    }
                                }
                            });
        } catch (Throwable e) {
            ExecutionException ex = new ExecutionException(e);
            throw ex;
        }
    }

    public void clear() {
        resultList.clear();
    }

    public String[] getResult() throws ExecutionException {
        String[] stringArray = new String[1];
        return resultList.toArray(stringArray);
    }
    public void join(List<GrepResultObject> callableResult)
            throws ExecutionException {
        for (GrepResultObject o : callableResult)
            resultList.add(o.toString());
    }

    public String[] createExpectedGrepResultArray(File inputFolder)
            throws IOException {
        if (!inputFolder.exists()) return new String[] { "" };
        List<GrepResultObject> exactGROList = new Grep(stringToSearch)
                .grepDirectory(inputFolder, javaFilesFilter);
        List<String> listStr = new ArrayList<String>();
        for (GrepResultObject o : exactGROList)
            listStr.add(o.toString());
        return listStr.toArray(new String[1]);
    }
}
