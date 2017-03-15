package com.zhouyj;


public class Equation{
	public int x;
	public int y;
	public int result;
	public String op;
	
	public Equation(int x, int y, String op, int result){
		this.x=x;
		this.y = y;
		this.op = op;
		this.result = result;
	}
	
	public String toString(){
//		return x + " " + op + " " + y + " = " + result;
		return x + " " + op + " " + y + " = ";
	}
	
	public static void main(String[] args) {
		String s = "asdftest123";
		s = s.replaceAll("test", "laozhou");
		System.out.println(s);
	}
	
}