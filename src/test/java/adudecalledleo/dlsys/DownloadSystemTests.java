package adudecalledleo.dlsys;

import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

/**
 * Tests for the {@link DownloadSystem}!
 * @author ADudeCalledLeo
 */
public class DownloadSystemTests {
    /**
     * Tests downloading a single file.
     * @throws IOException if something bad happens
     */
    @Test
    public void singleFileTest() throws IOException {
        DownloadSystem dlsys = new DownloadSystem();
        dlsys.addDownload(new URL("http://mirror.filearena.net/pub/speed/SpeedTest_16MB.dat"),
                new DownloadPathAdapterT(Paths.get("SpeedTest_16MB.dat"), new URL("http://mirror.filearena.net/pub/speed/SpeedTest_16MB.md5")));
        Thread dlsysT = new Thread(dlsys, "DownloadSystem");
        dlsysT.start();
        try {
            dlsysT.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Tests downloading multiple files (or multiples of the same file).
     * @throws IOException if something bad happens
     */
    @Test
    public void multipleFileTest() throws IOException {
        DownloadSystem dlsys = new DownloadSystem();
        for (int i = 0; i < 4; i++)
            dlsys.addDownload(new URL("http://mirror.filearena.net/pub/speed/SpeedTest_16MB.dat"),
                    new DownloadPathAdapterT(Paths.get(String.format("SpeedTest_16MB_m%d.dat", i + 1)),
                            new URL("http://mirror.filearena.net/pub/speed/SpeedTest_16MB.md5")));
        Thread dlsysT = new Thread(dlsys, "DownloadSystem");
        dlsysT.start();
        try {
            dlsysT.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
