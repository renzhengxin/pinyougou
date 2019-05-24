package cn.itcast.core.service;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsQuery;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemQuery;
import cn.itcast.core.pojo.seller.Seller;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;
import vo.GoodsVo;
import javax.jms.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * 商品管理
 */
@Service
@Transactional
public class GoodsServiceImpl implements  GoodsService {


    @Autowired
    private GoodsDao goodsDao;
    @Autowired
    private GoodsDescDao goodsDescDao;
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private SellerDao sellerDao;
    @Autowired
    private ItemCatDao itemCatDao;
    @Autowired
    private BrandDao brandDao;



    //添加商品
    @Override
    public void add(GoodsVo vo) {
        //1:商品表 主键
        //默认是0:未审核  1:审核通过  2:审核不通过 3:关闭
        vo.getGoods().setAuditStatus("0");
        //上班的时候  数据库 真删除吗 假删除  逻辑删除  is_delete = 1 表示删除  is_delete = null 不删除
        //保存
        goodsDao.insertSelective(vo.getGoods());

        //2:商品详情表
        //主键
        vo.getGoodsDesc().setGoodsId(vo.getGoods().getId());
        goodsDescDao.insertSelective(vo.getGoodsDesc());

        //判断是否启用规格
        if ("1".equals(vo.getGoods().getIsEnableSpec())) {
            //启用
            //3:库存多个表
            List<Item> itemList = vo.getItemList();
            for (Item item : itemList) {

                //商品名称  名称
                //标题  商品名称 " " + 规格1 + " " + 规格2 + ..
                String title = vo.getGoods().getGoodsName();

                //{"机身内存":"16G","网络":"联通3G"}
                Map<String, String> specMap = JSON.parseObject(item.getSpec(), Map.class);
                Set<Map.Entry<String, String>> entries = specMap.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    title += " " + entry.getValue();
                }
                item.setTitle(title);
                //设置库存的属性
                setAttribute(vo,item);

                //保存库存表
                itemDao.insertSelective(item);
            }

        } else {
            //不启用  默认
            Item item = new Item();
            //标题
            item.setTitle(vo.getGoods().getGoodsName());
            //设置库存的属性
            setAttribute(vo,item);
            //规格
            item.setSpec("{}");
            //价格
            item.setPrice(new BigDecimal(0));
            //库存
            item.setNum(9999);
            //状态 是否启用
            item.setStatus("1");

            itemDao.insertSelective(item);

        }

    }


    @Override
    public void update(GoodsVo vo) {
        //修改商品表
        goodsDao.updateByPrimaryKey(vo.getGoods());
        //修改商品描述表
        goodsDescDao.updateByPrimaryKey(vo.getGoodsDesc());
        //修改库存表(库存表修改很麻烦  需要先删除之前的信息  再添加新修改的库存信息)
        //删除之前记录操作
        ItemQuery query=new ItemQuery();
        query.createCriteria().andGoodsIdEqualTo(vo.getGoods().getId());
        itemDao.deleteByExample(query);

        //添加操作

        //判断是否启用规格
        if ("1".equals(vo.getGoods().getIsEnableSpec())) {
            //启用
            //3:库存多个表
            List<Item> itemList = vo.getItemList();
            for (Item item : itemList) {

                //商品名称  名称
                //标题  商品名称 " " + 规格1 + " " + 规格2 + ..
                String title = vo.getGoods().getGoodsName();

                //{"机身内存":"16G","网络":"联通3G"}
                Map<String, String> specMap = JSON.parseObject(item.getSpec(), Map.class);
                Set<Map.Entry<String, String>> entries = specMap.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    title += " " + entry.getValue();
                }
                item.setTitle(title);
                //设置库存的属性
                setAttribute(vo,item);

                //保存库存表
                itemDao.insertSelective(item);
            }

        } else {
            //不启用  默认
            Item item = new Item();
            //标题
            item.setTitle(vo.getGoods().getGoodsName());
            //设置库存的属性
            setAttribute(vo,item);
            //规格
            item.setSpec("{}");
            //价格
            item.setPrice(new BigDecimal(0));
            //库存
            item.setNum(9999);
            //状态 是否启用
            item.setStatus("1");

            itemDao.insertSelective(item);

        }


    }



    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private Destination topicPageAndSolrDestination;

    @Override
    public void updateStatus(Long[] ids, String status) {
        Goods goods=new Goods();
        goods.setAuditStatus(status);
//        GoodsQuery query=new GoodsQuery();
//        query.createCriteria().andIdIn(Arrays.asList(ids));
//        goodsDao.updateByExampleSelective(goods,query);
        for (Long id : ids) {

            goods.setId(id);
            goodsDao.updateByPrimaryKeySelective(goods);

            if ("1".equals(status)){

                jmsTemplate.send(topicPageAndSolrDestination,new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        TextMessage textMessage = session.createTextMessage(String.valueOf(id));
                        return textMessage;
                    }
                });







//                //solr添加索引
//                ItemQuery itemQuery=new ItemQuery();
//                itemQuery.createCriteria().andGoodsIdEqualTo(id).andStatusEqualTo("1").andIsDefaultEqualTo("1");
//
//                List<Item> itemList = itemDao.selectByExample(itemQuery);
//                for (Item item : itemList) {
//                    System.out.println(item);
//                }
//                solrTemplate.saveBeans(itemList);
//                solrTemplate.commit();
//                //静态化页面
//                staticPageService.index(id);

            }

        }



    }

    @Autowired
    private Destination queueSolrDeleteDestination;
    @Override
    public void delete(Long[] ids) {
        Goods goods=new Goods();
        goods.setIsDelete("1");
        for (Long id : ids) {
            goods.setId(id);
            goodsDao.updateByPrimaryKeySelective(goods);
//            SolrDataQuery solrDataQuery=new SimpleQuery(new Criteria("item_goodsid").is(id));


//            SolrDataQuery solrDataQuery=new SimpleQuery("item_goodsid:"+id);
//            solrTemplate.delete(solrDataQuery);
//            solrTemplate.commit();

            jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    TextMessage textMessage = session.createTextMessage(String.valueOf(id));
                    return textMessage;
                }
            });


        }

    }


    @Override
    public PageResult search(Integer page, Integer rows, Goods goods) {

        PageHelper.startPage(page, rows);

        PageHelper.orderBy("id desc");

        GoodsQuery query=new GoodsQuery();

        GoodsQuery.Criteria criteria = query.createCriteria();

            if (goods.getSellerId()!=null) {
                criteria.andSellerIdEqualTo(goods.getSellerId());
            }

            if (null!=goods.getAuditStatus()&&!"".equals(goods.getAuditStatus())){
                criteria.andAuditStatusEqualTo(goods.getAuditStatus());
            }
            if (null!=goods.getGoodsName()&&!"".equals(goods.getGoodsName().trim())){
                criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
            }
//        是否删除 值为Null 的为正常  不为null的为删除的商品
        criteria.andIsDeleteIsNull();




        Page<Goods> goodsPage= (Page<Goods>) goodsDao.selectByExample(query);
        return new PageResult(goodsPage.getTotal(),goodsPage.getResult());
    }

    @Override
    public GoodsVo findOne(Long id) {
        GoodsVo vo=new GoodsVo();
        vo.setGoods(goodsDao.selectByPrimaryKey(id));
        vo.setGoodsDesc(goodsDescDao.selectByPrimaryKey(id));
        ItemQuery query=new ItemQuery();
        query.createCriteria().andGoodsIdEqualTo(id);
        List<Item> itemList = itemDao.selectByExample(query);
        vo.setItemList(itemList);
        return vo;
    }


    //设置库存的属性
    public void setAttribute(GoodsVo vo,Item item){
        //图片
        //[{"color":"粉色","url":"http://192.168.25.133/group1/M00/00/00/wKgZhVmOXq2AFIs5AAgawLS1G5Y004.jpg"},{"color":"黑色","url":"http://192.168.25.133/group1/M00/00/00/wKgZhVmOXrWAcIsOAAETwD7A1Is874.jpg"}]
        String itemImages = vo.getGoodsDesc().getItemImages();
        List<Map> images = JSON.parseArray(itemImages, Map.class);
        if (null != images && images.size() > 0) {
            //{"color":"粉色","url":"http:
            item.setImage(String.valueOf(images.get(0).get("url")));
        }
        //三级分类Id
        item.setCategoryid(vo.getGoods().getCategory3Id());
        //三级分类的名称
        ItemCat itemCat = itemCatDao.selectByPrimaryKey(vo.getGoods().getCategory3Id());
        item.setCategory(itemCat.getName());

        //添加时间
        item.setCreateTime(new Date());
        //修改时间
        item.setUpdateTime(new Date());
        //商品ID (外键)
        item.setGoodsId(vo.getGoods().getId());

        //商家ID
        item.setSellerId(vo.getGoods().getSellerId());
        //商家名称  冗余的方式保存库存表?
        Seller seller = sellerDao.selectByPrimaryKey(vo.getGoods().getSellerId());
        item.setSeller(seller.getNickName());
        //品牌名称
        Brand brand = brandDao.selectByPrimaryKey(vo.getGoods().getBrandId());
        item.setBrand(brand.getName());
    }

}
