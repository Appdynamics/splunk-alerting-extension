/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */
package com.appdynamics.extensions.splunk;
public class SplunkAppDynamicsEvent {
    // ----------------------------------
    // AppDynamics fields
    // ----------------------------------
    public static final String APPDYNAMICS_APPLICATION_NAME = "ad_application_name";
    public static final String APPDYNAMICS_APPLICATION_ID = "ad_application_id";
    public static final String APPDYNAMICS_TAG = "ad_tag";
    public static final String APPDYNAMICS_RULE_NAME = "ad_rule_name";
    public static final String APPDYNAMICS_RULE_ID = "ad_rule_id";
    public static final String APPDYNAMICS_AFFECTED_ENTITY_TYPE = "ad_affected_entity_type";
    public static final String APPDYNAMICS_AFFECTED_ENTITY_NAME = "ad_affected_entity_name";
    public static final String APPDYNAMICS_AFFECTED_ENTITY_ID = "ad_affected_entity_id";
    public static final String APPDYNAMICS_EVALUATION_ENTITY_TYPE = "ad_evaluation_entity_type";
    public static final String APPDYNAMICS_EVALUATION_ENTITY_NAME = "ad_evaluation_entity_name";
    public static final String APPDYNAMICS_EVALUATION_ENTITY_ID = "ad_evaluation_entity_id";
    public static final String APPDYNAMICS_SCOPE_TYPE = "ad_scope_type";
    public static final String APPDYNAMICS_SCOPE_NAME = "ad_scope_name";
    public static final String APPDYNAMICS_SCOPE_ID = "ad_scope_id";
    public static final String APPDYNAMICS_CONDITION_NAME = "ad_condition_name";
    public static final String APPDYNAMICS_CONDITION_ID = "ad_condition_id";
    public static final String APPDYNAMICS_OPERATOR = "ad_operator";
    public static final String APPDYNAMICS_CONDITION_UNIT_TYPE = "ad_condition_unit_type";
    public static final String APPDYNAMICS_USE_DEFAULT_BASELINE = "ad_use_default_baseline";
    public static final String APPDYNAMICS_BASELINE_NAME = "ad_baseline_name";
    public static final String APPDYNAMICS_BASELINE_ID = "ad_baseline_id";
    public static final String APPDYNAMICS_THRESHOLD_VALUE = "ad_threshold_value";
    public static final String APPDYNAMICS_OBSERVED_VALUE = "ad_observed_value";

    public static final String APPDYNAMICS_EVENT_TYPES = "ad_event_types";
    public static final String APPDYNAMICS_NUMBER_OF_EVENTS_FOR_TYPES = "ad_num_events_for_types";
    public static final String APPDYNAMICS_EVENT_SUMMARY_ID = "ad_summary_id";
    public static final String APPDYNAMICS_EVENT_SUMMARY_TIME = "ad_summary_time";
    public static final String APPDYNAMICS_EVENT_SUMMARY_TYPE = "ad_summary_type";
    public static final String APPDYNAMICS_EVENT_SUMMARY_SEVERITY = "ad_summary_severity";
    public static final String APPDYNAMICS_POLICY_EVENT_TYPE = "ad_policy_event_type";

    public static final String APPDYNAMICS_ACCOUNT_NAME = "ad_account_name";
    public static final String APPDYNAMICS_ACCOUNT_ID = "ad_account_id";

    //Taken from com.splunk.logging.SplunkLogEvent

    public static String COMMON_DVC_TIME = "dvc_time";
    public static String COMMON_PRIORITY = "priority";
    public static String COMMON_SEVERITY = "severity";
    public static String COMMON_NAME = "name";
    public static String COMMON_DURATION = "duration";
    public static String COMMON_DESC = "desc";
    public static String COMMON_EVENT_ID = "event_id";
    public static String COMMON_URL = "url";

}
