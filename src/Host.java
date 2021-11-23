
import java.io.FileNotFoundException;
import java.util.Date;

public class Host {
    private static String outFile = "houtX.txt"; // X will be replaced by Id
    private static String lanFile = "lanX.txt";
    private static String lanFileName;
    private static String hostId;

    public static void main(String[] args) throws InterruptedException, FileNotFoundException {
        if (args.length < 4) {
            System.out.println("Missing Arguments");
            System.exit(-1);
        }
        String hostId = args[0];
        String lanId = args[1];
        String type = args[2];
        String lanFileName = lanFile.replace("X", lanId);
        String outFileName = outFile.replace("X", hostId);
        int timeToStart = 0, period = 0, fixedTimeInSec = 10;
        if (type == "sender" && args.length == 5) {
            timeToStart = Integer.parseInt(args[3]);
            period = Integer.parseInt(args[4]);
            Thread.sleep(timeToStart * 1000);
        } else if (!type.equals("receiver")) {
            System.out.println(type + " " + "receiver");
            System.exit(-1);
        }
        if (type.equals("sender")) {
            for (; ; ) {
                System.out.println("Sending a message " + generateSenderMessage());
                Thread.sleep(period * 1000);
            }
        } else if (type.equals("receiver")) {
            WriteWithLocks writeWithLocks = new WriteWithLocks(lanFileName);
            for (int i = 0; i < 20; i++) {
                String receiverMsg = String.format("I am a receiver with host id %s, at time :%s%s", hostId, new Date().toString(), System.lineSeparator());
                writeWithLocks.writeToFileWithLock(receiverMsg);
                Thread.sleep(fixedTimeInSec * 1000);
            }
        }
    }

    static String generateSenderMessage() {
        String message = "This is a message generated at time :" + new Date().toString();
        return message;
    }
}
