import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Alley {
    int carDirection;
    int carsInAlley;
    int carsWaiting1;
    int carsWaiting2;
    static ArrayList<Pos> criticalRegionEntrances = new ArrayList<>(Arrays.asList(new Pos(1,0), new Pos(8,0), new Pos(9,1), new Pos(9,2)));
    static ArrayList<Pos> criticalRegionExits = new ArrayList<>(Arrays.asList(new Pos(1,1), new Pos(10,2)));

    public Alley() {
        carsInAlley = 0;
        carDirection = 0;
    }

    public synchronized void enter(int no, Conductor cond ) throws InterruptedException {

        if(no < 5 && no > 0) {
            if (carDirection == 0) {
                carDirection = 1;
                carsInAlley++;
            } else if (carDirection == 2) {
                carsWaiting1++;
                while (carDirection == 2) try {
                    wait();
                } catch (InterruptedException e) {
                    if (cond.removed && !cond.newStart) {
                        System.out.println("Removed car in front of alley");
                        carsWaiting1--;
                        throw new InterruptedException();
                    }

                }
                carsWaiting1--;
            } else {
                carsInAlley++;
            }

        }else if (no > 4) {

            if (carDirection == 0) {
                carDirection = 2;
                carsInAlley++;
            } else if (carDirection == 1) {
                carsWaiting2++;
                while (carDirection == 1) try {
                    wait();
                } catch (InterruptedException e) {
                    if (cond.removed && !cond.newStart) {
                        System.out.println("Removed car in front of alley");
                        carsWaiting2--;
                        throw new InterruptedException();
                    }
                }
                carsWaiting2--;
            } else {
                carsInAlley++;
            }



        }

    }

    public synchronized void leave(int no) {
        if(carsInAlley == 1) {
            System.out.println("Car leaving");
            carDirection = 0;
            carsInAlley = carsInAlley+carsWaiting1+carsWaiting2;
            if (carsWaiting2 != 0) {
                carDirection = 2;
            } else if (carsWaiting1 != 0) {
                carDirection = 1;
            }
            notifyAll();
        }
        carsInAlley--;

    }

    public void enterAlleyIfInFront(Conductor cond) throws InterruptedException {
        if(!cond.inCriticalRegion && criticalRegionEntrances.contains(cond.newpos) && !criticalRegionEntrances.contains(cond.curpos)) {
            enter(cond.no, cond);
            cond.inCriticalRegion = true;
        }
    }

    public void leaveAlleyIfExit(Conductor cond) {
        if(cond.inCriticalRegion && criticalRegionExits.contains(cond.newpos) && criticalRegionEntrances.contains(cond.curpos)) {
            leave(cond.no);
            cond.inCriticalRegion = false;
        }
    }

}
