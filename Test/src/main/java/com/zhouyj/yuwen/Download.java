package com.zhouyj.yuwen;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class Download {

	String url = "http://www.kjzhan.com/dianzikeben/1s_yuwen_123.html";
	
	public static void main(String[] args) throws IOException {
		int start = 2;
		int end = 123;
		
		String prefix = "http://www.kjzhan.com/dianzikeben/1s_yuwen_";
		String sufix = ".html";
		
		for(int i=start;i<=end;i++){
			String url = prefix + i + sufix;
			String htmlContent = getContent(url);
			
			String keyword = "uploads/allimg";
			int x = htmlContent.indexOf(keyword);
			int y = htmlContent.indexOf( "jpg", x);
			String imgUrl = "http://www.kjzhan.com/" + htmlContent.substring(x,  y+3);
			System.out.println(imgUrl);
			String filename = "";
			if(i<10){
				filename = "00" + i + ".jpg";
			}else if(i<100){
				filename = "0" + i + ".jpg";
			}else{
				filename = i + ".jpg";
			}
			downLoadFromUrl(imgUrl, filename, "/Users/zhouyujuan/Desktop/1");
		}
	}
	
	public static String getContent(String urlStr){
		try {  
            // get URL content  
            URL url = new URL(urlStr);  
            URLConnection conn = url.openConnection();  
   
            // open the stream and put it into BufferedReader  
            BufferedReader br = new BufferedReader(  
                               new InputStreamReader(conn.getInputStream()));  
   
            StringBuffer content = new StringBuffer();
            String inputLine; 
   
            while ((inputLine = br.readLine()) != null) {  
            	content.append(inputLine); 
            }  
     
            br.close();  
            return content.toString();
   
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
		return "";
	}
	
	/** 
     * 从网络Url中下载文件 
     * @param urlStr 
     * @param fileName 
     * @param savePath 
     * @throws IOException 
     */  
    public static void  downLoadFromUrl(String urlStr,String fileName,String savePath) throws IOException{  
        URL url = new URL(urlStr);    
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();    
                //设置超时间为3秒  
        conn.setConnectTimeout(3*1000);  
        //防止屏蔽程序抓取而返回403错误  
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");  
  
        //得到输入流  
        InputStream inputStream = conn.getInputStream();    
        //获取自己数组  
        byte[] getData = readInputStream(inputStream);      
  
        //文件保存位置  
        File saveDir = new File(savePath);  
        if(!saveDir.exists()){  
            saveDir.mkdir();  
        }  
        File file = new File(saveDir+File.separator+fileName);      
        FileOutputStream fos = new FileOutputStream(file);       
        fos.write(getData);   
        if(fos!=null){  
            fos.close();    
        }  
        if(inputStream!=null){  
            inputStream.close();  
        }  
  
  
        System.out.println("info:"+url+" download success");   
  
    }  
  
  
  
    /** 
     * 从输入流中获取字节数组 
     * @param inputStream 
     * @return 
     * @throws IOException 
     */  
    public static  byte[] readInputStream(InputStream inputStream) throws IOException {    
        byte[] buffer = new byte[1024];    
        int len = 0;    
        ByteArrayOutputStream bos = new ByteArrayOutputStream();    
        while((len = inputStream.read(buffer)) != -1) {    
            bos.write(buffer, 0, len);    
        }    
        bos.close();    
        return bos.toByteArray();    
    }
}
