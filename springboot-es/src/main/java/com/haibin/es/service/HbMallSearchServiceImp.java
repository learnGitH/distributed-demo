package com.haibin.es.service;

import com.alibaba.fastjson.JSON;
import com.haibin.es.domain.EsProduct;
import com.haibin.es.vo.ESRequestParam;
import com.haibin.es.vo.ESResponseResult;
import io.searchbox.strings.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HbMallSearchServiceImp implements HbMallSearchService{

    @Qualifier("restHighLevelClient")
    @Autowired
    private RestHighLevelClient client;

    @Override
    public ESResponseResult search(ESRequestParam param) {

        try {
            //1、构建检索对象-封装请求相关参数信息
            SearchRequest searchRequest = startBuildRequestParam(param);

            //2、进行检索操作
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

            //3、分析响应数据，封装成指定的个刷
            ESResponseResult responseResult = startBuildResponseResult(response,param);
            return responseResult;
        }catch (Exception e) {
            log.error("search error",e);
        }
        return null;
    }

    /**
     * 封装请求参数信息
     * 关键字查询、根据属性、分类、品牌、价格区间、是否有库存等进行过滤、分页、高亮、以及聚合统计品牌分类属性
     *
     */
    private SearchRequest startBuildRequestParam(ESRequestParam param) {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        /**
         * 封装请求参数信息
         * 关键字查询、根据属性、分类、品牌、价格区间、是否有库存等进行过滤、分页、高亮、以及聚合统计品牌分类属性
         */
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        //1、查询关键字
        if (!StringUtils.isBlank(param.getKeyword())) {
            //单字段查询
            //boolQueryBuilder.must(QueryBuilders.matchQuery("name",param.getKeyword()));
            //多字段查询
            boolQueryBuilder.must(QueryBuilders.multiMatchQuery(param.getKeyword(),"name","keywords","subTitle"));

        }

        //2、根据类目ID进行过滤
        if (null != param.getCategoryId()) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("categoryId",param.getCategoryId()));
        }

        //3、根据品牌ID进行过滤
        if (null != param.getBrandId() && param.getBrandId().size() > 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId",param.getBrandId()));
        }

        //4、根据属性进行相关过滤
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            param.getAttrs().forEach(item -> {
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                String[] s = item.split("_");
                String attId = s[0];
                String[] attrValues = s[1].split(":");
                boolQuery.must(QueryBuilders.termQuery("attrs.attrId",attId));
                boolQuery.must(QueryBuilders.termsQuery("attrs.attrValue",attrValues));
                NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("attrs",boolQuery, ScoreMode.None);
                boolQueryBuilder.filter(nestedQueryBuilder);
            });
        }

        //5、是否有库存
        if (null != param.getHasStock()) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("hashStock",param.getHasStock() == 1));
        }

        //6、根据价格过滤
        if (!StringUtils.isBlank(param.getPrice())) {
            //价格的输入形式为：10-100（起始价格和最终价格）或-100（不指定起始价格）或10-（不限制最终价格）
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price");
            String[] price = param.getPrice().split("_");
            if (price.length == 2) {
                rangeQueryBuilder.gt(price[0]).lt(price[1]);
            }else if (price.length == 1) {
                if (param.getPrice().startsWith("_")) {
                    rangeQueryBuilder.lte(price[1]);
                }
                if (param.getPrice().endsWith("_")) {
                    rangeQueryBuilder.gte(price[0]);
                }
            }
            boolQueryBuilder.filter(rangeQueryBuilder);
        }

        //封装所有的查询条件
        searchSourceBuilder.query(boolQueryBuilder);

        /**
         * 实现排序、高亮、分页操作
         */

        //排序
        //页面传入的参数值形式 sort=price_asc/desc
        if (!StringUtils.isBlank(param.getSort())) {
            String sort = param.getSort();
            String[] sortFields = sort.split("_");
            System.out.println("sortFields:"+sortFields.length);
            if (!StringUtils.isBlank(sortFields[0])) {
                SortOrder sortOrder = "asc".equals(sortFields[1]) ? SortOrder.ASC : SortOrder.DESC;
                searchSourceBuilder.sort(sortFields[0],sortOrder);
            }
        }

        //分页查询
        searchSourceBuilder.from((param.getPageNum() - 1) * 10);
        searchSourceBuilder.size(10);

        //高亮显示
        if (!StringUtils.isBlank(param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("name");
            highlightBuilder.preTags("<b style=`color:red`>");
            highlightBuilder.postTags("</b>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }

        /**
         * 对品牌、分类信息、属性信息进行聚合分析
         */
        //1. 按照品牌进行聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);

        //1.1 品牌的子聚合-品牌名聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg"))
                .field("brandName").size(1);
        //1.2 品牌的子聚合-品牌图片聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg"))
                .field("brandImg").size(1);

        searchSourceBuilder.aggregation(brand_agg);

        //2. 安装风雷信息进行聚合
        TermsAggregationBuilder category_agg = AggregationBuilders.terms("category_agg");
        category_agg.field("categoryId").size(50);

        category_agg.subAggregation(AggregationBuilders.terms("category_name_agg").field("categoryName").size(1));

        searchSourceBuilder.aggregation(category_agg);

        //3，按照属性信息进行聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg","attrs");
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        attr_agg.subAggregation(attr_id_agg);
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        searchSourceBuilder.aggregation(attr_agg);

        System.out.println("构建的DSL语句{}：" + searchSourceBuilder.toString());

        SearchRequest searchRequest = new SearchRequest(new String[]{"product_db"},searchSourceBuilder);

        return searchRequest;
    }

    /**
     * 封装查询到的结果信息
     * 关键字查询、根据属性、分类、品牌、价格区间、是否有库存等进行过滤、分页、高亮、以及聚合统计品牌分类属性
     */
    private ESResponseResult startBuildResponseResult(SearchResponse response, ESRequestParam param) {

        ESResponseResult result = new ESResponseResult();

        //1、获取查询到的商品信息
        SearchHits hits = response.getHits();

        List<EsProduct> esModels = new ArrayList<>();
        //2、遍历所有商品信息
        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits) {
                String sourceAsString = hit.getSourceAsString();
                EsProduct esModel = JSON.parseObject(sourceAsString,EsProduct.class);
                if (!StringUtils.isBlank(param.getKeyword())) {
                    HighlightField name = hit.getHighlightFields().get("name");
                    String nameValue = name != null ? name.getFragments()[0].string() : esModel.getName();
                    esModel.setName(nameValue);
                }
                esModels.add(esModel);
            }
        }
        result.setProducts(esModels);

        //3、当前商品涉及到的所有品牌信息，小米手机和小米电脑都属于小米品牌，过滤重复品牌信息
        List<ESResponseResult.BrandVo> brandVos = new ArrayList<>();
        //获取到品牌的聚合
        ParsedLongTerms brandAgg = response.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brandAgg.getBuckets()) {
            ESResponseResult.BrandVo brandVo = new ESResponseResult.BrandVo();
            //获取品牌的id
            long brandId = bucket.getKeyAsNumber().longValue();
            brandVo.setBrandId(brandId);

            ParsedStringTerms brandNameAgg = bucket.getAggregations().get("brand_name_agg");
            String brandName = brandNameAgg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandName);

            ParsedStringTerms brandImgAgg = bucket.getAggregations().get("brand_img_agg");
            String brandImg = brandImgAgg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(brandImg);
            System.out.println("brandId:"+brandId + "brandName:" + brandName+"brandImg:" + brandImg);
            brandVos.add(brandVo);
        }
        System.out.println("brandVos.size:"+brandVos.size());
        result.setBrands(brandVos);

        //4、当前商品相关的所有类目信息
        //获取到分类的聚合
        List<ESResponseResult.categoryVo> categoryVos = new ArrayList<>();

        ParsedLongTerms categoryAgg = response.getAggregations().get("category_agg");

        for (Terms.Bucket bucket : categoryAgg.getBuckets()) {
            ESResponseResult.categoryVo categoryVo = new ESResponseResult.categoryVo();
            String keyAsString = bucket.getKeyAsString();
            categoryVo.setCategoryId(Long.parseLong(keyAsString));

            ParsedStringTerms categoryNameAgg = bucket.getAggregations().get("category_name_agg");
            String categoryName = categoryNameAgg.getBuckets().get(0).getKeyAsString();
            categoryVo.setCategoryName(categoryName);
            categoryVos.add(categoryVo);
        }
        result.setCategorys(categoryVos);

        //5、获取商品相关的所有属性信息
        List<ESResponseResult.AttrVo> attrVos = new ArrayList<>();
        //获取属性信息的聚合
        ParsedNested attrsAgg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attrIdAgg = attrsAgg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            ESResponseResult.AttrVo attrVo = new ESResponseResult.AttrVo();
            long attrId = bucket.getKeyAsNumber().longValue();
            attrVo.setAttrId(attrId);

            //获取属性的名字
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attr_name_agg");
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attrName);

            //获取属性的值
            ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attr_value_agg");
            List<String> attrValues = attrValueAgg.getBuckets().stream().map(item -> item.getKeyAsString()).collect(Collectors.toList());
            attrVo.setAttrValue(attrValues);
            attrVos.add(attrVo);
        }

        //6、进行分页操作
        result.setPageNum(param.getPageNum());
        long total = hits.getTotalHits().value;
        result.setTotal(total);

        int totalPages = (int) total % 10 == 0 ? (int) total / 10 : ((int)total / 10 + 1);
        result.setTotalPages(totalPages);

        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++){
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);

        return result;

    }

}
