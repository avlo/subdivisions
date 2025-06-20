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


## SubDivisions.  Reactive java web-socket client & related utilities: 

1. an easy to use, generalized and extensible [WebSocketClient](https://github.com/avlo/subdivisions/blob/master/src/main/java/com/prosilion/subdivisions/WebSocketClient.java)

2. a nostr-relay [event publishing client](https://github.com/avlo/subdivisions/blob/master/src/main/java/com/prosilion/subdivisions/event/EventPublisher.java)  

3. a multi-nostr-relay capable [request/subscriptions manager](https://github.com/avlo/subdivisions/blob/master/src/main/java/com/prosilion/subdivisions/request/RelaySubscriptionsManager.java)

4. a [NostrRelayClient](https://github.com/avlo/subdivisions/blob/master/src/main/java/com/prosilion/subdivisions/service/NostrRelayClient.java) client composite of 2 & 3 above.


advanced:  

5. a [request consolidator](https://github.com/avlo/subdivisions/blob/master/src/main/java/com/prosilion/subdivisions/request/RequestConsolidator.java), useful for nostr-relay-mesh-networks (ref: [afterimage](https://github.com/avlo/afterimage) nostr-reputation authority)

----

### Dependencies:
- Java 21 (or higher)

#### Internal implementation (auto-imported): 
- Spring [Boot](https://spring.io/projects/spring-boot) 3.4.3 
- Spring [WebSocketSession](https://docs.spring.io/spring-session/reference/guides/boot-websocket.html) 
- Spring [WebFlux](https://docs.spring.io/spring-framework/reference/web/webflux.html) Reactive Web-Socket Client
- [nostr-java-core](https://github.com/avlo/nostr-java-core) (nostr events & tags, event messages, request messages & filters)

----

### Build Tools Requirements

```sh
$ java -version
java version "21.0.5" 2024-10-15 LTS

$ gradle -version
Gradle 8.13
```

----

### Usage
#### 1. Check-out nostr-java-core dependency library

```sh
$ cd <your_git_home_dir>
$ git clone git@github.com:avlo/nostr-java-core.git
$ cd nostr-java-core
$ git checkout develop
$ ./gradlew clean test
$ ./gradlew publishToMavenLocal
```

#### 2. check out and build SubDivisions

```sh
$ cd <your_git_home_dir>
$ git clone git@github.com:avlo/subdivisions.git
$ cd subdivisions
$ git checkout develop
$ ./gradlew clean test
$ ./gradlew publishToMavenLocal
```

#### 3. add SubDivisions dependency to your project

<details>
  <summary>maven</summary>

    <dependency>
      <groupId>com.prosilion</groupId>
      <artifactId>subdivisions</artifactId>
      <version>1.1.0</version>
    </dependency>
</details>
<details>
  <summary>gradle</summary>

    implementation 'com.prosilion:subdivisions:1.1.0'
</details>

