package cn.itcast.core.service;

import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.dubbo.config.annotation.Service;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.servlet.ServletContext;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StaticPageServiceImpl implements StaticPageService,ServletContextAware {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Autowired
    private GoodsDao goodsDao;
    @Autowired
    private GoodsDescDao goodsDescDao;
    @Autowired
    private ItemCatDao itemCatDao;
    @Autowired
    private ItemDao itemDao;


    @Override
    public void index(Long id) {
        Configuration conf = freeMarkerConfigurer.getConfiguration();
        String path=getPath("/"+id+".html");

        Writer out=null;

        try {
            Template template = conf.getTemplate("item.ftl");

            Map<String,Object> root=new HashMap<>();

            //根据商品ID 外键   查询库存结果集
            ItemQuery itemQuery=new ItemQuery();
            itemQuery.createCriteria().andGoodsIdEqualTo(id);
            List<Item> itemList = itemDao.selectByExample(itemQuery);
            root.put("itemList", itemList);

            //根据商品ID 查询商品详情对象
            GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(id);
            root.put("goodsDesc", goodsDesc);


            //根据商品ID 查询商品对象
            Goods goods = goodsDao.selectByPrimaryKey(id);
            root.put("goods", goods);


            //查询一级 二级 三级分类的名称
            root.put("itemCat1", itemCatDao.selectByPrimaryKey(goods.getCategory1Id()).getName());
            root.put("itemCat2", itemCatDao.selectByPrimaryKey(goods.getCategory2Id()).getName());
            root.put("itemCat3", itemCatDao.selectByPrimaryKey(goods.getCategory3Id()).getName());


//            输出
            out=new OutputStreamWriter(new FileOutputStream(path),"UTF-8");
            template.process(root,out);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (null!=out){
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    private ServletContext servletContext;

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext=servletContext;
    }
    private String getPath(String s) {
        return servletContext.getRealPath(s);
    }
}
