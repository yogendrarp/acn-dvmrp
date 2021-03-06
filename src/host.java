
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class host {
    static String outFile = "houtX.txt"; // X will be replaced by Id
    static String lanFile = "lanX.txt"; // X will be replaced by Id
    static String inFile = "hinX.txt";
    static int selfDestructInMs = 120000;

    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("Starting Host");
        if (args.length < 3) {
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
        if (type.equals("sender") && args.length == 5) {
            timeToStart = Integer.parseInt(args[3]);
            period = Integer.parseInt(args[4]);
        } else if (!type.equals("receiver")) {
            System.out.println("wrong code");
            Thread.sleep(10000);
            // System.exit(-1);
        }
        System.out.println(String.format("Host Id is %s and type is %s", hostId, type));

        if (type.equals("sender")) {
            manageSender(outFileName, lanId, timeToStart, period);
        } else if (type.equals("receiver")) {
            manageReceiver(outFileName, lanId, sendActiveReceiverMsgTimeInSec, lanFileName, checkForMsgTimeInSec, inFileName);
        }
        Timer selfDestruct = new Timer();
        selfDestruct.schedule(new TimerTask() {
            @Override
            public void run() {
                System.exit(-1);
            }
        }, selfDestructInMs, 1);
    }

    static void manageSender(String outFileName, String lanId, int timeToStart, int period) {
        Timer activeReceiverTimer = new Timer();
        activeReceiverTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                String receiverMsg = String.format("data %s %s%s", lanId, lanId, System.lineSeparator());
                writewithlocks writeWithLocks = null;
                try {
                    writeWithLocks = new writewithlocks(outFileName);
                    writeWithLocks.writeToFileWithLock(receiverMsg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, timeToStart * 1000, period * 1000);
    }

    static void manageReceiver(String outFileName, String lanId, int sendActiveReceiverMsgTimeInSec,
                               String lanFileName, int checkForMsgTimeInSec, String inFileName) throws IOException {
        Timer activeReceiverTimer = new Timer();
        activeReceiverTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                String receiverMsg = String.format("receiver %s%s", lanId, System.lineSeparator());
                writewithlocks writeWithLocks = null;
                try {
                    writeWithLocks = new writewithlocks(outFileName);
                    writeWithLocks.writeToFileWithLock(receiverMsg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, sendActiveReceiverMsgTimeInSec * 1000);
        Timer checkMessagesTimer = new Timer();
        dataread msg = new dataread();
        msg.seek = 0;
        msg.dataLines = new ArrayList<String>();

        checkMessagesTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                long previousSeek = msg.seek;
                dataread localmsg = null;
                try {
                    File _tmpFile = new File(lanFileName);
                    if (_tmpFile.exists()) {
                        readwithlocks readWithLocks = new readwithlocks(lanFileName);
                        localmsg = readWithLocks.readFromFile(previousSeek);
                        msg.dataLines.clear();
                        for (String line : localmsg.dataLines) {
                            if (!line.trim().isEmpty()) {
                                try {
                                    if (line.startsWith("data")) {
                                        FileWriter fw = new FileWriter(inFileName, true);
                                        fw.write(line + System.lineSeparator());
                                        fw.close();
                                    }
                                } catch (IOException e) {
                                    System.out.println(e.getMessage());
                                }
                            }
                        }
                        msg.seek = localmsg.seek;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }, 0, checkForMsgTimeInSec * 1000);
    }
}
