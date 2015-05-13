package tomb.utils

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.auth.BasicAWSCredentials

import tomb.exception.FilesystemException

class AmazonS3Utils {
    static AmazonS3Client getClient(String key, String secret) {
        try {
            return new AmazonS3Client([key, secret] as BasicAWSCredentials)
        } catch(Exception e) {
            throw new FilesystemException("Cannot connect with amazon. Maybe the credentials are invalid?")
        }
    }

    static void checkBucket(AmazonS3Client client, String bucket) {
        if (!client.doesBucketExist(bucket)) {
            throw new FilesystemException("The bucket ${bucket} doesn't exist")
        }
    }
}
