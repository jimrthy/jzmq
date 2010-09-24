/*
    Copyright (c) 2007-2010 iMatix Corporation

    This file is part of 0MQ.

    0MQ is free software; you can redistribute it and/or modify it under
    the terms of the Lesser GNU General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    0MQ is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    Lesser GNU General Public License for more details.

    You should have received a copy of the Lesser GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.zeromq;

/**
 * ZeroMQ JNI Bindings.
 * 
 * @author Gonzalo Diethelm
 * 
 */
public class ZMQ {
    static {
        System.loadLibrary("jzmq");
    }

    // Values for flags in Socket's send and recv functions.
    /**
     * Socket flag to indicate a nonblocking send or recv mode.
     */
    public static final int NOBLOCK = 1;
    /**
     * Socket flag to indicate that more message parts are coming.
     */
    public static final int SNDMORE = 2;

    // Socket types, used when creating a Socket.
    /**
     * Flag to specify a exclusive pair of sockets.
     */
    public static final int PAIR = 0;
    /**
     * Flag to specify a PUB socket, receiving side must be a SUB.
     */
    public static final int PUB = 1;
    /**
     * Flag to specify the receiving part of the PUB socket.
     */
    public static final int SUB = 2;
    /**
     * Flag to specify a REQ socket, receiving side must be a REP.
     */
    public static final int REQ = 3;
    /**
     * Flag to specify the receiving part of a REQ socket.
     */
    public static final int REP = 4;
    /**
     * Flag to specify a XREQ socket, receiving side must be a XREP.
     */
    public static final int XREQ = 5;
    /**
     * Flag to specify the receiving part of a XREQ socket.
     */
    public static final int XREP = 6;
    /**
     * Flag to specify the receiving part of a PUSH socket.
     */
    public static final int PULL = 7;
    /**
     * Flag to specify a PUSH socket, receiving side must be a PULL.
     */
    public static final int PUSH = 8;

    /**
     * @see ZMQ#PULL
     */
    @Deprecated
    public static final int UPSTREAM = PULL;
    /**
     * @see ZMQ#PUSH
     */
    @Deprecated
    public static final int DOWNSTREAM = PUSH;

    /**
     * Create a new Context.
     * 
     * @param ioThreads
     *            Number of threads to use, usually 1 is sufficient for most use cases.
     * @return the Context
     */
    public static Context context(int ioThreads) {
        return new Context(ioThreads);
    }

    /**
     * Inner class: Context.
     */
    public static class Context {

        /**
         * This is an explicit "destructor". It can be called to ensure the corresponding 0MQ
         * Context has been disposed of.
         */
        public void term() {
            finalize();
        }

        /**
         * Create a new Socket within this context.
         * 
         * @param type
         *            the socket type.
         * @return the newly created Socket.
         */
        public Socket socket(int type) {
            return new Socket(this, type);
        }

        /**
         * Create a new Poller within this context.
         * 
         * @param size
         *            the poller size.
         * @return the newly created Poller.
         */
        public Poller poller(int size) {
            return new Poller(this, size);
        }

        /**
         * Class constructor.
         * 
         * @param ioThreads
         *            size of the threads pool to handle I/O operations.
         */
        protected Context(int ioThreads) {
            construct(ioThreads);
        }

        /** Initialize the JNI interface */
        protected native void construct(int ioThreads);

        /** Free all resources used by JNI interface. */
        @Override
        protected native void finalize();

        /**
         * Get the underlying context handle. This is private because it is only accessed from JNI,
         * where Java access controls are ignored.
         * 
         * @return the internal 0MQ context handle.
         */
        private long getContextHandle() {
            return this.contextHandle;
        }

        /** Opaque data used by JNI driver. */
        private long contextHandle;
    }

    /**
     * Inner class: Socket.
     */
    public static class Socket {
        /**
         * This is an explicit "destructor". It can be called to ensure the corresponding 0MQ Socket
         * has been disposed of.
         */
        public void close() {
            finalize();
        }

