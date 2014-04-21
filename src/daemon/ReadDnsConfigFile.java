/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package daemon;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dimitris
 */
public class ReadDnsConfigFile {

    private final String dnsConfigurationFile = "/etc/dnsUpdater/dns_server.config";
    private final Properties prop;
    private InputStream is;
    private String server;
    private String key;
    private String zone;
    private String record;
    private String type;
    private String TTL;
    private String hasReverse;
    private String reverseZone;
    private String reverseRecord;
    private String reverseTTL;

    {
        prop = new Properties();
    }

    public ReadDnsConfigFile() {
     readDnsConfigFile();
    }

    private void readDnsConfigFile() {

        try {
            is = new FileInputStream(dnsConfigurationFile);
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
                TTL = prop.getProperty("ttl_of_record");
            }
            if (prop.getProperty("has_reverse", "NO").equals("YES")) {
                hasReverse = "YES";
                if (prop.getProperty("reverse_zone", null) == null) {
                    Logger.getLogger(DnsDaemon.class.getName()).log(Level.SEVERE, "Daemon could not start! You must specify the reverse zone!");
                    System.exit(1);
                } else {
                    reverseZone = prop.getProperty("reverse_zone");
                }
                if (prop.getProperty("reverse_record", null) == null) {
                    Logger.getLogger(DnsDaemon.class.getName()).log(Level.SEVERE, "Daemon could not start! You must specify the reverse record!");
                    System.exit(1);
                } else {
                    reverseRecord = prop.getProperty("reverse_record");
                }
                if (prop.getProperty("reverse_ttl", null) == null) {
                    Logger.getLogger(DnsDaemon.class.getName()).log(Level.SEVERE, "Daemon could not start! You must specify the reverse ttl!");
                    System.exit(1);
                } else {
                    reverseTTL = prop.getProperty("reverse_ttl");
                }
            }else{
                hasReverse = "NO";
            }
            is.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DnsDaemon.class.getName()).log(Level.SEVERE, "An error occurred!", ex);
        } catch (IOException ex) {
            Logger.getLogger(DnsDaemon.class.getName()).log(Level.SEVERE, "An error occurred!", ex);
        }
    }

    public String getServer() {
        return server;
    }

    public String getKey() {
        return key;
    }

    public String getZone() {
        return zone;
    }

    public String getRecord() {
        return record;
    }

    public String getType() {
        return type;
    }

    public String getTTL() {
        return TTL;
    }

    public String getHasReverse() {
        return hasReverse;
    }

    public String getReverseZone() {
        return reverseZone;
    }

    public String getReverseRecord() {
        return reverseRecord;
    }

    public String getReverseTTL() {
        return reverseTTL;
    } 
}
