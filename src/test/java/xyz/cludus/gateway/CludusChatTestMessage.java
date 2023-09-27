package xyz.cludus.gateway;

import lombok.Data;

//10000
// 10
// dns

//casandra -> 1000
// gossip -> per to per

//kafka
// zookeper -> consensus (5 -> available 3)


/*

redis:
 user1 -> gateway1
 user2 -> gateway2
 user3 -> gateway1

gateway1
  users:
    user1 -> TCP
    user3 -> TCP

  gateways:
    gateway2 -> 172.23.43.45

gateway2
  users:
    user2 -> TCP

  gateways:
    gateway1 -> 172.23.43.45

 */

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
