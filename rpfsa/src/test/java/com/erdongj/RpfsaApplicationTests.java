package com.erdongj;

import com.erdongj.mapper.AdoptMapper;
import com.erdongj.pojo.Adopt;
import com.erdongj.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;

@SpringBootTest
class RpfsaApplicationTests {

    @Autowired
    private AdoptMapper adoptMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private AdoptService adoptService;

    @Autowired
    private PetService petService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private PostService postService;

    @Autowired
    private DonateService donateService;

    @Test
    void contextLoads() {
        userService.querymyadopt(100).forEach(System.out::println);
    }

    @Test
    void te() {
//        System.out.println(messageService.queryallnoread(101,"admin"));
        messageService.allhaveread(101,"admin");

    }

    @Test
    void shanchu() {
        System.out.println(userService.userlogin("test", "1233"));
    }

}
