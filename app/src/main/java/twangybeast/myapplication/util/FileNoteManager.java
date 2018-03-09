package twangybeast.myapplication.util;

import android.util.Log;

import java.io.*;

/**
 * Created by Twangybeast on 2/21/2018.
 */

public class FileNoteManager
{
    public static void printAllFileContents(File file, String tag)
    {
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null)
            {
                Log.d(tag, line);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public static void delete(File root)
    {
        if (root.exists()) {
            if (root.isDirectory())
            {
                for (File child : root.listFiles())
                {
                    delete(child);
                }
            }
            root.delete();
        }
    }
}
