
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

public class WriteWithLocks {
    private static FileChannel fc;
    private static RandomAccessFile randomAccessFile;

    public WriteWithLocks(String fileName) throws FileNotFoundException {
        randomAccessFile = new RandomAccessFile(fileName, "rw");
    }

    public void writeToFileWithLock(String data) throws IOException {
        ByteBuffer buffer = null;
        fc = randomAccessFile.getChannel();

        try (FileLock fileLock = fc.tryLock()) {
            if (fileLock != null) {

                buffer = ByteBuffer.wrap(data.getBytes());
                buffer.put(data.getBytes());
                buffer.flip();
                randomAccessFile.seek(randomAccessFile.length());
                while (buffer.hasRemaining())
                    fc.write(buffer);
            }
        } catch (OverlappingFileLockException | IOException ex) {
            System.out.println(String.format("%s .. %s", "Error in writing with Locks", ex.getMessage()));
        } finally {
            fc.close();
            randomAccessFile.close();
        }
    }
}
