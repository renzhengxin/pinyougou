package cn.itcast.core.service;

import cn.itcast.core.pojo.item.Item;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.*;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {

        Map<String, Object> resultMap = searchHighLight(searchMap);
        //关键字空格处理
        searchMap.put("keywords",searchMap.get("keywords").replaceAll(" ",""));
//        categoryList
        ArrayList<String> categoryList = searchCategoryList(searchMap);
        resultMap.put("categoryList", categoryList);
        if (null!=categoryList&&categoryList.size()>0){

        Map<String, Object> searchBrandAndSpecList = searchBrandAndSpecList(categoryList.get(0));
        resultMap.putAll(searchBrandAndSpecList);
        }


        return resultMap;
    }


    //查询品牌数据和规格列表数据
    private Map<String,Object> searchBrandAndSpecList(String categoryName){

        Map<String,Object> map=new HashMap<>();

        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(categoryName);

        List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);

        List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);

        map.put("brandList", brandList);
        map.put("specList", specList);

        return map;
    }



    //商品分类
    private ArrayList<String> searchCategoryList(Map<String, String> searchMap){
        //	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{},
        // 'price':'','pageNo':1,'pageSize':40,'sort':'','sortField':''};
        ArrayList<String> arrayList=new ArrayList<>();

        Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
        Query query=new SimpleQuery(criteria);
        GroupOptions groupOptions=new GroupOptions();
        groupOptions.addGroupByField("item_category");
        query.setGroupOptions(groupOptions);


        GroupPage<Item> itemGroupPage = solrTemplate.queryForGroupPage(query, Item.class);
        GroupResult<Item> item_category = itemGroupPage.getGroupResult("item_category");
        Page<GroupEntry<Item>> groupEntries = item_category.getGroupEntries();
        List<GroupEntry<Item>> content = groupEntries.getContent();

        for (GroupEntry<Item> itemGroupEntry : content) {
            arrayList.add(itemGroupEntry.getGroupValue());
        }



        return arrayList;
    }




    public Map<String, Object> searchHighLight(Map<String, String> searchMap){
        Map<String,Object> resultMap=new HashMap<>();
        //	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{},
        // 'price':'','pageNo':1,'pageSize':40,'sort':'','sortField':''};
//        关键字查询
        Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
        SimpleHighlightQuery highlightQuery=new SimpleHighlightQuery(criteria);


        if (null!=searchMap.get("category")&&!"".equals(searchMap.get("category"))){
            Criteria filterCriteria=new Criteria("item_category").is(searchMap.get("category"));
            FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
            highlightQuery.addFilterQuery(filterQuery);
        }

        if (null!=searchMap.get("brand")&&!"".equals(searchMap.get("brand"))){
            Criteria filterCriteria=new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
            highlightQuery.addFilterQuery(filterQuery);

        }

        if (null!=searchMap.get("price")&&!"".equals(searchMap.get("price"))){
            String[] prices = searchMap.get("price").split("-");
            FilterQuery filterQuery=new SimpleFilterQuery();
            if ("*".equals(prices[1])){
                filterQuery=new SimpleFilterQuery(new Criteria("item_price").greaterThanEqual(prices[0]));
            }else {
                filterQuery=new SimpleFilterQuery(new Criteria("item_price").between(prices[0], prices[1],true ,false ));
            }
            highlightQuery.addFilterQuery(filterQuery);
        }

        if(null != searchMap.get("spec") && !"".equals(searchMap.get("spec"))){
            Map<String,String > specMap = JSON.parseObject(searchMap.get("spec"), Map.class);
            Set<Map.Entry<String, String>> entrySet = specMap.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                FilterQuery filterQuery = new SimpleFilterQuery();
                filterQuery.addCriteria(new Criteria("item_spec_" + entry.getKey()).is(entry.getValue()));
                highlightQuery.addFilterQuery(filterQuery);
            }

        }

            if(null != searchMap.get("sort") && !"".equals(searchMap.get("sort"))){
            if ("DESC".equals(searchMap.get("sort"))){
                highlightQuery.addSort(new Sort(Sort.Direction.DESC,"item_"+searchMap.get("sortField")));
            }else {
                highlightQuery.addSort(new Sort(Sort.Direction.ASC,"item_"+searchMap.get("sortField")));

            }

        }



        Integer pageNo = Integer.parseInt(searchMap.get("pageNo"));
        Integer pageSize = Integer.parseInt(searchMap.get("pageSize"));

        HighlightOptions highlightOptions =new HighlightOptions();
        highlightOptions.addField("item_title");
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        highlightOptions.setSimplePostfix("</em>");


        highlightQuery.setHighlightOptions(highlightOptions);
        highlightQuery.setOffset((pageNo-1)*pageSize);
        highlightQuery.setRows(pageSize);

        HighlightPage<Item> page = solrTemplate.queryForHighlightPage(highlightQuery,Item.class);

        List<Item> content = page.getContent();

        List<HighlightEntry<Item>> highlighted = page.getHighlighted();
        for (HighlightEntry<Item> highlightEntry : highlighted) {
            Item item = highlightEntry.getEntity();
            if (null!=highlightEntry&&highlightEntry.getHighlights().size()>0){
                item.setTitle(highlightEntry.getHighlights().get(0).getSnipplets().get(0));
            }

        }

        //totalPages
        resultMap.put("totalPages", page.getTotalPages());
        resultMap.put("rows", content);
        resultMap.put("total", page.getTotalElements());




        return resultMap;
    }



    public Map<String, Object> searchPUTONG(Map<String, String> searchMap){
        Map<String,Object> resultMap=new HashMap<>();
        //	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sort':'','sortField':''};
        Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));


        SimpleQuery query=new SimpleQuery(criteria);
        Integer pageNo = Integer.parseInt(searchMap.get("pageNo"));
        Integer pageSize = Integer.parseInt(searchMap.get("pageSize"));

        query.setOffset((pageNo-1)*pageSize);
        query.setRows(pageSize);
        ScoredPage<Item> page = solrTemplate.queryForPage(query, Item.class);
        List<Item> content = page.getContent();

        //totalPages
        resultMap.put("totalPages", page.getTotalPages());
        resultMap.put("rows", content);

        return resultMap;
    }
}
