package OntologyPellet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class oretoret {
	public static void main (String args[]){
		ArrayList<String>[] ee = (ArrayList<String>[]) new ArrayList[6];
		cobaList(ee);
		
		List<List<Integer>> duplicates = new ArrayList();	
		cekDuplicate(duplicates, ee);
		
		int i=0;
		while(i<duplicates.size()){
			System.out.println(duplicates.get(i));
			i++;
		}
	}
	
	public static void cobaList(ArrayList<String>[] ee){		
		for(int j=0; j<6; j++){
			ee[j] = new ArrayList<String>();
		}
		ee[0].add("Event_1_009"); ee[0].add("Event_21_009");
		ee[0].add("Event_31_999"); ee[0].add("Event_31_002");
		ee[1].add("1");	ee[1].add("21"); ee[1].add("31"); ee[1].add("31");
		ee[2].add("9");	ee[2].add("9");	ee[2].add("999"); ee[2].add("2");;
		ee[3].add("n");	ee[3].add("n");	ee[3].add("n");	ee[3].add("n");
		
		for(int i=0; i<ee[0].size(); i++){
			for(int j=0; j<ee[0].size();j++)
				System.out.print(ee[j].get(i)+" ");
			System.out.println();
		}
		
		List<List<Integer>> coba = new ArrayList();
		coba.add(new ArrayList<Integer>());
		coba.get(0).add(1);
	}
	
	public static void cekDuplicate(List<List<Integer>> duplicates, ArrayList<String>[] ee){
		int j=-1;
		for(int h=0; h<ee[3].size();h++){
			if(!(ee[3].get(h).equals("m"))){
				j++;
				System.out.println(h + "masuk if");
				duplicates.add(new ArrayList<Integer>());
				duplicates.get(j).add(Integer.parseInt(ee[2].get(h)));
				ee[3].set(h, "m");
				System.out.println("j: " + j);
			}
			for(int i=1; i<ee[3].size();i++){
				System.out.println(" " + h +"masuk for");
				if(h<i){
					if(ee[2].get(h) == ee[2].get(i) && !(ee[3].get(i).equals("m"))){
						System.out.println("   masuk if");
						duplicates.get(j).add(Integer.parseInt(ee[2].get(i)));
						ee[3].set(i, "m");
					}
				}
				break;
			}
			System.out.println(" " + h +"break");
		}
	}
}