        /**
         * The 'ZMQ_HWM' option shall set the high water mark for the specified 'socket'. The high
         * water mark is a hard limit on the maximum number of outstanding messages 0MQ shall queue
         * in memory for any single peer that the specified 'socket' is communicating with.
         * 
         * If this limit has been reached the socket shall enter an exceptional state and depending
         * on the socket type, 0MQ shall take appropriate action such as blocking or dropping sent
         * messages. Refer to the individual socket descriptions in linkzmq:zmq_socket[3] for
         * details on the exact action taken for each socket type.
         * 
         * The default 'ZMQ_HWM' value of zero means "no limit".
         * 
         * @return the High Water Mark.
         */
        public long getHWM() {
            return getLongSockopt(HWM);
        }

        /**
         * The 'ZMQ_SWAP' option shall set the disk offload (swap) size for the specified 'socket'.
         * A socket which has 'ZMQ_SWAP' set to a non-zero value may exceed its high water mark; in
         * this case outstanding messages shall be offloaded to storage on disk rather than held in
         * memory.
         * 
         * The value of 'ZMQ_SWAP' defines the maximum size of the swap space in bytes.
         * 
         * @return the number of messages to swap at most.
         */
        public long getSwap() {
            return getLongSockopt(SWAP);
        }

        /**
         * The 'ZMQ_AFFINITY' option shall set the I/O thread affinity for newly created connections
         * on the specified 'socket'.
         * 
         * Affinity determines which threads from the 0MQ I/O thread pool associated with the
         * socket's _context_ shall handle newly created connections. A value of zero specifies no
         * affinity, meaning that work shall be distributed fairly among all 0MQ I/O threads in the
         * thread pool. For non-zero values, the lowest bit corresponds to thread 1, second lowest
         * bit to thread 2 and so on. For example, a value of 3 specifies that subsequent
         * connections on 'socket' shall be handled exclusively by I/O threads 1 and 2.
         * 
         * See also linkzmq:zmq_init[3] for details on allocating the number of I/O threads for a
         * specific _context_.
         * 
         * @return the affinity.
         */
        public long getAffinity() {
            return getLongSockopt(AFFINITY);
        }

        /**
         * The 'ZMQ_IDENTITY' option shall set the identity of the specified 'socket'. Socket
         * identity determines if existing 0MQ infastructure (_message queues_, _forwarding
         * devices_) shall be identified with a specific application and persist across multiple
         * runs of the application.
         * 
         * If the socket has no identity, each run of an application is completely separate from
         * other runs. However, with identity set the socket shall re-use any existing 0MQ
         * infrastructure configured by the previous run(s). Thus the application may receive
         * messages that were sent in the meantime, _message queue_ limits shall be shared with
         * previous run(s) and so on.
         * 
         * Identity should be at least one byte and at most 255 bytes long. Identities starting with
         * binary zero are reserved for use by 0MQ infrastructure.
         * 
         * @return the Identitiy.
         */
        public byte[] getIdentity() {
            return getBytesSockopt(IDENTITY);
        }

        /**
         * The 'ZMQ_RATE' option shall set the maximum send or receive data rate for multicast
         * transports such as linkzmq:zmq_pgm[7] using the specified 'socket'.
         * 
         * @return the Rate.
         */
        public long getRate() {
            return getLongSockopt(RATE);
        }

        /**
         * The 'ZMQ_RECOVERY_IVL' option shall set the recovery interval for multicast transports
         * using the specified 'socket'. The recovery interval determines the maximum time in
         * seconds that a receiver can be absent from a multicast group before unrecoverable data
         * loss will occur.
         * 
         * CAUTION: Excersize care when setting large recovery intervals as the data needed for
         * recovery will be held in memory. For example, a 1 minute recovery interval at a data rate
         * of 1Gbps requires a 7GB in-memory buffer.
         * 
         * @return the RecoveryIntervall.
         */
        public long getRecoveryInterval() {
            return getLongSockopt(RECOVERY_IVL);
        }

