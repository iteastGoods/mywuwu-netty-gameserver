package com.mywuwu.gameserver.core;

import com.mywuwu.gameserver.core.websocket.GameWebSocketSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 数据权限类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferData implements Serializable {
    private GameWebSocketSession gameWebSocketSession;
    private String channel;
    private int protocol;
    private byte[] data;
}
