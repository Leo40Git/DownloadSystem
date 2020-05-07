package adudecalledleo.dlsys;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class DownloadPathAdapter implements DownloadHandler {
    protected final Path outPath;
    protected ByteChannel chan;

    public DownloadPathAdapter(Path outPath) {
        this.outPath = outPath;
    }

    @Override
    public void started(long size) {
        try {
            chan = Files.newByteChannel(outPath,
                    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new RuntimeException("Failed to open ByteChannel", e);
        }
    }

    @Override
    public void updated(ByteBuffer bytes, long total) {
        try {
            chan.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write to ByteChannel", e);
        }
    }

    @Override
    public void completed() {
        try {
            chan.close();
        } catch (IOException ignored) { }
    }

    @Override
    public void failed(Exception e) {
        try {
            chan.close();
        } catch (IOException ignored) { }
        throw new RuntimeException("Download failed", e);
    }
}
