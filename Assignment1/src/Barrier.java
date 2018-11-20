import java.util.ArrayList;
import java.util.Arrays;

public class Barrier {

    static boolean barrierOn = false;
    static Semaphore[] barrierSemaphores = new Semaphore[9];
    ArrayList<Pos> barrierEntrance = new ArrayList<>(Arrays.asList(new Pos(4,3), new Pos(4,4), new Pos(4,5), new Pos(4,6), new Pos(4,7)
            , new Pos(5,8), new Pos(5,9), new Pos(5,10), new Pos(5,11)));

    static Semaphore thresholdSemaphore = new Semaphore(1);
    int threshold = 9;
    int noOfCarsWaiting = 0;
    int[] carsAtBarrier = new int[9];
    boolean increaseThreshold = false;
    int tempThreshold;
    static Semaphore tempSemaphore = new Semaphore(0);
    boolean notFirstCar = false;
    static Semaphore nextCar = new Semaphore(0);
    static Semaphore firstCarSemaphore = new Semaphore(1);



    public Barrier(){

    }

    public void sync(int no){
        // Enter critical section
        takeEnterOrLeaveToken();
        noOfCarsWaiting++;
        carsAtBarrier[no] = 1;
        // Leave critical section
        handInEnterOrLeaveToken();

        // Add a semaphore to every car's barrier semaphore except its own.
        for (int j = 0; j < 9 ; j++){
            if (j != no){
                barrierSemaphores[j].V();
            }
        }
        try {
        // Waits until it can pick up the amount of threshold-1 semaphores before allowed passing the barrier.
        for (int i = 0; i < threshold-1 ; i++){
            barrierSemaphores[no].P();
        }
        } catch (InterruptedException e) {
        }

        takeFirstCarToken();
        // If it's not the first car to leave, wait until allowed to leave the barrier.
        if (notFirstCar) {
            handInFirstCarToken();
            takeNextCarToken();

        } else { // If first car to leave
            notFirstCar = true;
            handInFirstCarToken();
            // Take the EnterOrLeaveToken to ensure that cars arriving at the barrier before the cars have left are
            // forced to wait until they can wait at the barrier
            takeEnterOrLeaveToken();
        }

        noOfCarsWaiting--;
        // Allow for increase in threshold if last car to leave.
        if(increaseThreshold && noOfCarsWaiting == 0){
            threshold = tempThreshold;
            increaseThreshold = false;
            tempSemaphore.V();

        }

        // If last car to leave, then reset semaphores for all cars that was not waiting at the barrier + itself.
            for (int i = 0; i < carsAtBarrier.length; i++) {
                if (carsAtBarrier[i] == 0 || i == no)
                    barrierSemaphores[i] = new Semaphore(0);
            }
        carsAtBarrier[no]=0;

       if (noOfCarsWaiting == 0) { // if last car
           notFirstCar = false;
           // Allow the witheld cars at the barrier to enter the waiting state for barrier.
           handInEnterOrLeaveToken();
       } else { // if not last car.
           handInNextCarToken();
       }



    }
    // Turn barrier on
    public void on() {
        // Resets the semaphores to 0 for every index
        if (!barrierOn) {
            for (int i = 0; i < barrierSemaphores.length; i++) {
                barrierSemaphores[i] = new Semaphore(0);
            }
            barrierOn = true;
        }
    }
    // Turn barrier off
    public void off(){
        if(barrierOn) {
            barrierOn = false;
            // Puts semaphores into every index of the array to allow waiting cars to proceed.
            for (int j = 0; j < 9; j++) {
                for (int i = 0; i < 9; i++) {
                    barrierSemaphores[j].V();
                }
            }
        }
    }
    // Checks if a car is at the barrier and if the barrier is active
    public void isPosBarrierEntrance(Pos pos, int no){
        if (barrierEntrance.contains(pos) && barrierOn){
            sync(no);


        }
    }


    public void barrierThreshold(int n) {
        takeEnterOrLeaveToken();
        // Change immediately if no cars are waiting.
        if (noOfCarsWaiting==0) {
            threshold = n;
            for (int i = 0; i < carsAtBarrier.length; i++){
                barrierSemaphores[i] = new Semaphore(0);
            }
            handInEnterOrLeaveToken();
        }

        else if(n==threshold){
            handInEnterOrLeaveToken();
        }

        else if (n < threshold){
            System.out.println("Decrease threshold");
            threshold = n;
            if (noOfCarsWaiting>=threshold){
                // Add a semaphore to "activate" the cars waiting at the barrier
                for (int i = 0; i < carsAtBarrier.length; i++){
                    if (carsAtBarrier[i] == 1)
                        barrierSemaphores[i].V();
                }
            }
            handInEnterOrLeaveToken();
        }

        else{ // If threshold is to be increased
                tempThreshold = n;
                increaseThreshold = true;
                handInEnterOrLeaveToken();
                try {
                    tempSemaphore.P();
                } catch (InterruptedException e) {
                }
        }
    }

// Methods to enter and leave different critical sections

    public void takeEnterOrLeaveToken() {
        try {
            thresholdSemaphore.P();
        } catch (InterruptedException e) {
        }
    }

    public void handInEnterOrLeaveToken() {
        thresholdSemaphore.V();
    }

    public void takeFirstCarToken() {
        try {
            firstCarSemaphore.P();
        } catch (InterruptedException e) {
        }
    }

    public void handInFirstCarToken() {
        firstCarSemaphore.V();
    }

    public void takeNextCarToken() {
        try {
            nextCar.P();
        } catch (InterruptedException e) {
        }
    }

    public void handInNextCarToken() {
        nextCar.V();
    }
}
