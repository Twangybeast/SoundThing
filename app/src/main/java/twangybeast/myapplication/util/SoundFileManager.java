package twangybeast.myapplication.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Twangybeast on 3/17/2018.
 */

public class SoundFileManager
{
    public static short[] convertBytesToShorts(byte[] bytes)
    {
        short[] shorts = new short[bytes.length/2];
        for (int i = 0; i < shorts.length; i++) {
            shorts[i] = (short)(( bytes[i*2+1] & 0xff )|( bytes[i*2] << 8 ));
        }
        return shorts;
    }
    public static byte[] getWavHeader(int dataLengthInBytes, int samplerate)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try
        {
            //Notes: This is the standard WAV header format for files. If you don't know what it is, don't touch it.
            //Semi-Official Link to header standard:
            //https://web.archive.org/web/20141213140451/https://ccrma.stanford.edu/courses/422/projects/WaveFormat/
            writeString(dos, "RIFF");
            writeInt(dos, 36 + dataLengthInBytes);
            writeString(dos, "WAVE");
            writeString(dos, "fmt");
            writeInt(dos, 16);
            writeShort(dos, (short)1);
            writeShort(dos, (short)1);//Num channels
            writeInt(dos, samplerate);
            writeInt(dos, samplerate * 1 * 2);//samplerate * channels * bytes per sample
            writeShort(dos, (short) (1 * 2)); // channels * bytes per sample
            writeShort(dos, (short)(16));// bits per sample
            writeString(dos, "data");
            writeInt(dos, dataLengthInBytes);//Obvious

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return baos.toByteArray();//should be 44 bytes long
    }
    public static void writeString(DataOutputStream dos, String val) throws IOException
    {
        for (char c : val.toCharArray())
        {
            dos.write(c);
        }
    }
    public static void writeInt(DataOutputStream dos, int val) throws IOException
    {
        //Low Endian
        dos.write(val);
        dos.write(val >> 8);
        dos.write(val >> 16);
        dos.write(val >> 24);
    }
    public static void writeShort(DataOutputStream dos, short val) throws IOException
    {
        //Low Endian
        dos.write(val);
        dos.write(val >> 8);
    }
}
