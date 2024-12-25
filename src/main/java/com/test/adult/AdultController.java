package com.test.adult;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.Certification;
import com.siot.IamportRestClient.response.IamportResponse;
import com.test.payment.PortOneService;
import com.test.users.UserDAO;
import com.test.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/adult")
public class AdultController {
    @Autowired
    private PortOneService portOneService;

    @Autowired
    private UserDAO userDAO;

    private final IamportClient iamportClient;
    @Autowired
    private UserService userService;

    @Autowired
    public AdultController(@Value("${REST_API_KEY}") String restApiKey,
                             @Value("${REST_API_SECRET}") String restApiSecret) {
        this.iamportClient = new IamportClient(restApiKey, restApiSecret);
    }

    // 본인인증 페이지를 열기 위한 GET 요청
    @GetMapping("/form")
    public String showIdentityVerificationPage(Model model) {
        String identityVerificationId = portOneService.generateVerificationId();
        model.addAttribute("identityVerificationId", identityVerificationId);
        return "/adult";
    }

    @ResponseBody
    @RequestMapping("/verify/{imp_uid}")
    public Map<String, Object> certificationByImpUid(@PathVariable("imp_uid") String imp_uid)
            throws IamportResponseException, IOException {
        IamportResponse<Certification> response = iamportClient.certificationByImpUid(imp_uid);
        // 응답에서 출생일(birth)을 가져옴
        Certification certification = response.getResponse();
        Date birthDate = certification.getBirth();
        java.sql.Date sDate = new java.sql.Date(birthDate.getTime());

        // Date를 LocalDate로 변환
        LocalDate birthLocalDate = birthDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        // 현재 날짜
        LocalDate currentDate = LocalDate.now();
        // 나이 계산
        int age = Period.between(birthLocalDate, currentDate).getYears();
        // 성인 여부 판단 (만 19세 이상인지 확인)
        boolean isAdult = age >= 19;

        // get gender and name
        String gender = certification.getGender();  // "male" or "female"
        String username = certification.getName();

        boolean saved = userService.insertUserVerification(isAdult, sDate, gender, username);

        // 결과를 JSON 형태로 반환
        Map<String, Object> result = new HashMap<>();
        result.put("saved", saved);
        result.put("isAdult", isAdult);
        result.put("age", age);

        return result;
    }
}
