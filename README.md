```java
 ________  ___  ___  ________  ________  ___  ___      ___ ___  ________  ___  ________  ________   ________      
|\   ____\|\  \|\  \|\   __  \|\   ___ \|\  \|\  \    /  /|\  \|\   ____\|\  \|\   __  \|\   ___  \|\   ____\     
\ \  \___|\ \  \\\  \ \  \|\ /\ \  \_|\ \ \  \ \  \  /  / | \  \ \  \___|\ \  \ \  \|\  \ \  \\ \  \ \  \___|_    
 \ \_____  \ \  \\\  \ \   __  \ \  \ \\ \ \  \ \  \/  / / \ \  \ \_____  \ \  \ \  \\\  \ \  \\ \  \ \_____  \   
  \|____|\  \ \  \\\  \ \  \|\  \ \  \_\\ \ \  \ \    / /   \ \  \|____|\  \ \  \ \  \\\  \ \  \\ \  \|____|\  \  
    ____\_\  \ \_______\ \_______\ \_______\ \__\ \__/ /     \ \__\____\_\  \ \__\ \_______\ \__\\ \__\____\_\  \ 
   |\_________\|_______|\|_______|\|_______|\|__|\|__|/       \|__|\_________\|__|\|_______|\|__| \|__|\_________\
   \|_________|                                                   \|_________|                        \|_________|                                                  
```


# SubDivisions.  A flexible java web-socket client & related utilities. #

1. a generalized, simple-yet-powerful and easy-to-use [WebSocketClient](https://github.com/avlo/subdivisions/blob/master/src/main/java/com/prosilion/subdivisions/WebSocketClient.java)

2. a nostr-relay [event publishing client](https://github.com/avlo/subdivisions/blob/master/src/main/java/com/prosilion/subdivisions/event/EventPublisher.java)  

3. a multi-relay-capable [request/subscriptions manager](https://github.com/avlo/subdivisions/blob/master/src/main/java/com/prosilion/subdivisions/request/RelaySubscriptionsManager.java)

4. a composite [NostrRelayClient](https://github.com/avlo/subdivisions/blob/master/src/main/java/com/prosilion/subdivisions/service/NostrRelayClient.java) client  comprised of both 2 and 3 above.


advanced:  

5. a [request consolidator](https://github.com/avlo/subdivisions/blob/master/src/main/java/com/prosilion/subdivisions/request/RequestConsolidator.java), useful for nostr-relay-mesh-networks (ex: [afterimage](https://github.com/avlo/afterimage) nostr-reputation authority)

----


###### built using [Spring Boot (v.3.4.3)](https://spring.io/projects/spring-boot) atop [Spring WebSocketClient](https://docs.spring.io/spring-boot/reference/messaging/websockets.html) ######
