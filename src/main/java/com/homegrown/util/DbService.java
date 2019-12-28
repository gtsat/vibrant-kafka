package com.homegrown.util;

import com.homegrown.services.model.*;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.SQLException;
import javax.transaction.Transactional;


public class DbService {
    private static Logger logger = Logger.getLogger (DbService.class);

    private JdbcTemplate dbTemplate;
    public JdbcTemplate getDbTemplate() {return dbTemplate;}
    public void setDbTemplate(JdbcTemplate dbTemplate) {this.dbTemplate = dbTemplate;}


    public int deleteBenchmark (String producer) {
        try{
            assert (dbTemplate != null);
            String sql = "DELETE FROM benchmarks WHERE producer=?";
            logger.info("DBService | deleteBenchmark() | Executing query: "+sql);
            return dbTemplate.update(sql,producer);
        }catch (Exception e){
            for (StackTraceElement elem : e.getStackTrace()) {
                logger.error(elem);
            }
            logger.error("DBService | deleteBenchmark() | EXCEPTION:\n" + e.getMessage() + "\n" + e);
        }
        return -1;
    }

    public int insertBenchmark (String producer, float frequency, byte[] sample) {
        try{
            String sql = "INSERT INTO benchmarks (producer,frequency,sample,creation_date) VALUES (?,?,?,CURRENT_TIMESTAMP)";
            logger.info("DBService | insertBenchmark() | Executing query: "+sql);
            StringBuilder str = new StringBuilder();
            for (byte b : sample) {
                str.append('\n');
                str.append(b);
            }
            if (str.length()>0) {
                str.deleteCharAt(0);
            }
            return dbTemplate.update(sql,producer,frequency,str.toString());
        }catch (Exception e){
            for (StackTraceElement elem : e.getStackTrace()) {
                logger.error(elem);
            }
            logger.error("DBService | insertBenchmark() | EXCEPTION:\n" + e.getMessage() + "\n" + e);
        }
        return -1;
    }

    private static class BenchmarkSampleMapper implements RowMapper {
        public Object mapRow (ResultSet rs, int rowNum) throws SQLException {
            BenchmarkSample sample = null;
            try{
                BufferedReader b = new BufferedReader(new InputStreamReader((rs.getClob("SAMPLE").getAsciiStream())));
                List<Byte> data = new ArrayList<>();
                for (String line=null;(line = b.readLine())!=null;) {
                    data.add((byte)Integer.parseInt(line));
                }
                sample = new BenchmarkSample(
                        rs.getString("PRODUCER"),
                        rs.getFloat("FREQUENCY"),
                        data,
                        rs.getTimestamp("CREATION_DATE"));
            }catch (Exception e) {
                for (StackTraceElement elem : e.getStackTrace()) {
                    logger.error(elem);
                }
                logger.error("DBService | BenchmarkSampleMapper | EXCEPTION:\n" + e.getMessage() + "\n" + e);
            }
            return sample;
        }
    }

    public BenchmarkSample getBenchmark (String producer) {
        BenchmarkSample sample = null;
        try{
            assert (dbTemplate != null);
            String sql = "SELECT producer,frequency,sample,creation_date FROM benchmarks WHERE producer=?";
            logger.info("DBService | getBenchmark() | Executing query: "+sql);
            sample = (BenchmarkSample) dbTemplate.queryForObject (sql, new Object[]{producer}, new BenchmarkSampleMapper());
        }catch (Exception e){
            for (StackTraceElement elem : e.getStackTrace()) {
                logger.error(elem);
            }
            logger.error("DBService | getBenchmark() | EXCEPTION:\n" + e.getMessage() + "\n" + e);
        }
        return sample;
    }

    private static class EventMapper implements RowMapper {
        public Object mapRow (ResultSet rs, int rowNum) throws SQLException {
            Event event = new Event();
            try{
                event.setOffset(rs.getInt("OFFSET"));
                event.setProducer(rs.getString("PRODUCER"));
                event.setSimilarity(rs.getInt("SIMILARITY"));
                event.setCreationDate(rs.getTimestamp("CREATION_DATE")!=null?new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(rs.getTimestamp("CREATION_DATE")):null);
            }catch (Exception e) {
                for (StackTraceElement elem : e.getStackTrace()) {
                    logger.error(elem);
                }
                logger.error("DBService | EventMapper | EXCEPTION:\n" + e.getMessage() + "\n" + e);
            }
            return event;
        }
    }

