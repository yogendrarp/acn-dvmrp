import java.io.*;
import java.lang.*;

public class Writer {

    int count;
    
    /** Creates a new instance of writer */
    public Writer() {
    
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
            System.out.println(e + "in writer");
        }
    }
    
    /*========= read output files and write into input files ==========*/
    void writeFile()
    {
        try
        {                              
                    String str = "Count " + count;
                                    String filePath = "SharedFile";
                                    BufferedWriter WriteFile = new BufferedWriter(new FileWriter(filePath,true));
                                    WriteFile.write(str);
                                    WriteFile.write("\n");
                                    WriteFile.close();                       
                   count++;
                   
        }
        catch(Exception e)
        {
            System.out.println(e + " in writeFile()");
        }
    }
                  
           
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        try
        {
            Writer wtr = new Writer();
            for(int i=1;i<10;++i)
            {
                Thread.sleep(10000);
                wtr.writeFile();
            }
        }
        catch(Exception e)
        {
            System.out.println(e + " in writer main()");
        }
    }
}
