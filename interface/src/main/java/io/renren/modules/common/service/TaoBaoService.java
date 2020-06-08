package io.renren.modules.common.service;


import io.renren.common.utils.UrlToMapUtil;
import io.renren.modules.common.entity.ReNTbkItem;
import io.renren.modules.common.manage.TaobaoAPIManager;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TaoBaoService {
    private TaobaoAPIManager tAPIManager = new TaobaoAPIManager();

    public List<ReNTbkItem> getGoodsInfo(String Url){
        Url= StringEscapeUtils.unescapeHtml(Url);
        String[] path = Url.split("&")[0].split("=",0);
        // data get
        List<ReNTbkItem> result = new ArrayList<ReNTbkItem>();
        ReNTbkItem errItem = new ReNTbkItem();
        errItem.setErrCode("500");
        try {
            if(path.length <= 1){
                errItem.setErrMsg("请输入正确的链接地址!");
                result.add(errItem);
                return result;
            }
            //在不同的平台，传上来的url都不同。部分url是调用接口找不到商品的。最原始的为https://item.taobao.com/item.htm?id=575726002555
            Map<String,Objects>paramsMap = UrlToMapUtil.getUrlParams(Url);
            String ids = String.valueOf(paramsMap.get("id"));
            result = tAPIManager.getTbkItemInfoForWeb(ids);
            if( result != null && result.size() > 0 ){
                ReNTbkItem item = result.get(0);
                if( item.getErrCode() != null && item.getErrCode().equals("50") ){
                    result.clear();
                    errItem.setErrCode("501");
                    errItem.setErrMsg(item.getErrMsg());
                    result.add(errItem);
                }else{
                    // 判断淘宝或者是天猫
                    item.setProduct_type(checkProduct(path[0].toString()));
                    item.setErrCode("200");
                    result.set(0, item);
                }
            }

        } catch (Exception e) {
            errItem.setErrCode("501");
            errItem.setErrMsg("服务器繁忙，请稍等！！");
            result.add(errItem);
        }
        return result;
    }


    public String checkProduct(String link){

        if( link.indexOf("taobao")  != -1 ){
            return "1";
        }else {
            if( link.indexOf("tmall") != -1 ){
                return "2";
            }
            return "0";
        }
    }
}
