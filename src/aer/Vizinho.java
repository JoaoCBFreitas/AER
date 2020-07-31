/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aer;

import java.io.Serializable;
import java.net.InetAddress;

/**
 *
 * @author core
 */
public class Vizinho implements Serializable {
    
    private InetAddress ipvizinho;
    private Integer saltos;

    public Vizinho(InetAddress ipvizinho, Integer saltos) {
        this.ipvizinho = ipvizinho;
        this.saltos = saltos;
    }

    public InetAddress getIpvizinho() {
        return ipvizinho;
    }

    public Integer getSaltos() {
        return saltos;
    }

    public void setIpvizinho(InetAddress ipvizinho) {
        this.ipvizinho = ipvizinho;
    }

    public void setSaltos(Integer saltos) {
        this.saltos = saltos;
    }

    @Override
    public String toString() {
        return "IPNode{" + ipvizinho + ", saltos=" + saltos + "}";
    }
    
    
}