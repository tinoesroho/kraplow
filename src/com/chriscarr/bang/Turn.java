package com.chriscarr.bang;

import java.util.ArrayList;
import java.util.List;

public class Turn {

	private List<Player> players;
	private Player currentPlayer; 
	private UserInterface userInterface;
	private boolean donePlaying = false;
	private Discard discard;
	private Deck deck;
	private int bangsPlayed = 0;
	
	public void setPlayers(List<Player> players) {
		this.players = players;
	}

	public Player getPlayersTurn() {		
		return currentPlayer;
	}
	
	public static Player getNextPlayer(Player player, List<Player> players){
		int index = players.indexOf(player);
		if(index == players.size() - 1){
			index = 0;
		} else {
			index = index + 1;
		}
		return players.get(index);
	}
	
	public static Player getPreviousPlayer(Player player, List<Player> players){
		int index = players.indexOf(player);
		if(index == 0){
			index = players.size() - 1;
		} else {
			index = index - 1;
		}
		return players.get(index);
	}

	public void nextTurn() {		
		currentPlayer = getNextPlayer(currentPlayer, players);
		donePlaying = false;
		bangsPlayed = 0;
		turnLoop(currentPlayer);
	}
	
	private void turnLoop(Player currentPlayer){
		try{
			if(isDynamiteExplode()){
				Player explodePlayer = currentPlayer;
				userInterface.printInfo("Dynamite Exploded on " + explodePlayer.getName());
				damagePlayer(explodePlayer, players, currentPlayer, 3, null, deck, discard, userInterface);			
			} else {			
				passDynamite();
			}
			if(!isInJail() && players.contains(currentPlayer)){
				this.drawCards(currentPlayer, deck);
				while(!donePlaying && players.contains(currentPlayer)){
					play();
				}
			}
		}catch(EndOfGameException e){
			e.printStackTrace();
		}
		if(players.contains(currentPlayer)){
			discard(currentPlayer);
		}
		nextTurn();
	}

	public void setSheriff() {
		for(int i = 0; i < players.size(); i++){
			if(players.get(i).getRole() == Player.SHERIFF){
				currentPlayer = players.get(i);
				turnLoop(currentPlayer);
			}
		}
	}
	
	public void setSheriffManualTest() {
		for(int i = 0; i < players.size(); i++){
			if(players.get(i).getRole() == Player.SHERIFF){
				currentPlayer = players.get(i);
			}
		}
	}


	public void drawCards(Player player, Deck deck) {
		Hand hand = player.getHand();		
		if(Figure.KITCARLSON.equals(player.getName())){			
			List<Object> cards = pullCards(deck, 3);
			Object cardToPutBack = chooseValidCardToPutBack(player, cards, userInterface);
			cards.remove(cardToPutBack);
			deck.add(cardToPutBack);
			for(Object card : cards){
				hand.add(card);
			}
		} else if(Figure.JESSEJONES.equals(player.getName())){
			List<Player> otherPlayers = new ArrayList<Player>();
			for(Player other: players){
				if(!other.equals(player) && other.getHand().size() > 0){
					otherPlayers.add(other);
				}
			}
			boolean chosenFromPlayer = false;
			if(!otherPlayers.isEmpty()){
				chosenFromPlayer = userInterface.chooseFromPlayer(player);
			}
			if(chosenFromPlayer){	
				Player chosenPlayer = getValidChosenPlayer(currentPlayer, otherPlayers, userInterface);
				Object randomCard = chosenPlayer.removeRandom();			
				hand.add(randomCard);
			} else {
				hand.add(deck.pull());
			}
			hand.add(deck.pull());
		} else if(Figure.PEDRORAMIREZ.equals(player.getName())){
			if(!discard.isEmpty()){
				boolean chosenDiscard = userInterface.chooseDiscard(player);
				if(chosenDiscard){
					hand.add(discard.remove());
				} else {
					hand.add(deck.pull());
				}
			} else {
				hand.add(deck.pull());
			}
			hand.add(deck.pull());
		} else {
			hand.add(deck.pull());
			Object secondCard = deck.pull();		
			hand.add(secondCard);
			if(Figure.BLACKJACK.equals(player.getName())){				
				int suit = ((Card)secondCard).getSuit();
				userInterface.printInfo(Figure.BLACKJACK + " drew a " + Card.suitToString(suit) + " " + ((Card)secondCard).getName());
				if(suit == Card.HEARTS || suit == Card.DIAMONDS){
					hand.add(deck.pull());
				}			
			}
		}
	}

