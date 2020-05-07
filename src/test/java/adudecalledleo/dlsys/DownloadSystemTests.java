package adudecalledleo.dlsys;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static org.junit.Assert.fail;

public class DownloadSystemTests {
    @Test
    public void singleFileTest() throws MalformedURLException {
        DownloadSystem dlsys = new DownloadSystem();
        dlsys.addDownload(new URL("https://speed.hetzner.de/100MB.bin"), new DownloadHandler() {
            private FileChannel chan;
            private long size;

            @Override
            public void started(long size) {
                System.out.format("Download started! Size is %gKB%n", size / 1024f);
                this.size = size;
                try {
                    FileOutputStream fos = new FileOutputStream("100MB.bin");
                    chan = fos.getChannel();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    fail("Failed to open FileOutputStream");
                }
            }

            @Override
            public void updated(ByteBuffer bytes, long total) {
                System.out.format("%03d%% complete (%gKB / %gKB)%n",
                        Math.floorDiv(total, size), total / 1024f, size / 1024f);
            }

            @Override
            public void completed() {
                System.out.println("Download complete!");
            }

            @Override
            public void failed(Exception e) {
                System.out.println("Download failed!");
                e.printStackTrace(System.out);
                fail("Download failed");
            }
        });
        Thread dlsysT = new Thread(dlsys, "DownloadSystem");
        dlsysT.start();
        try {
            dlsysT.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
