package com.blankzhou.netty.server;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.corundumstudio.socketio.SocketIOServer;

@Component
public class ServerRunner {
	
    private final SocketIOServer server;  
    
    @Autowired  
    public ServerRunner(SocketIOServer server) {  
        this.server = server;  
    }
    
    @PostConstruct
    public void startServer() {
    	server.start();
    }
    
    @PreDestroy
    public void stopServer() {
    	server.stop();
    }
}
