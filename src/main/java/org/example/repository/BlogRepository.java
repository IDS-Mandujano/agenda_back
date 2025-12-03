package org.example.repository;

import org.example.config.DBconfig;
import org.example.model.Blog;
import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class BlogRepository {
    private final DataSource dataSource;

    public BlogRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int crear(Blog b) throws SQLException {
        String sql = """
            INSERT INTO blog (
                idUsuario, titulo, contenido, img, fechaPublicacion, categoria, destacado
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection con = DBconfig.getDataSource().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, b.getIdUsuario());
            ps.setString(2, b.getTitulo());
            ps.setString(3, b.getContenido());
            ps.setBytes(4, b.getImg());
            ps.setDate(5, Date.valueOf(b.getFechaPublicacion()));
            ps.setString(6, b.getCategoria());
            ps.setBoolean(7, b.getDestacado());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public List<Blog> listarTodos() throws SQLException {
        String sql = "SELECT * FROM blog ORDER BY fechaPublicacion DESC";
        List<Blog> lista = new ArrayList<>();

        try (Connection con = DBconfig.getDataSource().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapResultSetToBlog(rs));
            }
        }
        return lista;
    }

    private Blog mapResultSetToBlog(ResultSet rs) throws SQLException {
        Blog b = new Blog();
        b.setIdBlog(rs.getInt("idBlog"));
        b.setIdUsuario(rs.getInt("idUsuario"));
        b.setTitulo(rs.getString("titulo"));
        b.setContenido(rs.getString("contenido"));
        b.setImg(rs.getBytes("img"));
        Date fechaDb = rs.getDate("fechaPublicacion");
        if (fechaDb != null) {
            b.setFechaPublicacion(fechaDb.toLocalDate());
        }
        b.setCategoria(rs.getString("categoria"));
        b.setDestacado(rs.getBoolean("destacado"));
        return b;
    }
}