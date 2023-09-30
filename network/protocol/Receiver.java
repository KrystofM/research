package protocol;

import client.Message;
import logger.Logger;
import protocol.models.Packet;

import java.util.*;
import logger.Level;
import protocol.reachability.ReachabilityTimeout;


public class Receiver implements Runnable {
    private final Protocol protocol;

    /**
     * Map<SourceIP,Message>
     * Stores fragments of messages
     */
    private final Map<Integer, String> messagesMap;


    public Receiver(Protocol protocol) {
        this.protocol = protocol;
        this.messagesMap = new HashMap<>();
    }

    // handling received packet
    public synchronized void handlePacket(Packet packet) {
        //test for IP collision
        if (protocol.getIPAddress() == packet.getLastHopIP() || (protocol.getIPAddress() == packet.getSourceIP()
                && !protocol.getConfirmedAckList().contains(packet) && !protocol.getAckList().containsKey(packet))) {
            //ip collision with other node
            protocol.changeIPAddress();
        }

        protocol.addToNeighbours(packet.getLastHopIP()); // add new neighbour, neighbours is a set so dont need to check

        handleTTL(packet);

        Set<Integer> ackSet = new HashSet<>(); // neighbours that have acknowledged the packet
        boolean alreadySeen = false; // have we seen the packet before?

        if (protocol.getAckList().containsKey(packet)) {
            ackSet = protocol.getAckList().get(packet);
            alreadySeen = true;
        } else if (protocol.getConfirmedAckList().contains(packet)){
            Logger.log(Level.INFO, "Already seen and confirmed");

            //we don't want to add this packet to ackList since its in confirmedAckList
            //but we still should forward if packet is a retransmission
            if (packet.isRet()) {
                // Add data packet to ACK/forwarding queue
                Logger.log(Level.INFO, "Forwarding a retransmission packet.");
                protocol.addToForwardingQueue(packet);
            }
            return;
        }

        // Add lasthop to ackList for the packet
        ackSet.add(packet.getLastHopIP());
        Logger.log(Level.INFO,"Adding to ACK List");
        protocol.addToAckList(packet, ackSet);

        logEvents();

        // checking acks
        if (ackSet.containsAll(protocol.getNeighbours())) {// received all the acks
            Logger.log(Level.INFO, "Removing the packet all acks confirmed");
            protocol.allAcksReceivedForPacket(packet); // remove packet from list of packets that need to be acked
        }

        // Ignore and don't forward packet that you've already seen and is not a retransmission
        if(alreadySeen && !packet.isRet()) {
            return;
        }

        // Add data packet to ACK/forwarding queue
        Logger.log(Level.INFO, "Forwarding packet");
        protocol.addToForwardingQueue(packet);

        // Forward packet, but don't print it
        // Or if no data then no need to print it
        if (alreadySeen || packet.getDataLength() == 0) {
            return;
        }

        handlePayload(packet);
    }

    private void handleTTL(Packet packet) {
        //add or refresh ttl for src of packet
        if(packet.getSourceIP() != protocol.getIPAddress() && protocol.getReachableNodes().containsKey(packet.getSourceIP())) {
            protocol.getReachableNodes().get(packet.getSourceIP()).interrupt();//refresh ttl
        } else if(packet.getSourceIP() != protocol.getIPAddress()) {
            Thread t = new Thread(new ReachabilityTimeout(protocol, packet.getSourceIP()));
            protocol.getReachableNodes().put(packet.getSourceIP(), t);
            t.start();
        }

        //add or refresh ttl for lastHop of packet
        if(protocol.getReachableNodes().containsKey(packet.getLastHopIP())) {
            protocol.getReachableNodes().get(packet.getLastHopIP()).interrupt();//refresh ttl
        } else {
            Thread t = new Thread(new ReachabilityTimeout(protocol, packet.getLastHopIP()));
            protocol.getReachableNodes().put(packet.getLastHopIP(), t);
            t.start();
        }
    }

    private void handlePayload(Packet packet) {
        // Update messagesMap
        String message;
        // If it doesn't exist create new message
        if (!messagesMap.containsKey(packet.getSourceIP())) {
            message = packet.getPayload();
        } else {
            message = messagesMap.get(packet.getSourceIP());
            message += packet.getPayload();
        }
        messagesMap.put(packet.getSourceIP(), message);
        // If (!FRAG) => print tempMessage to UI
        if (!packet.isFrag()) {
            messagesMap.remove(packet.getSourceIP());

            if(packet.isP2p() && packet.getDestIp() == protocol.getIPAddress()) {
                protocol.displayP2PMessage(message, packet.getSourceIP(), packet.getDestIp());
                return;
            }else if(packet.isP2p()) return;

            // displaying the message
            protocol.displayMessage(message, packet.getSourceIP());
        }
    }

    private void logEvents() {
        // printing the neighbours
        String neighboursLog = "Neighbours: ";
        for (Integer neighbourIP : protocol.getNeighbours()) {
            neighboursLog += neighbourIP + " ";
        }
        Logger.log(Level.INFO, neighboursLog);

        // printing the acklist
        for (Map.Entry<Packet, Set<Integer>> entry : protocol.getAckList().entrySet()) {
            Logger.log(Level.INFO, "IP+PMS: " + entry.getKey().getSourceIP() + "+" + entry.getKey().getPMS());
            String ackLog = "Ack list: ";
            for (int i: entry.getValue()) {
                ackLog += i + " ";
            }
            Logger.log(Level.INFO, ackLog);
        }
    }

    // Handle messages from the server / audio framework
    public synchronized void run() {
        while(true) {
            try {
                Message m = protocol.getReceivedQueue().take();
                Logger.log(Level.STATUS, m.getType().toString());
                switch (m.getType()) {
                    case BUSY:
                        protocol.setFreeChannel(false);
                        break;
                    case FREE:
                        protocol.setFreeChannel(true);
                        break;
                    case DATA:
                        Logger.log(Level.INFO, "[DATA] Data received!");
                        handlePacket(new Packet(m.getData()));
                        break;
                    case END:
                        System.exit(0);
                        break;
                    case DONE_SENDING:
                    case HELLO:
                    case SENDING:
                    default:
                        break;
                }
            } catch (InterruptedException e){
                Logger.log(Level.ERROR, "Failed to take from queue: "+e);
            }
        }
    }
}
