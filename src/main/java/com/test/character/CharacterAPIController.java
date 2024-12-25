package com.test.character;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@RestController
@CrossOrigin
@Slf4j
public class CharacterAPIController {
    @Autowired
    private CharacterService characterService;

    @Value("${spring.ai.openai.api-key}")
    private String OPENAI_API_KEY;

    private final ChatClient chatClient;
    public final OpenAiAudioApi openAiAudioApi = new OpenAiAudioApi(OPENAI_API_KEY);
    public final OpenAiAudioSpeechModel openAiAudioSpeechModel = new OpenAiAudioSpeechModel(openAiAudioApi);

    public CharacterAPIController(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
                .build();
    }

    @PostMapping("/stream")
    public Flux<String> chatWithStream(@RequestBody Map<String, String> payload) {
        Long cno = Long.parseLong(payload.get("cno"));
        Long room_id = Long.parseLong(payload.get("room_id"));
        String message = payload.get("message");
        String characterPrompt = payload.get("characterPrompt");

        CharacterChatVO cv = new CharacterChatVO();

        final StringBuilder fullResponse = new StringBuilder();

        cv.setRoom_id(room_id);
        cv.setSender(0L);
        cv.setMessage(message);

        int saved = characterService.saveChat(cv);

        return chatClient.prompt()
                .system(characterPrompt)
                .user(message)
                .stream()
                .content()
                .doOnNext(fullResponse::append)
                .doFinally(signal -> {
                    cv.setSender(cno);
                    cv.setMessage(fullResponse.toString());
                    characterService.saveChat(cv);
                });
    }

    @PostMapping("/upload-audio")
    public String uploadAudio(@RequestParam("audio") MultipartFile file) throws IOException, InterruptedException {
        // 서버에 파일 저장
        String uploadDir = "uploads/";
        File uploadFolder = new File(uploadDir);

        File uploadedFile = new File(uploadDir + file.getOriginalFilename());
        file.transferTo(uploadedFile);

        // FFmpeg로 파일 자르기
        String trimmedFile = uploadDir + "trimmed-" + System.currentTimeMillis() + ".mp3";
        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg",
                "-i", uploadedFile.getAbsolutePath(),
                "-t", "15", // 15초로 제한
                trimmedFile
        );
        processBuilder.start().waitFor();

        return "/uploads/" + new File(trimmedFile).getName();
    }
}
