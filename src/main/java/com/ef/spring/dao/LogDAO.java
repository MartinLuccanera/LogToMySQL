package com.ef.spring.dao;

import com.ef.spring.model.Parameters;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@Service
@Transactional(propagation = Propagation.SUPPORTS)
public class LogDAO {
    private final static Logger log = Logger.getLogger(LogDAO.class);

    private static String datasourceUrl;
    private static String datasourceUsername;
    private static String datasourcePassword;
    private static String tableName;
    private static String databaseName;
    private static long batchSize;

    private final JdbcTemplate jdbcTemplate;

    /**
     * <p>Imports data from provided log file into MySQL database.</p>
     *
     * @param file log file to get records from.
     */
    public void importLogIntoDB(String file) {
        String dateString = null;
        String ip;
        String request;
        int status;
        String user_agent;
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            // Establish connection.
            connection = DriverManager.getConnection(datasourceUrl, datasourceUsername, datasourcePassword);
            connection.createStatement();

            // Set query
            StringBuilder sb = new StringBuilder("INSERT INTO \n")
                    .append(databaseName)
                    .append(".")
                    .append(tableName)
                    .append("(date, ip, request, status, user_agent)")
                    .append(" VALUES (?, ?, ?, ?, ?)")
                    ;
            preparedStatement = connection.prepareStatement(sb.toString());

            // Stream-read from CSV file
            Reader reader = Files.newBufferedReader(Paths.get(file));
            CSVParser csvParser = new CSVParser(
                    reader,
                    CSVFormat.DEFAULT
                            .withQuote(null)
                            .withDelimiter('|')
            );

            // Assemble statement to store data in DB
            for (CSVRecord csvRecord : csvParser) {
                // Accessing Values by Column Index
                dateString = csvRecord.get(0);
                ip = csvRecord.get(1);
                request = csvRecord.get(2).replace("\"", "");
                status = Integer.parseInt(csvRecord.get(3));
                user_agent = csvRecord.get(4).replace("\"", "");


                Timestamp date = new Timestamp(
                        new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS")
                                .parse(dateString).getTime()
                );

                preparedStatement.setTimestamp(1, date);
                preparedStatement.setString(2, ip);
                preparedStatement.setString(3, request);
                preparedStatement.setInt(4, status);
                preparedStatement.setString(5, user_agent);

                preparedStatement.addBatch();

                // When record count reaches batchSize, send to DB.
                if (csvRecord.getRecordNumber() % batchSize == 0) {
                    preparedStatement.executeBatch();
                }
            }

            preparedStatement.executeBatch();
        } catch (SQLException e) {
            log.error("failed retrieving connection to: " + datasourceUrl, e);
        } catch (ParseException e) {
            log.error("Failed to parse date: " + dateString, e);
        } catch (IOException e) {
            log.error("failed to open source file: " + file, e);
        } finally {
            try {
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                log.error("failed to close DB connection", e);
            }
        }
    }

    public void findAll(Parameters parameters) {

        String sql = new StringBuilder(
                "SELECT * FROM log l ")
                .append("WHERE l.date BETWEEN ? AND ? ")
                .append("GROUP BY l.ip ")
                .append("HAVING count(l.ip) >= ?")
                .toString();

        List<Map<String, Object>> logRecords = jdbcTemplate.queryForList(sql,
                parameters.getStartDateAsString(), //"2017-01-01 03:00:00",
                parameters.getEndDateAsString(), //"2017-01-01 04:00:00",
                String.valueOf(parameters.getThreshold()) //"100"
        );

        for (Map<String, Object> logRecord : logRecords) {
            System.out.println(logRecord);
        }
    }

    @Autowired
    public LogDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Value("${spring.datasource.url}")
    public void setDatasourceUrl(String url) {
        datasourceUrl = url;
    }

    @Value("${spring.datasource.username}")
    public void setDatasourceUsername(String username) {
        datasourceUsername = username;
    }

    @Value("${spring.datasource.password}")
    public void setDatasourcePassword(String password) {
        datasourcePassword = password;
    }

    @Value("${spring.datasource.batch.size}")
    public void setBatchSize(long size) {
        batchSize = size;
    }

    @Value("${spring.datasource.database.name}")
    public void setDatabaseName(String name) {
        databaseName = name;
    }

    @Value("${spring.datasource.table.name}")
    public void setTableName(String name) {
        tableName = name;
    }
}
