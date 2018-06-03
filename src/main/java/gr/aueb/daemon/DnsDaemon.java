
package gr.aueb.daemon;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DnsDaemon implements Daemon {


    private final String commandCofigurationFile = "/etc/dnsUpdater/command.config";
    private final String[] newIP;
    private ReadDnsConfigFile dnsFile;
    private Process commandOutput;
    private String result;
    private String myIp;
    private Thread myThread;
    private boolean stopped;
    private Date initTime;

    {

        this.newIP = new String[3];
        result = null;
        stopped = false;
    }

    private static final String[] WHOAMI = {"/bin/sh",
            "-c", "dig +short myip.opendns.com @resolver1.opendns.com"};


    public void init(DaemonContext daemonContext) throws DaemonInitException, Exception {
        initTime = new Date();
        myThread = new Thread() {
            @Override
            public synchronized void start() {
                DnsDaemon.this.stopped = false;
                super.start();
            }

            @Override
            public void run() {
                dnsFile = new ReadDnsConfigFile();
                myIp = checkIdentity();
                if (myIp != null) {
                    sendUpdate(myIp);
                }
                while (!stopped) {
                    try {
                        if (myIp != null && !myIp.equals(checkIdentity())) {
                            myIp = checkIdentity();
                            if (!(myIp == null)) {
                                System.out.println("Public Ip Address has changed!\n");
                                sendUpdate(myIp);
                                System.out.println("Changes has been send\n");
                            } else {
                                System.out.println("Connectivity Lost...");
                            }
                        } else if (myIp == null) {
                            myIp = checkIdentity();
                            if (myIp != null) {
                                sendUpdate(myIp);
                            } else {
                                System.out.println("No connectivity...");
                            }
                        }
                        sleep(100000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(DnsDaemon.class.getName()).log(Level.SEVERE, "An error occurred!", ex);
                    }
                }
            }

            ;
        }

        ;
    }

    private void sendUpdate(String ip) {
        newIP[0] = "/bin/sh";
        newIP[1] = "-c";
        if (dnsFile.getKey() != null) {
            if (!dnsFile.getHasReverse().equals("YES")) {
                newIP[2] = "cat << EOF | nsupdate -k " + dnsFile.getKey() + "\n\r"
                        + "server " + dnsFile.getServer() + "\n\r"
                        + "zone " + dnsFile.getZone() + "\n\r"
                        + "update delete " + dnsFile.getRecord() + " " + dnsFile.getType() + "\n\r"
                        + "update add " + dnsFile.getRecord() + " " + dnsFile.getTTL() + " " + dnsFile.getType() + " " + ip + "\n\r"
                        + "send\n\r"
                        + "quit\n\r"
                        + "EOF\n\r";
            }
        } else {
            if (!dnsFile.getHasReverse().equals("YES")) {
                newIP[2] = "cat << EOF | nsupdate\n\r"
                        + "server " + dnsFile.getServer() + "\n\r"
                        + "zone " + dnsFile.getZone() + "\n\r"
                        + "update delete " + dnsFile.getRecord() + " " + dnsFile.getType() + "\n\r"
                        + "update add " + dnsFile.getRecord() + " " + dnsFile.getTTL() + " " + dnsFile.getType() + " " + ip + "\n\r"
                        + "send\n\r"
                        + "quit\n\r"
                        + "EOF\n\r";
            }
        }
        try {
            Date currentTime = new Date();
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
            System.out.println("New Ip: " + ip + " at: " + format.format(currentTime));
            Runtime.getRuntime().exec(newIP);
        } catch (IOException ex) {
            Logger.getLogger(DnsDaemon.class.getName()).log(Level.SEVERE, "An error occurred!", ex);
        }
    }

    private String checkIdentity() {
        try {
            commandOutput = Runtime.getRuntime().exec(WHOAMI);
            InputStream parseOutput = commandOutput.getInputStream();
            BufferedReader readableOutput = new BufferedReader(new InputStreamReader(parseOutput));
            result = readableOutput.readLine();
            parseOutput.close();
            readableOutput.close();
        } catch (IOException ex) {
            Logger.getLogger(DnsDaemon.class.getName()).log(Level.SEVERE, "An error occurred!", ex);
        }
        return result;
    }

    public void start() throws Exception {
        myThread.start();
    }

    public void stop() throws Exception {
        stopped = true;
        try {
            myThread.join(1000);
            Date finalTime = new Date();
            long duration = finalTime.getTime() - initTime.getTime();
            System.out.println("Uptime in millis: " + duration);
            System.out.println("<-------------------END-------------------------->");
            System.err.println();
            Logger.getLogger(DnsDaemon.class.getName()).log(Level.INFO, "Uptime in millis: {0}", duration);
        } catch (InterruptedException e) {
            Logger.getLogger(DnsDaemon.class.getName()).log(Level.SEVERE, "Daemon could not stop! please refer "
                    + "to the log file!");
            throw e;
        }
    }

    public void destroy() {
        myThread = null;
    }

}
