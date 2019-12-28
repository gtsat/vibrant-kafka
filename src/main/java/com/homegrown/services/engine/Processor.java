package com.homegrown.services.engine;

import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Properties;
import java.security.MessageDigest;
import com.homegrown.services.model.*;
import com.homegrown.util.DbService;
import com.homegrown.KafkaConsumer;
import org.apache.log4j.Logger;


public class Processor {
    private DbService dbService;
    public DbService getDbService(){
        return dbService;
    }
    public void setDbService(DbService dbService){
        this.dbService = dbService;
    }

    private Properties appProperties;
    public Properties getAppProperties() {return appProperties;}
    public void setAppProperties(Properties appProperties) {this.appProperties = appProperties;}

    private KafkaConsumer kafkaConsumer;
    public KafkaConsumer getKafkaConsumer() {return kafkaConsumer;}
    public void setKafkaConsumer(KafkaConsumer kafkaConsumer) {this.kafkaConsumer = kafkaConsumer;}

    private static Logger logger = Logger.getLogger(Processor.class);

    public ResponseDto resetAllProducers (String username, String password) {
        String debug = "resetAllProducers::username:"+username+". ";
        logger.info(debug+"START.");
        long start = System.currentTimeMillis();
        try {
            AuthenticationResponseDto auth = authenticate(username,password,false);
            if (auth.getStatus().equals("SUCCESS")){
                int rows = dbService.touchUser(username);
                if (rows != 1) {
                    return new ResponseDto ("ERROR", "ERROR: Unable to update last usage date in app database (code="+rows+").");
                }
                kafkaConsumer.resetAllProducers();
                for (String producer : kafkaConsumer.getCategories().keySet()) {
                    dbService.deleteBenchmark(producer);
                }
                ResponseDto responseDto = new ResponseDto("SUCCESS", "SUCCESS");
                return responseDto;
            }else{
                return new ResponseDto(auth.getStatus(),auth.getMessage());
            }
        }catch (Exception e){
            logger.error(debug+"EXCEPTION: "+e.getMessage());
            for (StackTraceElement element : e.getStackTrace()){
                logger.error (debug+element);
            }
            return new ResponseDto("ERROR","ERROR: "+e.getMessage());
        }finally{
            logger.info(debug+"END @ "+(System.currentTimeMillis()-start)+"msec.");
        }
    }

    public ResponseDto resetProducer (String username, String password, String producer) {
        String debug = "resetProducer::username:"+username+". ";
        logger.info(debug+"START.");
        long start = System.currentTimeMillis();
        try {
            AuthenticationResponseDto auth = authenticate(username,password,false);
            if (auth.getStatus().equals("SUCCESS")){
                int rows = dbService.touchUser(username);
                if (rows != 1) {
                    return new ResponseDto ("ERROR", "ERROR: Unable to update last usage date in app database (code="+rows+").");
                }
                kafkaConsumer.resetProducer(producer);
                dbService.deleteBenchmark(producer);
                ResponseDto responseDto = new ResponseDto("SUCCESS", "SUCCESS");
                return responseDto;
            }else{
                return new ResponseDto(auth.getStatus(),auth.getMessage());
            }
        }catch (Exception e){
            logger.error(debug+"EXCEPTION: "+e.getMessage());
            for (StackTraceElement element : e.getStackTrace()){
                logger.error (debug+element);
            }
            return new ResponseDto("ERROR","ERROR: "+e.getMessage());
        }finally{
            logger.info(debug+"END @ "+(System.currentTimeMillis()-start)+"msec.");
        }
    }

