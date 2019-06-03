package com.ef;

import com.ef.spring.model.Parameters;
import com.ef.spring.dao.LogDAO;
import com.ef.spring.utils.LogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class ParserApplication implements ApplicationRunner {

    private final LogDAO logDAO;
    private final LogUtils logUtils;

    //TODO: resume previous data load?
    //TODO: Tests?
    // Profit?
    
    public static void main(String... args) {
		SpringApplication.run(ParserApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws IOException{
        Parameters parameters = new Parameters(args);
        //TODO: add process continuation??? ->
        if (parameters.getAccessLog() != null && !logUtils.isDatabasePopulated(parameters.getAccessLog())) {
            logDAO.importLogIntoDB(parameters.getAccessLog());
        }
        logDAO.findAll(parameters);
    }

    @Autowired
    public ParserApplication(LogDAO logDAO, LogUtils logUtils) {
        this.logDAO = logDAO;
        this.logUtils = logUtils;
    }
}
