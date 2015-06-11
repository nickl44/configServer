#Spring Cloud Config Server & Bus Tutorial
##Microservices in this tutorial:

###ConfigLocal
  This µService loads it's config from LOCAL filesystem.  
  This is the simplest case, but is a 12-Factor violation as props are bundled locally at:
  `/src/main/resources/application.yml`
  this is a yml file , but we also support regular properties files e.g.
  `/src/main/resources/application.properties`  

  Start this app with:
    `mvn spring-boot:run`
  then hit:
    `curl localhost:8080`
  App will return:
    book.title=Inviting Disaster     // where the book title is sourced from the local props file


  **Notable Project Components:**  
    `/pom.xml`      // contains spring-boot-starter-web
    `/src/main/java/sprBootWeb/Book.java`  // contains public String title variable that has it's value injected by Spring.
    `/src/main/java/sprBootWeb/MyController.java`   // Book.java is Autowired in. Hit localhost to see book title value. e.g. `curl localhost:8080`


###ConfigLocalActuator
  As ConfigLocal but uses Spring Boot Actuator thus exposing useful endpoints,

  Start this app with:
    `mvn spring-boot:run`  
  then hit:  
    `curl localhost:8080/env`  
  App will return:
    book.title=Inviting Disaster     // contained amongst the JSON string returned, NB Use JSONView in Chrome to format output.


###ConfigServer
  Minimum required project to launch a Spring Cloud Config Server.  
  Provides configuration to requesting Spring Cloud Client Apps.  
  Loads configurations from GitHub.  
  Github root is specified in: /src/main/resources/application.yml e.g. uri: https://github.com/nlawpiv/config-repo.git  

  Start this app with:  
    `mvn spring-boot:run`   // Spring Cloud Config Server will start and listen on port specified in application.yml

**Notable Project Components:**  
      `/pom.xml`      // contains spring-cloud-starter-parent & spring-cloud-config-server
      `/src/main/java/demo/SprBootConfigSvrApplication.java`  // contains @EnableConfigServer annotation, otherwise this is just a normal Spring Boot start class.  
      `/src/main/resources/application.yml` // contains git repo uri for config files.

  Note: 'Spring Cloud Services' available in beta as part of PCF provides a Spring Cloud Config Server natively without the need to run this.

###ConfigClient
  Minimum additions to the ConfigLocal necessary to turn it into a client of the Spring Cloud Config Server.
  Ensure the ConfigServer project is running then:

  Start this app with:  
    `mvn spring-boot:run`            // You will see some log output in the ConfigServer when the ConfigClient connects to it.  
    then hit:
      `curl localhost:8080`
    App will return:
      book.title=Domain Driven Design     // where title is sourced from the Spring Cloud Config Server

  **Notable Project Components:**  
    `/pom.xml`      // contains spring-cloud-starter-parent & spring-cloud-config-client  
    `/src/main/resources/bootstrap.yml`   // specifies application.name (sprBootConfigClient), this is passed to config-server and thus it tries to load the client's configuration from the git file github.com/nlawpiv/config-repo/sprBootConfigClient.yml

  Note:
* Once the configuration is loaded into the client the config-server may go-away.
* If the config-server is not running when the client starts, the client will fall back to locally bundled properties if they exist. Thus the config-server provided config always takes highest precedence.

###ConfigClientRefresh
  Minimum additions to the ConfigClient necessary to make configuration hot reloadable.
  Ensure the ConfigServer project is running then:

  Start this app with:  
    `mvn spring-boot:run`            // You will see some log output in the ConfigServer when the ConfigClient connects to it.  
    then hit:
      `curl localhost:8080`  
    App will return:
      book.title=Domain Driven Design     // where title is sourced from the Spring Cloud Config Server  
    Now we will change the value provided by the ConfigServer and hot-reload the client...  
    `vi config-repo/sprBootConfigClient.yml`
        change content to: title: Domain Driven Design2  
        `git commit -am "chngd value" ; git push origin master`   // push to remote git repo  
        `curl -X POST localhost:8080/refresh`       // trigger refresh endpoint on ConfigClientRefresh  
      `curl localhost:8080`
    App will return:
      book.title=Domain Driven Design2     // where UPDATED title is sourced from the Spring Cloud Config Server

  **Notable Project Components:**  
    `/pom.xml`      // added spring-boot-starter-actuator to open /refresh endpoint  
    `/src/main/java/sprBootWeb/Book.java`   // added @RefreshScope to class, this is essential to trigger lazy reloading when a method is called (never access public vars directly, this doesn't trigger a reload!).


Spring Cloud Bus
================
Now we add Spring Cloud Bus so we may trigger reloads of all parties listening to the bus.
###RabbitMQ is used as the bus.
  RabbitMQ must be running:  
  on mac:  
  `brew install rabbitmq`	// install if nec (mac)  
  `/usr/local/sbin/rabbitmq-server`	// run it

  Both programs ConfigServerCloudBus & ConfigClientCloudBus will connect to the bus
    Any reports of 'java.net.ConnectException: Connection refused' means it cannot connect to RabbitMQ.

  Bus location is defaulted to localhost , otherwise it may be specified in application.yml:
  ```

       spring:
         rabbitmq:
           host: mybroker.com
           port: 5672
           username: user
           password: secret
```


###ConfigServerCloudBus
  Minimum additions to ConfigServer necessary to add Spring Cloud Bus support.  

  Start this app with:
    `mvn spring-boot:run`   // Spring Cloud Config Server will start and listen on port specified in application.yml  

  **Notable Project Components:**  
      `/pom.xml`      // added spring-cloud-starter-bus-amqp


###ConfigClientCloudBus
  Minimum additions to ConfigClientRefresh necessary to add Spring Cloud Bus support.

  Make sure ConfigServerCloudBus is running then Start this app with:  
    `mvn spring-boot:run`  
    `SERVER_PORT=8081 mvn spring-boot:run`  // start a 2nd instance on a diff port  
    then hit both app instances:  
      `curl localhost:8080`  
      `curl localhost:8081`  
    Apps will return:
      book.title=Domain Driven Design     // where title is sourced from the Spring Cloud Config Server  
    Now we will change the value provided by the ConfigServerCloudBus and hot-reload the clients...  
    `vi config-repo/sprBootConfigClient.yml`
        change content to: title: Domain Driven Design2  
        `git commit -am "chngd value" ; git push origin master`   // push to remote git repo  
        `curl -X POST localhost:8080/bus/refresh`       // trigger refresh endpoint on first instance, notice how ALL processes respond and output debug trace in their windows as the refresh event propagates to all.  
      `curl localhost:8080`
      `curl localhost:8081`  
    App will return:
    `book.title=Domain Driven Design2`    // where UPDATED title is sourced from the Spring Cloud Config Server  
    NOTE: You can still hit http://localhost:8080/refresh to refresh a single instance.


  **Notable Project Components:**  
      `/pom.xml`      // added spring-cloud-starter-bus-amqp
