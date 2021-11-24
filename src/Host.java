
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Host {
    private static String outFile = "houtX.txt"; // X will be replaced by Id
    private static String lanFile = "lanX.txt"; // X will be replaced by Id
    private static String inFile = "hinX.txt";

    public static void main(String[] args) throws InterruptedException, IOException {
        if (args.length < 4) {
            System.out.println("Missing Arguments");
            System.exit(-1);
        }
        String hostId = args[0];
        String lanId = args[1];
        String type = args[2];
        String outFileName = outFile.replace("X", hostId);
        String lanFileName = lanFile.replace("X", lanId);
        String inFileName = inFile.replace("X", lanId);
        int timeToStart = 0, period = 0,
                sendActiveReceiverMsgTimeInSec = 10, checkForMsgTimeInSec = 1;
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
            manageReceiver(outFileName, lanId, sendActiveReceiverMsgTimeInSec, lanFileName, checkForMsgTimeInSec, inFileName);
        }
    }

    static void manageReceiver(String outFileName, String lanId, int sendActiveReceiverMsgTimeInSec,
                               String lanFileName, int checkForMsgTimeInSec, String inFileName) throws IOException {
        WriteWithLocks writeWithLocks = new WriteWithLocks(outFileName);
        Timer activeReceiverTimer = new Timer();
        activeReceiverTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                String receiverMsg = String.format("receiver %s%s", lanId, System.lineSeparator());
                writeWithLocks.writeToFileWithLock(receiverMsg);
            }
        }, 0, sendActiveReceiverMsgTimeInSec * 1000);

        Timer checkMessagesTimer = new Timer();
        ReadWithLocks readWithLocks = new ReadWithLocks(lanFileName);
        DataRead msg = new DataRead();
        msg.seek = 0;
        msg.dataLines = new ArrayList<String>();

        checkMessagesTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                long previousSeek = msg.seek;
                DataRead localmsg = readWithLocks.readFromFile(previousSeek);
                msg.dataLines.clear();
                for (String line : localmsg.dataLines) {
                    if (!line.trim().isBlank()) {
                        try {
                            FileWriter fw = new FileWriter(inFileName, true);
                            System.out.println(line);
                            fw.write(line + System.lineSeparator());
                            fw.close();
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }
                msg.seek = localmsg.seek;
            }
        }, 0, checkForMsgTimeInSec * 1000);

    }

    static String generateSenderMessage() {
        String message = "This is a message generated at time :" + new Date().toString();
        return message;
    }
}
