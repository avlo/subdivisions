//package com.prosilion.subdivisions.client.virtualthread;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.prosilion.nostr.NostrException;
//import com.prosilion.nostr.codec.BaseMessageDecoder;
//import com.prosilion.nostr.message.BaseMessage;
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
//import java.net.Socket;
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.WebSocket;
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.atomic.AtomicReference;
//import lombok.NonNull;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.socket.TextMessage;
//
//@Slf4j
//class VThreadWebSocketClientRxR {
//  PrintWriter out;
//  BufferedReader in;
//
//  protected VThreadWebSocketClientRxR(@NonNull String relayUri) throws IOException {
//    URI uri = URI.create(relayUri);
//
//
//    HttpClient client = HttpClient.newHttpClient();
//    CompletableFuture<WebSocket> ws = client.newWebSocketBuilder()
//        .buildAsync(URI.create("ws://websocket.example.com"), listener);
//
//    try (Socket echoSocket = new Socket(uri.getHost(), uri.getPort())) {
//      this.out = new PrintWriter(echoSocket.getOutputStream(), true);
//      this.in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
//    }
//  }
//
//  protected <T extends BaseMessage> List<BaseMessage> send(T message) throws IOException {
//    String json = message.encode();
//    TextMessage textMessage = new TextMessage(json);
//    AtomicReference<List<BaseMessage>> decode = null;
//
//    Thread.ofVirtual().start(() -> {
//      byte[] bytes = textMessage.asBytes();
//      out.println(bytes);
//
//      List<BaseMessage> list = in.lines().map(s -> {
//        try {
//          return BaseMessageDecoder.decode(s);
//        } catch (JsonProcessingException e) {
//          throw new NostrException("NESTED SHIT");
//        }
//      }).toList();
//
//      decode.set(list);
//    });
//    List<BaseMessage> baseMessages = decode.get();
//    return baseMessages;
//  }
//}
