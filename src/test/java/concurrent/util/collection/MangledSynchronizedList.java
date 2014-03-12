package concurrent.util.collection;
import java.io.*;
import java.util.*;

import util.NullOutputStream;

public class MangledSynchronizedList {
  public static void main(String[] args) {
    final List<String> synchList = Collections.synchronizedList(
        new ArrayList<String>());
    Collections.addAll(synchList, "hello", "world");
    Thread tester = new Thread() {
      { setDaemon(true); }
      public void run() {
        while (true) {
          synchList.add("hey there");
          synchList.remove(2);
        }

      }
    };
    tester.start();

    while (true) {
      try {
        ObjectOutputStream out = new ObjectOutputStream(
            new NullOutputStream()
        );
        for (int i = 0; i < 100 * 1000; i++) {
        	if(i%10==0)System.out.println(i);
          out.writeObject(synchList);
        }
        out.close();
      } catch (IOException e) {
        e.printStackTrace();
        break;
      }
    }
  }
}