package protocol;

import client.*;
import protocol.models.Packet;
import logger.Level;
import logger.Logger;
import protocol.reachability.ReachabilityUpdater;


import java.util.*;
import java.util.concurrent.*;


/**
* This is just some example code to show you how to interact 
* with the server using the provided 'Client' class and two queues.
* Feel free to modify this code in any way you like!
*/

public class Protocol {
    private boolean fullWindow;
    private boolean freeChannel;

    private int messageSequenceNumber; // start message sequence number as a random number

    private int IPAddress;

    private final BlockingQueue<Message> receivedQueue;
    private final BlockingQueue<Message> sendingQueue;

    private final Deque<Packet> forwardingQueue;
    private final Deque<Packet> ownMessagesQueue;

    /**
     * Map of packets and their respective neighbours that
     * have acknoleged the packet.
     */
    private final Map<Packet, Set<Integer>> ackList;
    private final Queue<Packet> confirmedAckList;

    /**
     * Updates or sets timer for reachable nodes
     */
    private final Map<Integer, Thread> reachableNodes;
    /**
     * List of packets and their current timeout thread
     */
    private final Map<Packet, Thread> timeoutList;

    /**
     * Counting how many times we have retransmitted a packet
     */
    private final Map<Packet, Integer> retCountPerPacket;

    /**
     * makes sure other nodes know of your existence if you don't send many messages
     */
    private final Thread reachabilityUpdater;

    /**
     * Set of IP addresses of the neighbours
     */
    private final Set<Integer> neighbours;

    /**
     * Listener of the messages and important state changes
     */
    private final ProtocolListener protocolListener;

    public Protocol(ProtocolListener protocolListener) {
        this.protocolListener = protocolListener;

        this.freeChannel = true;
        this.fullWindow = false;

        this.confirmedAckList = new ConcurrentLinkedQueue<>();
        this.forwardingQueue = new ConcurrentLinkedDeque<>();
        this.ownMessagesQueue = new ConcurrentLinkedDeque<>();
        this.ackList = new ConcurrentHashMap<>();
        this.timeoutList = new ConcurrentHashMap<>();
        this.neighbours = new HashSet<>();
        this.retCountPerPacket = new ConcurrentHashMap<>();
        this.reachableNodes = new ConcurrentHashMap<>();

        this.messageSequenceNumber =  (int) (Math.random() * 256);
        this.IPAddress = (int) (Math.random() * 256);

        this.receivedQueue = new LinkedBlockingQueue<>();
        this.sendingQueue = new LinkedBlockingQueue<>();

        new Client(
                ProtocolSetup.SERVER_IP,
                ProtocolSetup.SERVER_PORT,
                ProtocolSetup.FREQUENCY,
                receivedQueue,
                sendingQueue
        );

        new Thread(new Receiver(this)).start(); // Start thread to handle received messages!
        new Thread(new Transmitter(this)).start(); // Start thread to transmit messages!
        (reachabilityUpdater = new Thread(new ReachabilityUpdater(this))).start();
    }

    public synchronized void sendPacket(Packet packet) {
        try {
            sendingQueue.put(new Message(MessageType.DATA, packet.toBytes()));

            //if ackList contains the packet, we are still waiting for acks for this packet,
            //so the packet should be in the sendingWindow
            //if ackList doesn't contain the packet, we don't need to wait for acks,
            // so the packet is removed from our sendingWindow
            if(ackList.containsKey(packet)) {
                setFullWindow(true);

                // adding packet to timeoutList . Timeout between TIMEOUT_LENGTH and 2*TIMEOUTLENGTH,
                // so there's less of a chance of collision when nodes retransmit from interferance range
                if (!retCountPerPacket.containsKey(packet)) {
                    retCountPerPacket.put(packet, 1);
                }
                Thread th = new Thread(new RetransmissionTimeout(retCountPerPacket.get(packet), packet, this));
                th.start();
                timeoutList.put(packet, th);
            }

        } catch (InterruptedException e) {
            Logger.log(Level.ERROR, "Failed to transmit!");
        }
    }

    public synchronized void sendMessage(String input, boolean p2p, int destination) { //what to do when user inputs a string
        // Break message into packets with headers
        int packetFragmentsCount = 1 + ((!p2p) ? (input.length() / Packet.PAYLOAD_LENGTH) : (input.length() / (Packet.P2P_PAYLOAD_LENGTH))) ;
        if (!p2p) {
            displayMessage(input, IPAddress);
        } else {
            displayP2PMessage(input, IPAddress, destination);
        }
        // making packets based on length of message
        for (int i = 0; i < packetFragmentsCount; i++) {
            boolean isLast = i == packetFragmentsCount - 1;
            int payloadLength = p2p ? Packet.P2P_PAYLOAD_LENGTH : Packet.PAYLOAD_LENGTH;
            int datalen = isLast ? input.length() % payloadLength : payloadLength;
            Logger.log(Level.INFO,"Creating data packet");

            Packet newPacket;
            if(!p2p) {
                newPacket = new Packet(
                        messageSequenceNumber,
                        IPAddress,
                        IPAddress,
                        !isLast,
                        false,
                        datalen,
                        input.substring(i * Packet.PAYLOAD_LENGTH, i * Packet.PAYLOAD_LENGTH + datalen),
                        false,
                        0
                );
            } else {
                newPacket = createDmPacket(
                        input.substring(i*(Packet.P2P_PAYLOAD_LENGTH),
                        i*(Packet.P2P_PAYLOAD_LENGTH) + datalen),
                        destination,
                        !isLast
                );
            }
            // add to ackList since once we get acks we have already seen this packet
            addMessagePacketToQueue(newPacket);
        }
    }

