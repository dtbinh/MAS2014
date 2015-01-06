/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mas.agent.student;

/**
 *
 * @author michal
 */
public class AdditionalConstraint {
    int [] disabled;

    public AdditionalConstraint(String nogood, int agentRow){

        String[] constraints=nogood.split(";");

        disabled =new int[agentRow];

        for (int i = 0; i < agentRow; i++) {
            String [] str = constraints[i].split(" ");
            int agent=Integer.parseInt(str[0]);
            int value=Integer.parseInt(str[1]);
            disabled[agent-1]=value;
        }
        
    }
    
    public boolean checkConsistency(int[] localView, int value){

        for (int i = 0; i < disabled.length-1; i++) {
            if((localView[i]!= disabled[i]) && (disabled[i] != 0)){
                return true;
            }
        }

        if(value == disabled[disabled.length-1]){
            return false;
        }

        return true;

    }
    
}
