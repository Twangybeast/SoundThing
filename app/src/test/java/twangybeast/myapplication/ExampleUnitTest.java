package twangybeast.myapplication;

import org.junit.Test;
import twangybeast.myapplication.soundAnalysis.FFT;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
        FFT.main(new String[]{"32"});
    }
}