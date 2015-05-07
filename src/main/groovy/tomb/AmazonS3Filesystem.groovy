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

    private File getTemporalFile() {
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
        } catch (Exception e) {
            throw new FilesystemException("Unable to upload to path ${path}")
        } finally {
            tempFile.delete()
        }
    }

    List<String> list(String path = null) {
        return s3Client.listObjects(this.bucket, path).objectSummaries.collect { it.key }
    }

    void copy(String initialPath, String destinationPath) {
        try {
            s3Client.copyObject(this.bucket, initialPath, this.bucket, destinationPath)
        } catch (Exception e) {
            throw new FilesystemException("Unable to copy path ${initialPath} to ${destinationPath}")
        }
    }

    void move(String initialPath, String destinationPath) {
        try {
            s3Client.copyObject(this.bucket, initialPath, this.bucket, destinationPath)
            this.delete(initialPath)
        } catch (Exception e) {
            throw new FilesystemException("Unable to move path ${initialPath} to ${destinationPath}")
        }
    }

    void delete(String path) {
        try {
            s3Client.deleteObject(this.bucket, path)
        } catch (Exception e) {
            throw new FilesystemException("Unable to delete path ${path}")
        }
    }

    URI getUri(String path) {
        try {
            return s3Client.getUrl(this.bucket, path).toURI()
        } catch (Exception e) {
            throw new FilesystemException("Unable to get url of path ${path}")
        }
    }

}
