package com.homegrown;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import com.homegrown.util.DbService;
import com.homegrown.sampling.Sampler;
import com.homegrown.consumer.ConsumerCreator;
import org.springframework.web.context.ContextLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.log4j.Logger;


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
    private TreeMap<String,Date> timestamps = new TreeMap<>();
    private TreeMap<String,Date> benchmarks = new TreeMap<>();
    private TreeMap<String,Integer> similarities = new TreeMap<>();
    private TreeMap<String,Float> frequencies = new TreeMap<>();
    private TreeMap<String,String> categories = new TreeMap<>();

    public TreeMap<String,double[]> getTrains () {return trains;}
    public TreeMap<String,double[]> getTests () {return tests;}
    public TreeMap<String,double[]> getTrainFFTs () {return trainFFTs;}
    public TreeMap<String,double[]> getTestFFTs () {return testFFTs;}
    public TreeMap<String,Date> getTimestamps () {return timestamps;}
    public TreeMap<String,Date> getBenchmarks () {return benchmarks;}
    public TreeMap<String,Integer> getSimilarities () {return similarities;}
    public TreeMap<String,Float> getFrequencies () {return frequencies;}
    public TreeMap<String,String> getCategories () {return categories;}


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
                System.out.println("Record Key: " + record.key());
                System.out.println("Record partition: " + record.partition());
                System.out.println("Record offset: " + record.offset());

                String[] key = record.key().split("_");
                String id = key[0];
                try {categories.put(id,key[2]);}
                catch (Exception e) {categories.put(id,appProperties.getProperty("defaultCategory"));}
                frequencies.put(id, Float.parseFloat(key[1]));
                double[] normalized = sampler.normalizeQuantiz(record.value());
                double[] trainFFT = trainFFTs.get(id);
                if (trainFFT == null) {
                    trainFFT = sampler.powerSpectrum(normalized,transformationSize);
                    System.out.println("Extracted train sample of size: " + record.value().length);

                    trains.put (id,normalized);
                    trainFFTs.put (id,trainFFT);
                    benchmarks.put (id,new Date(record.timestamp()));
                }else{
                    double[] testFFT = sampler.powerSpectrum(normalized,transformationSize);
                    double similarity = sampler.cosineSimilarity (testFFT,trainFFT);
                    System.out.println("Extracted test sample of size: " + record.value().length);
                    System.out.println("Similarity: " + similarity + ".");

                    tests.put(id,normalized);
                    testFFTs.put(id,testFFT);
                    similarities.put(id,(int)Math.round(similarity*100));
                    timestamps.put (id,new Date(record.timestamp()));

                    dbService.insertSimilarity(id,(int)Math.round(similarity*100));
                }
            }
            consumer.commitAsync();
        }

        for (Map.Entry<String, Date> entry : timestamps.entrySet()) {
            if (entry.getValue().before(new Date(System.currentTimeMillis()-(60*1000)))) {
                trains.remove(entry.getKey());
                tests.remove(entry.getKey());
                trainFFTs.remove(entry.getKey());
                testFFTs.remove(entry.getKey());
                timestamps.remove(entry.getKey());
                benchmarks.remove(entry.getKey());
                similarities.remove(entry.getKey());
                frequencies.remove(entry.getKey());
                categories.remove(entry.getKey());
            }
        }
        //consumer.close();
    }

    @Override public void finalize () {if (consumer != null) consumer.close();}
}
