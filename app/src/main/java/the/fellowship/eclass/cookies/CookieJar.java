package the.fellowship.eclass.cookies;

import java.net.CookieManager;

public interface CookieJar {
    public void setKey(String username);
    public CookieManager getCookieManager();
    public void load();
    public void store();
    public boolean clear();
}
