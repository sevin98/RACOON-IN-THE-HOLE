package com.ssafy.a410.game.domain.player.message.request;

public enum GamePlayerRequestType {
    MOVEMENT_SHARE, // 플레이어의 움직임을 공유
    INTERACT_HIDE,  // 플레이어가 오브젝트와 상호작용하여 숨기
    INTERACT_EXPLORE, // 찾는팀 플레이어가 오브젝트 탐색 시도
    INTERACT_EXPLORE_SUCCESS, // 찾는팀 플레이어가 오브젝트를 탐색성공
    INTERACT_EXPLORE_FAIL, // 찾는팀 플레이어가 오브젝트를 탐색실패

}
