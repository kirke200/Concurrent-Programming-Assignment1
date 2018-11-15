import java.util.ArrayList;
import java.util.Arrays;

// Monitor solution
public class Barrier {

    static boolean barrierOn = false;
    //static Semaphore[] barrierSemaphores = new Semaphore[9];
    ArrayList<Pos> barrierEntrance = new ArrayList<>(Arrays.asList(new Pos(4,3), new Pos(4,4), new Pos(4,5), new Pos(4,6), new Pos(4,7)
            , new Pos(5,8), new Pos(5,9), new Pos(5,10), new Pos(5,11)));

    //monitor
    int K;
    boolean OK = false; // Flag to avoid spurious wakeups.
    int ncars = 9;

    public Barrier(){
        K = 0;

    }
    // with inspiration from barrier monitor in  Synchronization mechanisms.
    public synchronized void sync() {
        while (OK)
            try {wait();} catch (InterruptedException e) {}
        K++;
        if (K >= ncars) { // If all cars are waiting at the barrier. !!!!!! WAS == instead of >=
            OK = true;
            notifyAll();
        }
        while (!OK)
            try {wait();} catch (InterruptedException e) {}
        K--;
        if (K == 0) {
            OK = false;
            notifyAll();
        }
    }

    public synchronized void on(){
        barrierOn = true;
        OK = false;

    }

    public synchronized void off(){
        System.out.println("Barrier off!");
        barrierOn = false;
        OK = true;
        notifyAll();
    }

    public synchronized void isPosBarrierEntrance(Pos pos){
        if (barrierEntrance.contains(pos) && barrierOn==true){
            sync();

        }
    }

    public synchronized void barrierThreshold(int n){
        if (n <= ncars) { // change immediately when threshold isn't increased.
            ncars = n;
            if (K>=ncars) { // allow cars waiting to be released in case the threshold is changed to equal or less than the amount of cars waiting
                OK = true;
                notifyAll();
            }
        }
        else { // if K>ncars
            while (K!=0) { // blocks the barrierThreshold call until all cars have been released before updating the INCREASED threshold.
                try {
                    wait();
                } catch (InterruptedException e) {}
            }
            ncars = n;
        }
    }

}



/*
// Semaphore Solution

import java.util.ArrayList;
        import java.util.Arrays;

public class Barrier {

    static boolean barrierOn = false;
    static Semaphore[] barrierSemaphores = new Semaphore[9];
    ArrayList<Pos> barrierEntrance = new ArrayList<>(Arrays.asList(new Pos(4,3), new Pos(4,4), new Pos(4,5), new Pos(4,6), new Pos(4,7)
            , new Pos(5,8), new Pos(5,9), new Pos(5,10), new Pos(5,11)));

    public Barrier(){

    }

    public void sync(int no){
        // Add a semaphore to every car's barrier semaphore except its own.
        for (int j = 0; j < 9 ; j++){
            if (j != no){
                barrierSemaphores[j].V();
            }
        }
        try {
            // Waits until it can pick up all 8 semaphores before allowed passing the barrier.
            for (int i = 0; i < 8 ; i++){
                barrierSemaphores[no].P();
                System.out.println("Car number " + no + " picked up " + i );
            }

        } catch (InterruptedException e) {
        }

    }

    public void on(){
        // Resets the semaphores to 0 for every index
        // ER MÅSKE DUMT AT LAVE NYE SEMAPHORE OBJECTER? MEN ER DET BEDRE AT RESETTE DEM MED .P()?
        for (int i = 0; i < barrierSemaphores.length; i++){
            barrierSemaphores[i] = new Semaphore(0);
        }

        barrierOn = true;

    }

    public void off(){
        System.out.println("BARRIER OFF");
        barrierOn = false;
        // Puts 9 semaphores into every index of the array to allow waiting cars to proceed.
        for (int j = 0; j < 9 ; j++) {
            for (int i = 0; i < 9; i++) {
                barrierSemaphores[j].V();
            }
        }
        System.out.println(String.valueOf(barrierOn));
        //for ( sem in )


    }
    public void isPosBarrierEntrance(Pos pos, int no){
        if (barrierEntrance.contains(pos) && barrierOn==true){
            sync(no);


        }
    }
}




*/