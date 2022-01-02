package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class HelloController {


    @FXML
    private TextField pathText;

    @FXML
    protected void onBrowseButtonClick() {
        FileChooser fileChooser=new FileChooser();
        File file = fileChooser.showOpenDialog(new Stage());
        pathText.setText(file.getPath());


        HashMap<String,State> stateHashMap=new HashMap<>();
        ArrayList<State> capitalStates=new ArrayList<>();
        ArrayList<String>finalStates=new ArrayList<>();
        HashMap<String, State> candidateFeasiblePairs=new HashMap<>();
        char []transitions=new char[46];
        try {
            BufferedReader bufferedReader=new BufferedReader(new FileReader(pathText.getText()));
            String []str1=bufferedReader.readLine().split(" ");
            for(int i=0;i<str1.length;i++){
                stateHashMap.put(str1[i],new State(str1[i]));
                stateHashMap.get(str1[i]).exist=true;
                capitalStates.add(new State(str1[i]));
            }
            String []str2=bufferedReader.readLine().split(" ");
            for(int i=0;i<str2.length;i++){
                for(int j=0;j< capitalStates.size();j++){
                    if(str2[i].equals(capitalStates.get(j).getStateName())){
                        finalStates.add(str2[i]);
                        break;
                    }
                }
            }
            String []str3=bufferedReader.readLine().split(" ");
            for(int i=0;i< transitions.length&&i<str3.length;i++){
                transitions[i]=str3[i].charAt(0);
            }


            String str="";
            while((str= bufferedReader.readLine())!=null){
                String []trans=str.split(" ");
                State st=stateHashMap.get(trans[0]);
                st.addTrans(trans[2],trans[1]);

            }

            removeLambda(stateHashMap, finalStates,capitalStates);
            removeNonDeterministic(stateHashMap,capitalStates, finalStates);
            removeUnreachable(stateHashMap,capitalStates, finalStates);
            candidateFeasiblePairs(stateHashMap,capitalStates, finalStates, candidateFeasiblePairs);

            for(int i=0;i< candidateFeasiblePairs.size();i++) {
                String value = String.valueOf(candidateFeasiblePairs.keySet().toArray()[i]);
                checkFeasiblePairs(candidateFeasiblePairs, value, i);
            }
            mergeEquivalentStates(candidateFeasiblePairs,capitalStates,stateHashMap,finalStates);

            for(int j=0;j< capitalStates.size();j++){
                System.out.println(capitalStates.get(j).getStateName());
                for(String st1: stateHashMap.get(capitalStates.get(j).getStateName()).transtionsList.keySet()){
                    System.out.println(st1+" "+stateHashMap.get(capitalStates.get(j).getStateName()).transtionsList.get(st1));
                }

                System.out.println("-----------------------------------");
            }
            System.out.println("the final states : ");
            for(int i=0;i< finalStates.size();i++) {
                System.out.print(finalStates.get(i));
                if(i!=finalStates.size()-1)
                    System.out.print(", ");
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void removeLambda(HashMap<String, State>stateHashMap, ArrayList<String>finalStates,ArrayList<State>capitalState){
        for(int j=capitalState.size()-1;j>=0;j--){
            if(stateHashMap.get(capitalState.get(j).getStateName()).transtionsList.containsKey("@")){
                String state=stateHashMap.get(capitalState.get(j).getStateName()).transtionsList.get("@");
                if(state.length()==1){
                    for (String strNext: stateHashMap.get(state).transtionsList.keySet())
                        stateHashMap.get(capitalState.get(j).getStateName()).addTrans(strNext,stateHashMap.get(state).transtionsList.get(strNext));
                }
                else{

                    for(int i=0;i< state.length();i++)
                        for (String strNext: stateHashMap.get(state.charAt(i)+"").transtionsList.keySet())
                            stateHashMap.get(capitalState.get(j).getStateName()).addTrans(strNext,stateHashMap.get(state.charAt(i)+"").transtionsList.get(strNext));

                }
            }

        }
        //make the state to final if needed
        for(int k=0;k< capitalState.size();k++){
            if(stateHashMap.get(capitalState.get(k).getStateName()).transtionsList.containsKey("@")){
                String state=stateHashMap.get(capitalState.get(k).getStateName()).transtionsList.get("@");
                for (int i=0;i< finalStates.size();i++){
                    if(state.length()==1)
                        if(finalStates.get(i).equals(state)){
                            finalStates.add(capitalState.get(k).getStateName());
                            break;
                        }
                        else{
                            for(int j=0;j< state.length();j++)
                                if(finalStates.get(i).equals(state.charAt(j)+"")){
                                    finalStates.add(state.charAt(j)+"");
                                    break;
                                }
                        }

                }
            }
        }

        for(String st: stateHashMap.keySet()){
            if(stateHashMap.get(st).transtionsList.containsKey("@")){
                //delete the lambda column
                stateHashMap.get(st).transtionsList.remove("@");
            }
        }

    }
    public void removeNonDeterministic(HashMap<String, State>stateHashMap, ArrayList<State>capitalStates,ArrayList<String> finalStates) {


        ArrayList<State> addItems = new ArrayList<>();
        int size = stateHashMap.size();
        int k=0;

        while (k<size) {
            String stateName = capitalStates.get(k).getStateName();
            for (String transition : stateHashMap.get(stateName).transtionsList.keySet()) {
                String states = stateHashMap.get(stateName).transtionsList.get(transition);
                if (states.length() > 1) {
                    for(int i=0;i< states.length();i++) {

                        if (finalStates.contains(states.charAt(i) + ""))
                            if (!finalStates.contains(states)) {
                                finalStates.add(states);
                                break;
                            }
                    }
                    //add it as new and one item
                    addItems.add(new State(states));

                }
            }
            k++;


            for (int j = 0; j < addItems.size(); j++) {
                if ((stateHashMap.get(addItems.get(j).getStateName()) == null) || (!stateHashMap.get(addItems.get(j).getStateName()).exist)) {
                    stateHashMap.put(addItems.get(j).getStateName(), new State(addItems.get(j).getStateName()));
                    stateHashMap.get(addItems.get(j).getStateName()).exist = true;
                    capitalStates.add(addItems.get(j));
                    size++;
                }
                String s = addItems.get(j).getStateName();
                for (int i = 0; i < s.length(); i++)
                    for (String ss : stateHashMap.get(s.charAt(i)+"").transtionsList.keySet())
                        //add their transitions
                        stateHashMap.get(addItems.get(j).getStateName()).addTrans(ss, stateHashMap.get(s.charAt(i)+"").transtionsList.get(ss));


            }

        }

    }
    public void findAccessable(String firstState,HashMap<String, State>stateHashMap, ArrayList<State>capitalStates){
        stateHashMap.get(firstState).reachable=true;
        for (String str : stateHashMap.get(firstState).transtionsList.keySet()) {

            String nextState = stateHashMap.get(firstState).transtionsList.get(str);
            if(!stateHashMap.get(nextState).reachable) {
                stateHashMap.get(nextState).reachable = true;
                findAccessable(nextState, stateHashMap, capitalStates);
            }

        }




    }
    public void removeUnreachable(HashMap<String, State>stateHashMap, ArrayList<State>capitalStates, ArrayList<String>finalState){
        findAccessable(capitalStates.get(0).getStateName(),stateHashMap,capitalStates);
        ArrayList<State>unTickedStates=new ArrayList<>();
        ArrayList<String>unTickedStrings=new ArrayList<>();
        for(int i=0;i<capitalStates.size(); i++) {
            if (!stateHashMap.get(capitalStates.get(i).getStateName()).reachable) {
                unTickedStates.add(capitalStates.get(i));
                unTickedStrings.add(capitalStates.get(i).getStateName());

            }

        }
        capitalStates.removeAll(unTickedStates);
        stateHashMap.keySet().removeAll(unTickedStrings);
        finalState.removeAll(unTickedStrings);



    }
    public void candidateFeasiblePairs(HashMap<String, State>stateHashMap, ArrayList<State>capitalStates, ArrayList<String>finalState,HashMap candidateFeasiblePairs){
        ArrayList<String> nonFinalState=new ArrayList<>();
        HashMap<String, State>feasiblePairsHash=candidateFeasiblePairs;
        ArrayList<String>feasiblePairsList=new ArrayList<>();

        boolean flag1=true;
        boolean flag2=true;
        for (int i=0;i< capitalStates.size();i++)
            if(!finalState.contains(capitalStates.get(i).getStateName())) {
                nonFinalState.add(capitalStates.get(i).getStateName());

            }

        for (int i=0;i<nonFinalState.size();i++)
            for (int j=1;j<nonFinalState.size();j++)
                if(!nonFinalState.get(i).equals(nonFinalState.get(j)))
                    if(stateHashMap.get(nonFinalState.get(i)).transtionsList.size()==stateHashMap.get(nonFinalState.get(j)).transtionsList.size()) {
                        for (String trans : stateHashMap.get(nonFinalState.get(i)).transtionsList.keySet())
                            if (!stateHashMap.get(nonFinalState.get(j)).transtionsList.containsKey(trans)) {
                                flag1 = false;
                                break;

                            }
                        if(flag1) {
                            if(!(feasiblePairsHash.containsKey(nonFinalState.get(i) + "," + nonFinalState.get(j))||feasiblePairsHash.containsKey(nonFinalState.get(j) + "," + nonFinalState.get(i))))
                                feasiblePairsHash.put(nonFinalState.get(i) + "," + nonFinalState.get(j), new State(nonFinalState.get(i) + "," + nonFinalState.get(j)));

                        }
                    }


        for (int i=0;i<finalState.size();i++)
            for (int j=i+1;j<finalState.size();j++)
                if(!finalState.get(i).equals(finalState.get(j)))
                    if(stateHashMap.get(finalState.get(i)).transtionsList.size()==stateHashMap.get(finalState.get(j)).transtionsList.size()) {
                        for (String trans : stateHashMap.get(finalState.get(i)).transtionsList.keySet())
                            if (!stateHashMap.get(finalState.get(j)).transtionsList.containsKey(trans)) {
                                flag2 = false;
                                break;

                            }
                        if(flag2)
                            if(!(feasiblePairsHash.containsKey(finalState.get(i)+","+finalState.get(j))))
                                feasiblePairsHash.put(finalState.get(i)+","+finalState.get(j),new State(finalState.get(i)+","+finalState.get(j)));
                    }

        for (String feasible: feasiblePairsHash.keySet()){
            String []feasiblePair=feasible.split(",");
            int size = stateHashMap.get(feasiblePair[0]).transtionsList.size();
            for(String str : stateHashMap.get(feasiblePair[0]).transtionsList.keySet()) {
                String str1 = stateHashMap.get(feasiblePair[0]).transtionsList.get(str);
                String str2 = stateHashMap.get(feasiblePair[1]).transtionsList.get(str);
                feasiblePairsHash.get(feasible).transtionsList.put(str, str1 + "," + str2);

            }


        }

    }
    public void checkFeasiblePairs(HashMap <String, State>candidateFeasiblePairs, String string, int i){
        candidateFeasiblePairs.get(string).visited=true;
        for (String st: candidateFeasiblePairs.get(string).transtionsList.keySet()){
            String str=candidateFeasiblePairs.get(string).transtionsList.get(st);
            //System.out.println("(r,s) = "+str);
            String []split= str.split(",");
            if(!split[0].equals(split[1])) {
                if (!(str.equals(st) || (split[1] + "," + split[0]).equals(st))) {
                    if (!(candidateFeasiblePairs.containsKey(str) || candidateFeasiblePairs.containsKey(split[1] + "," + split[0]))) {
                        candidateFeasiblePairs.get(string).marked = true;
                        break;
                    } else {
                        if (candidateFeasiblePairs.get(str) != null) {
                            if (candidateFeasiblePairs.get(str).marked) {
                                candidateFeasiblePairs.get(string).marked = true;
                                break;
                            } else if(!candidateFeasiblePairs.get(str).visited)
                                checkFeasiblePairs(candidateFeasiblePairs, str,i);


                        } else {
                            if (candidateFeasiblePairs.get(split[1] + "," + split[0]).marked) {
                                candidateFeasiblePairs.get(string).marked = true;
                                break;
                            }else if(!candidateFeasiblePairs.get(split[1] + "," + split[0]).visited)
                                checkFeasiblePairs(candidateFeasiblePairs, split[1] + "," + split[0],i);
                        }

                    }

                }
                //if the next state pair same to the candidate feasible pair
                else
                if(i<candidateFeasiblePairs.size()) {
                    String value=String.valueOf(candidateFeasiblePairs.keySet().toArray()[++i]);
                    checkFeasiblePairs(candidateFeasiblePairs,value,i);
                }
            }

        }

    }
    public void mergeEquivalentStates(HashMap<String, State>candidateFeasiblePairs, ArrayList<State>capitalStates, HashMap<String, State>stateHashMap, ArrayList<String>finalStates){
        ArrayList<State>equivalentValues=new ArrayList<>();
        ArrayList<String>equivalentStrings=new ArrayList<>();

        for(String str: candidateFeasiblePairs.keySet()){
            if(!candidateFeasiblePairs.get(str).marked) {
                String[] val=str.split(",");
                equivalentStrings.add(val[1]);
                for(int i=0;i< capitalStates.size();i++){
                    if(capitalStates.get(i).getStateName().equals(val[1])&&!equivalentValues.contains(capitalStates.get(i)))
                        equivalentValues.add(capitalStates.get(i));
                    for(String stt: stateHashMap.get(capitalStates.get(i).getStateName()).transtionsList.keySet()) {
                        if (stateHashMap.get(capitalStates.get(i).getStateName()).transtionsList.get(stt).equals(val[1])) {
                            stateHashMap.get(capitalStates.get(i).getStateName()).transtionsList.replace(stt, val[1], val[0]);
                        }
                    }
                }
            }
        }

        capitalStates.removeAll(equivalentValues);
        stateHashMap.keySet().removeAll(equivalentStrings);
        finalStates.removeAll(equivalentStrings);





    }


}