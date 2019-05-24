package cn.itcast.core.controller;

import entity.PageResult;
import entity.Result;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.service.BrandService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {

    @Reference
    private BrandService brandService;

    @RequestMapping("/findAll")
    public List<Brand> findAll(){
        List<Brand> brandList = brandService.findAll();

        return brandList;
    }

    @RequestMapping("/findPage")
    public PageResult findPage(Integer page,Integer rows){
        PageResult pageResult = brandService.findPage(page, rows);
        return pageResult;
    }
//    带条件的分页查询
    @RequestMapping("/search")
    public PageResult search(Integer page,Integer rows,@RequestBody Brand brand){
        System.out.println(brand);
        PageResult pageResult = brandService.findPageAndSearch(page,rows,brand);

        return pageResult;
    }

    @RequestMapping("/add")
    public Result add(@RequestBody Brand brand){

        try {
            brandService.add(brand);
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true,"添加失败");
        }
    }

    @RequestMapping("/findOne")
    public Brand findOne(Long id){
        return brandService.findOne(id);
    }

    @RequestMapping("/update")
    public Result update(@RequestBody Brand brand){
        try {
            brandService.update(brand);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true,"修改失败");
        }
    }

    @RequestMapping("/deletByIds")
    public Result deletByIds(Long[] ids){

        try {
            brandService.deletByIds(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true,"删除失败");
        }
    }

   @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
        return brandService.selectOptionList();
   }
}
