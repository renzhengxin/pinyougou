package cn.itcast.core.service;

import cn.itcast.core.dao.address.AddressDao;
import cn.itcast.core.dao.address.AreasDao;
import cn.itcast.core.dao.address.CitiesDao;
import cn.itcast.core.dao.address.ProvincesDao;
import cn.itcast.core.pojo.address.*;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressDao addressDao;

    @Autowired
    private ProvincesDao provincesDao;

    @Autowired
    private CitiesDao citiesDao;

    @Autowired
    private AreasDao areasDao;

    @Override
    public List<Address> findListByLoginUser(String name) {
        AddressQuery addressQuery = new AddressQuery();
        addressQuery.createCriteria().andUserIdEqualTo(name);
        List<Address> addressList = addressDao.selectByExample(addressQuery);
        for (Address address : addressList) {
            if (address.getProvinceId() != null && !"".equals(address.getProvinceId())) {
                //省
                ProvincesQuery provincesQuery = new ProvincesQuery();
                provincesQuery.createCriteria().andProvinceidEqualTo(address.getProvinceId());
                List<Provinces> provinces = provincesDao.selectByExample(provincesQuery);
                String province = provinces.get(0).getProvince() +"";
                //市
                CitiesQuery citiesQuery = new CitiesQuery();
                citiesQuery.createCriteria().andCityidEqualTo(address.getCityId()).andProvinceidEqualTo(address.getProvinceId());
                List<Cities> cities = citiesDao.selectByExample(citiesQuery);
                String City = cities.get(0).getCity()+"";
                //县/区
                AreasQuery areasQuery = new AreasQuery();
                areasQuery.createCriteria().andAreaidEqualTo(address.getTownId()).andCityidEqualTo(address.getCityId());
                List<Areas> areas = areasDao.selectByExample(areasQuery);
                String Area = areas.get(0).getArea()+"";
                String ad = address.getAddress();
                String stringAddress = province + " " + City + " " + Area + " " + ad;
                address.setStringAddress(stringAddress);
            }
        }
        return addressList;

    }

    @Override
    public List<Provinces> findProvinces() {
        List<Provinces> provinces = provincesDao.selectByExample(null);
        return provinces;
    }

    @Override
    public List<Cities> findCities(Long provinceId) {
        CitiesQuery citiesQuery=new CitiesQuery();
        citiesQuery.createCriteria().andProvinceidEqualTo(String.valueOf(provinceId));
        List<Cities> cities = citiesDao.selectByExample(citiesQuery);
        return cities;
    }

    @Override
    public List<Areas> findAreas(Long cityId) {
        AreasQuery areasQuery=new AreasQuery();
        areasQuery.createCriteria().andCityidEqualTo(String.valueOf(cityId));
        List<Areas> areas = areasDao.selectByExample(areasQuery);
        return areas;
    }

    @Override
    public void add(Address address,String name) {
        address.setUserId(name);
        addressDao.insert(address);
    }

    @Override
    public void delete(Long id) {
        addressDao.deleteByPrimaryKey(id);
    }
}
