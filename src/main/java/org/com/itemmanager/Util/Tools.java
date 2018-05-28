package org.com.itemmanager.Util;

import java.io.*;

public class Tools {
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
}
