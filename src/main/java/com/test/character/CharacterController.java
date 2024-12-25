package com.test.character;

import com.test.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("character")
public class CharacterController {
    @Autowired
    private CharacterService characterService;

    protected CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails;
        }
        return null;
    }

    @PostMapping("/create")
    @ResponseBody
    @Transactional
    public Map<String, Object> characterCreateForm(@ModelAttribute("character") CharacterVO character,
                                      @RequestParam("file") MultipartFile file,
                                      HttpServletRequest request,
                                      Model model) throws IOException {
        Long cno = characterService.createCharacter(character, file, request);
        Map<String, Object> map = new HashMap<>();
        map.put("created", cno > 0);
        map.put("cno", cno);
        return map;
    }

    @GetMapping("/list")
    public String characterList(@RequestParam(value = "sortOrder", defaultValue = "desc") String sortOrder,
                                Model model) {
        CustomUserDetails userDetails = getCurrentUserDetails();
        if (userDetails != null) {
            model.addAttribute("nickname", userDetails.getNickname());
            model.addAttribute("profileImg", userDetails.getProfileImg());
            Long userno = userDetails.getUserno();
            model.addAttribute("userno", userno);
        } else {
            model.addAttribute("nickname", "Guest");
        }

        List<CharacterVO> list = characterService.getList(sortOrder);
        model.addAttribute("list", list);
        for (CharacterVO cc : list)   {
            log.info(cc.toString());
        }
        return "character/characterList";
    }

    @GetMapping("/chat/{cno}")
    public String AIRequest(@PathVariable("cno") Long cno, Model model) {
        CustomUserDetails userDetails = getCurrentUserDetails();
        if (userDetails != null) {
            model.addAttribute("nickname", userDetails.getNickname());
            model.addAttribute("profileImg", userDetails.getProfileImg());
            Long userno = userDetails.getUserno();
            model.addAttribute("userno", userno);
        } else {
            model.addAttribute("nickname", "Guest");
        }

        Long room_id = characterService.getRoomId(cno);
        log.info("룸아이디");
        log.info("이거:{}", room_id);
        List<CharacterChatVO> chatHistory = characterService.getChatHistory(room_id);
        String memory = characterService.getMemory(room_id);
        CharacterVO character = characterService.getCharacter(cno);

        VoiceVO voice = characterService.getVoice(room_id);
        List<VoiceVO> voiceList = characterService.getVoiceList();
        model.addAttribute("voice", voice);
        model.addAttribute("voiceList", voiceList);

        String prompt = String.format(
                "You are role-playing as %s. Description: %s, Backstory: %s, Relationships with user: %s. Respond in the character of %s, reflecting their personality and known background. Your response should be under 120 characters. NEVER reference conversations or events from other chat rooms. Make sure your responses are specific to room %s, and avoid cross-chat references.",
                character.getCharacterName(),
                StringEscapeUtils.escapeHtml4(character.getCharacterDesc()).replaceAll("\\r?\\n", " "),
                StringEscapeUtils.escapeHtml4(character.getCharacterPrompt()).replaceAll("\\r?\\n", " "),
                StringEscapeUtils.escapeHtml4(character.getRelationships()).replaceAll("\\r?\\n", " "),
                character.getCharacterName(),
                room_id.toString()
        );
        log.info("프롬프트생성");
        log.info(prompt);

        model.addAttribute("cno", cno);
        model.addAttribute("room_id", room_id);
        model.addAttribute("userno", getCurrentUserDetails().getUserno());

        if(chatHistory != null) {
            model.addAttribute("chatHistory", chatHistory);
        }
        if(memory != null) {
            model.addAttribute("memory", memory);
        }

        model.addAttribute("character", character);
        model.addAttribute("characterPrompt", prompt);
        return "character/stream";
    }


    @GetMapping("/createVoice")
    public String createVoiceModel(VoiceVO vv, Model model) {
        model.addAttribute("vv", vv);
        return "character/tts";
    }

    @PostMapping("/voice")
    @ResponseBody
    @Transactional
    public Map<String, Object> uploadVoice(@ModelAttribute VoiceVO vv,
                                           @RequestParam("voiceFile") MultipartFile file,
                                           HttpServletRequest request,
                                           Model model) throws IOException {
        Long vno = characterService.uploadVoice(vv, file, request);
        Map<String, Object> map = new HashMap<>();
        map.put("uploaded", vno > 0);
        return map;
    }
}
