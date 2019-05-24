package cn.itcast.core.service;

import cn.itcast.core.pojo.specification.Specification;
import entity.PageResult;
import vo.SpecificationVo;

import java.util.List;
import java.util.Map;

public interface SpecificationService {

    public PageResult searchPage(Integer pageNum, Integer pageSize, Specification specification);

    void add(SpecificationVo specificationVo);

    SpecificationVo findOne(Long id);

    void update(SpecificationVo specificationVo);

    void delete(Long[] ids);

    List<Map> selectOptionList();

}
