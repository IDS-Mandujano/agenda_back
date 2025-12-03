package org.example.service;

import org.example.model.Servicio;
import org.example.repository.ServicioRepository;
import java.util.List;
import java.sql.SQLException;

public class ServicioService {
    private final ServicioRepository repository;

    public ServicioService(ServicioRepository repository) {
        this.repository = repository;
    }

    public List<Servicio> obtenerTodos() {
        try {
            return repository.listarTodos();
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }
}