import java.util.ArrayList;
import java.util.Arrays;

public class Barrier {

    static boolean barrierOn = false;
    static Semaphore[] barrierSemaphores = new Semaphore[9];
    ArrayList<Pos> barrierEntrance = new ArrayList<>(Arrays.asList(new Pos(4,3), new Pos(4,4), new Pos(4,5), new Pos(4,6), new Pos(4,7)
            , new Pos(5,8), new Pos(5,9), new Pos(5,10), new Pos(5,11)));

    static Semaphore thresholdSemaphore = new Semaphore(1);
    int threshold = 9;
    //int ncars = 9;
    int noOfCarsWaiting = 0;
    int[] carsAtBarrier = new int[9];
    static Semaphore anyCarsAtBarrier = new Semaphore(0);
    boolean increaseThreshold = false;
    int tempThreshold, waitingToWait;
    static Semaphore tempSemaphore = new Semaphore(0);
    boolean notFirstCar = false;
    static Semaphore nextCar = new Semaphore(0);
    static Semaphore firstCarSemaphore = new Semaphore(1);



    public Barrier(){

    }

    public void sync(int no){
        takeEnterOrLeaveToken();
        noOfCarsWaiting++;
        System.out.println("Car entered " + no);
        carsAtBarrier[no] = 1;
        handInEnterOrLeaveToken();

        // Add a semaphore to every car's barrier semaphore except its own.
        for (int j = 0; j < 9 ; j++){
            if (j != no){
                barrierSemaphores[j].V();
            }
        }
        try {
        // Waits until it can pick up all 8 semaphores before allowed passing the barrier.
        for (int i = 0; i < threshold-1 ; i++){
            barrierSemaphores[no].P();
            //System.out.println("Car number " + no + " picked up " + i );
        }
        } catch (InterruptedException e) {
        }
        takeFirstCarToken();
        if (notFirstCar) {
            handInFirstCarToken();
            try {
                nextCar.P();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } else {
            notFirstCar = true;
            handInFirstCarToken();
            takeEnterOrLeaveToken();
        }

        noOfCarsWaiting--;
        if(increaseThreshold && noOfCarsWaiting == 0){
            threshold = tempThreshold;
            increaseThreshold = false;
            tempSemaphore.V();

        }
        System.out.println("Cars waiting: " + noOfCarsWaiting);

        // If last car to leave, then reset semaphores for all cars (active or inactive)
//        if (noOfCarsWaiting==0) {
            for (int i = 0; i < carsAtBarrier.length; i++) {
                //if (carsAtBarrier[i] == 0){
                if (carsAtBarrier[i] == 0 || i == no)
                    barrierSemaphores[i] = new Semaphore(0);
                //}
            }
   //     }
        carsAtBarrier[no]=0;

        // last car to leave the barrier changes the threshold
       /* if(increaseThreshold && noOfCarsWaiting==0){
            threshold = tempThreshold;
            increaseThreshold = false;
            tempSemaphore.V();
        }*/
       if (noOfCarsWaiting == 0) {
           notFirstCar = false;
           handInEnterOrLeaveToken();
       } else {
           nextCar.V();
       }



    }

    public void on() {
        // Resets the semaphores to 0 for every index
        // ER MÅSKE DUMT AT LAVE NYE SEMAPHORE OBJECTER? MEN ER DET BEDRE AT RESETTE DEM MED .P()?
        if (!barrierOn) {
            for (int i = 0; i < barrierSemaphores.length; i++) {
                barrierSemaphores[i] = new Semaphore(0);
            }

            barrierOn = true;

        }
    }

    public void off(){
        if(barrierOn) {
            System.out.println("BARRIER OFF");
            barrierOn = false;
            // Puts 9 semaphores into every index of the array to allow waiting cars to proceed.
            for (int j = 0; j < 9; j++) {
                for (int i = 0; i < 9; i++) {
                    barrierSemaphores[j].V();
                }
            }
            System.out.println(String.valueOf(barrierOn));
            //for ( sem in )
        }
    }
    public void isPosBarrierEntrance(Pos pos, int no){
        if (barrierEntrance.contains(pos) && barrierOn){
            sync(no);


        }
    }

    public void takeEnterOrLeaveToken() {
        try {
            thresholdSemaphore.P();
        } catch (InterruptedException e) {
        }
        System.out.println("Took Token");
    }

    public void handInEnterOrLeaveToken() {
            thresholdSemaphore.V();
            System.out.println("Handed in token");

    }

    public void takeFirstCarToken() {
        try {
            firstCarSemaphore.P();
        } catch (InterruptedException e) {
        }
        System.out.println("Took Token");
    }

    public void handInFirstCarToken() {
        firstCarSemaphore.V();
        System.out.println("Handed in token");

    }



    public void barrierThreshold(int n) {
        takeEnterOrLeaveToken();
        // Change immdiately if no cars are waiting.
        if (noOfCarsWaiting==0) {
            threshold = n;
            for (int i = 0; i < carsAtBarrier.length; i++){
                barrierSemaphores[i] = new Semaphore(0);
            }
            handInEnterOrLeaveToken();
        }

        else if(n==threshold){
            // Do nothing
            handInEnterOrLeaveToken();
        }

        else if (n < threshold){
            System.out.println("Decrease threshold");
            threshold = n;
            if (noOfCarsWaiting>=threshold){
                // Add a semaphore to "activate" the cars
                for (int i = 0; i < carsAtBarrier.length; i++){
                    if (carsAtBarrier[i] == 1)
                        barrierSemaphores[i].V();
                    // Reset the semaphore count to 0 for those not waiting at barrier.
                    /*if (carsAtBarrier[i] == 0){
                        barrierSemaphores[i] = new Semaphore(0);
                    }*/
                }
                /*for (int i = 0; i < carsAtBarrier.length; i++){
                    barrierSemaphores[i] = new Semaphore(0);
                }*/

            }

            handInEnterOrLeaveToken();
        }

        else{

           /* if (noOfCarsWaiting == 0) {
                threshold = n;
                handInEnterOrLeaveToken();
            }
            else {*/
                tempThreshold = n;
                increaseThreshold = true;
                handInEnterOrLeaveToken();
                try {
                    tempSemaphore.P();
                } catch (InterruptedException e) {
                }


            //}
            /*if(noOfCarsWaiting!=0){
                handInEnterOrLeaveToken();
            }
            else if (noOfCarsWaiting == 0) {
                threshold=n;
                handInEnterOrLeaveToken();

            }*/

        }

    }
}
