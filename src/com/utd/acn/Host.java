package com.utd.acn;

import java.util.Date;

public class Host {
    private static String string;

    public static void main(String[] args) throws InterruptedException {
        if (args.length < 4) {
            System.exit(-1);
        }
        String hostId = args[0];
        String lanId = args[1];
        String type = args[2];
        int timeToStart = 0, period = 0;
        if (type == "sender" && args.length == 5) {
            timeToStart = Integer.parseInt(args[3]);
            period = Integer.parseInt(args[4]);
            Thread.sleep(timeToStart * 1000);
        } else if (type != "receiver" || (type == "sender" && args.length != 5)) {
            System.exit(-1);
        }
        if (type == "sender") {
            for (; ; ) {
                System.out.println("Sending a message " + generateMessage());
                Thread.sleep(period * 1000);
            }
        }else if(type=="receiver") {

        }


    }

    static String generateMessage() {
        String message = "This is a message generated at " + new Date().toString();
        return message;
    }
}
