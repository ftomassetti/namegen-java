package com.github.ftomassetti;

import java.util.*;

public class NameGenerator {
    
    private TransitionMap transitionMap;

    /**
     * State is given by the last two events 
     */
    private static class State {
        private Event last;
        private Event nextToLast;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            State state = (State) o;

            if (!last.equals(state.last)) return false;
            if (!nextToLast.equals(state.nextToLast)) return false;

            return true;
        }
        
        public State next(Event event){
            return new State(event, last);
        }

        @Override
        public int hashCode() {
            int result = last.hashCode();
            result = 31 * result + nextToLast.hashCode();
            return result;
        }

        public State(Event last, Event nextToLast) {
            if (last == null || nextToLast == null){
                throw new NullPointerException();
            }
            this.last = last;
            this.nextToLast = nextToLast;
            
        }

        public boolean isEnd() {
            return last.isEnd();
        }
    }
    
    private static class Event {
        
        static Event START = new Event(true, false, null);
        static Event END = new Event(false, true, null);
        
        private boolean start, end;
        private Character character;
        
        public static Event character(Character character){
            return new Event(false, false, character);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Event that = (Event) o;

            if (end != that.end) return false;
            if (start != that.start) return false;
            if (character != null ? !character.equals(that.character) : that.character != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (start ? 1 : 0);
            result = 31 * result + (end ? 1 : 0);
            result = 31 * result + (character != null ? character.hashCode() : 0);
            return result;
        }

        public Event(boolean start, boolean end, Character character){
            this.start = start;
            this.end = end;
            this.character = character;
        }

        public boolean isStart(){
            return start;
        }
        
        public boolean isEnd(){
            return end;
        }
        
        public Character getChar(){
            if (isStart() || isEnd()){
                throw new IllegalStateException("This state does not represent a character");
            }
            return character;
        }
    }
    
    private static class PossibileTransition {
        private float probability;

        public PossibileTransition(float probability, Event targetState) {
            this.probability = probability;
            this.targetState = targetState;
        }

        public float getProbability() {
            return probability;
        }

        public Event getTargetState() {
            return targetState;
        }

        private Event targetState;
        
    }
    
    private static class TransitionMap {
        
        private Map<State, List<PossibileTransition>> transitionsMap;
        
        private TransitionMap(Map<State, List<PossibileTransition>> transitionsMap){
            this.transitionsMap = transitionsMap;
        }
        
        Event next(State currentGenerationState, Random random) {
            if (!transitionsMap.containsKey(currentGenerationState)){
                throw new IllegalStateException();
            }
            float v = random.nextFloat();
            for (PossibileTransition possibileTransition : transitionsMap.get(currentGenerationState)){
                if (v <= possibileTransition.getProbability()){
                    return possibileTransition.getTargetState();
                }
                v -= possibileTransition.getProbability();
            }
            throw new RuntimeException("The probability map was not built correctly");
        }
    }
    
    private NameGenerator(TransitionMap transitionMap){
        this.transitionMap = transitionMap;
    }
    
    public String name(Random random){
        State state = new State(Event.START, Event.START);
        StringBuffer sb = new StringBuffer();
        while (!state.isEnd()){
            Event event = this.transitionMap.next(state, random);
            if (!event.isEnd()){
                sb.append(event.getChar());
            }
            state = state.next(event);
        }
        return sb.toString();
    }
    
    public static class Builder {
        List<String> samples = new ArrayList<String>();
        
        public NameGenerator build() {
            if (samples.size() == 0) {
                throw new IllegalStateException("No samples available");
            }
            return new NameGenerator(calculateTransitionsMap());
        }
        
        public void addSample(String sample){
            if (sample == null || sample.length()==0){
                throw new IllegalArgumentException();
            }
            samples.add(sample.toLowerCase());
        }

        private TransitionMap calculateTransitionsMap() {
            Map<State, Map<Event, Integer>> transitionsCounter = new HashMap<>();
            Map<State, Integer> totalsForSource = new HashMap<>();
            State state = new State(Event.START, Event.START);
            for (String sample : samples){
                for (char c : sample.toCharArray()){
                    Event event = Event.character(c);
                    incCounter(transitionsCounter, totalsForSource, state, event);
                    state = state.next(event);
                }
                incCounter(transitionsCounter, totalsForSource, state, Event.END);
            }
            Map<State, List<PossibileTransition>> transitionsMap = new HashMap<>();
            for (State currentState : transitionsCounter.keySet()){
                transitionsMap.put(currentState, new ArrayList<>());
                for (Event target : transitionsCounter.get(currentState).keySet()){
                    float p = transitionsCounter.get(currentState).get(target) / (float)totalsForSource.get(currentState);
                    transitionsMap.get(currentState).add(new PossibileTransition(p, target));
                }
            }
            return new TransitionMap(transitionsMap);
        }
        
        private void incCounter(Map<State, Map<Event, Integer>> transitionsCounter,
                                Map<State, Integer> totalsForSource, State source, Event target){
            if (!transitionsCounter.containsKey(source)){
                transitionsCounter.put(source, new HashMap<Event, Integer>());
                totalsForSource.put(source, 0);
            }
            if (!transitionsCounter.get(source).containsKey(target)){
                transitionsCounter.get(source).put(target, 0);
            }
            transitionsCounter.get(source).put(target, 1 + transitionsCounter.get(source).get(target));
            totalsForSource.put(source, 1 + totalsForSource.get(source));
        }

    } 
    
}
