package adudecalledleo.dlsys;

import java.nio.ByteBuffer;

public interface DownloadHandler {
    void started(long size);
    void updated(ByteBuffer bytes, long total);
    void completed();
    void failed(Exception e);
}
