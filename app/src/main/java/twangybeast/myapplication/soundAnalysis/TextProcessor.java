package twangybeast.myapplication.soundAnalysis;

import android.arch.core.util.Function;
import android.media.AudioFormat;
import android.media.AudioTrack;
import twangybeast.myapplication.activities.ProcessVoiceActivity;
import twangybeast.myapplication.activities.RecordSoundNoteActivity;
import twangybeast.myapplication.views.FourierHistoryView;
import twangybeast.myapplication.views.WaveformView;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by Twangybeast on 3/17/2018.
 */

public class TextProcessor
{
    short[] shorts;
    int currentPosition;
    float[] floats;
    LinkedList<float[]> history;
    LinkedList<Float> historyMaximums;
    float[] windowed;
    int N;
    Complex[] complexes;
    Complex[] ranged;
    float[] fourier;
    WindowHelper windowHelper;
    int[] filterFreqIndexes;
    WaveformView waveform;
    int progress;
    FourierHistoryView fourierView;
    public TextProcessor(int bufferLength, WaveformView waveform, FourierHistoryView fourierView)
    {
        shorts = new short[bufferLength/2];    //Short array where bytes are converted into
        floats = new float[shorts.length];      //float array where shorts are converted into
        history = new LinkedList<>();
        historyMaximums = new LinkedList<>();
        currentPosition = 0;
        N = Integer.highestOneBit(ProcessVoiceActivity.FOURIER_RADIUS * 2 + 1);
        windowed = new float[N];                //Holds float values multiplied by window for fourier
        complexes = new Complex[N];           //Fourier result
        ranged = new Complex[(N/2)+1];        //Restricted range fourier result
        fourier = new float[ranged.length];//Final fourier to display
        windowHelper = new WindowHelper(N);
        filterFreqIndexes = AudioAnalysis.getMelIndices(16000, N, 26);
        this.waveform = waveform;
        progress = 0;
        this.fourierView = fourierView;
    }
    public void processBytes(byte[] buffer, int amountRead)
    {
        for (int i = 0; i < amountRead/2; i++) {
            shorts[i] = (short)(( buffer[i*2+1] & 0xff )|( buffer[i*2] << 8 ));
        }
        AudioAnalysis.toFloatArray(shorts, floats, RecordSoundNoteActivity.MAX_AMPLITUDE, amountRead/2);
        float bufferMax = 0;
        for (float f  : floats)
        {
            bufferMax = Math.max(Math.abs(f), bufferMax);
        }
        historyMaximums.offer(bufferMax);
        float[] historyItem = waveform.updateAudioData(floats);//Get float array from waveform to save memory
        history.offer(historyItem);
        while (currentPosition - ProcessVoiceActivity.FOURIER_RADIUS + N < history.size() * shorts.length)
        {
            int index = currentPosition - ProcessVoiceActivity.FOURIER_RADIUS;
            int count = 0;
            if (index < 0)
            {
                count -= index;//Ensure positive
                index = 0;
            }
            Iterator<Float> maximumsIter = historyMaximums.iterator();
            float max = 0.4f;
            SampleLoop:
            for (float[] samples : history)
            {
                max = Math.max(max, maximumsIter.next());
                for (; index < samples.length; index++) {
                    windowed[count] = samples[index] * windowHelper.getValue(count);
                    count++;
                    if (!(count < N))
                    {
                        break SampleLoop;
                    }
                }
                index -= samples.length;
            }
            currentPosition += ProcessVoiceActivity.FOURIER_STEP;
            AudioAnalysis.restrictFloatArray(windowed, max);
            AudioAnalysis.calculateFourier(windowed, complexes, N);
            AudioAnalysis.getRange(complexes, ranged, 0, ranged.length);
            AudioAnalysis.complexToFloat(ranged, fourier, fourier.length);
            System.out.println(AudioAnalysis.getMax(fourier));
            //AudioAnalysis.restrictFloatArray(fourier, Math.max(N/16, AudioAnalysis.getMax(fourier)));//TODO Make maximum global
            AudioAnalysis.restrictFloatArray(fourier, N);
            System.out.println("\t\t"+AudioAnalysis.getMax(fourier));
            //https://kastnerkyle.github.io/posts/single-speaker-word-recognition-with-hidden-markov-models/
            //https://dsp.stackexchange.com/questions/29165/speech-recognition-using-mfcc-and-dtwdynamic-time-warping
            //http://www.fit.vutbr.cz/~grezl/ZRE/lectures/08_reco_dtw_en.pdf
            //Use this?
            float[] filterBanks = AudioAnalysis.melFilter(fourier, filterFreqIndexes);

            AudioAnalysis.takeNaturalLog(filterBanks);
            float[] melCoefficients = AudioAnalysis.dct(filterBanks, 13);
            //TODO DElete this?
            AudioAnalysis.functionOnFloats(melCoefficients, new Function<Float, Float>()
            {
                @Override
                public Float apply(Float input)
                {
                    return Math.abs(input);
                }
            });
            fourierView.updateFourierValues(melCoefficients);
            fourierView.updateDisplay();
        }
        if (currentPosition - ProcessVoiceActivity.FOURIER_RADIUS > shorts.length)
        {
            history.poll();
            historyMaximums.poll();
            currentPosition -= shorts.length;
        }

        waveform.updateDisplay();
        progress += amountRead;
    }
    public int getProgress()
    {
        return progress;
    }
}
