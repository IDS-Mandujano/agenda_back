package org.example.repository;

import org.example.config.DBconfig;
import org.example.model.Usuario;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioRepository {

    private final DataSource dataSource;

    // ✔ Constructor que recibe DataSource
    public UsuarioRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void registrarUsuario(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuario (idRol, nombre, apellido, segundoApellido, rfc, curp, contrasena, correo, telefono, img, imagenPublicId, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'activo')";

        try (Connection conn = DBconfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, usuario.getIdRol());
            stmt.setString(2, usuario.getNombre());
            stmt.setString(3, usuario.getApellido());
            stmt.setString(4, usuario.getSegundoApellido());
            stmt.setString(5, usuario.getRfc());
            stmt.setString(6, usuario.getCurp());
            stmt.setString(7, usuario.getContrasena());
            stmt.setString(8, usuario.getCorreo());
            stmt.setString(9, usuario.getTelefono());

            // Asumiendo que ahora 'img' es un String (URL) en tu modelo,
            // o si sigue siendo byte[], debes convertirlo o dejarlo null por ahora.
            // Como la BD dice VARCHAR, enviamos String.
            stmt.setString(10, null); // img (puedes poner usuario.getImg() si cambias el tipo)
            stmt.setString(11, null); // imagenPublicId

            stmt.executeUpdate();
        }
    }

    public boolean verificarCorreo(String correo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuario WHERE correo = ?";
        try (Connection conn = DBconfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, correo);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public Optional<Usuario> getCorreo(String correo) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE correo = ?";
        try (Connection conn = DBconfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, correo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapearUsuario(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<Usuario> getId(int id) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE idUsuario = ?";
        try (Connection conn = DBconfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapearUsuario(rs));
            }
        }
        return Optional.empty();
    }

    public void updatePerfil(Usuario usuario) throws SQLException {
        String sql = "UPDATE usuario SET nombre=?, apellido=?, segundoApellido=?, rfc=?, curp=?, telefono=?, correo=?, idRol=?, img=? WHERE idUsuario=?";

        try (Connection conn = DBconfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getApellido());
            stmt.setString(3, usuario.getSegundoApellido());
            stmt.setString(4, usuario.getRfc());
            stmt.setString(5, usuario.getCurp());
            stmt.setString(6, usuario.getTelefono());
            stmt.setString(7, usuario.getCorreo());
            stmt.setInt(8, usuario.getIdRol());
            stmt.setString(9, null);

            stmt.setInt(10, usuario.getIdUsuario());

            int filas = stmt.executeUpdate();
            if (filas == 0) {
                throw new IllegalArgumentException("No se encontró el usuario con ID " + usuario.getIdUsuario());
            }
        }
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("idUsuario"));
        u.setNombre(rs.getString("nombre"));
        u.setApellido(rs.getString("apellido"));
        u.setSegundoApellido(rs.getString("segundoApellido"));
        u.setTelefono(rs.getString("telefono"));
        u.setCorreo(rs.getString("correo"));
        u.setContrasena(rs.getString("contrasena"));
        u.setRfc(rs.getString("rfc"));   // Faltaba leer esto
        u.setCurp(rs.getString("curp"));
        u.setIdRol(rs.getInt("idRol"));
        u.setEstado(rs.getString("estado"));
        return u;
    }

    public void updatePassword(String correo, String nuevaPasswordEncriptada) throws SQLException {
        String sql = "UPDATE usuario SET contrasena = ? WHERE correo = ?";
        try (Connection conn = DBconfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nuevaPasswordEncriptada);
            stmt.setString(2, correo);
            stmt.executeUpdate();
        }
    }

    public List<Usuario> listarAsesores() throws SQLException {
        // Ajusta 'idRol = 2' al ID que uses para tus empleados/asesores
        String sql = "SELECT idUsuario, idRol, nombre, apellido FROM usuario WHERE idRol = 3";
        List<Usuario> lista = new ArrayList<>();

        try (Connection con = DBconfig.getDataSource().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Usuario u = new Usuario();
                u.setIdUsuario(rs.getInt("idUsuario"));
                u.setIdRol(rs.getInt("idRol"));
                u.setNombre(rs.getString("nombre"));
                u.setApellido(rs.getString("apellido"));
                lista.add(u);
            }
        }
        return lista;
    }

    public boolean cambiarEstado(int idUsuario, String nuevoEstado) throws SQLException {
        String sql = "UPDATE usuario SET estado = ? WHERE idUsuario = ?";
        try (Connection con = DBconfig.getDataSource().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Usuario> listarTodos() throws SQLException {
        String sql = "SELECT * FROM usuario ORDER BY idUsuario DESC";
        List<Usuario> lista = new ArrayList<>();

        try (Connection con = DBconfig.getDataSource().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearUsuario(rs));
            }
        }
        return lista;
    }

}