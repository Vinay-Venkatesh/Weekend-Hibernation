package com.automation;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClientBuilder;
import com.amazonaws.services.autoscaling.model.UpdateAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.UpdateAutoScalingGroupResult;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.util.Secrets;

public class Autoscaling {
	
	String accessKeyName = System.getenv("access_key_secret_name");
	String secretKeyName = System.getenv("secret_key_secret_name");
	String secret_manager_region = System.getenv("secret_manager_region");
	
	//Get Access and Secret Key
	String accessKey = Secrets.getSecrets(accessKeyName, secret_manager_region);
	String secretKey = Secrets.getSecrets(secretKeyName, secret_manager_region);
	
	//RoleArn
	String roleARN = System.getenv("roleArn");
	
	String param_values = System.getenv("asg_name_region_min_max");
	String clientRegion;
	String asg_name = null;
	int min_value;
	int max_value;
	String[] values = param_values.split(",");
	String roleSessionName = "autoscaling";
	
	AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
	public void stopInstances() {
		for(String value : values) {
			String[] splits;
			splits = value.split("_"); // autoscaling_group_name:region:min:max
			asg_name=splits[0];
			clientRegion=splits[1];
			min_value=Integer.parseInt(splits[2]);
			max_value=Integer.parseInt(splits[3]);
			
			System.out.println(asg_name+" "+clientRegion+" "+min_value+" "+max_value);
			
			//Update Autoscaling group
			UpdateAutoScalingGroupRequest updateAutoScalingGroupRequest = new UpdateAutoScalingGroupRequest()
					.withAutoScalingGroupName(asg_name)
					.withMaxSize(min_value)
					.withMinSize(max_value);
			
			UpdateAutoScalingGroupResult autoScalingGroupResult = getClient(clientRegion).updateAutoScalingGroup(updateAutoScalingGroupRequest);
		}
	}
	
	public AmazonAutoScaling getClient(String aws_region) {
		
        AWSSecurityTokenService asgClient = AWSSecurityTokenServiceClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(aws_region)
                .build();
        
        AssumeRoleRequest roleRequest = new AssumeRoleRequest()
                .withRoleArn(roleARN)
                .withRoleSessionName(roleSessionName);
		AssumeRoleResult roleResponse = asgClient.assumeRole(roleRequest);
		Credentials sessionCredentials = roleResponse.getCredentials();
		
		// Create a BasicSessionCredentials object that contains the credentials you just retrieved.
		BasicSessionCredentials awsCredentials = new BasicSessionCredentials(
		sessionCredentials.getAccessKeyId(),
		sessionCredentials.getSecretAccessKey(),
		sessionCredentials.getSessionToken());
		
		AmazonAutoScaling client = AmazonAutoScalingClientBuilder.standard()
			.withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
            .withRegion(aws_region)
            .build();
		
		return client;
	}

}
