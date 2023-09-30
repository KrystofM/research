package protocol;

public interface ProtocolListener {
    void displayMessage(String message, int ip);
    void displayP2PMessage(String message, int originIp, int destinationIp);
    void changeOwnIP(int oldIp, int newIp);
}
