package cn.itcast.core.service;

import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.pojo.template.TypeTemplateQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class TypeTemplateServiceImpl implements TypeTemplateService{

    @Autowired
    TypeTemplateDao typeTemplateDao;
    @Autowired
    RedisTemplate redisTemplate;


    @Override
    public PageResult search(Integer page, Integer rows, TypeTemplate typeTemplate) {

        List<TypeTemplate> typeTemplateList = findAll();

        for (TypeTemplate template : typeTemplateList) {
            List<Map>  brandList= JSON.parseArray(template.getBrandIds(), Map.class);
            redisTemplate.boundHashOps("brandList").put(template.getId(), brandList);


            //List<Map> specList1 = JSON.parseArray(template.getSpecIds(), Map.class);
            List<Map> specList = findBySpecList(template.getId());
            redisTemplate.boundHashOps("specList").put(template.getId(), specList);


        }


        PageHelper.startPage(page, rows);
        TypeTemplateQuery query=new TypeTemplateQuery();
        if (typeTemplate!=null){
                if (null!=typeTemplate.getName()&&!"".equals(typeTemplate.getName().trim())){
                    query.createCriteria().andNameLike("%"+typeTemplate.getName()+"%");
                }
        }

        Page<TypeTemplate> typeTemplatePage= (Page<TypeTemplate>) typeTemplateDao.selectByExample(query);

//        List<TypeTemplate> list=typeTemplatePage.getResult();
//        for (TypeTemplate template : list) {
//            System.out.println(template);
//        }

        return new PageResult(typeTemplatePage.getTotal(),typeTemplatePage.getResult());
    }

    @Override
    public void add(TypeTemplate typeTemplate) {
        typeTemplateDao.insertSelective(typeTemplate);
    }

    @Override
    public TypeTemplate findOne(Long id) {

        return typeTemplateDao.selectByPrimaryKey(id);
    }

    @Override
    public void update(TypeTemplate typeTemplate) {
        typeTemplateDao.updateByPrimaryKeySelective(typeTemplate);
    }

    @Override
    public void delete(Long[] ids) {
        if (ids!=null&&ids.length>0) {
            TypeTemplateQuery query=new TypeTemplateQuery();
            query.createCriteria().andIdIn(Arrays.asList(ids));
            typeTemplateDao.deleteByExample(query);
        }
    }
    @Autowired
    SpecificationOptionDao specificationOptionDao;
    @Override
    public List<Map> findBySpecList(Long id) {
        TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(id);
        List<Map> listMap= JSON.parseArray(typeTemplate.getSpecIds(), Map.class);
        for (Map map : listMap) {

            SpecificationOptionQuery query=new SpecificationOptionQuery();
            query.createCriteria().andSpecIdEqualTo(Long.parseLong(String.valueOf(map.get("id"))));
            List<SpecificationOption> options = specificationOptionDao.selectByExample(query);
            map.put("options", options);

        }

        return listMap;
    }

    @Override
    public List<TypeTemplate> findAll() {
        return typeTemplateDao.selectByExample(null);
    }
}
