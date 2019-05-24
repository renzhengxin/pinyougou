package cn.itcast.core.controller;

import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.service.GoodsService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vo.GoodsVo;

@RestController
@RequestMapping("/goods")
public class GoodsController {
    @Reference
    GoodsService goodsService;

    @RequestMapping("/add")
    public Result add(@RequestBody GoodsVo vo){
        //设置sellerId  目的是把商品添加到该登录商户的下面
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        vo.getGoods().setSellerId(name);
        try {
            goodsService.add(vo);
            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }

    }

    //update  修改商品
    @RequestMapping("/update")
    public Result update(@RequestBody GoodsVo vo){
        try {
            goodsService.update(vo);
            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }

    }


    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows, @RequestBody Goods goods){
        return goodsService.search(page,rows,goods);
    }


    //findOne
    @RequestMapping("/findOne")
    public GoodsVo findOne(Long id){
        return goodsService.findOne(id);
    }


    //updateStatus
    @RequestMapping("/updateStatus")
    public Result updateStatus(Long[] ids,String status){
        try {
            goodsService.updateStatus(ids,status);
            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }

    }

    //delete
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            goodsService.delete(ids);
            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }

    }


}
