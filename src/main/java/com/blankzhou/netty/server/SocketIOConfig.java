package com.blankzhou.netty.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.corundumstudio.socketio.AuthorizationListener;
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.Transport;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;

@Configuration
public class SocketIOConfig {
	
	@Bean
	public SocketIOServer socketIOServer() {
		com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
		config.setHostname("172.17.13.142");
		config.setPort(9000);
		config.setTransports(Transport.WEBSOCKET);
		config.setAuthorizationListener(new AuthorizationListener() {

			@Override
			public boolean isAuthorized(HandshakeData arg0) {
				return true;
			}  
			
		});
		
		final SocketIOServer server = new SocketIOServer(config);  
        return server;
	}

	@Bean  
    public SpringAnnotationScanner springAnnotationScanner(SocketIOServer socketServer) {  
        return new SpringAnnotationScanner(socketServer);  
    }	
}
