/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.splunk;

import org.apache.commons.lang.time.FastDateFormat;

import java.util.Date;

/**
 * @author Satish Muddam
 */
public class LogMessage {

    private static FastDateFormat DATEFORMATTER = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss:SSSZ");

    private StringBuilder message;

    public LogMessage() {
        message = new StringBuilder();
    }

    public LogMessage addPair(String name, boolean value) {
        return addPair(name, String.valueOf(value));
    }

    public LogMessage addPair(String name, String value) {
        if (value != null) {
            message.append(name).append("=").append('\"').append(value).append('\"').append(" ");
        }
        return this;
    }

    public String getMessage() {
        StringBuilder sb = new StringBuilder(DATEFORMATTER.format(new Date())).append(" ");
        sb.append(this.message);
        return sb.toString();
    }

    public LogMessage clone() {
        LogMessage clone = new LogMessage();
        clone.message.append(this.message);
        return clone;
    }

}
