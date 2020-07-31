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
public class MessageInfo implements Serializable {

    private InetAddress iporigem;
    private InetAddress ipdestino;
    private String info;
    private int opt;

    public MessageInfo(InetAddress iporigem, InetAddress ipdestino, int opt, String info) {
        this.iporigem = iporigem;
        this.ipdestino = ipdestino;
        this.opt = opt;
        this.info = info;
    }

    public InetAddress getIpdestino() {
        return ipdestino;
    }

    public InetAddress getIporigem() {
        return iporigem;
    }

    public String getInfo() {
        return info;
    }

    public int getOpt() {
        return opt;
    }

    public void setIpdestino(InetAddress ipdestino) {
        this.ipdestino = ipdestino;
    }

    public void setIporigem(InetAddress iporigem) {
        this.iporigem = iporigem;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public void setOpt(int opt) {
        this.opt = opt;
    }

    @Override
    public String toString() {
        return "MessageInfo{" + "iporigem=" + iporigem + ", ipdestino=" + ipdestino + ", info=" + info + ", opt=" + opt
                + '}';
    }

}
