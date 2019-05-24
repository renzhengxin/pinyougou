package cn.itcast.core.service;

import cn.itcast.core.dao.specification.SpecificationDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.specification.SpecificationQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import vo.SpecificationVo;

import java.util.List;
import java.util.Map;

@Service
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    private SpecificationDao specificationDao;
    @Autowired
    private SpecificationOptionDao specificationOptionDao;

    @Override
    public PageResult searchPage(Integer pageNum, Integer pageSize, Specification specification) {
        PageHelper.startPage(pageNum, pageSize);
        SpecificationQuery specificationQuery=new SpecificationQuery();
        if (specification!=null){
            if (specification.getSpecName()!=null&&!"".equals(specification.getSpecName().trim())){

                SpecificationQuery.Criteria criteria = specificationQuery.createCriteria();
                criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
            }

        }

        Page<Specification> page= (Page<Specification>) specificationDao.selectByExample(specificationQuery);
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public void add(SpecificationVo specificationVo) {

        if (specificationVo!=null){
            specificationDao.insertSelective(specificationVo.getSpecification());

            List<SpecificationOption> specificationOptionList = specificationVo.getSpecificationOptionList();

            for (SpecificationOption specificationOption : specificationOptionList) {
                specificationOption.setSpecId(specificationVo.getSpecification().getId());
                specificationOptionDao.insertSelective(specificationOption);
            }


        }




    }

    @Override
    public SpecificationVo findOne(Long id) {


        Specification specification = specificationDao.selectByPrimaryKey(id);
        SpecificationOptionQuery query=new SpecificationOptionQuery();
        query.createCriteria().andSpecIdEqualTo(specification.getId());
        List<SpecificationOption> specificationOptionList = specificationOptionDao.selectByExample(query);

        return new SpecificationVo(specification,specificationOptionList);
    }

    @Override
    public void update(SpecificationVo specificationVo) {
        specificationDao.updateByPrimaryKeySelective(specificationVo.getSpecification());

        SpecificationOptionQuery query=new SpecificationOptionQuery();
        query.createCriteria().andSpecIdEqualTo(specificationVo.getSpecification().getId());
        specificationOptionDao.deleteByExample(query);

        List<SpecificationOption> specificationOptionList = specificationVo.getSpecificationOptionList();
        for (SpecificationOption specificationOption : specificationOptionList) {
            specificationOption.setSpecId(specificationVo.getSpecification().getId());
            specificationOptionDao.insertSelective(specificationOption);

        }



    }

    @Override
    public void delete(Long[] ids) {

        for (Long id : ids) {
            specificationDao.deleteByPrimaryKey(id);
            SpecificationOptionQuery query=new SpecificationOptionQuery();
            query.createCriteria().andIdEqualTo(id);
            specificationOptionDao.deleteByExample(query);
        }

    }

    @Override
    public List<Map> selectOptionList() {


        return specificationDao.selectOptionList();
    }
}