        /**
         * The 'ZMQ_MCAST_LOOP' option shall control whether data sent via multicast transports
         * using the specified 'socket' can also be received by the sending host via loopback. A
         * value of zero disables the loopback functionality, while the default value of 1 enables
         * the loopback functionality. Leaving multicast loopback enabled when it is not required
         * can have a negative impact on performance. Where possible, disable 'ZMQ_MCAST_LOOP' in
         * production environments.
         * 
         * @return the Multicast Loop.
         */
        public boolean hasMulticastLoop() {
            return getLongSockopt(MCAST_LOOP) != 0;
        }

        /**
         * The 'ZMQ_SNDBUF' option shall set the underlying kernel transmit buffer size for the
         * 'socket' to the specified size in bytes. A value of zero means leave the OS default
         * unchanged. For details please refer to your operating system documentation for the
         * 'SO_SNDBUF' socket option.
         * 
         * @return the kernel send buffer size.
         */
        public long getSendBufferSize() {
            return getLongSockopt(SNDBUF);
        }

        /**
         * The 'ZMQ_RCVBUF' option shall set the underlying kernel receive buffer size for the
         * 'socket' to the specified size in bytes. A value of zero means leave the OS default
         * unchanged. For details refer to your operating system documentation for the 'SO_RCVBUF'
         * socket option.
         * 
         * @return the kernel receive buffer size.
         */
        public long getReceiveBufferSize() {
            return getLongSockopt(RCVBUF);
        }

        /**
         * The 'ZMQ_RCVMORE' option shall return a boolean value indicating if the multi-part
         * message currently being read from the specified 'socket' has more message parts to
         * follow. If there are no message parts to follow or if the message currently being read is
         * not a multi-part message a value of zero shall be returned. Otherwise, a value of 1 shall
         * be returned.
         * 
         * @return true if there are more messages to receive.
         */
        public boolean hasReceiveMore() {
            return getLongSockopt(RCVMORE) != 0;
        }

        /**
         * The 'ZMQ_HWM' option shall set the high water mark for the specified 'socket'. The high
         * water mark is a hard limit on the maximum number of outstanding messages 0MQ shall queue
         * in memory for any single peer that the specified 'socket' is communicating with.
         * 
         * If this limit has been reached the socket shall enter an exceptional state and depending
         * on the socket type, 0MQ shall take appropriate action such as blocking or dropping sent
         * messages. Refer to the individual socket descriptions in linkzmq:zmq_socket[3] for
         * details on the exact action taken for each socket type.
         * 
         * @param hwm
         *            the number of messages to queue.
         */
        public void setHWM(long hwm) {
            setLongSockopt(HWM, hwm);
        }

        /**
         * Get the Swap. The 'ZMQ_SWAP' option shall set the disk offload (swap) size for the
         * specified 'socket'. A socket which has 'ZMQ_SWAP' set to a non-zero value may exceed its
         * high water mark; in this case outstanding messages shall be offloaded to storage on disk
         * rather than held in memory.
         * 
         * @param swap
         *            The value of 'ZMQ_SWAP' defines the maximum size of the swap space in bytes.
         */
        public void setSwap(long swap) {
            setLongSockopt(SWAP, swap);
        }

        /**
         * Get the Affinity. The 'ZMQ_AFFINITY' option shall set the I/O thread affinity for newly
         * created connections on the specified 'socket'.
         * 
         * Affinity determines which threads from the 0MQ I/O thread pool associated with the
         * socket's _context_ shall handle newly created connections. A value of zero specifies no
         * affinity, meaning that work shall be distributed fairly among all 0MQ I/O threads in the
         * thread pool. For non-zero values, the lowest bit corresponds to thread 1, second lowest
         * bit to thread 2 and so on. For example, a value of 3 specifies that subsequent
         * connections on 'socket' shall be handled exclusively by I/O threads 1 and 2.
         * 
         * See also linkzmq:zmq_init[3] for details on allocating the number of I/O threads for a
         * specific _context_.
         * 
         * @param affinity
         *            the affinity.
         */
        public void setAffinity(long affinity) {
            setLongSockopt(AFFINITY, affinity);
        }

