# AppDynamics Splunk Alerting Extension

This extension works only with a dedicated SaaS controller or an on-prem controller.

##Use Case

Splunk [www.splunk.com](http://www.splunk.com) indexes and makes searchable data from any app, server or network device in real time including logs, config files, messages, alerts, scripts and metrics.

### Prerequisites

 1. You need to have edit_tcp permission

 2. Splunk sdk and logging libraries are not in maven repo. To get it using maven we have to install the library in the local maven repo. Splunk sdk 1.4.0 and Splunk logging 1.2 are checked in to the lib folder. Use the below maven commands to install the libraries to local maven repo.

mvn install:install-file -Dfile=lib/splunklogging.jar -DgroupId=splunkjavalogging -DartifactId=splunkjavalogging -Dversion=1.2 -Dpackaging=jar

mvn install:install-file -Dfile=lib/splunk-sdk-java-1.4.0.jar -DgroupId=com.splunk -DartifactId=splunk -Dversion=1.4.0 -Dpackaging=jar


##Installation Steps

 1. Run "mvn clean install"

 2. Find the zip file at 'target/splunk-alerting-extension.zip'

 3. Unzip the splunk-alerting-extension.zip file into <CONTROLLER_HOME_DIR>/custom/actions/ . You should have  <CONTROLLER_HOME_DIR>/custom/actions/splunk-alert created.

 4. Check if you have custom.xml file in <CONTROLLER_HOME_DIR>/custom/actions/ directory. If yes, add the following xml to the <custom-actions> element.

  ```
      <action>
    		  <type>splunk-alert</type>
          <!-- For Linux/Unix *.sh -->
     		  <executable>splunk-alert.sh</executable>
          <!-- For windows *.bat -->
     		  <!--<executable>splunk-alert.bat</executable>-->
      </action>
  ```

   If you don't have custom.xml already, create one with the below xml content

      ```
      <custom-actions>
          <action>
              <type>splunk-alert</type>
            <!-- For Linux/Unix *.sh -->
              <executable>splunk-alert.sh</executable>
            <!-- For windows *.bat -->
              <!--<executable>splunk-alert.bat</executable>-->
          </action>
        </custom-actions>
      ```
      Uncomment the appropriate executable tag based on windows or linux/unix machine.

##Setting up .splunkrc file

A sample .splunkrc file is included in splunk-alert/conf

1.  Edit the .splunkrc file to add information that allows the Controller to communicate with Splunk.

```
        # Host at which Splunk is reachable (OPTIONAL)
        host=localhost
        # Port at which Splunk is reachable (OPTIONAL)
        # Use the admin port, which is 8089 by default.
        port=8089
        # Splunk username
        username=admin
        # Splunk password
        password=changeme
        # Access scheme (OPTIONAL)
        scheme=https
        # Namespace to use (OPTIONAL)
        namespace=*:*

        #Host Name
        eventHost=localhost
        #Index Name
        index=appdynamics_events
        #Source Type
        sourceType=events
```

Note: An index with index name should be present in Splunk.

2.  Copy the .splunkrc file to the platform home directory of the user that started the Controller. In Linux, this is the environment variable $HOME location; in Windows, it is the environment variable %USERPROFILE% location.

## Installing Custom Actions:

      To create a Custom Action, first refer to the the following topics (requires login):
      * [Creating custom action](http://docs.appdynamics.com/display/PRO13S/Custom+Actions)
      * [Build an Alerting Extension](http://docs.appdynamics.com/display/PRO13S/Build+an+Alerting+Extension)

Now you are ready to use this extension as a custom action. In the AppDynamics UI, go to Alert & Respond -> Actions. Click Create Action. Select Custom Action and click OK. In the drop-down menu you can find the action called 'splunk-alert'.


##Contributing

Find out more in the [AppSphere]() community.

##Support

For any questions or feature request, please contact [AppDynamics Center of Excellence](mailto:help@appdynamics.com).