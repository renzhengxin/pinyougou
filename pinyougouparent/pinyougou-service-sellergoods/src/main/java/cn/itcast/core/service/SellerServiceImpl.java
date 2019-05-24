package cn.itcast.core.service;

import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.pojo.seller.SellerQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class SellerServiceImpl implements SellerService {

    @Autowired
    SellerDao sellerDao;

    @Override
    public void add(Seller seller) {
        sellerDao.insertSelective(seller);
    }

    @Override
    public Seller findOne(String username) {
        return sellerDao.selectByPrimaryKey(username);
    }

    @Override
    public PageResult search(Integer page, Integer rows, Seller seller) {

        PageHelper.startPage(page, rows);
        SellerQuery query=new SellerQuery();
        query.createCriteria().andStatusEqualTo(seller.getStatus());
        Page<Seller> sellerPage= (Page<Seller>) sellerDao.selectByExample(query);


        return new PageResult(sellerPage.getTotal(),sellerPage.getResult());
    }

    @Override
    public void updateStatus(String id, String status) {
        Seller seller=new Seller();
        seller.setSellerId(id);
        seller.setStatus(status);
        sellerDao.updateByPrimaryKeySelective(seller);
    }
}
