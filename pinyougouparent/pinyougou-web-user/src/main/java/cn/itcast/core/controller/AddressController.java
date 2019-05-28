package cn.itcast.core.controller;

import cn.itcast.core.pojo.address.Address;
import cn.itcast.core.pojo.address.Areas;
import cn.itcast.core.pojo.address.Cities;
import cn.itcast.core.pojo.address.Provinces;
import cn.itcast.core.service.AddressService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/address")
public class AddressController {

    @Reference
    private AddressService addressService;


    @RequestMapping("/findListByLoginUser")
    public List<Address> findListByLoginUser(){

        String name = SecurityContextHolder.getContext().getAuthentication().getName();

        return addressService.findListByLoginUser(name);
    }

    @RequestMapping("/findProvinces")
    public List<Provinces> findProvinces(){
        return addressService.findProvinces();
    }
    @RequestMapping("/findCities")
    public List<Cities> findCities(Long provinceId){
        return addressService.findCities(provinceId);
    }
    @RequestMapping("/findAreas")
    public List<Areas> findAreas(Long cityId){
        return addressService.findAreas(cityId);
    }

    @RequestMapping("/add")
    public Result add(@RequestBody Address address){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            addressService.add(address,name);
            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }
    }

    @RequestMapping("/delete")
    public Result delete(Long id){
        try {
            addressService.delete(id);
            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }
    }
}
