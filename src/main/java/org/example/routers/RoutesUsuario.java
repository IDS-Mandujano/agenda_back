package org.example.routers;

import io.javalin.Javalin;
import org.example.controller.UsuarioController;
import org.example.config.AuthMiddleware;

public class RoutesUsuario {
    private final UsuarioController controller;

    public RoutesUsuario(UsuarioController controller) {
        this.controller = controller;
    }

    public void register(Javalin app) {
        // Rutas públicas
        app.post("/registro", controller::registrar);
        app.post("/login", controller::login);
        app.post("/recuperar", controller::recuperarPassword);

        // --- RUTAS PROTEGIDAS DE PERFIL ---

        // ¡ESTA ES LA LÍNEA QUE FALTABA!
        app.before("/perfil/*", AuthMiddleware.requireAuth);

        app.get("/perfil/{id}", controller::obtenerPerfil);
        app.put("/perfil/{id}", controller::editarPerfil);

        // --- DASHBOARDS PROTEGIDOS ---
        app.before("/cliente/*", AuthMiddleware.requireCliente);
        app.get("/cliente/dashboard", ctx -> {
            ctx.json(java.util.Map.of("mensaje", "Bienvenido al dashboard de cliente"));
        });

        // ... resto de rutas (admin, asesor) ...
        app.before("/admin/*", AuthMiddleware.requireAdmin);
        app.get("/admin/dashboard", ctx -> {
            ctx.json(java.util.Map.of("mensaje", "Bienvenido al dashboard de administrador"));
        });

        app.get("/api/usuarios/asesores", controller::listarAsesores);
        app.get("/admin/usuarios", controller::listarUsuarios);
        app.put("/usuario/eliminar/{id}", controller::eliminarUsuario);
    }
}