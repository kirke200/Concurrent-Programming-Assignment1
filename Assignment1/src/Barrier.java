public class Barrier {

    static boolean barrierOn = false;
    static Semaphore[] barrierSemaphores;

    public Barrier(){
        this.barrierSemaphores = new Semaphore[9];
        for (int i = 0; i < barrierSemaphores.length; i++){
            barrierSemaphores[i] = new Semaphore(0);
        }


    }

    public static void sync(int no){
        // Add a semaphore to every car's barrier semaphore except its own.
        for (int j = 0; j < 9 ; j++){
            if (j != no){
                barrierSemaphores[j].V();
                System.out.println("sync entered");
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

    public static void on(){
        barrierOn = true;

    }

    public static void off(){
        barrierOn = false;
        //for ( sem in )


    }
}
