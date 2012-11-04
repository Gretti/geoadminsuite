package fr.sie.brique.dao;

import java.sql.Connection;
import java.sql.SQLException;
import fr.sie.brique.model.Point;

public interface PointDAO {

	public Point getPointById(String id, Connection conn) throws SQLException;
	public Integer insert(Point point, Connection conn) throws SQLException;
	public Integer delete(Point point, Connection conn) throws SQLException;
	public Boolean update(Point point, Connection conn) throws SQLException;
	
}
