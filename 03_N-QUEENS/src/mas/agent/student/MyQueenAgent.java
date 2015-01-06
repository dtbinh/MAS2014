package mas.agent.student;

import java.util.LinkedList;
import java.util.List;

import mas.agent.MASQueenAgent;
import cz.agents.alite.communication.Message;

/**
 * This example agent illustrates the usage API available in the MASQueenAgent class.
 */
public class MyQueenAgent extends MASQueenAgent {

    int nAgents, agentRow, agentValue;        //pocet agentu,radek agenta,jeho aktualni x-ova souradnice
    boolean isActualAgentValueConsistent,solutionFound, allStarted;

    static long startTime;

    // bezpecnostni 20s timelimit, uzije se jen v pripade, ze nejakym zpusobem selze detekce neexistence reseni
    static final int LIMIT = 20000;

    //ulozeni nazvu agentu a detekce jejich startu
    static String [] nameMap;

    // aktualni hodnoty ostatnich agentu
    int [] localView;
    //hodnoty potencialne spravneho reseni od agenta s nizsi prioritou
    int [] potentialGoodAssignment;

    LinkedList<Constraint> constraints;
    LinkedList<AdditionalConstraint> additionalConstraints;


    public MyQueenAgent(int agentId, int nAgents) {
		// Leave this method as it is...
    	super(agentId, nAgents);
	}

	@Override
	protected void start(int agentId, int nAgents) {
		// This method is called when the agent is initialized.
		// e.g., you can start sending messages: 
		//broadcast(new StringContent("Hello world"));

        agentRow = agentId+1; agentValue = 0;
        this.nAgents = nAgents;

        isActualAgentValueConsistent = false; allStarted = false; solutionFound = false;

        constraints = new LinkedList<>();
        additionalConstraints = new LinkedList<>();

        localView = new int[nAgents];
        potentialGoodAssignment = new int[nAgents];

        if(nameMap == null){
            nameMap = new String[nAgents];
        }

        nameMap[agentRow -1] = Integer.toString(agentId);

        // inicializace zakladnich podminek
        for (int i = 0; i < agentRow -1; i++) {
            constraints.add(new Constraint(agentRow,i+1));
        }

        // posledni agent odstartuje vypocet a ulozi cas startu
        if(agentId == nAgents-1){
            broadcast(new MyStringContent("Alright let's go"));
            startTime=System.currentTimeMillis();
        }

	}

	@Override
	protected void processMessages(List<Message> newMessages) {
		// This method is called whenever there are any new messages for the robot 
		
	/*	// You can list the messages like this:
        for (Message message : newMessages) {
            System.out.println(getAgentId() + ": Received a message from " + message.getSender() + " with the content " + message.getContent().toString());
        }*/

        readMessagesAndReact(newMessages);

        // kontrola inicializace vsech agentu
        if(!allStarted){

            boolean allAdded = true;
            for (int i = 0; i < nAgents; i++) {
                if(nameMap[i] == null){
                    allAdded = false;
                }
            }

            allStarted = allAdded;

            if(allStarted){
                agentValue = 1;
                localView[agentRow -1] = 1;
                broadcast(new MyStringContent("handle-ok:"+ agentRow +" "+ agentValue));
            }

        }

        // pokud je agent s nejvyssi prioritou konzistentni, je search u konce
        if(checkConsistency() && agentRow ==1){
            solutionFound =true;
            broadcast(new MyStringContent("solutionFound"));
        }

        if(solutionFound){
            System.out.println("Queen on row "+agentRow+" is placed!");
            notifySolutionFound(agentValue -1);
        }

        // pojistka ukonceni vypoctu
        if(System.currentTimeMillis() - startTime > LIMIT){
            System.out.println("Sefe, skoncili jsme pojistkou!");
            notifySolutionDoesNotExist();
        }

        // This is how you send a message to the agent "2":
        // sendMessage("2", new StringContent("Hello world"));

        // This is how you broadcast a message:
        // broadcast(new StringContent("Hello world"));

        // This is how you notify us that all the agents are finished with computing and successfully found a cons,.istent solution.
        // Each agent must call this method. The first parameter represent the index of the column, 
        // where the agent(queen) stands within the found solution. The columns are indexed 0,..,(nAgents-1).
       // notifySolutionFound(0 /* i.e. first column */);

        // In the case there is no valid solution, at least one agent should call the following method:
        // notifySolutionDoesNotExist();

        // You can slow down the search process like this:
       /* try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}*/
		
	}


