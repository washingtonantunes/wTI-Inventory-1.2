package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import db.DB;
import db.DBException;
import entities.Monitor;
import model.dao.MonitorDao;

public class MonitorDaoJDBC implements MonitorDao {

	private Connection conn;

	public MonitorDaoJDBC(Connection conn) {
		this.conn = conn;
	}

	@Override
	public void insert(Monitor obj) {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(
					"INSERT INTO `monitors` "
					+ "(`serialNumberMonitor`,"
					+ "`modelMonitor`,"
					+ "`patrimonyNumberMonitor`,"
					+ "`statusMonitor`,"
					+ "`dateEntry`) "
					+ "VALUES "
					+ "(?, ?, ?, ?, ?)");
			
			st.setString(1, obj.getSerialNumberMonitor());
			st.setString(2, obj.getModelMonitor());
			st.setString(3, obj.getPatrimonyNumberMonitor());
			st.setString(4, obj.getStatusMonitor());
			st.setDate(5, new java.sql.Date(obj.getDateEntry().getTime()));

			st.executeUpdate();
		} catch (SQLException e) {
			throw new DBException(e.getMessage());
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public void update(Monitor obj) {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(
					"UPDATE `monitors` "
					+ "SET `patrimonyNumberMonitor` = ? "
					+ "WHERE `serialNumberMonitor` = ?");

			st.setString(1, obj.getPatrimonyNumberMonitor());
			st.setString(2, obj.getSerialNumberMonitor());

			st.executeUpdate();
		} 
		catch (SQLException e) {
			throw new DBException(e.getMessage());
		} 
		finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public void updateStatus(String serialNumberMonitor, String status) {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(
					"UPDATE `monitors` "
					+ "SET `statusMonitor` = ? "
					+ "WHERE `serialNumberMonitor` = ?");

			st.setString(1, status);
			st.setString(2, serialNumberMonitor);

			st.executeUpdate();
		} 
		catch (SQLException e) {
			throw new DBException(e.getMessage());
		} 
		finally {
			DB.closeStatement(st);
		}
	}
	
	@Override
	public void disable(String serialNumberMonitor, String status, String reason) {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(
					"UPDATE `monitors` " 
					+ "SET `statusEquipment` = ?, `reason` = ? "
					+ "WHERE `serialNumber` = ?");

			st.setString(1, status);
			st.setString(2, reason);
			st.setString(3, serialNumberMonitor);
			
			st.executeUpdate();
		} 
		catch (SQLException e) {
			throw new DBException(e.getMessage());
		} 
		finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public List<Monitor> findAll() {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement("SELECT * FROM `monitors`");

			rs = st.executeQuery();

			List<Monitor> monitors = new ArrayList<Monitor>();

			while (rs.next()) {
				Monitor monitor = new Monitor();

				monitor.setSerialNumberMonitor(rs.getString("serialNumberMonitor"));
				monitor.setModelMonitor(rs.getString("modelMonitor"));
				monitor.setPatrimonyNumberMonitor(rs.getString("patrimonyNumberMonitor"));
				monitor.setStatusMonitor(rs.getString("statusMonitor"));
				monitor.setDateEntry(rs.getDate("dateEntry"));
				//monitor.setChanges(Window.getChange().stream().filter(c -> c.getObject().equals(monitor.getSerialNumberMonitor())).collect(Collectors.toList()));
				monitor.setReason(rs.getString("reason"));
				monitors.add(monitor);
			}
			return monitors;
		} 
		catch (SQLException e) {
			throw new DBException(e.getMessage());
		} 
		finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}
}