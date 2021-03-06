//Prototype implementation of Car Control
//Mandatory assignment
//Course 02158 Concurrent Programming, DTU, Fall 2018

//Hans Henrik Lovengreen      Oct 8, 2018


import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

class Gate {

	Semaphore g = new Semaphore(0);
	Semaphore e = new Semaphore(1);
	boolean isopen = false;

	public void pass() throws InterruptedException {
		g.P(); 
		g.V();
	}

	public void open() {
		try { e.P(); } catch (InterruptedException e) {}
		if (!isopen) { g.V();  isopen = true; }
		e.V();
	}

	public void close() {
		try { e.P(); } catch (InterruptedException e) {}
		if (isopen) { 
			try { g.P(); } catch (InterruptedException e) {}
			isopen = false;
		}
		e.V();
	}

}

class Conductor extends Thread {

	final static int steps = 10;

	double basespeed = 6.0;          // Tiles per second
	double variation =  50;          // Percentage of base speed

	CarDisplayI cd;                  // GUI part

	int no;                          // Car number
	Pos startpos;                    // Start position (provided by GUI)
	Pos barpos;                      // Barrier position (provided by GUI)
	Color col;                       // Car  color
	Gate mygate;                     // Gate at start position

	Pos curpos;                      // Current position 
	Pos newpos;                      // New position to go to
	Semaphore[][] _semaphores;
	Semaphore _criticalRegion;

	boolean inCriticalRegion = false;
	Barrier _barrier;




	public Conductor(int no, CarDisplayI cd, Gate g, Semaphore[][] semaphores, Semaphore criticalRegion, Barrier barrier) {
		ArrayList<Pos> barrierEntrance = new ArrayList<>(Arrays.asList(new Pos(4,3), new Pos(4,4), new Pos(4,5), new Pos(4,6), new Pos(4,7)
			, new Pos(5,8), new Pos(5,9), new Pos(5,10), new Pos(5,11)));
		_barrier = barrier;
		_semaphores = semaphores;
		_criticalRegion = criticalRegion;
		this.no = no;
		this.cd = cd;
		mygate = g;
		startpos = cd.getStartPos(no);
		barpos   = cd.getBarrierPos(no);  // For later use


		col = chooseColor();

		// special settings for car no. 0
		if (no==0) {
			basespeed = -1.0;
			variation = 0; 
		}
	}

	public synchronized void setSpeed(double speed) { 
		basespeed = speed;
	}

	public synchronized void setVariation(int var) { 
		if (no != 0 && 0 <= var && var <= 100) {
			variation = var;
		}
		else
			cd.println("Illegal variation settings");
	}

	synchronized double chooseSpeed() { 
		double factor = (1.0D+(Math.random()-0.5D)*2*variation/100);
		return factor*basespeed;
	}

	Color chooseColor() { 
		return Color.blue; // You can get any color, as longs as it's blue 
	}

	Pos nextPos(Pos pos) {
		// Get my track from display
		return cd.nextPos(no,pos);
	}

	boolean atGate(Pos pos) {
		return pos.equals(startpos);
	}

	public void run() {
		try {
			CarI car = cd.newCar(no, col, startpos);
			curpos = startpos;
			cd.register(car);

			while (true) {

				if (atGate(curpos)) {
					mygate.pass(); 
					car.setSpeed(chooseSpeed());
				}


				newpos = nextPos(curpos);
				Alley.enterAlleyIfInFront(this);

				_semaphores[newpos.row][newpos.col].P();
				car.driveTo(newpos);
				_semaphores[curpos.row][curpos.col].V();


				Alley.leaveAlleyIfExit(this);

				_barrier.isPosBarrierEntrance(newpos,no);




				curpos = newpos;




			}

		} catch (Exception e) {
			cd.println("Exception in Car no. " + no);
			System.err.println("Exception in Car no. " + no + ":" + e);
			e.printStackTrace();
		}
	}

}

public class CarControl implements CarControlI{

	CarDisplayI cd;           // Reference to GUI
	Conductor[] conductor;    // Car controllers
	Gate[] gate;              // Gates
	Semaphore[][] semaphores;
	Semaphore criticalRegion;
	Alley alley;
	Barrier barrier;

	public CarControl(CarDisplayI cd) {
		this.cd = cd;
		conductor = new Conductor[9];
		gate = new Gate[9];
		semaphores = new Semaphore[11][12];
		criticalRegion = new Semaphore(1);
		barrier = new Barrier();

		//Creates array of semaphores for every tile
		for (int i = 0; i < semaphores.length; i++) {
			for (int j = 0; j < semaphores[0].length; j++ ) {
				semaphores[i][j] = new Semaphore(1);
			}
		}

		for (int no = 0; no < 9; no++) {
			gate[no] = new Gate();
			conductor[no] = new Conductor(no,cd,gate[no], semaphores, criticalRegion, barrier);
			//Sets the semaphore for the starting position as taken.
			try
			{
				semaphores[cd.getStartPos(no).row][cd.getStartPos(no).col].P();
			} catch (InterruptedException e) { }
			conductor[no].setName("Conductor-" + no);
			conductor[no].start();
		} 
	}

	public void startCar(int no) {
		gate[no].open();
	}

	public void stopCar(int no) {
		gate[no].close();
	}

	public void barrierOn() {
		barrier.on();
	}

	public void barrierOff() {
		barrier.off();
	}

	public void barrierSet(int k) { 
		cd.println("Barrier threshold setting not implemented in this version");
		// This sleep is solely for illustrating how blocking affects the GUI
		// Remove when feature is properly implemented.
		try { Thread.sleep(3000); } catch (InterruptedException e) { }
	}

	public void removeCar(int no) { 
		cd.println("Remove Car not implemented in this version");
	}

	public void restoreCar(int no) { 
		cd.println("Restore Car not implemented in this version");
	}

	/* Speed settings for testing purposes */

	public void setSpeed(int no, double speed) { 
		conductor[no].setSpeed(speed);
	}

	public void setVariation(int no, int var) { 
		conductor[no].setVariation(var);
	}

}






