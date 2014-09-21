package nl.tudelft.in4303.grading;


public interface IResult {
	public static enum Status {
		SUCCESS,
		FAILURE,
		ERROR
	};
	
	public abstract Status getStatus();

	public abstract String getReport();
}