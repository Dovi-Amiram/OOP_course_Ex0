package ex0.algo;

import ex0.Building;
import ex0.CallForElevator;
import ex0.Elevator;

public class DestinationDispatchElevatorSystem implements ElevatorAlgo{

    private Building building;
    private ElevatorScheduler[] system;

    public DestinationDispatchElevatorSystem(Building b){
        building = b;
        system = new ElevatorScheduler[building.numberOfElevetors()];
        for (int i = 0; i < system.length; i++)
            system[i] = new ElevatorScheduler(building.getElevetor(i));
    }

    @Override
    public Building getBuilding() {
        return this.building;
    }

    @Override
    public String algoName() {
        return "Online Destination-dispatch Elevator System algorithm";
    }

    @Override
    public int allocateAnElevator(CallForElevator call) {
        int result = 0;
        double minimum_average_time_added = Double.MAX_VALUE;
        for (int i = 0; i < system.length; i++) {
            double original_local_average = system[i].averageCallCompletionTime();
            ElevatorScheduler copy = new ElevatorScheduler(system[i]);
            copy.allocated_calls.add(call);
            copy.updateSchedule(call);
            double new_local_average = copy.averageCallCompletionTime();
            double addition_to_average_time = Math.abs(new_local_average - original_local_average);
            if (addition_to_average_time < minimum_average_time_added){
                minimum_average_time_added = addition_to_average_time;
                result = i;
            }
        }

        system[result].updateSchedule(call);
        system[result].allocated_calls.add(call);
        return result;
    }

    @Override
    public void cmdElevator(int elevatorID) {

        ElevatorScheduler schedule = system[elevatorID];
        Elevator elevator = building.getElevetor(elevatorID);

        schedule.updateAllocatedCalls();

        if (!schedule.isEmpty()) {
            // elevator is level
            if (elevator.getState() == Elevator.LEVEL) {
                elevator.goTo(schedule.getFirst());
                schedule.removeFirst();
            }

            // elevator is moving
            else if (elevator.getState() == Elevator.UP || elevator.getState() == Elevator.DOWN)
                elevator.stop(schedule.getFirst());

                //error
            else{
                schedule.updateAllocatedCalls();
                return;
            }
        }
        schedule.updateAllocatedCalls();
        return;
    }
}