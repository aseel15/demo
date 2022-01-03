package com.example.demo;
import java.util.HashMap;

public class State {
    private String stateName;
    private String nextStateName;
    private String transition;
    boolean exist;
    boolean reachable;
    boolean marked;
    boolean visited;
    HashMap<String, String>transtionsList=new HashMap<>();

    public State(){
    }

    public State(String stateName){
        this.stateName=stateName;
    }

    public State(String nextStateName, String transition){
        this.nextStateName=nextStateName;
        this.transition=transition;
    }
    public String getStateName() {
        return stateName;
    }

    public String getNextStateName() {
        return nextStateName;
    }

    public void setNextStateName(String nextStateName) {
        this.nextStateName = nextStateName;
    }

    public String getTransition() {
        return transition;
    }

    public void setTransition(String transition) {
        this.transition = transition;
    }

    public void setStateName(String stateName) {
        stateName = stateName;
    }


    public void addTrans(String s1, String s2){
        boolean flag=false;
        if(transtionsList.containsKey(s1)){
            if(transtionsList.get(s1).length()==1){
                if(transtionsList.get(s1).equals(s2))
                    flag=true;
            }
            else{
                String s3=transtionsList.get(s1);
                for(int i=0;i<s3.length();i++)
                    if((s3.charAt(i)+"").equals(s2)){
                        flag=true;
                        break;
                    }


            }
            if(!flag)
                transtionsList.put(s1,transtionsList.get(s1)+""+s2);


        }
        else{
            transtionsList.put(s1,s2);
        }
    }

}
