package org.com.itemmanager.Util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class Tools {
    public static class HttpResponseContainer
    {
        public int responseCode;
        public String responseBody;

        public HttpResponseContainer(int rescode,String resbody)
        {
            responseCode = rescode;
            responseBody = resbody;
        }
    }
    public static String readToString(String fileName) {
        String encoding = "UTF-8";
        File file = new File(fileName);
        Long fileLength = file.length();
        byte[] fileContent = new byte[fileLength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(fileContent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        try {
            return new String(fileContent, encoding);
        } catch (UnsupportedEncodingException e) {
            System.err.println("The OS does not support " + encoding);
            e.printStackTrace();
            return null;
        }
    }

    public static void writeString(String fileName, String Data)
    {
        File file = new File(fileName);

        System.err.println("Write: "+Data);
        try {
            if(!file.exists())
            {
                file.createNewFile();
                System.err.println("File:"+file.getName()+" created.");
            }
            FileOutputStream out = new FileOutputStream(file,false);
            byte[] fileContent = Data.getBytes();
            out.write(fileContent);
            out.close();
            System.err.println("File:"+file.getName()+" wrote");
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public static HttpResponseContainer HttpRequest(String targethost, String requestBody)
    {
        HttpURLConnection connection = null;
        InputStream is = null;
        OutputStream os = null;
        BufferedReader br = null;
        String result = null;
        try
        {
            URL url = new URL(targethost);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(60000);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            os = connection.getOutputStream();
            os.write(requestBody.getBytes());
            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                is = connection.getInputStream();
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuffer sbf = new StringBuffer();
                String temp = null;
                while ((temp = br.readLine()) != null) {
                    sbf.append(temp);
                    sbf.append("\r\n");
                }
                result = sbf.toString();
                return new HttpResponseContainer(responseCode, result);
            }
            else
            {
               return  new HttpResponseContainer(responseCode, "");
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return  new HttpResponseContainer(-1, "");
        }
    }
}
