# 토스페이먼츠 API 서버

토스페이먼츠 결제 승인을 처리하는 Java API 서버입니다.

## 프로젝트 구조

```
toss-payment-api/
├── src/
│   └── SimplePaymentServer.java    # 메인 서버 파일
├── toss-payment-api.iml           # IntelliJ 모듈 파일
└── .idea/                         # IntelliJ 설정
```

## 기능

- 토스페이먼츠 결제 승인 API 연동
- CORS 설정으로 프론트엔드와 통신
- 실시간 로그 출력
- RESTful API 엔드포인트 제공

## API 엔드포인트

### 결제 승인
```
POST /api/payment/confirm
Content-Type: application/json

{
  "paymentKey": "string",
  "orderId": "string", 
  "amount": number
}
```

### 헬스체크
```
GET /api/payment/health
```

## 실행 방법

### 1. 터미널에서 실행
```bash
javac src/SimplePaymentServer.java -d .
java SimplePaymentServer
```

### 2. IntelliJ에서 실행
1. IntelliJ로 `toss-payment-api` 폴더 열기
2. Run Configuration에서 `TossPaymentServer` 선택 후 실행
3. 또는 `SimplePaymentServer.java` 우클릭 → Run

## 서버 정보
- **포트**: 8080
- **주소**: http://localhost:8080
- **로그**: 실시간 결제 처리 과정 출력

## 프론트엔드 연동
이 API 서버는 `learn-nuxt-3` 프론트엔드와 연동되어 작동합니다.
결제 인증 완료 후 이 서버로 승인 요청이 전송됩니다.