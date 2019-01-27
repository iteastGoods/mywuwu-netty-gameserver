package com.mywuwu.game.card.poker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Poker {
    private PokerNumber number;
    private PokerType cardType;
}
