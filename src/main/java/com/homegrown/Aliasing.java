package com.homegrown;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import java.util.Properties;
import java.io.IOException;
import com.homegrown.sampling.*;

public class Aliasing {

    private static final Logger logger = Logger.getLogger (Aliasing.class);
    private static final Sampler sampler = new Sampler();

    public static float compute (float fs1,float fs2) throws IOException{
        String debugMsg = "compute::";
        ApplicationContext ctx = new ClassPathXmlApplicationContext("producerContext.xml");
        Properties appProperties = (Properties) ctx.getBean("appProperties");
        String recordingsDirectory = appProperties.getProperty("recordingsDirectory");
        int samplesNumber = Integer.parseInt(appProperties.getProperty("samplesNumber"));

        for  (int counter=1; Math.abs(fs2-fs1)>50.0f; ++counter) {
            byte[] sample1 = sampler.record (fs1,samplesNumber,recordingsDirectory);
            if (getMin(sample1) == getMax(sample1)) {
                fs1 = 2 * fs1;
                continue;
            }
            byte[] sample2 = sampler.record (fs2,samplesNumber,recordingsDirectory);

            int N1 = Integer.parseInt(appProperties.getProperty("transformationSize"));
            int N2 = (int)Math.ceil(N1*fs2/fs1);

            double[] fft1 = sampler.powerSpectrum(sampler.normalizeQuantiz(sample1),N1);
            double[] fft2 = sampler.powerSpectrum(sampler.normalizeQuantiz(sample2),N2);
            int tail_length = (int)Math.floor((N2-N1)/2);
            double[] fft2pruned = new double[N1];
            int j=0;
            for (int i=1+tail_length; i<N2-tail_length; ++i) {
                    fft2pruned[j++] = fft2[i];
            }
            double similarity = sampler.cosineSimilarity (fft1,fft2pruned);
            if (similarity < .9f) {
                fs1 = (fs2+fs1)/2.0f;
            }else{
                fs2 = fs1;
                fs1 = fs1/2.0f;
            }
            logger.info (debugMsg+"RUNNING ITERATION #"+counter+"\t[fs1="+fs1+",fs2="+fs2+"]");
        }
        float fs_converg = (fs1+fs1)/2.0f;
        logger.info(debugMsg+"fs_converg="+fs_converg);
        return fs_converg;
    }

    private static byte getMin (byte[] audioData) throws IOException {
        byte min = Byte.MAX_VALUE;
        for (int i=0; i<audioData.length; ++i) {
            if (audioData[i] < min) {
                min = audioData[i];
            }
        }
        //logger.info ("getMin::MIN="+min);
        return min;
    }

    private static byte getMax (byte[] audioData) throws IOException{
        byte max = Byte.MIN_VALUE;
        for (int i=0; i<audioData.length; ++i) {
            if (audioData[i] > max) {
                max = audioData[i];
            }
        }
        //logger.info ("getMax::MAX="+max);
        return max;
    }
}
