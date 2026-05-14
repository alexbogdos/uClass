package the.fellowship.eclass.cookies;

import android.content.SharedPreferences;

import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class PrefsCookieJar implements CookieJar {
    final CookieManager cookieManager;
    final SharedPreferences prefs;
    String key;

    public PrefsCookieJar(SharedPreferences prefs) {
        this.prefs = prefs;
        this.cookieManager = new CookieManager();
        //this.cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
    }

    public CookieManager getCookieManager() {
        return cookieManager;
    }

    public void setKey(String username) {
        this.key = String.format("%s_eclass_cookies.pkl", username);
    }

    public boolean clear() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(key);
        editor.apply();
        return true;
    }

    public void load() {
        String cookies = prefs.getString(key, null);
        if (cookies == null) {
            return;
        }

        cookies.lines().forEach(line -> {
            final HttpCookie cookie = parse(line);
            final URI uri = URI.create(cookie.getDomain() + cookie.getPath());
            this.cookieManager.getCookieStore().add(uri, cookie);
        });
    }

    public void store() {
        List<HttpCookie> cookies = this.cookieManager.getCookieStore().getCookies();
        StringBuilder str = new StringBuilder();
        for (HttpCookie cookie : cookies) {
            str.append(String.format("%s=%s;$Path=%s;$Domain=%s;$Expires=%s\n", cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(), cookie.getMaxAge()));
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, str.toString().strip());
        editor.apply();
    }

    private HttpCookie parse(String line) {
        String[] row = line.split(";");
        List<String> pairs = new ArrayList<>();
        for (String column : row) {
            String[] pair = column.split("=");
            pairs.add(pair[0]);
            pairs.add(pair[1]);
        }
        HttpCookie cookie = new HttpCookie(pairs.get(0), pairs.get(1));
        cookie.setDomain(pairs.get(3));
        cookie.setPath(pairs.get(5));
        cookie.setMaxAge(Integer.parseInt(pairs.get(7)));
        return cookie;
    }
}
