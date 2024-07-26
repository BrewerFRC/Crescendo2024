package frc.robot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Common {
    	private static final DateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
	private static String lastMessage = "";

	public static void debug(String text) {
		if (!lastMessage.equals(text)) {
			System.out.println(formatter.format(new Date(time())) + text);
		}
		lastMessage = text;
	}
	
	public static void dashStr(String title, String a) {
		SmartDashboard.putString(title + ": ", a);
	}
	
	public static void dashNum(String title, int a) {
		SmartDashboard.putNumber(title + ": ", a);
	}
	
	public static void dashNum(String title, double a) {
		SmartDashboard.putNumber(title + ": ", a);
	}
	
	public static void dashBool(String Title, boolean a) {
		SmartDashboard.putBoolean(Title, a);
	}
	
	public static long time() {
		return Calendar.getInstance().getTimeInMillis();
	}
	/**Takes a value and a range it falls within and converts it to a different range.
	 * Defaults to minimum input if it exceeds the min or max input.
	 * 
	 * @param input -Value to be converted to a different range
	 * @param minInput -Minimum value for the range of the input
	 * @param maxInput -Maximum value for the range of the input
	 * @param minOutput -Minimum value for the range of the output
	 * @param maxOutput -Maximum value for the range of the output
	 * @return double - A value in the output range that is proportional to the input
	 */
	public static double map(double input, double minInput, double maxInput, double minOutput, double maxOutput) {
		input = Math.min(Math.max(input, minInput), maxInput);
		double inputRange = maxInput - minInput;
		double inputPercentage = (input-minInput)/inputRange;
		double outputRange = maxOutput - minOutput;
		double output = (outputRange * inputPercentage) + minOutput;
		return output;
	}

    /**
	 * Used mostly to specify the neutral range of a controller axis.
	 * 
	 * @param input the current axis input value.
	 * @param deadzone the absolute value of the deadzone.  
	 * @return Inputs upto the magnitude of the deadzone will return as zero, otherwise the input value is returned.
	 */
	public static double deadzone(double input, double deadzone) {
		if (Math.abs(input) < deadzone) {
			return(0);
		} else {
			return(input);
		}
	}

	/**
	 * Constrain a value between min and max values
	 * @param x
	 * @param low
	 * @param high
	 * @return x constrained
	 */
	public static double constrain(double x, double low, double high) {
		if (x < low) {
			x = low;
		} else {
			if (x > high) {
				x = high;
			}
		}
		return x;
	}

	public static double ramp(double currentValue, double targetValue, double rampRate){
		if(currentValue < targetValue){
			return currentValue + rampRate;
		}
		if(currentValue > targetValue){
			return currentValue - rampRate;
		}
		return currentValue;
	}
}
