import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;

public class ReadWithLocks {
    private static FileChannel fc;
    private static RandomAccessFile randomAccessFile;

    public ReadWithLocks(String fileName) throws FileNotFoundException {
        System.out.println(fileName);
        randomAccessFile = new RandomAccessFile(fileName, "r");
    }

    public DataRead readFromFile(long seekPosition) {
        fc = randomAccessFile.getChannel();
        DataRead dr = new DataRead();
        dr.dataLines = new ArrayList<String>();
        dr.seek = seekPosition;
        try (FileLock fileLock = fc.tryLock()) {
           /*
            if (fileLock != null) {
                randomAccessFile.seek(randomAccessFile.length());
                while (buffer.hasRemaining())
                    fc.read(buffer);
            }
            DataRead dr = new DataRead(buffer.toString(), randomAccessFile.length());
            return dr;*/
            randomAccessFile.seek(seekPosition);
            String tmp;
            while ((tmp = randomAccessFile.readLine()) != null) {
                dr.dataLines.add(tmp);
            }
            dr.seek = randomAccessFile.length();
            return dr;
        } catch (OverlappingFileLockException | IOException ex) {
            System.out.println(String.format("%s .. %s", "Error in writing with Locks", ex.getMessage()));
            return dr;
        }
    }
}