	public void discard(Player player) {
		discardTwoCardsForLife(player, userInterface);
		Hand hand = player.getHand();
		while(hand.size() > player.getHealth()){
			askPlayerToDiscard(player, discard);
		}
	}

	private void askPlayerToDiscard(Player player, Discard discard) {
		int card = -1;
		while(card < 0 || card > player.getHand().size() - 1){
			card = userInterface.askDiscard(player);
		}
		Object removedCard = (Card)player.getHand().remove(card);
		discard.add(removedCard);
	}

	public void setUserInterface(UserInterface userInterface) {
		this.userInterface = userInterface;
	}

	public void play() throws EndOfGameException {
		Hand hand = currentPlayer.getHand();
		int card = -2;
		while(card < -1 || card > hand.size() - 1){
			card = userInterface.askPlay(currentPlayer);
		}
		if(hand.size() == 0 || card == -1){
			donePlaying = true;
			return;
		}		
		Card playedCard = (Card)hand.get(card);
		String cardName = playedCard.getName();
		if(playedCard.getType() == Card.TYPEGUN){
			hand.remove(card);
			playGun(playedCard, currentPlayer.getInPlay(), discard);			
		} else if(playedCard.getType() == Card.TYPEITEM){
			if(isItemPlayable(currentPlayer, cardName, players)){
				hand.remove(card);
				Player targetPlayer = currentPlayer;
				if(cardName == Card.CARDJAIL){
					List<Player> jailablePlayers = getJailablePlayers(currentPlayer, players);					
					targetPlayer = getValidChosenPlayer(currentPlayer, jailablePlayers, userInterface);
				}
				targetPlayer.addInPlay(playedCard);
			}
		} else {			
			if(playedCard instanceof Playable){
				Playable playable = ((Playable)playedCard);
				boolean canPlay = playable.canPlay(currentPlayer, players, bangsPlayed);
				if(!canPlay){
					return;
				} else {
					if(playedCard instanceof Bang || playedCard instanceof Missed){
						bangsPlayed++;
					}
					discard.add(hand.remove(card));
					playable.play(currentPlayer, players, userInterface, deck, discard);
					return;
				}
			}		
		}
	}
	
	static boolean validPlayMiss(Player player, int misses, int missesRequired, UserInterface userInterface){
		while(true){
			boolean playedMiss = userInterface.respondMiss(player, misses, missesRequired);
			if(!playedMiss || (playedMiss && misses >= missesRequired)){
				return playedMiss;
			}
		}
	}
	
	static boolean validPlayBeer(Player player, int beers, UserInterface userInterface){
		while(true){
			boolean playedMiss = userInterface.respondBeer(player, beers);
			if(!playedMiss || (playedMiss && beers > 0)){
				return playedMiss;
			}
		}
	}
	
	static boolean validPlayBang(Player player, int bangs, UserInterface userInterface){
		while(true){
			boolean playerShot = userInterface.respondBang(player, bangs);
			if(!playerShot || (playerShot && bangs > 0)){
				return playerShot;
			}
		}
	}
	
	private void playGun(Card gun, InPlay inPlay, Discard discard){		
		if(inPlay.hasGun()){
			if(inPlay.getGunName().equals(gun.getName())){
				return;
			}
			discard.add(inPlay.removeGun());
		}
		inPlay.setGun(gun);
	}

	public boolean isDonePlaying() {
		return donePlaying;
	}

	public void setDiscard(Discard discard) {
		this.discard = discard;
	}

	public void setDeck(Deck deck) {
		this.deck = deck;
	}

	public static List<Player> getPlayersWithinRange(Player player, List<Player> players, int range) {
		List<Player> others = new ArrayList<Player>();
		for(Player otherPlayer : players){
			int distance = AlivePlayers.getDistance(players.indexOf(player), players.indexOf(otherPlayer), players.size());
			if(otherPlayer.getInPlay().hasItem(Card.CARDMUSTANG)){
				distance = distance + 1;
			}
			if(Figure.PAULREGRET.equals(otherPlayer.getName())){
				distance = distance + 1;
			}
			if(player.getInPlay().hasItem(Card.CARDAPPALOOSA)){
				distance = distance - 1;
			}
			if(Figure.ROSEDOOLAN.equals(player.getName())){
				distance = distance - 1;
			}
			if(distance <= range){
				others.add(otherPlayer);
			}
		}
		return others(player, others);
	}

	public boolean isDynamiteExplode(){
		InPlay currentInPlay = currentPlayer.getInPlay();
		if(currentInPlay.hasItem(Card.CARDDYNAMITE)){
			Card drawnCard = (Card)draw(currentPlayer, deck, discard, userInterface);
			return Card.isExplode(drawnCard);
		}
		return false;
	}
	
