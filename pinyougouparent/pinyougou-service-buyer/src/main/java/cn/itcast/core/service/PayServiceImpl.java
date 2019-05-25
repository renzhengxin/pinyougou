package cn.itcast.core.service;

import cn.itcast.core.dao.log.PayLogDao;
import cn.itcast.core.dao.order.OrderDao;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.pojo.order.Order;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import util.HttpClient;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class PayServiceImpl implements PayService{



    @Autowired
    private RedisTemplate redisTemplate;

    //项目中的properties 都放在pinyougou-dao下  包括这里的微信的配置文件
    @Value("${appid}")
    private String appid;   //公众号ID
    @Value("${partner}")
    private String partner;   //商户号
    @Value("${partnerkey}")
    private String partnerkey; //密钥  进行加密用的   xml格式的字符串要不要加密 用什么加密


    @Override
    public Map<String, String> createNative(String name) {

        PayLog payLog = (PayLog) redisTemplate.boundValueOps(name).get();

        //从微信服务端查询  API
        String url="https://api.mch.weixin.qq.com/pay/unifiedorder";
        //使用Java代码 发请求 接收响应   Apache Java 实现了浏览器的功能
        HttpClient httpClient=new HttpClient(url);
        //是https请求?
        httpClient.setHttps(true);

        Map<String,String> params=new HashMap<>();

        //        字段名	变量名	必填	类型	示例值	描述
//        公众账号ID	appid	是	String(32)	wxd678efh567hg6787	微信支付分配的公众账号ID（企业号corpid即为此appId）
        params.put("appid", appid);
//        商户号	mch_id	是	String(32)	1230000109	微信支付分配的商户号
        params.put("mch_id", partner);
//        设备号	device_info	否	String(32)	013467007045764	自定义参数，可以为终端设备号(门店号或收银设备ID)，PC网页或公众号内支付可以传"WEB"
//        随机字符串	nonce_str	是	String(32)	5K8264ILTKCH16CQ2502SI8ZNMTM67VS	随机字符串，长度要求在32位以内。推荐随机数生成算法
        params.put("nonce_str", WXPayUtil.generateNonceStr());
        //        商品描述	body	是	String(128)	腾讯充值中心-QQ会员充值
        params.put("body", "品优购支付订单的金额,快点给老子付钱!");

        //        商品简单描述，该字段请按照规范传递，具体请见参数规定
//
//        商品详情	detail	否	String(6000)	 	商品详细描述，对于使用单品优惠的商户，改字段必须按照规范上传，详见“单品优惠参数说明”
//        附加数据	attach	否	String(127)	深圳分店	附加数据，在查询API和支付通知中原样返回，可作为自定义参数使用。
//        商户订单号	out_trade_no	是	String(32)	20150806125346	商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|* 且在同一个商户号下唯一。详见商户订单号
        params.put("out_trade_no", payLog.getOutTradeNo());
//        标价币种	fee_type	否	String(16)	CNY	符合ISO 4217标准的三位字母代码，默认人民币：CNY，详细列表请参见货币类型
//        标价金额	total_fee	是	Int	88	订单总金额，单位为分，详见支付金额
        //param.put("total_fee", String.valueOf(payLog.getTotalFee()));
        params.put("total_fee", "1");
//        终端IP	spbill_create_ip	是	String(64)	123.12.12.123	支持IPV4和IPV6两种格式的IP地址。调用微信支付API的机器IP
        params.put("spbill_create_ip", "127.0.0.1");
//        交易起始时间	time_start	否	String(14)	20091225091010	订单生成时间，格式为yyyyMMddHHmmss，如2009年12月25日9点10分10秒表示为20091225091010。其他详见时间规则
//        交易结束时间	time_expire	否	String(14)	20091227091010
//        订单失效时间，格式为yyyyMMddHHmmss，如2009年12月27日9点10分10秒表示为20091227091010。订单失效时间是针对订单号而言的，由于在请求支付的时候有一个必传参数prepay_id只有两小时的有效期，所以在重入时间超过2小时的时候需要重新请求下单接口获取新的prepay_id。其他详见时间规则
//
//        建议：最短失效时间间隔大于1分钟
//
//        订单优惠标记	goods_tag	否	String(32)	WXG	订单优惠标记，使用代金券或立减优惠功能时需要的参数，说明详见代金券或立减优惠
//        通知地址	notify_url	是	String(256)	http://www.weixin.qq.com/wxpay/pay.php	异步接收微信支付结果通知的回调地址，通知url必须为外网可访问的url，不能携带参数。
        params.put("notify_url", "http://itcast.cn");
//        交易类型	trade_type	是	String(16)	JSAPI
        params.put("trade_type", "NATIVE");

//        签名	sign	是	String(32)	C380BEC2BFD727A4B6845133519F3AD6	通过签名算法计算得出的签名值，详见签名生成算法
//        签名类型	sign_type	否	String(32)	MD5	签名类型，默认为MD5，支持HMAC-SHA256和MD5。

        //返回参数的map
        Map<String, String> map=null;

        try {
            String xml = WXPayUtil.generateSignedXml(params, partnerkey);
            //设置入参
            httpClient.setXmlParam(xml);
            //POSt提交
            httpClient.post();
            //接收响应
            String content = httpClient.getContent();

            map = WXPayUtil.xmlToMap(content);
            //二维码URL是有的
            map.put("total_fee",String.valueOf(payLog.getTotalFee()));
            map.put("out_trade_no",payLog.getOutTradeNo());


            Set<Map.Entry<String, String>> entrySet = map.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                System.out.println(entry.getKey()+entry.getValue());
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


        return map;
    }

    @Override
    public Map<String, String> queryPayStatus(String out_trade_no,String name) {
        //从微信服务端查询  API
        String url="https://api.mch.weixin.qq.com/pay/orderquery";
        //使用Java代码 发请求 接收响应   Apache Java 实现了浏览器的功能
        HttpClient httpClient=new HttpClient(url);
        //是https请求?
        httpClient.setHttps(true);

        Map<String,String> params=new HashMap<>();
        //        字段名	变量名	必填	类型	示例值	描述
//        公众账号ID	appid	是	String(32)	wxd678efh567hg6787	微信支付分配的公众账号ID（企业号corpid即为此appId）
        params.put("appid", appid);
//        商户号	mch_id	是	String(32)	1230000109	微信支付分配的商户号
        params.put("mch_id", partner);
        //商户订单号	out_trade_no	String(32)	20150806125346	商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|*@ ，且在同一个商户号下唯一。 详见商户订单号
        params.put("out_trade_no", out_trade_no);
//随机字符串
        params.put("nonce_str", WXPayUtil.generateNonceStr());

        //返回参数的map

        try {
            //把参数转成XML格式并加密
            String xml = WXPayUtil.generateSignedXml(params, partnerkey);
            //设置入参
            httpClient.setXmlParam(xml);
            //POSt提交
            httpClient.post();
            //接收响应
            String content = httpClient.getContent();

            Map<String, String> resultMap = WXPayUtil.xmlToMap(content);

            //查看结果
            Set<Map.Entry<String, String>> entrySet = resultMap.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                System.out.println(entry.getKey()+entry.getValue());
            }

            if ("SUCCESS".equals(resultMap.get("trade_state"))){

                //已付款
                //更新
                //这个方法主要做三件事：
                //1. 修改支付日志状态
                //2. 修改关联的订单的状态
                //3. 清除缓存中的支付日志对象
                updateOrderStatus(resultMap.get("transaction_id"),name);

            }

            return  resultMap;

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }


        return null;

    }


    @Autowired
    private PayLogDao payLogDao;
    @Autowired
    private OrderDao orderDao;

    private void updateOrderStatus(String transaction_id, String name) {
        //已付款
        //更新
        //这个方法主要做三件事：
        //1. 修改支付日志状态  trade_state  transaction_id流水号  pay_time
        PayLog payLog= (PayLog) redisTemplate.boundValueOps(name).get();
        payLog.setPayTime(new Date());
        payLog.setTransactionId(transaction_id);
        payLog.setTradeState("1");
        payLogDao.updateByPrimaryKeySelective(payLog);
        //2. 修改关联的订单的状态  payment_time  status  状态：1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭,7、待评价

        String[] orderList = payLog.getOrderList().split(",");
        Order order=new Order();
        order.setPaymentTime(new Date());
        order.setStatus("2");
        for (String oid : orderList) {
            order.setOrderId(Long.valueOf(oid));
            orderDao.updateByPrimaryKeySelective(order);
        }
        //3. 清除缓存中的支付日志对象  redisTemplate
        redisTemplate.delete(name);




    }



    @Override
    public Map<String, String> closeOrder(String out_trade_no) {
        //https://api.mch.weixin.qq.com/pay/closeorder

        String url="https://api.mch.weixin.qq.com/pay/closeorder";
        //使用Java代码 发请求 接收响应   Apache Java 实现了浏览器的功能
        HttpClient httpClient=new HttpClient(url);
        //是https请求?
        httpClient.setHttps(true);

        Map<String,String> params=new HashMap<>();

//        公众账号ID	appid	是	String(32)	wx8888888888888888	微信分配的公众账号ID（企业号corpid即为此appId）
//        商户号	mch_id	是	String(32)	1900000109	微信支付分配的商户号
//        商户订单号	out_trade_no	是	String(32)	1217752501201407033233368018	商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|*@ ，且在同一个商户号下唯一。
//        随机字符串	nonce_str	是	String(32)	5K8264ILTKCH16CQ2502SI8ZNMTM67VS	随机字符串，不长于32位。推荐随机数生成算法
//        签名	sign	是	String(32)	C380BEC2BFD727A4B6845133519F3AD6	签名，详见签名生成算法
//        签名类型	sign_type	否	String(32)	HMAC-SHA256	签名类型，目前支持HMAC-SHA256和MD5，默认为MD5

    //        公众账号ID	appid	是	String(32)	wxd678efh567hg6787	微信支付分配的公众账号ID（企业号corpid即为此appId）
            params.put("appid", appid);
    //        商户号	mch_id	是	String(32)	1230000109	微信支付分配的商户号
            params.put("mch_id", partner);
            //商户订单号	out_trade_no	String(32)	20150806125346	商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|*@ ，且在同一个商户号下唯一。 详见商户订单号
            params.put("out_trade_no", out_trade_no);
    //随机字符串
            params.put("nonce_str", WXPayUtil.generateNonceStr());




        try {
            //把参数转成XML格式并加密
            String xml = WXPayUtil.generateSignedXml(params, partnerkey);
            //设置入参
            httpClient.setXmlParam(xml);
            //POSt提交
            httpClient.post();
            //接收响应
            String content = httpClient.getContent();

            Map<String, String> resultMap = WXPayUtil.xmlToMap(content);

            //查看结果
//            Set<Map.Entry<String, String>> entrySet = resultMap.entrySet();
//            for (Map.Entry<String, String> entry : entrySet) {
//                System.out.println(entry.getKey()+entry.getValue());
//            }
            if ("SUCCESS".equals(resultMap.get("return_code"))) {//通话连接成功
                if ("FAIL".equals(resultMap.get("result_code"))) {
                    //订单结果失败
                    //关闭订单,从缓存清除
                    //根据out_trade_no查询到支付订单日志，并从缓存清除
                    return resultMap;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
