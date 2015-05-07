package tomb

import java.nio.file.Path

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.auth.BasicAWSCredentials

class AmazonS3Filesystem implements FilesystemProvider {

    String bucket
    AmazonS3 s3Client
    Path basePath

    AmazonS3Filesystem(String key, String secret, String bucket, Path basePath) {
        this.bucket = bucket
        this.s3Client = new AmazonS3Client([key, secret] as BasicAWSCredentials)
        this.basePath = basePath
    }

    private File getTemporalFile() {
        return File.createTempFile(this.bucket, '_tmp')
    }

    InputStream get(Path path) {
        return s3Client.getObject(this.bucket, path.toString()).objectContent
    }

    void put(InputStream inputStream, Path path) {
        File tempFile = this.temporalFile
        tempFile.withOutputStream { it.write(inputStream.bytes) }

        try {
            s3Client.putObject(this.bucket, path.toString(), tempFile)
        } catch (Exception e) {
            throw new FilesystemException("Unable to upload to path ${path}")
        } finally {
            tempFile.delete()
        }
    }

    List<String> list(Path path = Paths.get('')) {
        return s3Client.listObjects(this.bucket, path.toString()).objectSummaries.collect { it.key }
    }

    void copy(Path initialPath, Path destinationPath) {
        try {
            s3Client.copyObject(this.bucket, initialPath.toString(), this.bucket, destinationPath.toString())
        } catch (Exception e) {
            throw new FilesystemException("Unable to copy path ${initialPath} to ${destinationPath}")
        }
    }

    void move(Path initialPath, Path destinationPath) {
        try {
            s3Client.copyObject(this.bucket, initialPath.toString(), this.bucket, destinationPath.toString())
            this.delete(initialPath)
        } catch (Exception e) {
            throw new FilesystemException("Unable to move path ${initialPath} to ${destinationPath}")
        }
    }

    void delete(Path path) {
        try {
            s3Client.deleteObject(this.bucket, path.toString())
        } catch (Exception e) {
            throw new FilesystemException("Unable to delete path ${path}")
        }
    }

    URI getUri(Path path) {
        try {
            return s3Client.getUrl(this.bucket, path.toString()).toURI()
        } catch (Exception e) {
            throw new FilesystemException("Unable to get url of path ${path}")
        }
    }

}
