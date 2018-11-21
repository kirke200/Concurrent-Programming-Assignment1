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
	public boolean newStart = false;

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
	ArrayList<Pos> criticalRegionEntrances = new ArrayList<>(Arrays.asList(new Pos(1,0), new Pos(8,0), new Pos(9,1), new Pos(9,2)));
	ArrayList<Pos> criticalRegionExits = new ArrayList<>(Arrays.asList(new Pos(1,1), new Pos(10,2)));

	boolean inCriticalRegion = false;
	Alley _alley;
	Barrier _barrier;
	CarI thisCar;
	Semaphore stopped;
	boolean removed;
	boolean restorationUnderway = false;
	boolean sleeping = false;
	Semaphore removeSemaphore = new Semaphore(1);
	RemovingCars _removingCars;




	public Conductor(int no, CarDisplayI cd, Gate g, Semaphore[][] semaphores, Semaphore criticalRegion, Alley alley, Barrier barrier, RemovingCars removingCars) {
		_alley = alley;
		_barrier = barrier;
		_semaphores = semaphores;
		_criticalRegion = criticalRegion;
		this.no = no;
		this.cd = cd;
		mygate = g;
		startpos = cd.getStartPos(no);
		barpos   = cd.getBarrierPos(no);  // For later use
		stopped = new Semaphore(0);
		_removingCars = removingCars;


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


	public CarI getCar() {
		return thisCar;
	}

	public void setCar(CarI car) {
		this.thisCar = car;
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

	public void getSemaphoreTokenFromPos(Pos pos) throws InterruptedException {
		_semaphores[pos.row][pos.col].P();
	}

	public void giveSemaphoreTokenFromPos(Pos pos) {
		_semaphores[pos.row][pos.col].V();
	}

	public void run() {
		try {
			if (this.newStart) {
				if (inCriticalRegion) {
					_alley.enter(no,this);
				}
				System.out.println(startpos);
				getSemaphoreTokenFromPos(startpos);
				this.newStart = false;
			}
			setCar(cd.newCar(no, col, startpos));
			curpos = startpos;
			cd.register(thisCar);

			W : while (true) {


				try {

				if (atGate(curpos)) { 
					mygate.pass(); 
					thisCar.setSpeed(chooseSpeed());
				}
				newpos = nextPos(curpos);


					_alley.enterAlleyIfInFront(this);
					getSemaphoreTokenFromPos(newpos);
				} catch (InterruptedException e) {
					giveSemaphoreTokenFromPos(curpos);
					break W;
				}
				try {
					thisCar.driveTo(newpos);
				} catch (InterruptedException e) {
					giveSemaphoreTokenFromPos(curpos);
					giveSemaphoreTokenFromPos(newpos);
					break W;
				}
					_semaphores[curpos.row][curpos.col].V();
					_alley.leaveAlleyIfExit(this);

					//_barrier.isPosBarrierEntrance(newpos,no);
					_barrier.isPosBarrierEntrance(newpos);


					curpos = newpos;


			}
			if (this.inCriticalRegion) {
				_alley.leave(no);
			}
			this.sleeping = true;
			this.removed = true;
			System.out.println("Car "+no+" removed");

		} catch (Exception e) {
			cd.println("Exception in Car no. " + no);
			System.err.println("Exception in Car no. " + no + ":" + e);
			e.printStackTrace();
		}
	}


	public synchronized void notifyCars() {
		notifyAll();
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
	RemovingCars removingCars = new RemovingCars(this);

	public CarControl(CarDisplayI cd) {
		this.cd = cd;
		conductor = new Conductor[9];
		gate = new Gate[9];
		semaphores = new Semaphore[11][12];
		criticalRegion = new Semaphore(1);
		alley = new Alley();
		barrier = new Barrier();

		//Creates array of semaphores for every tile
		for (int i = 0; i < semaphores.length; i++) {
			for (int j = 0; j < semaphores[0].length; j++ ) {
				semaphores[i][j] = new Semaphore(1);
			}
		}

		for (int no = 0; no < 9; no++) {
			gate[no] = new Gate();
			conductor[no] = new Conductor(no,cd,gate[no], semaphores, criticalRegion, alley, barrier, removingCars);
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

	public void stopCar(int no) { gate[no].close(); }

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
		removingCars.removeCar(no);
	}

	public void restoreCar(int no) {
		removingCars.restoreCar(no);
	}

	/* Speed settings for testing purposes */

	public void setSpeed(int no, double speed) { 
		conductor[no].setSpeed(speed);
	}

	public void setVariation(int no, int var) { 
		conductor[no].setVariation(var);
	}

}






