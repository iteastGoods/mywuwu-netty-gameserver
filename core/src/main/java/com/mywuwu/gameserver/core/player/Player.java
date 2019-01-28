package com.mywuwu.gameserver.core.player;

import com.mywuwu.gameserver.core.websocket.GameWebSocketSession;
import lombok.Data;

@Data
public class Player {
    private GameWebSocketSession gameWebSocketSession;
    public int chip;
    private boolean isReady;
    private String roomId;
    private boolean isDisConnection;
    private boolean isDisbanded;

    private boolean isOp;

    public Player(int chip, boolean isReady, GameWebSocketSession gameWebSocketSession) {
        this.chip = chip;
        this.isReady = isReady;
        this.gameWebSocketSession = gameWebSocketSession;
    }
}
