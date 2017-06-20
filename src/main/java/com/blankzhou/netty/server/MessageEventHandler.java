package com.blankzhou.netty.server;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;

@Component
public class MessageEventHandler {

	private final Logger logger = LoggerFactory.getLogger(MessageEventHandler.class);

	private final SocketIOServer server;

	private static Map<String, ClientInfo> clientInfoMap = new ConcurrentHashMap<>();
	
	static {
		clientInfoMap.put("123456", new ClientInfo("123456"));
	}

	@Autowired
	public MessageEventHandler(SocketIOServer server) {
		this.server = server;
	}

	// 添加connect事件，当客户端发起连接时调用，本文中将clientid与sessionid存入数据库
	// 方便后面发送消息时查找到对应的目标client,
	@OnConnect
	public void onConnect(SocketIOClient client) {
		String clientId = client.getHandshakeData().getSingleUrlParam("clientid");
		ClientInfo clientInfo = clientInfoMap.get(clientId);
		
		if (clientInfo != null) {
			Date nowTime = new Date(System.currentTimeMillis());
			clientInfo.setConnected((short) 1);
			clientInfo.setMostsignbits(client.getSessionId().getMostSignificantBits());
			clientInfo.setLeastsignbits(client.getSessionId().getLeastSignificantBits());
			clientInfo.setLastconnecteddate(nowTime);
			logger.info("客户端{}连上服务器...", clientId);
		}
	}

	// 添加@OnDisconnect事件，客户端断开连接时调用，刷新客户端信息
	@OnDisconnect
	public void onDisconnect(SocketIOClient client) {
		String clientId = client.getHandshakeData().getSingleUrlParam("clientid");
		ClientInfo clientInfo = clientInfoMap.get(clientId);

		if (clientInfo != null) {
			clientInfo.setConnected((short) 0);
			clientInfo.setMostsignbits(null);
			clientInfo.setLeastsignbits(null);
			logger.info("客户端{}连接断开...", clientId);
		}
	}

	// 消息接收入口，当接收到消息后，查找发送目标客户端，并且向该客户端发送消息，且给自己发送消息
	@OnEvent(value = "messageevent")
	public void onEvent(SocketIOClient client, AckRequest request, Message data) {
		String targetClientId = data.getTargetClientId();
		ClientInfo clientInfo = clientInfoMap.get(targetClientId);
		
		if (clientInfo != null && clientInfo.getConnected() != 0) {
			UUID uuid = new UUID(clientInfo.getMostsignbits(), clientInfo.getLeastsignbits());
			Message sendData = new Message();
			sendData.setSourceClientId(data.getSourceClientId());
			sendData.setTargetClientId(data.getTargetClientId());
			sendData.setMsgType("chat");
			sendData.setMsgContent(data.getMsgContent());
//			client.sendEvent("messageevent", sendData);
			server.getClient(uuid).sendEvent("messageevent", sendData);
		}
	}
}
