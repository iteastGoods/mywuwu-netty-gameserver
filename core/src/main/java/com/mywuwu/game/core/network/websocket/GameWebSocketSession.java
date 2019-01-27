package com.mywuwu.game.core.network.websocket;


import io.netty.channel.ChannelId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.yeauty.pojo.Session;

import java.io.Serializable;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameWebSocketSession implements Serializable {
    @Id
    private String id;
    private ChannelId sessionId;
    private String token;
    private String lastLoginTime;
    private String lastLogoutTime;
    //0: 登陆 1:退出
    private String state;
    private String address;
    private String channel;
    private String roomNumber;
}