        /**
         * The 'ZMQ_IDENTITY' option shall set the identity of the specified 'socket'. Socket
         * identity determines if existing 0MQ infastructure (_message queues_, _forwarding
         * devices_) shall be identified with a specific application and persist across multiple
         * runs of the application.
         * 
         * If the socket has no identity, each run of an application is completely separate from
         * other runs. However, with identity set the socket shall re-use any existing 0MQ
         * infrastructure configured by the previous run(s). Thus the application may receive
         * messages that were sent in the meantime, _message queue_ limits shall be shared with
         * previous run(s) and so on.
         * 
         * Identity should be at least one byte and at most 255 bytes long. Identities starting with
         * binary zero are reserved for use by 0MQ infrastructure.
         * 
         * @param identity
         */
        public void setIdentity(byte[] identity) {
            setBytesSockopt(IDENTITY, identity);
        }

        /**
         * The 'ZMQ_SUBSCRIBE' option shall establish a new message filter on a 'ZMQ_SUB' socket.
         * Newly created 'ZMQ_SUB' sockets shall filter out all incoming messages, therefore you
         * should call this option to establish an initial message filter.
         * 
         * An empty 'option_value' of length zero shall subscribe to all incoming messages. A
         * non-empty 'option_value' shall subscribe to all messages beginning with the specified
         * prefix. Mutiple filters may be attached to a single 'ZMQ_SUB' socket, in which case a
         * message shall be accepted if it matches at least one filter.
         * 
         * @param topic
         */
        public void subscribe(byte[] topic) {
            setBytesSockopt(SUBSCRIBE, topic);
        }

        /**
         * The 'ZMQ_UNSUBSCRIBE' option shall remove an existing message filter on a 'ZMQ_SUB'
         * socket. The filter specified must match an existing filter previously established with
         * the 'ZMQ_SUBSCRIBE' option. If the socket has several instances of the same filter
         * attached the 'ZMQ_UNSUBSCRIBE' option shall remove only one instance, leaving the rest in
         * place and functional.
         * 
         * @param topic
         */
        public void unsubscribe(byte[] topic) {
            setBytesSockopt(UNSUBSCRIBE, topic);
        }

        /**
         * The 'ZMQ_RATE' option shall set the maximum send or receive data rate for multicast
         * transports such as linkzmq:zmq_pgm[7] using the specified 'socket'.
         * 
         * @param rate
         */
        public void setRate(long rate) {
            setLongSockopt(RATE, rate);
        }

        /**
         * The 'ZMQ_RECOVERY_IVL' option shall set the recovery interval for multicast transports
         * using the specified 'socket'. The recovery interval determines the maximum time in
         * seconds that a receiver can be absent from a multicast group before unrecoverable data
         * loss will occur.
         * 
         * CAUTION: Excersize care when setting large recovery intervals as the data needed for
         * recovery will be held in memory. For example, a 1 minute recovery interval at a data rate
         * of 1Gbps requires a 7GB in-memory buffer. {Purpose of this Method}
         * 
         * @param recovery_ivl
         */
        public void setRecoveryInterval(long recovery_ivl) {
            setLongSockopt(RECOVERY_IVL, recovery_ivl);
        }

        /**
         * The 'ZMQ_MCAST_LOOP' option shall control whether data sent via multicast transports
         * using the specified 'socket' can also be received by the sending host via loopback. A
         * value of zero disables the loopback functionality, while the default value of 1 enables
         * the loopback functionality. Leaving multicast loopback enabled when it is not required
         * can have a negative impact on performance. Where possible, disable 'ZMQ_MCAST_LOOP' in
         * production environments.
         * 
         * @param mcast_loop
         */
        public void setMulticastLoop(boolean mcast_loop) {
            setLongSockopt(MCAST_LOOP, mcast_loop ? 1 : 0);
        }

        /**
         * The 'ZMQ_SNDBUF' option shall set the underlying kernel transmit buffer size for the
         * 'socket' to the specified size in bytes. A value of zero means leave the OS default
         * unchanged. For details please refer to your operating system documentation for the
         * 'SO_SNDBUF' socket option.
         * 
         * @param sndbuf
         */
        public void setSendBufferSize(long sndbuf) {
            setLongSockopt(SNDBUF, sndbuf);
        }

