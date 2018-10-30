public class Barrier {

    static boolean barrierOn = false;
    static Semaphore[] barrierSemaphores = new Semaphore[9];

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
}
