package com.ef.spring.model;

import com.google.common.collect.ImmutableList;

public class Constants {
    public static final String START_DATE_PARAM = "startDate";
    public static final String DURATION_PARAM = "duration";
    public static final String THRESHOLD_PARAM = "threshold";
    public static final String FILE_PARAM = "accesslog";
    public static final String DURATION_DAILY = "daily";
    public static final String DURATION_HOURLY = "hourly";

    public static final ImmutableList<String> DURATION_TYPES =
            ImmutableList.of(DURATION_DAILY, DURATION_HOURLY);

    public static final ImmutableList<String> REQUIRED_PARAMS =
            ImmutableList.of(START_DATE_PARAM, DURATION_PARAM, THRESHOLD_PARAM);
    public static String DATE_FORMAT_INPUT = "yyyy-MM-dd.HH:mm:ss";
    public static String DATE_FORMAT_OUTPUT = "yyyy-MM-dd HH:mm:ss";
}
