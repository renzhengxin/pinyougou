package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.log.PayLogDao;
import cn.itcast.core.dao.order.OrderDao;
import cn.itcast.core.dao.order.OrderItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.pojo.order.OrderItem;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import util.IdWorker;
import vo.Cart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderDao orderDao;
    @Autowired
    private OrderItemDao orderItemDao;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private PayLogDao payLogDao;
    @Autowired
    private IdWorker idWorker;

    @Override
    public void add(Order order) {
        //订单已有地址手机联系人
        //获取此用户的购物车
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("CART").get(order.getUserId());

        //用于存入paylog表
        long allTotalPay=0;
        List<String> ids=new ArrayList<>();

        for (Cart cart : cartList) {
            double payment=0;


            long id = idWorker.nextId();
            order.setOrderId(id);
            ids.add(String.valueOf(id));

            for (OrderItem orderItem : cart.getOrderItemList()) {
                Item item = itemDao.selectByPrimaryKey(orderItem.getItemId());

                orderItem.setId(idWorker.nextId());
                //goodsid  orderid   title  price picpath sellerid
                orderItem.setGoodsId(item.getGoodsId());
                orderItem.setOrderId(id);
                orderItem.setTitle(item.getTitle());
                orderItem.setPrice(item.getPrice());
                orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*orderItem.getNum()));
                orderItem.setPicPath(item.getImage());
                orderItem.setSellerId(item.getSellerId());

                orderItemDao.insertSelective(orderItem);

                //商家小订单的总金额
                payment+=orderItem.getTotalFee().doubleValue();
            }


            order.setPayment(new BigDecimal(payment));
            order.setCreateTime(new Date());
            order.setUpdateTime(new Date());
            order.setUserId(order.getUserId());
            order.setSourceType("2");
            orderDao.insertSelective(order);

            allTotalPay+=order.getPayment().doubleValue()*100;

        }

        PayLog payLog=new PayLog();
        payLog.setOutTradeNo(String.valueOf(idWorker.nextId()));
        payLog.setCreateTime(new Date());
        payLog.setTotalFee(allTotalPay);
        payLog.setUserId(order.getUserId());
        payLog.setTradeState("0");
        payLog.setOrderList(ids.toString().replace("[", "").replace("]", ""));
        payLog.setPayType(order.getPaymentType());

        payLogDao.insertSelective(payLog);

        redisTemplate.boundValueOps(order.getUserId()).set(payLog, 24, TimeUnit.HOURS);



    }
}
