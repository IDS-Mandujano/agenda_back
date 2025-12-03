package org.example.routers;

import io.javalin.Javalin;
import static io.javalin.apibuilder.ApiBuilder.*;

import org.example.controller.BlogController;

public class RouteBlog {

    private final BlogController controller;

    public RouteBlog(BlogController controller) {
        this.controller = controller;
    }

    public void register(Javalin app) {


        app.get("/blog/ver", controller.verPublicacion);

        app.post("/admin/blog/crear", controller.crearPublicacion);
    }
}
