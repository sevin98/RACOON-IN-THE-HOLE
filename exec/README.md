# 실행 환경 정보

- **JVM** : Openjdk 17.0.11
- **WAS** : Spring Boot 내장 Tomcat
- **IDE** : IntelliJ IDEA, Visual Studio Code
- **환경 변수** : --profile=[dev, staging, prod]
- **DB** : dev의 경우 H2, staging과 prod의 경우 MySQL (Spring Data JPA의 자동 DDL 생성 기능을 사용하여 테이블 생성)

# 시연 시나리오

## 타이틀 화면

- **LOGIN** : ID, PW를 입력하여 인증된 회원으로써 게임 플레이
- **JOIN** : 새 회원 정보를 등록
- **GUEST** : 임의로 주어지는 닉네임으로 세션이 만료될 때까지 게임 플레이
- **좌측 상단 볼륨 버튼** : 배경 음악 On/Off
- **우측 상단 가이드 버튼** : 간략한 게임 플레이 가이드 열람

## 로비

- **새로운 방으로 시작하기** : 공개, 또는 비공개 방을 생성하여 게임 시작 준비
- **초대 받은 방으로 이동** : 사전에 공유된 방 코드와 비밀번호를 사용하여 게임 시작 준비
- **즉시 게임 시작하기** : 이미 생성되어 있는 방 중 참가할 수 있는 임의의 방에서 게임 시작 준비
- **랭킹 보기** : 플레이어들의 누적된 전적을 확인

## 대기실

- **준비하기** : 게임에 참가할 준비 신호로, 과반수가 준비하면 게임 시작
- **참가 링크 복사** : URL을 공유하여 다른 플레이어들을 초대

## 인게임 화면

- **방향키** : 탑뷰 맵에서 자신의 플레이어를 이동
- **스페이스 바** : 상호 작용 가능한 구조물에 대해 숨기, 또는 찾기 동작 수행
- **Q, W** : 게임 시작 시에 랜덤하게 주어지는 아이템 사용

## 랭킹

- 승수, 잡은 수, 생존 시간 순으로 정렬하여 전적 누계 확인

# 게임 진행

1. 게임은 2~8인의 플레이어로 구성된 방에서 진행되며, 랜덤하게 최대 1명의 인원만큼 차이나도록 팀이 나뉜다.
2. 나뉜 양 팀은 찾는 역할과 숨는 역할을 번갈아가며 수행한다.
3. READY Phase : 먼저 주어진 시간 동안 찾는 역할 팀이 구조물에 숨거나 방해 아이템을 설치하며, 이때 숨지 못한 경우 자동 탈락된다.
4. Main Phase : 찾는 역할 팀 플레이어들이 구조물을 탐색하며 숨는 역할 팀 플레이어들을 찾아야 한다.
5. Main Phase에 숨는 팀 플레이어들이 모두 잡히면 찾는 팀이 승리하며, 그렇지 않으면 다음 라운드로 넘어간다.
6. 게임은 라운드 단위로 진행되며, 라운드가 끝날 때마다 역할이 교체된다.
7. 게임의 맵은 매 라운드가 끝날 때마다 좁아지며, 좁아진 영역에 라운드가 끝날 때까지 속해 있는 플레이어는 자동 탈락된다.
8. Main Phase가 시작될 때 숨어 있는 플레이어들의 방향을 가리키는 화살표가 각 플레이어의 주변에 표시된다.
9. 고추와 킁킁 버섯을 사용하여 본인에게 유리한 상황을 만들거나, 구조물에 방해 아이템을 설치할 수 있다.

---

_DB 덤프 파일은 실행에 필요하지 않습니다._
