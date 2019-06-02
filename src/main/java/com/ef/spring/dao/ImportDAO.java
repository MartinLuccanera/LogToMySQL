package com.ef.spring.dao;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
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

@Service
@Transactional(propagation = Propagation.SUPPORTS)
public class ImportDAO {
    private final static Logger logger = Logger.getLogger(ImportDAO.class);

    private static String datasourceUrl;
    private static String datasourceUsername;
    private static String datasourcePassword;
    private static String tableName;
    private static String databaseName;
    private static long batchSize;

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
            connection = DriverManager.getConnection(datasourceUrl, datasourceUsername, datasourcePassword);
            connection.createStatement();

            StringBuilder sb = new StringBuilder("INSERT INTO \n")
                    .append(databaseName)
                    .append(".")
                    .append(tableName)
                    .append("(date, ip, request, status, user_agent)")
                    .append(" VALUES (?, ?, ?, ?, ?)")
                    ;
            preparedStatement = connection.prepareStatement(sb.toString());

            Reader reader = Files.newBufferedReader(Paths.get(file));
            CSVParser csvParser = new CSVParser(
                    reader,
                    CSVFormat.DEFAULT
                            .withQuote(null)
                            .withDelimiter('|')
            );
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

                if (csvRecord.getRecordNumber() % batchSize == 0) {
                    preparedStatement.executeBatch();
                }
            }

            preparedStatement.executeBatch();
        } catch (SQLException e) {
            logger.error("failed retrieving connection to: " + datasourceUrl, e);
        } catch (ParseException e) {
            logger.error("Failed to parse date: " + dateString, e);
        } catch (IOException e) {
            logger.error("failed to open source file: " + file, e);
        } finally {
            try {
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                logger.error("failed to close DB connection", e);
            }
        }
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
