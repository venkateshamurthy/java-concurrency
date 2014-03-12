package concurrent.examples;

import java.io.File;
import java.util.Comparator;

public class GrepResultObject implements Comparable<GrepResultObject>, Comparator<GrepResultObject> {
    File   file;
    int    lineNo;
    String line;

    public GrepResultObject(File file, int lineNo, String line) {
        super();
        this.file = file;
        this.lineNo = lineNo;
        this.line = line;
    }

    @Override
    public String toString() {
        return file + ":" + lineNo + ":" + line;
    }

    @Override
    public int hashCode() {
        return file.hashCode() + lineNo + line.hashCode();
    }

    public int compareTo(GrepResultObject o) {
        return compare(o, this);
    }

    public int compare(GrepResultObject o1, GrepResultObject o2) {
        GrepResultObject gro1 = o1;
        GrepResultObject gro2 = o2;
        if (gro1.hashCode() < gro2.hashCode())
            return -1;
        else if (gro1.hashCode() > gro2.hashCode())
            return 1;
        else
            return 0;
    }
}
