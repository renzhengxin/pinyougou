package cn.itcast.core.service;

import cn.itcast.core.pojo.address.Address;
import cn.itcast.core.pojo.address.Areas;
import cn.itcast.core.pojo.address.Cities;
import cn.itcast.core.pojo.address.Provinces;

import java.util.List;

public interface AddressService {
    List<Address> findListByLoginUser(String name);

    List<Provinces> findProvinces();

    List<Cities> findCities(Long provinceId);

    List<Areas> findAreas(Long cityId);

    void add(Address address,String name);

    void delete(Long id);
}
