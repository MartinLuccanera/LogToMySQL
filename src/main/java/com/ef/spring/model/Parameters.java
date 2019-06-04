package com.ef.spring.model;

import lombok.Getter;
import org.apache.log4j.Logger;
import org.springframework.boot.ApplicationArguments;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static com.ef.spring.model.Constants.*;

/**
 * <p>Class to store and validate user-issued params</p>
 */
@Getter
public class Parameters {
    private final static Logger log = Logger.getLogger(Parameters.class);
    private Long threshold;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String accessLog;
    private ApplicationArguments args;

    /**
     * <p>Implementation of parameters received via console.
     * Checks for validity/existence and stores values through process' life-cycle.</p>
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

        this.startDate = parseStringToDate(args.getOptionValues(START_DATE_PARAM).get(0));

        this.endDate = calculateEndDate(args.getOptionValues(DURATION_PARAM).get(0), startDate);

        this.threshold = parseThreshold(args.getOptionValues(THRESHOLD_PARAM).get(0));

        if (args.containsOption(FILE_PARAM)) {
            this.accessLog = parseAccessLog(args.getOptionValues(FILE_PARAM).get(0));
        }
    }

    /**
     * <p>Parses received String date to correct {@link LocalDateTime} formatting.
     * Uses date format: {@value Constants#DATE_FORMAT_INPUT}</p>
     *
     * @param date Argument-issued date.
     *
     * @return Properly formatted date.
     */
    private LocalDateTime parseStringToDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT_INPUT);
        LocalDateTime formattedDate;
        try {
            formattedDate = LocalDateTime.parse(date, formatter);
        } catch (DateTimeParseException e) {
            String error = "Could not parse date: " + date;
            log.error(error, e);
            throw new RuntimeException(error);
        }
        return formattedDate;
    }

    /**
     * <p>Returns startDate as String.
     * Uses {@value Constants#DATE_FORMAT_OUTPUT}<./p>
     *
     * @return Properly formatted date-string.
     */
    public String getStartDateAsString() {
        return convertLocalDateTimeToString(startDate);
    }

    /**
     * <p>Returns startDate as String.
     * Uses {@value Constants#DATE_FORMAT_OUTPUT}<./p>
     *
     * @return Properly formatted date-string.
     */
    public String getEndDateAsString() {
        return convertLocalDateTimeToString(endDate);
    }

    private String convertLocalDateTimeToString(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT_OUTPUT);
        try {
            return date.format(formatter);
        } catch (DateTimeParseException e) {
            String error = "Could not parse date: " + date;
            log.error(error, e);
            throw new RuntimeException(error);
        }
    }

    /**
     * <p>Calculates end-date starting from start-date and adding duration.
     * Duration is a user-issued param {@value Constants#DURATION_PARAM}</p>
     *
     * @param duration Pre-defined time amount to add to startDate.
     *
     * @return Properly formatted end-date.
     */
    private LocalDateTime calculateEndDate(String duration, LocalDateTime startDate) {

        //This could have been done in many different ways [State pattern, enums, etc]. Chose this one for speed
        // and readability.
        switch (duration) {
            case DURATION_HOURLY:
                return startDate.plusHours(1L);
            case DURATION_DAILY:
                return startDate.plusDays(1L);
            default:
                throw new IllegalArgumentException(new StringBuilder
                        ("Illegal parameter \"")
                        .append(DURATION_PARAM)
                        .append("\" with value: ")
                        .append(duration).append("\n")
                        .append("Valid values are: ")
                        .append(DURATION_TYPES.toString())
                        .toString()
                );
        }
    }

    /**
     * Checks for errors in converting user-issued param {@value Constants#THRESHOLD_PARAM}.
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
     * Checks for errors in finding user-issued param {@value Constants#FILE_PARAM}.
     *
     * @param accessLog accessLog file user-command-issued.
     *
     * @return valid accessLog file path.
     */
    private String parseAccessLog(String accessLog) {
        try {
            new FileReader(accessLog);
            return accessLog;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(new StringBuilder(
                    "Illegal parameter \"")
                    .append(FILE_PARAM)
                    .append("\" with value: ")
                    .append(accessLog).append("\n")
                    .append("Should be a valid existent file")
                    .toString(), e);
        }
    }
}
