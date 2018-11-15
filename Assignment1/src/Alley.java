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
            if (carDirection == 0) {
                carDirection = 1;
                carsInAlley++;
            } else if (carDirection == 2) {
                carsWaiting1++;
                while (carDirection == 2) try {wait();} catch (InterruptedException e) {}
                carsWaiting1--;
            } else {
                carsInAlley++;
            }
            carsGoneThrough[no-1] = true;
        }else if (no > 4) {

            if (carDirection == 0) {
                carDirection = 2;
                carsInAlley++;
            } else if (carDirection == 1) {
                carsWaiting2++;
                while (carDirection == 1) try {wait();} catch (InterruptedException e) {}
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
        System.out.println(Arrays.toString(carsGoneThrough));
        System.out.println(checkIfCarsThrough(no));
        if(carsInAlley == 1 && checkIfCarsThrough(no)) {
            if (otherDirectionThrough(no)) {
                carDirection = 0;
            } else {
                if (no < 5) {
                    carDirection = 2;
                } else {
                    carDirection = 1;
                }
            }
            System.out.println("Notified all");
            notifyAll();
            carsInAlley = carsInAlley+carsWaiting1+carsWaiting2;

            /*if (carsWaiting2 != 0) {
                carDirection = 2;
            } else if (carsWaiting1 != 0) {
                carDirection = 1;
            }*/

        }
        if (checkIfCarsThrough(1) && checkIfCarsThrough(5)) {
            for (int i = 0; i < carsGoneThrough.length; i++) {
                carsGoneThrough[i] = false;
            }
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

    /*static Semaphore enterOrLeaveAlley;
    static Semaphore waitingDirection1;
    static Semaphore waitingDirection2;
    //carDirection 0 = no direction, 1 = clockwise, 2 = counterclockwise
    int carDirection;
    int carsInAlley;
    int carsWaiting1;
    int carsWaiting2;


    public Alley(Semaphore semaphore) {
        this.enterOrLeaveAlley = semaphore;
        carsInAlley = 0;
        carDirection = 0;
        waitingDirection2 = new Semaphore(0);
        waitingDirection1 = new Semaphore(0);
    }


    public void enter(int no) {
        takeOutAlleyToken();
        if (no < 5 && no > 0) {
            if (carDirection == 0) {
                carDirection = 1;
                carsInAlley++;
                handInAlleyToken();
            } else if (carDirection == 2) {
                carsWaiting1++;
                handInAlleyToken();
                takeOutDirectionToken(1);
            } else {
                carsInAlley++;
                handInAlleyToken();
            }
        } else if (no > 4) {
            if (carDirection == 0) {
                carDirection = 2;
                carsInAlley++;
                handInAlleyToken();
            } else if (carDirection == 1) {
                carsWaiting2++;
                handInAlleyToken();
                takeOutDirectionToken(2);
            } else {
                carsInAlley++;
                handInAlleyToken();
            }
        }


    }

    public void takeOutDirectionToken(int carDirection) {
        try {
            if (carDirection == 1) {
                waitingDirection1.P();
            } else {
                waitingDirection2.P();
            }

        } catch (InterruptedException e) {
            System.out.println("Failed to take out token");
        }

    }

    public void handInDirectionToken(int carDirection) {
            if (carDirection == 1) {
                waitingDirection1.V();
            } else {
                waitingDirection2.V();
            }
            


    }

    public void takeOutAlleyToken() {
        try {
            enterOrLeaveAlley.P();
        } catch (InterruptedException e) {
        }
        //System.out.println("Took out alley token");
    }

    public void handInAlleyToken() {
        //System.out.println("Handed in alley token");
        enterOrLeaveAlley.V();
    }

    public void leave(int no) {

        takeOutAlleyToken();
        if(carsInAlley == 1) {
            carDirection = 0;
            if (carsWaiting2 != 0) {
                for (int i = 0; i < carsWaiting2; i++) {
                    handInDirectionToken(2);
                }
                carDirection = 2;
                carsInAlley = carsInAlley+carsWaiting2;
                carsWaiting2 = 0;

            }
            if (carsWaiting1 != 0) {
                for (int i = 0; i < carsWaiting1; i++) {
                    handInDirectionToken(1);
                }
                carsInAlley = carsInAlley+carsWaiting1;
                carDirection = 1;
                carsWaiting1 = 0;
            }
        }
        carsInAlley--;
        handInAlleyToken();

    } */

}
