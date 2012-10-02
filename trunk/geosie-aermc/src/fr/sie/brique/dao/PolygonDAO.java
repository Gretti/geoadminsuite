package fr.sie.brique.dao;

import java.sql.Connection;
import java.sql.SQLException;

import fr.sie.brique.model.Polygon;

public interface PolygonDAO {

	public Polygon getPolygonById(String id, Connection conn) throws SQLException;
	public Integer insert(Polygon poly, Connection conn) throws SQLException;
	public Integer delete(Polygon poly, Connection conn) throws SQLException;
	public Boolean update(Polygon poly, Connection conn) throws SQLException;
	
}
