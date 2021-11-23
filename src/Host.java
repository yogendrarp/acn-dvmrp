
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Host {
    private static String outFile = "houtX.txt"; // X will be replaced by Id
    private static String lanFile = "lanX.txt"; // X will be replaced by Id
    private static String inFile = "hinX.txt";
    private static String hostId;

    public static void main(String[] args) throws InterruptedException, FileNotFoundException {
        if (args.length < 4) {
            System.out.println("Missing Arguments");
            System.exit(-1);
        }
        String hostId = args[0];
        String lanId = args[1];
        String type = args[2];
        String outFileName = outFile.replace("X", hostId);
        String lanFileName = lanFile.replace("X", lanId);
        int timeToStart = 0, period = 0,
                sendActiveReceiverMsgTimeinSec = 10, checkForMsgTimeinSec = 1;
        if (type == "sender" && args.length == 5) {
            timeToStart = Integer.parseInt(args[3]);
            period = Integer.parseInt(args[4]);
            Thread.sleep(timeToStart * 1000);
        } else if (!type.equals("receiver")) {
            System.out.println(type + " " + "receiver");
            System.exit(-1);
        }
        if (type.equals("sender")) {
            WriteWithLocks writeWithLocks = new WriteWithLocks(outFileName);
            for (; ; ) {
                String receiverMsg = String.format("data %s%s", lanId, System.lineSeparator());
                writeWithLocks.writeToFileWithLock(receiverMsg);
                Thread.sleep(period * 1000);
            }
        } else if (type.equals("receiver")) {
            WriteWithLocks writeWithLocks = new WriteWithLocks(outFileName);
            Timer activeReceiverTimer = new Timer();
            activeReceiverTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    String receiverMsg = String.format("receiver %s%s", lanId, System.lineSeparator());
                    writeWithLocks.writeToFileWithLock(receiverMsg);
                }
            }, 0, sendActiveReceiverMsgTimeinSec * 1000);

            Timer checkMessagesTimer = new Timer();
            long previousSeek = 0;
            ReadWithLocks readWithLocks = new ReadWithLocks(lanFileName);
            checkMessagesTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    DataRead msg = readWithLocks.readFromFile(previousSeek);
                    for (String line : msg.dataLines
                    ) {
                        System.out.println(line);
                    }
                }
            }, 0, checkForMsgTimeinSec * 1000);

        }
    }


    static String generateSenderMessage() {
        String message = "This is a message generated at time :" + new Date().toString();
        return message;
    }
}
