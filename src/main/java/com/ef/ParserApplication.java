package com.ef;

import com.ef.spring.model.Parameters;
import com.ef.spring.dao.ImportDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ParserApplication implements ApplicationRunner {

    private final ImportDAO importDAO;

    //TODO: Calculate length of csv. Check length of DB. Import or skip
    // -> https://stackoverflow.com/questions/30624727/what-is-the-fastest-way-to-get-dimensions-of-a-csv-file-in-java
    //TODO: resume previous data load?
    // TODO: Check how to instantiate Parameters and run queries
    // Profit?

    //TODO: Tests?

    public static void main(String... args) {
		SpringApplication.run(ParserApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) {
        Parameters parameters = new Parameters(args);
        //importDAO.importLogIntoDB("access.log");
        //importDAO.findAll();
    }

    @Autowired
    public ParserApplication(ImportDAO importDAO) {
        this.importDAO = importDAO;
    }
}
