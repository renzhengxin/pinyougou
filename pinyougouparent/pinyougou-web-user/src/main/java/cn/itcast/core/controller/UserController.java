package cn.itcast.core.controller;

import cn.itcast.core.pojo.user.User;
import cn.itcast.core.service.UserService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.PhoneFormatCheckUtils;

@RestController
@RequestMapping("/user")
public class UserController {

    @Reference
    private UserService userService;

    @RequestMapping("/sendCode")
    public Result sendCode(String phone) {
        System.out.println("controller");
        if (PhoneFormatCheckUtils.isPhoneLegal(phone)) {
            userService.sendCode(phone);
            return new Result(true, "发送信息成功");
        } else {

            return new Result(false, "发送信息失败");

        }

    }

    @RequestMapping("/add")
    public Result add(String smscode, @RequestBody User user) {
        try {
            userService.add(smscode, user);
            return new Result(true, "注册成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "注册失败");
        }


    }

    @RequestMapping("/findUser")
    public User findUser() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.findUser(name);
    }

    @RequestMapping("/update")
    public Result update(@RequestBody User user) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            userService.update(name, user);
            return new Result(true, "成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "失败");
        }
    }
}
