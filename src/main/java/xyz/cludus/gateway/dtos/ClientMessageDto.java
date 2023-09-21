package xyz.cludus.gateway.dtos;

import lombok.Data;

/**
 * This DTO refers to a chat message sent by a client device.
 * The 'action' field specifies what the server should do with the message.
 * The 'token' field contains an auth token identifying the user id,
 * the device id and the session id of the sender of the message.
 * The 'seq' field is monotonic integer that identifies the message
 * for the current session of the sender.
 * The 'recipient' field contains the user or group for whom this message is intended.
 * The 'content' field contains the actual content of the message.
 */
@Data
public class ClientMessageDto {
    /**
     * Values for the 'action' field
     */
    public enum Actions {
        /**
         * The message should be sent to the recipient
         */
        SEND,
        /**
         * The message is a simple heartbeat message, it should be recorded.
         */
        HEARTBEAT
    }

    private Actions action;
    private String recipient;
    private String content;
}
