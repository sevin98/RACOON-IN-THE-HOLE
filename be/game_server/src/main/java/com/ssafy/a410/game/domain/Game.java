package com.ssafy.a410.game.domain;

import com.ssafy.a410.game.domain.message.GamePlayerControlMessage;
import com.ssafy.a410.game.domain.message.GamePlayerControlType;
import com.ssafy.a410.game.domain.message.GamePlayerRequest;
import com.ssafy.a410.socket.domain.Subscribable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

@Getter
@Slf4j
public class Game implements Runnable, Subscribable {
    private static final int MAX_ROUND = 3;

    // 플레이어들이 속해 있는 방
    private final Room room;
    // 숨는 팀
    private final Team hidingTeam;
    private final Queue<GamePlayerRequest> hidingTeamRequests;
    // 찾는 팀
    private final Team seekingTeam;
    private final Queue<GamePlayerRequest> seekingTeamRequests;
    private final SimpMessagingTemplate messagingTemplate;
    // 현재 게임이 머물러 있는 상태(단계)
    private Phase currentPhase;

    public Game(Room room, SimpMessagingTemplate messagingTemplate) {
        this.room = room;
        this.hidingTeam = new Team(Team.Character.RACOON, this);
        this.hidingTeamRequests = new ConcurrentLinkedDeque<>();
        this.seekingTeam = new Team(Team.Character.FOX, this);
        this.seekingTeamRequests = new ConcurrentLinkedDeque<>();
        this.messagingTemplate = messagingTemplate;
        initialize();
    }

    private void initialize() {
        // 초기화 시작 (게임 진입 불가)
        this.currentPhase = Phase.INITIALIZING;

        // 랜덤으로 플레이어 편 나누기
        randomAssignPlayersToTeam();
        // 방에 실행 중인 게임으로 연결
        room.setPlayingGame(this);

        // 게임 시작 준비 완료
        this.currentPhase = Phase.INITIALIZED;
    }

    private void randomAssignPlayersToTeam() {
        // 모든 멤버를 섞고
        List<Player> allPlayers = new ArrayList<>(room.getPlayers().values());
        Collections.shuffle(allPlayers);

        // 멤버를 반씩, 최대 1명 차이가 나도록 나누어 숨는 팀과 찾는 팀으로 나누기
        for (int i = 0; i < allPlayers.size(); i++) {
            Player player = allPlayers.get(i);
            if (i % 2 == 0) {
                hidingTeam.addPlayer(player);
            } else {
                seekingTeam.addPlayer(player);
            }
        }
    }

    public boolean canJoin() {
        return this.currentPhase != null && this.currentPhase.isNowOrBefore(Phase.INITIALIZING);
    }

    public boolean isGameRunning() {
        return this.currentPhase.isNowOrAfter(Phase.INITIALIZED);
    }

    @Override
    public void run() {
        log.info("Game start!");
        for (int round = 1; round <= MAX_ROUND && !isGameFinished(); round++) {
            log.info("Round {} of room {} ==================================", round, room.getRoomNumber());
            log.info("READY Phase start ------------------------------------");
            runReadyPhase();
            log.info("MAIN Phase start -------------------------------------");
            runMainPhase();
            log.info("END Phase start --------------------------------------");
            runEndPhase();
        }
    }

    private boolean isTimeToSwitch(long timeToSwitchPhase) {
        return System.currentTimeMillis() >= timeToSwitchPhase;
    }

    // 게임의 승패가 결정되었는지 확인
    private boolean isGameFinished() {
        return false;
    }

    private void runReadyPhase() {
        // 상태 전환
        this.currentPhase = Phase.READY;

        // 숨는 팀만 움직일 수 있으며, 화면 가리기 해제 설정
        messagingTemplate.convertAndSend(hidingTeam.getTopic(), new GamePlayerControlMessage(GamePlayerControlType.UNCOVER_SCREEN, null));
        messagingTemplate.convertAndSend(hidingTeam.getTopic(), new GamePlayerControlMessage(GamePlayerControlType.UNFREEZE, null));
        hidingTeam.unfreezePlayers();

        // 찾는 팀은 움직일 수 없으며, 화면 가리기 설정
        messagingTemplate.convertAndSend(seekingTeam.getTopic(), new GamePlayerControlMessage(GamePlayerControlType.FREEZE, null));
        messagingTemplate.convertAndSend(seekingTeam.getTopic(), new GamePlayerControlMessage(GamePlayerControlType.COVER_SCREEN, null));
        seekingTeam.freezePlayers();

        // 요청 처리 큐 초기화
        hidingTeamRequests.clear();

        // 제한 시간이 끝날 때까지 루프 반복
        final long TIME_TO_SWITCH = System.currentTimeMillis() + Phase.READY.getDuration();
        while (!isTimeToSwitch(TIME_TO_SWITCH) && !isGameFinished()) {
            // 현 시점까지 들어와 있는 요청까지만 처리
            final int NUM_OF_MESSAGES = hidingTeamRequests.size();
            for (int cnt = 0; cnt < NUM_OF_MESSAGES; cnt++) {
                GamePlayerRequest request = hidingTeamRequests.poll();
            }
        }
    }

    private void runMainPhase() {
        this.currentPhase = Phase.MAIN;

        // 숨는 팀은 움직일 수 없으며, 화면 가리기 해제 설정
        messagingTemplate.convertAndSend(hidingTeam.getTopic(), new GamePlayerControlMessage(GamePlayerControlType.FREEZE, null));
        messagingTemplate.convertAndSend(hidingTeam.getTopic(), new GamePlayerControlMessage(GamePlayerControlType.UNCOVER_SCREEN, null));
        hidingTeam.freezePlayers();

        // 찾는 팀은 움직일 수 있으며, 화면 가리기 해제 설정
        messagingTemplate.convertAndSend(seekingTeam.getTopic(), new GamePlayerControlMessage(GamePlayerControlType.UNFREEZE, null));
        messagingTemplate.convertAndSend(seekingTeam.getTopic(), new GamePlayerControlMessage(GamePlayerControlType.UNCOVER_SCREEN, null));
        seekingTeam.unfreezePlayers();

        // 요청 처리 큐 초기화
        seekingTeamRequests.clear();

        // 제한 시간이 끝날 때까지 루프 반복
        final long TIME_TO_SWITCH = System.currentTimeMillis() + Phase.MAIN.getDuration();
        while (!isTimeToSwitch(TIME_TO_SWITCH) && !isGameFinished()) {
            // 현 시점까지 들어와 있는 요청까지만 처리
            final int NUM_OF_MESSAGES = seekingTeamRequests.size();
            for (int cnt = 0; cnt < NUM_OF_MESSAGES; cnt++) {
                GamePlayerRequest request = seekingTeamRequests.poll();
            }
        }
    }

    private void runEndPhase() {
    }

    @Override
    public String getTopic() {
        return "/topic/rooms/" + room.getRoomNumber() + "/game";
    }
}
