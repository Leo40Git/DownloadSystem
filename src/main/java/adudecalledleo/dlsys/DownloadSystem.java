package adudecalledleo.dlsys;

import java.net.Proxy;

public class DownloadSystem {
    private Proxy proxy;

    public DownloadSystem() {
    }

    public Proxy getProxy() {
        return proxy;
    }

    public DownloadSystem setProxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }
}
