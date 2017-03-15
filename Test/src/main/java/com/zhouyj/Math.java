package com.zhouyj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Math {

	public static void main(String[] args) {
		
		
		int column = 2;
		int min = 10;
		int max = 20;
		List<Equation> addlist = new ArrayList<>();
		List<Equation> sublist = new ArrayList<>();
		
		for(int i=1;i<=max;i++){
			for(int j=1;j<=max;j++){
				Equation add = new Equation(i, j, "+", (i+j));
				Equation sub = new Equation(i, j, "-", (i-j));
				if(i+j<=max && i+j > min && i<min && j<min){
					addlist.add(add);
				}
//				if(i-j>0){
//					
//					if(i>10 && (i-10)<j || i==20) continue;
//					sublist.add(sub);
//				}
				
			}
		}
		List<Equation> list = new ArrayList<>();
		list.addAll(addlist);
		list.addAll(sublist);
		
//		Collections.shuffle(list);
		
		int x = 0;
		for(Equation e:list){
			if(x%column==0){
				System.out.print(e.toString());
			}else if(x%column==(column-1)){
				System.out.print("\t" + e.toString() + "\n");
			}else {
				System.out.print("\t" + e.toString());
			}
			x++;
		}
//		for(Equation e:sublist){
//			if(x%column==0){
//				System.out.println(e.toString());
//			}else if(x%column==(column-1)){
//				System.out.print("\t" + e.toString() + "\n");
//			}else {
//				System.out.print("\t" + e.toString());
//			}
//			x++;
//		}
	}
	
}
