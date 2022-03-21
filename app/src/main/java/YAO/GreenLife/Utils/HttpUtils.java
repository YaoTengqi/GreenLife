package YAO.GreenLife.Utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtils {
    /**
    * @Description: 封装好的http网络连接工具类
    * @Param: [urlStr] 为springboot所要求访问的RequestMapping网址
    * @return: java.lang.String
    * @Author: YAO
    * @Date: 2022/3/6
    */

    public static String gethttpresult(String urlStr) {
        try {
            URL url = new URL(urlStr);//获取url对象
            HttpURLConnection connect = (HttpURLConnection) url.openConnection();//url对象进行http连接
            InputStream input = connect.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            String line = null;
            System.out.println(connect.getResponseCode());
            StringBuffer sb = new StringBuffer();
            while ((line = in.readLine()) != null) {
                sb.append(line);//逐行读取传来的String
            }
            return sb.toString();
        } catch (Exception e) {
            System.out.println(e.toString());
            return null;
        }
    }
}