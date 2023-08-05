package org.in.com.aggregator.aws;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.in.com.config.ApplicationConfig;
import org.in.com.dto.AuthDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import net.sf.json.JSONObject;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import hirondelle.date4j.DateTime;

public class S3Client {

	public JsonArray getActiveTripList(AuthDTO authDTO, DateTime tripDate) {
		JsonArray tripList = null;
		try {
			Regions regions = Regions.fromName(ApplicationConfig.getS3Region());
			BasicAWSCredentials creds = new BasicAWSCredentials(ApplicationConfig.getS3AccessKey(), ApplicationConfig.getS3SecretKey());
			AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(creds)).withRegion(regions).build();

			String oldfileName = "bits/dailytrips/" + authDTO.getNamespaceCode() + "/" + tripDate.format("YYYY-MM-DD") + ".json";
			String fileName = "bits/trip/" + authDTO.getNamespaceCode() + "/" + tripDate.format("YYYY") + "/" + tripDate.format("YYYY-MM-DD") + ".json";
			S3Object s3Object = null;
			if (s3Client.doesObjectExist(ApplicationConfig.getS3BucketName(), fileName)) {
				s3Object = s3Client.getObject(ApplicationConfig.getS3BucketName(), fileName);
			}
			else if (s3Client.doesObjectExist(ApplicationConfig.getS3BucketName(), oldfileName)) {
				s3Object = s3Client.getObject(ApplicationConfig.getS3BucketName(), oldfileName);
			}
			else {
				throw new ServiceException(ErrorCode.UNABLE_TO_PROVIDE_DATA);
			}
			InputStream objectData = s3Object.getObjectContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(objectData));

			String s3data = reader.readLine();
			JsonParser jsonParser = new JsonParser();
			JsonObject jsonObject = (JsonObject) jsonParser.parse(s3data);
			if (jsonObject.get("status").getAsInt() != 1) {
				throw new ServiceException(ErrorCode.UNABLE_TO_PROVIDE_DATA);
			}
			tripList = jsonObject.get("data").getAsJsonArray();
			objectData.close();
		}
		catch (AmazonS3Exception e) {
			System.out.println(e.getErrorCode() + " - " + e.getErrorMessage() + " " + e.getAdditionalDetails().get("Key"));
			throw new ServiceException(ErrorCode.UNABLE_TO_PROVIDE_DATA);
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(ErrorCode.UNABLE_TO_PROVIDE_DATA);
		}
		return tripList;
	}

	public void exportReport(AuthDTO authDTO, ByteArrayInputStream stream, String finalUrl) {
		try {
			Regions regions = Regions.fromName(ApplicationConfig.getS3Region());
			BasicAWSCredentials creds = new BasicAWSCredentials(ApplicationConfig.getS3AccessKey(), ApplicationConfig.getS3SecretKey());
			AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(creds)).withRegion(regions).build();

			if (s3Client.doesBucketExistV2(ApplicationConfig.getS3BucketName())) {
				ObjectMetadata metadata = new ObjectMetadata();
				metadata.setContentLength(stream.available());
				PutObjectRequest putObjectRequest = new PutObjectRequest(ApplicationConfig.getS3BucketName(), finalUrl, stream, metadata);// .withCannedAcl(CannedAccessControlList.PublicRead);
				PutObjectResult result = s3Client.putObject(putObjectRequest);
				System.out.println(authDTO.getNamespaceCode() + "  - - " + finalUrl + " - " + "uploaded " + result.getETag());
			}
		}
		catch (AmazonS3Exception e) {
			System.out.println(e.getErrorCode() + " - " + e.getErrorMessage() + " " + e.getAdditionalDetails().get("Key"));
			throw new ServiceException(ErrorCode.UNABLE_TO_PROVIDE_DATA);
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(ErrorCode.UNABLE_TO_PROVIDE_DATA);
		}
	}

	public URL generatePresignedURL(AuthDTO authDTO, String reportName, String fileName) {
		URL url = null;
		Regions regions = Regions.fromName(ApplicationConfig.getS3Region());
		try {
			BasicAWSCredentials creds = new BasicAWSCredentials(ApplicationConfig.getS3AccessKey(), ApplicationConfig.getS3SecretKey());
			AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(creds)).withRegion(regions).build();
			if (!s3Client.doesBucketExistV2(ApplicationConfig.getS3BucketName())) {
				throw new ServiceException(ErrorCode.INVALID_CREDENTIALS, "S3 Bucket Name not Found");
			}
			if (!s3Client.doesObjectExist(ApplicationConfig.getS3BucketName(), fileName)) {
				throw new ServiceException(ErrorCode.INVALID_CREDENTIALS, "File Name not Found");
			}

			// Set the presigned URL to expire after 15 mins.
			java.util.Date expiration = new java.util.Date();
			long expTimeMillis = expiration.getTime();
			expTimeMillis += 1000 * 60 * 15;
			expiration.setTime(expTimeMillis);
			// fileName = ApplicationConfig.getServerZoneCode() + "/" +
			// authDTO.getNamespaceCode() + "/report/" + reportName + "/" +
			// fileName;

			// Generate the presigned URL.
			GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(ApplicationConfig.getS3BucketName(), fileName).withMethod(HttpMethod.GET).withExpiration(expiration);
			url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);

			System.out.println("Pre-Signed URL: " + url.toString());
		}
		catch (AmazonServiceException e) {
			e.printStackTrace();
			throw new ServiceException(ErrorCode.INVALID_CREDENTIALS, "Unable to read files");
		}
		catch (SdkClientException e) {
			e.printStackTrace();
			throw new ServiceException(ErrorCode.INVALID_CREDENTIALS, "Unable to read files");
		}
		return url;
	}

	public JSONObject getAllStateFuelPrice(AuthDTO authDTO, DateTime currentDate) {
		JSONObject fuelDetails = null;
		try {
			Regions regions = Regions.fromName(ApplicationConfig.getS3Region());
			BasicAWSCredentials creds = new BasicAWSCredentials(ApplicationConfig.getS3AccessKey(), ApplicationConfig.getS3SecretKey());
			AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(creds)).withRegion(regions).build();

			String fileName = "bits/fuel-price/" + currentDate.format("YYYY-MM-DD") + ".json";
			S3Object s3Object = null;
			if (s3Client.doesObjectExist(ApplicationConfig.getS3BucketName(), fileName)) {
				s3Object = s3Client.getObject(ApplicationConfig.getS3BucketName(), fileName);
			}
			else {
				throw new ServiceException(ErrorCode.UNABLE_TO_PROVIDE_DATA);
			}
			InputStream objectData = s3Object.getObjectContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(objectData));

			String s3data = reader.readLine();
 			JSONObject jsonObject = JSONObject.fromObject(s3data);
			if (!jsonObject.has("price")) {
				throw new ServiceException(ErrorCode.UNABLE_TO_PROVIDE_DATA);
			}
			fuelDetails = jsonObject;
			objectData.close();
		}
		catch (AmazonS3Exception e) {
			System.out.println(e.getErrorCode() + " - " + e.getErrorMessage() + " " + e.getAdditionalDetails().get("Key"));
			throw new ServiceException(ErrorCode.UNABLE_TO_PROVIDE_DATA);
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(ErrorCode.UNABLE_TO_PROVIDE_DATA);
		}
		return fuelDetails;
	}
}
