/*
 * Copyright (c) 2023 Dennis Lang (LanDen Labs) landenlabs@gmail.com
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * @author Dennis Lang
 * @see https://LanDenLabs.com/
 */

package com.landenlabs.all_devtool.shortcuts.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    // 1 = localAddress
    // 2 = localPort
    // 3 = remoteAddress
    // 4 = remotePort
    // 5 = status  0A=listent
    // 6 = tx queue
    // 7 = rx quque
    // 8 = uid
    //
    //
    // tcp (tcp4)
    // sl  local_address rem_address   st tx_queue rx_queue tr tm->when retrnsmt   uid  timeout inode
    // 4: 00000000:830D 00000000:0000 8A 00000000:00000000 00:00000000 00000000     0        0 28420 1 0000000000000000 99 0 0 10 0
    // 5: 6501A8C0:BF3B 2E0BD9AC:01BB 08 00000000:0000026D 00:00000000 00000000 10087        0 1008831 1 0000000000000000 55 3 28 10 -1
    // 6: 6501A8C0:A84B 820CD9AC:01BB 08 00000000:0000026D 00:00000000 00000000 10087        0 1008818 1 0000000000000000 34 3 28 10 -1
    //
    // tcp6
    // sl  local_address                         remote_address                        st tx_queue rx_queue tr tm->when retrnsmt   uid  timeout inode
    // 8: 0000000000000000FFFF00006501A8C0:AE02 0000000000000000FFFF000063D21968:01BB 01 00000000:00000000 00:00000000 00000000 10327        0 1009652 1 0000000000000000 21 3 29 10 -1
    // 9: 0000000000000000FFFF00006501A8C0:B459 0000000000000000FFFF00005C1C1168:01BB 01 00000000:00000000 00:00000000 00000000 10320        0 1009378 1 0000000000000000 73 3 24 10 -1

    // --------------
    //     This document describes the interfaces /proc/net/tcp and /proc/net/tcp6.
    //     Note that these interfaces are deprecated in favor
    //     of tcp_diag.
    //
    //     These /proc interfaces provide information about currently active TCP
    //     connections, and are implemented by tcp4_seq_show() in
    //     net/ipv4/tcp_ipv4.c and tcp6_seq_show() in net/ipv6/tcp_ipv6.c,
    //     respectively.
    //
    //     It will first list all listening TCP sockets, and next list all
    //     established TCP connections. A typical entry of /proc/net/tcp would
    //     look like this (split up into 3 parts because of the length of the
    //     line):
    //
    //  46: 010310AC:9C4C 030310AC:1770 01
    //   |      |      |      |      |   |--> connection state
    //   |      |      |      |      |------> remote TCP port number
    //   |      |      |      |-------------> remote IPv4 address
    //   |      |      |--------------------> local TCP port number
    //   |      |---------------------------> local IPv4 address
    //   |----------------------------------> number of entry
    //
    // 00000150:00000000 01:00000019 00000000
    //   |        |     |     |       |--> number of unrecovered RTO timeouts
    //   |        |     |     |----------> number of jiffies until timer expires
    //   |        |     |----------------> timer_active (see below)
    //   |        |----------------------> receive-queue
    //   |-------------------------------> transmit-queue
    //
    // 1000        0 54165785 4 cd1e6040 25 4 27 3 -1
    // |          |    |     |    |     |  | |  | |--> slow start size threshold,
    // |          |    |     |    |     |  | |  |      or -1 if the threshold
    // |          |    |     |    |     |  | |  |      is >= 0xFFFF
    // |          |    |     |    |     |  | |  |----> sending congestion window
    // |          |    |     |    |     |  | |-------> (ack.quick<<1)|ack.pingpong
    // |          |    |     |    |     |  |---------> Predicted tick of soft clock
    // |          |    |     |    |     |              (delayed ACK control data)
    // |          |    |     |    |     |------------> retransmit timeout
    // |          |    |     |    |------------------> location of socket in memory
    // |          |    |     |-----------------------> socket reference count
    // |          |    |-----------------------------> inode
    // |          |----------------------------------> unanswered 0-window probes
    // |---------------------------------------------> uid
    // --------------
    private static final String TCP_4_PATTERN =
            "\\d+:\\s([0-9A-F]{8}):([0-9A-F]{4})\\s([0-9A-F]{8}):([0-9A-F]{4})\\s([0-9A-F]{2})\\s([0-9A-F]{8}):([0-9A-F]{8})\\s[0-9]{2}:[0-9]{8}\\s[0-9A-F]{8}\\s+([0-9]+)";

    private static final String TCP_6_PATTERN =
           //  "\\d+:\\s([0-9A-F]{32}):([0-9A-F]{4})\\s([0-9A-F]{32}):([0-9A-F]{4})\\s([0-9A-F]{2})\\s([0-9]{8}):([0-9]{8})\\s[0-9]{2}:[0-9]{8}\\s[0-9]{8}\\s+([0-9]+)";
            " *\\d+:\\s([0-9A-F]{32}):([0-9A-F]{4})\\s([0-9A-F]{32}):([0-9A-F]{4})\\s([0-9A-F]{2})\\s([0-9]{8}):([0-9]{8})\\s([0-9]{2}):([0-9]{8})\\s([0-9]{8})\\s+([0-9]+)\\s.*";
    public enum  NetStatus {
        UNKNOWN(0),
        ESTABLISHED(1),
        SYN_SENT(2),
        SYN_RECV(3),
        FIN_WAIT1(4),
        FIN_WAIT2(5),
        TIME_WAIT(6),
        CLOSE(7),
        CLOSE_WAIT(8),
        LAST_ACK(9),
        LISTEN(10),
        CLOSING(11),   /* Now a valid state */
        NEW_SYN_RECV(12);

        final int value;
        NetStatus(int value) {
            this.value = value;
        }
        public static NetStatus getFor(int val) {
            for (NetStatus netStatus : NetStatus.values()) {
                if (netStatus.value == val) {
                    return netStatus;
                }
            }
            return UNKNOWN;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class NetConnection {
         NetConnection(String type) {
            this.type = type;
        }

        public final String type;
        public InetAddress localAddr;
        public String localAddrHexStr;
        public int localPort;
        public InetAddress remoteAddr;
        public String remoteAddrHexStr;
        public int remotePort;
        public int status;
        public NetStatus netStatus;
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
        netConnection.netStatus = NetStatus.getFor(netConnection.status & 0xf);

        netConnection.txQueue = Integer.parseInt(m6.group(6), 16);
        netConnection.rxQueue = Integer.parseInt(m6.group(7), 16);

        netConnection.pidEntry = Integer.valueOf(m6.group(8));

        netConnection.localAddr = parseHexAddrStr(netConnection.localAddrHexStr, netConnection.type);
        netConnection.remoteAddr = parseHexAddrStr(netConnection.remoteAddrHexStr, netConnection.type);
    }

    private static InetAddress parseHexAddrStr(String hexStr, String type) {
        InetAddress addr = null;
        byte[] bytes = hexStringToByteArray(hexStr);

        switch (type.toLowerCase()) {
            case "tcp":
            case "tcp4":
                try {
                    htonb(bytes);
                    addr = Inet4Address.getByAddress(bytes);
                } catch (Exception ignore) {
                }
                break;
            case "tcp6":
                try {
                    // if (hexStr.startsWith("fe80") || hexStr.startsWith("FE80")) // skipping link-local addresses
                    //    continue;
                    flipHex4(bytes);
                    addr = Inet6Address.getByAddress(bytes);
                    // Log.d("ipv6 ", hexStr+" " + addr.getHostAddress());
                    // addr = Inet6Address.getByAddress(null, bytes, 0);
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

    private static byte[] flipHex4(byte[] bytes) {
        int len = bytes.length;
        for (int idx = 0; idx < len; idx +=4) {
            swab(bytes, idx, idx+3);
            swab(bytes, idx+1, idx+2);
        }
        return bytes;
    }

    private static void swab(byte[] bytes, int fromIdx, int toIdx) {
        byte bTemp = bytes[fromIdx];
        bytes[fromIdx] = bytes[toIdx];
        bytes[toIdx] = bTemp;
    }

    private static byte[] htoni(int x)
    {
        byte[] res = new byte[4];
        for (int i = 0; i < 4; i++)
        {
            res[i] = (Integer.valueOf(x >>> 24)).byteValue();
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
        } catch (Exception ex) {
            // Android Q - privacy changes.
            //  https://developer.android.com/about/versions/10/privacy/changes
            Log.e("NetUtils", "Failed to read " + filePath + " ex=" + ex.getMessage());
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
     *
     * Example ipv6=2606:4700:20::6819:d163
     *
     * https://tools.keycdn.com/geo?host=2606%3A4700%3A20%3A%3A6819%3Ad163
     * https://www.whtop.com/tools.ip/2606:4700:20::6819:d163
     * http://api.antideo.com/ip/location/2606:4700:20::6819:d163
     * http://ipv6.my-addr.com/ipv6-whois-lookup.php?ip=2606:4700:20::6819:d163
     * https://ipgeolocation.io/ip-location/2606:4700:20::6819:d163
     * http://api.db-ip.com/v2/free/2606:4700:20::6819:d163
     *
     * http://api.ipinfodb.com/v3/ip-country/?key=YOUR_API_KEY&ip=IP_V4_OR_IPV6_ADDRESS
     * http://api.ipinfodb.com/v3/ip-city/?key=YOUR_API_KEY&ip=IP_V4_OR_IPV6_ADDRESS
     *
     * https://ipstack.com/
     *

     * https://geo.ipify.org/api/v1?apiKey=at_CuuoXZ5zzMc00Iao7i3c2SoPYbKG7&ipAddress=2606:4700:20::6819:d163
     * https://geoipify.whoisxmlapi.com/api/v1?apiKey=at_teShvVcLkW0i1mA0Lf8KRLsyMXDh3&ipAddress=2606:4700:20::6819:d163
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
                    line = line.replaceAll(",$", "");
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
