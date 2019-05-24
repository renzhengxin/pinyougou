package cn.itcast.core.listener;

import cn.itcast.core.service.StaticPageService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

public class PageListener implements MessageListener{
    @Autowired
    private StaticPageService staticPageService;
    @Override
    public void onMessage(Message message) {
        ActiveMQTextMessage atm= (ActiveMQTextMessage) message;

        try {
            String textId = atm.getText();
            System.out.println("静态化项目接收到的ID：" + textId);
            staticPageService.index(Long.parseLong(textId));
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
