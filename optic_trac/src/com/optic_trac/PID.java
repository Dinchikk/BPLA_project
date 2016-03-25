package com.optic_trac;

public class PID {
	private double P;
	private double I;
	private double D;
	private int max_cmd;
	private int min_cmd;
	
	private double p_error_last = 0.0f;
	private double p_error = 0.0f;
	private double d_error = 0.0f;
	private double i_error = 0.0f;
	private int cmd = 0;

	
	public PID() {
		P = 1.0f;
		I = 0.0f;
		D = 0.0f;
		max_cmd = 200;
		min_cmd = -200;
		
		p_error_last = 0.0f;
		p_error = 0.0f;
		d_error = 0.0f;
		i_error = 0.0f;
		cmd = 0;
		
	}
	public PID(double P, double I, double D, int Max, int Min) {
		this.P = P;
		this.I = I;
		this.D = D;
		max_cmd = Max;
		min_cmd = Min;
		
		p_error_last = 0.0f;
		p_error = 0.0f;
		d_error = 0.0f;
		i_error = 0.0f;
		cmd = 0;
		
	}
	
	public int updatePid(double error, double dt){
		  double p_term, d_term, i_term;
		  p_error = error; //this is pError = pState-pTarget

		  if (dt == 0.0)
		    return 0;

		  // Calculate proportional contribution to command
		  p_term = P * p_error;

		  // Calculate the integral error
		  i_error = i_error + dt * p_error;

		  //Calculate integral contribution to command
		  i_term = I * i_error;

		  // Limit i_term so that the limit is meaningful in the output

		  // Calculate the derivative error
		  if (dt != 0.0f)
		  {
		    d_error = (p_error - p_error_last) / dt;
		    p_error_last = p_error;
		  }
		  // Calculate derivative contribution to command
		  d_term = D * d_error;
		  
		  cmd = (int) (-(p_term + i_term + d_term));

		  if (cmd > max_cmd)
		  {
			  return max_cmd;
		  }
		  else if (cmd < min_cmd)
		  {
			  return min_cmd;
		  }
		  return cmd;
		
	}

	public void setGains(double P, double I, double D, int Max, int Min){
		this.P = P;
		this.I = I;
		this.D = D;
		max_cmd = Max;
		min_cmd = Min;
	}
	public void reset(){
		p_error_last = 0.0f;
		p_error = 0.0f;
		d_error = 0.0f;
		i_error = 0.0f;
		cmd = 0;
	}

}
