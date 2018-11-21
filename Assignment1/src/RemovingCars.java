public class RemovingCars {

    public CarControl carControl;

    public RemovingCars(CarControl carControl) {
        this.carControl = carControl;
    }



    public synchronized void removeCar(int no) {
        if (!carControl.conductor[no].removed && !carControl.conductor[no].newStart) {
            carControl.conductor[no].removed = true;
            carControl.conductor[no].interrupt();

            carControl.cd.deregister(carControl.conductor[no].getCar());
            carControl.cd.println("Removed car "+no);

        } else {
            carControl.cd.println("Car "+ no + " is already removed");
        }

    }

    public synchronized void restoreCar(int no) {
        if (carControl.conductor[no].removed) {
            Pos newstart = carControl.conductor[no].curpos;
            boolean inCrit = carControl.conductor[no].inCriticalRegion;
            carControl.conductor[no] = new Conductor(no,carControl.cd,carControl.gate[no], carControl.semaphores, carControl.criticalRegion, carControl.alley, carControl.barrier, this);
            carControl.conductor[no].startpos = newstart;
            carControl.conductor[no].newStart = true;
            carControl.conductor[no].inCriticalRegion = inCrit;
            carControl.conductor[no].start();
            carControl.conductor[no].removed = false;
            notifyAll();
        } else {
            carControl.conductor[no].cd.println("Car " + no + " is not removed or is in the process of being restored.");
        }

    }


}
