@echo off
rem ------------------------------------------------
rem Batch file to send event notifications to Splunk
rem ------------------------------------------------
java -Dlog4j.configuration=file:conf/log4j.xml -jar splunk-alerting-extension.jar %*