	public void passDynamite() {
		InPlay currentInPlay = currentPlayer.getInPlay();
		if(currentInPlay.hasItem(Card.CARDDYNAMITE)){
			Object dynamiteCard = currentInPlay.removeDynamite();
			Player nextPlayer = getNextPlayer(currentPlayer, players);
			InPlay nextInPlay = nextPlayer.getInPlay();
			userInterface.printInfo("Dynamite Passed to " + nextPlayer.getName());
			nextInPlay.add(dynamiteCard);
		}
	}

	public static Object draw(Player player, Deck deck, Discard discard, UserInterface userInterface) {
		if(Figure.LUCKYDUKE.equals(player.getName())){
			List<Object> cards = pullCards(deck, 2);
			int chosenCard = -1;
			while(chosenCard < 0 || chosenCard > cards.size() - 1){
				chosenCard = userInterface.chooseDrawCard(player, cards);
			}
			for(Object card : cards){
				discard.add(card);
			}
			Card drawnCard = (Card)cards.get(chosenCard);
			userInterface.printInfo(player.getName() + " drew a " + Card.valueToString(drawnCard.getValue()) + " of " + Card.suitToString(drawnCard.getSuit()) + " " + drawnCard.getName());
			return cards.get(chosenCard);
		} else {
			Object card = deck.pull();
			Card drawnCard = (Card)card;
			userInterface.printInfo(player.getName() + " drew a " + Card.valueToString(drawnCard.getValue()) + " of " + Card.suitToString(drawnCard.getSuit()) + " " + drawnCard.getName());
			discard.add(card);			
			return card;
		}
	}

	public boolean isInJail() {		
		InPlay currentInPlay = currentPlayer.getInPlay();
		if(currentInPlay.hasItem(Card.CARDJAIL)){
			Object jailCard = currentInPlay.removeJail();
			discard.add(jailCard);
			Card drawn = (Card)draw(currentPlayer, deck, discard, userInterface);
			boolean inJail =  drawn.getSuit() != Card.HEARTS;
			if(inJail){
				userInterface.printInfo(currentPlayer.getName() + " stays in jail");
			} else {
				userInterface.printInfo(currentPlayer.getName() + " breaks out of jail");
			}
			return inJail;
		}
		return false;
	}

	public static int isBarrelSave(Player player, Deck deck, Discard discard, UserInterface userInterface) {
		int misses = 0;
		if(Figure.JOURDONNAIS.equals(player.getName())){
			Card drawn = (Card)draw(player, deck, discard, userInterface);
			if(drawn.getSuit() == Card.HEARTS){
				misses = misses + 1;
			}
		}
		InPlay currentInPlay = player.getInPlay();		
		if(currentInPlay.hasItem(Card.CARDBARREL)){			
			Card drawn = (Card)draw(player, deck, discard, userInterface);
			if(drawn.getSuit() == Card.HEARTS){
				misses = misses + 1;
			}
		}
		return misses;
	}
	
	public static void damagePlayer(Player player, List<Player> players, Player currentPlayer, int damage, Player damager, Deck deck, Discard discard, UserInterface userInterface) throws EndOfGameException{
		discardTwoCardsForLife(player, userInterface);
		player.setHealth(player.getHealth() - damage);
		if(Figure.BARTCASSIDY.equals(player.getName())){
			for(int i = 0; i < damage; i++){
				player.getHand().add(deck.pull());
			}
		} else if(damager != null && Figure.ELGRINGO.equals(player.getName())){
			Hand otherHand = damager.getHand();
			if(otherHand.size() != 0){
				Hand playerHand = player.getHand();
				playerHand.add(otherHand.removeRandom());
			}
		}
		if(player.getHealth() <= 0 && players.size() > 2){
			boolean doNotPlayBeer = false;
			while(!doNotPlayBeer && player.getHealth() <= 0){
				if(validPlayBeer(player, player.countBeers(), userInterface)){
					player.setHealth(player.getHealth() + 1);
					discard.add(player.getHand().removeBeer());
				} else {
					doNotPlayBeer = true;
				}
			}
			if(player.getHealth() <= 0){
				handleDeath(player, damager, currentPlayer, players, userInterface, deck, discard);				
			}
		}
	}