    public ResponseDto clearEvents (String username, String password, String producer) {
        String debug = "clearEvents::username:"+username+". ";
        logger.info(debug+"START.");
        long start = System.currentTimeMillis();
        try {
            AuthenticationResponseDto auth = authenticate(username,password,false);
            if (auth.getStatus().equals("SUCCESS")){
                int rows = dbService.touchUser(username);
                if (rows != 1) {
                    return new ResponseDto ("ERROR", "ERROR: Unable to update last usage date in app database (code="+rows+").");
                }
                dbService.clearEvents(producer);
                ResponseDto responseDto = new ResponseDto("SUCCESS", "SUCCESS");
                return responseDto;
            }else{
                return new ResponseDto(auth.getStatus(),auth.getMessage());
            }
        }catch (Exception e){
            logger.error(debug+"EXCEPTION: "+e.getMessage());
            for (StackTraceElement element : e.getStackTrace()){
                logger.error (debug+element);
            }
            return new ResponseDto("ERROR","ERROR: "+e.getMessage());
        }finally{
            logger.info(debug+"END @ "+(System.currentTimeMillis()-start)+"msec.");
        }
    }

    public EventListResponseDto getEvents (String username, String password,
                                                  String producer, int threshold,int limit) {
        String debug = "getEvents::username:"+username+". ";
        logger.info(debug+"START.");
        long start = System.currentTimeMillis();
        try {
            AuthenticationResponseDto auth = authenticate(username,password,false);
            if (auth.getStatus().equals("SUCCESS")){
                int rows = dbService.touchUser(username);
                if (rows != 1) {
                    return new EventListResponseDto("ERROR", "ERROR: Unable to update last usage date in app database (code="+rows+").");
                }
                EventListResponseDto responseDto = new EventListResponseDto("SUCCESS", "SUCCESS");
                responseDto.setEvents(dbService.getEvents(producer, threshold, limit));
                return responseDto;
            }else{
                return new EventListResponseDto(auth.getStatus(),auth.getMessage());
            }
        }catch (Exception e){
            logger.error(debug+"EXCEPTION: "+e.getMessage());
            for (StackTraceElement element : e.getStackTrace()){
                logger.error (debug+element);
            }
            return new EventListResponseDto("ERROR","ERROR: "+e.getMessage());
        }finally{
            logger.info(debug+"END @ "+(System.currentTimeMillis()-start)+"msec.");
        }
    }

    public synchronized KafkaConsumerResponseDto updateConsumer (String username, String password, String category) {
        String debug = "updateConsumer::username:"+username+". ";
        logger.info(debug+"START.");
        long start = System.currentTimeMillis();
        try {
            AuthenticationResponseDto auth = authenticate(username,password,false);
            if (auth.getStatus().equals("SUCCESS")){
                int rows = dbService.touchUser(username);
                if (rows != 1) {
                    return new KafkaConsumerResponseDto("ERROR", "ERROR: Unable to update last usage date in app database (code="+rows+").");
                }
                kafkaConsumer.refresh();
                KafkaConsumerResponseDto responseDto = new KafkaConsumerResponseDto("SUCCESS", "SUCCESS");
                responseDto.setCategories(kafkaConsumer.getCategories());
                if (category == null || category.trim().isEmpty()) {
                    responseDto.setSimilarities(kafkaConsumer.getSimilarities());
                    responseDto.setFrequencies(kafkaConsumer.getFrequencies());
                    responseDto.setTimestamps(kafkaConsumer.getTimestamps());
                    responseDto.setBenchmarks(kafkaConsumer.getBenchmarks());
                    responseDto.setMotionUrls(kafkaConsumer.getMotionUrls());
                    responseDto.setTrainFFTs(kafkaConsumer.getTrainFFTs());
                    responseDto.setTestFFTs(kafkaConsumer.getTestFFTs());
                    responseDto.setTrains(kafkaConsumer.getTrains());
                    responseDto.setTests(kafkaConsumer.getTests());
                }else{
                    responseDto.setSimilarities(new TreeMap<>());
                    responseDto.setFrequencies(new TreeMap<>());
                    responseDto.setTimestamps(new TreeMap<>());
                    responseDto.setBenchmarks(new TreeMap<>());
                    responseDto.setMotionUrls(new TreeMap<>());
                    responseDto.setTrainFFTs(new TreeMap<>());
                    responseDto.setTestFFTs(new TreeMap<>());
                    responseDto.setTrains(new TreeMap<>());
                    responseDto.setTests(new TreeMap<>());
                    for (Map.Entry<String, String> c : kafkaConsumer.getCategories().entrySet()) {
                        if (category.equalsIgnoreCase(c.getValue())) {
                            responseDto.getSimilarities().put(c.getKey(),kafkaConsumer.getSimilarities().get(c.getKey()));
                            responseDto.getFrequencies().put(c.getKey(),kafkaConsumer.getFrequencies().get(c.getKey()));
                            responseDto.getBenchmarks().put(c.getKey(),kafkaConsumer.getBenchmarks().get(c.getKey()));
                            responseDto.getMotionUrls().put(c.getKey(),kafkaConsumer.getMotionUrls().get(c.getKey()));
                            responseDto.getTrainFFTs().put(c.getKey(),kafkaConsumer.getTrainFFTs().get(c.getKey()));
                            responseDto.getTestFFTs().put(c.getKey(),kafkaConsumer.getTestFFTs().get(c.getKey()));
                            responseDto.getTrains().put(c.getKey(),kafkaConsumer.getTrains().get(c.getKey()));
                            responseDto.getTests().put(c.getKey(),kafkaConsumer.getTests().get(c.getKey()));
                        }
                    }
                }
                return responseDto;
            }else{
                return new KafkaConsumerResponseDto(auth.getStatus(),auth.getMessage());
            }
        }catch (Exception e){
            logger.error(debug+"EXCEPTION: "+e.getMessage());
            for (StackTraceElement element : e.getStackTrace()){
                logger.error (debug+element);
            }
            return new KafkaConsumerResponseDto("ERROR","ERROR: "+e.getMessage());
        }finally{
            logger.info(debug+"END @ "+(System.currentTimeMillis()-start)+"msec.");
        }
    }

