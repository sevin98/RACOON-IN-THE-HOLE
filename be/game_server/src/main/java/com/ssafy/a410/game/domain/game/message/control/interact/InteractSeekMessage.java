package com.ssafy.a410.game.domain.game.message.control.interact;

import lombok.Getter;

@Getter
public class InteractSeekMessage extends InteractControlMessage {

    private final InteractSeekInfo data;

    public InteractSeekMessage(InteractType type, String roomId, String playerId, String objectId, String foundPlayerId, String requestId) {
        super(type, null, requestId);
        this.data = new InteractSeekInfo(roomId, playerId, objectId, foundPlayerId);
    }

    // 찾기 성공 했을때 등장할 메시지
    public static InteractSeekMessage successMessage(String roomId, String playerId, String objectId, String foundPlayerId, String requestId) {
        return new InteractSeekMessage(InteractType.INTERACT_SEEK_SUCCESS, roomId, playerId, objectId, foundPlayerId, requestId);
    }

    // 찾기 실패 했을때 등장할 메시지
    public static InteractSeekMessage failureMessage(String roomId, String playerId, String objectId, String requestId) {
        return new InteractSeekMessage(InteractType.INTERACT_SEEK_FAIL, playerId, roomId, objectId, "NONE", requestId);
    }
}
