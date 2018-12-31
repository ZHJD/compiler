package com.zhj.scan;

import java.io.*;

public class FileUtils {
    public static void readToBuffer(StringBuffer buffer, String filePath){
        InputStream is = null;
        try {
            is = new FileInputStream(filePath);
            String line; // 用来保存每行读取的内容
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            line = reader.readLine(); // 读取第一行
            while (line != null) { // 如果 line 为空说明读完了
                buffer.append(line); // 将读到的内容添加到 buffer 中
                buffer.append("\n"); // 添加换行符
                line = reader.readLine(); // 读取下一行
            }
            reader.close();
            is.close();
        } catch (FileNotFoundException e) {
            System.out.println("找不到文件");
        } catch (IOException e) {
            System.out.println("读取异常");
        }

    }

    public static String readFile(String filePath){
        StringBuffer sb = new StringBuffer();
        FileUtils.readToBuffer(sb, filePath);
      //  System.out.println(sb.toString());
        return sb.toString();
    }

}
