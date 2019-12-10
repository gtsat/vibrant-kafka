package com.homegrown.services.iface;

import com.homegrown.services.engine.Processor;
import com.homegrown.services.model.*;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@RestController
@RequestMapping("/rest")
public class RestService implements ApplicationContextAware {
    private ApplicationContext ctx;
    @Override public void setApplicationContext(ApplicationContext context) {this.ctx=context;}

    private static Logger logger = Logger.getLogger (RestService.class);

    private static Processor processor;
    public Processor getProcessor() {return processor;}
    public void setProcessor(Processor processor) {this.processor = processor;}

    @RequestMapping(method=RequestMethod.GET,value="/test")
    public ResponseEntity<String> test () {
        return new ResponseEntity<>("Hello World!!",null,HttpStatus.OK);
    }

    @RequestMapping(method=RequestMethod.POST,value="/getSimilaritiesByProducer",produces="application/json")
    public ResponseEntity<SimilaritiesListResponseDto> getSimilaritiesByProducer (@RequestBody RequestDto request) {
        String debug = "authenticate::username:"+request.getUsername()+". ";
        logger.info(debug + "START.");
        long start = System.currentTimeMillis();
        try{
            if (request.getProducer() == null || request.getProducer().isEmpty()) {
                return new ResponseEntity<>(new SimilaritiesListResponseDto("ERROR", "ERROR: No producer name in input."), null, HttpStatus.INTERNAL_SERVER_ERROR);
            }else
            if (request.getThreshold() == null) {
                return new ResponseEntity<>(new SimilaritiesListResponseDto("ERROR", "ERROR: No threshold percentage in input."), null, HttpStatus.INTERNAL_SERVER_ERROR);
            }else
            if (request.getLimit() == null) {
                return new ResponseEntity<>(new SimilaritiesListResponseDto("ERROR", "ERROR: No result size limitation in input."), null, HttpStatus.INTERNAL_SERVER_ERROR);
            }else {
                return new ResponseEntity<>(processor.getSimilaritiesByProducer(request.getUsername(), request.getPassword(), request.getProducer(), request.getThreshold(), request.getLimit()), null, HttpStatus.OK);
            }
        }catch(Exception e){
            logger.error(debug + "EXCEPTION: " + e.getMessage());
            for (StackTraceElement element : e.getStackTrace()) {
                logger.error (debug + element);
            }
            return new ResponseEntity<>(new SimilaritiesListResponseDto("ERROR","ERROR: "+e.getMessage()),null,HttpStatus.INTERNAL_SERVER_ERROR);
        }finally{
            logger.info(debug + "END @ " + (System.currentTimeMillis()-start) + "msec.");
        }
    }

    @RequestMapping(method=RequestMethod.POST,value="/refreshSimilarities",produces="application/json")
    public ResponseEntity<SimilaritiesResponseDto> refreshSimilarities (@RequestBody RequestDto request) {
        String debug = "authenticate::username:"+request.getUsername()+". ";
        logger.info(debug + "START.");
        long start = System.currentTimeMillis();
        try{
            return new ResponseEntity<>(processor.refreshSimilarities(request.getUsername(),request.getPassword()),null,HttpStatus.OK);
        }catch(Exception e){
            logger.error(debug + "EXCEPTION: " + e.getMessage());
            for (StackTraceElement element : e.getStackTrace()) {
                logger.error (debug + element);
            }
            return new ResponseEntity<>(new SimilaritiesResponseDto("ERROR","ERROR: "+e.getMessage()),null,HttpStatus.INTERNAL_SERVER_ERROR);
        }finally{
            logger.info(debug + "END @ " + (System.currentTimeMillis()-start) + "msec.");
        }
    }

    @RequestMapping(method=RequestMethod.POST,value="/authenticate",produces="application/json")
    public ResponseEntity<AuthenticationResponseDto> authenticate (@RequestBody RequestDto request) {
        String debug = "authenticate::username:"+request.getUsername()+". ";
        logger.info(debug + "START.");
        long start = System.currentTimeMillis();
        try{
            return new ResponseEntity<>(processor.authenticate(request.getUsername(),request.getPassword(),true),null,HttpStatus.OK);
        }catch(Exception e){
            logger.error(debug + "EXCEPTION: " + e.getMessage());
            for (StackTraceElement element : e.getStackTrace()) {
                logger.error (debug + element);
            }
            return new ResponseEntity<>(new AuthenticationResponseDto("ERROR","ERROR: "+e.getMessage()),null,HttpStatus.INTERNAL_SERVER_ERROR);
        }finally{
            logger.info(debug + "END @ " + (System.currentTimeMillis()-start) + "msec.");
        }
    }

