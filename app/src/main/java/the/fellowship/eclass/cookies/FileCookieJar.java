package the.fellowship.eclass.cookies;

import java.io.*;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class FileCookieJar {
    private final static String cookiePath = "";
    final CookieManager cookieManager;
    String path;

    public FileCookieJar() {
        this.cookieManager = new CookieManager();
        //this.cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
    }

    public CookieManager getCookieManager() {
        return cookieManager;
    }

    public void setPath(String username) {
        this.path = cookiePath + String.format("%s_eclass_cookies.pkl", username);
    }

    public boolean clear() {
        File file = new File(path);
        if (!file.exists()) {
            return true;
        }
        return file.delete();
    }

    public void load() {
        if (!new File(path).exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                final HttpCookie cookie = parse(line);
                final URI uri = URI.create(cookie.getDomain() + cookie.getPath());
                this.cookieManager.getCookieStore().add(uri, cookie);
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load cookies from file");
        }
    }

    public void store() {
        List<HttpCookie> cookies = this.cookieManager.getCookieStore().getCookies();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            for (HttpCookie cookie : cookies) {
                writer.write(String.format("%s=%s;$Path=%s;$Domain=%s;$Expires=%s\n", cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(), cookie.getMaxAge()));
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to save cookies to file");
        }
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
