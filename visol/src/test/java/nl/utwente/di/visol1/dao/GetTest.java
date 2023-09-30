package nl.utwente.di.visol1.dao;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.util.ArrayList;
import java.util.List;

import nl.utwente.di.visol1.models.Berth;
import nl.utwente.di.visol1.models.Port;
import nl.utwente.di.visol1.models.Schedule;
import nl.utwente.di.visol1.models.Terminal;
import nl.utwente.di.visol1.models.Vessel;
import nl.utwente.di.visol1.util.Configuration;


/**
 * Tests GET only, without asserting that any other function works Since we can't know what data should be in the database it is not a unit test, but
 * you can see example outputs of the get functions
 */
public class GetTest {


	public static void main(String[] args) {

		try {
			Configuration.useTestEnvironment(true);

			JAXBContext context = JAXBContext.newInstance(Port.class, Terminal.class, Berth.class, Vessel.class, Schedule.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			//Port get test
			List<Port> portList = new ArrayList<>(PortDao.getPorts().values());
			for (Port port : portList) {
				marshaller.marshal(port, System.out);
				System.out.println();
			}
			Port port1 = PortDao.getPort(1);
			marshaller.marshal(port1, System.out);
			System.out.println();

			//Terminal get test
			List<Terminal> terminalList = new ArrayList<>(TerminalDao.getTerminalsByPort(1).values());
			for (Terminal terminal : terminalList) {
				marshaller.marshal(terminal, System.out);
				System.out.println();
			}
			Terminal terminal1 = TerminalDao.getTerminal(1);
			marshaller.marshal(terminal1, System.out);
			System.out.println();

			Berth berth1 = BerthDao.getBerth(1);
			marshaller.marshal(berth1, System.out);
			System.out.println();

			//Vessel get test
			List<Vessel> vesselList = new ArrayList<>(VesselDao.getVesselsByTerminal(1, GenericDao.MIN_TIME, GenericDao.MAX_TIME).values());
			for (Vessel vessel : vesselList) {
				marshaller.marshal(vessel, System.out);
				System.out.println();
			}

			//Schedule get test
			Schedule schedule1 = ScheduleDao.getScheduleByVessel(1);
			marshaller.marshal(schedule1, System.out);
			System.out.println();


		} catch (JAXBException exception) {
			exception.printStackTrace();
		}
	}
}
