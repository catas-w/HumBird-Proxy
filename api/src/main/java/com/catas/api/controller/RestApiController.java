package com.catas.api.controller;

import com.catas.api.param.Param;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("")
public class RestApiController {

    @RequestMapping("")
    public String host() {
        return "Time" + System.currentTimeMillis();
    }

    @RequestMapping("/api/test")
    public String testApi() throws InterruptedException {
        Thread.sleep(5000);
        return "From wicked-proxy";
    }

    @RequestMapping("/api/page")
    public String page() {
        return "Page";
    }

    @RequestMapping("/api/page/{num}")
    public String pageNum(@PathVariable("num") int num) {
        return "Page-" + num;
    }

    @RequestMapping("/api/page/{num}/detail")
    public String pageDetail(@PathVariable("num") int num) {
        return "Page-" + num + "-Detail";
    }

    @RequestMapping("/api/test/params")
    public String param(@RequestParam Param param) {
        return "Param: " + param.toString();
    }

    @PostMapping("/api/file-upload")
    public String testFile(MultipartFile file) {
        String res = file == null ? "empty file" : String.valueOf(file.getSize());
        return "File size: " + res;
    }
}
