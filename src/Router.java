import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Router {
    static String lanOutFile = "lanX.txt";
    private static int selfDestructInMs = 120000;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Need at the least one LAN attached to the router");
            System.exit(-1);
        }
        int routerId = Integer.parseInt(args[0]);
        int[] lanIds = new int[args.length - 1];
        for (int i = 1; i < args.length; i++) {
            lanIds[i - 1] = Integer.parseInt(args[i]);
        }
        long checkMessagesTimerInMs = 1000, dvmrpTimersInMs = 5000;
        manageDvmrp(routerId, lanIds, dvmrpTimersInMs);
        readLanOutFiles(routerId, lanIds, checkMessagesTimerInMs);
        Timer selfDestruct = new Timer();
        selfDestruct.schedule(new TimerTask() {
            @Override
            public void run() {
                System.exit(-1);
            }
        }, selfDestructInMs, 1);
    }


    private static void readLanOutFiles(int routerId, int[] lanIds, long checkMessagesTimerInMs) {
        Timer checkMessagesTimer = new Timer();
        DataRead[] msgs = new DataRead[lanIds.length];

        for (int i = 0; i < msgs.length; i++) {
            DataRead msg = new DataRead();
            msg.seek = 0;
            msg.dataLines = new ArrayList<String>();
            msgs[i] = msg;
        }

        checkMessagesTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < lanIds.length; i++) {
                        long previousSeek = msgs[i].seek;
                        DataRead localmsg = null;
                        String lanFileName = lanOutFile.replace("X", lanIds[i] + "");
                        ReadWithLocks readWithLocks = new ReadWithLocks(lanFileName);
                        localmsg = readWithLocks.readFromFile(previousSeek);
                        msgs[i].dataLines.clear();
                        for (String line : localmsg.dataLines) {
                            if (!line.trim().isBlank()) {
                                try {
                                    System.out.println(line);
                                } catch (Exception e) {
                                    System.out.println(e.getMessage());
                                }
                            }
                        }
                        msgs[i].seek = localmsg.seek;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }, 0, checkMessagesTimerInMs);
    }

    private static void manageDvmrp(int routerId, int[] lanIds, long dvmrpTimersInMs) {
    }
}