    public void addMessagePacketToQueue(Packet packet) {
        ackList.put(packet, new HashSet<>());
        addToMessagesQueue(packet);
        incrementPMS();
    }

    public void addTopologyPacketToQueue() {
        Logger.log(Level.INFO, "Creating topology packet");
        Packet topologyPacket = createTopologyPacket();
        addMessagePacketToQueue(topologyPacket);
    }

    public Packet createDmPacket(String msg, int destination, boolean frag) {
        return new Packet(
                messageSequenceNumber,
                IPAddress,
                IPAddress,
                frag,
                false,
                msg.length(),
                msg,
                true,
                destination
        );
    }

    private Packet createTopologyPacket() {
        return new Packet(
                messageSequenceNumber,
                IPAddress,
                IPAddress,
                false,
                false,
                0,
                "",
                false,
                0
        );
    }

    public synchronized void addToForwardingQueue(Packet packet) {
        packet.setRet(false);
        packet.setLastHopIP(IPAddress);
        forwardingQueue.add(packet);
    }

    public synchronized void addRetransmissionToQueue(Packet packet) {
        Logger.log(Level.INFO,"Retransmitting.");
        // increase retransmission time
        retCountPerPacket.put(packet, retCountPerPacket.get(packet) + 1);
        packet.setRet(true);
        forwardingQueue.add(packet);
        setFullWindow(false);
    }

    public void removeAFKNeighbours(Packet packet) {
        ArrayList<Integer> removedNeighbours = new ArrayList<>();
        for(int neighbour : getNeighbours()) {
            if(!ackList.get(packet).contains(neighbour)) {
                Logger.log(Level.INFO, "Removed node " + neighbour + " from neighbours");
                removedNeighbours.add(neighbour);
            }
        }
        removedNeighbours.forEach(neighbours::remove);
        allAcksReceivedForPacket(packet);
    }

    public void removeReachableNode(int node) {
        Logger.log(Level.INFO, "Removing node " + node + " from reachability list");
        reachableNodes.remove(node);
    }

    /**
     * Removes a packet from the ackList and timeoutList and interrupts its timeout thread
     * @param packet packet to remove
     */
    public synchronized void allAcksReceivedForPacket(Packet packet){
        assert ackList.get(packet).containsAll(neighbours);
        if (timeoutList.containsKey(packet)) {
            Logger.log(Level.INFO,"Trying to interrupt Timeout");
            timeoutList.get(packet).interrupt();
            timeoutList.remove(packet);
            retCountPerPacket.remove(packet);
        }
        ackList.remove(packet);
        // adding acknowledges packets to confirmed ack list
        addToConfirmedAckList(packet);
        // all acks received time to allow for transmission of new packet
        setFullWindow(false);
    }

    public synchronized void addToConfirmedAckList(Packet packet) {
        confirmedAckList.add(packet);
        if (confirmedAckList.size() >= 128) {
            confirmedAckList.poll();
        }
    }

    public void changeIPAddress(){
        Logger.log(Level.SEVERE, "IP Collision detected, changing IP");
        int oldIP = getIPAddress();
        while(true) {
            IPAddress = (int) (Math.random() * 256);
            if (!reachableNodes.containsKey(IPAddress)) {
                break;
            }
        }
        protocolListener.changeOwnIP(oldIP, IPAddress);
    }

    /**
     * Increment the Personal Message Sequence Number (PMS) and wrap around of it reaches 255
     */
    public void incrementPMS() {
        messageSequenceNumber = messageSequenceNumber == 255 ? 0 : messageSequenceNumber + 1;
    }

    public Thread getReachabilityUpdater() {
        return reachabilityUpdater;
    }

    public synchronized boolean isFullWindow() {
        return fullWindow;
    }

    public synchronized void setFullWindow(boolean fullWindow) {
        this.fullWindow = fullWindow;
    }

    public synchronized boolean isFreeChannel() {
        return freeChannel;
    }

    public synchronized void setFreeChannel(boolean freeChannel) {
        this.freeChannel = freeChannel;
    }

    public synchronized void addToMessagesQueue(Packet packet) {
        ownMessagesQueue.add(packet);
    }

    public synchronized boolean hasEmptyMessagesQueue(){
        return ownMessagesQueue.isEmpty();
    }

    public synchronized boolean hasEmptyForwardingQueue(){
        return forwardingQueue.isEmpty();
    }

    public synchronized Packet pollForwardingQueue(){
        return forwardingQueue.poll();
    }

    public synchronized Packet pollMessagesQueue(){
        return ownMessagesQueue.poll();
    }

    public int getIPAddress(){
        return IPAddress;
    }

    public synchronized void addToNeighbours(int neighbour) {
        neighbours.add(neighbour);
    }

    public Map<Packet, Set<Integer>> getAckList() {
        return ackList;
    }

    public synchronized void addToAckList(Packet packet, Set<Integer> ackSet){
        ackList.put(packet, ackSet);
    }

    public synchronized Map<Integer, Thread> getReachableNodes() {return this.reachableNodes;}

    public synchronized Queue<Packet> getConfirmedAckList() {
        return confirmedAckList;
    }

    public synchronized BlockingQueue<Message> getReceivedQueue() {
        return receivedQueue;
    }

    public Set<Integer> getNeighbours() {
        return neighbours;
    }

    public void displayMessage(String message, int ip) {
        protocolListener.displayMessage(message, ip);
    }

    public void displayP2PMessage(String message, int originIp, int destinationIp) {
        protocolListener.displayP2PMessage(message, originIp, destinationIp);
    }

}

