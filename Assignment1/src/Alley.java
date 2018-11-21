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
    Boolean[] carsGoneThrough = {false, false, false, false, false, false, false, false};


    public Alley() {
        carsInAlley = 0;
        carDirection = 0;
    }

    public synchronized void enter(int no) {

        if(no < 5 && no > 0) {
            if (carDirection == 0 && !checkIfCarsThrough(no)) {
                carDirection = 1;
                carsInAlley++;
            } else if (carDirection == 2 || checkIfCarsThrough(no)) {
                carsWaiting1++;
                while (carDirection == 2 || checkIfCarsThrough(no)) try {wait();} catch (InterruptedException e) {}
                carsWaiting1--;
            } else {
                carsInAlley++;
            }
            carsGoneThrough[no-1] = true;
        }else if (no > 4) {

            if (carDirection == 0 && !checkIfCarsThrough(no)) {
                carDirection = 2;
                carsInAlley++;
            } else if (carDirection == 1 || checkIfCarsThrough(no)) {
                carsWaiting2++;
                while (carDirection == 1 || checkIfCarsThrough(no)) try {wait();} catch (InterruptedException e) {}
                carsWaiting2--;
            } else {
                carsInAlley++;
            }
            carsGoneThrough[no-1] = true;



        }

    }

    public boolean checkIfCarsThrough(int no) {
        if (no < 5) {
            return carsGoneThrough[0] && carsGoneThrough[1] && carsGoneThrough[2] && carsGoneThrough[3];
        } else {
            return carsGoneThrough[4] && carsGoneThrough[5] && carsGoneThrough[6] && carsGoneThrough[7];
        }
    }

    public boolean otherDirectionThrough(int no) {
        if (!(no < 5)) {
            return carsGoneThrough[0] && carsGoneThrough[1] && carsGoneThrough[2] && carsGoneThrough[3];
        } else {
            return carsGoneThrough[4] && carsGoneThrough[5] && carsGoneThrough[6] && carsGoneThrough[7];
        }
    }

    public synchronized void leave(int no) {

        if(carsInAlley == 1 && checkIfCarsThrough(no)) {
            if (no < 5) {
                carDirection = 2;
            } else {
                carDirection = 1;
            }

            if (checkIfCarsThrough(1) && checkIfCarsThrough(5)) {
                for (int i = 0; i < carsGoneThrough.length; i++) {
                    carsGoneThrough[i] = false;
                }
            }
            if (no < 5) {
                carsInAlley = carsInAlley+carsWaiting2;
            } else {
                carsInAlley = carsInAlley+carsWaiting1;
            }
            notifyAll();
        }
        carsInAlley--;
    }

    public void enterAlleyIfInFront(Conductor cond) {
        if(!cond.inCriticalRegion && criticalRegionEntrances.contains(cond.newpos) && !criticalRegionEntrances.contains(cond.curpos)) {
            enter(cond.no);
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
