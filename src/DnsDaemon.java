package daemon;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import java.text.SimpleDateFormat;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

public class DnsDaemon implements Daemon {

    private final String configurationFile = "/etc/dnsUpdater/dns_server.config";
    private final String[] newIP = new String[3];
    private String server;
    private String key;
    private String zone;
    private String record;
    private String type;
    private String ttl;
    private Properties prop;
    private Process commandOutput;
    private String result = null;
    private String myIp;
    private Thread myThread;
    private boolean stopped = false;
    private Date initTime;
    private static final String[] WHOAMI = {"/bin/sh",
        "-c", "dig +short myip.opendns.com @resolver1.opendns.com"};

    @Override
    public void init(DaemonContext daemonContext) throws DaemonInitException, Exception {
        initTime = new Date();
        prop = new Properties();
        try {
            InputStream is = new FileInputStream(configurationFile);
            prop.load(is);
            if (prop.getProperty("server", null) == null) {
                Logger.getLogger(DnsDaemon.class.getName()).log(Level.SEVERE, "Daemon could not start! You must specify the server to connect to!");
                System.exit(1);
            } else {
                server = prop.getProperty("server");
            }
            key = prop.getProperty("key_path", null);
            if (prop.getProperty("zone", null) == null) {
                Logger.getLogger(DnsDaemon.class.getName()).log(Level.SEVERE, "Daemon could not start! You must specify the zone!");
                System.exit(1);
            } else {
                zone = prop.getProperty("zone");
            }
            if (prop.getProperty("record", null) == null) {
                Logger.getLogger(DnsDaemon.class.getName()).log(Level.SEVERE, "Daemon could not start! You must specify the record!");
                System.exit(1);
            } else {
                record = prop.getProperty("record");
            }
            if (prop.getProperty("type_of_record", null) == null) {
                Logger.getLogger(DnsDaemon.class.getName()).log(Level.SEVERE, "Daemon could not start! You must specify the type of the record!");
                System.exit(1);
            } else {
                type = prop.getProperty("type_of_record");
            }
            if (prop.getProperty("ttl_of_record", null) == null) {
                Logger.getLogger(DnsDaemon.class.getName()).log(Level.SEVERE, "Daemon could not start! You must specify the time to live (TTL)!");
                System.exit(1);
            } else {
                ttl = prop.getProperty("ttl_of_record");
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DnsDaemon.class.getName()).log(Level.SEVERE, "An error occurred!", ex);
        } catch (IOException ex) {
            Logger.getLogger(DnsDaemon.class.getName()).log(Level.SEVERE, "An error occurred!", ex);
        }
        myThread = new Thread() {
            @Override
            public synchronized void start() {
                DnsDaemon.this.stopped = false;
                super.start();
            }

            @Override
            public void run() {
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
        if (key != null) {
            newIP[2] = "cat << EOF | nsupdate -k " + key + "\n\r"
                    + "server " + server + "\n\r"
                    + "zone " + zone + "\n\r"
                    + "update delete " + record + " " + type + "\n\r"
                    + "update add " + record + " " + ttl + " " + type + " " + ip + "\n\r"
                    + "send\n\r"
                    + "quit\n\r"
                    + "EOF\n\r";
        } else {
            newIP[2] = "cat << EOF | nsupdate\n\r"
                    + "server " + server + "\n\r"
                    + "zone " + zone + "\n\r"
                    + "update delete " + record + " " + type + "\n\r"
                    + "update add " + record + " " + ttl + " " + type + " " + ip + "\n\r"
                    + "send\n\r"
                    + "quit\n\r"
                    + "EOF\n\r";
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

    @Override
    public void start() throws Exception {
        myThread.start();
    }

    @Override
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

    @Override
    public void destroy() {
        myThread = null;
    }

}
