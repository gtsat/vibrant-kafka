package com.homegrown.services.iface;

import com.homegrown.services.engine.Processor;
import com.homegrown.services.model.*;
import org.apache.log4j.Logger;

import javax.jws.WebParam;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.annotation.Resource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.ws.WebServiceContext;
import javax.xml.bind.annotation.XmlElement;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class SoapService implements ApplicationContextAware {
    private ApplicationContext ctx;
    @Override @WebMethod(exclude=true) public void setApplicationContext(ApplicationContext context) {this.ctx=context;}

    private static Logger logger = Logger.getLogger(SoapService.class);

    private static Processor processor;
    @WebMethod(exclude=true) public Processor getProcessor() {return processor;}
    @WebMethod(exclude=true) public void setProcessor(Processor processor) {this.processor = processor;}

    @Resource WebServiceContext wsContext;

    private static final JAXBContext JAXB_CONTEXT;

    static{
        try {
            JAXB_CONTEXT = JAXBContext.newInstance
                    (
                            EventListResponseDto.class,
                            AuthenticationResponseDto.class,
                            KafkaConsumerResponseDto.class,
                            UsersListResponseDto.class,
                            ResponseDto.class
                    );
        }catch(JAXBException je){
            je.printStackTrace();
            logger.fatal("Could not initialize JAXBContext instance!");
            throw new RuntimeException("Could not initialize JAXBContext instance", je);
        }
    }


    @WebMethod
    public ResponseDto resetAllProducers (
            @XmlElement(required=true)
            @WebParam(name="username") String username,
            @XmlElement(required=true)
            @WebParam(name="password") String password
    ) {
        String debug = "resetAllProducers::username:"+username+". ";
        logger.info(debug + "START.");
        long start = System.currentTimeMillis();
        try{
            return processor.resetAllProducers(username, password);
        }catch(Exception e){
            logger.error(debug + "EXCEPTION: " + e.getMessage());
            for (StackTraceElement element : e.getStackTrace()) {
                logger.error (debug + element);
            }
            return new ResponseDto("ERROR","ERROR: "+e.getMessage());
        }finally{
            logger.info(debug + "END @ " + (System.currentTimeMillis()-start) + "msec.");
        }
    }

    @WebMethod
    public ResponseDto resetProducer (
            @XmlElement(required=true)
            @WebParam(name="username") String username,
            @XmlElement(required=true)
            @WebParam(name="password") String password,
            @XmlElement(required=true)
            @WebParam(name="producer") String producer
    ) {
        String debug = "resetProducer::username:"+username+". ";
        logger.info(debug + "START.");
        long start = System.currentTimeMillis();
        try{
            return processor.resetProducer(username, password, producer);
        }catch(Exception e){
            logger.error(debug + "EXCEPTION: " + e.getMessage());
            for (StackTraceElement element : e.getStackTrace()) {
                logger.error (debug + element);
            }
            return new ResponseDto("ERROR","ERROR: "+e.getMessage());
        }finally{
            logger.info(debug + "END @ " + (System.currentTimeMillis()-start) + "msec.");
        }
    }

    @WebMethod
    public ResponseDto clearEvents (
            @XmlElement(required=true)
            @WebParam(name="username") String username,
            @XmlElement(required=true)
            @WebParam(name="password") String password,
            @XmlElement(required=true)
            @WebParam(name="producer") String producer
    ) {
        String debug = "clearEvents::username:"+username+". ";
        logger.info(debug + "START.");
        long start = System.currentTimeMillis();
        try{
            return processor.clearEvents(username, password, producer);
        }catch(Exception e){
            logger.error(debug + "EXCEPTION: " + e.getMessage());
            for (StackTraceElement element : e.getStackTrace()) {
                logger.error (debug + element);
            }
            return new ResponseDto("ERROR","ERROR: "+e.getMessage());
        }finally{
            logger.info(debug + "END @ " + (System.currentTimeMillis()-start) + "msec.");
        }
    }

    @WebMethod
    public EventListResponseDto getEvents (
            @XmlElement(required=true)
            @WebParam(name="username") String username,
            @XmlElement(required=true)
            @WebParam(name="password") String password,
            @XmlElement(required=true)
            @WebParam(name="producer") String producer,
            @XmlElement(required=true)
            @WebParam(name="threshold") Integer threshold,
            @XmlElement(required=true)
            @WebParam(name="limit") Integer limit
    ) {
        String debug = "getEvents::username:"+username+". ";
        logger.info(debug + "START.");
        long start = System.currentTimeMillis();
        try{
            return processor.getEvents(username, password, producer, threshold, limit);
        }catch(Exception e){
            logger.error(debug + "EXCEPTION: " + e.getMessage());
            for (StackTraceElement element : e.getStackTrace()) {
                logger.error (debug + element);
            }
            return new EventListResponseDto("ERROR","ERROR: "+e.getMessage());
        }finally{
            logger.info(debug + "END @ " + (System.currentTimeMillis()-start) + "msec.");
        }
    }

    @WebMethod
    public KafkaConsumerResponseDto updateConsumer (
            @XmlElement(required=true)
            @WebParam(name="username") String username,
            @XmlElement(required=true)
            @WebParam(name="password") String password,
            @XmlElement(required=true)
            @WebParam(name="category") String category
    ) {
        String debug = "updateConsumer::username:"+username+". ";
        logger.info(debug + "START.");
        long start = System.currentTimeMillis();
        try{
            return processor.updateConsumer(username, password, category);
        }catch(Exception e){
            logger.error(debug + "EXCEPTION: " + e.getMessage());
            for (StackTraceElement element : e.getStackTrace()) {
                logger.error (debug + element);
            }
            return new KafkaConsumerResponseDto("ERROR","ERROR: "+e.getMessage());
        }finally{
            logger.info(debug + "END @ " + (System.currentTimeMillis()-start) + "msec.");
        }
    }

    @WebMethod
    public AuthenticationResponseDto authenticate (
            @XmlElement(required=true)
            @WebParam(name="username") String username,
            @XmlElement(required=true)
            @WebParam(name="password") String password
    ) {
        String debug = "authenticate::username:"+username+". ";
        logger.info(debug + "START.");
        long start = System.currentTimeMillis();
        try{
            return processor.authenticate(username,password,true);
        }catch(Exception e){
            logger.error(debug + "EXCEPTION: " + e.getMessage());
            for (StackTraceElement element : e.getStackTrace()) {
                logger.error (debug + element);
            }
            return new AuthenticationResponseDto("ERROR","ERROR: "+e.getMessage());
        }finally{
            logger.info(debug + "END @ " + (System.currentTimeMillis()-start) + "msec.");
        }
    }

    @WebMethod
    public ResponseDto register (
            @XmlElement(required=true)
            @WebParam(name="username") String username,
            @XmlElement(required=true)
            @WebParam(name="password") String password,
            @XmlElement(required=true)
            @WebParam(name="firstname") String firstname,
            @XmlElement(required=true)
            @WebParam(name="lastname") String lastname,
            @XmlElement(required=true)
            @WebParam(name="msisdn") String msisdn,
            @XmlElement(required=true)
            @WebParam(name="email") String email,
            @XmlElement(required=true)
            @WebParam(name="region") String region
    ) {
        String debug = "register::username:"+username+". ";
        logger.info(debug + "START.");
        long start = System.currentTimeMillis();
        try{
            return processor.register(username,password,firstname,lastname,msisdn,email,region);
        }catch(Exception e){
            logger.error(debug + "EXCEPTION: " + e.getMessage());
            for (StackTraceElement element : e.getStackTrace()) {
                logger.error (debug + element);
            }
            return new ResponseDto("ERROR","ERROR: "+e.getMessage());
        }finally{
            logger.info(debug + "END @ " + (System.currentTimeMillis()-start) + "msec.");
        }
    }

    @WebMethod
    public ResponseDto updateUserDetails (
            @XmlElement(required=true)
            @WebParam(name="username") String username,
            @XmlElement(required=true)
            @WebParam(name="password") String password,
            @XmlElement(required=true)
            @WebParam(name="firstname") String firstname,
            @XmlElement(required=true)
            @WebParam(name="lastname") String lastname,
            @XmlElement(required=true)
            @WebParam(name="msisdn") String msisdn,
            @XmlElement(required=true)
            @WebParam(name="email") String email,
            @XmlElement(required=true)
            @WebParam(name="region") String region
    ) {
        String debug = "updateUserDetails::username:"+username+". ";
        logger.info(debug + "START.");
        long start = System.currentTimeMillis();
        try{
            return processor.updateUserDetails(username, password,
                    firstname, lastname,
                    email, msisdn,
                    region);
        }catch(Exception e){
            logger.error(debug + "EXCEPTION: " + e.getMessage());
            for (StackTraceElement element : e.getStackTrace()) {
                logger.error (debug + element);
            }
            return new ResponseDto("ERROR","ERROR: "+e.getMessage());
        }finally{
            logger.info(debug + "END @ " + (System.currentTimeMillis()-start) + "msec.");
        }
    }

    @WebMethod
    public ResponseDto activateUser (
            @XmlElement(required=true)
            @WebParam(name="username") String username,
            @XmlElement(required=true)
            @WebParam(name="password") String password,
            @XmlElement(required=true)
            @WebParam(name="toActivate") String toActivate
    ) {
        String debug = "activateUser::username:"+username+". ";
        logger.info(debug + "START.");
        long start = System.currentTimeMillis();
        try{
            return processor.activateUser(username,password,toActivate);
        }catch(Exception e){
            logger.error(debug + "EXCEPTION: " + e.getMessage());
            for (StackTraceElement element : e.getStackTrace()) {
                logger.error (debug + element);
            }
            return new ResponseDto("ERROR","ERROR: "+e.getMessage());
        }finally{
            logger.info(debug + "END @ " + (System.currentTimeMillis()-start) + "msec.");
        }
    }

    @WebMethod
    public ResponseDto disableUser (
            @XmlElement(required=true)
            @WebParam(name="username") String username,
            @XmlElement(required=true)
            @WebParam(name="password") String password,
            @XmlElement(required=true)
            @WebParam(name="toDisable") String toDisable
    ) {
        String debug = "disableUser::username:"+username+". ";
        logger.info(debug + "START.");
        long start = System.currentTimeMillis();
        try{
            return processor.disableUser(username,password,toDisable);
        }catch(Exception e){
            logger.error(debug + "EXCEPTION: " + e.getMessage());
            for (StackTraceElement element : e.getStackTrace()) {
                logger.error (debug + element);
            }
            return new ResponseDto("ERROR","ERROR: "+e.getMessage());
        }finally{
            logger.info(debug + "END @ " + (System.currentTimeMillis()-start) + "msec.");
        }
    }

    @WebMethod
    public UsersListResponseDto getUsers (
            @XmlElement(required=true)
            @WebParam(name="username") String username,
            @XmlElement(required=true)
            @WebParam(name="password") String password
    ) {
        String debug = "getUsers::username:"+username+". ";
        logger.info(debug + "START.");
        long start = System.currentTimeMillis();
        try{
            return processor.getUsers(username, password);
        }catch(Exception e){
            logger.error(debug + "EXCEPTION: " + e.getMessage());
            for (StackTraceElement element : e.getStackTrace()) {
                logger.error (debug + element);
            }
            return new UsersListResponseDto("ERROR","ERROR: "+e.getMessage());
        }finally{
            logger.info(debug + "END @ " + (System.currentTimeMillis()-start) + "msec.");
        }
    }
}
