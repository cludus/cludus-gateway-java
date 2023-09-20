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
        ERROR
    }

    private Actions action;
    private long seq;
    private long messageSeq;
    private String errorMsg;

    public static ServerMessageDto ack(long seq, long messageSeq) {
        ServerMessageDto result = new ServerMessageDto();
        result.action = Actions.ACK;
        result.seq = seq;
        result.messageSeq = messageSeq;
        return result;
    }

    public static ServerMessageDto error(long seq, long messageSeq, String errorMsg) {
        ServerMessageDto result = new ServerMessageDto();
        result.action = Actions.ERROR;
        result.seq = seq;
        result.messageSeq = messageSeq;
        result.errorMsg = errorMsg;
        return result;
    }
}
