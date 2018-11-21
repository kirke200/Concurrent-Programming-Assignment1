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
    public synchronized void sync() {
        if(!barrierOn){
            return;
        }
        while (OK && barrierOn)
            try {wait();} catch (InterruptedException e) {}
        K++;
        if (K == ncars && barrierOn) { // If all cars are waiting at the barrier.
            OK = true;
            notifyAll();
        }
        while (!OK && barrierOn)
            try {wait();} catch (InterruptedException e) {}
        K--;
        if (K == 0 && barrierOn) {  // If all cars have left, allow cars waiting to wait for monitor to enter the critical section
            OK = false;
            notifyAll();
        }
    }
    // Turns on barrier
    public synchronized void on(){
        OK = false;
        barrierOn = true;

    }
    // Turns off barrier
    public synchronized void off(){
        barrierOn = false;
        OK = true;
        notifyAll();
    }
    // Checks if a car should stop at the barrier.
    public synchronized void isPosBarrierEntrance(Pos pos){
        if (barrierEntrance.contains(pos) && barrierOn==true){
            sync();


        }
    }
}