        /**
         * The 'ZMQ_RCVBUF' option shall set the underlying kernel receive buffer size for the
         * 'socket' to the specified size in bytes. A value of zero means leave the OS default
         * unchanged. For details refer to your operating system documentation for the 'SO_RCVBUF'
         * socket option.
         * 
         * @param rcvbuf
         */
        public void setReceiveBufferSize(long rcvbuf) {
            setLongSockopt(RCVBUF, rcvbuf);
        }

        /**
         * Bind to network interface. Start listening for new connections.
         * 
         * @param addr
         *            the endpoint to bind to.
         */
        public native void bind(String addr);

        /**
         * Connect to remote application.
         * 
         * @param addr
         *            the endpoint to connect to.
         */
        public native void connect(String addr);

        /**
         * Send a message.
         * 
         * @param msg
         *            the message to send, as an array of bytes.
         * @param flags
         *            the flags to apply to the send operation.
         * @return true if send was successful, false otherwise.
         */
        public native boolean send(byte[] msg, long flags);

        /**
         * Receive a message.
         * 
         * @param flags
         *            the flags to apply to the receive operation.
         * @return the message received, as an array of bytes; null on error.
         */
        public native byte[] recv(long flags);

        /**
         * Class constructor.
         * 
         * @param context
         *            a 0MQ context previously created.
         * @param type
         *            the socket type.
         */
        protected Socket(Context context, int type) {
            // We keep a local handle to context so that
            // garbage collection won't be too greedy on it.
            this.context = context;
            construct(context, type);
        }

        /** Initialize the JNI interface */
        protected native void construct(Context ctx, int type);

        /** Free all resources used by JNI interface. */
        @Override
        protected native void finalize();

        /**
         * Get the socket option value, as a long.
         * 
         * @param option
         *            ID of the option to set.
         * @return The socket option value (as a long).
         */
        protected native long getLongSockopt(int option);

        /**
         * Get the socket option value, as a byte array.
         * 
         * @param option
         *            ID of the option to set.
         * @return The socket option value (as a byte array).
         */
        protected native byte[] getBytesSockopt(int option);

        /**
         * Set the socket option value, given as a long.
         * 
         * @param option
         *            ID of the option to set.
         * @param optval
         *            value (as a long) to set the option to.
         */
        protected native void setLongSockopt(int option, long optval);

        /**
         * Set the socket option value, given as a byte array.
         * 
         * @param option
         *            ID of the option to set.
         * @param optval
         *            value (as a byte array) to set the option to.
         */
        protected native void setBytesSockopt(int option, byte[] optval);

        /**
         * Get the underlying socket handle. This is private because it is only accessed from JNI,
         * where Java access controls are ignored.
         * 
         * @return the internal 0MQ socket handle.
         */
        private long getSocketHandle() {
            return this.socketHandle;
        }

        /** Opaque data used by JNI driver. */
        private long socketHandle;
        private Context context = null;
        // private Constants use the appropriate setter instead.
        private static final int HWM = 1;
        // public static final int LWM = 2; // No longer supported
        private static final int SWAP = 3;
        private static final int AFFINITY = 4;
        private static final int IDENTITY = 5;
        private static final int SUBSCRIBE = 6;
        private static final int UNSUBSCRIBE = 7;
        private static final int RATE = 8;
        private static final int RECOVERY_IVL = 9;
        private static final int MCAST_LOOP = 10;
        private static final int SNDBUF = 11;
        private static final int RCVBUF = 12;
        private static final int RCVMORE = 13;
    }

    /**
     * Inner class: Poller.
     */
    public static class Poller {
        /**
         * Register a Socket for polling on all events.
         * 
         * @param socket
         *            the Socket we are registering.
         * @return the index identifying this Socket in the poll set.
         */
        public int register(Socket socket) {
            return register(socket, POLLIN | POLLOUT | POLLERR);
        }

