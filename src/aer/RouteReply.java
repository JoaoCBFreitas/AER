/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aer;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 *
 * @author core
 */
public class RouteReply implements Serializable{
    
    private ArrayList<InetAddress> caminho;
    private int contador;
    private InetAddress ipdestino;
    private InetAddress iporigem;
    private int radius;
    
    
    public RouteReply(ArrayList<InetAddress> caminho,InetAddress ipdestino,InetAddress iporigem, int radius){
        this.caminho = caminho;
        this.contador = 0;
        this.ipdestino = ipdestino;
        this.iporigem = iporigem;
        this.radius = radius;
    }

    public ArrayList<InetAddress> getCaminho() {
        return caminho;
    }
    
    public int getContador(){
        return contador;
    }

    public InetAddress getIpdestino(){
        return ipdestino;
    }
    
    public InetAddress getIporigem(){
        return iporigem;
    }
    
    public int getRadius() {
        return radius;
    }
    
    public void setContador(int contador){
        this.contador = contador;
    }
    
    public void setRadius(int radius) {
        this.radius = radius;
    }

    @Override
    public String toString() {
        return "RouteReply{" + "caminho=" + caminho + ", contador=" + contador + ", ipdestino=" + ipdestino + ", iporigem=" + iporigem + ", radius=" + radius + '}';
    }
    
}
