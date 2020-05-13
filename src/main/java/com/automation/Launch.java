package com.automation;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class Launch implements RequestHandler<Object, Object> {
	// public static void main(String[] args) {
	public Object handleRequest(Object arg0, Context arg1) {
		try {
			Autoscaling autoscaling = new Autoscaling();
			autoscaling.stopInstances();
		} catch (Exception e) {
			return e.getMessage();
		}
		return "Success";
		/*
		 * RDS rds = new RDS(); rds.stopRds();
		 */
	}
}
