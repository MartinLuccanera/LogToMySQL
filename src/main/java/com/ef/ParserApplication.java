package com.ef;

import com.ef.spring.dao.ImportDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ParserApplication implements CommandLineRunner {

    private final ImportDAO importDAO;

    public static void main(String[] args) {
		SpringApplication.run(ParserApplication.class, args);
	}

	@Override
	public void run(String... args) {
        importDAO.importLogIntoDB("access.log");
    }

    @Autowired
    public ParserApplication(ImportDAO importDAO) {
        this.importDAO = importDAO;
    }
}
