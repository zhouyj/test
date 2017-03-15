package com.zhouyj.yuwen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Pinyin {

	final static String shenmu = "b p m f d t n l g k h j q x zh ch sh r z c s y w";
	final static String yunmu = "a o e i u ü ai ei ui ao ou iu ie üe er an en in un ün ang eng ing ong";
	final static String zhengti = "zhi chi shi ri zi ci si yi wu yu ye yue yuan yin yun ying";
	final static String yinjie = "zhu xiao feng zhou juan ze lin wo jin tian shang xue qu ni hao peng you";
	
	public static void main(String[] args) {
		int count = 6; //每一种都是7
		List<String> result = new ArrayList<>();
		
		String[] shens = shenmu.split(" ");
		List<String> shenList = Arrays.asList(shens);
		Collections.shuffle(shenList);
		result.addAll(shenList.subList(0, count));
		
		String[] yuns = yunmu.split(" ");
		List<String> yunList = Arrays.asList(yuns);
		Collections.shuffle(yunList);
		result.addAll(yunList.subList(0, count));
		
		String[] zhens = zhengti.split(" ");
		List<String> zhenList = Arrays.asList(zhens);
		Collections.shuffle(zhenList);
		result.addAll(zhenList.subList(0, count));
		
		String[] yins = yinjie.split(" ");
		List<String> yinList = Arrays.asList(yins);
		Collections.shuffle(yinList);
		result.addAll(yinList.subList(0, count));
		
		Collections.shuffle(result);
		int i=0;
		for(String s:result){
			if(i%count==0){
				System.out.println("\n");
			}
			System.out.print(s + "\t");
			i++;
		}
	}
}
