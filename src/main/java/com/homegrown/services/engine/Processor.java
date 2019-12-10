package com.homegrown.services.engine;

import com.homegrown.KafkaConsumer;
import com.homegrown.util.DbService;
import com.homegrown.services.model.*;
import org.apache.log4j.Logger;

import java.util.Properties;
import java.security.MessageDigest;


public class Processor { /*implements ApplicationContextAware {
    private ApplicationContext context;
    @Override public void setApplicationContext(ApplicationContext context) {this.context=context;}

    private AsyncProcessor asyncProcessor;
    public AsyncProcessor getAsyncProcessor() {return asyncProcessor;}
    public void setAsyncProcessor(AsyncProcessor asyncProcessor) {this.asyncProcessor = asyncProcessor;}

    public Processor () {
        if (context == null) {
            WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
        }
        asyncProcessor = (AsyncProcessor) context.getBean("asyncProcessor");
    }
    */
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

    public SimilaritiesListResponseDto getSimilaritiesByProducer (String username, String password,
                                                                  String producer, int threshold,int limit) {
        String debug = "getSimilaritiesByProducer::username:"+username+". ";
        logger.info(debug+"START.");
        long start = System.currentTimeMillis();
        try {
            AuthenticationResponseDto auth = authenticate(username,password,false);
            if (auth.getStatus().equals("SUCCESS")){
                int rows = dbService.touchUser(username);
                if (rows != 1) {
                    return new SimilaritiesListResponseDto ("ERROR", "ERROR: Unable to update last usage date in app database (code="+rows+").");
                }
                SimilaritiesListResponseDto responseDto = new SimilaritiesListResponseDto("SUCCESS", "SUCCESS");
                responseDto.setSimilarities(dbService.getSimilaritiesByProducer(producer,threshold,limit));
                return responseDto;
            }else{
                return new SimilaritiesListResponseDto(auth.getStatus(),auth.getMessage());
            }
        }catch (Exception e){
            logger.error(debug+"EXCEPTION: "+e.getMessage());
            for (StackTraceElement element : e.getStackTrace()){
                logger.error (debug+element);
            }
            return new SimilaritiesListResponseDto("ERROR","ERROR: "+e.getMessage());
        }finally{
            logger.info(debug+"END @ "+(System.currentTimeMillis()-start)+"msec.");
        }
    }

    public synchronized SimilaritiesResponseDto refreshSimilarities (String username, String password) {
        String debug = "refreshSimilarities::username:"+username+". ";
        logger.info(debug+"START.");
        long start = System.currentTimeMillis();
        try {
            AuthenticationResponseDto auth = authenticate(username,password,false);
            if (auth.getStatus().equals("SUCCESS")){
                int rows = dbService.touchUser(username);
                if (rows != 1) {
                    return new SimilaritiesResponseDto ("ERROR", "ERROR: Unable to update last usage date in app database (code="+rows+").");
                }
                kafkaConsumer.refresh();
                SimilaritiesResponseDto responseDto = new SimilaritiesResponseDto("SUCCESS", "SUCCESS");
                responseDto.setSimilarities(kafkaConsumer.getSimilarities());
                responseDto.setFrequencies(kafkaConsumer.getFrequencies());
                responseDto.setTimestamps(kafkaConsumer.getTimestamps());
                responseDto.setBenchmarks(kafkaConsumer.getBenchmarks());
                responseDto.setTrainFFTs(kafkaConsumer.getTrainFFTs());
                responseDto.setTestFFTs(kafkaConsumer.getTestFFTs());
                responseDto.setTrains(kafkaConsumer.getTrains());
                responseDto.setTests(kafkaConsumer.getTests());
                return responseDto;
            }else{
                return new SimilaritiesResponseDto(auth.getStatus(),auth.getMessage());
            }
        }catch (Exception e){
            logger.error(debug+"EXCEPTION: "+e.getMessage());
            for (StackTraceElement element : e.getStackTrace()){
                logger.error (debug+element);
            }
            return new SimilaritiesResponseDto("ERROR","ERROR: "+e.getMessage());
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