    public ResponseDto register (String username, String password,
                                 String firstname, String lastname,
                                 String msisdn, String email,
                                 String region) {
        String logMsg = "register::username:"+username+":: ";
        System.err.println(logMsg+"START");
        ResponseDto response = new ResponseDto();
        if (username == null || username.isEmpty()){
            response.setMessage("ERROR: Please provide a username...");
            response.setStatus("FAILURE");
        }else if (password == null || password.isEmpty()){
            response.setMessage("ERROR: Please provide a password...");
            response.setStatus("FAILURE");
        }else if (firstname == null || firstname.isEmpty()){
            response.setMessage("ERROR: Please provide your firstname...");
            response.setStatus("FAILURE");
        }else if (lastname == null || lastname.isEmpty()){
            response.setMessage("ERROR: Please provide your lastname...");
            response.setStatus("FAILURE");
        }else if (msisdn == null || msisdn.isEmpty()){
            response.setMessage("ERROR: Please provide your MSISDN...");
            response.setStatus("FAILURE");
        }else if (email == null || email.isEmpty()){
            response.setMessage("ERROR: Please provide your email...");
            response.setStatus("FAILURE");
        }else{
            try{
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.reset();
                byte[] digested = md.digest (password.getBytes());
                StringBuffer encoded = new StringBuffer();
                for(int i=0;i<digested.length;i++) {
                    //encoded.append (Integer.toHexString(0xff & digested[i]));
                    encoded.append(Integer.toString((digested[i] & 0xff) + 0x100, 16).substring(1));
                }
                String errorDesc = dbService.insertUser(username,encoded.toString(),firstname,lastname,msisdn,email,8192,250,5,region);
                if (errorDesc == null){
                    System.err.println (logMsg+"Successful registration!");
                    response.setMessage ("Successful registration!");
                    response.setStatus ("SUCCESS");
                    return response;
                }
                System.err.println(logMsg+"Unable to register... ("+errorDesc+")");
                response.setMessage(errorDesc);
                response.setStatus("FAILURE");
            }catch (Exception e){
                response.setMessage("ERROR: "+e.getMessage());
                response.setStatus("FAILURE");
                e.printStackTrace();
            }finally{
                System.err.println(logMsg+"END");
            }
        }
        return response;
    }

