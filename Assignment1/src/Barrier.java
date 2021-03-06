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
        if(!barrierOn) {
            // Resets the semaphores to 0 for every index
            for (int i = 0; i < barrierSemaphores.length; i++) {
                barrierSemaphores[i] = new Semaphore(0);
            }
            barrierOn = true;
        }
    }

    public void off(){
        if (barrierOn) {
            barrierOn = false;
            // Puts 9 semaphores into every index of the array to allow waiting cars to proceed.
            for (int j = 0; j < 9; j++) {
                for (int i = 0; i < 9; i++) {
                    barrierSemaphores[j].V();
                }
            }
        }


    }
    public void isPosBarrierEntrance(Pos pos, int no){
        if (barrierEntrance.contains(pos) && barrierOn==true){
            sync(no);


        }
    }
}
