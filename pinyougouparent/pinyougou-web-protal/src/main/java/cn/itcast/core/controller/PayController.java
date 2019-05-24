package cn.itcast.core.controller;

import cn.itcast.core.service.PayService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private PayService payService;

    @RequestMapping("/createNative")
    //前端的返回数据不知道用什么类型返回的时候,没有实体类的时候,用Map返回  key value 看情况啦
    public Map<String,String> createNative(){

        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        return payService.createNative(name);
    }



    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            int i=0;
            while (true) {
                Map<String, String> resultMap = payService.queryPayStatus(out_trade_no,name);
                if ("NOTPAY".equals(resultMap.get("trade_state"))) {
                    //支付失败

                    i++;
                    if (i>30){
                    //关闭订单API
                    payService.closeOrder(out_trade_no);
                    return new Result(false,"二维码超时");
                    }



                }else if(null==resultMap){
                    return new Result(false,"支付失败");
                } else {

                    return new Result(true, "支付成功");
                }

                Thread.sleep(10000);//10秒执行一次
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"支付失败");
        }
    }


    //url:closeorder





}
