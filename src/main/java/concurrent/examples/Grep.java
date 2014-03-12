package concurrent.examples;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Grep {
    // Charset and decoder for ISO-8859-15
    private static Charset        charset     = Charset.forName("ISO-8859-15");
    private static CharsetDecoder decoder     = charset.newDecoder();
    // Pattern used to parse lines
    private static Pattern        linePattern = Pattern.compile(".*\r?\n");
    // The input pattern that we're looking for
    private Pattern               pattern;

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    // Compile the pattern from the command line
    //
    public Grep(String pat) {
        try {
            pattern = Pattern.compile(pat);
        } catch (PatternSyntaxException x) {
            System.err.println(x.getMessage());
            System.exit(1);
        }
    }

    // Use the linePattern to break the given CharBuffer into lines, applying
    // the input pattern to each line to see if we have a match
    //
    private List<GrepResultObject> grep(File f, CharBuffer cb) {
        List<GrepResultObject> list = new ArrayList<GrepResultObject>();
        Matcher lm = linePattern.matcher(cb); // Line matcher
        Matcher pm = null; // Pattern matcher
        int lines = 0;
        while (lm.find()) {
            lines++;
            CharSequence cs = lm.group(); // The current line
            if (pm == null)
                pm = pattern.matcher(cs);
            else
                pm.reset(cs);
            if (pm.find()) list.add(new GrepResultObject(f, lines, cs.toString()));
            if (lm.end() == cb.limit()) break;
        }
        return list;
    }

    public List<GrepResultObject> grep(File f, FileFilter filter) throws IOException {
        return filter.accept(f) ? grep(f) : Collections.<GrepResultObject> emptyList();
    }

    // Search for occurrences of the input pattern in the given file
    //
    public List<GrepResultObject> grep(File f) throws IOException {
        // Open the file and then get a channel from the stream
        FileInputStream fis = new FileInputStream(f);
        FileChannel fc = fis.getChannel();
        // Get the file's size and then map it into memory
        int sz = (int) fc.size();
        MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);
        // Decode the file into a char buffer
        CharBuffer cb = decoder.decode(bb);
        // Perform the search
        List<GrepResultObject> list = grep(f, cb);
        // Close the channel and the stream
        fc.close();
        return list;
    }

    List<GrepResultObject> listFromDirectories = new ArrayList<GrepResultObject>();

    public List<GrepResultObject> grepDirectory(File f, FileFilter filter) throws IOException {
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            for (File file : files)
                if (file.isDirectory())
                    grepDirectory(file, filter);
                else
                    listFromDirectories.addAll(grep(file, filter));
        }
        return listFromDirectories;
    }

    public List<GrepResultObject> grepDirectory(File f) throws IOException {
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            for (File file : files)
                if (file.isDirectory())
                    grepDirectory(file);
                else
                    listFromDirectories.addAll(grep(file));
        }
        return listFromDirectories;
    }

}
