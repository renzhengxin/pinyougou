package cn.itcast.core.service;

import cn.itcast.core.dao.good.BrandDao;
import entity.PageResult;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.BrandQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class BrandServiceImpl implements BrandService {
    @Autowired
    private BrandDao brandDao;

    @Override
    public List<Brand> findAll() {
        List<Brand> brandList = brandDao.selectByExample(null);
        return brandList;
    }


    @Override
    public PageResult findPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
//        PageInfo pageInfo=new PageInfo()

        List<Brand> brandList = brandDao.selectByExample(null);
        Page<Brand> page= (Page<Brand>)brandList;
        PageResult pageResult=new PageResult(page.getTotal(),page.getResult());
        return pageResult;
    }

    @Override
    public void add(Brand brand) {
        brandDao.insert(brand);
    }

    @Override
    public Brand findOne(Long id) {
        Brand brand = brandDao.selectByPrimaryKey(id);
        return brand;
    }

    @Override
    public void update(Brand brand) {
        brandDao.updateByPrimaryKeySelective(brand);
    }

    @Override
    public void deletByIds(Long[] ids) {

        if (ids!=null&&ids.length>0){
            BrandQuery brandQuery=new BrandQuery();
            BrandQuery.Criteria criteria = brandQuery.createCriteria();
            criteria.andIdIn(Arrays.asList(ids));
            brandDao.deleteByExample(brandQuery);

        }

    }

    @Override
    public PageResult findPageAndSearch(Integer page, Integer rows, Brand brand) {
        PageHelper.startPage(page, rows);
        BrandQuery brandQuery=new BrandQuery();
        if (brand!=null){
            BrandQuery.Criteria criteria = brandQuery.createCriteria();
            if (brand.getName()!=null&&!"".equals(brand.getName().trim())){
                criteria.andNameLike("%"+brand.getName()+"%");
            }
            if (brand.getFirstChar()!=null&&!"".equals(brand.getFirstChar().trim())){
                criteria.andFirstCharEqualTo(brand.getFirstChar());
            }

        }
        Page<Brand> brandPage = (Page<Brand>) brandDao.selectByExample(brandQuery);


        return new PageResult(brandPage.getTotal(),brandPage.getResult());
    }

    @Override
    public List<Map> selectOptionList() {
        List<Map> maps = brandDao.selectOptionList();
//        for (Map map : maps) {
//            Collection values = map.values();
//            for (Object value : values) {
//                System.out.println(value);
//            }
//        }

        return maps;
    }
}