    public AuthenticationResponseDto authenticate (String username, String password, boolean encode){
        String logMsg = "authenticate::username:"+username+":: ";
        System.err.println(logMsg+"START");
        AuthenticationResponseDto response = new AuthenticationResponseDto();
        if (username == null || username.isEmpty()){
            response.setMessage("Please provide a username...");
            response.setStatus("FAILURE");
        }else if (password == null || password.isEmpty()){
            response.setMessage("Please provide a password...");
            response.setStatus("FAILURE");
        }else{
            try{User user = dbService.getUserByUsername(username);
                if (user != null){
                    if (encode) {
                        logger.info(logMsg+"Encoding password input: '"+password+"'.");
                        MessageDigest md = MessageDigest.getInstance("MD5");
                        md.reset();
                        byte[] digested = md.digest(password.getBytes());
                        StringBuffer encoded = new StringBuffer();
                        for (int i = 0; i < digested.length; i++) {
                            //encoded.append(Integer.toHexString(0xff & digested[i]));
                            encoded.append(Integer.toString((digested[i] & 0xff) + 0x100, 16).substring(1));
                        }
                        password = encoded.toString();
                    }
                    logger.info (logMsg+"Comparing db password '"+user.getPassword()+"' with encoded input: '"+password.toString()+"'");
                    if (user.getPassword().equals(password)){
                        System.err.println (logMsg+"Successful authentication!!!");
                        response.setMessage ("Welcome back!!!");
                        response.setStatus ("SUCCESS");
                        response.setUser (user);
                        int rows = dbService.touchUser(username);
                        if (rows != 1) {
                            response.setMessage ("ERROR: Unable to update last usage date in app database (code="+rows+").");
                        }
                        return response;
                    }
                }else{
                    response.setMessage("ERROR: Unknown user '"+username+"'...");
                    System.err.println(logMsg+"Unable to authenticate... ("+response.getMessage()+")");
                    response.setStatus("FAILURE");
                    return response;
                }
                response.setMessage("ERROR: Invalid password for user '"+username+"'...");
                response.setStatus("FAILURE");
            }catch (Exception e){
                response.setMessage("ERROR: "+e.getMessage());
                response.setStatus("FAILURE");
                e.printStackTrace();
            }finally{
                System.err.println(logMsg+"END");
            }
        }
        System.err.println(logMsg+"Unable to authenticate... ("+response.getMessage()+")");
        return response;
    }

    public ResponseDto updateUserDetails (String username, String password,
                                          String firstname, String lastname,
                                          String email, String msisdn, String region){
        String debug = "updateUserDetails::username:"+username+". ";
        logger.info(debug+"START.");
        long start = System.currentTimeMillis();
        try {
            AuthenticationResponseDto auth = authenticate(username,password,false);
            if (auth.getStatus().equals("SUCCESS")){
                int rows = dbService.touchUser(username);
                if (rows != 1) {
                    return new ResponseDto ("ERROR", "ERROR: Unable to update last usage date in app database (code="+rows+").");
                }
                rows = dbService.updateUserDetails(username, firstname, lastname, email, msisdn, region);
                if (rows != 1) {
                    return new ResponseDto("ERROR", "ERROR: Unable to change sharing details of user '" + username + "' (code=" + rows + ").");
                }else{
                    return new ResponseDto("SUCCESS", "SUCCESS");
                }
            }else{
                return new ResponseDto(auth.getStatus(),auth.getMessage());
            }
        }catch (Exception e){
            logger.error(debug+"EXCEPTION: "+e.getMessage());
            for (StackTraceElement element : e.getStackTrace()){
                logger.error (debug+element);
            }
            return new ResponseDto("ERROR","ERROR: "+e.getMessage());
        }finally{
            logger.info(debug+"END @ "+(System.currentTimeMillis()-start)+"msec.");
        }
    }

