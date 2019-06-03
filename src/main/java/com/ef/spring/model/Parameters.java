package com.ef.spring.model;

import lombok.Getter;
import org.apache.log4j.Logger;
import org.springframework.boot.ApplicationArguments;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.ef.spring.model.Constants.*;

/**
 * <p>Class to store and validate user-issued params</p>
 */
@Getter
public class Parameters {
    private final static Logger log = Logger.getLogger(Parameters.class);
    private Long threshold;
    private Date startDate;
    private Date endDate;
    private String accessLog;
    private ApplicationArguments args;

    /**
     * <p>Implementation of parameters received via console.
     * Checks for validity/existence and keeps through like-cycle.</p>
     *
     * @param args User-issued arguments.
     */
    public Parameters(ApplicationArguments args)  {
        this.args = args;

        if (!args.containsOption(START_DATE_PARAM) ||
                !args.containsOption(DURATION_PARAM) ||
                !args.containsOption(THRESHOLD_PARAM)
                ) {
            throw new IllegalArgumentException(new StringBuilder(
                    "Illegal parameter count.\n")
                    .append("Required params are: ")
                    .append(REQUIRED_PARAMS.toString())
                    .toString()
            );
        }

        this.startDate = parseDate(args.getOptionValues(START_DATE_PARAM).get(0));

        this.endDate = calculateEndDate(args.getOptionValues(DURATION_PARAM).get(0));

        this.threshold = parseThreshold(args.getOptionValues(THRESHOLD_PARAM).get(0));

        if (args.containsOption(FILE_PARAM)) {
            this.accessLog = parseAccessLog(args.getOptionValues(FILE_PARAM).get(0));
        }
    }

    /**
     * <p>Parses received date via param {@link Constants.START_DATE_PARAM} to correct formatting.</p>
     *
     * @param date Argument-issued date.
     *
     * @return Properly formatted date.
     */
    private Date parseDate(String date) {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd.hh:mm:ss");
        Date formattedDate;
        try {
            formattedDate = formatter.parse(date);
        } catch (ParseException e) {
            String error = "Could not parse date: " + date;
            log.error(error, e);
            throw new RuntimeException(error);
        }
        return formattedDate;
    }

    /**
     * <p>Calculates end-date starting from start-date and adding duration.</p>
     *
     * @param duration Pre-defined time amount to add to startDate.
     *
     * @return Properly formatted end-date.
     */
    private Date calculateEndDate(String duration) {
        Date newDate;

        //This could have been done in many different ways [State pattern, enums, etc]. Chose this one for speed
        // and readability.
        switch (duration) {
            case DURATION_HOURLY:
                newDate = new Date(this.startDate.getTime() + TimeUnit.HOURS.toMillis( 1 ));
                break;
            case DURATION_DAILY:
                newDate = new Date(this.startDate.getTime() + TimeUnit.DAYS.toMillis( 1 ));
                break;
            default:
                throw new IllegalArgumentException(new StringBuilder(
                        "Illegal parameter \"")
                        .append(DURATION_PARAM)
                        .append("\" with value: ")
                        .append(duration).append("\n")
                        .append("Valid values are: ")
                        .append(DURATION_TYPES.toString())
                        .toString()
                );
        }
        return newDate;
    }

    /**
     * Checks for errors in converting user-issued param {@link Constants.THRESHOLD_PARAM}.
     *
     * @param threshold Threshold user-command-issued.
     *
     * @return Properly formatted threshold.
     */
    private Long parseThreshold(String threshold) {
        try {
            return new Long(threshold);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(new StringBuilder(
                    "Illegal parameter \"")
                    .append(THRESHOLD_PARAM)
                    .append("\" with value: ")
                    .append(threshold).append("\n")
                    .append("Should be an int/long")
                    .toString()
            );
        }
    }

    /**
     * Checks for errors in finding user-issued param {@link Constants.FILE_PARAM}.
     *
     * @param accessLog accessLog file user-command-issued.
     *
     * @return valid accessLog file path.
     */
    private String parseAccessLog(String accessLog) {
        Path path = Paths.get(accessLog);
        if (Files.exists(path) && Files.isRegularFile(path)) {
            return accessLog;
        }
        throw new IllegalArgumentException(new StringBuilder(
                "Illegal parameter \"")
                .append(FILE_PARAM)
                .append("\" with value: ")
                .append(accessLog).append("\n")
                .append("Should be a valid existent file")
                .toString()
        );
    }
}
