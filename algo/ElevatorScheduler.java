package ex0.algo;

import ex0.CallForElevator;
import ex0.Elevator;

import java.util.ArrayList;
import java.util.LinkedList;

public class ElevatorScheduler extends LinkedList<Integer> {

    protected Elevator elevator;
    protected ArrayList<CallForElevator> allocated_calls;


    public ElevatorScheduler(Elevator elevator) {
        this.elevator = elevator;
        allocated_calls = new ArrayList<>();
    }

    public ElevatorScheduler(ElevatorScheduler other) {
        this.elevator = other.elevator;
        this.addAll(other);

        allocated_calls = new ArrayList<>(other.allocated_calls);
    }

    public Elevator getElevator() {
        return elevator;
    }

    public ArrayList<CallForElevator> getAllocatedCalls() {
        return allocated_calls;
    }

    private double stopCycleTime() {
        return elevator.getStopTime() + elevator.getTimeForOpen() + elevator.getTimeForClose() + elevator.getTimeForOpen();
    }


    private int numberOfStops(int start_floor_index, int end_floor_index) {
        if (start_floor_index > end_floor_index || end_floor_index >= this.size()) return 0;
        return end_floor_index - start_floor_index;
    }


    private int travelDistance(int start_floor_index, int end_floor_index) {
        if (start_floor_index > end_floor_index || end_floor_index >= size()) return 0;
        int distance = 0;
        for (; start_floor_index < end_floor_index; start_floor_index++)
            distance += Math.abs(get(start_floor_index) - get(start_floor_index + 1));
        return distance;
    }

    private double travelTime(int start_pos, int end_floor_index) {
        if (!isEmpty() && end_floor_index < size()) {
            return (Math.abs(getFirst() - start_pos) //distance to first floor in queue
                    + (travelDistance(0, end_floor_index))) / elevator.getSpeed() // + travel-distance from first in queue to given floor
                    + (stopCycleTime() * numberOfStops(0, end_floor_index));
        }
        return 0;
    }

    private double timeToCompletion(CallForElevator call) {
//        if (!(contains(call.getSrc()) && contains(call.getDest())))
//            throw new IllegalArgumentException("call source and destination must already be assigned to the queue");
        return travelTime(call.getSrc(), indexOf(call.getDest()));
    }

    protected double averageCallCompletionTime() {
        int sum = 0;
        for (CallForElevator call : allocated_calls)
            sum += timeToCompletion(call);
        return (!allocated_calls.isEmpty()) ? sum / allocated_calls.size() : 0;
    }

    protected int direction() {
        if (elevator.getState() == Elevator.LEVEL && !isEmpty()) {
            int signed = (getFirst() - elevator.getPos());
            return (signed != 0) ? (signed / Math.abs(signed)) : 0;
        } else return elevator.getState();
    }

    private boolean ability(CallForElevator call) {
        if (call.getType() == CallForElevator.UP) {
            return call.getSrc() > elevator.getPos();
        } else
            return call.getSrc() < elevator.getPos();
    }

    private void dest(CallForElevator call) {
        int destination = call.getDest();
        if (contains(destination)) return;
        int i = 0;
        while (i < size() && destination > get(i))
            i++;
        add(i, destination);
    }

    private void src(CallForElevator call) {
        int source = call.getSrc();
        if (contains(source)) return;
        int i = 0;
        while (i < size() && source > get(i))
            i++;
        add(i, source);
    }

    protected void updateAllocatedCalls() {
        for (int i = 0; i < allocated_calls.size(); i++) {
            CallForElevator current = allocated_calls.get(i);

            if (current.getState() == CallForElevator.GOIND2DEST)
                removeFirstOccurrence(current.getSrc());

            else if (current.getState() == CallForElevator.DONE) {
                removeFirstOccurrence(current.getSrc());
                removeFirstOccurrence(current.getDest());
                allocated_calls.remove(current);
            } else return;
        }
    }

    protected void updateSchedule(CallForElevator call) {
        int source = call.getSrc();
        int destination = call.getDest();
        if (direction() == call.getType()) {
            if (ability(call)) {
                if (contains(source) && contains(destination)) return;
                dest(call);
                src(call);
                return;
            }
        }
        addLast(source);
        addLast(destination);
    }
}