    public int clearEvents (String producer) {
        try{
            assert (dbTemplate != null);
            String sql = "DELETE FROM events WHERE producer=?";
            logger.info("DBService | clearEvents() | Executing query: "+sql);
            return dbTemplate.update(sql,producer);
        }catch (Exception e){
            for (StackTraceElement elem : e.getStackTrace()) {
                logger.error(elem);
            }
            logger.error("DBService | clearEvents() | EXCEPTION:\n" + e.getMessage() + "\n" + e);
        }
        return -1;
    }

    public int insertEvent (String producer, int similarity, long offset, Timestamp timestamp) {
        try{
            assert (dbTemplate != null);
            String sql = "INSERT INTO events (offset,producer,similarity,creation_date) VALUES (?,?,?,?)";
            logger.info("DBService | insertEvent() | Executing query: "+sql);
            return dbTemplate.update(sql,offset,producer,similarity,timestamp);
        }catch (Exception e){
            for (StackTraceElement elem : e.getStackTrace()) {
                logger.error(elem);
            }
            logger.error("DBService | insertEvent() | EXCEPTION:\n" + e.getMessage() + "\n" + e);
        }
        return -1;
    }

    public List<Event> getEvents (String producer) {return getEvents (producer,0,10);}
    public List<Event> getEvents (String producer, Integer threshold, Integer limit) {
        assert (dbTemplate != null);
        String sql = "select * from events where producer=? and similarity<? order by creation_date asc, similarity desc limit ?";
        logger.info("DBService | getEvents() | Executing query: "+sql);
        try{
            return (List<Event>) dbTemplate.query(sql, new Object[]{producer,threshold,limit}, new EventMapper());
        }catch (Exception e){
            for (StackTraceElement elem : e.getStackTrace()) {
                logger.error(elem);
            }
            logger.error("DBService | getEvents() | EXCEPTION:\n" + e.getMessage() + "\n" + e);
        }
        return new ArrayList<>();
    }

    private static class UserMapper implements RowMapper {
        public Object mapRow (ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            try{
                user.setUsername (rs.getString("USERNAME"));
                user.setPassword (rs.getString("PASSWORD"));
                user.setFirstname (rs.getString("FIRSTNAME"));
                user.setLastname (rs.getString("LASTNAME"));
                user.setMsisdn (rs.getString("MSISDN"));
                user.setEmail (rs.getString("EMAIL"));
                user.setStatus(rs.getBoolean("STATUS"));
                user.setAdmin(rs.getBoolean("ADMIN"));
                user.setRegion(rs.getString("REGION"));
                user.setLastUsage(rs.getTimestamp("LAST_USAGE")!=null?new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(rs.getTimestamp("LAST_USAGE")):null);
                user.setCreationDate(rs.getTimestamp("CREATION_DATE")!=null?new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(rs.getTimestamp("CREATION_DATE")):null);
            }catch (Exception e) {
                for (StackTraceElement elem : e.getStackTrace()) {
                    logger.error(elem);
                }
                logger.error("DBService | UserMapper | EXCEPTION:\n" + e.getMessage() + "\n" + e);
            }
            return user;
        }
    }

    public User getUserByUsername (String username) {
        assert (dbTemplate != null);
        String sql = "select * from accounts where username=? and status='1'";
        logger.info("DBService | getUserByUsername() | Executing query: "+sql);
        try{
            List<User> userList = (List<User>) dbTemplate.query(sql, new Object[]{username}, new UserMapper());
            if (userList != null && userList.size() > 0) return userList.get(0);
        }catch (Exception e){
            for (StackTraceElement elem : e.getStackTrace()) {
                logger.error(elem);
            }
            logger.error("DBService | getUserByUsername() | EXCEPTION:\n" + e.getMessage() + "\n" + e);
        }
        return null;
    }

