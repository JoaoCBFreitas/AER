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
public class Node implements Serializable{
    
    private InetAddress ipAddress;
    private RouteTable rt;
    
    public Node(){
        this.ipAddress = null;
        this.rt = new RouteTable();
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public RouteTable getRouteTable() {
        return rt;
    }

    public void setIpAddress(InetAddress ipAddress) {
        this.ipAddress = ipAddress;
    }
    
}
