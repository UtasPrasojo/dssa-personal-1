package Mahasiswa;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebServer {

    private static final List<Mahasiswa> daftar = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        daftar.add(new Mahasiswa("Andi Pratama", "2440001", "Teknik Informatika", 3.75));
        daftar.add(new Mahasiswa("Budi Santoso", "2440002", "Sistem Informasi", 3.40));
        daftar.add(new Mahasiswa("Citra Lestari", "2440003", "Teknik Informatika", 3.90));
        daftar.add(new Mahasiswa("Joni Suhartono", "2440004", "Teknik Industri", 3.00));
        daftar.add(new Mahasiswa("Bulan Suci", "2440005", "Akuntansi", 3.20));

        int port = 3000;
        String portEnv = System.getenv("PORT");
        if (portEnv != null && !portEnv.isBlank()) {
            port = Integer.parseInt(portEnv);
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new PageHandler());
        server.createContext("/update", new UpdateHandler());
        server.setExecutor(null);
        server.start();

        System.out.println("Server berjalan di http://0.0.0.0:" + port);
    }

    private static class PageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            Map<String, String> query = parseParams(exchange.getRequestURI().getRawQuery());
            String status = query.getOrDefault("status", "");
            String nim = query.getOrDefault("nim", "");
            sendHtml(exchange, 200, renderPage(status, nim));
        }
    }

    private static class UpdateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> form = parseParams(body);
            String nim = form.getOrDefault("nim", "");
            String ipkRaw = form.getOrDefault("ipk", "");

            double ipkBaru = -1;
            try {
                ipkBaru = Double.parseDouble(ipkRaw);
            } catch (NumberFormatException ignored) {
                // ipkBaru tetap -1, akan dianggap tidak valid di bawah
            }

            boolean ditemukan = false;
            if (ipkBaru >= 0 && ipkBaru <= 4) {
                for (Mahasiswa m : daftar) {
                    if (m.getNim().equals(nim)) {
                        m.updateIpk(ipkBaru);
                        ditemukan = true;
                        break;
                    }
                }
            }

            String status = ditemukan ? "success" : "error";
            String location = "/?status=" + status + "&nim=" + java.net.URLEncoder.encode(nim, StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Location", location);
            exchange.sendResponseHeaders(303, -1);
            exchange.close();
        }
    }

    private static Map<String, String> parseParams(String raw) {
        Map<String, String> params = new HashMap<>();
        if (raw == null || raw.isBlank()) {
            return params;
        }
        for (String pair : raw.split("&")) {
            if (pair.isBlank()) continue;
            String[] kv = pair.split("=", 2);
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            params.put(key, value);
        }
        return params;
    }

    private static void sendHtml(HttpExchange exchange, int status, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String escapeHtml(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String renderPage(String status, String highlightNim) {
        StringBuilder rows = new StringBuilder();
        StringBuilder options = new StringBuilder();

        for (Mahasiswa m : daftar) {
            String rowClass = m.getNim().equals(highlightNim) ? " class=\"highlight\"" : "";
            String badgeClass = "Lulus".equals(m.cekKelulusan()) ? "lulus" : "belum";

            rows.append("<tr").append(rowClass).append(">")
                    .append("<td>").append(escapeHtml(m.getNama())).append("</td>")
                    .append("<td>").append(escapeHtml(m.getNim())).append("</td>")
                    .append("<td>").append(escapeHtml(m.getJurusan())).append("</td>")
                    .append("<td>").append(String.format("%.2f", m.getIpk())).append("</td>")
                    .append("<td><span class=\"badge ").append(badgeClass).append("\">")
                    .append(m.cekKelulusan()).append("</span></td>")
                    .append("<td>").append(m.hitungPredikat()).append("</td>")
                    .append("</tr>\n");

            String selected = m.getNim().equals(highlightNim) ? " selected" : "";
            options.append("<option value=\"").append(escapeHtml(m.getNim())).append("\"")
                    .append(selected).append(">")
                    .append(escapeHtml(m.getNim())).append(" - ").append(escapeHtml(m.getNama()))
                    .append("</option>\n");
        }

        String messageHtml = "";
        if ("success".equals(status)) {
            messageHtml = "<div class=\"message success\">Data berhasil diperbarui!</div>";
        } else if ("error".equals(status)) {
            messageHtml = "<div class=\"message error\">NIM tidak ditemukan atau IPK tidak valid!</div>";
        }

        return "<!DOCTYPE html>\n" +
                "<html lang=\"id\">\n" +
                "<head>\n" +
                "<meta charset=\"UTF-8\">\n" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "<title>Data Mahasiswa</title>\n" +
                "<style>\n" + STYLE + "\n</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<main>\n" +
                "<header class=\"page-header\">\n" +
                "<h1>Data Mahasiswa</h1>\n" +
                "<p>Versi web dari program Java terminal (Mahasiswa &amp; Main).</p>\n" +
                "</header>\n" +
                "<section class=\"card\">\n" +
                "<h2>Daftar Mahasiswa</h2>\n" +
                "<div class=\"table-wrap\">\n" +
                "<table>\n" +
                "<thead><tr><th>Nama</th><th>NIM</th><th>Jurusan</th><th>IPK</th><th>Status</th><th>Predikat</th></tr></thead>\n" +
                "<tbody>\n" + rows + "</tbody>\n" +
                "</table>\n" +
                "</div>\n" +
                "</section>\n" +
                "<section class=\"card\">\n" +
                "<h2>Update IPK Mahasiswa</h2>\n" +
                "<form class=\"update-form\" method=\"POST\" action=\"/update\">\n" +
                "<div class=\"field\">\n" +
                "<label for=\"nim\">NIM Mahasiswa</label>\n" +
                "<select id=\"nim\" name=\"nim\">\n" +
                "<option value=\"\">-- Pilih NIM --</option>\n" + options +
                "</select>\n" +
                "</div>\n" +
                "<div class=\"field\">\n" +
                "<label for=\"ipk\">IPK Baru</label>\n" +
                "<input id=\"ipk\" name=\"ipk\" type=\"number\" min=\"0\" max=\"4\" step=\"0.01\" placeholder=\"cth. 3.85\" required>\n" +
                "</div>\n" +
                "<button type=\"submit\">Perbarui</button>\n" +
                "</form>\n" +
                messageHtml +
                "</section>\n" +
                "</main>\n" +
                "</body>\n" +
                "</html>";
    }

    private static final String STYLE = """
            :root {
              --bg: #f4f6fb; --card: #ffffff; --border: #e2e6ee; --text: #1c2130;
              --muted: #6b7280; --primary: #3856e8; --primary-hover: #2c44c4;
              --success: #1a9e5c; --success-bg: #e6f7ef; --danger: #d93636; --danger-bg: #fdeaea;
              --radius: 12px;
            }
            * { box-sizing: border-box; }
            body { margin: 0; background: var(--bg); color: var(--text);
              font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; }
            main { max-width: 960px; margin: 0 auto; padding: 40px 20px 80px; }
            header.page-header { margin-bottom: 28px; }
            header.page-header h1 { font-size: 26px; margin: 0 0 6px; }
            header.page-header p { margin: 0; color: var(--muted); font-size: 14px; }
            .card { background: var(--card); border: 1px solid var(--border); border-radius: var(--radius);
              padding: 24px; margin-bottom: 24px; box-shadow: 0 1px 2px rgba(16, 24, 40, 0.04); }
            .card h2 { margin: 0 0 16px; font-size: 16px; }
            .table-wrap { overflow-x: auto; }
            table { width: 100%; border-collapse: collapse; font-size: 14px; }
            th, td { text-align: left; padding: 10px 12px; border-bottom: 1px solid var(--border); white-space: nowrap; }
            th { color: var(--muted); font-weight: 600; font-size: 12px; text-transform: uppercase; letter-spacing: 0.03em; }
            tr.highlight { background: #eef1ff; }
            .badge { display: inline-block; padding: 3px 10px; border-radius: 999px; font-size: 12px; font-weight: 600; }
            .badge.lulus { background: var(--success-bg); color: var(--success); }
            .badge.belum { background: var(--danger-bg); color: var(--danger); }
            form.update-form { display: flex; flex-wrap: wrap; gap: 12px; align-items: flex-end; }
            .field { display: flex; flex-direction: column; gap: 6px; flex: 1 1 200px; }
            .field label { font-size: 13px; font-weight: 600; color: var(--muted); }
            .field select, .field input { padding: 10px 12px; border: 1px solid var(--border); border-radius: 8px;
              font-size: 14px; background: #fff; color: var(--text); }
            .field select:focus, .field input:focus { outline: 2px solid var(--primary); outline-offset: 1px; }
            button { padding: 10px 20px; border: none; border-radius: 8px; background: var(--primary); color: white;
              font-size: 14px; font-weight: 600; cursor: pointer; }
            button:hover { background: var(--primary-hover); }
            .message { margin-top: 16px; padding: 10px 14px; border-radius: 8px; font-size: 14px; }
            .message.success { background: var(--success-bg); color: var(--success); }
            .message.error { background: var(--danger-bg); color: var(--danger); }
            """;
}
