### Concurrency sample for Java EE7 [![Build Status](https://travis-ci.org/WASdev/sample.javaee7.concurrency.svg?branch=master)](https://travis-ci.org/WASdev/sample.javaee7.concurrency)

This application on demonstrates how to use managed executors, managed scheduled executors and context service to perform tasks in parallel in a simple application.

## WAS Liberty
### Running with Maven

The sample can be built using Apache Maven. In the directory where you cloned the repository issue the following command to build the source.

  `$ mvn install`

Then, in the same directory issue the following command to run it on a Liberty server.

  `$ mvn liberty:run-server`

You can connect to the application at [http://hostname:port/sample.javaee7.concurrency/](http://hostname:port/sample.javaee7.concurrency/).

#### Running in Eclipse/WDT with Maven

The WebSphere Development Tools (WDT) for Eclipse can be used to control the server (start/stop/dump/etc.), it also supports incremental publishing with minimal restarts, working with a debugger to step through your applications, etc.

WDT also provides:

- content-assist for server configuration (a nice to have: server configuration is minimal, but the tools can help you find what you need and identify finger-checks, etc.)
- automatic incremental publish of applications so that you can write and test your changes locally without having to go through a build/publish cycle or restart the server (which is not that big a deal given the server restarts lickety-split, but less is more!).
- improved Maven integration for web projects starting with WDT 17.0.0.2 including support for loose applications.

Installing WDT on Eclipse is as simple as a drag-and-drop, but the process is explained on [wasdev.net](https://developer.ibm.com/wasdev/downloads/liberty-profile-using-eclipse/).

#### Import project and running in Eclipse/WDT:

1.	Select menu File -> Import -> Maven -> Existing Maven Projects.
2.	Select Browse... to the top level directory titled sample.javaee7.concurrency and select Finish
3.	Click Yes to the WebSphere Liberty dialog to automatically create server in the Servers view for this project.
4.  Right-click the project and select Run As > Run on Server.
5.  Select the server and click Finish.
6.  Confirm web browser opens with the sample url, [http://localhost:9080/sample.javaee7.concurrency/](http://localhost:9080/sample.javaee7.concurrency/).

## Running with Gradle

This project can also be built and run with [Gradle]. The provided `build.gradle` file applies the [Liberty Gradle Plug-in] and is configured to automatically download and install the Liberty Java EE7 Web Profile runtime from Maven Central. The Liberty Gradle Plug-in has built-in tasks that can be used to create, configure, and run the application on the Liberty server.

Use the following steps to run the application with Gradle:

1. Execute the full Gradle build. The Liberty Gradle Plug-in will download and install the Liberty server.
    ```bash
    $ gradle clean build
    ```

2. To start the server with the Servlet sample execute:
    ```bash
    $ gradle libertyStart
    ```

    Alternatively, execute the run command:
    ```bash
    $ gradle libertyRun --no-daemon
    ```

Once the server has started, the application will be available under [http://localhost:9080/sample.javaee7.jsonp](http://localhost:9080/sample.javaee7.jsonp).

3. To stop the server, execute:
    ```bash
    $ gradle libertyStop
    ```  

Please refer to the [ci.gradle] repository for documentation about using the Liberty Gradle Plug-in.

### WAS Classic

Configure required resources

1.	Verify that a Derby JDBC Provider instance exists. In the administrative console, click Resources > JDBC > JDBC providers.
  - If that provider does not exist, create one with a Connection pool datasource implementation type, and point to the Derby.jar file; for example: ${WAS_INSTALL_ROOT}/derby/lib

2.	Verify that a Default datasource instance is configured. Click Resources > JDBC > Data sources.
  - If that datasource does not exist, create one with the name "Default datasource" and the JNDI name "DefaultDatasource" that points to the Derby JDBC Provider and "${WAS_INSTALL_ROOT}/derby/DefaultDB" database.
  - To create the actual database, remotely connect to your machine hosting WebSphere Classic using SSH.
    - Navigate to ${WAS_INSTALL_ROOT}/derby/bin/embedded/
    - Run "./ij.sh". When you see the prompt "ij>", enter the following command:
        $ connect 'jdbc:derby:DefaultDB;create=true';
    - The default Derby database is created in the following directory: ${WAS_INSTALL_ROOT}/derby/DefaultDB

#### Install using wsadmin tool

1. Start wsadmin tool with Jython option
2. Run command
`AdminApp.install('<path-to-the-app>/sample.javaee7.concurrency.war','[-node <your-node-name> -server <your-server-name> -appname sample.javaee7.concurrency -contextroot sample.javaee7.concurrency -MapWebModToVH [[ sample.javaee7.concurrency sample.javaee7.concurrency.war,WEB-INF/web.xml default_host ]] -MapResEnvRefToRes [[ sample.javaee7.concurrency "" sample.javaee7.concurrency.war,WEB-INF/web.xml com.ibm.ws.samples.concurrency.ConcurrencySampleServlet/contextService javax.enterprise.concurrent.ContextService wm/default ][ sample.javaee7.concurrency "" sample.javaee7.concurrency.war,WEB-INF/web.xml com.ibm.ws.samples.concurrency.ConcurrencySampleServlet/executor javax.enterprise.concurrent.ManagedScheduledExecutorService wm/default ]]]' ) `

3. Save configuration

#### Install using the Administrative Console

1.	In your preferred browser, go to the Integrated Solutions Console; for example: [http://hostname:9060/ibm/console/]
(http://hostname:9060/ibm/console/)
2.	Log in with your user name and password.
3.	Select Applications > New Application.
4.	Select the New Enterprise Application link.
5.	Using the Local file system option, click Browse, and select the war file that you built using Maven.
6.	Click Next to follow the wizard using the default options, until the Finish button is displayed.
7.	When the Confirm changes section is displayed, click Save.
8.	Click Applications > Application Types > WebSphere enterprise applications.
9.	Select the check box next to the sample application, and click Start.

## Notice

© Copyright IBM Corporation 2016, 2017.

## License

```text
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
````

[Liberty Maven Plug-in]: https://github.com/WASdev/ci.maven
[Liberty Gradle Plug-in]: https://github.com/WASdev/ci.gradle
[ci.gradle]: https://github.com/WASdev/ci.gradle
[Gradle]: https://gradle.org
