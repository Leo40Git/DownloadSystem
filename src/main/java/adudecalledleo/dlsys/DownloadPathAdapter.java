package adudecalledleo.dlsys;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Implementation of {@link DownloadHandler} that saves the downloaded bytes to a file specified by a {@link Path}.
 * @author ADudeCalledLeo
 */
public class DownloadPathAdapter implements DownloadHandler {
    /**
     * Path to output bytes to.
     */
    protected final Path outPath;
    /**
     * Channel to use to output bytes.
     */
    protected ByteChannel chan;

    /**
     * Creates a new {@link DownloadPathAdapter}.
     * @param outPath path to output bytes to
     */
    public DownloadPathAdapter(Path outPath) {
        this.outPath = outPath;
    }

    /**
     * {@inheritDoc}<br>
     * Creates {@linkplain #chan the channel to output bytes to}.
     * @param size total number of bytes that will be downloaded
     */
    @Override
    public void started(long size) {
        try {
            chan = Files.newByteChannel(outPath,
                    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new RuntimeException("Failed to open ByteChannel", e);
        }
    }

    /**
     * {@inheritDoc}<br>
     * Writes the downloaded bytes to {@linkplain #chan the channel}.
     * @param bytes bytes downloaded since the last time this method was called
     * @param total total bytes downloaded
     */
    @Override
    public void updated(ByteBuffer bytes, long total) {
        try {
            chan.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write to ByteChannel", e);
        }
    }

    /**
     * {@inheritDoc}<br>
     * Closes the {@linkplain #chan output channel}.
     */
    @Override
    public void completed() {
        try {
            chan.close();
        } catch (IOException ignored) { }
    }

    /**
     * {@inheritDoc}<br>
     * Closes the {@linkplain #chan output channel}, and throws the provided exception wrapped in a {@link RuntimeException}.
     * @param e exception that caused download to fail
     */
    @Override
    public void failed(Exception e) {
        try {
            chan.close();
        } catch (IOException ignored) { }
        throw new RuntimeException("Download failed", e);
    }
}
