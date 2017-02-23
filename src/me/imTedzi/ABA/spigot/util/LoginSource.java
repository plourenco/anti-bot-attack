package me.imTedzi.ABA.spigot.util;

import java.net.InetSocketAddress;

public interface LoginSource {

    void setOnlineMode() throws Exception;

    void kick(String message) throws Exception;

    InetSocketAddress getAddress();
}
