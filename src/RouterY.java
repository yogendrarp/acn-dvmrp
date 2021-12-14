import java.io.*;
import java.util.*;

public class RouterY {

    static String lanOutFile = "lanX.txt";
    static String routFile = "routX.txt";
    private static int selfDestructInMs = 120000;
    private static long DVMRPTimeInMs = 5000;
    private static long NMRTimeInMs = 10000;
    private static long readLanTimerInMs = 1000;
    private static final int MAX = 10;
    static HashMap<Integer, Integer[]> dvRoutingMap = new HashMap<>();
    static Integer[] attachedLans = new Integer[MAX];
    static Long[] lanSeekPosition = new Long[MAX];
    static Long[] receiverTracking = new Long[MAX];

    public static int directlyAttachedLanCounts = 0;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting Router");
        if (args.length < 2) {
            System.out.println("Need at the least one LAN attached to the router");
            //System.exit(-1);
        }
        int routerId = Integer.parseInt(args[0]);

        for (int i = 1; i < args.length; i++) {
            attachedLans[i - 1] = Integer.parseInt(args[i]);
        }
        Integer[] routerHop;
        int i = 0;
        while (i < 10) {
            routerHop = new Integer[2];
            routerHop[0] = 10;
            routerHop[1] = 10;
            dvRoutingMap.put(i, routerHop);
            i++;

        }
        Arrays.fill(lanSeekPosition, 0L);
        Arrays.fill(receiverTracking, 0L);
        directlyAttachedLanCounts = args.length - 1;
        manageDVMRP(routerId);
        checkAndSendNMRS(routerId);
        readLanFile(routerId);
        Timer selfDestruct = new Timer();
        selfDestruct.schedule(new TimerTask() {
            @Override
            public void run() {
                System.exit(-1);
            }
        }, selfDestructInMs, 1);
    }

    private static void checkAndSendNMRS(int routerId) {
        Timer nmrCheckTimers = new Timer();
        nmrCheckTimers.schedule(new TimerTask() {
            @Override
            public void run() {
                int lanNumber = 0;
                while (lanNumber < directlyAttachedLanCounts) {
                    Integer lanId = attachedLans[lanNumber];
                    String routerFileName = routFile.replace("X", String.format("%s", routerId));
                    Long currentTime = System.currentTimeMillis();
                    if (currentTime - receiverTracking[lanId] > 20000 && receiverTracking[lanId] > 0) {
                        StringBuffer nmrmsg = new StringBuffer();
                        nmrmsg.append("NMR ").append(lanId + " ").append(routerId + " ").append(lanId);
                        WriteWithLocks writeWithLocks = null;
                        try {
                            writeWithLocks = new WriteWithLocks(routerFileName);
                            writeWithLocks.writeToFileWithLock(nmrmsg.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    lanNumber++;
                }
            }
        }, NMRTimeInMs, 1);
    }


    public static void manageDVMRP(int routerId) {
        Timer manageRouterTimer = new Timer();
        String routerOutFileName = routFile.replace("X", String.format("%s", routerId));
        manageRouterTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                StringBuilder dvmrpMsg = new StringBuilder();
                for (Integer dvLanId : dvRoutingMap.keySet()) {
                    dvmrpMsg.append(" ").append(dvRoutingMap.get(dvLanId)[0]).append(" ").append(dvRoutingMap.get(dvLanId)[1]);
                }
                for (int i = 0; i < directlyAttachedLanCounts; i++) {
                    Integer srcLanId = attachedLans[i];
                    if (srcLanId != null) {
                        StringBuilder dvMessageComplete = new StringBuilder();
                        dvMessageComplete.append("DV ").append(srcLanId).append(" ").append(routerId).append(dvmrpMsg).append(System.lineSeparator());
                        String msg = dvMessageComplete.toString();
                        WriteWithLocks writeWithLocks = null;
                        try {
                            writeWithLocks = new WriteWithLocks(routerOutFileName);
                            writeWithLocks.writeToFileWithLock(msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, DVMRPTimeInMs, 1);
    }


    private static void readLanFile(int routerId) {
        Timer readLanTimer = new Timer();
        readLanTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                String routerFileName = routFile.replace("X", String.format("%s", routerId));
                int lanNumber = 0;
                while (lanNumber < directlyAttachedLanCounts) {
                    Integer lanId = attachedLans[lanNumber];
                    String lanFileName = lanOutFile.replace("X", String.format("%s", lanId));
                    File lanFile = new File(lanFileName);
                    if (lanFile.exists()) {
                        DataRead localmsg = null;
                        try {
                            File _tmpFile = new File(lanFileName);
                            if (_tmpFile.exists()) {
                                ReadWithLocks readWithLocks = new ReadWithLocks(lanFileName);
                                localmsg = readWithLocks.readFromFile(lanSeekPosition[lanNumber]);
                                for (String line : localmsg.dataLines) {
                                    String[] contents = line.split(" ");
                                    if (contents[0].equalsIgnoreCase("data")) {
                                        for (int j = 0; j < directlyAttachedLanCounts; j++) {
                                            Integer localLanId = attachedLans[j];
                                            System.out.println(localLanId + " " + dvRoutingMap.get(localLanId)[0]);
                                            if (null != localLanId && lanId != localLanId && dvRoutingMap.get(localLanId)[0] == 0) {
                                                String dataLanLine = line.replaceFirst(String.valueOf(line.charAt(5)), localLanId.toString());
                                                WriteWithLocks writeWithLocks = null;
                                                try {
                                                    writeWithLocks = new WriteWithLocks(routerFileName);
                                                    writeWithLocks.writeToFileWithLock(dataLanLine);
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    } else if (contents[0].equals("DV")) {
                                        int msgSrcLanId = Integer.parseInt(contents[1]);
                                        int msgSrcRouterId = Integer.parseInt(contents[2]);
                                        int indexForRoutingMap = 0;
                                        int hopCountIndex = 3;
                                        System.out.println("DV " + msgSrcLanId + " " + msgSrcRouterId + " ");
                                        dvRoutingMap.get(msgSrcLanId)[0] = 0;
                                        dvRoutingMap.get(msgSrcLanId)[1] = Math.min(msgSrcRouterId, dvRoutingMap.get(msgSrcLanId)[1]);
                                        while (hopCountIndex + 1 < contents.length && indexForRoutingMap < 10 && indexForRoutingMap != msgSrcLanId) {
                                            Integer hopCount = Integer.valueOf(contents[hopCountIndex]);
                                            Integer rouId = Integer.valueOf(contents[hopCountIndex + 1]);
                                            if (dvRoutingMap.get(indexForRoutingMap)[0] - hopCount > 1) {
                                                dvRoutingMap.put(indexForRoutingMap, new Integer[]{hopCount + 1, msgSrcRouterId});
                                            } else if ((dvRoutingMap.get(indexForRoutingMap)[0] - hopCount) == 1) {
                                                if (dvRoutingMap.get(indexForRoutingMap)[1] > rouId) {
                                                    dvRoutingMap.put(indexForRoutingMap, new Integer[]{hopCount + 1, msgSrcRouterId});
                                                }
                                            }
                                            indexForRoutingMap++;
                                            hopCountIndex += 2;
                                        }
                                    } else if ((contents[0].equalsIgnoreCase("NMR"))) {
                                        dvRoutingMap.get(Integer.parseInt(contents[3].trim()))[0] = 10;
                                        dvRoutingMap.get(Integer.parseInt(contents[3].trim()))[1] = 10;
                                    } else if (contents[0].equalsIgnoreCase("receiver")) {
                                        receiverTracking[lanId] = System.currentTimeMillis();
                                        dvRoutingMap.get(lanId)[0] = 0;
                                        dvRoutingMap.get(lanId)[1] = routerId;
                                    }
                                }
                                lanSeekPosition[lanNumber] = localmsg.seek;
                                lanNumber++;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, readLanTimerInMs, 1);
    }
}