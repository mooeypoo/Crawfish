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
	
	private int sad, ead, iad, rad, dad, sch, ech, ich, rch, dch, st, et, it, rt, dt;
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
			this.sad++;
		}else if(agent.getHealthStatus().equals(DiseaseStages.E)){
			this.ead++;
		}else if(agent.getHealthStatus().equals(DiseaseStages.I)){
			this.iad++;
		}else if(agent.getHealthStatus().equals(DiseaseStages.R)){
			this.rad++;
		}else if(agent.getHealthStatus().equals(DiseaseStages.D)){
			this.dad++;
		}
		}// end of agent_list.hasNext()
		int i =0,j=0;
		Iterator<IAgent> child_list = GlobalVars.popListChild.iterator();
		while(child_list.hasNext()){
		IAgent agent = child_list.next();
		if(agent.getType() == GlobalVars.P_TEEN){
			i++;
			if(agent.getHealthStatus().equals(DiseaseStages.S)){
				this.st++;
			}else if(agent.getHealthStatus().equals(DiseaseStages.E)){
				this.et++;
			}else if(agent.getHealthStatus().equals(DiseaseStages.I)){
				this.it++;
			}else if(agent.getHealthStatus().equals(DiseaseStages.R)){
				this.rt++;
			}else if(agent.getHealthStatus().equals(DiseaseStages.D)){
				this.dt++;
			}
		}else if(agent.getType() == GlobalVars.P_CHILD) { // if it's child
			j++;
			if(agent.getHealthStatus().equals(DiseaseStages.S)){
				this.sch++;
			}else if(agent.getHealthStatus().equals(DiseaseStages.E)){
				this.ech++;
			}else if(agent.getHealthStatus().equals(DiseaseStages.I)){
				this.ich++;
			}else if(agent.getHealthStatus().equals(DiseaseStages.R)){
				this.rch++;
			}else if(agent.getHealthStatus().equals(DiseaseStages.D)){
				this.dch++;
			}
		}
		}// end of child_list.hasNext()
		disease_agents = initialize_list(this.sad, this.ead, this.iad, this.rad, this.dad, this.st, this.et, this.it, this.rt, this.dt, this.sch, this.ech, this.ich, this.rch, this.dch);
		reset_them();
		return disease_agents;
		
	}
	
	
	private void reset_them(){
		this.sad = 0; this.ead = 0; this.iad = 0; this.rad = 0; this.dad = 0; this.sch = 0; 
		this.ech = 0; this.ich = 0; this.rch = 0; this.dch= 0; this.st = 0; this.et = 0; 
		this.it = 0; this.rt = 0; this.dt = 0;
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
