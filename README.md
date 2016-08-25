# AppDynamics Splunk Alerting Extension

This extension works only with a dedicated SaaS controller or an on-prem controller.

##Use Case

Splunk [www.splunk.com](http://www.splunk.com) indexes and makes searchable data from any app, server or network device in real time including logs, config files, messages, alerts, scripts and metrics.

### Prerequisites

 1. User needs to have edit_tcp permission to post events to Splunk


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

##Setting up config.yml file

A sample config.yml file is included in splunk-alert/conf

1.  Edit the config.yml file to add information that allows the Controller to communicate with Splunk.

```
        # Host at which Splunk is reachable
        host: localhost
        # Port at which Splunk is reachable
        # Use the admin port, which is 8089 by default.
        port: 8089
        # Splunk username
        username: admin
        # Splunk password, provide password or passwordEncrypted and encryptionKey.
        password: admin

        passwordEncrypted:
        encryptionKey:

        #Proxy server URI
        proxyUri:
        #Proxy server user name
        proxyUser:
        #Proxy server password
        proxyPassword:

        #Index Name, should be available in Splunk
        index: appdynamics_events
        #Source Type
        sourceType: events
```

Note: An index with the index name should be present in Splunk.

## Installing Custom Actions:
To create a Custom Action, first refer to the the following topics (requires login):
* [Creating custom action](http://docs.appdynamics.com/display/PRO13S/Custom+Actions)
* [Build an Alerting Extension](http://docs.appdynamics.com/display/PRO13S/Build+an+Alerting+Extension)


Now you are ready to use this extension as a custom action. In the AppDynamics UI, go to Alert & Respond -> Actions. Click Create Action. Select Custom Action and click OK. In the drop-down menu you can find the action called 'splunk-alert'.


##Contributing

Find out more in the [AppSphere](http://www.appdynamics.com/community/exchange/extension/splunk-alerting-extension/) community.

##Support

For any questions or feature request, please contact [AppDynamics Center of Excellence](mailto:help@appdynamics.com).
