package org.example.controller;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.ForbiddenResponse;
import org.example.model.Blog;
import org.example.service.BlogService;
import java.util.Map;

public class BlogController {

    private BlogService service;

    public BlogController(BlogService service) {
        this.service = service;
    }

    public Handler crearPublicacion = ctx -> {
        Blog blog = ctx.bodyAsClass(Blog.class);

        Integer idUsuarioAutenticado = ctx.attribute("usuarioId");
        if (idUsuarioAutenticado == null) {
            throw new ForbiddenResponse("No se pudo identificar al usuario autenticado.");
        }

        blog.setIdUsuario(idUsuarioAutenticado);

        Integer idGenerado = service.crearPublicacion(blog);

        if (idGenerado == null || idGenerado <= 0) {
            ctx.status(500).json(Map.of("error", "Error al crear la publicación."));
            return;
        }

        ctx.json(Map.of(
                "msg", "Publicación creada exitosamente",
                "idBlog", idGenerado
        ));
        ctx.status(201);
    };

    public Handler verPublicacion = ctx -> {
        ctx.json(service.verPublicacion());
    };
}