package cn.itcast.core.listener;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.List;

public class ItemSearchListener implements MessageListener {

    @Autowired
    private ItemDao itemDao;
    @Autowired
    private SolrTemplate solrTemplate;


    @Override
    public void onMessage(Message message) {
        ActiveMQTextMessage atm= (ActiveMQTextMessage) message;
        try {
            String textId = atm.getText();
            System.out.println("搜索管理项目中接收到的ID:" + textId);

            //solr添加索引
                ItemQuery itemQuery=new ItemQuery();
                itemQuery.createCriteria().andGoodsIdEqualTo(Long.parseLong(textId)).andStatusEqualTo("1").andIsDefaultEqualTo("1");

                List<Item> itemList = itemDao.selectByExample(itemQuery);
                for (Item item : itemList) {
                    System.out.println(item);
                }
                solrTemplate.saveBeans(itemList);
                solrTemplate.commit();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
