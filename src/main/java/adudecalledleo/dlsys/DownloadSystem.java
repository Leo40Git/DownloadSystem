package adudecalledleo.dlsys;

import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.*;

public class DownloadSystem implements Runnable {
    private final ExecutorService execService;
    private final BlockingQueue<Future<Void>> futures;
    private Proxy proxy;

    public DownloadSystem() {
        execService = Executors.newCachedThreadPool();
        futures = new LinkedBlockingQueue<>();
        noProxy();
    }

    public DownloadSystem setProxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    public DownloadSystem noProxy() {
        return setProxy(Proxy.NO_PROXY);
    }

    public DownloadSystem addDownload(final URL url, final DownloadHandler handler) {
        final Proxy proxy1 = proxy;
        futures.add(execService.submit(() -> {
            performDownload(url, proxy1, handler);
            return null;
        }));
        return this;
    }

    private void performDownload(URL url, Proxy proxy, DownloadHandler handler) throws Exception {
        URLConnection connection = url.openConnection(proxy);
        long size;
        handler.started(size = connection.getContentLengthLong());
        ReadableByteChannel chan = Channels.newChannel(connection.getInputStream());
        ByteBuffer buf = ByteBuffer.allocate(1024);
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
            handler.failed(e);
        } finally {
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
