package com.ef.spring.utils;

import com.ef.spring.dao.LogDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class LogUtils {
    private final LogDAO logDAO;

    public boolean isDatabasePopulated(String file) throws IOException {
        LineNumberReader lnr = new LineNumberReader(new FileReader(new File(file)));
        lnr.skip(Long.MAX_VALUE);
        long csvTotalRecordsCount = lnr.getLineNumber();
        lnr.close();
        return csvTotalRecordsCount == logDAO.getRecordCountFromLogTable();
    }

    @Autowired
    public LogUtils(LogDAO logDAO) {
        this.logDAO = logDAO;
    }
}
