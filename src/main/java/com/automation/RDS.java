package com.automation;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.AmazonRDSClientBuilder;
import com.amazonaws.services.rds.model.StopDBInstanceRequest;
import com.amazonaws.services.rds.model.StartDBInstanceRequest;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.util.Secrets;

public class RDS {
	
	String accessKeyName = System.getenv("access_key_secret_name");
	String secretKeyName = System.getenv("secret_key_secret_name");
	String secret_manager_region = System.getenv("secret_manager_region");
	
	//Get Access and Secret Key
	String accessKey = Secrets.getSecrets(accessKeyName, secret_manager_region);
	String secretKey = Secrets.getSecrets(secretKeyName, secret_manager_region);
		
	String param_values = System.getenv("identifier_region_status");
	String clientRegion;
	String asg_name = null;
	int min_value;
	int max_value;
	String[] values = param_values.split(",");
	String roleSessionName = "rds";
	String roleARN = System.getenv("roleArn");
	 
	
	AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
	public void stopRds() {
		for(String value : values) {
			String[] splits;
			splits = value.split("_"); // rds_name:region:status
			String identifier = splits[0];
			String clientRegion = splits[1];
			String rdsStatus = splits[2];
			
			//Start RDS
			if(rdsStatus.equalsIgnoreCase("start")) {
				StartDBInstanceRequest startDbRequest = new StartDBInstanceRequest()
						.withDBInstanceIdentifier(identifier);
				
				getClient(clientRegion).startDBInstance(startDbRequest);
					
			}else if(rdsStatus.equalsIgnoreCase("stop")) {
				//Stop RDS
				StopDBInstanceRequest stopDbRequest = new StopDBInstanceRequest()
						.withDBInstanceIdentifier(identifier);
				
				getClient(clientRegion).stopDBInstance(stopDbRequest);
			}
		}
	}
	
	public AmazonRDS getClient(String aws_region) {
		
        AWSSecurityTokenService rdsClient = AWSSecurityTokenServiceClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(aws_region)
                .build();
        
        AssumeRoleRequest roleRequest = new AssumeRoleRequest()
                .withRoleArn(roleARN)
                .withRoleSessionName(roleSessionName);
		AssumeRoleResult roleResponse = rdsClient.assumeRole(roleRequest);
		Credentials sessionCredentials = roleResponse.getCredentials();
		
		// Create a BasicSessionCredentials object that contains the credentials you just retrieved.
		BasicSessionCredentials awsCredentials = new BasicSessionCredentials(
		sessionCredentials.getAccessKeyId(),
		sessionCredentials.getSecretAccessKey(),
		sessionCredentials.getSessionToken());
		
		AmazonRDS client = AmazonRDSClientBuilder.standard()
			.withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
            .withRegion(aws_region)
            .build();
		
		return client;
	}

}
