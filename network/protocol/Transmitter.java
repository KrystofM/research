package protocol;

/**
 * Handles continuous streaming of data in queues.
 * Call the protocol for timeouts
 */
public class Transmitter implements Runnable {
    private final Protocol protocol;

    public Transmitter(Protocol protocol) {
        this.protocol = protocol;
    }

    public synchronized void transmitFromQueue() { //for use in transmitterThread
        if(protocol.isFullWindow()) { return; }

         if(!protocol.hasEmptyForwardingQueue()) {

             waitRandomTime(0);

             if (!protocol.isFreeChannel()) {
                return;
             }
             protocol.sendPacket(protocol.pollForwardingQueue());
             return;
        }

        if(!protocol.hasEmptyMessagesQueue()) {
            //wait longer random time
            waitRandomTime(ProtocolSetup.RANDOM_TRANSMISSION_DELAY);

            if (!protocol.isFreeChannel()) {
                return;
            }
            protocol.getReachabilityUpdater().interrupt();
            protocol.sendPacket(protocol.pollMessagesQueue());
        }

    }

    private synchronized void waitRandomTime(int minimumWaitTime) {
        try {
            Thread.sleep((int) ((Math.random()*4*ProtocolSetup.RANDOM_TRANSMISSION_DELAY) + minimumWaitTime));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void run() {
        while (true) {
            if (protocol.isFreeChannel()) {
                transmitFromQueue();
            }
        }
    }
}
