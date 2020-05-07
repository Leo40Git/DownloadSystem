package adudecalledleo.dlsys;

import java.nio.ByteBuffer;

/**
 * Handles download events fired by {@link DownloadSystem}.
 * @author ADudeCalledLeo
 */
public interface DownloadHandler {
    /**
     * Called when the download is started.
     * @param size total number of bytes that will be downloaded
     */
    void started(long size);

    /**
     * Called when progress is made on the download.
     * @param bytes bytes downloaded since the last time this method was called
     * @param offset offset of bytes in file
     * @param total total bytes downloaded
     */
    void updated(ByteBuffer bytes, long offset, long total);

    /**
     * Called when the download is completed.
     */
    void completed();

    /**
     * Called when the download fails.
     * @param e exception that caused download to fail
     */
    void failed(Exception e);
}
