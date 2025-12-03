package org.example;

import io.javalin.Javalin;
import io.javalin.http.Header;
import io.javalin.http.staticfiles.Location;
import org.example.config.DBconfig;
import org.example.config.Inicio;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {

        // 1. Crear la carpeta "uploads" si no existe
        try {
            Files.createDirectories(Paths.get("uploads"));
        } catch (Exception e) {
            System.err.println("⚠️ No se pudo crear la carpeta uploads: " + e.getMessage());
        }

        // 2. Iniciar Javalin
        Javalin app = Javalin.create(config -> {

            // A. Configuración CORS
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.allowHost("http://127.0.0.1:5500", "http://localhost:5500",
                            "http://127.0.0.1:5501", "http://localhost:5501");
                    it.allowCredentials = true;
                });
            });

            // B. Configuración de Archivos Estáticos (CORREGIDO)
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/uploads";      // Cómo se accede en la URL
                staticFiles.directory = "uploads";        // Dónde está en tu disco
                staticFiles.location = Location.EXTERNAL; // No está dentro del JAR
            });

        }).start(7001);

        // 3. Manejo de OPTIONS (Preflight para Cookies)
        app.options("/*", ctx -> {
            String origin = ctx.header("Origin");
            if (origin != null) {
                ctx.header(Header.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
            }
            ctx.header(Header.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            ctx.header(Header.ACCESS_CONTROL_ALLOW_HEADERS, "Authorization, Content-Type, X-Requested-With");
            ctx.header(Header.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS");
            ctx.status(200);
            ctx.result("OK");
        });

        // 4. Iniciar Dependencias y Rutas
        Inicio inicio = new Inicio(DBconfig.getDataSource());

        inicio.inicioUsuario().register(app);
        inicio.inicioCita().register(app);
        inicio.inicioPago().register(app);
        inicio.inicioBlog().register(app);
        inicio.inicioPayPal().register(app);
        inicio.inicioServicio().register(app);

        System.out.println("🚀 API iniciada en http://localhost:7001");
        System.out.println("📂 Carpeta de imágenes configurada en /uploads");
    }
}