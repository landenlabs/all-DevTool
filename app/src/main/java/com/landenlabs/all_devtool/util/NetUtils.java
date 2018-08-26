package com.landenlabs.all_devtool.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Dennis Lang on 8/25/18.
 */
public class NetUtils {

    /*
     * In a Linux-based OS, each active TCP socket is mapped in the following
     * two files. A socket may be mapped in the '/proc/net/tcp' file in case
     *  of a simple IPv4 address, or in the '/proc/net/tcp6' if an IPv6 address
     *  is available.
     */
    private static final String TCP_4_FILE_PATH = "/proc/net/tcp";
    private static final String TCP_6_FILE_PATH = "/proc/net/tcp6";
    /*
     * Two regular expressions that are able to extract valuable informations
     * from the two /proc/net/tcp* files. More specifically, there are three
     * fields that are extracted:
     * 	- address
     * 	- port
     * 	- PID
     */

    // sl  local_address                         remote_address                        st tx_queue rx_queue tr tm->when retrnsmt   uid  timeout inode
    // 99: 00000000000000000000000000000000:C383 00000000000000000000000000000000:0000 8A 00000000:00000000 00:00000000 00000000     0        0 38022 1 0000000000000000 99 0 0 10 -1
    //
    // 1 = localAddress
    // 2 = localPort
    // 3 = remoteAddress
    // 4 = remotePort
    // 5 = status
    // 6 = tx queue
    // 7 = rx quque
    // 8 = uid
    private static final String TCP_6_PATTERN =
            "\\d+:\\s([0-9A-F]{32}):([0-9A-F]{4})\\s([0-9A-F]{32}):([0-9A-F]{4})\\s([0-9A-F]{2})\\s([0-9]{8}):([0-9]{8})\\s[0-9]{2}:[0-9]{8}\\s[0-9]{8}\\s+([0-9]+)";
    private static final String TCP_4_PATTERN =
            "\\d+:\\s([0-9A-F]{8}):([0-9A-F]{4})\\s([0-9A-F]{8}):([0-9A-F]{4})\\s([0-9A-F]{2})\\s([0-9A-F]{8}):([0-9A-F]{8})\\s[0-9]{2}:[0-9]{8}\\s[0-9A-F]{8}\\s+([0-9]+)";



    @SuppressWarnings("WeakerAccess")
    public static class NetConnection {
         NetConnection(String type) {
            this.type = type;
        }

        public String type;
        public InetAddress localAddr;
        public String localAddrHexStr;
        public int localPort;
        public InetAddress remoteAddr;
        public String remoteAddrHexStr;
        public int remotePort;
        public int status;
        public int txQueue;
        public int rxQueue;
        public int pidEntry;
        public String packageName;
        public String appName;
        public String appVersion;
    }
    public  static class NetConnections extends ArrayList<NetConnection> {
        public String typeErr;
    }

    private static  class MutableBoolean {
        public boolean value;
        MutableBoolean(boolean value) {
            this.value = value;
        }
    }


    /**
     * Create list of all active TCP4 and TCP6 connections.
     */
    public static NetConnections getConnetions(Context context) {
        NetConnections netConnections = new NetConnections();

        MutableBoolean hasIPv4 = new MutableBoolean(false);
        MutableBoolean hasIPv6 = new MutableBoolean(false);
        netConnections.typeErr = connectionTypes(hasIPv4, hasIPv6);

        if (hasIPv6.value) {
            parse(TCP_6_FILE_PATH, TCP_6_PATTERN, context, netConnections);
        }

        if (hasIPv4.value) {
            parse(TCP_4_FILE_PATH, TCP_4_PATTERN, context, netConnections);
        }

        return netConnections;
    }

    private static void parse(String path, String tcpPattern, Context context, NetConnections netConnections) {
        String type = path.replace("/proc/net/", "");
        String content = readFile(path);
        Matcher m4 = Pattern.compile(tcpPattern,
                Pattern.CASE_INSENSITIVE | Pattern.UNIX_LINES | Pattern.DOTALL)
                .matcher(content);

        while (m4.find()) {
            NetConnection netConnection = new NetConnection(type);
            extractInfo(m4, netConnection);
            addPkg(context, netConnection);
            netConnections.add(netConnection);
        }
    }

    // 1 = localAddress
    // 2 = localPort
    // 3 = remoteAddress
    // 4 = remotePort
    // 5 = status
    // 6 = tx queue
    // 7 = rx quque
    // 8 = uid
    private static void extractInfo(Matcher m6, NetConnection netConnection) {
        netConnection.localAddrHexStr = m6.group(1);
        netConnection.localPort = Integer.parseInt(m6.group(2), 16);
        netConnection.remoteAddrHexStr = m6.group(3);
        netConnection.remotePort = Integer.parseInt(m6.group(4), 16);

        netConnection.status = Integer.parseInt(m6.group(5), 16);

        netConnection.txQueue = Integer.parseInt(m6.group(6), 16);
        netConnection.rxQueue = Integer.parseInt(m6.group(7), 16);

        netConnection.pidEntry = Integer.valueOf(m6.group(8));

        netConnection.localAddr = parseHexAddrStr(netConnection.localAddrHexStr, netConnection.type);
        netConnection.remoteAddr = parseHexAddrStr(netConnection.remoteAddrHexStr, netConnection.type);
    }

