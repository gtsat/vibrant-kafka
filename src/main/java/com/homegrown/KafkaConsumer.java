package com.homegrown;

import com.homegrown.util.DbService;
import com.homegrown.sampling.Sampler;
import com.homegrown.consumer.ConsumerCreator;
import com.homegrown.services.model.BenchmarkSample;
import org.springframework.web.context.ContextLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.sql.Timestamp;
import java.util.*;


public class KafkaConsumer {
    private Properties appProperties;
    public Properties getAppProperties() {return appProperties;}
    public void setAppProperties(Properties appProperties) {this.appProperties = appProperties;}

    private DbService dbService;
    public DbService getDbService(){
        return dbService;
    }
    public void setDbService(DbService dbService){
        this.dbService = dbService;
    }

    private static final Logger logger = Logger.getLogger (Main.class);

    private Consumer<String,byte[]> consumer;

    private TreeMap<String,double[]> trains = new TreeMap<>();
    private TreeMap<String,double[]> tests = new TreeMap<>();
    private TreeMap<String,double[]> trainFFTs = new TreeMap<>();
    private TreeMap<String,double[]> testFFTs = new TreeMap<>();
    private TreeMap<String,String> timestamps = new TreeMap<>();
    private TreeMap<String,String> benchmarks = new TreeMap<>();
    private TreeMap<String,Integer> similarities = new TreeMap<>();
    private TreeMap<String,Float> frequencies = new TreeMap<>();
    private TreeMap<String,String> categories = new TreeMap<>();
    private TreeMap<String,String> motionUrls = new TreeMap<>();

    public TreeMap<String,double[]> getTrains () {return trains;}
    public TreeMap<String,double[]> getTests () {return tests;}
    public TreeMap<String,double[]> getTrainFFTs () {return trainFFTs;}
    public TreeMap<String,double[]> getTestFFTs () {return testFFTs;}
    public TreeMap<String,String> getTimestamps () {return timestamps;}
    public TreeMap<String,String> getBenchmarks () {return benchmarks;}
    public TreeMap<String,Integer> getSimilarities () {return similarities;}
    public TreeMap<String,Float> getFrequencies () {return frequencies;}
    public TreeMap<String,String> getCategories () {return categories;}
    public TreeMap<String,String> getMotionUrls () {return motionUrls;}

    public synchronized void resetAllProducers () {
        trains.clear();
        tests.clear();
        trainFFTs.clear();
        testFFTs.clear();
        timestamps.clear();
        benchmarks.clear();
        similarities.clear();
        frequencies.clear();
        categories.clear();
        motionUrls.clear();
    }

    public synchronized void resetProducer (String producer) {
        trains.remove(producer);
        tests.remove(producer);
        trainFFTs.remove(producer);
        testFFTs.remove(producer);
        timestamps.remove(producer);
        benchmarks.remove(producer);
        similarities.remove(producer);
        frequencies.remove(producer);
        categories.remove(producer);
        motionUrls.remove(producer);
    }

