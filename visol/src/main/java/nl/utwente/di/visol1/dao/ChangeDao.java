package nl.utwente.di.visol1.dao;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.utwente.di.visol1.models.Change;
import nl.utwente.di.visol1.models.Schedule;
import nl.utwente.di.visol1.models.ScheduleChange;
import nl.utwente.di.visol1.models.Vessel;
import nl.utwente.di.visol1.models.VesselChange;
import nl.utwente.di.visol1.type_adapters.CustomJacksonJsonProvider;

public class ChangeDao {
	/**
	 * Undo Last change made by a certain userAccount
	 * @param employeeEmail email of the user
	 * @return 1 for success, 0 for if there is nothing to undo, -1 for an exception
	 */
	public static int undoLastChange(String employeeEmail) {
		try (GenericDao.Query query = GenericDao.Query.prepared(
			"SELECT date, reason, old, new, undo FROM schedulechange WHERE author = ? UNION ALL SELECT date, reason, old, new, undo FROM vesselchange WHERE author = ? ORDER BY date DESC",
			stmt -> {
				stmt.setString(1, employeeEmail);
				stmt.setString(2, employeeEmail);
			}
		)) {
			ResultSet rs = query.getResultSet();
			int i = 1;
			//we want to get the last change that wasn't undone yet
			//so if the current row is an UNDO, we rs.next() an additional 2 times (so i+=1 instead of i-=1)
			while (i > 0) {
				if (!rs.next()) return 0;
				if (rs.getBoolean("undo")) {
					i += 1;
				} else {
					i -= 1;
				}
			}
			String reason = rs.getString("reason");
			reason = "(undo)" + (reason == null ? "" : " " + reason);
			String oldObject = rs.getString("old");
			String newObject = rs.getString("new");
			Timestamp date = rs.getTimestamp("date");
			ObjectMapper mapper = CustomJacksonJsonProvider.MAPPER;
			if (oldObject == null) {
				// TODO could this not be done just a little bit more elegantly?
				//we need to undo a create
				try {
					Schedule oldSchedule = mapper.readValue(newObject, Schedule.class);
					ScheduleDao.deleteScheduleByVessel(oldSchedule.getVessel());
					ScheduleChangeDao.createScheduleChange(
						new ScheduleChange(0, employeeEmail, null, reason, oldSchedule.getVessel(), newObject, null, true));
					return 1;
				} catch (JsonMappingException wrongClass) {
					//If it's not a schedule, map it to a vessel
					try {
						Vessel oldVessel = mapper.readValue(newObject, Vessel.class);
						VesselDao.deleteVessel(oldVessel.getId());
						VesselChangeDao.createVesselChange(
							new VesselChange(0, employeeEmail, null, reason, oldVessel.getId(), newObject, null, true));
						return 1;
					} catch (IOException e) {
						e.printStackTrace();
						return -1;
					}
				} catch (IOException e) {
					e.printStackTrace();
					return -1;
				}
			} else {
				try {
					Schedule newSchedule = mapper.readValue(oldObject, Schedule.class);
					ScheduleDao.replaceSchedule(newSchedule.getVessel(), newSchedule);
					ScheduleChangeDao.createScheduleChange(
						new ScheduleChange(0, employeeEmail, null, reason, newSchedule.getVessel(), newObject, oldObject, true));
					return 1;
				} catch (JsonMappingException wrongClass) {
					//If it's not a schedule, map it to a vessel
					try {
						Vessel newVessel = mapper.readValue(oldObject, Vessel.class);
						int j = VesselDao.replaceVessel(newVessel.getId(), newVessel);
						if (j == 0) {
							//Replace returned 0, so the vessel doesn't exist
							//this means we need to undo a delete action, so we create the vessel
							VesselDao.createVessel(newVessel);
						}
						VesselChangeDao.createVesselChange(
							new VesselChange(0, employeeEmail, null, reason, newVessel.getId(), newObject, oldObject, true));
						return 1;
					} catch (IOException e) {
						e.printStackTrace();
						return -1;
					}
				} catch (IOException e) {
					e.printStackTrace();
					return -1;
				}
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			return -1;
		}
	}

	/**
	 * Redo last change a certain employee made
	 * @param employeeEmail mail of the user
	 * @return 1 for success, 0 if there is nothing to redo, -1 for an excpetion
	 */
	public static int redoLastChange(String employeeEmail) {
		try (GenericDao.Query query = GenericDao.Query.prepared(
			"SELECT date, old, new, undo FROM schedulechange WHERE author = ? UNION ALL SELECT date, old, new, undo FROM vesselchange WHERE author = ? ORDER BY date DESC LIMIT 1;",
			stmt -> {
				stmt.setString(1, employeeEmail);
				stmt.setString(2, employeeEmail);
			}
		)) {
			ResultSet rs = query.getResultSet();
			if (!rs.next()) return 0;
			//Check if the last change was an UNDO
			//if it wasn't we return 0 and don't do anything
			if (rs.getBoolean("undo")) {
				String oldObject = rs.getString("old");
				Timestamp date = rs.getTimestamp("date");
				ObjectMapper mapper = CustomJacksonJsonProvider.MAPPER;
				// TODO same story here as above, could this be done just a little bit more elegantly?
				if (oldObject == null) {
					//we need to undo a create
					String newObject = rs.getString("new");
					try {
						Schedule oldSchedule = mapper.readValue(newObject, Schedule.class);
						ScheduleDao.deleteScheduleByVessel(oldSchedule.getVessel());
						return ScheduleChangeDao.deleteScheduleChangeByAuthor(employeeEmail, date);
					} catch (JsonMappingException wrongClass) {
						//If it's not a schedule, map it to a vessel
						try {
							Vessel oldVessel = mapper.readValue(newObject, Vessel.class);
							VesselDao.deleteVessel(oldVessel.getId());
							return VesselChangeDao.deleteVesselChangeByAuthor(employeeEmail, date);
						} catch (IOException e) {
							e.printStackTrace();
							return -1;
						}
					} catch (IOException e) {
						e.printStackTrace();
						return -1;
					}
				} else {
					try {
						Schedule newSchedule = mapper.readValue(oldObject, Schedule.class);
						ScheduleDao.replaceSchedule(newSchedule.getVessel(), newSchedule);
						return ScheduleChangeDao.deleteScheduleChangeByAuthor(employeeEmail, date);
					} catch (JsonMappingException wrongClass) {
						//If it's not a schedule, map it to a vessel
						try {
							Vessel newVessel = mapper.readValue(oldObject, Vessel.class);
							int i = VesselDao.replaceVessel(newVessel.getId(), newVessel);
							if (i == 0) {
								VesselDao.createVessel(newVessel);
							}
							return VesselChangeDao.deleteVesselChangeByAuthor(employeeEmail, date);
						} catch (IOException e) {
							e.printStackTrace();
							return -1;
						}
					} catch (IOException e) {
						e.printStackTrace();
						return -1;
					}
				}
			} else {
				return 0;
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			return -1;
		}
	}

	public static List<Change> getChangesByTerminal(int terminalId, Timestamp from, Timestamp to) {
		List<Change> result = new ArrayList<>();
		String queryString = //if it works, it ain't stupid
			"SELECT c.*, v.name AS vessel_name, 'schedule' AS type FROM schedulechange c, vessel v WHERE v.id = c.vessel AND v.destination = ? AND tsrange(?, ?, '[]') @> c.date "
			+ "UNION ALL SELECT c.*, v.name AS vessel_name, 'vessel' AS type FROM vesselchange c, vessel v WHERE v.id = c.vessel AND v.destination = ? AND tsrange(?, ?, '[]') @> c.date "
			+ "ORDER BY date DESC;";
		try (GenericDao.Query query = GenericDao.Query.prepared(queryString, stmt -> {
			stmt.setInt(1, terminalId);
			stmt.setTimestamp(2, from);
			stmt.setTimestamp(3, to);
			stmt.setInt(4, terminalId);
			stmt.setTimestamp(5, from);
			stmt.setTimestamp(6, to);
		})) {
			ResultSet rs = query.getResultSet();
			while (rs.next()) {
				result.add(new Change(
					rs.getInt("id"),
					rs.getString("author"),
					rs.getTimestamp("date"),
					rs.getString("reason"),
					rs.getBoolean("undo"),
					rs.getInt("vessel"),
					rs.getString("vessel_name"),
					rs.getString("type"),
					rs.getString("old"),
					rs.getString("new")
				));
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return result;
	}
}
