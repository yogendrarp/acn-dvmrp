import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;

public class readwithlocks {
    private static FileChannel fc;
    private static RandomAccessFile randomAccessFile;

    public readwithlocks(String fileName) throws FileNotFoundException {
        randomAccessFile = new RandomAccessFile(fileName, "r");
    }

    public dataread readFromFile(long seekPosition) throws IOException {
        fc = randomAccessFile.getChannel();
        dataread dr = new dataread();
        dr.dataLines = new ArrayList<String>();
        dr.seek = seekPosition;
        try {
            randomAccessFile.seek(seekPosition);
            String tmp;
            while ((tmp = randomAccessFile.readLine()) != null) {
                dr.dataLines.add(tmp);
            }
            dr.seek = randomAccessFile.length();
            return dr;
        } catch (OverlappingFileLockException | IOException ex) {
            //System.out.println(String.format("%s .. %s", "Error in writing with Locks", ex.getMessage()));
            return dr;
        } finally {
            fc.close();
            randomAccessFile.close();
        }
    }
}
