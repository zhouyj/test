package com.feedss.user.model;

import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.feedss.user.entity.UsrProduct;

import lombok.Data;

/**
 * Created by qinqiang on 2016/8/2.
 */
@Data
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class ProductVo {

	 private String uuid;
	 private String name;
	 private String pic;
	 //private String type;
	 private int price;
	 private Date updated;
	 private int status;

     public static ProductVo product2Vo(UsrProduct product){
		 if(product==null){
			 return null;
		 }
		 ProductVo productVo = new ProductVo();
		 productVo.setUuid(product.getUuid());
		 productVo.setName(product.getName());
		 productVo.setPic(product.getPic());
		 ///productVo.setType(product.getType());
		 productVo.setPrice(product.getPrice());
		 productVo.setUpdated(product.getUpdated());
		 return productVo;
     }
}
