package net.floodlightcontroller.core.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.threadpool.IThreadPoolService;

import org.openflow.protocol.OFCounterReply;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;

/**
 * Used for waiting OFCounterReply message from switch
 * when controller send a OFCounterRequest message for 
 * query a counter value. 
 * 
 */
public class OFCounterFuture extends
        OFMessageFuture<List<OFCounterReply>> {

    protected volatile boolean finished;

    public OFCounterFuture(IThreadPoolService tp,
            IOFSwitch sw, int transactionId) {
        super(tp, sw, OFType.COUNTER_REPLY, transactionId);
        init();
    }

    public OFCounterFuture(IThreadPoolService tp,
            IOFSwitch sw, int transactionId, long timeout, TimeUnit unit) {
        super(tp, sw, OFType.COUNTER_REPLY, transactionId, timeout, unit);
        init();
    }

    private void init() {
        this.finished = true;
        this.result = new CopyOnWriteArrayList<OFCounterReply>();
    }

    @Override
    protected void handleReply(IOFSwitch sw, OFMessage msg) {
        OFCounterReply cr = (OFCounterReply) msg;
        List<OFCounterReply> list = new ArrayList<OFCounterReply>();
        list.add(cr);
        synchronized (this.result) {
            this.result.addAll(list);
        }
    }

    @Override
    protected boolean isFinished() {
        return finished;
    }
    
    @Override
    protected void unRegister() {
        super.unRegister();
    }
}
