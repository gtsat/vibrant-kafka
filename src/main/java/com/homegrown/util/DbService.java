package com.homegrown.util;

import com.homegrown.services.model.*;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.apache.log4j.Logger;

import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbService {
    private static Logger logger = Logger.getLogger (DbService.class);

    private JdbcTemplate dbTemplate;
    public JdbcTemplate getDbTemplate() {
        return dbTemplate;
    }
    public void setDbTemplate(JdbcTemplate dbTemplate) {
        this.dbTemplate = dbTemplate;
    }

    private static class SimilarityMapper implements RowMapper {
        public Object mapRow (ResultSet rs, int rowNum) throws SQLException {
            Similarity sim = new Similarity();
            try{
                sim.setId(rs.getInt("ID"));
                sim.setProducer(rs.getString("PRODUCER"));
                sim.setSimilarity(rs.getInt("SIMILARITY"));
                sim.setCreationDate(rs.getTimestamp("CREATION_DATE")!=null?new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(rs.getTimestamp("CREATION_DATE")):null);
            }catch (Exception e) {e.printStackTrace();}
            return sim;
        }
    }

    public int insertSimilarity (String producer, int similarity) {
        try{
            assert (dbTemplate != null);
            String sql = "INSERT INTO similarities (producer,similarity,creation_date) VALUES (?,?,CURRENT_TIMESTAMP)";
            logger.info("DBService | insertUser() | Executing query: "+sql);
            return getDbTemplate().update(sql,producer,similarity);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("DBService | insertSimilarity() | EXCEPTION:\n" + e.getMessage() + "\n" + e);
        }
        return -1;
    }

    public List<Similarity> getSimilaritiesByProducer (String producer) {return getSimilaritiesByProducer(producer,0,10);}
    public List<Similarity> getSimilaritiesByProducer (String producer, Integer threshold, Integer limit) {
        assert (dbTemplate != null);
        String sql = "select * from similarities where producer = ? and similarity < ? order by creation_date asc, similarity desc limit ?";
        logger.info("DBService | getSimilaritiesByProducer() | Executing query: "+sql);
        try{
            return (List<Similarity>) getDbTemplate().query(sql, new Object[]{producer,threshold,limit}, new SimilarityMapper());
        }catch (Exception e){
            e.printStackTrace();
            logger.error("DBService | getSimilaritiesByProducer() | EXCEPTION:\n" + e.getMessage() + "\n" + e);
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
            }catch (Exception e) {e.printStackTrace();}
            return user;
        }
    }

    public User getUserByUsername (String username) {
        assert (dbTemplate != null);
        String sql = "select * from accounts where username=? and status='1'";
        logger.info("DBService | getUserByUsername() | Executing query: "+sql);
        try{
            List<User> userList = (List<User>) getDbTemplate().query(sql, new Object[]{username}, new UserMapper());
            if (userList != null && userList.size() > 0) return userList.get(0);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("DBService | getUserByUsername() | EXCEPTION:\n" + e.getMessage() + "\n" + e);
        }
        return null;
    }

    public List<User> getUsers () {
        assert (dbTemplate != null);
        String sql = "select * from accounts";
        logger.info("DBService | getUsers() | Executing query: "+sql);
        try{
            return (List<User>) getDbTemplate().query(sql, new Object[]{}, new UserMapper());
        }catch (Exception e){
            e.printStackTrace();
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
            int rval = getDbTemplate().update(sql,username,password,firstname,lastname,msisdn,email,quotaMemory,quotaDisk,quotaCpus,quotaMemory,quotaDisk,quotaCpus,region);
            if (rval > 0) {
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
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
            return getDbTemplate().update(sql,firstname,lastname,email,msisdn,region,username);
        }catch (Exception e){
            e.printStackTrace();
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
            return getDbTemplate().update(sql,username);
        }catch (Exception e){
            e.printStackTrace();
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
            return getDbTemplate().update(sql,username);
        }catch (Exception e){
            e.printStackTrace();
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
            return getDbTemplate().update(sql,username);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("DBService | touchUser() | EXCEPTION:\n" + e.getMessage() + "\n" + e);
        }
        return -1;
    }
}
