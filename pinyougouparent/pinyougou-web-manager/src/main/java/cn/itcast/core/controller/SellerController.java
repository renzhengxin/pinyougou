package cn.itcast.core.controller;

import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.service.SellerService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller")
public class SellerController {

    @Reference
    SellerService sellerService;

    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows, @RequestBody Seller seller){
        PageResult pageResult=sellerService.search(page,rows,seller);

        return pageResult;
    }

    @RequestMapping("/findOne")
    public Seller findOne(String id){

        return sellerService.findOne(id);
    }
    @RequestMapping("/updateStatus")
    public Result updateStatus(String sellerId,String status){
        try {
            sellerService.updateStatus(sellerId,status);
            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }
    }

}
