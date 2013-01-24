package repastcity3.agent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import repastcity3.agent.IAgent.DiseaseStages;
import repastcity3.main.GlobalVars;



public class Operations {
	
	private static int sad, ead, iad, rad, dad, sch, ech, ich, rch, dch, st, et, it, rt, dt;
	private static int childC, teenC;

	public static String getTheDate(){
		DateFormat dateFor = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		StringBuilder sb = new StringBuilder(dateFor.format(date)); 
		sb.setCharAt(4, '.');
		sb.setCharAt(7, '.');
		sb.setCharAt(10, '.');
		sb.setCharAt(13, '_');
		sb.setCharAt(16, '_');
		
		
		return sb.toString();
	}
	
	public List<Integer> getTheDiseasesAndAgents(){
		List<Integer> disease_agents = new ArrayList<Integer>();
		
		
		Iterator<IAgent> agent_list = GlobalVars.popListAdult.iterator();
		while(agent_list.hasNext()){
		IAgent agent = agent_list.next();
		if(agent.getHealthStatus().equals(DiseaseStages.S)){
			sad++;
		}else if(agent.getHealthStatus().equals(DiseaseStages.E)){
			ead++;
		}else if(agent.getHealthStatus().equals(DiseaseStages.I)){
			iad++;
		}else if(agent.getHealthStatus().equals(DiseaseStages.R)){
			rad++;
		}else if(agent.getHealthStatus().equals(DiseaseStages.D)){
			dad++;
		}
		}// end of agent_list.hasNext()
		int i =0,j=0;
		Iterator<IAgent> child_list = GlobalVars.popListChild.iterator();
		while(child_list.hasNext()){
		IAgent agent = child_list.next();
		if(agent.getType() == GlobalVars.P_TEEN){
			i++;
			if(agent.getHealthStatus().equals(DiseaseStages.S)){
				st++;
			}else if(agent.getHealthStatus().equals(DiseaseStages.E)){
				et++;
			}else if(agent.getHealthStatus().equals(DiseaseStages.I)){
				it++;
			}else if(agent.getHealthStatus().equals(DiseaseStages.R)){
				rt++;
			}else if(agent.getHealthStatus().equals(DiseaseStages.D)){
				dt++;
			}
		}else if(agent.getType() == GlobalVars.P_CHILD) { // if it's child
			j++;
			if(agent.getHealthStatus().equals(DiseaseStages.S)){
				sch++;
			}else if(agent.getHealthStatus().equals(DiseaseStages.E)){
				ech++;
			}else if(agent.getHealthStatus().equals(DiseaseStages.I)){
				ich++;
			}else if(agent.getHealthStatus().equals(DiseaseStages.R)){
				rch++;
			}else if(agent.getHealthStatus().equals(DiseaseStages.D)){
				dch++;
			}
		}
		}// end of child_list.hasNext()
		disease_agents = initialize_list(sad, ead, iad, rad, dad, st, et, it, rt, dt, sch, ech, ich, rch, dch);

		return disease_agents;
		
	}
	
	
	
	private List<Integer> initialize_list(int s_ad, int e_ad, int i_ad,
			int r_ad, int d_ad, int s_t, int e_t, int i_t, int r_t, int d_t, int s_ch, int e_ch,
			int i_ch, int r_ch, int d_ch) {
		// TODO Auto-generated method stub
		List<Integer> temp = new ArrayList<Integer>();
		temp.add(s_ad); temp.add(e_ad); temp.add(i_ad); temp.add(r_ad); temp.add(d_ad);
		temp.add(s_t); temp.add(e_t); temp.add(i_t); temp.add(r_t); temp.add(d_t);
		temp.add(s_ch); temp.add(e_ch); temp.add(i_ch); temp.add(r_ch); temp.add(d_ch);
		return temp;
	}

	public List<Integer> getAgentCount(){
		List<Integer> temp = new ArrayList<Integer>();
		temp.add(GlobalVars.popListAdult.size());
		Iterator<IAgent> children = GlobalVars.popListChild.iterator();
		while(children.hasNext()){
			if ( children.next().getType() == GlobalVars.P_CHILD ){
				childC++;
			}
		}
		temp.add(childC);
		teenC = GlobalVars.popListChild.size() - childC;
		childC= 0;
		temp.add(teenC);
		
		
		return temp;
	}
}