    private void readMessagesAndReact(List<Message> messages){

        for (Message message : messages) {

            String[] split = message.getContent().toString().split(":");

            if(split[0].equals("good")) {

                String[] as = split[1].split(";");

                for (int i = 0; i < potentialGoodAssignment.length; i++) {
                    String[] str = as[i].split(" ");
                    potentialGoodAssignment[i] = Integer.parseInt(str[1]);
                }

            } else if(split[0].equals("nogood")){

                additionalConstraints.add(new AdditionalConstraint(split[1], agentRow));
                checkLocalView();
                broadcast(new MyStringContent("handle-ok:" + agentRow + " " + agentValue));

            } else if(split[0].equals("handle-ok")){

                String [] string = split[1].split(" ");
                int xj = Integer.parseInt(string[0]);
                int j = Integer.parseInt(string[1]);
                localView[xj-1] = j;
                checkLocalView();

            } else if(split[0].equals("solutionFound")) {

                solutionFound = true;

            } else if(split[0].equals("solutionNotFound")) {
                
                notifySolutionDoesNotExist();
                
            } else {

                System.out.println("Received init message.");

            }
        }
    }

    private boolean checkConsistency() {         //detekce konzistence řešení daného agenta na základě zpráv od potomka

        if (!isActualAgentValueConsistent) {
            return false;
        }

        // true pokud je LocalView jiz uplne, tedy vyplneme az do konce
        boolean isLocalViewReady = true;

        for (int i = 0; i < localView.length; i++) {
            if(localView[i] == 0){
                isLocalViewReady = false;
                break;
            }
        }

        if (isLocalViewReady) {

            //posledni agent vytvori potentialGoodAssignment a posle ho vys
            if (agentRow == nAgents) {

                System.arraycopy(localView, 0, potentialGoodAssignment, 0, localView.length);
                StringBuilder sb=new StringBuilder("good:");

                for (int i = 0; i < potentialGoodAssignment.length; i++) {
                    sb.append(i+1);
                    sb.append(" ");
                    sb.append(potentialGoodAssignment[i]);
                    sb.append(";");
                }

                sendMessage(nameMap[agentRow -2], new MyStringContent(sb.toString()));
                return true;
            }

            //kontrola, zda je localView agenta shodny se zpravou potomka
            for (int i = 0; i < localView.length; i++) {
                if (localView[i] != potentialGoodAssignment[i]) {
                    return false;
                }
            }

            //pokud ma agent rodice, posle mu zprávu o potencialnim reseni
            if (agentRow > 1) {
                StringBuilder sb=new StringBuilder("good:");

                for (int i = 0; i < potentialGoodAssignment.length; i++) {
                    sb.append(i+1);
                    sb.append(" ");
                    sb.append(potentialGoodAssignment[i]);
                    sb.append(";");
                }

                sendMessage(nameMap[agentRow -2], new MyStringContent(sb.toString()));
            }

            return true;

        } else {
            return false;
        }
    }

    private void checkLocalView(){

        if(!checkConsistency(agentValue)){
            boolean found=false;

            for (int i = 1; i <= nAgents; i++) {
                if((i!= agentValue) && checkConsistency(i)){
                    agentValue =i;
                    localView[agentRow -1]=i;
                    found=true;
                    break;
                }
            }

            if(!found){
                isActualAgentValueConsistent =false;
                backtrack();
                // System.out.println("DING!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }else {
                isActualAgentValueConsistent =true;
                broadcast(new MyStringContent("handle-ok:" + agentRow + " " + agentValue));
            }

        }else {
            isActualAgentValueConsistent =true;
        }
    }

    private boolean checkConsistency(int value){

        for (Constraint constr : constraints) {
            if (!constr.checkConsistency(value, localView[constr.secondPos - 1])){
                return false;
            }
        }

        for (AdditionalConstraint constr : additionalConstraints) {
            if (!constr.checkConsistency(localView, value)){
                return false;
            }
        }

        return true;
    }

    private void backtrack(){

        // reseni neexistuje, pokud backtrack vola prvni agent
        if(agentRow ==1) {
            // System.out.println("DING!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            notifySolutionDoesNotExist();
            broadcast(new MyStringContent("solutionNotFound"));

        } else {
            // nalezeni agenta, kteremu se zasle nogood, tedy ten s nejnizsi prioritou z tech s vyssi prioritou
            int toSend=0;
            for(int i= agentRow -2;i>=0;i--){
                if(localView[i]!=0){
                    toSend = i+1;
                    break;
                }
            }

            //zakodovani nogood do zpravy
            StringBuilder sb = new StringBuilder("nogood:");

            for (int i = 0; i < agentRow -1; i++) {
                sb.append(i+1);
                sb.append(" ");
                sb.append(localView[i]);
                sb.append(";");
            }

            String message=sb.toString();

            sendMessage(nameMap[toSend-1],new MyStringContent(message));

            //smazani hodnoty agenta, kteremu byl odeslan nogood
            localView[toSend-1]=0;
            checkLocalView();
        }

    }


}