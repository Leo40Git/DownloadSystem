package adudecalledleo.dlsys;

import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.*;

/**
 * Handles downloading data from the Internet asynchronously.
 * @author ADudeCalledLeo
 */
public class DownloadSystem implements Runnable {
    private static final int DEFAULT_BUFFER_SIZE = 0x8000;

    private final ExecutorService execService;
    private final BlockingQueue<Future<Void>> futures;
    private Proxy proxy;
    private int bufferSize;

    /**
     * Creates a new {@link DownloadSystem}.
     */
    public DownloadSystem() {
        execService = Executors.newCachedThreadPool();
        futures = new LinkedBlockingQueue<>();
        proxy = Proxy.NO_PROXY;
        bufferSize = DEFAULT_BUFFER_SIZE;
    }

    /**
     * Sets the {@link Proxy} to use while downloading.<br>
     * This applies to {@linkplain #addDownload(URL, DownloadHandler) every download added to the queue},
     * until the next <code>setProxy</code> call.
     * @param proxy proxy to use to create connection
     * @return this {@link DownloadSystem} instance
     */
    public DownloadSystem setProxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    /**
     * Sets the {@link Proxy} to use while downloading to no proxy at all.<br>
     * This method is equivalent to <code>{@link #setProxy(Proxy) setProxy}({@link Proxy#NO_PROXY})</code>.
     * @return this {@link DownloadSystem} instance
     * @see #setProxy
     */
    public DownloadSystem noProxy() {
        return setProxy(Proxy.NO_PROXY);
    }

    /**
     * Sets the buffer size to use while downloading.<br>
     * This applies to {@linkplain #addDownload(URL, DownloadHandler) every download added to the queue},
     * until the next <code>setBufferSize</code> call.
     * @param bufferSize size of partial buffer
     * @return this {@link DownloadSystem} instance
     */
    public DownloadSystem setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    /**
     * Sets the buffer size to use while downloading to the default size.
     * @return this {@link DownloadSystem} instance
     * @see #setBufferSize(int)
     */
    public DownloadSystem defaultBufferSize() {
        return setBufferSize(DEFAULT_BUFFER_SIZE);
    }

    /**
     * Adds a download to the queue.
     * @param url {@link URL} to download from
     * @param handler {@link DownloadHandler} that will handle this download's events
     * @return this {@link DownloadSystem} instance
     */
    public DownloadSystem addDownload(final URL url, final DownloadHandler handler) {
        final Proxy proxy1 = proxy;
        final int bufferSize1 = bufferSize;
        futures.add(execService.submit(() -> {
            performDownload(url, proxy1, handler, bufferSize1);
            return null;
        }));
        return this;
    }

    private void performDownload(final URL url, final Proxy proxy, final DownloadHandler handler, final int bufferSize)
            throws Exception {
        URLConnection connection = url.openConnection(proxy);
        long size;
        handler.started(size = connection.getContentLengthLong());
        ReadableByteChannel chan = Channels.newChannel(connection.getInputStream());
        ByteBuffer buf = ByteBuffer.allocate(bufferSize);
        long total = 0;
        try {
            while (total < size) {
                int read = chan.read(buf);
                total += read;
                buf.flip();
                handler.updated(buf, total);
                buf.clear();
            }
        } catch (Exception e) {
            chan.close();
            handler.failed(e);
        } finally {
            chan.close();
            handler.completed();
        }
    }

    /**
     * Processes the download queue.<br>Should not be called directly,
     * instead this object should be assigned to a {@link Thread} as its {@link Runnable} object.
     */
    @Override
    public void run() {
        while (!futures.isEmpty()) {
            try {
                futures.take().get();
            } catch (InterruptedException | ExecutionException ignored) {
            }
        }
    }
}