	public static void handleDeath(Player player, Player damager, Player currentPlayer, List<Player> players, UserInterface userInterface, Deck deck, Discard discard) throws EndOfGameException {
		if(player.equals(currentPlayer)){
			currentPlayer = getPreviousPlayer(currentPlayer, players);
		}
		players.remove(player);
		userInterface.printInfo(player.getName() + " is dead. Role was " + Player.roleToString(player.getRole()));
		if(damager != null){
			if(damager.getRole() == Player.SHERIFF && player.getRole() == Player.DEPUTY){
				userInterface.printInfo(damager.getName() + " killed own deputy, loses all cards");
				discardAll(damager, discard);
			} else if(player.getRole() == Player.OUTLAW) {
				userInterface.printInfo(damager.getName() + " killed an outlaw, draws 3 cards");
				deckToHand(damager.getHand(), deck, 3);
			}
			deadDiscardAll(player, players, discard);
			if(isGameOver(players)){
				userInterface.printInfo("Winners are " + getWinners(players));
				throw new EndOfGameException("Game over");
			}
		}		
	}
	
	public static void discardAll(Player player, Discard discard){
		List<Object> discardCards = new ArrayList<Object>();
		Hand hand = player.getHand();
		while(hand.size() != 0){
			discardCards.add(hand.remove(0));
		}
		InPlay inPlay = player.getInPlay();
		discardCards.add(inPlay.removeGun());
		while(inPlay.count() > 0){
			discardCards.add(inPlay.remove(0));
		}
		for(Object card : discardCards){
			discard.add(card);
		}
	}
	
	public static void deadDiscardAll(Player player, List<Player> players, Discard discard){
		List<Object> discardCards = new ArrayList<Object>();
		Hand hand = player.getHand();
		hand.setEmptyListener(new DoNothingEmptyHandListener());
		while(hand.size() != 0){
			discardCards.add(hand.remove(0));
		}
		InPlay inPlay = player.getInPlay();
		if(inPlay.hasGun()){
			discardCards.add(inPlay.removeGun());
		}
		while(inPlay.count() > 0){
			discardCards.add(inPlay.remove(0));
		}
		Player vultureSam = null;
		for(Player alivePlayer : players){
			if(Figure.VULTURESAM.equals(alivePlayer.getName())){
				vultureSam = alivePlayer;
			}
		}
		if(vultureSam == null){
			for(Object card : discardCards){
				discard.add(card);
			}
		} else {
			for(Object card : discardCards){
				vultureSam.getHand().add(card);
			}
		}
	}
	
	public static boolean isGameOver(List<Player> players) {
		return isDead(Player.SHERIFF, players) || isDead(Player.RENEGADE, players) && isDead(Player.OUTLAW, players);
	}
	
	private static boolean isDead(int role, List<Player> players) {
		for(Player player : players){
			if(player.getRole() == role){
				return false;
			}
		}
		return true;
	}

	public static String getWinners(List<Player> players) {
		if(isDead(Player.OUTLAW, players) && isDead(Player.RENEGADE, players)){
			return "Sheriff and Deputies";	
		} else if(isDead(Player.SHERIFF, players) && (!isDead(Player.DEPUTY, players) || !isDead(Player.OUTLAW, players))) {
			return "Outlaws";
		} else if(isDead(Player.DEPUTY, players) && isDead(Player.OUTLAW, players) && isDead(Player.SHERIFF, players)){
			return "Renegades";
		} else {
			throw new RuntimeException("No Winner");
		}
	}
	
	public static void discardTwoCardsForLife(Player player, UserInterface userInterface){
		if(Figure.SIDKETCHUM.equals(player.getName())){
			Hand hand = player.getHand();
			if(hand.size() >= 2){
				List<Object> cardsToDiscard = null;
				while(cardsToDiscard == null || cardsToDiscard.size() % 2 != 0){
					cardsToDiscard = userInterface.chooseTwoDiscardForLife(player);
				}
				if(cardsToDiscard.size() % 2 == 0){
					for(Object card : cardsToDiscard){
						hand.remove(card);
					}
					player.setHealth(player.getHealth() + (cardsToDiscard.size() / 2));
				}
			}
		}
	}
	
	public static boolean playerHasCardsToTake(Player player){
		boolean emptyHand = player.getHand().isEmpty();
		InPlay inPlay = player.getInPlay();
		boolean hasGun = inPlay.hasGun();
		boolean emptyInPlay = inPlay.isEmpty();
		return !emptyHand || hasGun || !emptyInPlay;
	}
	
	public Player getCurrentPlayer(){
		return currentPlayer;
	}
	
	public static List<Player> othersWithCardsToTake(Player player, List<Player> others){
		List<Player> othersCopy = new ArrayList<Player>();
		for(Player otherPlayer : others){
			if(playerHasCardsToTake(otherPlayer)){
				othersCopy.add(otherPlayer);
			}
		}
		othersCopy.remove(player);
		return othersCopy;
	}
	
