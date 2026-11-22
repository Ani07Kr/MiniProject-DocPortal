import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.sun.net.httpserver.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class AppServer {
    private static final Path WEBAPP_DIR = Paths.get("sample/src/main/webapp");
    private static final Map<String, SimpleHttpSession> sessions = new ConcurrentHashMap<>();

    private static int getPort() {
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.trim().isEmpty()) {
            try {
                return Integer.parseInt(envPort.trim());
            } catch (NumberFormatException ignored) {}
        }
        return 8080;
    }

    public static void main(String[] args) throws Exception {
        int port = getPort();
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new RootHandler());
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();

        System.out.println("=======================================================");
        System.out.println("🚀 DocPortal Server is RUNNING on Port " + port + "!");
        System.out.println("👉 URL: http://localhost:" + port + "/Welcome.html");
        System.out.println("=======================================================");
    }

    static class SimpleHttpSession {
        private final String id = UUID.randomUUID().toString();
        private final Map<String, Object> attributes = new ConcurrentHashMap<>();
        private final long creationTime = System.currentTimeMillis();

        public String getId() { return id; }
        public Object getAttribute(String name) { return attributes.get(name); }
        public void setAttribute(String name, Object value) {
            if (value == null) attributes.remove(name);
            else attributes.put(name, value);
        }
        public void removeAttribute(String name) { attributes.remove(name); }
        public Enumeration<String> getAttributeNames() { return Collections.enumeration(attributes.keySet()); }
        public void invalidate() { attributes.clear(); }
    }

    static class RootHandler implements HttpHandler {
        private final Map<String, HttpServlet> servlets = new HashMap<>();

        public RootHandler() {
            try {
                servlets.put("/LoginServlet", new LoginServlet());
                servlets.put("/RegisterServlet", new RegisterServlet());
                servlets.put("/DoctorServlet", new DoctorServlet());
                servlets.put("/ManageAppointmentServlet", new ManageAppointmentServlet());
                servlets.put("/PrescriptionServlet", new PrescriptionServlet());
                servlets.put("/DownloadSlipServlet", new DownloadSlipServlet());
                servlets.put("/downloadSlip", new DownloadSlipServlet());
                servlets.put("/AuthServlet", new AuthServlet());
                servlets.put("/LogoutServlet", new LogoutServlet());
                servlets.put("/AdminApprovalServlet", new AdminApprovalServlet());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.startsWith("/sample")) {
                path = path.substring(7);
            }
            if (path.isEmpty() || path.equals("/")) {
                path = "/Welcome.html";
            }

            // Check if it's a servlet request
            if (servlets.containsKey(path)) {
                handleServlet(exchange, servlets.get(path));
                return;
            }

            // Check static file in webapp directory
            Path filePath = WEBAPP_DIR.resolve(path.startsWith("/") ? path.substring(1) : path).normalize();
            if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                serveFile(exchange, filePath);
                return;
            }

            // 404
            String msg = "404 Not Found: " + path;
            exchange.sendResponseHeaders(404, msg.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(msg.getBytes());
            }
        }

        private void serveFile(HttpExchange exchange, Path filePath) throws IOException {
            String name = filePath.getFileName().toString().toLowerCase();
            String mime = "application/octet-stream";
            if (name.endsWith(".html") || name.endsWith(".htm")) mime = "text/html; charset=UTF-8";
            else if (name.endsWith(".css")) mime = "text/css";
            else if (name.endsWith(".js")) mime = "application/javascript";
            else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) mime = "image/jpeg";
            else if (name.endsWith(".png")) mime = "image/png";
            else if (name.endsWith(".gif")) mime = "image/gif";

            byte[] bytes = Files.readAllBytes(filePath);
            exchange.getResponseHeaders().set("Content-Type", mime);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private void handleServlet(HttpExchange exchange, HttpServlet servlet) throws IOException {
            Map<String, String> params = new HashMap<>();

            // Parse Query String
            String rawQuery = exchange.getRequestURI().getRawQuery();
            parseQueryParams(rawQuery, params);

            // Parse Body if POST
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                try (InputStream is = exchange.getRequestBody()) {
                    String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    parseQueryParams(body, params);
                }
            }

            // Handle Cookies & Session
            String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
            List<Cookie> cookieList = new ArrayList<>();
            String sessionId = null;
            if (cookieHeader != null) {
                for (String part : cookieHeader.split(";")) {
                    String[] kv = part.trim().split("=", 2);
                    if (kv.length == 2) {
                        cookieList.add(new Cookie(kv[0].trim(), kv[1].trim()));
                        if ("JSESSIONID".equalsIgnoreCase(kv[0].trim())) {
                            sessionId = kv[1].trim();
                        }
                    }
                }
            }

            final SimpleHttpSession[] currentSession = { (sessionId != null) ? sessions.get(sessionId) : null };
            final List<String> setCookieHeaders = new ArrayList<>();

            if (currentSession[0] == null) {
                SimpleHttpSession newSession = new SimpleHttpSession();
                sessions.put(newSession.getId(), newSession);
                currentSession[0] = newSession;
                setCookieHeaders.add("JSESSIONID=" + newSession.getId() + "; Path=/; HttpOnly");
            }

            ByteArrayOutputStream responseBytes = new ByteArrayOutputStream();
            PrintWriter responseWriter = new PrintWriter(new OutputStreamWriter(responseBytes, StandardCharsets.UTF_8), true);
            final int[] statusCode = {200};
            final String[] redirectUrl = {null};
            final Map<String, String> respHeaders = new HashMap<>();

            HttpServletRequest req = (HttpServletRequest) java.lang.reflect.Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    String mName = method.getName();
                    if (mName.equals("getParameter")) {
                        return params.get((String) args[0]);
                    } else if (mName.equals("getMethod")) {
                        return exchange.getRequestMethod();
                    } else if (mName.equals("getContextPath")) {
                        return "";
                    } else if (mName.equals("getRequestURI")) {
                        return exchange.getRequestURI().getPath();
                    } else if (mName.equals("getSession")) {
                        return createHttpSessionProxy(currentSession[0]);
                    } else if (mName.equals("getCookies")) {
                        return cookieList.toArray(new Cookie[0]);
                    }
                    return null;
                }
            );

            HttpServletResponse resp = (HttpServletResponse) java.lang.reflect.Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    String mName = method.getName();
                    if (mName.equals("getWriter")) {
                        return responseWriter;
                    } else if (mName.equals("setContentType")) {
                        respHeaders.put("Content-Type", (String) args[0]);
                    } else if (mName.equals("setHeader")) {
                        respHeaders.put((String) args[0], (String) args[1]);
                    } else if (mName.equals("addCookie")) {
                        Cookie c = (Cookie) args[0];
                        String cVal = c.getName() + "=" + c.getValue() + "; Path=" + (c.getPath() != null ? c.getPath() : "/");
                        setCookieHeaders.add(cVal);
                    } else if (mName.equals("sendRedirect")) {
                        redirectUrl[0] = (String) args[0];
                        statusCode[0] = 302;
                    } else if (mName.equals("sendError")) {
                        statusCode[0] = (int) args[0];
                        if (args.length > 1 && args[1] != null) {
                            responseWriter.write("Error: " + args[1]);
                        }
                    } else if (mName.equals("setStatus")) {
                        statusCode[0] = (int) args[0];
                    }
                    return null;
                }
            );

            try {
                servlet.service(req, resp);
            } catch (Exception e) {
                e.printStackTrace();
                statusCode[0] = 500;
                responseWriter.write("Internal Server Error: " + e.getMessage());
            }

            responseWriter.flush();

            // Set cookies on response
            for (String cookieVal : setCookieHeaders) {
                exchange.getResponseHeaders().add("Set-Cookie", cookieVal);
            }

            if (redirectUrl[0] != null) {
                exchange.getResponseHeaders().set("Location", redirectUrl[0]);
                exchange.sendResponseHeaders(302, -1);
            } else {
                for (Map.Entry<String, String> h : respHeaders.entrySet()) {
                    exchange.getResponseHeaders().set(h.getKey(), h.getValue());
                }
                if (!respHeaders.containsKey("Content-Type")) {
                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                }
                byte[] outBytes = responseBytes.toByteArray();
                exchange.sendResponseHeaders(statusCode[0], outBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(outBytes);
                }
            }
        }

        private HttpSession createHttpSessionProxy(SimpleHttpSession backing) {
            return (HttpSession) java.lang.reflect.Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(),
                new Class<?>[]{HttpSession.class},
                (proxy, method, args) -> {
                    String mName = method.getName();
                    if (mName.equals("getAttribute")) {
                        return backing.getAttribute((String) args[0]);
                    } else if (mName.equals("setAttribute")) {
                        backing.setAttribute((String) args[0], args[1]);
                        return null;
                    } else if (mName.equals("removeAttribute")) {
                        backing.removeAttribute((String) args[0]);
                        return null;
                    } else if (mName.equals("getId")) {
                        return backing.getId();
                    } else if (mName.equals("invalidate")) {
                        backing.invalidate();
                        return null;
                    }
                    return null;
                }
            );
        }

        private void parseQueryParams(String query, Map<String, String> params) {
            if (query == null || query.isEmpty()) return;
            for (String pair : query.split("&")) {
                int idx = pair.indexOf("=");
                try {
                    if (idx > 0 && idx < pair.length() - 1) {
                        String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                        String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                        params.put(key, value);
                    } else if (idx > 0) {
                        String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                        params.put(key, "");
                    }
                } catch (Exception ignored) {}
            }
        }
    }
}
