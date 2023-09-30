package protocol.reachability;

import protocol.Protocol;
import protocol.ProtocolSetup;

public class ReachabilityUpdater implements Runnable{
    private final Protocol protocol;


    public ReachabilityUpdater(Protocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public void run() {
        try {

            Thread.sleep(ProtocolSetup.REACHABILITY_UPDATE_TIME);
            // If we already have a message in queue we dont have to update reachability
            // If our neighbours are in reachable nodes then a forward will reach everyone
            if (protocol.hasEmptyMessagesQueue()
                    && !(protocol.getNeighbours().equals(protocol.getReachableNodes())
                    && !protocol.hasEmptyForwardingQueue())) {
                protocol.addTopologyPacketToQueue();
            }
            run();
        } catch (InterruptedException e) {
            run();
        }
    }
}
