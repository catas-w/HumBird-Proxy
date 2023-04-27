package com.catas.api.param;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class Param {

    String name;

    Integer age;

    MultipartFile file;

}
