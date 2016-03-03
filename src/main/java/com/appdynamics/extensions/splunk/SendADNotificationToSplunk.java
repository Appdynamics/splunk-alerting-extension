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
 * @author Satish Muddam
 */

import com.appdynamics.extensions.alerts.customevents.EvaluationEntity;
import com.appdynamics.extensions.alerts.customevents.Event;
import com.appdynamics.extensions.alerts.customevents.EventBuilder;
import com.appdynamics.extensions.alerts.customevents.EventSummary;
import com.appdynamics.extensions.alerts.customevents.EventType;
import com.appdynamics.extensions.alerts.customevents.HealthRuleViolationEvent;
import com.appdynamics.extensions.alerts.customevents.OtherEvent;
import com.appdynamics.extensions.alerts.customevents.TriggerCondition;
import com.appdynamics.extensions.yml.YmlReader;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SendADNotificationToSplunk {
    private static Logger logger =
            Logger.getLogger(SendADNotificationToSplunk.class);

    private static final String BANNER = "***************************" +
            "*****************************************************";


    private static String CONFIG_FILENAME = "." + File.separator + "conf" + File.separator + "config.yml";

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
    public static void main(String[] args) {

        logger.info(BANNER);
        String details = SendADNotificationToSplunk.class.getPackage().getImplementationTitle();
        String msg = "Using Extension Version [" + details + "]";
        logger.info(msg);

        if (args == null || args.length == 0) {
            logger.error("No arguments passed to the extension, exiting the program.");
            return;
        }

        try {

            Configuration config = YmlReader.readFromFile(CONFIG_FILENAME, Configuration.class);

            Event event = parseEventParams(args);
            List<LogMessage> logMessages = convertToSplunkEvent(event);

            HttpHandler httpHandler = new HttpHandler(config);

            httpHandler.sendEventsToSplunk(logMessages);

            logger.info(BANNER);
        } catch (Throwable e) {
            logger.error("Failed to send event to Splunk", e);
        }
    }

    private static List<LogMessage> convertToSplunkEvent(Event event) {
        List<LogMessage> logMessages = new ArrayList<LogMessage>();

        if (event instanceof HealthRuleViolationEvent) {
            LogMessage logMessage = new LogMessage();
            HealthRuleViolationEvent hrve = (HealthRuleViolationEvent) event;

            logMessage.addPair("name", hrve.getHealthRuleName()).addPair("event_id", hrve.getIncidentID());
            logMessages.add(logMessage);

            logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_APPLICATION_NAME, hrve.getAppName());
            logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_APPLICATION_ID, hrve.getAppID());
            logMessage.addPair(SplunkAppDynamicsEvent.COMMON_DVC_TIME, hrve.getPvnAlertTime());
            logMessage.addPair(SplunkAppDynamicsEvent.COMMON_PRIORITY, hrve.getPriority());
            logMessage.addPair(SplunkAppDynamicsEvent.COMMON_SEVERITY, hrve.getSeverity());
            logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_TAG, hrve.getTag());
            logMessage.addPair(SplunkAppDynamicsEvent.COMMON_NAME, hrve.getHealthRuleName());
            logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_RULE_ID, hrve.getHealthRuleID());
            logMessage.addPair(SplunkAppDynamicsEvent.COMMON_DURATION, hrve.getPvnTimePeriodInMinutes());
            logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_AFFECTED_ENTITY_TYPE, hrve.getAffectedEntityType());
            logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_AFFECTED_ENTITY_NAME, hrve.getAffectedEntityName());
            logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_AFFECTED_ENTITY_ID, hrve.getAffectedEntityID());

            logMessage.addPair(SplunkAppDynamicsEvent.COMMON_DESC, hrve.getSummaryMessage());
            logMessage.addPair(SplunkAppDynamicsEvent.COMMON_EVENT_ID, hrve.getIncidentID());
            logMessage.addPair(SplunkAppDynamicsEvent.COMMON_URL, hrve.getDeepLinkUrl() + hrve.getIncidentID());
            logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_POLICY_EVENT_TYPE, hrve.getEventType());
            //Peoperties not yet added in the commons lib
            /*logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_ACCOUNT_NAME, hrve.getAccountName());
            logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_ACCOUNT_ID, hrve.getAccountId());*/


            List<EvaluationEntity> evaluationEntities = hrve.getEvaluationEntity();
            if (evaluationEntities != null) {
                int evaluationEntityIndex = 0;
                LogMessage clonedLogMessage = null;
                if (evaluationEntities.size() > 1) {
                    clonedLogMessage = logMessage.clone();
                }
                for (EvaluationEntity evaluationEntity : evaluationEntities) {

                    List<TriggerCondition> triggeredConditions = evaluationEntity.getTriggeredConditions();
                    if (evaluationEntityIndex >= 1) {
                        LogMessage logMessageTmp = null;
                        if (evaluationEntities.size() == 2) {
                            logMessageTmp = clonedLogMessage;
                        } else {
                            logMessageTmp = clonedLogMessage.clone();
                        }
                        logMessages.add(logMessageTmp);

                        logMessageTmp.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_EVALUATION_ENTITY_TYPE, evaluationEntity.getType());
                        logMessageTmp.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_EVALUATION_ENTITY_NAME, evaluationEntity.getName());
                        logMessageTmp.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_EVALUATION_ENTITY_ID, evaluationEntity.getId());

                        addTriggeredConditions(logMessageTmp, triggeredConditions, logMessages);
                    } else {
                        logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_EVALUATION_ENTITY_TYPE, evaluationEntity.getType());
                        logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_EVALUATION_ENTITY_NAME, evaluationEntity.getName());
                        logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_EVALUATION_ENTITY_ID, evaluationEntity.getId());

                        addTriggeredConditions(logMessage, triggeredConditions, logMessages);
                    }
                    evaluationEntityIndex++;
                }
            }
        } else {
            OtherEvent oe = (OtherEvent) event;
            LogMessage logMessage = new LogMessage();
            logMessage.addPair("name", oe.getEventNotificationName()).addPair("event_id", oe.getEventNotificationId());


            logMessages.add(logMessage);

            logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_APPLICATION_NAME, oe.getAppName());
            logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_APPLICATION_ID, oe.getAppID());
            logMessage.addPair(SplunkAppDynamicsEvent.COMMON_DVC_TIME, oe.getEventNotificationTime());
            logMessage.addPair(SplunkAppDynamicsEvent.COMMON_PRIORITY, oe.getPriority());
            logMessage.addPair(SplunkAppDynamicsEvent.COMMON_SEVERITY, oe.getSeverity());
            logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_TAG, oe.getTag());
            logMessage.addPair(SplunkAppDynamicsEvent.COMMON_NAME, oe.getEventNotificationName());
            logMessage.addPair(SplunkAppDynamicsEvent.COMMON_EVENT_ID, oe.getEventNotificationId());
            logMessage.addPair(SplunkAppDynamicsEvent.COMMON_DURATION, oe.getEventNotificationIntervalInMin());
            logMessage.addPair(SplunkAppDynamicsEvent.COMMON_URL, oe.getDeepLinkUrl() + oe.getEventNotificationId());
            //Peoperties not yet added in the commons lib
            /*logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_ACCOUNT_NAME, oe.getAccountName());
            logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_ACCOUNT_ID, oe.getAccountId());*/

            addEventTypesAndEventTypeNum(logMessage, oe.getEventTypes());

            List<EventSummary> eventSummaries = oe.getEventSummaries();

            if (eventSummaries != null) {

                int eventSummariesIndex = 0;
                LogMessage clonedLogMessage = null;
                if (eventSummaries.size() > 1) {
                    clonedLogMessage = logMessage.clone();
                }
                for (EventSummary eventSummary : eventSummaries) {

                    if (eventSummariesIndex >= 1) {
                        LogMessage logMessageTmp = null;
                        if (eventSummaries.size() == 2) {
                            logMessageTmp = clonedLogMessage;
                        } else {
                            logMessageTmp = clonedLogMessage.clone();
                        }
                        logMessages.add(logMessageTmp);
                    } else {
                        logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_EVENT_SUMMARY_ID, eventSummary.getEventSummaryId());
                        logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_EVENT_SUMMARY_TIME, eventSummary.getEventSummaryTime());
                        logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_EVENT_SUMMARY_TYPE, eventSummary.getEventSummaryType());
                        logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_EVENT_SUMMARY_SEVERITY, eventSummary.getEventSummarySeverity());
                        logMessage.addPair(SplunkAppDynamicsEvent.COMMON_DESC, eventSummary.getEventSummaryString());
                    }
                    eventSummariesIndex++;
                }

            }
        }

        return logMessages;
    }

    private static void addEventTypesAndEventTypeNum(LogMessage logMessage, List<EventType> eventTypes) {
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
            logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_EVENT_TYPES, eventTypesString.toString());
            logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_NUMBER_OF_EVENTS_FOR_TYPES, eventTypesNumString.toString());
        }
    }

    private static void addTriggeredConditions(LogMessage logMessage, List<TriggerCondition> triggeredConditions, List<LogMessage> logMessages) {
        int triggeredConditionsIndex = 0;
        if (triggeredConditions != null) {
            LogMessage clonedLogMessage = null;
            if (triggeredConditions.size() > 1) {
                clonedLogMessage = logMessage.clone();
            }
            for (TriggerCondition triggerCondition : triggeredConditions) {

                if (triggeredConditionsIndex >= 1) {

                    LogMessage logMessageTmp = null;
                    if (triggeredConditions.size() == 2) {
                        logMessageTmp = clonedLogMessage;
                    } else {
                        logMessageTmp = clonedLogMessage.clone();
                    }

                    logMessages.add(logMessageTmp);

                    addTriggeredConditionsToLogEvent(logMessageTmp, triggerCondition);

                } else {
                    addTriggeredConditionsToLogEvent(logMessage, triggerCondition);
                }
                triggeredConditionsIndex++;
            }
        }
    }

    private static void addTriggeredConditionsToLogEvent(LogMessage logMessage, TriggerCondition triggerCondition) {
        logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_SCOPE_TYPE, triggerCondition.getScopeType());
        logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_SCOPE_NAME, triggerCondition.getScopeName());
        logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_SCOPE_ID, triggerCondition.getScopeId());
        logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_CONDITION_NAME, triggerCondition.getConditionName());
        logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_CONDITION_ID, triggerCondition.getConditionId());
        logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_OPERATOR, triggerCondition.getOperator());
        logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_CONDITION_UNIT_TYPE, triggerCondition.getConditionUnitType());
        logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_USE_DEFAULT_BASELINE, triggerCondition.isUseDefaultBaseline());
        logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_BASELINE_NAME, triggerCondition.getBaselineName());
        logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_BASELINE_ID, triggerCondition.getBaselineId());
        logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_THRESHOLD_VALUE, triggerCondition.getThresholdValue());
        logMessage.addPair(SplunkAppDynamicsEvent.APPDYNAMICS_OBSERVED_VALUE, triggerCondition.getObservedValue());
    }
}