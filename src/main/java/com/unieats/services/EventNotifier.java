package com.unieats.services;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

/**
 * Broadcasts lightweight change events over localhost UDP so multiple running
 * app instances can synchronize UI updates without heavy polling.
 */
public final class EventNotifier {
    private static final int PORT = 46877; // local-only

    private EventNotifier() {}

    public static void notifyChange(String topic) {
        try (DatagramSocket socket = new DatagramSocket()) {
            byte[] data = topic.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName("127.0.0.1"), PORT);
            socket.send(packet);
        } catch (Exception ignored) {}
    }
}


