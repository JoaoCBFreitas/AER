/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aer;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 *
 * @author core
 */
public class RouteRequest implements Serializable{
 
    
    private InetAddress iporigem;
    private InetAddress ipdestino;
    private long timeout;
    private int radius;
    private ArrayList<InetAddress> caminho;

    public RouteRequest(InetAddress ipdestino,InetAddress iporigem,long timeout, int radius, ArrayList<InetAddress> caminho) {
        this.ipdestino = ipdestino;
        this.iporigem = iporigem;
        this.timeout = timeout;
        this.radius = radius;
        this.caminho = caminho;
    }

    public InetAddress getIporigem() {
        return iporigem;
    }
    
    public InetAddress getIpdestino() {
        return ipdestino;
    }
    
    public long getTimeout() {
        return timeout;
    }
    
    public int getRadius() {
        return radius;
    }

    public ArrayList<InetAddress> getCaminho() {
        return caminho;
    }
    
    public void setIporigem(InetAddress iporigem) {
        this.iporigem = iporigem;
    }
    
    public void setIpdestino(InetAddress ipdestino) {
        this.ipdestino = ipdestino;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
    
    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setCaminho(ArrayList<InetAddress> caminho) {
        this.caminho = caminho;
    }

    @Override
    public String toString() {
        return "\nRouteRequest{" + "ipdestino=" + ipdestino + ", radius=" + radius + ", caminho=" + caminho + "}";
    }
}