    public List<User> getUsers () {
        assert (dbTemplate != null);
        String sql = "select * from accounts";
        logger.info("DBService | getUsers() | Executing query: "+sql);
        try{
            return (List<User>) dbTemplate.query(sql, new Object[]{}, new UserMapper());
        }catch (Exception e){
            for (StackTraceElement elem : e.getStackTrace()) {
                logger.error(elem);
            }
            logger.error("DBService | getUsers() | EXCEPTION:\n" + e.getMessage() + "\n" + e);
        }
        return new ArrayList<>();
    }

    @Transactional
    public String insertUser (String username, String password,
                           String firstname, String lastname,
                           String msisdn, String email,
                           Integer quotaMemory, Integer quotaDisk, Integer quotaCpus,
                           String region) {
        try{
            assert (dbTemplate != null);
            String sql = "INSERT INTO accounts (username,password,firstname,lastname,msisdn,email,quota_memory,quota_disk,quota_cpus,free_memory,free_disk,free_cpus,status,region) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,0,?,?)";
            logger.info("DBService | insertUser() | Executing query: "+sql);
            int rval = dbTemplate.update(sql, username, password, firstname, lastname, msisdn, email, quotaMemory, quotaDisk, quotaCpus, quotaMemory,quotaDisk,quotaCpus,region);
            if (rval > 0) {
                return null;
            }
        }catch (Exception e){
            for (StackTraceElement elem : e.getStackTrace()) {
                logger.error(elem);
            }
            logger.error("DBService | insertUser() | EXCEPTION:\n" + e.getMessage() + "\n" + e);
            return e.getMessage();
        }
        return "ERROR: Unable to insert user '"+username+"' in database.";
    }

    @Transactional
    public int updateUserDetails (String username, String firstname, String lastname, String email, String msisdn, String region) {
        try{
            assert (dbTemplate != null);
            String sql = "UPDATE accounts SET " +
                    "firstname=?, lastname=?," +
                    "email=?, msisdn=?, region=? " +
                    "WHERE username=? and status='1'";
            logger.info("DBService | updateUserDetails() | Executing query: "+sql);
            return dbTemplate.update(sql, firstname,lastname,email,msisdn,region,username);
        }catch (Exception e){
            for (StackTraceElement elem : e.getStackTrace()) {
                logger.error(elem);
            }
            logger.error("DBService | updateUserDetails() | EXCEPTION:\n" + e.getMessage() + "\n" + e);
        }
        return -1;
    }

    @Transactional
    public int activateUser (String username) {
        try{
            assert (dbTemplate != null);
            String sql = "UPDATE accounts SET status='1' WHERE username=? and status=0";
            logger.info("DBService | activateUser() | Executing query: "+sql);
            return dbTemplate.update(sql,username);
        }catch (Exception e){
            for (StackTraceElement elem : e.getStackTrace()) {
                logger.error(elem);
            }
            logger.error("DBService | activateUser() | EXCEPTION:\n" + e.getMessage() + "\n" + e);
        }
        return -1;
    }

    @Transactional
    public int disableUser (String username) {
        try{
            assert (dbTemplate != null);
            String sql = "UPDATE accounts SET status=0 WHERE username=? and status='1'";
            logger.info("DBService | disableUser() | Executing query: "+sql);
            return dbTemplate.update(sql,username);
        }catch (Exception e){
            for (StackTraceElement elem : e.getStackTrace()) {
                logger.error(elem);
            }
            logger.error("DBService | disableUser() | EXCEPTION:\n" + e.getMessage() + "\n" + e);
        }
        return -1;
    }

    @Transactional
    public int touchUser (String username) {
        try{
            assert (dbTemplate != null);
            String sql = "UPDATE accounts SET last_usage=CURRENT_TIMESTAMP WHERE username=? and status='1'";
            logger.info("DBService | touchUser() | Executing query: "+sql);
            return dbTemplate.update(sql,username);
        }catch (Exception e){
            for (StackTraceElement elem : e.getStackTrace()) {
                logger.error(elem);
            }
            logger.error("DBService | touchUser() | EXCEPTION:\n" + e.getMessage() + "\n" + e);
        }
        return -1;
    }
}
