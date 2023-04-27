package com.catas.api.controller;

import com.catas.api.param.Param;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RestApiController {

    @RequestMapping("/test")
    public String testApi() {
        return "From wicked-proxy";
    }

    @RequestMapping("/page")
    public String page() {
        return "Page";
    }

    @RequestMapping("/page/{num}")
    public String pageNum(@PathVariable("num") int num) {
        return "Page-" + num;
    }

    @RequestMapping("/page/{num}/detail")
    public String pageDetail(@PathVariable("num") int num) {
        return "Page-" + num + "-Detail";
    }

    @RequestMapping("/test/params")
    public String param(@RequestParam Param param) {
        return "Param: " + param.toString();
    }
}
