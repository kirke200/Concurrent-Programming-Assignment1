import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Alley {

    static Semaphore enterOrLeaveAlley = new Semaphore(1);
    static Semaphore waitingDirection1= new Semaphore(0);
    static Semaphore waitingDirection2= new Semaphore(0);
    //carDirection 0 = no direction, 1 = clockwise, 2 = counterclockwise
    static int carDirection = 0;
    static int carsInAlley = 0;
    static int carsWaiting1 = 0;
    static int carsWaiting2 = 0;
    static ArrayList<Pos> criticalRegionEntrances = new ArrayList<>(Arrays.asList(new Pos(1,0), new Pos(8,0), new Pos(9,1), new Pos(9,2)));
    static ArrayList<Pos> criticalRegionExits = new ArrayList<>(Arrays.asList(new Pos(1,1), new Pos(10,2)));



    public static void enter(int no) {
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

    public static void enterAlleyIfInFront(Conductor cond) {
        if(!cond.inCriticalRegion && criticalRegionEntrances.contains(cond.newpos) && !criticalRegionEntrances.contains(cond.curpos)) {
            Alley.enter(cond.no);
            cond.inCriticalRegion = true;
        }
    }

    public static void leaveAlleyIfExit(Conductor cond) {
        if(cond.inCriticalRegion && criticalRegionExits.contains(cond.newpos) && criticalRegionEntrances.contains(cond.curpos)) {
            Alley.leave(cond.no);
            cond.inCriticalRegion = false;
        }
    }

    public static void takeOutDirectionToken(int carDirection) {
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

    public static void handInDirectionToken(int carDirection) {
            if (carDirection == 1) {
                waitingDirection1.V();
            } else {
                waitingDirection2.V();
            }
            


    }

    public static void takeOutAlleyToken() {
        try {
            enterOrLeaveAlley.P();
        } catch (InterruptedException e) {
        }
        //System.out.println("Took out alley token");
    }


    public static void handInAlleyToken() {
        //System.out.println("Handed in alley token");
        enterOrLeaveAlley.V();
    }

    public static void leave(int no) {
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

    }

}
