package com.ssafy.a410.game.domain;

import com.ssafy.a410.common.exception.handler.GameException;
import com.ssafy.a410.socket.domain.Subscribable;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class Room implements Subscribable {
    // 한 방에 참가할 수 있는 최대 플레이어 수
    private static final int NUM_OF_MAX_PLAYERS = 8;

    // 방 참여 코드 (고유값)
    private final String roomNumber;
    // 방 비밀 번호
    private final String password;

    private final Map<String, Player> players;
    @Setter
    public Game playingGame;

    public Room(String roomNumber, String password) {
        this.roomNumber = roomNumber;
        this.password = password;
        players = new ConcurrentHashMap<>();
    }

    public void addPlayer(Player player) {
        if (!canJoin(player)) {
            throw new GameException("Player cannot join to room");
        } else {
            players.put(player.getId(), player);
        }
    }

    public void removePlayer(Player player) {
        if (!has(player)) {
            throw new GameException("Player is not in room");
        } else {
            players.remove(player.getId());
        }
    }

    protected boolean isFull() {
        return players.size() >= NUM_OF_MAX_PLAYERS;
    }

    public boolean has(Player player) {
        return players.containsKey(player.getId());
    }

    public boolean canJoin(Player player) {
        // 방에 사람이 더 들어올 수 있고, 게임이 준비되지 않아야 함
        return !isFull() && !isGameInitialized() && !this.has(player);
    }

    public boolean isGameInitialized() {
        return playingGame != null;
    }

    public boolean isReadyToStartGame() {
        // 참가한 인원의 과반수 이상이 레디 상태여야 함
        long readyCount = players.values().stream().filter(Player::isReadyToStart).count();
        return readyCount > players.size() / 2;
    }

    public boolean isGameRunning() {
        return playingGame != null && playingGame.isGameRunning();
    }

    public boolean isPublic() {
        return password == null || password.isEmpty();
    }

    public boolean hasAuthority(Player player, String password) {
        return isPublic() || this.password.equals(password) || has(player);
    }

    public boolean hasPlayingGame() {
        return playingGame != null;
    }

    public boolean canJoin(Player player, String password) {
        boolean isFull = isFull();
        boolean isAuthenticated = hasAuthority(player, password);
        boolean isGameRunning = isGameRunning();
        return !isFull && isAuthenticated && hasPlayingGame() && isGameRunning;
    }

    @Override
    public String getTopic() {
        return "/topic/rooms/" + roomNumber;

    }
}
