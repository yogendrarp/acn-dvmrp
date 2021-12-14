import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class TimerTest {
    public static void main(String[] args) {
//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                System.out.println("Hello World");
//            }
//        }, 0, 3000);
//        Timer timer2 = new Timer();
//        timer2.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                System.out.println("Bello Bold");
//            }
//        }, 0, 1000);

        ArrayList<Integer> numbers = new ArrayList<>();
        addNumbers(numbers);
        for (int n : numbers) {
            System.out.println(n);
        }
        multiply(numbers);
        for (int n : numbers) {
            System.out.println(n);
        }
    }

    private static void multiply(ArrayList<Integer> numbers) {
        for (int i = 0; i < 10; i++) {
            numbers.set(i, i * 10);
        }
    }

    private static void addNumbers(ArrayList<Integer> numbers) {
        for (int i = 0; i < 10; i++) {
            numbers.add(i);
        }
    }


}

/*
import java.io.*;
import java.util.*;

public class Router {

    static String lanOutFile = "lanX.txt";
    static String routFile = "routX.txt";
    private static int selfDestructInMs = 120000;
    private static long DVMRPTimeInMs = 1000;
    private static final int MAX = 10;
    public static Integer[] startLineNumber = new Integer[10];
    public static int directlyAttachedLanCounts = 0;
    public static Long[] receiverTracking = new Long[10];

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
        int[] routerHop;
        HashMap<Integer, int[]> dvRoutingMap = new HashMap<>();
        int i = 0;
        while (i < 10) {
            routerHop = new int[2];
            routerHop[0] = 10;
            routerHop[1] = 10;
            dvRoutingMap.put(i, routerHop);
            i++;

        }
        Arrays.fill(startLineNumber, 0);
        Arrays.fill(receiverTracking, 0L);
        directlyAttachedLanCounts = args.length - 1;
        manageDVMRP(routerId, attachedLanIds, dvRoutingMap);

        Timer selfDestruct = new Timer();
        selfDestruct.schedule(new TimerTask() {
            @Override
            public void run() {
                System.exit(-1);
            }
        }, selfDestructInMs, 1);
    }

    public static void manageDVMRP(int routerId, ArrayList<Integer> attachedLans, HashMap<Integer, int[]> dvRoutingMap) {
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

    private static void readLanFile(BufferedWriter bw) {
        BufferedReader br = null;

        try {
            int lanNumber = 0;
            while (lanNumber < directlyAttachedLanCounts) {
                Integer lanId = lanIds[lanNumber];
                if (null != lanId) {
                    String sCurrentLine;
                    File lanFile = new File("lan" + lanId + ".txt");
                    if (lanFile.exists()) {
                        br = new BufferedReader(new FileReader("lan" + lanId + ".txt"));
                        int currLineNumber = 0;
                        while (currLineNumber <= startLineNumber[lanNumber]) {
                            br.readLine();
                            currLineNumber++;
                        }
                        while ((sCurrentLine = br.readLine()) != null) {
                            startLineNumber[lanNumber]++;

                            Long currentTime = System.currentTimeMillis();
                            //Time checking for sending NMR for no receiver messages received
                            if (currentTime - receiverTracking[lanId] > 20000 && receiverTracking[lanId] > 0) {
                                StringBuffer nmr = new StringBuffer();
                                nmr.append("NMR ").append(lanId + " ").append(id + " ").append(lanId);
                                bw.write(nmr.toString());
                                bw.newLine();
                                bw.flush();
                            }
                            //For data message read from lan file
                            if (sCurrentLine.startsWith("data")) {
                                String[] spMsg = sCurrentLine.split(" ");
                                for (int j = 0; j < directlyAttachedLanCounts; j++) {
                                    Integer localLanId = lanIds[j];
                                    if (null != localLanId && lanId != localLanId && dvRoutingMap.get(localLanId)[0] == 0) {
                                        bw.write(sCurrentLine.replaceFirst(String.valueOf(sCurrentLine.charAt(5)), localLanId.toString()));
                                        bw.newLine();
                                        bw.flush();
                                    }
                                }
                                //For DV message read from the lan
                            } else if (sCurrentLine.startsWith("DV")) {
                                String[] msgParameters = sCurrentLine.split(" ");
                                int msgSrcLanId = Integer.valueOf(msgParameters[1]);
                                int msgSrcRouterId = Integer.valueOf(msgParameters[2]);
                                int indexForRoutingMap = 0;
                                int hopCountIndex = 3;
                                //Updating DV for the LAN connected to router
                                dvRoutingMap.get(msgSrcLanId)[0] = 0;
                                dvRoutingMap.get(msgSrcLanId)[1] = Math.min(msgSrcRouterId, dvRoutingMap.get(msgSrcLanId)[1]);
                                while (hopCountIndex + 1 < msgParameters.length && indexForRoutingMap < 10 && indexForRoutingMap != msgSrcLanId) {
                                    Integer hopCount = Integer.valueOf(msgParameters[hopCountIndex]);
                                    Integer routerId = Integer.valueOf(msgParameters[hopCountIndex + 1]);
                                    //Checking for lesser hopcount
                                    if (dvRoutingMap.get(indexForRoutingMap)[0] - hopCount > 1) {
                                        dvRoutingMap.put(indexForRoutingMap, new Integer[]{hopCount + 1, msgSrcRouterId});
                                    } else if ((dvRoutingMap.get(indexForRoutingMap)[0] - hopCount) == 1) {
                                        //checking for lesser routerid
                                        if (dvRoutingMap.get(indexForRoutingMap)[1] > routerId) {
                                            dvRoutingMap.put(indexForRoutingMap, new Integer[]{hopCount + 1, msgSrcRouterId});
                                        }
                                    }
                                    indexForRoutingMap++;
                                    hopCountIndex += 2;
                                }
                                //Update DV if NMR received from lan file
                            } else if (sCurrentLine.startsWith("NMR")) {
                                String[] msgSplitNMR = sCurrentLine.split(" ");
                                dvRoutingMap.get(Integer.parseInt(msgSplitNMR[3].trim()))[0] = 10;
                                dvRoutingMap.get(Integer.parseInt(msgSplitNMR[3].trim()))[1] = 10;
                            }//if receiver message read from LAN file
                            else if (sCurrentLine.startsWith("receiver")) {
                                receiverTracking[lanId] = System.currentTimeMillis();
                                dvRoutingMap.get(lanId)[0] = 0;
                                dvRoutingMap.get(lanId)[1] = id;
                            }
                        }
                    }
                }
                lanNumber++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


}


 */
