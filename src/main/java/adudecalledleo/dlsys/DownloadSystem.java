package adudecalledleo.dlsys;

import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.*;

public class DownloadSystem implements Runnable {
    private static final int DEFAULT_BUFFER_SIZE = 0x8000;

    private final ExecutorService execService;
    private final BlockingQueue<Future<Void>> futures;
    private Proxy proxy;
    private int bufferSize;

    public DownloadSystem() {
        execService = Executors.newCachedThreadPool();
        futures = new LinkedBlockingQueue<>();
        proxy = Proxy.NO_PROXY;
        bufferSize = DEFAULT_BUFFER_SIZE;
    }

    public DownloadSystem setProxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    public DownloadSystem noProxy() {
        return setProxy(Proxy.NO_PROXY);
    }

    public DownloadSystem setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    public DownloadSystem defaultBufferSize() {
        return setBufferSize(DEFAULT_BUFFER_SIZE);
    }

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
