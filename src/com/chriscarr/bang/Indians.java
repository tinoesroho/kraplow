package com.chriscarr.bang;

import java.util.List;

public class Indians extends Card implements Playable {
	public Indians(String name, int suit, int value, int type) {
		super(name, suit, value, type);
	}

	/* (non-Javadoc)
	 * @see com.chriscarr.bang.Playable#canPlay(com.chriscarr.bang.Player, java.util.List, int)
	 */
	public boolean canPlay(Player player, List<Player> players, int bangsPlayed){			
		return true;
	}
	
	/* (non-Javadoc)
	 * @see com.chriscarr.bang.Playable#targets(com.chriscarr.bang.Player, java.util.List)
	 */
	public List<Player> targets(Player player, List<Player> players){
		return Turn.others(player, players);
	}
	
	/* (non-Javadoc)
	 * @see com.chriscarr.bang.Playable#play(com.chriscarr.bang.Player, java.util.List, com.chriscarr.bang.UserInterface, com.chriscarr.bang.Deck, com.chriscarr.bang.Discard)
	 */
	public void play(Player currentPlayer, List<Player> players, UserInterface userInterface, Deck deck, Discard discard){
		discard.add(this);
		Player indianPlayer = Turn.getNextPlayer(currentPlayer, players);
		while(indianPlayer != currentPlayer){
			int bangPlayed = Turn.validPlayBang(indianPlayer, userInterface);
			if(bangPlayed == -1){
				Turn.damagePlayer(indianPlayer, players, currentPlayer, 1, currentPlayer, deck, discard, userInterface);	
			} else {
				discard.add(indianPlayer.getHand().remove(bangPlayed));	
			}
			indianPlayer = Turn.getNextPlayer(indianPlayer, players);
		}
	}
}
