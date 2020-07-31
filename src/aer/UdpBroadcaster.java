/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UdpBroadcaster implements Serializable {

    public static final int PORT = 9999;
    public static final String MCAST_ADDR = "FF02::1";
    public static InetAddress GROUP;
    public static Node no = new Node();
    public static Timer timer = new Timer();
    public static long timeoutHello = 30000; // milliseconds
    public static long timeoutRR = 40000;
    public static final String ipnode = "fe80:0:0:0:200:ff:feaa:";
    public static boolean difusion_server = false;
    public static boolean difusion_client = false;
    public static String server_info = null;

    public static Thread console() throws IOException {
        // Consola do UTILIZADOR
        return new Thread(new Runnable() {

            public void run() {
                Scanner sc = new Scanner(System.in);
                System.out.println("ConsoleInputReadTask run() called.");
                String res = null;
                int opt = 0;

                do {
                    try {
                        res = null;
                        System.out.println("Escreva a opção:");
                        System.out.println("1-Indicar que é servidor de difusão");
                        System.out.println("2-Indicar que é cliente de difusão");
                        System.out.println("3-Pedido de rota");
                        System.out.println("4-Pedido de consulta de informação");
                        System.out.println("5-Pedido de submissão de informação");
                        opt = sc.nextInt();
                        switch (opt) {
                            case 1:
                                // TODO
                                difusion_server = true;
                                System.out.println("Nó servidor");
                                break;
                            case 2:
                                // TODO
                                difusion_client = true;
                                System.out.println("Nó cliente");
                                break;
                            case 3:
                                System.out.println("Escreva IP destino: ");
                                res = ipnode + sc.next();
                                InetAddress ip = InetAddress.getByName(res);
                                Thread serverRR = serverRouteRequest(ip);
                                serverRR.start();
                                break;
                            case 4:
                                // TODO
                                if (difusion_client) {
                                    System.out.println("Escreva IP destino: ");
                                    res = ipnode + sc.next();
                                    InetAddress ipDestino = InetAddress.getByName(res);
                                    if (no.getRouteTable().getCaminhos().containsKey(ipDestino)) {
                                        System.out.println("Efetuado pedido de consulta.");
                                        res = null;
                                        Thread getInfFromNode = new Thread(getInfFromNode(ipDestino, opt, null));
                                        getInfFromNode.start();
                                    } else {
                                        System.out.println("Não há caminho para esse nó!");
                                    }
                                } else {
                                    System.out.println("Nó não é cliente");
                                }
                                break;
                            case 5:
                                if (difusion_client) {
                                    System.out.println("Escreva IP destino: ");
                                    res = ipnode + sc.next();
                                    InetAddress ipDestino = InetAddress.getByName(res);
                                    if (no.getRouteTable().getCaminhos().containsKey(ipDestino)) {
                                        String info = null;
                                        System.out.println("Escreva info: ");
                                        sc.nextLine();
                                        info = sc.nextLine();
                                        System.out.println("Efetuado pedido de consulta.");
                                        res = null;
                                        Thread getInfFromNode = new Thread(getInfFromNode(ipDestino, opt, info));
                                        getInfFromNode.start();

                                    } else {
                                        System.out.println("Não há caminho para esse nó!");
                                    }
                                } else {
                                    System.out.println("Nó não é cliente!");
                                }
                                break;
                            default:
                                System.out.println("Exitting...");
                                break;
                        }
                    } catch (UnknownHostException ex) {
                        Logger.getLogger(UdpBroadcaster.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } while (opt != 0);
            }
        });
    }

    public static void main(String[] args) throws SocketException {
        synchronized (UdpBroadcaster.class) {
            try {
                GROUP = (InetAddress) InetAddress.getByName(MCAST_ADDR);

                // START CONSOLE
                Thread consoleThread = console();
                consoleThread.start();

                // START HELLO
                Thread serverHello = serverHello();
                serverHello.start();
                Thread clientHello = clientHello();
                clientHello.start();

                Thread clientRR = clientRouteRequest();
                clientRR.start();
                Thread clientRouteReply = clientRouteReply();
                clientRouteReply.start();

                Thread receiveInfFromNode = receiveInfFromNode();
                receiveInfFromNode.start();

                Thread UdpsenderInfFromNode = UdpsenderInfFromNode();
                UdpsenderInfFromNode.start();

                serverHello.join();
                clientRR.join();
                clientRouteReply.join();
                receiveInfFromNode.join();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static Thread clientHello() {
        return new Thread(new Runnable() {

            public void run() {
                MulticastSocket multicastSocket = null;
                try {
                    multicastSocket = new MulticastSocket(PORT);
                    multicastSocket.setReuseAddress(true);
                    multicastSocket.joinGroup(GROUP);

                    while (true) {
                        byte[] receiveData = new byte[64 * 1024];
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, 64 * 1024, GROUP, PORT);
                        multicastSocket.receive(receivePacket);
                        ByteArrayInputStream bais = new ByteArrayInputStream(receiveData);
                        ObjectInputStream ois = new ObjectInputStream(bais);
                        Object obj = (Object) ois.readObject();
                        if (obj instanceof Message) {
                            Message msg = (Message) obj;
                            ois.close();
                            try {
                                int res = 0;
                                Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
                                for (NetworkInterface netint : Collections.list(nets)) {
                                    Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                                    for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                                        if ((inetAddress.equals(receivePacket.getAddress()))) {
                                            no.setIpAddress(receivePacket.getAddress());
                                            res = 1;
                                        }
                                    }
                                }
                                if (res == 0) {

                                    if (!no.getRouteTable().getVizinhos().containsKey(receivePacket.getAddress())
                                            || no.getRouteTable().getVizinhos().get(receivePacket.getAddress())
                                                    .getSaltos() > 1) {
                                        Vizinho v = new Vizinho(receivePacket.getAddress(), 1);
                                        addEntry(receivePacket.getAddress(), v, timeoutHello);
                                        ArrayList<InetAddress> al2 = new ArrayList<InetAddress>(20);
                                        al2.add(receivePacket.getAddress());
                                        addEntryCaminhos(receivePacket.getAddress(), al2, timeoutHello);
                                    }

                                    if (msg.getRt() != null && no.getIpAddress() != null) {

                                        for (InetAddress ipaddress : msg.getRt().getVizinhos().keySet()) {

                                            if ((!no.getIpAddress().equals(ipaddress))
                                                    && no.getRouteTable().getVizinhos()
                                                            .containsKey(receivePacket.getAddress())
                                                    && msg.getRt().getVizinhos().get(ipaddress).getSaltos() < 2) {

                                                if (no.getRouteTable().getVizinhos().containsKey(ipaddress)) {
                                                    if (no.getRouteTable().getVizinhos().get(ipaddress)
                                                            .getSaltos() > msg.getRt().getVizinhos().get(ipaddress)
                                                                    .getSaltos()) {
                                                        if (msg.getRt().getCaminhos().get(ipaddress) != null) {
                                                            ArrayList<InetAddress> al = new ArrayList<InetAddress>(20);
                                                            al.add(receivePacket.getAddress());
                                                            al.add(ipaddress);
                                                            addEntryCaminhos(ipaddress, al, timeoutHello);
                                                            Vizinho v2 = new Vizinho(receivePacket.getAddress(),
                                                                    msg.getRt().getVizinhos().get(ipaddress).getSaltos()
                                                                            + 1);
                                                            addEntry(ipaddress, v2, timeoutHello);
                                                            //
                                                        }
                                                    }
                                                }

                                                if (msg.getRt().getVizinhos().get(ipaddress) != null
                                                        && !no.getRouteTable().getVizinhos().containsKey(ipaddress)
                                                        && msg.getRt().getVizinhos().get(ipaddress).getSaltos() < 2) {

                                                    ArrayList<InetAddress> al = new ArrayList<InetAddress>(20);
                                                    al.add(receivePacket.getAddress());
                                                    al.add(ipaddress);
                                                    addEntryCaminhos(ipaddress, al, timeoutHello);
                                                    Vizinho v2 = new Vizinho(receivePacket.getAddress(),
                                                            msg.getRt().getVizinhos().get(ipaddress).getSaltos() + 1);
                                                    addEntry(ipaddress, v2, timeoutHello);
                                                }
                                            } else {
                                                if (!no.getRouteTable().getVizinhos()
                                                        .containsKey(receivePacket.getAddress())
                                                        || no.getRouteTable().getVizinhos()
                                                                .get(receivePacket.getAddress()).getSaltos() > 1) {
                                                    Vizinho v = new Vizinho(receivePacket.getAddress(), 1);
                                                    addEntry(receivePacket.getAddress(), v, timeoutHello);
                                                    ArrayList<InetAddress> al2 = new ArrayList<InetAddress>(20);
                                                    al2.add(receivePacket.getAddress());
                                                    al2.add(ipaddress);
                                                    addEntryCaminhos(receivePacket.getAddress(), al2, timeoutHello);
                                                }
                                            }
                                        }
                                    }
                                    System.out.println(server_info);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    multicastSocket.close();
                }
            }
        });
    }

    private static Thread serverHello() {
        return new Thread(new Runnable() {

            public void run() {
                MulticastSocket serverSocket = null;
                try {
                    serverSocket = new MulticastSocket();
                    serverSocket.setReuseAddress(true);
                    serverSocket.setTimeToLive(1);

                    try {
                        // envio de routetables
                        while (true) {
                            Thread.sleep(3000);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ObjectOutputStream oos = new ObjectOutputStream(baos);
                            Message msg = new Message("Hello", no.getRouteTable());
                            baos = new ByteArrayOutputStream();
                            oos = new ObjectOutputStream(baos);
                            oos.writeObject(msg);
                            oos.flush();
                            oos.close();
                            byte[] data = baos.toByteArray();
                            DatagramPacket sendPacket = new DatagramPacket(data, data.length, GROUP, PORT);
                            serverSocket.send(sendPacket);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    static void addEntry(final InetAddress key, Vizinho value, long timeout) {
        // set timeout for the key-value pair to say 10 seconds
        no.getRouteTable().getVizinhos().put(key, value);

        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                actionAfterTimeout(key);
            }
        }, timeout);
    }

    static void addEntryCaminhos(final InetAddress key, ArrayList<InetAddress> value, long timeout) {
        // set timeout for the key-value pair to say 10 seconds
        no.getRouteTable().getCaminhos().put(key, value);

        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                actionAfterTimeoutCaminhos(key);
            }
        }, timeout);
    }

    static void actionAfterTimeout(InetAddress key) {
        // do something
        no.getRouteTable().getVizinhos().remove(key);
    }

    static void actionAfterTimeoutCaminhos(InetAddress key) {
        // do something
        no.getRouteTable().getCaminhos().remove(key);
    }

    private static Thread serverRouteRequest(final InetAddress ip) {
        return new Thread(new Runnable() {

            public void run() {
                try {

                    MulticastSocket serverSocket = new MulticastSocket(PORT);
                    try {
                        System.out.println(ip.toString());
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ObjectOutputStream oos = new ObjectOutputStream(baos);
                        RouteRequest rr = new RouteRequest(ip, no.getIpAddress(), timeoutRR, 5,
                                new ArrayList<InetAddress>());
                        System.out.println("Sent RouteRequest: " + rr);
                        oos.writeObject(rr);
                        oos.flush();
                        byte[] dataRr = baos.toByteArray();
                        dataRr = baos.toByteArray();
                        DatagramPacket sendPacket = new DatagramPacket(dataRr, dataRr.length, GROUP, PORT);
                        serverSocket.send(sendPacket);

                        Thread.sleep(3000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(UdpBroadcaster.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    private static Thread clientRouteRequest() {
        return new Thread(new Runnable() {

            public void run() {
                try {
                    MulticastSocket datagramSocket = new MulticastSocket(PORT);
                    while (true) {
                        try {
                            byte[] receiveData = new byte[128000];
                            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                            datagramSocket.receive(receivePacket);
                            ByteArrayInputStream bais = new ByteArrayInputStream(receiveData);
                            ObjectInputStream ois = new ObjectInputStream(bais);
                            Object obj = (Object) ois.readObject();
                            ois.close();
                            int res = 0;
                            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
                            for (NetworkInterface netint : Collections.list(nets)) {
                                Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                                for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                                    if ((inetAddress.equals(receivePacket.getAddress()))) {
                                        no.setIpAddress(receivePacket.getAddress());
                                        res = 1;
                                    }
                                }
                            }
                            if (res == 0 && no.getIpAddress() != null) {
                                if (obj instanceof RouteRequest) {
                                    RouteRequest rr = (RouteRequest) obj;
                                    rr.setRadius(rr.getRadius() - 1);
                                    if (!rr.getIporigem().equals(no.getIpAddress())
                                            && !rr.getCaminho().contains(no.getIpAddress())) {
                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        ObjectOutputStream oos = new ObjectOutputStream(baos);

                                        if (!rr.getCaminho().contains(no.getIpAddress())) {
                                            rr.getCaminho().add(no.getIpAddress());
                                        }

                                        int flag = 0;

                                        if (no.getIpAddress().equals(rr.getIpdestino())) {
                                            rr.getCaminho().add(no.getIpAddress());
                                        }
                                        // MANDAR REPLY SE RECEBEU REQUEST
                                        if (no.getRouteTable().getVizinhos().containsKey(rr.getIpdestino())) {
                                            if ((no.getRouteTable().getVizinhos().get(rr.getIpdestino())
                                                    .getSaltos() <= 2)) {
                                                flag = 1;

                                                if (no.getRouteTable().getVizinhos().get(rr.getIpdestino())
                                                        .getSaltos() == 1) {
                                                    rr.getCaminho().add(no.getRouteTable().getVizinhos()
                                                            .get(rr.getIpdestino()).getIpvizinho());
                                                }

                                                if (no.getRouteTable().getVizinhos().get(rr.getIpdestino())
                                                        .getSaltos() == 2) {
                                                    rr.getCaminho().add(no.getRouteTable().getVizinhos()
                                                            .get(rr.getIpdestino()).getIpvizinho());
                                                    rr.getCaminho().add(rr.getIpdestino());
                                                }

                                                RouteReply rreply = new RouteReply(rr.getCaminho(), rr.getIpdestino(),
                                                        rr.getIporigem(), 5);
                                                rreply.setContador(rreply.getContador() + 1);
                                                oos.writeObject(rreply);
                                                oos.flush();
                                                byte[] dataRr = baos.toByteArray();
                                                dataRr = baos.toByteArray();
                                                DatagramPacket sendPacket = new DatagramPacket(dataRr, dataRr.length,
                                                        GROUP, PORT);
                                                datagramSocket.send(sendPacket);

                                            }

                                        }
                                        // MANDAR REQUEST SE RECEBEU REQUEST
                                        if (flag == 0 && rr.getRadius() > 0) {
                                            oos.writeObject(rr);
                                            oos.flush();
                                            byte[] dataRr = baos.toByteArray();
                                            dataRr = baos.toByteArray();
                                            DatagramPacket sendPacket = new DatagramPacket(dataRr, dataRr.length, GROUP,
                                                    PORT);
                                            datagramSocket.send(sendPacket);
                                        }

                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(UdpBroadcaster.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    private static Thread clientRouteReply() {
        return new Thread(new Runnable() {

            public void run() {
                try {
                    MulticastSocket datagramSocket = new MulticastSocket(PORT);
                    while (true) {
                        try {
                            byte[] receiveData = new byte[128000];
                            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                            datagramSocket.receive(receivePacket);
                            ByteArrayInputStream bais = new ByteArrayInputStream(receiveData);
                            ObjectInputStream ois = new ObjectInputStream(bais);
                            Object obj = (Object) ois.readObject();
                            ois.close();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ObjectOutputStream oos = new ObjectOutputStream(baos);
                            int res = 0;
                            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
                            for (NetworkInterface netint : Collections.list(nets)) {
                                Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                                for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                                    if ((inetAddress.equals(receivePacket.getAddress()))) {
                                        no.setIpAddress(receivePacket.getAddress());
                                        res = 1;
                                    }
                                }
                            }
                            if (res == 0 && no.getIpAddress() != null) {
                                if (obj instanceof RouteReply) {
                                    RouteReply rreply = (RouteReply) obj;
                                    rreply.setRadius(rreply.getRadius() - 1);
                                    if (rreply.getRadius() > 0) {
                                        // NO RECEBIDO E O NO ORIGEM
                                        if (no.getIpAddress() != null && rreply.getCaminho() != null
                                                && rreply.getIporigem().equals(no.getIpAddress())) {
                                            Vizinho v = new Vizinho(rreply.getCaminho().get(0),
                                                    rreply.getCaminho().size());
                                            addEntry(rreply.getIpdestino(), v, timeoutRR);
                                            addEntryCaminhos(rreply.getIpdestino(), rreply.getCaminho(), timeoutRR);
                                            System.out.println("Reached the original node, no need to send reply");
                                        } // MANDAR REPLY SE RECEBEU REPLY E N É O NO ORIGEM
                                        else {
                                            if (!rreply.getIporigem().equals(no.getIpAddress())
                                                    && rreply.getCaminho().contains(no.getIpAddress())
                                                    && rreply.getRadius() > 0) {
                                                oos.writeObject(rreply);
                                                oos.flush();
                                                byte[] dataRr = baos.toByteArray();
                                                dataRr = baos.toByteArray();
                                                DatagramPacket sendPacket = new DatagramPacket(dataRr, dataRr.length,
                                                        GROUP, PORT);
                                                datagramSocket.send(sendPacket);
                                            }
                                            if (!no.getRouteTable().getVizinhos().containsKey(rreply.getIpdestino())
                                                    && !no.getRouteTable().getCaminhos()
                                                            .containsKey(rreply.getIpdestino())
                                                    && rreply.getCaminho().contains(no.getIpAddress())
                                                    && !no.getIpAddress().equals(rreply.getIpdestino())
                                                    && rreply.getRadius() > 0) {
                                                ArrayList al = new ArrayList(20);
                                                for (int i = rreply.getCaminho().indexOf(no.getIpAddress())
                                                        + 1; i < rreply.getCaminho().size(); i++) {
                                                    al.add(rreply.getCaminho().get(i));
                                                }
                                                Vizinho v = new Vizinho(no.getIpAddress(), al.size());
                                                addEntry(rreply.getIpdestino(), v, timeoutRR);
                                                addEntryCaminhos(rreply.getIpdestino(), al, timeoutRR);
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(UdpBroadcaster.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    private static Thread getInfFromNode(final InetAddress ipdestino, final int opt, final String info) {
        return new Thread(new Runnable() {

            public void run() {

                try {
                    if (difusion_client) {

                        if (no.getIpAddress() != null) {
                            Socket socket = new Socket(no.getIpAddress(), 9999);
                            OutputStream oos = socket.getOutputStream();
                            ObjectOutputStream objectoutputstream = new ObjectOutputStream(oos);
                            System.out.println("Connected");
                            MessageInfo msg = new MessageInfo(no.getIpAddress(), ipdestino, opt, info);
                            System.out.println("Get info message.");
                            objectoutputstream.writeObject(msg);

                            System.out.println("Closing socket");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static Thread receiveInfFromNode() {
        return new Thread(new Runnable() {

            public void run() {

                try {
                    while (true) {
                        MulticastSocket ds = new MulticastSocket(9999);
                        ServerSocket ss = new ServerSocket(9999);
                        System.out.println("Awaiting connections...");
                        Socket socket = ss.accept();
                        System.out.println("Connection from " + socket);

                        InputStream inputStream = socket.getInputStream();
                        ObjectInputStream ois = new ObjectInputStream(inputStream);
                        Object msg = (Object) ois.readObject();

                        socket.close();

                        if (msg instanceof MessageInfo) {
                            MessageInfo msginfo = (MessageInfo) msg;
                            System.out.println("Received message " + msginfo);

                            if (no.getIpAddress() != null && msginfo.getIpdestino() != null
                                    && no.getRouteTable().getCaminhos().containsKey(msginfo.getIpdestino())
                                    && !no.getIpAddress().equals(msginfo.getIpdestino())) {

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                ObjectOutputStream oos = new ObjectOutputStream(baos);

                                oos.writeObject(msginfo);
                                oos.flush();
                                oos.close();
                                byte[] dataRr = baos.toByteArray();
                                dataRr = baos.toByteArray();
                                DatagramPacket sendPacket = new DatagramPacket(dataRr, dataRr.length,
                                        no.getRouteTable().getCaminhos().get(msginfo.getIpdestino()).get(0), PORT);
                                ds.send(sendPacket);
                                ds.close();

                            }
                        }
                        ss.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static Thread UdpsenderInfFromNode() {
        return new Thread(new Runnable() {

            public void run() {

                try {
                    while (true) {
                        MulticastSocket ds = new MulticastSocket(PORT);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ObjectOutputStream oos = new ObjectOutputStream(baos);
                        byte[] receiveData = new byte[128000];
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        ds.receive(receivePacket);
                        ByteArrayInputStream bais = new ByteArrayInputStream(receiveData);
                        ObjectInputStream ois = new ObjectInputStream(bais);
                        Object obj = (Object) ois.readObject();
                        ois.close();

                        if (no.getIpAddress() != null) {
                            if (obj instanceof MessageInfo) {

                                MessageInfo msginfo = (MessageInfo) obj;
                                System.out.println("Received info msg:" + msginfo.getInfo());

                                if (!no.getIpAddress().equals(msginfo.getIporigem())) {
                                    if (no.getRouteTable().getCaminhos().containsKey(msginfo.getIpdestino())) {

                                        oos.writeObject(msginfo);
                                        oos.flush();
                                        oos.close();
                                        byte[] dataRr = baos.toByteArray();
                                        dataRr = baos.toByteArray();
                                        DatagramPacket sendPacket = new DatagramPacket(dataRr, dataRr.length,
                                                no.getRouteTable().getCaminhos().get(msginfo.getIpdestino()).get(0),
                                                PORT);
                                        System.out.println(
                                                no.getRouteTable().getCaminhos().get(msginfo.getIpdestino()).get(0));
                                        ds.send(sendPacket);

                                    }
                                    if (no.getIpAddress().equals(msginfo.getIpdestino())) {
                                        if (msginfo.getInfo() != null && difusion_server && msginfo.getOpt() == 5) {
                                            server_info = msginfo.getInfo();

                                            // objectoutputstream.writeObject(msginfo);
                                        } else if (difusion_server && msginfo.getOpt() == 4) {
                                            baos = new ByteArrayOutputStream();
                                            oos = new ObjectOutputStream(baos);
                                            msginfo.setInfo(server_info);
                                            msginfo.setIpdestino(msginfo.getIporigem());
                                            msginfo.setIporigem(no.getIpAddress());

                                            oos.writeObject(msginfo);
                                            oos.flush();
                                            oos.close();
                                            System.out.println("Sent server msg:" + msginfo.toString());
                                            byte[] dataRr = baos.toByteArray();
                                            dataRr = baos.toByteArray();
                                            DatagramPacket sendPacket = new DatagramPacket(dataRr, dataRr.length,
                                                    no.getRouteTable().getCaminhos().get(msginfo.getIpdestino()).get(0),
                                                    PORT);
                                            ds.send(sendPacket);

                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