    private static InetAddress parseHexAddrStr(String hexStr, String type) {
        InetAddress addr = null;
        byte[] bytes = hexStringToByteArray(hexStr);
        htonb(bytes);

        switch (type.toLowerCase()) {
            case "tcp":
            case "tcp4":
                try {
                    addr = Inet4Address.getByAddress(bytes);
                } catch (Exception ignore) {
                }
                break;
            case "tcp6":
                try {
                    addr = Inet6Address.getByAddress(bytes);
                } catch (Exception ignore) {
                }
                break;
        }

        return addr;
    }

    @NonNull
    public String formatIp(int ipAddrss) {
        byte[] myIPAddress = BigInteger.valueOf(ipAddrss).toByteArray();
        // you must reverse the byte array before conversion. Use Apache's commons library
        htonb(myIPAddress);
        try {
            InetAddress myInetIP = InetAddress.getByAddress(myIPAddress);
            return myInetIP.getHostAddress();
        } catch (Exception ex) {
            return "<unknownIP>";
        }
    }


    //  Len = 8,  half = 4
    //  0 -> 7   7 -> 0
    //  1 -> 6   6 -> 1
    //  2 -> 5   5 -> 2
    //  3 -> 4   4 -> 3
    //
    //  Len = 3, half = 1
    //  0 -> 2   2 -> 0
    //  1 -> 1
    private static byte[] htonb(byte[] bytes) {
        if (ByteOrder.nativeOrder() != ByteOrder.BIG_ENDIAN) {
            int len = bytes.length;
            for (int idx = 0, dst=len-1; idx < len/2; idx++, dst--) {
                byte bTemp = bytes[idx];
                bytes[idx] = bytes[dst];
                bytes[dst] = bTemp;
            }
        }
        return bytes;
    }

    private static byte[] htoni(int x)
    {
        byte[] res = new byte[4];
        for (int i = 0; i < 4; i++)
        {
            res[i] = (new Integer(x >>> 24)).byteValue();
            x <<= 8;
        }
        return res;
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }




    @NonNull
    private static String readFile(String filePath) {
        try {
            File inFile = new File(filePath);
            if (inFile.exists()) {
                String line;
                StringBuilder strBuf = new StringBuilder((int)inFile.length());
                try (BufferedReader reader = new BufferedReader(new FileReader(inFile))) {
                    while ((line = reader.readLine()) != null) {
                        strBuf.append(line);
                    }
                }
                return strBuf.toString();
            }
        } catch (Exception ignore) {
        }

        return "";
    }

    private static void addPkg(Context context, NetConnection netConnection) {
        PackageManager manager = context.getPackageManager();
        String[] packagesForUid = manager.getPackagesForUid( netConnection.pidEntry);
        if (packagesForUid != null) {
            netConnection.packageName = packagesForUid[0];

            try {
                PackageInfo pInfo = manager.getPackageInfo( netConnection.packageName, 0);
                netConnection.appName = pInfo.applicationInfo.loadLabel(manager).toString();
                netConnection.appVersion = pInfo.versionName;
            } catch (PackageManager.NameNotFoundException ignore) {
            }

        }
    }

    private static String connectionTypes(MutableBoolean haveIp4, MutableBoolean haveIp6) {
        haveIp4.value = false;
        haveIp6.value = false;

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress.getHostAddress() != null) {
                            if (inetAddress instanceof Inet4Address) {
                                haveIp4.value = true;
                            } else if (inetAddress instanceof Inet6Address) {
                                String sAddr = inetAddress.getHostAddress().toUpperCase();
                                // skipping link-local addresses
                                if (!sAddr.startsWith("fe80") && !sAddr.startsWith("FE80")) {
                                    haveIp6.value = true;
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception ex) {
            ex.getMessage();
        }
        return "";
    }

    /**
     * Perform IP geoLocation lookup
     * WARNING - Performs network I/O, run on background thread.
     * @return Map of geolocation values
     *
     * https://ipapi.co/api/?java#complete-location
     */
    @Nullable
    public static Map<String, String> getIpLocation(@NonNull InetAddress addr) {

        try {
            URL ipapi = new URL("https://ipapi.co/" + addr.getHostAddress() + "/json/");

            URLConnection c = ipapi.openConnection();
            c.setRequestProperty("User-Agent", "java-ipapi-client");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(c.getInputStream()))) {
                Map<String, String> ipLocation = new HashMap<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        ipLocation.put(
                                parts[0].replaceAll("\"", "").trim(),
                                parts[1].replaceAll("\"", "").trim());
                    }
                }
                return ipLocation;
            }
        } catch (Exception ignore) {
            return null;
        }
    }
}
