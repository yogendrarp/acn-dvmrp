import java.io.*;
import java.util.*;

public class Router {

    static String lanOutFile = "lanX.txt";
    static String routFile = "routX.txt";
    private static int selfDestructInMs = 120000;
    private static long DVMRPTimeInMs = 1000;
    private static long NMRTimeInMs = 10000;
    private static final int MAX = 10;

    public static int directlyAttachedLanCounts = 0;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Need at the least one LAN attached to the router");
            System.exit(-1);
        }
        int routerId = Integer.parseInt(args[0]);
        ArrayList<Integer> attachedLanIds = new ArrayList<Integer>(args.length - 1);

        for (int i = 1; i < args.length; i++) {
            attachedLanIds.add(Integer.parseInt(args[i]));
        }
        Integer[] routerHop;
        HashMap<Integer, Integer[]> dvRoutingMap = new HashMap<>();
        int i = 0;
        while (i < 10) {
            routerHop = new Integer[2];
            routerHop[0] = 10;
            routerHop[1] = 10;
            dvRoutingMap.put(i, routerHop);
            i++;

        }
        Long[] lanSeekPosition = new Long[MAX];
        Long[] receiverTracking = new Long[MAX];
        Arrays.fill(lanSeekPosition, 0);
        Arrays.fill(receiverTracking, 0L);
        directlyAttachedLanCounts = args.length - 1;
        manageDVMRP(routerId, attachedLanIds, dvRoutingMap);
        checkAndSendNMRS(routerId, attachedLanIds, receiverTracking);
        readLanFile(routerId, attachedLanIds, dvRoutingMap, lanSeekPosition, receiverTracking);
        Timer selfDestruct = new Timer();
        selfDestruct.schedule(new TimerTask() {
            @Override
            public void run() {
                System.exit(-1);
            }
        }, selfDestructInMs, 1);
    }

    private static void checkAndSendNMRS(int routerId, ArrayList<Integer> attachedLans, Long[] receiverTracking) {
        Timer nmrCheckTimers = new Timer();
        nmrCheckTimers.schedule(new TimerTask() {
            @Override
            public void run() {
                for (Integer lanId : attachedLans) {
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
                }
            }
        }, NMRTimeInMs, 1);
    }


    public static void manageDVMRP(int routerId, ArrayList<Integer> attachedLans, HashMap<Integer, Integer[]>
            dvRoutingMap) {
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
                    Integer srcLanId = attachedLans.get(i);
                    if (null != srcLanId) {
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


    private static void readLanFile(int routerId, ArrayList<Integer> attachedLans, HashMap<Integer, Integer[]>
            dvRoutingMap, Long[] lanSeekPos, Long[] receiverTracking) {
        //need timer around this
        String routerFileName = routFile.replace("X", String.format("%s", routerId));
        int index = 0;
        for (Integer lanId : attachedLans) {
            String lanFileName = lanOutFile.replace("X", String.format("%s", lanId));

            File lanFile = new File(lanFileName);
            if (lanFile.exists()) {
                DataRead localmsg = null;
                try {
                    File _tmpFile = new File(lanFileName);
                    if (_tmpFile.exists()) {
                        ReadWithLocks readWithLocks = new ReadWithLocks(lanFileName);
                        localmsg = readWithLocks.readFromFile(lanSeekPos[index]);
                        for (String line : localmsg.dataLines) {
                            String[] contents = line.split(" ");
                            if (contents[0].equalsIgnoreCase("data")) {
                                for (int j = 0; j < directlyAttachedLanCounts; j++) {
                                    Integer localLanId = attachedLans.get(j);
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
                                //Updating DV for the LAN connected to router
                                dvRoutingMap.get(msgSrcLanId)[0] = 0;
                                dvRoutingMap.get(msgSrcLanId)[1] = Math.min(msgSrcRouterId, dvRoutingMap.get(msgSrcLanId)[1]);
                                while (hopCountIndex + 1 < contents.length && indexForRoutingMap < 10 && indexForRoutingMap != msgSrcLanId) {
                                    Integer hopCount = Integer.valueOf(contents[hopCountIndex]);
                                    Integer rouId = Integer.valueOf(contents[hopCountIndex + 1]);
                                    //Checking for lesser hopcount
                                    if (dvRoutingMap.get(indexForRoutingMap)[0] - hopCount > 1) {
                                        dvRoutingMap.put(indexForRoutingMap, new Integer[]{hopCount + 1, msgSrcRouterId});
                                    } else if ((dvRoutingMap.get(indexForRoutingMap)[0] - hopCount) == 1) {
                                        //checking for lesser routerid
                                        if (dvRoutingMap.get(indexForRoutingMap)[1] > rouId) {
                                            dvRoutingMap.put(indexForRoutingMap, new Integer[]{hopCount + 1, msgSrcRouterId});
                                        }
                                    }
                                    indexForRoutingMap++;
                                    hopCountIndex += 2;
                                }
                                //Update DV if NMR received from lan file
                            } else if ((contents[0].equalsIgnoreCase("NMR"))) {
                                dvRoutingMap.get(Integer.parseInt(contents[3].trim()))[0] = 10;
                                dvRoutingMap.get(Integer.parseInt(contents[3].trim()))[1] = 10;
                            }//if receiver message read from LAN file
                            else if (contents[0].equalsIgnoreCase("receiver")) {
                                receiverTracking[lanId] = System.currentTimeMillis();
                                dvRoutingMap.get(lanId)[0] = 0;
                                dvRoutingMap.get(lanId)[1] = routerId;
                            }
                        }
                        lanSeekPos[index] = localmsg.seek;
                        index++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}