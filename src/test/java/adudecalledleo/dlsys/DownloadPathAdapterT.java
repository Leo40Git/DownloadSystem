package adudecalledleo.dlsys;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Extension of {@link DownloadPathAdapter} to be used for testing.<br>
 * In addition to writing to a file, this class also verifies the downloaded file's MD5 hash.<br>
 * It also adds some debug logging.
 */
public class DownloadPathAdapterT extends DownloadPathAdapter {
    private long size;
    private final URL md5URL;
    private String md5Expected;

    public DownloadPathAdapterT(Path outPath, URL md5URL) {
        super(outPath);
        this.md5URL = md5URL;
    }

    @Override
    public void started(long size) {
        this.size = size;
        System.out.format("Starting download of file %s! Size is %gKB%n", outPath.toString(), size / 1024f);
        System.out.println("Downloading file hash");
        // download MD5 synchronously so we can verify file integrity
        try {
            md5Expected = IOUtils.toString(md5URL, StandardCharsets.US_ASCII);
        } catch (IOException e) {
            e.printStackTrace();
        }
        md5Expected = md5Expected.substring(0, 32);
        System.out.format("We're expecting file hash to be %s%n", md5Expected);
        try {
            super.started(size);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Override
    public void updated(ByteBuffer bytes, long offset, long total) {
        System.out.format("%3d%% complete (%gKB / %gKB)%n",
                (long) Math.floor(100 * (total / (float)size)), total / 1024f, size / 1024f);
        try {
            super.updated(bytes, offset, total);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Override
    public void completed() {
        super.completed();
        System.out.println("Download completed!");
        System.out.println("Verifying file hash");
        // get MD5 hash of downloaded file
        String md5Actual = null;
        try {
            md5Actual = new DigestUtils(MessageDigestAlgorithms.MD5).digestAsHex(outPath, StandardOpenOption.READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.format("File's hash is %s%n", md5Actual);
        assertTrue("Downloaded file's MD5 does not meet expectations!", md5Expected.equalsIgnoreCase(md5Actual));
    }

    @Override
    public void failed(Exception e) {
        e.printStackTrace();
        fail("Download failed");
    }
}
