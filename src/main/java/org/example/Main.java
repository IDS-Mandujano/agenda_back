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

            // A. Configuración CORS (CORREGIDO: Permitir todo para evitar errores en AWS)
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.anyHost(); // <--- ESTO ES CLAVE: Permite conexiones desde tu IP pública, localhost, etc.
                });
            });

            // B. Configuración de Archivos Estáticos
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/uploads";
                staticFiles.directory = "uploads";
                staticFiles.location = Location.EXTERNAL;
            });

        }); // Cerramos la configuración aquí

        // 3. INICIAR EL SERVIDOR (CORREGIDO: Escuchar en 0.0.0.0)
        // Esto permite que AWS acepte conexiones desde fuera
        app.start("0.0.0.0", 7001);

        // 4. Manejo de OPTIONS (Respaldo extra para seguridad de navegadores)
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

        // 5. Iniciar Dependencias y Rutas
        Inicio inicio = new Inicio(DBconfig.getDataSource());

        inicio.inicioUsuario().register(app);
        inicio.inicioCita().register(app);
        inicio.inicioPago().register(app);
        inicio.inicioBlog().register(app);
        inicio.inicioPayPal().register(app);
        inicio.inicioServicio().register(app);

        System.out.println("🚀 API iniciada en http://0.0.0.0:7001");
        System.out.println("📂 Carpeta de imágenes configurada en /uploads");
    }
}