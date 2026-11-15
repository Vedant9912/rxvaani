package com.rxvaani.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home() { return "forward:/index.html"; }

    @GetMapping("/consultation")
    public String consultation() { return "forward:/consultation.html"; }

    @GetMapping("/studio")
    public String studio() { return "forward:/studio.html"; }

    @GetMapping("/prescription")
    public String prescription() { return "forward:/prescription.html"; }

    @GetMapping("/records")
    public String records() { return "forward:/records.html"; }

    @GetMapping("/settings")
    public String settings() { return "forward:/settings.html"; }

    @GetMapping({"/video", "/demo"})
    public String videoDemo() { return "forward:/video_demo.html"; }

    @GetMapping({"/animate", "/generate-video"})
    public String animatedVideo() { return "forward:/animated_video.html"; }
}
