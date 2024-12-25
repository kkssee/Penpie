package com.test.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PortOneService {
    @Value("${IAMPORT_KEY}")
    private String API_KEY;
    @Value("${IAMPORT_SECRET}")
    private String API_SECRET;

    // 포트원 API 토큰 발급
    public String getPortOneToken() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("imp_key", API_KEY);
        body.put("imp_secret", API_SECRET);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity("https://api.iamport.kr/users/getToken", entity, Map.class);

        Map<String, Object> responseBody = response.getBody();
        String token = (String) ((Map) responseBody.get("response")).get("access_token");

        return token;
    }


    // 결제 취소 요청 메서드
    public Map<String, Object> cancelPayment(String token, String merchantUid, int cancelAmount, String reason) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token); // 발급받은 토큰을 헤더에 추가
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 요청 데이터 구성
        Map<String, Object> body = new HashMap<>();
        body.put("merchant_uid", merchantUid); // 취소할 주문 번호
        body.put("cancel_request_amount", cancelAmount); // 취소할 금액
        body.put("reason", reason); // 취소 사유

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        // 포트원 결제 취소 API 호출
        ResponseEntity<Map> response = restTemplate.postForEntity("https://api.iamport.kr/payments/cancel", entity, Map.class);

        return response.getBody(); // 응답 데이터를 반환
    }

    private final RestTemplate restTemplate = new RestTemplate();

    // 인증 요청 ID 생성
    public String generateVerificationId() {
        return "identity-verification-" + UUID.randomUUID().toString();
    }


    public Map<String, Object> checkVerification(String token, String merchantUid) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);   // 발급받은 토큰을 헤더에 추가
        headers.setContentType(MediaType.APPLICATION_JSON);

        String verifyURI = "https://api.iamport.kr/certification/";

        // 요청 데이터 구성
        Map<String, Object> body = new HashMap<>();
        body.put("merchant_uid", merchantUid); // 취소할 주문 번호

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        // 포트원 결제 취소 API 호출
        ResponseEntity<Map> response = restTemplate.postForEntity(verifyURI, entity, Map.class);

        return response.getBody(); // 응답 데이터를 반환
    }
}
