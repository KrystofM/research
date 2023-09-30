package protocol.models;

import logger.Level;
import logger.Logger;

import java.nio.ByteBuffer;
import java.util.Objects;

public class Packet {
    private int pms; // personal message sequence number
    private int sourceIP;
    private int lastHopIP;
    private boolean frag;
    private boolean ret;
    private int dataLength;
    private String payload;

    private boolean p2p;
    private int destIp;

    public static final int HEADER_LENGTH = 4;
    public static final int P2P_HEADER_LENGTH = HEADER_LENGTH + 1;
    public static final int PACKET_LENGTH = 32;
    public static final int PAYLOAD_LENGTH = 28;
    public static final int P2P_PAYLOAD_LENGTH = PAYLOAD_LENGTH - 1;

    private final int headerLength;

    public Packet(int pms, int sourceIP, int lastHopIP, boolean frag, boolean ret, int dataLength, String payload, boolean p2p, int destIp) {
        this.pms = pms;
        this.sourceIP = sourceIP;
        this.lastHopIP = lastHopIP;
        this.frag = frag;
        this.ret = ret;
        this.dataLength = dataLength;
        this.payload = payload;
        this.p2p = p2p;
        this.destIp = destIp;
        this.headerLength = p2p ? P2P_HEADER_LENGTH : HEADER_LENGTH;
    }


    public Packet(ByteBuffer bytes) {
        this.pms = (short) (bytes.get(0) & 0xff);
        this.sourceIP = (short) (bytes.get(1) & 0xff);
        this.lastHopIP = (short) (bytes.get(2) & 0xff);
        this.frag = (bytes.get(3) >> 7 & 1) != 0;
        this.ret = (bytes.get(3) >> 6 & 1) != 0;
        this.dataLength = ((short) (bytes.get(3) & 0x1f)); // only the last 5 bits for data length
        this.p2p = (bytes.get(3) >> 5 & 1) != 0;
        if(p2p) destIp = (short) (bytes.get(4) & 0xff);
        this.headerLength = p2p ? P2P_HEADER_LENGTH : HEADER_LENGTH;


        String message = "";

        for(int b = headerLength; b < headerLength + dataLength; b++) {
            message += (char) bytes.get(b);
        }
        payload = message;

        Logger.log(Level.PACKET, this.toString());
    }

    public ByteBuffer toBytes(){
        ByteBuffer res = ByteBuffer.allocate(PACKET_LENGTH);
        res.put(0, (byte) pms);
        res.put(1, (byte) sourceIP);
        res.put(2, (byte) lastHopIP);
        int lastByte = dataLength;
        lastByte = ret ? lastByte + 64 : lastByte;
        lastByte = frag ? lastByte + 128 : lastByte;
        lastByte = p2p ? lastByte  + 32 : lastByte;
        res.put(3, (byte) lastByte);

        char[] charArray = payload.toCharArray();
        if(p2p) res.put(HEADER_LENGTH, (byte) destIp);
        for (int i = 0; i < charArray.length; i++) {
            res.put(i + headerLength, (byte) charArray[i]);
        }

        Logger.log(Level.PACKET, this.toString());

        return res;
    }


    public int getDestIp() {return this.destIp;}


    public int getPMS() {
        return this.pms;
    }

    public int getSourceIP() {
        return this.sourceIP;
    }

    public int getLastHopIP() {
        return this.lastHopIP;
    }

    public String getPayload() { return this.payload; }

    public boolean isFrag() {
        return this.frag;
    }

    public boolean isRet() {
        return this.ret;
    }

    public int getDataLength() {
        return this.dataLength;
    }

    public void setPMS(int pms) {
        this.pms = pms;
    }

    public void setSourceIP(int sourceIP) {
        this.sourceIP = sourceIP;
    }

    public void setLastHopIP(int lastHopIP) {
        this.lastHopIP = lastHopIP;
    }

    public void setFrag(boolean frag) {
        this.frag = frag;
    }

    public void setRet(boolean ret) {
        this.ret = ret;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    public void setPayload(String payload) {this.payload = payload;}

    public boolean isP2p() {return p2p;}

    @Override
    public String toString() {
        return  "Packet information: " +
                "PMS: " + pms + "  " +
                "SourceIP: " + sourceIP + "  " +
                "LastHopIP: " + lastHopIP + "  " +
                "Fragment: " + frag + "  " +
                "Retransmission: " + ret + "  " +
                "DataLength: " + dataLength + "  " +
                "Payload: " + payload + "  " +
                "p2p " + p2p + "  " +
                "DestIp: " + destIp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Packet packet = (Packet) o;
        return pms == packet.pms && sourceIP == packet.sourceIP;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pms, sourceIP);
    }
}
