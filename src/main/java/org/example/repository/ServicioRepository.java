package org.example.repository;

import org.example.config.DBconfig;
import org.example.model.Servicio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServicioRepository {

    public List<Servicio> listarTodos() throws SQLException {
        String sql = "SELECT * FROM servicio";
        List<Servicio> lista = new ArrayList<>();

        try (Connection con = DBconfig.getDataSource().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Servicio s = new Servicio();
                s.setIdServicio(rs.getInt("idServicio"));
                s.setNombre(rs.getString("nombre"));
                s.setPrecio(rs.getBigDecimal("precio"));
                lista.add(s);
            }
        }
        return lista;
    }
}