        /**
         * Register a Socket for polling on the specified events.
         * 
         * @param socket
         *            the Socket we are registering.
         * @param events
         *            a mask composed by XORing POLLIN, POLLOUT and POLLERR.
         * @return the index identifying this Socket in the poll set.
         */
        public int register(Socket socket, int events) {
            if (this.next >= this.size) {
                return -1;
            }
            this._sockets[this.next] = socket;
            this._events[this.next] = (short) events;
            return this.next++;
        }

        /**
         * Get the socket associated with an index.
         * 
         * @param index
         *            the desired index.
         * @return the Socket associated with that index (or null).
         */
        public Socket getSocket(int index) {
            if (index < 0 || index >= this.next)
                return null;
            return this._sockets[index];
        }

        /**
         * Get the current poll timeout.
         * 
         * @return the current poll timeout in ms.
         */
        public long getTimeout() {
            return this._timeout;
        }

        /**
         * Set the poll timeout.
         * 
         * @param timeout
         *            the desired poll timeout in ms.
         */
        public void setTimeout(long timeout) {
            this._timeout = timeout;
        }

        /**
         * Get the current poll set size.
         * 
         * @return the current poll set size.
         */
        public int getSize() {
            return this.size;
        }

        /**
         * Get the index for the next position in the poll set size.
         * 
         * @return the index for the next position in the poll set size.
         */
        public int getNext() {
            return this.next;
        }

        /**
         * Issue a poll call.
         * 
         * @return how many objects where signalled by poll().
         */
        public long poll() {
            if (this.size <= 0 || this.next <= 0) {
                return 0;
            }
            for (int i = 0; i < this.next; ++i) {
                this._revents[i] = 0;
            }
            return run_poll(this.next, this._sockets, this._events, this._revents, this._timeout);
        }

        /**
         * Check whether the specified element in the poll set was signalled for input.
         * 
         * @param index
         * 
         * @return true if the element was signalled.
         */
        public boolean pollin(int index) {
            return poll_mask(index, POLLIN);
        }

        /**
         * Check whether the specified element in the poll set was signalled for output.
         * 
         * @param index
         * 
         * @return true if the element was signalled.
         */
        public boolean pollout(int index) {
            return poll_mask(index, POLLOUT);
        }

        /**
         * Check whether the specified element in the poll set was signalled for error.
         * 
         * @param index
         * 
         * @return true if the element was signalled.
         */
        public boolean pollerr(int index) {
            return poll_mask(index, POLLERR);
        }

        /**
         * Class constructor.
         * 
         * @param context
         *            a 0MQ context previously created.
         * @param size
         *            the number of Sockets this poller will contain.
         */
        protected Poller(Context context, int size) {
            this.context = context;
            this.size = size;
            this.next = 0;

            this._sockets = new Socket[size];
            this._events = new short[size];
            this._revents = new short[size];
        }

        /**
         * Issue a poll call on the specified 0MQ sockets.
         * 
         * @param sockets
         *            an array of 0MQ Socket objects to poll.
         * @param events
         *            an array of short values specifying what to poll for.
         * @param revents
         *            an array of short values with the results.
         * @param timeout
         *            the maximum timeout in microseconds.
         * @return how many objects where signalled by poll().
         */
        private native long run_poll(int count, Socket[] sockets, short[] events, short[] revents, long timeout);

        /**
         * Check whether a specific mask was signalled by latest poll call.
         * 
         * @param index
         *            the index indicating the socket.
         * @param mask
         *            a combination of POLLIN, POLLOUT and POLLERR.
         * @return true if specific socket was signalled as specified.
         */
        private boolean poll_mask(int index, int mask) {
            if (mask <= 0 || index < 0 || index >= this.next) {
                return false;
            }
            return (this._revents[index] & mask) > 0;
        }

        private Context context = null;
        private long _timeout = 0;
        private int size = 0;
        private int next = 0;
        private Socket[] _sockets = null;
        private short[] _events = null;
        private short[] _revents = null;

        private static final int POLLIN = 1;
        private static final int POLLOUT = 2;
        private static final int POLLERR = 4;

    }
}