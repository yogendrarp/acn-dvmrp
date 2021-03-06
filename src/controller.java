import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class controller {
    static int selfDestructInMs = 120000;
    static String hostFile = "houtX.txt";
    static String lanFile = "lanX.txt";
    static String routFile = "routX.txt";

    public static void main(String[] args) {
        System.out.println("Starting Controller");

        if (args.length < 6) {
            System.out.println("Minimum arguments needed");
            System.exit(-1);
        }
        boolean routerFound = false, hostFound = false, lanFound = false;
        int hostIndex = 0, routerIndex = 0, lanIndex = 0, checkTimeInMs = 1000;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("host")) {
                hostFound = true;
                hostIndex = i;
            } else if (args[i].equals("router")) {
                routerFound = true;
                routerIndex = i;
            } else if (args[i].equals("lan")) {
                lanFound = true;
                lanIndex = i;
            }
        }
        if (!(routerFound && lanFound && hostFound)) {
            System.out.println("Some pieces of information missing, check all arguments");
            // System.exit(-1);
        }

        //Assuming the args are in order of host, router and lan
        int[] hostIds = new int[(routerIndex) - (hostIndex + 1)];
        int[] routerIds = new int[(lanIndex) - (routerIndex + 1)];
        int[] lanIds = new int[(args.length) - (lanIndex + 1)];

        for (int i = hostIndex + 1, j = 0; i < routerIndex; i++, j++) {
            hostIds[j] = Integer.parseInt(args[i]);
        }

        for (int i = routerIndex + 1, j = 0; i < lanIndex; i++, j++) {
            routerIds[j] = Integer.parseInt(args[i]);
        }

        for (int i = lanIndex + 1, j = 0; i < args.length; i++, j++) {
            lanIds[j] = Integer.parseInt(args[i]);
        }

        readAllDeviceFiles(hostIds, routerIds, lanIds, checkTimeInMs);

        Timer selfDestruct = new Timer();
        selfDestruct.schedule(new TimerTask() {
            @Override
            public void run() {
                System.exit(-1);
            }
        }, selfDestructInMs, 1);
    }

    private static void readAllDeviceFiles(int[] hostIds, int[] routerIds, int[] lanIds, int checkTimeInMs) {
        Timer checkMessagesTimer = new Timer();
        dataread[] hostMsgs = new dataread[hostIds.length];
        dataread[] routerMsgs = new dataread[routerIds.length];

        for (int i = 0; i < hostMsgs.length; i++) {
            dataread msg = new dataread();
            msg.seek = 0;
            msg.dataLines = new ArrayList<String>();
            hostMsgs[i] = msg;
        }

        for (int i = 0; i < routerMsgs.length; i++) {
            dataread msg = new dataread();
            msg.seek = 0;
            msg.dataLines = new ArrayList<String>();
            routerMsgs[i] = msg;
        }

        checkMessagesTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < hostIds.length; i++) {
                        long previousSeek = hostMsgs[i].seek;
                        dataread localmsg = null;
                        String hostFileName = hostFile.replace("X", hostIds[i] + "");
                        File _tmpFile = new File(hostFileName);
                        if (!(_tmpFile.exists())) {
                            continue;
                        }
                        readwithlocks readWithLocks = new readwithlocks(hostFileName);
                        localmsg = readWithLocks.readFromFile(previousSeek);
                        hostMsgs[i].dataLines.clear();
                        for (String line : localmsg.dataLines) {
                            if (!line.trim().isEmpty()) {
                                try {
                                    String[] content = line.split(" ");
                                    String _lanFileName = lanFile.replace("X", content[1]);
                                    writewithlocks _writeWithLocks = new writewithlocks(_lanFileName);
                                    _writeWithLocks.writeToFileWithLock(line + "\n");
                                    hostMsgs[i].dataLines.add(line);
                                } catch (Exception e) {
                                    System.out.println(e.getMessage());
                                }
                            }
                        }
                        hostMsgs[i].seek = localmsg.seek;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    for (int i = 0; i < routerIds.length; i++) {
                        long previousSeek = routerMsgs[i].seek;
                        dataread localmsg = null;
                        String routerFileName = routFile.replace("X", routerIds[i] + "");
                        File file = new File(routerFileName);
                        if (!file.exists()) {
                            continue;
                        }
                        readwithlocks readWithLocks = new readwithlocks(routerFileName);
                        localmsg = readWithLocks.readFromFile(previousSeek);
                        routerMsgs[i].dataLines.clear();
                        for (String line : localmsg.dataLines) {
                            if (!line.trim().isEmpty()) {
                                try {
                                    String[] splitLine = line.split(" ");
                                    String _lanFileName = lanFile.replace("X", splitLine[1]);
                                    writewithlocks _writeWithLocks = new writewithlocks(_lanFileName);
                                    _writeWithLocks.writeToFileWithLock(line + "\n");
                                    routerMsgs[i].dataLines.add(line);
                                } catch (Exception e) {
                                    System.out.println(e.getMessage());
                                }
                            }
                        }
                        routerMsgs[i].seek = localmsg.seek;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, checkTimeInMs);


    }
}
