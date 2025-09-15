# Payment API Server

토스페이먼츠와 이니시스를 지원하는 Spring Boot 기반 결제 API 서버입니다.

## 📋 프로젝트 개요

완전한 결제 시스템의 백엔드 API 서버로, 다양한 PG사와의 통합 및 복합결제를 지원합니다.

### 주요 특징
- **멀티 PG 지원**: 토스페이먼츠, 이니시스 통합
- **복합결제**: 카드결제 + 적립금결제 동시 지원  
- **전략 패턴**: 확장 가능한 결제수단 아키텍처
- **개별 취소**: 결제 ID 기반 세밀한 취소 관리
- **회원 시스템**: 적립금 관리 및 포인트 이력

## 🏗️ 아키텍처

### 패키지 구조
```
src/main/java/com/example/payment/
├── controller/          # REST API 엔드포인트
│   ├── PaymentController.java
│   ├── MemberController.java  
│   └── InicisController.java
├── service/            # 비즈니스 로직
│   ├── PaymentService.java
│   └── MemberService.java
├── entity/             # JPA 엔티티
│   ├── Payment.java
│   ├── Member.java
│   └── PointHistory.java
├── repository/         # 데이터 액세스
├── dto/               # 데이터 전송 객체
├── strategy/          # 결제 전략 패턴
│   ├── PaymentStrategy.java
│   ├── CardPaymentStrategy.java
│   └── PointsPaymentStrategy.java
├── factory/           # 결제 전략 팩토리
├── client/            # 외부 PG API 클라이언트
├── config/            # 설정 클래스
└── enums/             # 열거형 상수
```

### 핵심 디자인 패턴
- **Strategy Pattern**: 다양한 결제수단 지원 (`PaymentStrategy`)
- **Factory Pattern**: 전략 객체 생성 (`PaymentStrategyFactory`)  
- **Repository Pattern**: Spring Data JPA 기반 데이터 액세스
- **Client Pattern**: PG사별 전용 API 클라이언트

## 🚀 실행 방법

### 개발 서버 실행
```bash
# Maven 기반 실행
mvn spring-boot:run

# 클린 컴파일 후 실행  
mvn clean compile spring-boot:run

# JAR 빌드 후 실행
mvn package
java -jar target/payment-api.jar
```

### 요구사항
- **Java 21** 이상
- **Maven 3.6+**
- **포트 8080** 사용

## 📡 API 엔드포인트

### 결제 APIs
```http
# 결제 승인 (복합결제 지원)
POST /api/payment/confirm
Content-Type: application/json
{
  "paymentKey": "string",
  "orderId": "string", 
  "amount": number,
  "memberId": "string"
}

# 개별 결제 취소 (ID 기반)
POST /api/payment/cancel/{paymentId}

# 회원별 결제내역 조회 (그룹핑)
GET /api/payment/history/{memberId}

# 헬스체크
GET /api/payment/health
```

### 회원 APIs
```http
# 회원가입
POST /api/member/signup
Content-Type: application/json
{
  "memberId": "string",
  "name": "string",
  "email": "string",
  "phone": "string"
}

# 로그인
POST /api/member/login
Content-Type: application/json
{
  "memberId": "string"
}

# 적립금 조회
GET /api/member/{memberId}/points
```

### 이니시스 APIs
```http
# 이니시스 결제 승인
POST /api/inicis/confirm
Content-Type: application/json
{
  "paymentKey": "string",
  "orderId": "string",
  "amount": number
}
```

## 🗄️ 데이터베이스 설계

### 핵심 테이블
- **MEMBERS**: 회원 정보 및 적립금 관리
- **PAYMENTS**: 결제 내역 (APPROVE/CANCEL 구분, PG사별 구분)
- **POINT_HISTORIES**: 적립금 이력 (EARN/USE/REFUND)

### 중요한 설계 포인트
- `PAY_TYPE`: APPROVE/CANCEL로 결제/취소 구분
- `PG_PROVIDER`: 토스페이먼츠/이니시스 구분  
- `order_id` 기반 복합결제 추적
- H2 database 호환성을 위한 VARCHAR 타입 사용

## 🔄 결제 플로우

### 결제 승인 프로세스
1. **결제 요청** → PaymentController 진입
2. **전략 선택** → PaymentStrategyFactory에서 적절한 전략 생성
3. **PG 연동** → 선택된 전략으로 PG사 API 호출
4. **데이터 저장** → Payment(APPROVE), PointHistory 저장
5. **응답 반환** → 클라이언트에 결과 전달

### 결제 취소 프로세스  
1. **취소 요청** → Payment ID 기반 취소 요청
2. **전략별 취소** → 카드: PG API 호출, 적립금: 포인트 환원
3. **취소 기록** → Payment(CANCEL), PointHistory(REFUND) 저장

## 🧪 테스트 환경

- **H2 인메모리 데이터베이스**: 개발용 DB
- **토스페이먼츠 테스트 키**: 실제 결제 없음
- **이니시스 테스트 환경**: 안전한 테스트 가능
- **CORS 허용**: 프론트엔드와 원활한 통신

## 🔗 관련 프로젝트

이 API 서버는 **payment-frontend** (Nuxt 3)와 함께 동작합니다.
- Frontend Repository: `../payment-frontend`
- Frontend URL: `http://localhost:3000`
- API Base URL: `http://localhost:8080/api`

## 📝 개발 가이드

### 새로운 결제수단 추가
1. `PaymentStrategy` 인터페이스 구현
2. `PaymentStrategyFactory`에 전략 등록
3. 필요시 새로운 PG 클라이언트 작성

### 로깅 및 모니터링
- SLF4J 기반 상세 로깅
- 결제 과정 전체 추적 가능
- 에러 상황 자동 롤백 처리