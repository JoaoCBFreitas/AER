/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aer;

import java.io.Serializable;

/**
 *
 * @author core
 */
public class Message implements Serializable{
    
    private String hello;
    private RouteTable rt;

    public Message(String hello, RouteTable rt) {
        this.hello = hello;
        this.rt = rt;
    }

    public String getHello() {
        return this.hello;
    }

    public RouteTable getRt() {
        return this.rt;
    }

    public void setHello(String hello) {
        this.hello = hello;
    }

    public void setRt(RouteTable rt) {
        this.rt = rt;
    }

    @Override
    public String toString() {
        return "Message{" + "msg_String=" + hello + ", rt=" + rt + '}';
    }
    
    
}
