package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import db.DB;
import db.DBException;
import entities.Equipment;
import entities.Inventory;
import entities.Monitor;
import entities.Project;
import entities.User;
import entities.WorkPosition;
import model.dao.InventoryDao;

public class InventoryDaoJDBC implements InventoryDao {
	
	private Connection conn;

	public InventoryDaoJDBC(Connection conn) {
		this.conn = conn;
	}

	@Override
	public void insert(Inventory obj) {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(
					"INSERT INTO `inventories` "
					+ "(`workPosition`,"
					+ "`project`,"
					+ "`user`,"
					+ "`equipment`,"
					+ "`monitor1`, "
					+ "`monitor2`) "
					+ "VALUES "
					+ "(?, ?, ?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS);
			
			st.setString(1, obj.getWorkPosition().getWorkPoint());
			st.setString(2, obj.getProject().getNameProject());
			st.setString(3, obj.getUser().getRegistration());
			st.setString(4, obj.getEquipment().getSerialNumber());
			st.setString(5, obj.getMonitor1().getSerialNumberMonitor());
			st.setString(6, obj.getMonitor2().getSerialNumberMonitor());
			
			int rowsAffected = st.executeUpdate();
			
			if (rowsAffected > 0) {
				ResultSet rs = st.getGeneratedKeys();
				if (rs.next()) {
					int id = rs.getInt(1);
					obj.setIdInventory(id);
				}
				DB.closeResultSet(rs);
			}
			else {
				throw new DBException("Unexpected error! No rows affected!");
			}
		}
		catch (SQLException e) {
			throw new DBException(e.getMessage());
		}
		finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public void update(Inventory obj) {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(
					"UPDATE `inventories` "
					+ "SET `workPosition` = ?, `equipment` = ?, `monitor1` = ?, `monitor2` = ? "
					+ "WHERE `idInventory` = ?");

			st.setString(1, obj.getWorkPosition().getWorkPoint());
			st.setString(2, obj.getEquipment().getSerialNumber());
			st.setString(3, obj.getMonitor1().getSerialNumberMonitor());
			st.setString(4, obj.getMonitor2().getSerialNumberMonitor());
			st.setInt(5, obj.getIdInventory());

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
	public void deleteById(Integer id) {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement("DELETE FROM `inventories` WHERE `idInventory` = ?");
			
			st.setInt(1, id);
			
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
	public List<Inventory> findAll() {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement("CALL `wti_inventory`.`list_inventory_1.2`()");

			rs = st.executeQuery();
			
			List<Inventory> list = new ArrayList<Inventory>();
			Map<String, WorkPosition> workPositions = new HashMap<>();
			Map<String, Project> projects = new HashMap<>();
			Map<String, User> users = new HashMap<>();
			Map<String, Equipment> equipments = new HashMap<>();
			Map<String, Monitor> monitors = new HashMap<>();
			
			while (rs.next()) {
				int id = rs.getInt("IdInventory");
				
				WorkPosition workPosition = workPositions.get(rs.getString("workPoint"));
				
				if (workPosition == null) {
					workPosition = instatiateWorkPosition(rs);
					workPositions.put(rs.getString("workPoint"), workPosition);
				}
				
				Project project = projects.get(rs.getString("nameProject"));
				
				if (project == null) {
					project = instatiateProject(rs);
					projects.put(rs.getString("nameProject"), project);
				}
				
				User user = users.get(rs.getString("registration"));
				
				if (user == null) {
					user = instatiateUser(rs);
					users.put(rs.getString("registration"), user);
				}
				
				Equipment equipment = equipments.get(rs.getString("serialNumber"));

				if (equipment == null) {
					equipment = instatiateEquipment(rs);
					equipments.put(rs.getString("serialNumber"), equipment);
				}
				
				Monitor monitor1 = monitors.get(rs.getString("serialNumberMonitor1"));

				if (monitor1 == null) {
					monitor1 = instatiateMonitor1(rs);
					monitors.put(rs.getString("serialNumberMonitor1"), monitor1);
				}
				
				Monitor monitor2 = monitors.get(rs.getString("serialNumberMonitor2"));

				if (monitor2 == null) {
					monitor2 = instatiateMonitor2(rs);
					monitors.put(rs.getString("serialNumberMonitor2"), monitor2);
				}
								
				Inventory obj = new Inventory(id, workPosition, project, user, equipment, monitor1, monitor2);
				list.add(obj);
			}
			return list;
		} 
		catch (SQLException e) {
			throw new DBException(e.getMessage());
		} 
		finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	private WorkPosition instatiateWorkPosition(ResultSet rs) throws SQLException {
		WorkPosition workPosition = new WorkPosition();
		workPosition.setWorkPoint(rs.getString("workPoint"));
		workPosition.setLocation(rs.getString("location"));
		workPosition.setFloors(rs.getString("floors"));
		workPosition.setNetPoint(rs.getString("netPoint"));
		return workPosition;
	}
	
	private Project instatiateProject(ResultSet rs) throws SQLException {
		Project project = new Project();
		project.setIdProject(rs.getInt("idProject"));
		project.setNameProject(rs.getString("nameProject"));
		project.setLocality(rs.getString("locality"));
		project.setCostCenter(rs.getString("costCenter"));
		return project;
	}
	
	private User instatiateUser(ResultSet rs) throws SQLException {
		User user = new User();
		user.setRegistration(rs.getString("registration"));
		user.setNameUser(rs.getString("nameUser"));
		user.setCPF(rs.getString("cpf"));
		user.setPhone(rs.getString("phone"));
		user.setProject(rs.getString("project"));
		user.setEmail(rs.getString("email"));
		user.setDepartment(rs.getString("department"));
		return user;
	}

	private Equipment instatiateEquipment(ResultSet rs) throws SQLException {
		Equipment equipment = new Equipment();
		equipment.setSerialNumber(rs.getString("serialNumber"));
		equipment.setHostName(rs.getString("hostname"));
		equipment.setAddressMAC(rs.getString("addressMAC"));
		equipment.setTypeEquipment(rs.getString("typeEquipment"));
		equipment.setPatrimonyNumberEquipment(rs.getString("patrimonyNumberEquipment"));
		equipment.setBrandEquipment(rs.getString("brandEquipment"));
		equipment.setModelEquipment(rs.getString("modelEquipment"));
		equipment.setMemoryRam(rs.getString("memoryRam"));
		equipment.setHardDisk(rs.getString("hardDisk"));
		equipment.setCostType(rs.getString("costType"));
		equipment.setValueEquipment(rs.getDouble("valueEquipment"));
		return equipment;
	}
	
	private Monitor instatiateMonitor1(ResultSet rs) throws SQLException {
		Monitor monitor = new Monitor();
		monitor.setSerialNumberMonitor(rs.getString("serialNumberMonitor1"));
		monitor.setModelMonitor(rs.getString("modelMonitor1"));
		monitor.setPatrimonyNumberMonitor(rs.getString("patrimonyNumberMonitor1"));
		return monitor;
	}
	
	private Monitor instatiateMonitor2(ResultSet rs) throws SQLException {
		Monitor monitor = new Monitor();
		monitor.setSerialNumberMonitor(rs.getString("serialNumberMonitor2"));
		monitor.setModelMonitor(rs.getString("modelMonitor2"));
		monitor.setPatrimonyNumberMonitor(rs.getString("patrimonyNumberMonitor2"));
		return monitor;
	}
}