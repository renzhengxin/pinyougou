package cn.itcast.core.controller;

import cn.itcast.core.pojo.address.Address;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.service.CartService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vo.Cart;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {


    @Reference
    private CartService cartService;

    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins="http://localhost:9003",allowCredentials="true")
    public Result addGoodsToCartList(Long itemId, Integer num, HttpServletRequest request,HttpServletResponse response){
        try {

        boolean loginFlag=false;
        //未登录状态下
        List<Cart> cartList=null;
        Cookie[] cookies = request.getCookies();
        if (cookies!=null&&cookies.length>0){
            for (Cookie cookie : cookies) {
                if ("CART".equals(cookie.getName())){
                    cartList=JSON.parseArray(cookie.getValue(), Cart.class);
                    loginFlag=true;
                }
            }
        }
        if (cartList==null){
            cartList=new ArrayList<>();
        }
        //新购物车
        Cart newCart=new Cart();
        //根据库存ID查询 商家ID
        Item item=cartService.findItemById(itemId);
        newCart.setSellerId(item.getSellerId());

        List<OrderItem> newOrderItemList=new ArrayList<>();
        OrderItem newOrderItem=new OrderItem();
        newOrderItem.setItemId(itemId);
        newOrderItem.setNum(num);
        newOrderItemList.add(newOrderItem);

        newCart.setOrderItemList(newOrderItemList);

        //逻辑判断  原来的购物车中  是否有新购物车中的商家
        int cartIndexOf = cartList.indexOf(newCart);
        if (cartIndexOf!=-1){

            //存在
            Cart cart = cartList.get(cartIndexOf);
            List<OrderItem> oldOrderItemList = cart.getOrderItemList();
            int orderItemIndexOf = oldOrderItemList.indexOf(newOrderItem);
            if (orderItemIndexOf!=-1){
                OrderItem oldOrderItem = oldOrderItemList.get(orderItemIndexOf);
                oldOrderItem.setNum(oldOrderItem.getNum()+newOrderItem.getNum());

            }else {
                oldOrderItemList.add(newOrderItem);
            }

        }else {

            cartList.add(newCart);
        }



         String name = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!"anonymousUser".equals(name)){
//            已登录状态
            cartService.addCartListToRedis(cartList,name);

            if (loginFlag){
//                清空购物车cookie 返回页面
                Cookie cookie=new Cookie("CART",null);
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);

            }


        }else {
//            未登录状态
            Cookie cookie=new Cookie("CART",JSON.toJSONString(cartList));
            cookie.setPath("/");
            cookie.setMaxAge(60*60*24*365);

            response.addCookie(cookie);

        }

            return new Result(true,"添加购物车成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加购物车失败");
        }
    }

    @RequestMapping("/findCartList")
    public List<Cart> findCartList(HttpServletRequest request,HttpServletResponse response){
        List<Cart> cartList=null;
        Cookie[] cookies = request.getCookies();
        if (cookies!=null&&cookies.length>0){
            for (Cookie cookie : cookies) {
                if ("CART".equals(cookie.getName())){
                    cartList=JSON.parseArray(cookie.getValue(), Cart.class);
                    break;
                }
            }
        }
//        已登录
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!"anonymousUser".equals(name)){
        //不是匿名登录用户
            if (cartList!=null){
                cartService.addCartListToRedis(cartList,name);
                Cookie cookie = new Cookie("CART", null);
                cookie.setPath("/");
                cookie.setMaxAge(0);// 0 :立即销毁  -1:关闭浏览器销毁 >0:到时间销毁 (秒)
                response.addCookie(cookie);
            }

            cartList=cartService.findCartListFromRedis(name);

        }

//        填充完整数据

        if (null != cartList) {//现在只有商家ID 库存ID  数量  , 其它值都没有
            cartList = cartService.findAllCartList(cartList);
        }


        return cartList;
    }





}
