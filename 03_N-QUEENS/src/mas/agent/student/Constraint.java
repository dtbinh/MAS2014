/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mas.agent.student;

/**
 *
 * @author michal
 */
public class Constraint {
    int firstPos, secondPos;
   
    public Constraint(int first, int second){
        firstPos  = first;
        secondPos = second;
        
    }
    
    public boolean checkConsistency(int x1, int x2){

        //0 = neprirazena hodnota
        if(x1==0||x2==0){
            return true;
        }

        //sloupce
        if(x1==x2){
            return false;
        }

        //hlavni diagonala
        if((x1+ firstPos)==(x2+ secondPos)){
            return false;
        }

        //vedlejsi diagonala
        if((x1- firstPos)==(x2- secondPos)){
            return false;
        }

        return true;

    }
}
