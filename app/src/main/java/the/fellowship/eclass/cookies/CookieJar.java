package the.fellowship.eclass.cookies;

import java.net.CookieManager;

public interface CookieJar {
    void setKey(String username);

    CookieManager getCookieManager();

    void load();

    void store();

    boolean clear();
}
