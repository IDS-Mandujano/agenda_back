package org.example.controller;

import io.javalin.http.Context;
import org.example.model.Usuario;
import org.example.config.JwtUtil;
import org.example.service.UsuarioService;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class UsuarioController {
    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    public void registrar(Context ctx) {
        try {
            Usuario usuario = ctx.bodyAsClass(Usuario.class);
            service.registrar(usuario);
            ctx.status(201).result("Usuario registrado");
        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (SQLException e) {
            ctx.status(500).result("Error de base de datos: " + e.getMessage());
        } catch (Exception e) {
            ctx.status(500).result("Error interno: " + e.getMessage());
        }
    }
    public static class LoginRequest {
        private String correo;
        private String contrasena;

        public String getCorreo() {
            return correo;
        }

        public void setCorreo(String correo) {
            this.correo = correo;
        }

        public String getContrasena() {
            return contrasena;
        }

        public void setContrasena(String contrasena) {
            this.contrasena = contrasena;
        }
    }

    public void login(Context ctx) {
        try {
            LoginRequest loginRequest = ctx.bodyAsClass(LoginRequest.class);
            String correo = loginRequest.getCorreo();
            String contrasena = loginRequest.getContrasena();

            if (correo == null || contrasena == null) {
                ctx.status(400).result("Faltan los datos");
                return;
            }

            Optional<Usuario> usuarioOpt = service.getCorreo(correo);
            if (usuarioOpt.isPresent()) {
                Usuario u = usuarioOpt.get();
                boolean valido = BCrypt.checkpw(contrasena, u.getContrasena());
                if (valido) {
                    String token = JwtUtil.generarToken(u);
                    ctx.json(Map.of(
                            "mensaje", "Login exitoso",
                            "token", token,
                            "rol", u.getIdRol(),
                            "id", u.getIdUsuario()
                    ));
                } else {
                    ctx.status(401).result("Contraseña incorrecta");
                }
            } else {
                ctx.status(404).result("Correo no registrado");
            }
        } catch (SQLException e) {
            ctx.status(500).result("Error de base de datos: " + e.getMessage());
        } catch (Exception e) {
            ctx.status(500).result("Error interno: " + e.getMessage());
        }
    }

    public void encriptarPassword(Context ctx) {
        String contrasena = ctx.formParam("contrasena");
        if (contrasena == null || contrasena.isEmpty()) {
            ctx.status(400).result("Falta 'contrasena'");
            return;
        }
        String hash = BCrypt.hashpw(contrasena, BCrypt.gensalt());
        ctx.status(200).result("Contraseña encriptada: " + hash);
    }

    public void listarAsesores(Context ctx) {
        ctx.json(service.listarAsesores());
    }

    public void editarPerfil(Context ctx) {
        try {
            int idRuta = Integer.parseInt(ctx.pathParam("id"));
            int idToken = ctx.attribute("usuarioId") != null ? (int) ctx.attribute("usuarioId") : -1;

            if (idRuta != idToken) {
                ctx.status(403).result("Acceso no valido");
                return;
            }

            Usuario usuario = ctx.bodyAsClass(Usuario.class);
            usuario.setIdUsuario(idRuta);

            if (usuario.getContrasena() != null && !usuario.getContrasena().isEmpty()) {
                String hashed = BCrypt.hashpw(usuario.getContrasena(), BCrypt.gensalt());
                usuario.setContrasena(hashed);
            }

            service.editarPerfil(usuario);
            ctx.status(200).result("Perfil actualizado con ID " + idRuta);
        } catch (IllegalArgumentException e) {
            ctx.status(400).result("Error de validación: " + e.getMessage());
        } catch (SQLException e) {
            ctx.status(500).result("Error de base de datos: " + e.getMessage());
        } catch (Exception e) {
            ctx.status(500).result("Error interno: " + e.getMessage());
        }
    }

    public void recuperarPassword(Context ctx) {
        try {
            String correo = ctx.formParam("correo");
            String nuevaPassword = ctx.formParam("nuevaPassword");

            service.recuperarPassword(correo, nuevaPassword);
            ctx.status(200).result("Contraseña actualizada correctamente");
        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (SQLException e) {
            ctx.status(500).result("Error de base de datos: " + e.getMessage());
        }
    }

    public void obtenerPerfil(Context ctx) {
        try {
            int idRuta = Integer.parseInt(ctx.pathParam("id"));
            int idToken = ctx.attribute("usuarioId") != null ? (int) ctx.attribute("usuarioId") : -1;

            if (idRuta != idToken) {
                ctx.status(403).result("Acceso Inavalido");
                return;
            }

            Optional<Usuario> usuarioOpt = service.getId(idRuta);
            if (usuarioOpt.isPresent()) {
                ctx.json(usuarioOpt.get());
            } else {
                ctx.status(404).result("Usuario no encontrado con ID " + idRuta);
            }
        } catch (SQLException e) {
            ctx.status(500).result("Error de base de datos: " + e.getMessage());
        } catch (Exception e) {
            ctx.status(500).result("Error interno: " + e.getMessage());
        }
    }

    public void eliminarUsuario(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        boolean eliminado = service.cambiarEstado(id, "inactivo");
        if (eliminado) ctx.json(Map.of("mensaje", "Usuario desactivado"));
        else ctx.status(400).json(Map.of("error", "Error al desactivar"));
    }

    public void listarUsuarios(Context ctx) {
        try {
            ctx.json(service.listarTodos());
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Error al listar usuarios"));
        }
    }
}