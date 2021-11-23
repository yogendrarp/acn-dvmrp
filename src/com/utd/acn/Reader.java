package com.utd.acn;

import java.io.*;
import java.util.*;
import java.lang.*;

/*
 */
public class Reader {

    int count;
    
    /** Creates a new instance of Reader */
    public Reader() 
    {
    
        try
        {
                    String pathname = "SharedFile";
                    File SharedFile = new File(pathname);
                    FileWriter SFile = new FileWriter(SharedFile);
                    SFile.close();
                    count = 0;
                
            
        }
        catch(Exception e)
        {
            System.out.println(e + "in InitReader");
        }
    }
    
    /*========= read output files and write into input files ==========*/
    void readFile()
    {
        try
        {                              
                    String str = "SharedFile";
                    BufferedReader ReadFile = new BufferedReader(new FileReader(str));
                    int temp = 0;
                    while((str = ReadFile.readLine()) != null)
                    {
                        ++temp;
                        if(temp > count) /* new msg */
                        {
                                    String filePath = "WhatRead";
                                    BufferedWriter WriteFile = new BufferedWriter(new FileWriter(filePath,true));
                                    WriteFile.write(str);
                                    WriteFile.write("\n");
                                    WriteFile.close();                       
                        }
                   }
                   count = temp;
                   
        }
        catch(Exception e)
        {
            System.out.println(e + " in readFile()");
        }
    }
                  
           
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        try
        {
            Reader Rdr = new Reader(); 
            for(int i=1;i<10;++i)
            {
                Thread.sleep(10000);
                Rdr.readFile();
            }
        }
        catch(Exception e)
        {
            System.out.println(e + " in reader main()");
        }
    }
}
