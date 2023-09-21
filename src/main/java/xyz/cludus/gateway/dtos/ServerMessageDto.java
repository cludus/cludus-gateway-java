package xyz.cludus.gateway.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class ServerMessageDto {
    /**
     * Values for the 'action' field
     */
    public enum Actions {
        /**
         * The message has been received
         */
        ACK,
        ERROR,
        MESSAGE
    }

    private Actions action;
    private String errorMsg;
    private String sender;
    private String content;

    public static ServerMessageDto ack() {
        ServerMessageDto result = new ServerMessageDto();
        result.action = Actions.ACK;
        return result;
    }

    public static ServerMessageDto error(String errorMsg) {
        ServerMessageDto result = new ServerMessageDto();
        result.action = Actions.ERROR;
        result.errorMsg = errorMsg;
        return result;
    }

    public static ServerMessageDto message(String sender, String content) {
        ServerMessageDto result = new ServerMessageDto();
        result.action = Actions.MESSAGE;
        result.sender = sender;
        result.content = content;
        return result;
    }
}
