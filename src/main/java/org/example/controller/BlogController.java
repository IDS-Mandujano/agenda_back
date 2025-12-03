package org.example.controller;

import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import io.javalin.http.ForbiddenResponse;
import org.example.model.Blog;
import org.example.service.BlogService;
import org.example.service.FileService;

import java.time.LocalDate;
import java.util.Map;

public class BlogController {

    private final BlogService service;
    private final FileService fileService = new FileService(); // Para guardar imágenes

    public BlogController(BlogService service) {
        this.service = service;
    }

    // 1. CREAR (Con subida de imagen)
    public void crearPublicacion(Context ctx) {
        try {
            // Validar sesión
            Integer idUsuarioAutenticado = ctx.attribute("usuarioId");
            if (idUsuarioAutenticado == null) throw new ForbiddenResponse("Usuario no autenticado");

            // Obtener datos del formulario (multipart)
            String titulo = ctx.formParam("titulo");
            String contenido = ctx.formParam("contenido");
            String categoria = ctx.formParam("categoria");
            Boolean destacado = Boolean.parseBoolean(ctx.formParam("destacado"));

            // Manejar imagen
            UploadedFile archivo = ctx.uploadedFile("imagen");
            String urlImagen = null;
            if (archivo != null) {
                urlImagen = fileService.guardarImagenLocal(archivo, idUsuarioAutenticado);
            }

            // Crear objeto
            Blog blog = new Blog();
            blog.setIdUsuario(idUsuarioAutenticado);
            blog.setTitulo(titulo);
            blog.setContenido(contenido);
            blog.setCategoria(categoria);
            blog.setDestacado(destacado);
            blog.setFechaPublicacion(LocalDate.now());
            blog.setImg(urlImagen);

            Integer idGenerado = service.crearPublicacion(blog);

            if (idGenerado != null && idGenerado > 0) {
                ctx.status(201).json(Map.of("mensaje", "Blog creado", "id", idGenerado));
            } else {
                ctx.status(500).json(Map.of("error", "No se pudo crear el blog"));
            }
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }
    }

    // 2. VER LISTA
    public void verPublicaciones(Context ctx) {
        ctx.json(service.verPublicacion());
    }

    // 3. VER UNO SOLO
    public void verUno(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Blog blog = service.obtenerPorId(id);
        if (blog != null) ctx.json(blog);
        else ctx.status(404).json(Map.of("error", "Blog no encontrado"));
    }

    // 4. EDITAR (Con imagen opcional)
    public void editarPublicacion(Context ctx) {
        try {
            int idBlog = Integer.parseInt(ctx.pathParam("id"));

            Blog blogExistente = service.obtenerPorId(idBlog);
            if (blogExistente == null) {
                ctx.status(404).json(Map.of("error", "Blog no encontrado"));
                return;
            }

            // Datos formulario
            String titulo = ctx.formParam("titulo");
            String contenido = ctx.formParam("contenido");
            String categoria = ctx.formParam("categoria");

            // --- CORRECCIÓN: LEER LA FECHA ---
            String fechaStr = ctx.formParam("fechaPublicacion");

            // Manejo de imagen (Igual que antes)
            UploadedFile archivo = ctx.uploadedFile("imagen");
            String urlImagen = blogExistente.getImg();

            if (archivo != null) {
                if(urlImagen != null) fileService.borrarImagenLocal(urlImagen);
                urlImagen = fileService.guardarImagenLocal(archivo, blogExistente.getIdUsuario());
            }

            // Actualizar objeto
            blogExistente.setTitulo(titulo);
            blogExistente.setContenido(contenido);
            blogExistente.setCategoria(categoria);
            blogExistente.setImg(urlImagen);

            // --- CORRECCIÓN: ACTUALIZAR LA FECHA EN EL OBJETO ---
            if (fechaStr != null && !fechaStr.isEmpty()) {
                // LocalDate.parse acepta formato "YYYY-MM-DD" por defecto
                blogExistente.setFechaPublicacion(LocalDate.parse(fechaStr));
            }

            if(ctx.formParam("destacado") != null) {
                blogExistente.setDestacado(Boolean.parseBoolean(ctx.formParam("destacado")));
            }

            boolean actualizado = service.actualizarPublicacion(blogExistente);

            if (actualizado) ctx.json(Map.of("mensaje", "Blog actualizado"));
            else ctx.status(500).json(Map.of("error", "Error al actualizar en BD"));

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }
    }

    // 5. ELIMINAR
    public void eliminarPublicacion(Context ctx) {
        int idBlog = Integer.parseInt(ctx.pathParam("id"));

        // Obtener para borrar imagen del disco
        Blog blog = service.obtenerPorId(idBlog);
        if (blog != null && blog.getImg() != null) {
            fileService.borrarImagenLocal(blog.getImg());
        }

        boolean eliminado = service.eliminarPublicacion(idBlog);
        if (eliminado) ctx.json(Map.of("mensaje", "Blog eliminado"));
        else ctx.status(400).json(Map.of("error", "No se pudo eliminar"));
    }
}