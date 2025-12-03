package org.example.routers;

import io.javalin.Javalin;
import org.example.controller.ServicioController;

public class RouteServicio {
    private final ServicioController controller;

    public RouteServicio(ServicioController controller) {
        this.controller = controller;
    }

    public void register(Javalin app) {
        app.get("/api/servicios", controller::listar);
    }
}