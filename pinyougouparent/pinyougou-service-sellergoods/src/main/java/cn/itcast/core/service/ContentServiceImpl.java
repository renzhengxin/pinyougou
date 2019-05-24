package cn.itcast.core.service;

import cn.itcast.core.dao.ad.ContentDao;
import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.pojo.ad.ContentQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

@Service
public class ContentServiceImpl implements ContentService {

    @Autowired
    private ContentDao contentDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<Content> findAll() {
        List<Content> list = contentDao.selectByExample(null);
        return list;
    }

    @Override
    public PageResult findPage(Content content, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<Content> page = (Page<Content>)contentDao.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void add(Content content) {
        redisTemplate.boundHashOps("content").delete(content.getCategoryId());
        contentDao.insertSelective(content);
    }

    @Override
    public void edit(Content content) {

        Content oldContend = contentDao.selectByPrimaryKey(content.getId());
        redisTemplate.boundHashOps("content").delete(oldContend.getCategoryId());

        if (!content.getCategoryId().equals(oldContend.getCategoryId())){

        redisTemplate.boundHashOps("content").delete(content.getCategoryId());

        }

        contentDao.updateByPrimaryKeySelective(content);

    }

    @Override
    public Content findOne(Long id) {
        Content content = contentDao.selectByPrimaryKey(id);
        return content;
    }

    @Override
    public void delAll(Long[] ids) {
        if(ids != null){
            for(Long id : ids){
                Content oldContend = contentDao.selectByPrimaryKey(id);

                contentDao.deleteByPrimaryKey(id);

                redisTemplate.boundHashOps("content").delete(oldContend.getCategoryId());


            }
        }


    }

    @Override
    public List<Content> findByCategoryId(Long categoryId) {

        List<Content> contentList = (List<Content>) redisTemplate.boundHashOps("content").get(categoryId);

        if (contentList==null||contentList.size()==0){
            System.out.println("走数据库查询");
            ContentQuery query=new ContentQuery();
            query.createCriteria().andCategoryIdEqualTo(categoryId).andStatusEqualTo("1");
            query.setOrderByClause("sort_order desc");
            contentList=contentDao.selectByExample(query);
            redisTemplate.boundHashOps("content").put(categoryId, contentList);


        }else {
            System.out.println("缓存中查询");
        }


        return contentList;

    }

}
