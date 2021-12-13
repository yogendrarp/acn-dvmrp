import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Router {
    static String lanOutFile = "lanX.txt";
    private static int selfDestructInMs = 120000;
    private static final int MAX = 10;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Need at the least one LAN attached to the router");
            System.exit(-1);
        }
        int routerId = Integer.parseInt(args[0]);


        ArrayList<Integer> attachedLanIds = new ArrayList<Integer>(args.length - 1);
        ArrayList<Lan> allLans = new ArrayList<>(MAX);

        for (int i = 1; i < args.length; i++) {
            attachedLanIds.add(Integer.parseInt(args[i]));
        }

        for (int i = 0; i < MAX; i++) {
            Lan lan = new Lan();
            lan.id = i;
            lan.dist = 10;//MAX
            lan.seek = 0;//file seek
            if (attachedLanIds.contains(i)) {
                lan.dist = 0;
                lan.nextHop = routerId;
                lan.attached = true;
            }
        }

        long checkMessagesTimerInMs = 1000, dvmrpTimersInMs = 5000;

        readAllAttachedLans(allLans, routerId, checkMessagesTimerInMs);


        Timer selfDestruct = new Timer();
        selfDestruct.schedule(new TimerTask() {
            @Override
            public void run() {
                System.exit(-1);
            }
        }, selfDestructInMs, 1);
    }

    private static void readAllAttachedLans(ArrayList<Lan> allLans, int routerId, long checkMessagesTimerInMs) {
        Timer readLanTimer = new Timer();

        readLanTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (Lan _lan : allLans) {
                    if (_lan.attached) {
                        try {
                            String lanFileName = lanOutFile.replace("X", _lan.id + "");
                            DataRead localmsg = null;
                            ReadWithLocks readWithLocks = new ReadWithLocks(lanFileName);
                            localmsg = readWithLocks.readFromFile(_lan.seek);
                            for (String line : localmsg.dataLines) {
                                if (!line.trim().isBlank()) {
                                    try {
                                        String[] content = line.split(" ");
                                        if (content[0].toUpperCase(Locale.ROOT).equals("DV")) {
                                            //manage Distance Vectors
                                        } else if (content[0].toUpperCase(Locale.ROOT).equals("RECEIVER")) {
                                            //There is a receiver on the LAN, either make active or increase the receiver active timer
                                            handleReceiver(_lan.id, routerId);

                                        } else if (content[0].toUpperCase(Locale.ROOT).equals("DATA")) {
                                            int incomingLanId = Integer.parseInt(content[1]);
                                            int hostLanId = Integer.parseInt(content[2]);
                                            forwardDataOnOtherLans(routerId, incomingLanId, hostLanId);
                                        } else if (content[0].toUpperCase(Locale.ROOT).equals("NMR")) {
                                            int lanId = Integer.parseInt(content[1]);
                                            int recievedRouterId = Integer.parseInt(content[2]);
                                            int hostLanId = Integer.parseInt(content[3]);
                                            markDownNMR(routerId, lanId, recievedRouterId, hostLanId);
                                        }
                                    } catch (Exception e) {
                                        System.out.println(e.getMessage());
                                    }
                                }
                            }
                            _lan.seek = localmsg.seek;
                        } catch (Exception e) {

                            System.out.println("Something went wrong reading " + lanOutFile + " " + e.getLocalizedMessage());
                        }
                    }
                }
            }
        }, 0, checkMessagesTimerInMs);
    }

    private static void markDownNMR(int routerId, int lanId, int receivedRouterId, int hostLanId) {

    }

    private static void handleReceiver(int id, int routerId) {

    }

    private static void forwardDataOnOtherLans(int routerId, int incomingLanId, int hostLanId) {


    }

    // TODO: 11/25/2021 NMR implementation
    private static void manageNmr(int routerId, int[] lanIds) {

    }


    private static void expireNMRs() {

    }

    private static void setMyNMRs() {
    }

    private static void inspectNMRs() {

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
                                    System.out.println(line); // take action here
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

    private static void manageDvmrp(int routerId, ArrayList<Integer> lanIds, long dvmrpTimersInMs) {

    }
}