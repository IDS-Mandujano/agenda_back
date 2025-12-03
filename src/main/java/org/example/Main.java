package org.example;

import io.javalin.Javalin;
import io.javalin.http.Header; // Asegúrate de importar esto
import org.example.config.DBconfig;
import org.example.config.Inicio;

public class Main {

    public static void main(String[] args) {
        // Iniciar Javalin
        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(rule -> {
                    // rule.anyHost(); // <-- ESTO A VECES FALLA CON TOKENS
                    rule.reflectClientOrigin = true; // <-- USAR ESTO: Permite el origen y credenciales
                });
            });
        }).start(7001);

        // --- CORRECCIÓN CORS: MANEJO DE PREFLIGHT ---
        // Esto asegura que las peticiones OPTIONS respondan OK y con los headers correctos
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
        // ---------------------------------------------

        // Iniciar configuración de dependencias
        Inicio inicio = new Inicio(DBconfig.getDataSource());

        // Registrar Rutas
        inicio.inicioUsuario().register(app);
        inicio.inicioCita().register(app);
        inicio.inicioPago().register(app);
        inicio.inicioBlog().register(app);
        inicio.inicioPayPal().register(app);
        inicio.inicioServicio().register(app);

        System.out.println("🚀 API iniciada en http://localhost:7001");
    }

}