    @RequestMapping(method=RequestMethod.POST,value="/register",produces="application/json")
    public ResponseEntity<ResponseDto> register (@RequestBody RequestDto request) {
        String debug = "register::username:"+request.getUsername()+". ";
        logger.info(debug + "START.");
        long start = System.currentTimeMillis();
        try{
            return new ResponseEntity<>(processor.register(request.getUsername(), request.getPassword(),
                                        request.getFirstname(), request.getLastname(),
                                        request.getMsisdn(), request.getEmail(),request.getRegion()),null,HttpStatus.OK);
        }catch(Exception e){
            logger.error(debug + "EXCEPTION: " + e.getMessage());
            for (StackTraceElement element : e.getStackTrace()) {
                logger.error (debug + element);
            }
            return new ResponseEntity<>(new ResponseDto("ERROR","ERROR: "+e.getMessage()),null,HttpStatus.INTERNAL_SERVER_ERROR);
        }finally{
            logger.info(debug + "END @ " + (System.currentTimeMillis()-start) + "msec.");
        }
    }

    @RequestMapping(method=RequestMethod.POST,value="/updateUserDetails",produces="application/json")
    public ResponseEntity<ResponseDto> updateUserDetails (@RequestBody RequestDto request) {
        String debug = "updateUserDetails::username:"+request.getUsername()+". ";
        logger.info(debug + "START.");
        long start = System.currentTimeMillis();
        try{
            return new ResponseEntity<>(processor.updateUserDetails(request.getUsername(), request.getPassword(),
                    request.getFirstname(), request.getLastname(),
                    request.getEmail(), request.getMsisdn(), request.getRegion()),null,HttpStatus.OK);
        }catch(Exception e){
            logger.error(debug + "EXCEPTION: " + e.getMessage());
            for (StackTraceElement element : e.getStackTrace()) {
                logger.error (debug + element);
            }
            return new ResponseEntity<>(new ResponseDto("ERROR","ERROR: "+e.getMessage()),null,HttpStatus.INTERNAL_SERVER_ERROR);
        }finally{
            logger.info(debug + "END @ " + (System.currentTimeMillis()-start) + "msec.");
        }
    }

    @RequestMapping(method=RequestMethod.POST,value="/activateUser",produces="application/json")
    public ResponseEntity<ResponseDto> activateUser (@RequestBody RequestDto request) {
        String debug = "activateUser::username:"+request.getUsername()+". ";
        logger.info(debug + "START.");
        long start = System.currentTimeMillis();
        try{
            return new ResponseEntity<>(processor.activateUser(request.getUsername(), request.getPassword(),
                    request.getToActivate()),null,HttpStatus.OK);
        }catch(Exception e){
            logger.error(debug + "EXCEPTION: " + e.getMessage());
            for (StackTraceElement element : e.getStackTrace()) {
                logger.error (debug + element);
            }
            return new ResponseEntity<>(new ResponseDto("ERROR","ERROR: "+e.getMessage()),null,HttpStatus.INTERNAL_SERVER_ERROR);
        }finally{
            logger.info(debug + "END @ " + (System.currentTimeMillis()-start) + "msec.");
        }
    }

    @RequestMapping(method=RequestMethod.POST,value="/disableUser",produces="application/json")
    public ResponseEntity<ResponseDto> disableUser (@RequestBody RequestDto request) {
        String debug = "disableUser::username:"+request.getUsername()+". ";
        logger.info(debug + "START.");
        long start = System.currentTimeMillis();
        try{
            return new ResponseEntity<>(processor.disableUser(request.getUsername(), request.getPassword(),
                    request.getToDisable()),null,HttpStatus.OK);
        }catch(Exception e){
            logger.error(debug + "EXCEPTION: " + e.getMessage());
            for (StackTraceElement element : e.getStackTrace()) {
                logger.error (debug + element);
            }
            return new ResponseEntity<>(new ResponseDto("ERROR","ERROR: "+e.getMessage()),null,HttpStatus.INTERNAL_SERVER_ERROR);
        }finally{
            logger.info(debug + "END @ " + (System.currentTimeMillis()-start) + "msec.");
        }
    }

    @RequestMapping(method=RequestMethod.POST,value="/getUsers",produces="application/json",consumes="application/json")
    public ResponseEntity<UsersListResponseDto> getUsers (@RequestBody RequestDto request) {
        String debug = "getUsers::username:"+request.getUsername()+". ";
        logger.info(debug + "START.");
        long start = System.currentTimeMillis();
        try{
            return new ResponseEntity<>(processor.getUsers(request.getUsername(), request.getPassword()),null,HttpStatus.OK);
        }catch(Exception e){
            logger.error(debug + "EXCEPTION: " + e.getMessage());
            for (StackTraceElement element : e.getStackTrace()) {
                logger.error (debug + element);
            }
            return new ResponseEntity<>(new UsersListResponseDto("ERROR","ERROR: "+e.getMessage()),null,HttpStatus.INTERNAL_SERVER_ERROR);
        }finally{
            logger.info(debug + "END @ " + (System.currentTimeMillis()-start) + "msec.");
        }
    }
}
