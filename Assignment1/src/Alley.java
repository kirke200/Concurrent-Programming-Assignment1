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
        try {
            boolean enterFailed;
            do {
                enterFailed = false;
                this.enterOrLeaveAlley.P();
                System.out.println("Took out alley token: Direction:"+carDirection + "Number of cars: "+carsInAlley);
                if (no < 5 && no > 0) {
                    if (carDirection == 0) {
                        carDirection = 1;
                        carsInAlley++;
                    } else if (carDirection == 2) {
                        handInAlleyToken();
                        Thread.sleep((long)Math.random()*1000);
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
                        Thread.sleep((long)Math.random()*1000);
                        enterFailed = true;
                    } else {
                        carsInAlley++;
                    }
                }
            } while(enterFailed);

        } catch (InterruptedException e) {
        }
    }

    public void handInAlleyToken() {
        System.out.println("Handed in alley token");
        this.enterOrLeaveAlley.V();
    }

    public void leave(int no) {
        try {
            System.out.println("Took out alley token");
            this.enterOrLeaveAlley.P();
            if(carsInAlley == 1) {
                carDirection = 0;
            }
            carsInAlley--;

        } catch (InterruptedException e) {
        }

    }

}
