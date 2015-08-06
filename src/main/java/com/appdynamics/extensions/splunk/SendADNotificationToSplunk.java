/**
 * Copyright 2013 AppDynamics
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.appdynamics.extensions.splunk;
/**
 * This class will be used to relay AppDynamics notifications to the
 * Splunk Server via REST.
 * <p/>
 * Copyright (c) AppDynamics, Inc.
 *
 * @author Pranta Das
 * Created on: August 14, 2012.
 *
 * @author Satish Reddy M
 *
 */

import com.appdynamics.extensions.alerts.customevents.EvaluationEntity;
import com.appdynamics.extensions.alerts.customevents.Event;
import com.appdynamics.extensions.alerts.customevents.EventBuilder;
import com.appdynamics.extensions.alerts.customevents.EventSummary;
import com.appdynamics.extensions.alerts.customevents.EventType;
import com.appdynamics.extensions.alerts.customevents.HealthRuleViolationEvent;
import com.appdynamics.extensions.alerts.customevents.OtherEvent;
import com.appdynamics.extensions.alerts.customevents.TriggerCondition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.splunk.logging.RestEventData;
import com.splunk.logging.SplunkLogEvent;
import com.splunk.logging.SplunkRestInput;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SendADNotificationToSplunk  {
    private static Logger logger =
            Logger.getLogger(SendADNotificationToSplunk.class);

    /**
     * Default property values
     */
    private static final String DEFAULT_HOSTNAME
            = "localhost";
    private static final int DEFAULT_PORT = 8089;

    private static final String userHome = System.getProperty("user.home"),
            fileName = userHome + "/.splunkrc";
    private static String userName = "admin", password = "ad@Splunk0";
    private static final String BANNER = "***************************" +
            "*****************************************************";
    /**
     * Connection properties
     */
    private static final String SPLUNK_HOST = "host";
    private static final String SPLUNK_PORT = "port";
    private static final String SPLUNK_USERNAME = "username";
    private static final String SPLUNK_PASSWORD = "password";

    private static final String SPLUNK_EVENT_HOST = "eventHost";
    private static final String SPLUNK_INDEX = "index";
    private static final String SPLUNK_SOURCETYPE = "sourceType";



    static String hostname;
    static int port;
    static Properties theProperties;

    static RestEventData restEventData = new RestEventData();
    static SplunkRestInput splunkRestInput;

    /**
     * Set up connection to the web server using the global configuration
     * information from the input properties file
     *
     * @return true if successful, false otherwise
     */
    private static boolean setupConnection() {
        /**
         * Read the Splunk client properties file for the connection properties
         */
        theProperties = new Properties();
        hostname = DEFAULT_HOSTNAME;
        port = DEFAULT_PORT;

        System.getProperties();

        logger.info(" Reading .splunkrc file:" +
                fileName);

        FileInputStream propFile = null;
        try {
            propFile = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            logger.error(e);
        }

        if (propFile != null) {
            try {
                theProperties.load(propFile);
            } catch (IOException e) {
                logger.error(e);
            }

            String strVal;

            if ((strVal = theProperties.getProperty(SPLUNK_HOST)) != null)
                hostname = strVal.trim();

            if ((strVal = theProperties.getProperty(SPLUNK_PORT)) != null)
                port = Integer.parseInt(strVal.trim());

            if ((strVal = theProperties.getProperty(SPLUNK_USERNAME)) != null)
                userName = strVal.trim();

            if ((strVal = theProperties.getProperty(SPLUNK_PASSWORD)) != null)
                password = strVal.trim();
        }

        try {
            if (splunkRestInput == null) {
                String eventHost = theProperties.getProperty(SPLUNK_EVENT_HOST);

                if(eventHost != null && eventHost.length() > 0) {
                    restEventData.setHost(eventHost);
                }

                String indexName = theProperties.getProperty(SPLUNK_INDEX);
                if(indexName != null && indexName.length() > 0) {
                    restEventData.setIndex(indexName);
                }

                String sourceType = theProperties.getProperty(SPLUNK_SOURCETYPE);
                if(sourceType != null && sourceType.length() > 0) {
                    restEventData.setSourcetype(sourceType);
                }

                splunkRestInput = new SplunkRestInput(userName, password, hostname, port, restEventData, false);
                splunkRestInput.setMaxQueueSize("30MB");
                splunkRestInput.setDropEventsOnQueueFull(false);
            }
        } catch (Exception e) {
            logger.error("Unable to connect to Splunk", e);
            return false;
        }
        return true;
    }

    /**
     * Tear down connection to the Splunk server
     */
    private static void teardownConnection() {
        logger.info(" Calling disconnect");

        if (splunkRestInput != null) {
            splunkRestInput.closeStream();
            splunkRestInput = null;
        }

        logger.info(" Disconnected from Splunk.");
    }

    /**
     * Send an event to Splunk via REST
     *
     * @param event The event being passed to Splunk
     */
    private static void sendEvent(SplunkLogEvent event) {
        // send event to the cell
        logger.info(" Sending event to Splunk.");
        splunkRestInput.sendEvent(event.toString());
    }

    private static Event parseEventParams(String[] args) {
        EventBuilder eventBuilder = new EventBuilder();
        Event event = eventBuilder.build(args);
        return event;
    }

    /**
     * Main method called from AppDynamics custom action shell script
     * or batch file.
     *
     * @param args - arguments passed
     */
    public static void main(String[] args) throws JsonProcessingException {

        logger.info(BANNER);
        String details = SendADNotificationToSplunk.class.getPackage().getImplementationTitle();
        String msg = "Using Extension Version [" + details + "]";
        logger.info(msg);

        if (args == null || args.length == 0) {
            logger.error("No arguments passed to the extension, exiting the program.");
            return;
        }

        try {
            boolean connectionSetup = setupConnection();

            if (!connectionSetup) {
                logger.error("Unable to setup connection");
                System.exit(-1);
            }

            Event event = parseEventParams(args);
            List<SplunkLogEvent> splunkLogEvents = convertToSplunkEvent(event);

            for(SplunkLogEvent splunkLogEvent : splunkLogEvents) {
                sendEvent(splunkLogEvent);
            }

            teardownConnection();

            logger.info(BANNER);
        } catch (Throwable e) {
            logger.error("Failed to send event to Splunk", e);
        }
    }

    private static List<SplunkLogEvent> convertToSplunkEvent(Event event) {
        List<SplunkLogEvent> splunkLogEvents = new ArrayList<SplunkLogEvent>();
        if (event instanceof HealthRuleViolationEvent) {
            HealthRuleViolationEvent hrve = (HealthRuleViolationEvent) event;
            SplunkLogEvent splunkLogEvent = new SplunkLogEvent(hrve.getHealthRuleName(), hrve.getIncidentID());

            splunkLogEvents.add(splunkLogEvent);

            addPair(splunkLogEvent, SplunkAppDynamicsEvent.APPDYNAMICS_APPLICATION_NAME, hrve.getAppName());
            addPair(splunkLogEvent, SplunkAppDynamicsEvent.APPDYNAMICS_APPLICATION_ID, hrve.getAppID());
            addPair(splunkLogEvent, SplunkAppDynamicsEvent.COMMON_DVC_TIME, hrve.getPvnAlertTime());
            addPair(splunkLogEvent, SplunkAppDynamicsEvent.COMMON_PRIORITY, hrve.getPriority());
            addPair(splunkLogEvent, SplunkAppDynamicsEvent.COMMON_SEVERITY, hrve.getSeverity());
            addPair(splunkLogEvent, SplunkAppDynamicsEvent.APPDYNAMICS_TAG, hrve.getTag());
            addPair(splunkLogEvent, SplunkAppDynamicsEvent.COMMON_NAME, hrve.getHealthRuleName());
            addPair(splunkLogEvent, SplunkAppDynamicsEvent.APPDYNAMICS_RULE_ID, hrve.getHealthRuleID());
            addPair(splunkLogEvent, SplunkAppDynamicsEvent.COMMON_DURATION, hrve.getPvnTimePeriodInMinutes());
            addPair(splunkLogEvent, SplunkAppDynamicsEvent.APPDYNAMICS_AFFECTED_ENTITY_TYPE, hrve.getAffectedEntityType());
            addPair(splunkLogEvent, SplunkAppDynamicsEvent.APPDYNAMICS_AFFECTED_ENTITY_NAME, hrve.getAffectedEntityName());
            addPair(splunkLogEvent, SplunkAppDynamicsEvent.APPDYNAMICS_AFFECTED_ENTITY_ID, hrve.getAffectedEntityID());

            addPair(splunkLogEvent, SplunkAppDynamicsEvent.COMMON_DESC, hrve.getSummaryMessage());
            addPair(splunkLogEvent, SplunkAppDynamicsEvent.COMMON_EVENT_ID, hrve.getIncidentID());
            addPair(splunkLogEvent, SplunkAppDynamicsEvent.COMMON_URL, hrve.getDeepLinkUrl() + hrve.getIncidentID());
            addPair(splunkLogEvent, SplunkAppDynamicsEvent.APPDYNAMICS_POLICY_EVENT_TYPE, hrve.getEventType());
            //Peoperties not yet added in the commons lib
            /*splunkLogEvent.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_ACCOUNT_NAME, hrve.getAccountName());
            splunkLogEvent.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_ACCOUNT_ID, hrve.getAccountId());*/


            List<EvaluationEntity> evaluationEntities = hrve.getEvaluationEntity();
            if (evaluationEntities != null) {
                int evaluationEntityIndex = 0;
                SplunkLogEvent clonedLogEvent = null;
                if (evaluationEntities.size() > 1) {
                    clonedLogEvent = splunkLogEvent.clone();
                }
                for (EvaluationEntity evaluationEntity : evaluationEntities) {

                    List<TriggerCondition> triggeredConditions = evaluationEntity.getTriggeredConditions();
                    if (evaluationEntityIndex >= 1) {
                        SplunkLogEvent splunkLogEventTmp = null;
                        if (evaluationEntities.size() == 2) {
                            splunkLogEventTmp = clonedLogEvent;
                        } else {
                            splunkLogEventTmp = clonedLogEvent.clone();
                        }
                        splunkLogEvents.add(splunkLogEventTmp);

                        addPair(splunkLogEventTmp, SplunkAppDynamicsEvent.APPDYNAMICS_EVALUATION_ENTITY_TYPE, evaluationEntity.getType());
                        addPair(splunkLogEventTmp, SplunkAppDynamicsEvent.APPDYNAMICS_EVALUATION_ENTITY_NAME, evaluationEntity.getName());
                        addPair(splunkLogEventTmp, SplunkAppDynamicsEvent.APPDYNAMICS_EVALUATION_ENTITY_ID, evaluationEntity.getId());

                        addTriggeredConditions(splunkLogEventTmp, triggeredConditions, splunkLogEvents);
                    } else {
                        addPair(splunkLogEvent, SplunkAppDynamicsEvent.APPDYNAMICS_EVALUATION_ENTITY_TYPE, evaluationEntity.getType());
                        addPair(splunkLogEvent, SplunkAppDynamicsEvent.APPDYNAMICS_EVALUATION_ENTITY_NAME, evaluationEntity.getName());
                        addPair(splunkLogEvent, SplunkAppDynamicsEvent.APPDYNAMICS_EVALUATION_ENTITY_ID, evaluationEntity.getId());

                        addTriggeredConditions(splunkLogEvent, triggeredConditions, splunkLogEvents);
                    }
                    evaluationEntityIndex++;
                }
            }
        } else {
            OtherEvent oe = (OtherEvent) event;
            SplunkLogEvent splunkLogEvent = new SplunkLogEvent(oe.getEventNotificationName(), oe.getEventNotificationId());

            splunkLogEvents.add(splunkLogEvent);

            addPair(splunkLogEvent, SplunkAppDynamicsEvent.APPDYNAMICS_APPLICATION_NAME, oe.getAppName());
            addPair(splunkLogEvent, SplunkAppDynamicsEvent.APPDYNAMICS_APPLICATION_ID, oe.getAppID());
            addPair(splunkLogEvent, SplunkAppDynamicsEvent.COMMON_DVC_TIME, oe.getEventNotificationTime());
            addPair(splunkLogEvent, SplunkAppDynamicsEvent.COMMON_PRIORITY, oe.getPriority());
            addPair(splunkLogEvent, SplunkAppDynamicsEvent.COMMON_SEVERITY, oe.getSeverity());
            addPair(splunkLogEvent, SplunkAppDynamicsEvent.APPDYNAMICS_TAG, oe.getTag());
            addPair(splunkLogEvent, SplunkAppDynamicsEvent.COMMON_NAME, oe.getEventNotificationName());
            addPair(splunkLogEvent, SplunkAppDynamicsEvent.COMMON_EVENT_ID, oe.getEventNotificationId());
            addPair(splunkLogEvent, SplunkAppDynamicsEvent.COMMON_DURATION, oe.getEventNotificationIntervalInMin());
            addPair(splunkLogEvent, SplunkAppDynamicsEvent.COMMON_URL, oe.getDeepLinkUrl() + oe.getEventNotificationId());
            //Peoperties not yet added in the commons lib
            /*splunkLogEvent.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_ACCOUNT_NAME, oe.getAccountName());
            splunkLogEvent.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_ACCOUNT_ID, oe.getAccountId());*/

            addEventTypesAndEventTypeNum(splunkLogEvent, oe.getEventTypes());

            List<EventSummary> eventSummaries = oe.getEventSummaries();

            if (eventSummaries != null) {

                int eventSummariesIndex = 0;
                SplunkLogEvent clonedLogEvent = null;
                if (eventSummaries.size() > 1) {
                    clonedLogEvent = splunkLogEvent.clone();
                }
                for (EventSummary eventSummary : eventSummaries) {

                    if (eventSummariesIndex >= 1) {
                        SplunkLogEvent splunkLogEventTmp = null;
                        if (eventSummaries.size() == 2) {
                            splunkLogEventTmp = clonedLogEvent;
                        } else {
                            splunkLogEventTmp = clonedLogEvent.clone();
                        }
                        splunkLogEvents.add(splunkLogEventTmp);
                    } else {
                        addPair(splunkLogEvent, SplunkAppDynamicsEvent.APPDYNAMICS_EVENT_SUMMARY_ID, eventSummary.getEventSummaryId());
                        addPair(splunkLogEvent, SplunkAppDynamicsEvent.APPDYNAMICS_EVENT_SUMMARY_TIME, eventSummary.getEventSummaryTime());
                        addPair(splunkLogEvent, SplunkAppDynamicsEvent.APPDYNAMICS_EVENT_SUMMARY_TYPE, eventSummary.getEventSummaryType());
                        addPair(splunkLogEvent, SplunkAppDynamicsEvent.APPDYNAMICS_EVENT_SUMMARY_SEVERITY, eventSummary.getEventSummarySeverity());
                        addPair(splunkLogEvent, SplunkAppDynamicsEvent.COMMON_DESC, eventSummary.getEventSummaryString());
                    }
                    eventSummariesIndex++;
                }

            }
        }

        return splunkLogEvents;
    }

    private static void addEventTypesAndEventTypeNum(SplunkLogEvent splunkLogEvent, List<EventType> eventTypes) {
        if (eventTypes != null) {
            StringBuilder eventTypesString = new StringBuilder("{");
            StringBuilder eventTypesNumString = new StringBuilder("{");
            boolean first = true;
            for (EventType eventType : eventTypes) {
                if (!first) {
                    eventTypesString.append(",");
                    eventTypesNumString.append(",");
                }
                eventTypesString.append(eventType.getEventType());
                eventTypesNumString.append(eventType.getEventTypeNum());
                first = false;
            }
            eventTypesString.append("}");
            eventTypesNumString.append("}");
            addPair(splunkLogEvent, SplunkAppDynamicsEvent.APPDYNAMICS_EVENT_TYPES, eventTypesString.toString());
            addPair(splunkLogEvent, SplunkAppDynamicsEvent.APPDYNAMICS_NUMBER_OF_EVENTS_FOR_TYPES, eventTypesNumString.toString());
        }
    }

    private static void addTriggeredConditions(SplunkLogEvent splunkLogEvent, List<TriggerCondition> triggeredConditions, List<SplunkLogEvent> splunkLogEvents) {
        int triggeredConditionsIndex = 0;
        if (triggeredConditions != null) {
            SplunkLogEvent clonedLogEvent = null;
            if (triggeredConditions.size() > 1) {
                clonedLogEvent = splunkLogEvent.clone();
            }
            for (TriggerCondition triggerCondition : triggeredConditions) {

                if (triggeredConditionsIndex >= 1) {

                    SplunkLogEvent splunkLogEventTmp = null;
                    if (triggeredConditions.size() == 2) {
                        splunkLogEventTmp = clonedLogEvent;
                    } else {
                        splunkLogEventTmp = clonedLogEvent.clone();
                    }

                    splunkLogEvents.add(splunkLogEventTmp);

                    addTriggeredConditionsToLogEvent(splunkLogEventTmp, triggerCondition);

                } else {
                    addTriggeredConditionsToLogEvent(splunkLogEvent, triggerCondition);
                }
                triggeredConditionsIndex++;
            }
        }
    }

    private static void addTriggeredConditionsToLogEvent(SplunkLogEvent splunkLogEvent, TriggerCondition triggerCondition) {
        addPair(splunkLogEvent, SplunkAppDynamicsEvent.APPDYNAMICS_SCOPE_TYPE, triggerCondition.getScopeType());
        addPair(splunkLogEvent, SplunkAppDynamicsEvent.APPDYNAMICS_SCOPE_NAME, triggerCondition.getScopeName());
        addPair(splunkLogEvent, SplunkAppDynamicsEvent.APPDYNAMICS_SCOPE_ID, triggerCondition.getScopeId());
        addPair(splunkLogEvent, SplunkAppDynamicsEvent.APPDYNAMICS_CONDITION_NAME, triggerCondition.getConditionName());
        addPair(splunkLogEvent, SplunkAppDynamicsEvent.APPDYNAMICS_CONDITION_ID, triggerCondition.getConditionId());
        addPair(splunkLogEvent, SplunkAppDynamicsEvent.APPDYNAMICS_OPERATOR, triggerCondition.getOperator());
        addPair(splunkLogEvent, SplunkAppDynamicsEvent.APPDYNAMICS_CONDITION_UNIT_TYPE, triggerCondition.getConditionUnitType());
        addPair(splunkLogEvent, SplunkAppDynamicsEvent.APPDYNAMICS_USE_DEFAULT_BASELINE, triggerCondition.isUseDefaultBaseline());
        addPair(splunkLogEvent, SplunkAppDynamicsEvent.APPDYNAMICS_BASELINE_NAME, triggerCondition.getBaselineName());
        addPair(splunkLogEvent, SplunkAppDynamicsEvent.APPDYNAMICS_BASELINE_ID, triggerCondition.getBaselineId());
        addPair(splunkLogEvent, SplunkAppDynamicsEvent.APPDYNAMICS_THRESHOLD_VALUE, triggerCondition.getThresholdValue());
        addPair(splunkLogEvent, SplunkAppDynamicsEvent.APPDYNAMICS_OBSERVED_VALUE, triggerCondition.getObservedValue());
    }

    private static void addPair(SplunkLogEvent splunkLogEvent, String key, String value) {
        if (value != null) {
            splunkLogEvent.addPair(key, value);
        }
    }

    private static void addPair(SplunkLogEvent splunkLogEvent, String key, boolean value) {
        splunkLogEvent.addPair(key, value);
    }
}