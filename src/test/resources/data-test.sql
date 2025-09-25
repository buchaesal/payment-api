-- 테스트용 회원 데이터
INSERT INTO members (member_id, name, email, phone, points, created_at, updated_at) VALUES
('testuser1', '홍길동', 'test1@test.com', '01012345678', 5000, NOW(), NOW()),
('testuser2', '김철수', 'test2@test.com', '01098765432', 3000, NOW(), NOW()),
('testuser3', '이영희', 'test3@test.com', '01055667788', 10000, NOW(), NOW());

-- 테스트용 적립금 내역 데이터
INSERT INTO point_histories (member_id, point_type, point_amount, order_id, created_at, updated_at) VALUES
(1, 'EARN', 1000, NULL, NOW(), NOW()), -- 회원가입 적립금
(1, 'USE', 500, 'ORDER_001', NOW(), NOW()), -- 적립금 사용
(1, 'REFUND', 200, 'ORDER_001', NOW(), NOW()), -- 적립금 환불
(2, 'EARN', 1000, NULL, NOW(), NOW()), -- 회원가입 적립금
(2, 'EARN', 2000, 'ORDER_002', NOW(), NOW()), -- 구매 적립
(3, 'EARN', 1000, NULL, NOW(), NOW()), -- 회원가입 적립금
(3, 'EARN', 9000, 'ORDER_003', NOW(), NOW()); -- 구매 적립

-- 테스트용 결제 데이터 (member_id는 Members 테이블의 id를 참조)
INSERT INTO payments (order_id, member_id, payment_method, pay_type, pg_provider, payment_amount, product_name, tid, payment_at, created_at, updated_at) VALUES
('ORDER_001', 1, 'CARD', 'APPROVE', 'toss', 15000, '테스트 상품 1', 'TOSS_TID_001', NOW(), NOW(), NOW()),
('ORDER_001', 1, 'POINTS', 'APPROVE', NULL, 500, '테스트 상품 1', NULL, NOW(), NOW(), NOW()),
('ORDER_002', 2, 'CARD', 'APPROVE', 'inicis', 20000, '테스트 상품 2', 'INICIS_TID_001', NOW(), NOW(), NOW()),
('ORDER_003', 3, 'CARD', 'APPROVE', 'toss', 50000, '테스트 상품 3', 'TOSS_TID_002', NOW(), NOW(), NOW()),
('ORDER_004', 1, 'CARD', 'APPROVE', 'toss', 10000, '테스트 상품 4', 'TOSS_TID_003', NOW(), NOW(), NOW()),
('ORDER_004', 1, 'CARD', 'CANCEL', 'toss', 10000, '테스트 상품 4', NULL, NOW(), NOW(), NOW()); -- 취소 기록