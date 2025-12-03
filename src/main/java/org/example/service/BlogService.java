package org.example.service;

import org.example.model.Blog;
import org.example.repository.BlogRepository;
import java.util.List;
import java.sql.SQLException;

public class BlogService {

    private final BlogRepository repository;

    public BlogService(BlogRepository repository) {
        this.repository = repository;
    }

    public Integer crearPublicacion(Blog blog) {
        try {
            return repository.crear(blog);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Blog> verPublicacion() {
        try {
            return repository.listarTodos();
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }
}