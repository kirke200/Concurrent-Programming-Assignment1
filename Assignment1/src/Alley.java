import java.util.Random;

public class Alley {
    int carDirection;
    int carsInAlley;
    int carsWaiting1;
    int carsWaiting2;

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
            } else {
                carsInAlley++;
            }

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



        }

    }

    public synchronized void leave(int no) {

        if(carsInAlley == 1) {
            carDirection = 0;
            carsInAlley = carsInAlley+carsWaiting1;
            if (carsWaiting2 != 0) {
                carDirection = 2;
            } else if (carsWaiting1 != 0) {
                carDirection = 1;
            }
            notifyAll();
        }
        carsInAlley--;

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
