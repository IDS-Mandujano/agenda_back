package org.example.controller;

import io.javalin.http.Context;
import org.example.model.Servicio;
import org.example.service.ServicioService;
import java.util.List;

public class ServicioController {

    private final ServicioService service;

    public ServicioController(ServicioService service) {
        this.service = service;
    }

    public void listar(Context ctx) {
        try {

            List<Servicio> servicios = service.obtenerTodos();
            ctx.json(servicios);

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json("Error interno al obtener los servicios");
        }
    }
}