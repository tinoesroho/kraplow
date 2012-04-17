package com.chriscarr.bang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ManualUserInterface implements UserInterface, GameStateListener {
	
	Turn turn;
	
	public void setTurn(Turn turn){
		this.turn = turn;
	}
	
	public void printInfo(String info){
		System.out.println(info);
	}
	
	public int askDiscard(Player player) {
		System.out.println(player.getFigure().getName());
		System.out.println("Discard");
		Hand hand = player.getHand();
		int handSize = hand.size();
		for(int i = 0; i < handSize; i++){
			System.out.println(i + ") " + ((Card)hand.get(i)).getName());
		}
		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);
		while (true){
			try {
				String line = in.readLine();
				int cardNumber = Integer.parseInt(line);
				if(cardNumber >= 0 && cardNumber < handSize){
					return cardNumber;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public int askOthersCard(Player player, InPlay inPlay, boolean hasHand) {
		System.out.println(player.getFigure().getName());
		System.out.println("Choose Other Players Card");
		int handSize = inPlay.size();	
		if(hasHand){
			System.out.println("-1) Hand");
		}
		boolean hasGun = inPlay.hasGun();
		if(hasGun){
			System.out.println("-2) Gun");
		}
		for(int i = 0; i < handSize; i++){
			System.out.println(i + ") " + ((Card)inPlay.get(i)).getName());
		}
		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);
		while (true){
			try {
				String line = in.readLine();
				int cardNumber = Integer.parseInt(line);
				if(cardNumber >= -2 && cardNumber < handSize){
					if(cardNumber == -2 && !hasGun){
						continue;
					}
					if(cardNumber == -1 && !hasHand){
						continue;
					}
					return cardNumber;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public int askPlay(Player player) {
		printGameState();
		printPrivateInfo(player);
		System.out.println("Play");
		Hand hand = player.getHand();
		int handSize = hand.size();
		System.out.println("-1) done playing");
		for(int i = 0; i < handSize; i++){
			Card card = ((Card)hand.get(i));
			boolean canPlay = turn.canPlay(player, card);
			System.out.print(i + ") " + card.getName() + " can play? " + canPlay);
			if(canPlay){
				System.out.print(" Targets: ");
				for(String name : turn.targets(player, card)){
					System.out.print(name + " ");
				}
			}
			System.out.println("");
		}
		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);
		while (true){
			try {
				String line = in.readLine();
				int cardNumber = Integer.parseInt(line);
				if(cardNumber >= -1 && cardNumber < handSize){
					return cardNumber;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void printPrivateInfo(Player player) {
		System.out.println(Player.roleToString(player.getRole()));
	}

	@Override
	public int askPlayer(Player player, List<String> otherPlayers) {
		System.out.println(player.getFigure().getName());
		System.out.println("Choose Player");
		int handSize = otherPlayers.size();
		for(int i = 0; i < handSize; i++){
			System.out.println(i + ") " + otherPlayers.get(i));
		}
		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);
		while (true){
			try {
				String line = in.readLine();
				int cardNumber = Integer.parseInt(line);
				if(cardNumber >= 0 && cardNumber < handSize){
					return cardNumber;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean chooseDiscard(Player player) {
		System.out.println(player.getFigure().getName());
		System.out.println("Draw Card From Discard");
		System.out.println("0) From Discard");
		System.out.println("1) From Deck");
		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);
		while (true){
			try {
				String line = in.readLine();
				int cardNumber = Integer.parseInt(line);
				if(cardNumber == 0){
					return true;
				} else if(cardNumber == 1) {
					return false;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public int chooseGeneralStoreCard(Player player, List<Object> cards) {
		System.out.println(player.getFigure().getName());
		System.out.println("Choose General Store Card");
		int handSize = cards.size();
		for(int i = 0; i < handSize; i++){
			System.out.println(i + ") " + ((Card)cards.get(i)).getName());
		}
		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);
		while (true){
			try {
				String line = in.readLine();
				int cardNumber = Integer.parseInt(line);
				if(cardNumber >= 0 && cardNumber < handSize){
					return cardNumber;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public List<Object> chooseTwoDiscardForLife(Player player) {
		System.out.println(player.getFigure().getName());
		System.out.println("Discard Two cards for 1 Life, 4 for 2, etc");
		Hand hand = player.getHand();
		int handSize = hand.size();
		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);
		List<Object> chosenCards = new ArrayList<Object>();
		while (true){
			System.out.println("-1) done choosing");
			for(int i = 0; i < handSize; i++){
				String chosen = " not chosen";
				if(chosenCards.contains(hand.get(i))){
					chosen = " chosen";
				}
				System.out.println(i + ") " + ((Card)hand.get(i)).getName() + chosen);
			}
			try {
				String line = in.readLine();
				int cardNumber = Integer.parseInt(line);
				if(cardNumber >= -1 && cardNumber < handSize){
					if(cardNumber == -1){
						return chosenCards;
					} else {
						Object card = hand.get(cardNumber);
						if(chosenCards.contains(card)){
							chosenCards.remove(card);
						} else {
							chosenCards.add(card);
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean respondBang(Player player, int bangs) {
		System.out.println(player.getFigure().getName());
		System.out.println("Respond with Bang you have " + bangs);
		if(bangs >= 1){
			System.out.println("0) bang");
		}
		System.out.println("1) not bang");
		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);
		while (true){
			try {
				String line = in.readLine();
				int cardNumber = Integer.parseInt(line);
				if(cardNumber == 0 && bangs >= 1){
					return true;
				} else if(cardNumber == 1) {
					return false;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public int respondBangMiss(Player player, int bangs, int misses,
			int missesRequired) {
		System.out.println(player.getFigure().getName());
		System.out.println("Respond with Miss or Bang you have " + bangs + " and " + misses + " misses there are " + missesRequired + " required");
		Hand hand = player.getHand();
		if(hand.countBangs() >= missesRequired){
			System.out.println(Figure.PLAYBANG + ") bang");
		}
		if(hand.countMisses() >= missesRequired){
			System.out.println(Figure.PLAYMISSED + ") miss");
		}
		if(missesRequired == 2 && hand.countBangs() >= 1 && hand.countMisses() >= 1){
			System.out.println(Figure.PLAYONEEACH + ") one of each");
		}
		System.out.println(Figure.GETSHOT + ") not bang or miss");
		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);
		while (true){
			try {
				String line = in.readLine();
				int cardNumber = Integer.parseInt(line);
				if(cardNumber == Figure.PLAYBANG && hand.countBangs() >= missesRequired){
					return Figure.PLAYBANG;
				} else if(cardNumber == Figure.PLAYMISSED && hand.countMisses() >= missesRequired) {
					return Figure.PLAYMISSED;
				} else if(cardNumber == Figure.PLAYONEEACH && missesRequired == 2 && hand.countBangs() >= 1 && hand.countMisses() >= 1) {
					return Figure.PLAYONEEACH;
				} else if(cardNumber == Figure.GETSHOT) {
					return Figure.GETSHOT;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean respondBeer(Player player, int beers) {
		System.out.println(player.getFigure().getName());
		System.out.println("Respond with Beer you have " + beers);
		if(beers >= 1){
			System.out.println("0) beer");
		}
		System.out.println("1) not beer");
		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);
		while (true){
			try {
				String line = in.readLine();
				int cardNumber = Integer.parseInt(line);
				if(cardNumber == 0 && beers >= 1){
					return true;
				} else if(cardNumber == 1) {
					return false;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean respondMiss(Player player, int misses, int missesRequired) {
		System.out.println(player.getFigure().getName());
		System.out.println("Respond with Miss you have " + misses + " there are " + missesRequired + " required");
		if(misses >= missesRequired){
			System.out.println("0) miss");
		}
		System.out.println("1) not miss");
		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);
		while (true){
			try {
				String line = in.readLine();
				int cardNumber = Integer.parseInt(line);
				if(cardNumber == 0 && misses >= missesRequired){
					return true;
				} else if(cardNumber == 1) {
					return false;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean chooseFromPlayer(Player player) {
		System.out.println(player.getFigure().getName());
		System.out.println("Draw Card From Player");
		System.out.println("0) From Player");
		System.out.println("1) From Deck");
		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);
		while (true){
			try {
				String line = in.readLine();
				int cardNumber = Integer.parseInt(line);
				if(cardNumber == 0){
					return true;
				} else if(cardNumber == 1) {
					return false;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public int chooseDrawCard(Player player, List<Object> cards) {
		System.out.println(player.getFigure().getName());
		System.out.println("Choose Draw Card to keep");
		int handSize = cards.size();
		for(int i = 0; i < handSize; i++){
			System.out.println(i + ") " + ((Card)cards.get(i)).getName());
		}
		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);
		while (true){
			try {
				String line = in.readLine();
				int cardNumber = Integer.parseInt(line);
				if(cardNumber >= 0 && cardNumber < handSize){
					return cardNumber;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public int chooseCardToPutBack(Player player, List<Object> cards) {
		System.out.println(player.getFigure().getName());
		System.out.println("Choose card put back");
		int handSize = cards.size();
		for(int i = 0; i < handSize; i++){
			System.out.println(i + ") " + ((Card)cards.get(i)).getName());
		}
		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);
		while (true){
			try {
				String line = in.readLine();
				int cardNumber = Integer.parseInt(line);
				if(cardNumber >= 0 && cardNumber < handSize){
					return cardNumber;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void printGameState(){
		GameState gameState = turn.getGameState();
		System.out.println("Current Turn: " + gameState.getCurrentName());
		System.out.println("Is game over: " + gameState.isGameOver());		
		System.out.println("Deck size: " + gameState.getDeckSize());
		System.out.println("Discard top card: " + gameState.discardTopCard());
		List<GameStatePlayer> players = gameState.getPlayers();
		for(GameStatePlayer player : players){
			System.out.println("Name: " + player.name);
			System.out.println("Is Sheriff: " + player.isSheriff);
			System.out.println("Ability: " + player.specialAbility);
			System.out.println("Health: " + player.health);
			System.out.println("Max: " + player.maxHealth);
			System.out.println("Hand: " + player.handSize);
			GameStateCard gun = player.gun;
			if(gun != null){
				System.out.println("Discard top card: " + gun.name);
				System.out.println("Discard top card: " + gun.suit);
				System.out.println("Discard top card: " + gun.description);
				System.out.println("Discard top card: " + gun.type);
				System.out.println("Discard top card: " + gun.value);
			}
			List<GameStateCard> cards = player.inPlay;
			for(GameStateCard card : cards){
				System.out.println("name: " + card.name);
				System.out.println("suit: " + card.suit);
				System.out.println("desc: " + card.description);
				System.out.println("type: " + card.type);
				System.out.println("value: " + card.value);
			}
		}
	}

}
