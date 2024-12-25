package com.test.payment;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import com.test.users.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/pp")
public class PaymentController {
    @Autowired
    PortOneService portOneService;

    @Autowired
    UserService userService;

    private final IamportClient iamportClient;


    @Autowired
    public PaymentController(@Value("${REST_API_KEY}") String restApiKey,
                             @Value("${REST_API_SECRET}") String restApiSecret) {
        this.iamportClient = new IamportClient(restApiKey, restApiSecret);
    }

    @ResponseBody
    @RequestMapping("/verify/{imp_uid}")
    public IamportResponse<Payment> paymentByImpUid(@PathVariable("imp_uid") String imp_uid)
            throws IamportResponseException, IOException {
        // 결제 검증
        IamportResponse<Payment> response = iamportClient.paymentByImpUid(imp_uid);

        // 결제 검증이 성공한 경우
        if (response.getResponse() != null) {
            Payment payment = response.getResponse();
            // 결제 금액을 로그로 찍기
            log.info("Payment Amount: {}", payment.getAmount());
            userService.savePoint(payment.getAmount().intValue());
        } else {
            log.warn("No payment found for imp_uid: {}", imp_uid);
        }

        return response;

        /*
        imp_uid (아임포트 결제 고유 ID) ,merchant_uid (가맹점 주문 고유 ID)
        imp_uid: 아임포트가 결제 시 자동으로 생성하는 결제 고유 ID. 아임포트의 결제 데이터를 조회할 때 사용.
        merchant_uid: 가맹점이 주문을 구분하기 위해 직접 생성하는 주문 고유 ID. 가맹점 내부적으로 주문 관리에 사용.
        */
    }


    @PostMapping("/cancel")
    public ResponseEntity<Map<String, Object>> cancelPayment(@RequestBody Map<String, Object> cancelRequest) {
        // 클라이언트에서 넘어온 데이터
        String merchantUid = (String) cancelRequest.get("merchant_uid");
        int cancelAmount = (int) cancelRequest.get("cancel_request_amount");
        String reason = (String) cancelRequest.get("reason");

        // 포트원 토큰 발급
        String token = portOneService.getPortOneToken();

        // 결제 취소 요청
        Map<String, Object> cancelResponse = portOneService.cancelPayment(token, merchantUid, cancelAmount, reason);

        // 응답 결과 반환
        return ResponseEntity.ok(cancelResponse);

    }

    @GetMapping("/payment")
    public String payment(Model model) {
        model.addAttribute("point", userService.getPoint());
        return "/pay";
    }



}