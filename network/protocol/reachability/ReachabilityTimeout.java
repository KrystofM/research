package protocol.reachability;

import protocol.Protocol;
import protocol.ProtocolSetup;

public class ReachabilityTimeout implements Runnable{
    private final Protocol protocol;
    private final int node;

    public ReachabilityTimeout(Protocol protocol, int node) {
        this.protocol = protocol;
        this.node = node;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(ProtocolSetup.REACHABILITY_TTL);
            protocol.removeReachableNode(node);
        } catch (InterruptedException e) {
            run();
        }
    }

}
