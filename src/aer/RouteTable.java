/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aer;

import java.io.Serializable;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 *
 * @author core
 */
public class RouteTable implements Serializable {

    private HashMap<InetAddress, Vizinho> vizinhos;
    private HashMap<InetAddress, ArrayList<InetAddress>> caminhos;

    public RouteTable() {
        vizinhos = new HashMap<InetAddress, Vizinho>(20);
        caminhos = new HashMap<InetAddress, ArrayList<InetAddress>>(20);
    }

    public RouteTable(InetAddress ip) {
        // ipsCaminho = new ArrayList<InetAddress>();
        // ipsCaminho.add(ip);
    }

    public HashMap<InetAddress, Vizinho> getVizinhos() {
        return this.vizinhos;
    }

    public HashMap<InetAddress, ArrayList<InetAddress>> getCaminhos() {
        return this.caminhos;
    }
}
