public class RemovingCars {



    public synchronized void removeCar(int no, CarControl carControl) {
        if (!carControl.conductor[no].removed && !carControl.conductor[no].restorationUnderway) {
            carControl.conductor[no].removed = true;

                if (carControl.conductor[no].inCriticalRegion) {
                    carControl.alley.leave(no);
                }

                carControl.conductor[no].interrupt();
                carControl.cd.deregister(carControl.conductor[no].getCar());
                carControl.cd.println("Removed car "+no);

        } else {
            carControl.cd.println("Car "+ no + " is already removed");
        }



    }
    public synchronized void restoreCarFirstStep(int no, CarControl carControl) {
        if (carControl.conductor[no].removed && !carControl.conductor[no].restorationUnderway) {
            carControl.conductor[no].restorationUnderway = true;
            (new RestoreCar(no,carControl)).start();
        } else {
            carControl.conductor[no].cd.println("Car " + no + " is not removed.");
        }

    }

    private synchronized void restoreCarSecondStep(int no, CarControl carControl) {
        carControl.cd.println("Second step");
        carControl.conductor[no].setCar(carControl.cd.newCar(no, carControl.conductor[no].col, carControl.conductor[no].curpos));
        carControl.cd.register(carControl.conductor[no].getCar());
        carControl.conductor[no].removed = false;
        carControl.conductor[no].restorationUnderway = false;
        notifyAll();

    }

    public synchronized void waitForReactivation(Conductor conductor) {
        conductor.sleeping = true;
        while (conductor.removed) {
            notifyAll();
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        conductor.sleeping = false;
    }

    public class RestoreCar extends Thread {

        int no;
        CarControl carControl;

        public RestoreCar(int number, CarControl carControl2) {
            no = number;
            carControl = carControl2;
        }

        public synchronized void run() {

                try {
                    while (!carControl.conductor[no].sleeping) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                        }
                    }
                    if (carControl.conductor[no].inCriticalRegion) {
                        carControl.conductor[no]._alley.enter(no,carControl.conductor[no]);
                    }
                    carControl.conductor[no].getSemaphoreTokenFromPos(carControl.conductor[no].curpos);

                    restoreCarSecondStep(no,carControl);


                } catch (InterruptedException e) {
                    carControl.conductor[no].cd.println("Exception in Car no. " + no + " when trying to restore");
                    System.err.println("Exception in Car no. " + no + " when trying to restore :" + e);
                }



            //carControl.conductor[no].curpos = carControl.conductor[no].newpos;


            //carControl.cd.register(carControl.conductor[no].getCar());


        }

    }

}
