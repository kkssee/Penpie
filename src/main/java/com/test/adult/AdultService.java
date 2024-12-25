package com.test.adult;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class AdultService {
    @Value("${REST_API_KEY}")
    private static String API_KEY;
    @Value("${REST_API_SECRET}")
    private static String API_SECRET;

    private final String IAMPORT_SECRET_KEY = "{IAMPORT_SECRET_KEY}";

    public boolean checkCertification(String impUid) {
        String IAMPORT_API_URL = "https://api.iamport.kr/payments/";
        String url = IAMPORT_API_URL + impUid;
        RestTemplate restTemplate = new RestTemplate();

        // Iamport API 호출
        try {
            String response = restTemplate.getForObject(url, String.class);
            // TODO: response에서 인증 결과를 확인하여 성인 여부 판단
            // 실제 Iamport API의 응답 형식에 따라 처리해야 함
            // 예시로 true 반환
            return true;
        } catch (Exception e) {
            // API 호출 실패 시 예외 처리
            log.error(String.valueOf(e));
            return false;
        }
    }
}
