package cn.itcast.core.controller;

import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.service.TypeTemplateService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("typeTemplate")
public class TypeTemplateController {

    @Reference
    TypeTemplateService typeTemplateService;


    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows, @RequestBody TypeTemplate typeTemplate){
        PageResult pageResult=typeTemplateService.search(page,rows,typeTemplate);
        return pageResult;
    }


    @RequestMapping("/findOne")
    public TypeTemplate findOne(Long id){
        TypeTemplate typeTemplate = typeTemplateService.findOne(id);
        System.out.println(typeTemplate);
        return typeTemplate;
    }

   @RequestMapping("/findBySpecList")
    public List<Map> findBySpecList(Long id){
        return typeTemplateService.findBySpecList(id);
   }


}
