package cn.itcast.core.service;

import entity.PageResult;
import cn.itcast.core.pojo.good.Brand;

import java.util.List;
import java.util.Map;

public interface BrandService {

    public List<Brand> findAll();

    public PageResult findPage(Integer pageNum,Integer pageSize);

    public void add(Brand brand);

    public Brand findOne(Long id);

    public void update(Brand brand);

    public void deletByIds(Long[] ids);
//    带条件的分页查询
    public PageResult findPageAndSearch(Integer page, Integer rows, Brand brand);

    List<Map> selectOptionList();
}
