/**
 * Copyright 2013 AppDynamics
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.appdynamics.extensions.splunk;
/**
 *  This class encapsulates an AppDynamics event for Splunk.
 *
 * Copyright (c) AppDynamics, Inc.
 * @author Pranta Das
 * Created on: August 14, 2012.
 */

import com.splunk.logging.SplunkLogEvent;

public class SplunkAppDynamicsEvent extends SplunkLogEvent
{
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
}
