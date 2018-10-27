public class Barrier {

    static boolean barrierOn = false;
    static Semaphore barrierSemaphores[];

    public Barrier(){


    }

    public static void sync(int no){
        // Add a semaphore to every car's barrier semaphore except its own.
        for (int j = 0; j < 10 ; j++){
            if (j != no){
                barrierSemaphores[j].V();
                System.out.println(barrierSemaphores[j]);
            }
        }
        try {
        // Waits until it can pick up all 8 semaphores before allowed passing the barrier.
        for (int i = 0; i < 9 ; i++){
            barrierSemaphores[no].P();
        }

        } catch (InterruptedException e) {
        }

    }

    public static void on(){
        barrierOn = true;

    }

    public static void off(){
        barrierOn = false;
        //for ( sem in )


    }
}
