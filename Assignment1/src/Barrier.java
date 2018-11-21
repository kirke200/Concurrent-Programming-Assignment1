import java.util.ArrayList;
import java.util.Arrays;

// Monitor solution
public class Barrier {

    static boolean barrierOn = false;
    ArrayList<Pos> barrierEntrance = new ArrayList<>(Arrays.asList(new Pos(4,3), new Pos(4,4), new Pos(4,5), new Pos(4,6), new Pos(4,7)
            , new Pos(5,8), new Pos(5,9), new Pos(5,10), new Pos(5,11)));

    int K;
    boolean OK = false; // Flag to avoid spurious wakeups.
    int ncars = 9;
    boolean increaseThreshold = false;

    public Barrier(){
        K = 0;

    }
    // with inspiration from barrier monitor in  Synchronization mechanisms.
    public synchronized void sync() {
        while (OK && barrierOn)
            try {wait();} catch (InterruptedException e) {}
        K++;
        if (K >= ncars && barrierOn) { // If more or same amount of cars are waiting as the threshold level
            OK = true;
            notifyAll();
        }
        while (!OK && barrierOn)
            try {wait();} catch (InterruptedException e) {}
        K--;
        if (K == 0 && barrierOn) {
            // if increase in threshold is awaiting, stop new cars from coming in after release
            // to ensure it is changed before the next round of cars are allowed in.
            if(increaseThreshold){
                increaseThreshold = false;
                notifyAll();
            }
            OK = false;
            notifyAll();
        }
    }
    // Turns on barrier
    public synchronized void on(){
        if(!barrierOn){
            OK = false;
            barrierOn = true;
        }

    }
    // Turns off barrier
    public synchronized void off(){
        if(barrierOn) {
            barrierOn = false;
            OK = true;
            notifyAll();
        }
    }
    // Checks if a car should stop at the barrier.
    public synchronized void isPosBarrierEntrance(Pos pos){
        if (barrierEntrance.contains(pos) && barrierOn==true){
            sync();

        }
    }

    public synchronized void barrierThreshold(int n){
        if (n <= ncars) { // change immediately when threshold isn't increased.
            ncars = n;
            if (K>=ncars) { // allow cars waiting to be released in case the threshold is changed to equal or less than the amount of cars waiting
                OK = false;
                sync();
            }
        }
        else { // if K>ncars
            increaseThreshold = true;
            while (increaseThreshold) { // blocks the barrierThreshold call until all cars have been released before updating the INCREASED threshold.
                try {
                    wait();
                } catch (InterruptedException e) {}
            }
            ncars = n;
        }
    }

}