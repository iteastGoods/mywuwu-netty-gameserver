/*
 * @author   作者: qugang
 * @E-mail   邮箱: qgass@163.com
 * @date     创建时间：2018/11/12
 * 类说明     基于WebFlex 登陆模块
 */
package com.mywuwu.gameserver.webserver;

import com.mywuwu.gameserver.core.security.JwtTokenUtil;
import com.mywuwu.gameserver.core.websocket.GameWebSocketSession;
import com.mywuwu.gameserver.data.monoModel.UserModel;
import com.mywuwu.gameserver.data.monoRepository.UserRepository;
import com.mywuwu.gameserver.mapper.entity.Test;
import com.mywuwu.gameserver.mapper.service.TestService;
import com.mywuwu.gameserver.webserver.config.GameConfig;
import com.mywuwu.gameserver.webserver.protocol.GuestResponse;
import com.mywuwu.gameserver.webserver.protocol.LoginRequest;
import com.mywuwu.gameserver.webserver.protocol.LoginResponse;
import com.mywuwu.gameserver.webserver.protocol.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
public class LoginController {

    private final UserRepository userRepository;

    private final GameConfig config;

    protected final ValueOperations<String, GameWebSocketSession> valueOperationsByGameWebSocketSession;

    private final RedisTemplate redisTemplate;

    private final JwtTokenUtil jwtTokenUtil;

    private final TestService testService;


    @Autowired
    public LoginController(UserRepository userRepository,
                           GameConfig config,
                           RedisTemplate redisTemplate,
                           JwtTokenUtil jwtTokenUtil, TestService testService) {
        this.userRepository = userRepository;
        this.config = config;
        this.redisTemplate = redisTemplate;
        this.jwtTokenUtil = jwtTokenUtil;
        this.valueOperationsByGameWebSocketSession = this.redisTemplate.opsForValue();
        this.testService = testService;
    }


    @PostMapping("api/login")
    public Optional<LoginResponse> login(@RequestBody LoginRequest loginForm) {
        UserModel userModel = userRepository.findByNameAndPassword(loginForm.getName(), loginForm.getPassword());
        if (userModel != null) {
            String token = jwtTokenUtil.generateToken(userModel.getName());
            LoginResponse loginResponse = new LoginResponse(userModel.getName(),
                    userModel.getNickName(),
                    token,
                    0,
                    0,
                    config.getServers()
            );

            return Optional.of(loginResponse);
        }
        return Optional.empty();
    }

    @GetMapping("api/guest")
    public GuestResponse guest(String deviceId) {
        List<Test> list = testService.getTest();
        System.out.println(list.size());
        UserModel userModel = userRepository.findByNameAndUserType(deviceId, 1);
        if (userModel == null) {
            userModel = new UserModel();
            userModel.setName(deviceId);
            userModel.setNickName("访客");
            userModel.setBalance(0);
            userModel.setCardNumber(3);
            userModel.setMobileNumber("");
            userModel.setPassword("");
            userModel.setSex("0");
            userModel.setSponsor("");
            userModel.setUserType(1);
            userRepository.save(userModel);
        }


        String token = jwtTokenUtil.generateToken(String.valueOf(userModel.getId()));
        return new GuestResponse(userModel.getId()
                , userModel.getName(),
                userModel.getNickName(),
                token,
                0,
                3,
                config.getServers()
        );
    }

    @PostMapping("api/register")
    public UserModel register(@RequestBody RegisterRequest registerForm) {
        //todo 验证码
        UserModel userModel = new UserModel();

        userModel.setName(registerForm.getName());
        userModel.setNickName(registerForm.getNickName());
        userModel.setBalance(0);
        userModel.setCardNumber(3);
        userModel.setMobileNumber(registerForm.getMobileNumber());
        userModel.setPassword(registerForm.getPassword());
        userModel.setSex("0");
        userModel.setSponsor("");
        userModel.setUserType(0);
        userRepository.save(userModel);
        return userModel;
    }
}