    public ResponseDto activateUser (String username, String password, String toActivate){
        String debug = "activateUser::username:"+username+". ";
        logger.info(debug+"START.");
        long start = System.currentTimeMillis();
        try {
            AuthenticationResponseDto auth = authenticate(username,password,false);
            if (auth.getStatus().equals("SUCCESS")){
                int rows = dbService.touchUser(username);
                if (rows != 1) {
                    return new ResponseDto ("ERROR", "ERROR: Unable to update last usage date in app database (code="+rows+").");
                }
                if (auth.getUser().getAdmin()) {
                    rows = dbService.activateUser(toActivate);
                    if (rows != 1) {
                        return new ResponseDto("ERROR", "ERROR: Unable to activate user '" + toActivate + "' (code=" + rows + ").");
                    }else{
                        return new ResponseDto("SUCCESS", "SUCCESS");
                    }
                }else{
                    return new ResponseDto("FAILURE","You are not authorized to perform this operation!");
                }
            }else{
                return new ResponseDto(auth.getStatus(),auth.getMessage());
            }
        }catch (Exception e){
            logger.error(debug+"EXCEPTION: "+e.getMessage());
            for (StackTraceElement element : e.getStackTrace()){
                logger.error (debug+element);
            }
            return new ResponseDto("ERROR","ERROR: "+e.getMessage());
        }finally{
            logger.info(debug+"END @ "+(System.currentTimeMillis()-start)+"msec.");
        }
    }

    public ResponseDto disableUser (String username, String password, String toDisable){
        String debug = "disableUser::username:"+username+". ";
        logger.info(debug+"START.");
        long start = System.currentTimeMillis();
        try {
            AuthenticationResponseDto auth = authenticate(username,password,false);
            if (auth.getStatus().equals("SUCCESS")){
                if (username.equals(toDisable)) {
                    return new ResponseDto("ERROR", "ERROR: Unable to disable yourself.");
                }else{
                    int rows = dbService.touchUser(username);
                    if (rows != 1) {
                        return new ResponseDto ("ERROR", "ERROR: Unable to update last usage date in app database (code="+rows+").");
                    }
                    if (auth.getUser().getAdmin()) {
                        rows = dbService.disableUser(toDisable);
                        if (rows != 1) {
                            return new ResponseDto("ERROR", "ERROR: Unable to disable user '" + toDisable + "' (code=" + rows + ").");
                        }else{
                            return new ResponseDto("SUCCESS", "SUCCESS");
                        }
                    }else{
                        return new ResponseDto("FAILURE","You are not authorized to perform this operation!");
                    }
                }
            }else{
                return new ResponseDto(auth.getStatus(),auth.getMessage());
            }
        }catch (Exception e){
            logger.error(debug+"EXCEPTION: "+e.getMessage());
            for (StackTraceElement element : e.getStackTrace()){
                logger.error (debug+element);
            }
            return new ResponseDto("ERROR","ERROR: "+e.getMessage());
        }finally{
            logger.info(debug+"END @ "+(System.currentTimeMillis()-start)+"msec.");
        }
    }

    public UsersListResponseDto getUsers (String username, String password){
        String debug = "getUsers::username:"+username+". ";
        logger.info(debug+"START.");
        long start = System.currentTimeMillis();
        try {
            AuthenticationResponseDto auth = authenticate(username,password,false);
            if (auth.getStatus().equals("SUCCESS")){
                int rows = dbService.touchUser(username);
                if (rows != 1) {
                    return new UsersListResponseDto ("ERROR", "ERROR: Unable to update last usage date in app database (code="+rows+").");
                }else{
                    if (auth.getUser().getAdmin()) {
                        UsersListResponseDto response = new UsersListResponseDto("SUCCESS", "SUCCESS");
                        response.setUsers(dbService.getUsers());
                        return response;
                    }else{
                        return new UsersListResponseDto("FAILURE","You are not authorized to perform this operation!");
                    }
                }
            }else{
                return new UsersListResponseDto(auth.getStatus(),auth.getMessage());
            }
        }catch (Exception e){
            logger.error(debug+"EXCEPTION: "+e.getMessage());
            for (StackTraceElement element : e.getStackTrace()){
                logger.error (debug+element);
            }
            return new UsersListResponseDto("ERROR","ERROR: "+e.getMessage());
        }finally{
            logger.info(debug+"END @ "+(System.currentTimeMillis()-start)+"msec.");
        }
    }
}
