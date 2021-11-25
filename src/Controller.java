import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Controller {
    private static int selfDestructInMs = 120000;

    public static void main(String[] args) {
        if (args.length < 6) {
            System.out.println("Minimum arguments needed");
            System.exit(-1);
        }
        boolean routerFound = false, hostFound = false, lanFound = false;
        int hostIndex = 0, routerIndex = 0, lanIndex = 0;
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
            System.exit(-1);
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


        Timer selfDestruct = new Timer();
        selfDestruct.schedule(new TimerTask() {
            @Override
            public void run() {
                System.exit(-1);
            }
        }, selfDestructInMs, 1);
    }
}
