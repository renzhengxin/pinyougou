package cn.itcast.core.controller;

import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.service.CollectService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 用户添加收藏管理
 */
@RestController
@RequestMapping("/collect")
public class CollectController {

    @Reference
    CollectService collectService;
    /**
     * 用户添加收藏
     * @param itemId
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/collectGoods")
    @CrossOrigin(origins = {"http://localhost:9003"}, allowCredentials = "true")    //解决跨域问题
    public Result collectGoods(Long itemId, HttpServletRequest request, HttpServletResponse response){
        try {
            //创建集合，存储收藏商品
            List<Item> itemList = null;
            //获取用户名
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            //用户未登录
            if ("anonymousUser".equals(name)){
                return new Result(false, "请先去登录");
            }

                //用户已登录
            Item item = collectService.findItemById(itemId);

            collectService.collectGoods(name,item);

        }catch (Exception e){
            e.printStackTrace();
            return new Result(false, "添加收藏失败");
        }
        return null;
    }
}
