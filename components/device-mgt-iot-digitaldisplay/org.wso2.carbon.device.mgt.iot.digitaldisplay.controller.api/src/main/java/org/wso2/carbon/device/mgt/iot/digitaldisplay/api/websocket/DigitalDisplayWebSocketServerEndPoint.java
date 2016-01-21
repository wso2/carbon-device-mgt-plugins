package org.wso2.carbon.device.mgt.iot.digitaldisplay.api.websocket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.inject.Singleton;
import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import javax.ws.rs.QueryParam;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@ServerEndpoint(value = "/{token}")
@Singleton
public class DigitalDisplayWebSocketServerEndPoint {

    private static Log log = LogFactory.getLog(DigitalDisplayWebSocketServerEndPoint.class);
    private static Map<String,Session> clientSessions = new HashMap<>();

    /**
     * This method will be invoked when a client requests for a
     * WebSocket connection.
     *
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession , @PathParam("token") String token){
        log.info(" Connected with Session Id : " + userSession.getId());
        clientSessions.put(token , userSession);
    }

    /**
     * This method will be invoked when a client closes a WebSocket
     * connection.
     *
     * @param userSession the userSession which is opened.
     */
    @OnClose
    public void onClose(Session userSession){
        log.info("Client disconnected - Session Id : " + userSession.getId());
        clientSessions.values().remove(userSession);

    }

    @OnError
    public void onError(Throwable t){
        log.error("Error occurred " + t );
    }

    /**
     * This method will be invoked when a message received from device
     * to send client.
     *
     * @param token the client of message to be sent.
     * @param message the message sent by device to client
     */
    public static void sendMessage(String token , String message){
        Session session = clientSessions.get(token);
        if(session != null){
            session.getAsyncRemote().sendText(message);
        }else {
            log.error("Client already disconnected.");
        }
    }

}
