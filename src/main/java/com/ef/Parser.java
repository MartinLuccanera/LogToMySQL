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
public class Parser implements ApplicationRunner {

    private final LogDAO logDAO;
    private final LogUtils logUtils;

    public static void main(String... args) {
		SpringApplication.run(Parser.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws IOException{
        Parameters parameters = new Parameters(args);
        if (parameters.getAccessLog() != null && !logUtils.isDatabasePopulated(parameters.getAccessLog())) {
            logDAO.importLogIntoDB(parameters.getAccessLog());
        }
        logDAO.findAll(parameters);
    }

    @Autowired
    public Parser(LogDAO logDAO, LogUtils logUtils) {
        this.logDAO = logDAO;
        this.logUtils = logUtils;
    }
}
