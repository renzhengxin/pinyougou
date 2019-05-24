package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import vo.Cart;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartServiceImpl implements CartService{

    @Autowired
    private ItemDao itemDao;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Item findItemById(Long itemId) {
        return itemDao.selectByPrimaryKey(itemId);
    }




    @Override
    public void addCartListToRedis(List<Cart> newCartList, String name) {
        List<Cart> oldCartList = (List<Cart>) redisTemplate.boundHashOps("CART").get(name);

        //2:在新老车集合大合并  最终合并到老车
        oldCartList = mergeCartList(newCartList,oldCartList);


        redisTemplate.boundHashOps("CART").put(name, oldCartList);



    }

    @Override
    public List<Cart> findCartListFromRedis(String name) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("CART").get(name);
        return cartList;
    }


    @Override
    public List<Cart> findAllCartList(List<Cart> cartList) {

        for (Cart cart : cartList) {

            List<OrderItem> orderItemList = cart.getOrderItemList();
            for (OrderItem orderItem : orderItemList) {
                Item item = findItemById(orderItem.getItemId());
                orderItem.setPicPath(item.getImage());
                orderItem.setTitle(item.getTitle());
                orderItem.setPrice(item.getPrice());
                orderItem.setTotalFee(new BigDecimal(orderItem.getNum()*item.getPrice().doubleValue()));
                cart.setSellerName(item.getSeller());
            }


        }


        return cartList;
    }

    private List<Cart> mergeCartList(List<Cart> newCartList, List<Cart> oldCartList) {


        if (null!=newCartList&&newCartList.size()>0){

            if (null!=oldCartList&&oldCartList.size()>0){
                for (Cart newCart : newCartList) {

                    int i = oldCartList.indexOf(newCart);
                    if (i!=-1){
                        Cart oldCart = oldCartList.get(i);
                        List<OrderItem> oldCartOrderItemList = oldCart.getOrderItemList();
                        List<OrderItem> newCartOrderItemList = newCart.getOrderItemList();
                        for (OrderItem newOrderItem : newCartOrderItemList) {

                            int j = oldCartOrderItemList.indexOf(newOrderItem);
                            if (j!=-1){

                                OrderItem oldOrderItem = oldCartOrderItemList.get(j);
                                oldOrderItem.setNum(oldOrderItem.getNum()+newOrderItem.getNum());

                            }else {
                                oldCartOrderItemList.add(newOrderItem);
                            }
                        }

                    }else {
                        oldCartList.add(newCart);
                    }


                }




            }else {
                return newCartList;
            }
        }else {
            return oldCartList;
        }
        return oldCartList;
    }



}
