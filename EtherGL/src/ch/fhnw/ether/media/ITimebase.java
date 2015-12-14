package ch.fhnw.ether.media;

public interface ITimebase {
	double ASAP = -1000;
	double SEC2NS = 1000 * 1000 * 1000;
	double SEC2US = 1000 * 1000;
	double SEC2MS = 1000;
	
	double  getTime();	
	boolean isRealTime();
}
