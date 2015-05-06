package tomb

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.auth.BasicAWSCredentials

class AmazonS3Filesystem implements FilesystemProvider {

    String bucket
    AmazonS3 s3Client

    AmazonS3Filesystem(String key, String secret, String bucket) {
        this.bucket = bucket
        this.s3Client = new AmazonS3Client([key, secret] as BasicAWSCredentials)
    }

    File getTemporalFile() {
        return File.createTempFile(this.bucket, '_tmp')
    }

    InputStream get(String path) {
        return s3Client.getObject(this.bucket, path).objectContent
    }

    void put(InputStream inputStream, String path) {
        File tempFile = this.temporalFile
        tempFile.append(inputStream)

        try {
            s3Client.putObject(this.bucket, path, tempFile)
        } catch(Exception e) {
            throw new FilesystemException("Unable to upload to path ${path}")
        } finally {
            tempFile.delete()
        }
    }

    List<String> list(String path) {
        return s3Client.listObjects(this.bucket).objectSummaries.collect { it.key }
    }

    void copy(String initialPath, String destinationPath) {}

    void move(String initialPath, String destinationPath) {}

    void delete(String path) {}

    URL getUrl(String path) {}

}
