package cn.itcast.core.service;

import cn.itcast.core.dao.user.UserDao;
import cn.itcast.core.pojo.user.User;
import cn.itcast.core.pojo.user.UserQuery;
import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.*;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private Destination smsDestination;
    @Autowired
    private UserDao userDao;

    @Override
    public void sendCode(String phone) {
        String code = RandomStringUtils.randomNumeric(6);
        System.out.println("验证码为" + code);

        //把验证码存入缓存中  手机号为键  对应验证码为值
        redisTemplate.boundValueOps(phone).set(code);
        redisTemplate.boundValueOps(phone).expire(5, TimeUnit.HOURS);

        jmsTemplate.send(smsDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                MapMessage mapMessage = session.createMapMessage();
                mapMessage.setString("PhoneNumbers", phone);
                mapMessage.setString("TemplateParam", "{'number':" + code + "}");
                mapMessage.setString("SignName", "正欣品优购");
                mapMessage.setString("TemplateCode", "SMS_165380473");
                return mapMessage;
            }
        });

    }

    @Override
    public void add(String smscode, User user) {
        String code = (String) redisTemplate.boundValueOps(user.getPhone()).get();
        if (code != null) {
            if (code.equals(smscode)) {
                user.setCreated(new Date());
                user.setUpdated(new Date());
                userDao.insertSelective(user);
            } else {
                throw new RuntimeException("验证码错误");
            }
        } else {
            throw new RuntimeException("验证码失效");
        }
    }

    @Override
    public User findUser(String name) {
        UserQuery userQuery = new UserQuery();
        userQuery.createCriteria().andUsernameEqualTo(name);
        List<User> users = userDao.selectByExample(userQuery);
        return users.get(0);
    }

    @Override
    public void update(String name, User user) {
        UserQuery userQuery = new UserQuery();
        userQuery.createCriteria().andUsernameEqualTo(name);
        userDao.updateByExample(user, userQuery);
    }
}
