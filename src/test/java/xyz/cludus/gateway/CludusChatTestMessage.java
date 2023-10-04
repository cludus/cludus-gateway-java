package xyz.cludus.gateway;

import lombok.Data;

@Data
public class CludusChatTestMessage {
    private String from;

    private String to;

    private String content;

    private boolean sent;

    private boolean received;

    private long sentTs;

    private long receivedTs;

    public CludusChatTestMessage(String from, String to, String content) {
        this.from = from;
        this.to = to;
        this.content = content;
    }
}
