package nl.tudelft.in4303.grading;

public interface IResult {
	public static enum Status {
		SUCCESS,
		FAILURE,
		ERROR
	};
	
	public abstract Status getStatus();

	/**
	 * Get status description (maxium is 140 characters)
	 *
	 * @return
	 */
	public abstract String getStatusDescription();
	
	public abstract String getFeedback();
	
	public abstract String getGrade();
}
