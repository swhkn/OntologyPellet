package OntologyPellet;

import java.util.ArrayList;

public class oretoret {
	public static void main (String args[]){
		ArrayList<String>[] ee = (ArrayList<String>[]) new ArrayList[6];
		
		for(int j=0; j<6; j++){
			ee[j] = new ArrayList<String>();
		}
		ee[0].add("Event_1_99");
		ee[0].add("Event_21_9");
		ee[0].add("Event_31_999");
		ee[1].add("1");
		ee[1].add("21");
		ee[1].add("31");
		ee[2].add("99");
		ee[2].add("9");
		ee[2].add("999");
		
		for(int i=0; i<ee[0].size(); i++){
			System.out.println(ee[0].get(i));
			System.out.println(" "+ ee[1].get(i));
			System.out.println("  "+ ee[2].get(i));
		}
	}

}
