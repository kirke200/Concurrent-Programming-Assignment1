import java.util.Random;

public class Alley {

    static Semaphore enterOrLeaveAlley;
    //carDirection 0 = no direction, 1 = clockwise, 2 = counterclockwise
    int carDirection;
    int carsInAlley;


    public Alley(Semaphore semaphore) {
        this.enterOrLeaveAlley = semaphore;
        carsInAlley = 0;
        carDirection = 0;
    }


    public void enter(int no) {
            boolean enterFailed;
            do {
                enterFailed = false;
                takeOutAlleyToken();
                if (no < 5 && no > 0) {
                    if (carDirection == 0) {
                        carDirection = 1;
                        carsInAlley++;
                    } else if (carDirection == 2) {
                        handInAlleyToken();
                        try {
                            Thread.sleep((long)Math.random()*1000);
                        } catch (InterruptedException e) {
                            System.out.print("Sleep failed");
                        }
                        enterFailed = true;
                    } else {
                        carsInAlley++;
                    }
                } else if (no > 4) {
                    if (carDirection == 0) {
                        carDirection = 2;
                        carsInAlley++;
                    } else if (carDirection == 1) {
                        handInAlleyToken();
                        try {
                            Thread.sleep((long)Math.random()*1000);
                        } catch (InterruptedException e) {
                            System.out.print("Sleep failed");
                        }
                        enterFailed = true;
                    } else {
                        carsInAlley++;
                    }
                }
            } while(enterFailed);
            handInAlleyToken();

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

    public void leave() {

            takeOutAlleyToken();
            if(carsInAlley == 1) {
                carDirection = 0;
            }
            carsInAlley--;
            handInAlleyToken();

    }

}