	public static List<Player> others(Player player, List<Player> others){
		List<Player> othersCopy = new ArrayList<Player>();
		for(Player otherPlayer : others){
			othersCopy.add(otherPlayer);
		}
		othersCopy.remove(player);
		return othersCopy;
	}
	
	public static List<Object> pullCards(Deck deck, int countCards){
		List<Object> cards = new ArrayList<Object>();
		for(int i = 0; i < countCards; i++){
			cards.add(deck.pull());
		}
		return cards;
	}
	
	public static void deckToHand(Hand hand, Deck deck, int countCards){
		for(int i = 0; i < countCards; i++){
			hand.add(deck.pull());
		}
	}
	
	public static List<Player> getJailablePlayers(Player player, List<Player> players){
		List<Player> others = new ArrayList<Player>();
		for(Player otherPlayer : players){
			boolean isInJail = otherPlayer.getInPlay().hasItem(Card.CARDJAIL);
			boolean isSheriff = otherPlayer.getRole() == Player.SHERIFF;
			boolean isPlayer = otherPlayer.equals(player);
			if(!isInJail && !isSheriff && !isPlayer){
				others.add(otherPlayer);
			}
		}
		return others;
	}
	
	public static List<Player> getPlayersWithCards(List<Player> players){
		List<Player> playersWithCards = new ArrayList<Player>();
		for(Player player : players){
			if(playerHasCardsToTake(player)){
				playersWithCards.add(player);
			}
		}
		return playersWithCards;
	}
	
	public static List<String> getPlayersNames(List<Player> players){
		List<String> names = new ArrayList<String>();
		for(Player player : players){
			names.add(player.getName());
		}
		return names;
	}
	
	public static boolean isItemPlayable(Player player, String cardName, List<Player> players){
		if(Card.CARDJAIL.equals(cardName)){
			List<Player> others = getJailablePlayers(player, players);
			return !others.isEmpty();				
		} else {
			return !player.isInPlay(cardName);
		}
	}
	
	public static Player getValidChosenPlayer(Player player, List<Player> choosable, UserInterface userInterface){
		int chosenPlayer = -1;
		while(chosenPlayer < 0 || chosenPlayer > choosable.size() - 1){
			chosenPlayer = userInterface.askPlayer(player, getPlayersNames(choosable));
		}
		return choosable.get(chosenPlayer);
	}
	
	public static boolean isBeerGiveHealth(List<Player> players){
		if(players.size() > 2){
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isMaxHealth(Player player){
		return player.getHealth() == player.getMaxHealth();
	}
	
	public static Object chooseValidCardToPutBack(Player player, List<Object> cards, UserInterface userInterface){
		int cardIndex = -1;
		while(cardIndex < 0 || cardIndex > cards.size() - 1){
			cardIndex = userInterface.chooseCardToPutBack(player, cards);
		}
		return cards.get(cardIndex);
	}
	
	public static boolean calamityBangOrMiss(Player player, List<Player> players, Player currentPlayer, int required, Deck deck, Discard discard, UserInterface userInterface){
		int misses = player.countMisses();
		int bangs = player.countBangs();
		int playedBangMiss = -3;
		boolean validPlay = false;
		while(!validPlay){
			playedBangMiss = userInterface.respondBangMiss(player, bangs, misses, required);
			if(playedBangMiss == Figure.PLAYBANG){
				if(bangs >= required){
					validPlay = true;
				}
			} else if(playedBangMiss == Figure.PLAYMISSED){
				if(misses >= required){
					validPlay = true;
				}
			} else if(playedBangMiss == Figure.PLAYONEEACH){
				if(misses >= 1 && bangs >= 1 && required == 2){
					validPlay = true;
				}
			} else if(playedBangMiss == Figure.GETSHOT){
				validPlay = true;
			}
		}
		if(playedBangMiss == Figure.PLAYBANG){
			for(int i = 0; i < required; i++){
				discard.add(player.getHand().removeBang());
			}
		} else if(playedBangMiss == Figure.PLAYMISSED){
			for(int i = 0; i < required; i++){
				discard.add(player.getHand().removeMiss());
			}
		} else if(playedBangMiss == Figure.PLAYONEEACH){
			discard.add(player.getHand().removeMiss());
			discard.add(player.getHand().removeBang());
		} else {
			damagePlayer(player, players, currentPlayer, 1, player, deck, discard, userInterface);
			return false;
		}
		return true;
	}
}
