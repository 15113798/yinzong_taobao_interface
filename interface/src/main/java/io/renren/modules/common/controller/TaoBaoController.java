package io.renren.modules.common.controller;

import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.TbkShopRecommendGetRequest;
import com.taobao.api.response.TbkShopRecommendGetResponse;

import io.renren.common.utils.R;
import io.renren.common.utils.UrlToMapUtil;
import io.renren.modules.common.entity.ReNTbkItem;
import io.renren.modules.common.entity.ReShopItem;
import io.renren.modules.common.service.TaoBaoService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.*;



@RestController
@RequestMapping("/taobao")
public class TaoBaoController {

    protected static final String TBK_SHOP_FIELDS = "user_id,shop_title,shop_type,seller_nick,pict_url,shop_url";

    @Autowired
    private TaoBaoService taoBaoService;

    /**
     * 获取商品信息
     */
    @GetMapping("/getGoods")
    public R getAjaxGoods(String url) {
        List<ReNTbkItem> data = taoBaoService.getGoodsInfo(url);
        Map<String, Object> map = new HashMap<String, Object>();

        //简单校验url
        if ( data == null || data.isEmpty() || data.size() <= 0) {
            return R.error("请输入正确的商品链接地址！");
        }else if( data.size() == 1){
            ReNTbkItem item = (ReNTbkItem)data.get(0);
            map.put("errCode", item.getErrCode());
            map.put("errMsg", item.getErrMsg());
            map.put("data", item);
            return R.ok(map);
        }
        map.put("errCode", 200);
        map.put("errMsg", null);
        map.put("data", data);
        return R.ok(map);
    }



    /**
     * 获取店铺信息
     */
    @GetMapping("/getShop")
    public R getShop(String url){
        TaobaoClient client = new DefaultTaobaoClient("https://eco.taobao.com/router/rest", "25045938", "89c5d2fcc294d8bdcf0d7862373dff44");
        TbkShopRecommendGetRequest req = new TbkShopRecommendGetRequest();
        req.setFields(TBK_SHOP_FIELDS);
        Map<String, Object> map = new HashMap<String, Object>();

        Map<String, Objects>paramsMap = UrlToMapUtil.getUrlParams(url);
        Long user_number_id  = MapUtils.getLong(paramsMap, "user_number_id", 0L);
        req.setUserId(user_number_id);
        TbkShopRecommendGetResponse rsp = null;
        try {
            rsp = client.execute(req);
            List<ReShopItem> list = new ArrayList<>();
            if (rsp.isSuccess()) {
                JSONObject obj = JSONObject.fromObject(rsp.getBody());
                if(obj.has("tbk_shop_recommend_get_response")){
                    JSONObject obj1 = JSONObject.fromObject(obj.getString("tbk_shop_recommend_get_response"));
                    JSONObject obj2 = JSONObject.fromObject(obj1.getString("results"));
                    if(obj2 == null || obj2.size() == 0){
                        map.put("errCode", 500);
                        map.put("errMsg", "未查询到商铺");
                        map.put("data", null);
                        return R.ok(map);
                    }
                    JSONArray obj3 = obj2.getJSONArray("n_tbk_shop");
                    String[] fieldArr = TBK_SHOP_FIELDS.split(",",0);
                    for(int i=0; i< obj3.size();i++){
                        JSONObject obj4 = obj3.getJSONObject(i);
                        //num_iid,title,,small_images,reserve_price,,,,
                        //item_url,,,,,,,
                        ReShopItem item = new ReShopItem();
                        for(String field:fieldArr){
                            switch( field ){
                                case "user_id":
                                    item.setUser_id(obj4.getString(field));
                                    break;
                                case "shop_title":
                                    item.setShop_title(obj4.getString(field));
                                    break;
                                case "shop_type":
                                    item.setShop_type(obj4.getString(field));
                                    break;
                                case "seller_nick":
                                    item.setSeller_nick(obj4.getString(field));
                                    break;
                                case "pict_url":
                                    item.setPict_url(obj4.getString(field));
                                    break;
                                case "shop_url":
                                    item.setShop_url(obj4.getString(field));
                                    break;
                            }
                        }
                        list.add(item);
                    }
                    map.put("errCode", 200);
                    map.put("errMsg", null);
                    map.put("data", list);
                    return R.ok(map);
                }else{
                    System.out.println(rsp.getBody());
                    JSONObject obj1 = JSONObject.fromObject(obj.getString("error_response"));
                    ReShopItem item = new ReShopItem();
                    item.setErrCode("500");
                    if( obj1.has("msg") ){
                        item.setErrMsg(obj1.getString("msg"));
                        map.put("errMsg", obj1.getString("msg"));
                    }else if( obj1.has("sub_msg") ){
                        item.setErrMsg(obj1.getString("sub_msg"));
                        map.put("errMsg", obj1.getString("sub_msg"));
                    }else{
                        item.setErrMsg("未知异常，请联系管理员");
                        map.put("errMsg", null);
                    }
                    map.put("errCode", 500);
                    map.put("data", item);
                    return R.ok(map);
                }
            }
        } catch (ApiException e) {
            map.put("errCode", 500);
            map.put("errMsg", "API调用异常");
            map.put("data", null);
            return R.ok(map);
        }
        return  R.error("未知异常，请联系管理员");
    }





    /*
        获取
     */








}
