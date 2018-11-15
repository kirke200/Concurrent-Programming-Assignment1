public class RemovingCars {

    public static void removeCar(int no, CarControl carControl) {
        if (carControl.conductor[no].inCriticalRegion) {
            carControl.alley.leave(no);
        }
        carControl.conductor[no].interrupt();
        carControl.cd.deregister(carControl.conductor[no].getCar());
    }


    public static void restoreCar(int no, CarControl carControl) {
        (new RestoreCar(no, carControl)).start();

        //carControl.conductor[no].curpos = carControl.conductor[no].startpos;
        /*if (carControl.conductor[no].inCriticalRegion) {
            carControl.conductor[no]._alley.enter(no);
        }

        try {
            System.out.println("Inserting car "+no+" at position "+carControl.conductor[no].curpos);
            carControl.semaphores[carControl.conductor[no].newpos.row][carControl.conductor[no].newpos.col].P();
        } catch (InterruptedException e) {
            carControl.conductor[no].cd.println("Exception in Car no. " + no + " when trying to restore");
            System.err.println("Exception in Car no. " + no + " when trying to restore :" + e);
        }

        carControl.conductor[no].curpos = carControl.conductor[no].newpos;
        carControl.conductor[no].stopped.V();

        carControl.cd.register(carControl.conductor[no].getCar());*/

    }

    public static class RestoreCar extends Thread {

        int no;
        CarControl carControl;

        public RestoreCar(int number, CarControl carControl2) {
            no = number;
            carControl = carControl2;
        }

        public void run() {

            if (carControl.conductor[no].inCriticalRegion) {
                carControl.conductor[no]._alley.enter(no);
            }

            try {
                System.out.println("Inserting car "+no+" at position "+carControl.conductor[no].newpos);
                carControl.semaphores[carControl.conductor[no].curpos.row][carControl.conductor[no].curpos.col].P();
            } catch (InterruptedException e) {
                carControl.conductor[no].cd.println("Exception in Car no. " + no + " when trying to restore");
                System.err.println("Exception in Car no. " + no + " when trying to restore :" + e);
            }

            //carControl.conductor[no].curpos = carControl.conductor[no].newpos;
            carControl.conductor[no].stopped.V();

            carControl.cd.register(carControl.conductor[no].getCar());


        }

    }

}
