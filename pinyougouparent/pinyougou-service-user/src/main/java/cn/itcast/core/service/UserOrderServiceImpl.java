package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.order.OrderDao;
import cn.itcast.core.dao.order.OrderItemDao;
import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.pojo.order.OrderItemQuery;
import cn.itcast.core.pojo.order.OrderQuery;
import cn.itcast.core.pojo.seller.Seller;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class UserOrderServiceImpl implements UserOrderService {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private SellerDao sellerDao;

    @Override
    public PageResult  findAllOrders(Integer page, Integer rows, String name) {
        PageHelper.startPage(page,rows);
        OrderQuery orderQuery=new OrderQuery();
        orderQuery.createCriteria().andUserIdEqualTo(name);
        List<Order> orders = orderDao.selectByExample(orderQuery);
        for (Order order : orders) {
            String sellerId = order.getSellerId();
            Seller seller = sellerDao.selectByPrimaryKey(sellerId);
            order.setSellerName(seller.getName());
            OrderItemQuery orderItemQuery=new OrderItemQuery();
            orderItemQuery.createCriteria().andOrderIdEqualTo(order.getOrderId());
            List<OrderItem> orderItemList = orderItemDao.selectByExample(orderItemQuery);
            for (OrderItem orderItem : orderItemList) {
                Item item = itemDao.selectByPrimaryKey(orderItem.getItemId());
                orderItem.setItem(item);
            }
            order.setOrderItemList(orderItemList);
        }
        Page<Order> ordersPage= (Page<Order>) orders;
        //List<List<Item>>
        return new PageResult(ordersPage.getTotal(),ordersPage.getResult());
    }
}
