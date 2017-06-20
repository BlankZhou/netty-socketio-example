package com.blankzhou.netty.client;

import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter.Listener;
import io.socket.engineio.client.transports.WebSocket;

public class SocketIOClientTest {
	
	public static void main(String args[]) {
		String url = "http://172.17.13.142:9000?clientid=123456";
		CountDownLatch countDownLatch = new CountDownLatch(1);
		
		try {
			IO.Options opts = new IO.Options();
			opts.transports = new String[]{WebSocket.NAME};
			
			
			Socket socket = IO.socket(url, opts);
			socket.on(Socket.EVENT_CONNECT, new Listener() {
				
				@Override
				public void call(Object... args) {
					System.out.println("客户端连接上服务器...");
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					JSONObject json = new JSONObject();
					json.put("sourceClientId", "123456");
					json.put("targetClientId", "123456");
					json.put("msgType", "chat");
					json.put("msgContent", "SocketIO Java Client Test!!");
					socket.emit("messageevent", json);
				}
			});
			
			socket.on(Socket.EVENT_DISCONNECT, new Listener() {
				
				@Override
				public void call(Object... args) {
					System.out.println("客户端断开了链接...");
				}
			});
			
			socket.on("messageevent", new Listener() {
				
				@Override
				public void call(Object... args) {
					JSONObject obj = (JSONObject)args[0];
					System.out.println("收到服务器的信息：" + obj);
					countDownLatch.countDown();
					socket.close();
				}
			});
			
			socket.open();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
