package fr.sie.brique.dao;

import java.sql.Connection;
import java.sql.SQLException;

import fr.sie.brique.model.Line;

public interface LineDAO {

	public Line getLineById(String id, Connection conn) throws SQLException;
	public Integer insert(Line line, Connection conn) throws SQLException;
	public Integer delete(Line line, Connection conn) throws SQLException;
	public Boolean update(Line line, Connection conn) throws SQLException;
	
}