    public synchronized void refresh () {
        if (consumer == null) {
            String brokers = appProperties.getProperty("brokers");
            String topic = appProperties.getProperty("topic");
            String role = appProperties.getProperty("role");
            String id = appProperties.getProperty("id");
            String group = appProperties.getProperty("group");
            String offsetReset = appProperties.getProperty("offsetReset");
            int pollingCount = Integer.parseInt(appProperties.getProperty("pollingCount"));
            int transformationSize = Integer.parseInt(appProperties.getProperty("transformationSize"));

            String debugMsg = "KafkaConsumer::";
            logger.info(debugMsg + "Brokers: " + brokers);
            logger.info(debugMsg + "Topic: " + topic);
            logger.info(debugMsg + "Role: " + role);
            logger.info(debugMsg + "ID: " + id);
            logger.info(debugMsg + "Group: " + group);
            logger.info(debugMsg + "OffsetReset: " + offsetReset);
            logger.info(debugMsg + "PollingCount: " + pollingCount);
            logger.info(debugMsg + "TransformationSize: " + transformationSize);

            consumer = ConsumerCreator.createConsumer (brokers,topic,id,group,offsetReset,pollingCount);
        }
        String debugMsg = "refresh::";
        Sampler sampler = new Sampler();
        int pollingCount = Integer.parseInt(appProperties.getProperty("pollingCount"));
        int transformationSize = Integer.parseInt(appProperties.getProperty("transformationSize"));
        int noMessageFoundCount = Integer.parseInt(appProperties.getProperty("noMessageFoundCount"));
        logger.info(debugMsg + "PollingCount: " + pollingCount);
        logger.info(debugMsg + "TransformationSize: " + transformationSize);
        logger.info(debugMsg + "NoMessageFoundCount: " + noMessageFoundCount);
        int noMessageToFetch = 0;
        while (true) {
            final ConsumerRecords<String,byte[]> consumerRecords = consumer.poll (pollingCount);
            logger.info (debugMsg+"Brought "+consumerRecords.count()+" records...");
            if (consumerRecords.count() == 0) {
                noMessageToFetch++;
                if (noMessageToFetch > noMessageFoundCount) {
                    break;
                }
            }

            for (ConsumerRecord<String,byte[]> record : consumerRecords) {
                logger.info(debugMsg + "Record Key: " + record.key());
                logger.info(debugMsg + "Record partition: " + record.partition());
                logger.info(debugMsg + "Record offset: " + record.offset());

                String[] key = record.key().split("_");
                String id = key[0];
                try{motionUrls.put(id,key[3]);}
                catch (Exception e) {}
                try {categories.put(id,key[2]);}
                catch (Exception e) {categories.put(id,appProperties.getProperty("defaultCategory"));}
                double[] normalized = sampler.normalizeQuantiz(record.value());
                double[] trainFFT = trainFFTs.get(id);
                float frequency = Float.parseFloat(key[1]);

                if (trainFFT != null && frequency == frequencies.get(id)) {
                    double[] testFFT = sampler.powerSpectrum(normalized, transformationSize);
                    double similarity = sampler.cosineSimilarity(testFFT, trainFFT);
                    logger.info(debugMsg + "1. Extracted test sample of size: " + record.value().length);
                    logger.info(debugMsg + "1. Similarity: " + similarity + ".");

                    tests.put(id, computeHistogram(record.value()));
                    testFFTs.put(id, testFFT);
                    similarities.put(id, (int) Math.round(similarity * 100));
                    timestamps.put(id, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Timestamp(record.timestamp())));

                    dbService.insertEvent(id, (int) Math.round(similarity * 100),record.offset(),new Timestamp(record.timestamp()));
                }else{
                    BenchmarkSample dbSample = dbService.getBenchmark(id);
                    if (trainFFT == null && dbSample != null && frequency == dbSample.getFrequency()) {
                        trainFFT = sampler.powerSpectrum(sampler.normalizeQuantiz(dbSample.getSignal()), transformationSize);
                        trains.put(id, computeHistogram(dbSample.getSignal()));
                        benchmarks.put(id, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dbSample.getCreationDate()));
                        frequencies.put(id, dbSample.getFrequency());
                        trainFFTs.put(id, trainFFT);

                        double[] testFFT = sampler.powerSpectrum(normalized, transformationSize);
                        double similarity = sampler.cosineSimilarity(testFFT, trainFFT);
                        logger.info(debugMsg + "2. Extracted test sample of size: " + record.value().length);
                        logger.info(debugMsg + "2. Similarity: " + similarity + ".");

                        tests.put(id, computeHistogram(record.value()));
                        testFFTs.put(id, testFFT);
                        similarities.put(id, (int) Math.round(similarity * 100));
                        timestamps.put(id, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Timestamp(record.timestamp())));

                        dbService.insertEvent(id, (int) Math.round(similarity * 100), record.offset(), new Timestamp(record.timestamp()));
                    }else{
                        if (dbSample != null) {
                            dbService.deleteBenchmark(id);
                        }
                        dbService.insertBenchmark(id, frequency, record.value());

                        trainFFT = sampler.powerSpectrum(normalized, transformationSize);
                        logger.info(debugMsg + "Extracted train sample of size: " + record.value().length);

                        benchmarks.put(id, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Timestamp(record.timestamp())));
                        trains.put(id, computeHistogram(record.value()));
                        frequencies.put(id, frequency);
                        trainFFTs.put(id, trainFFT);
                    }
                }
            }
            consumer.commitAsync();
        }

        for (Map.Entry<String,String> entry : timestamps.entrySet()) {
            try {
                if (entry.getValue() == null || entry.getValue().isEmpty()
                        || new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(entry.getValue()).before(new Date(System.currentTimeMillis()-60000))) {
                    trains.remove(entry.getKey());
                    tests.remove(entry.getKey());
                    trainFFTs.remove(entry.getKey());
                    testFFTs.remove(entry.getKey());
                    timestamps.remove(entry.getKey());
                    benchmarks.remove(entry.getKey());
                    similarities.remove(entry.getKey());
                    frequencies.remove(entry.getKey());
                    categories.remove(entry.getKey());
                    motionUrls.remove(entry.getKey());
                }
            }catch(Exception e){
                trains.remove(entry.getKey());
                tests.remove(entry.getKey());
                trainFFTs.remove(entry.getKey());
                testFFTs.remove(entry.getKey());
                timestamps.remove(entry.getKey());
                benchmarks.remove(entry.getKey());
                similarities.remove(entry.getKey());
                frequencies.remove(entry.getKey());
                categories.remove(entry.getKey());
                motionUrls.remove(entry.getKey());
            }
        }
        //consumer.close();
    }

    private double[] computeHistogram (byte[] data) {
        double[] hist = new double[1<<8];
        for (byte b : data) {
            ++hist[b+(1<<7)];
        }
        return hist;
    }

    private double[] computeHistogram (List<Byte> data) {
        double[] hist = new double[1<<8];
        for (byte b : data) {
            ++hist[b+(1<<7)];
        }
        return hist;
    }

    @Override public void finalize () {if (consumer != null) consumer.close();}
}
