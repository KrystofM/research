package protocol;

import logger.Logger;
import protocol.models.Packet;
import logger.Level;


public class RetransmissionTimeout implements Runnable {
    private final int ret;
    private final Packet packet;
    private final Protocol protocol;

    public RetransmissionTimeout(int ret, Packet packet, Protocol protocol) {
        this.ret = ret;
        this.packet = packet;
        this.protocol = protocol;
    }

    @Override
    public void run() {
        try {
            Logger.log(Level.INFO, "Timeout started.");
            Thread.sleep(ProtocolSetup.BACKOFF_TIMEOUT_BASE + ProtocolSetup.BACKOFF_TIMEOUT_INCREMENT * (int) (Math.random() * ret));
            Logger.log(Level.INFO, "Timeout expired.");

            if(ret == ProtocolSetup.RETS_FOR_LOST_NEIGHBOUR) {
                protocol.removeAFKNeighbours(packet);
                return;
            }

            // when time is out, we retransmit it
            Logger.log(Level.INFO, "Timeout expired.");
            protocol.addRetransmissionToQueue(packet);
        } catch (InterruptedException e) {
            //acks received so delete thread from array
            Logger.log(Level.INFO, "Received acks: timeout interrupted");
            return;
        }



    }

}
