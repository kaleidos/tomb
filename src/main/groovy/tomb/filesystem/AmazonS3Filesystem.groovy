package tomb.filesystem

import java.nio.file.Path
import java.nio.file.Paths

import com.amazonaws.services.s3.AmazonS3Client

import tomb.exception.FilesystemException

class AmazonS3Filesystem implements FilesystemProvider {

    String bucket
    AmazonS3Client s3Client
    Path basePath

    private File getTemporalFile() {
        return File.createTempFile(this.bucket, '_tmp')
    }

    Path resolve(Path path) {
        return basePath.resolve(path)
    }

    Boolean exists(Path relativePath) {
        Path path = resolve(relativePath)

        try {
            s3Client.getObjectMetadata(this.bucket, path.toString())
            return true
        } catch(Exception e) {
            return false
        }
    }

    InputStream get(Path relativePath) {
        Path path = resolve(relativePath)

        try {
            return s3Client.getObject(this.bucket, path.toString()).objectContent
        } catch(Exception e) {
            throw new FilesystemException("The key ${path} does not exist")
        }
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

    List<String> list(Path relativePath = null) {
        Path path = resolve(relativePath ?: Paths.get(''))

        List<String> list = s3Client.listObjects(this.bucket, path.toString()).objectSummaries.collect { it.key }

        if (path.toString()) {
            Closure relativizeFilename = { String fileName -> fileName - "${path}/" }

            return list.collect(relativizeFilename)
        } else {
            return list
        }

    }

    void copy(Path initialRelativePath, Path destinationRelativePath) {
        Path initialPath = resolve(initialRelativePath)
        Path destinationPath = resolve(destinationRelativePath)

        if (this.exists(destinationRelativePath)) {
            throw new FilesystemException("The destination path ${destinationPath} already exists")
        }

        try {
            s3Client.copyObject(this.bucket, initialPath.toString(), this.bucket, destinationPath.toString())
        } catch (Exception e) {
            throw new FilesystemException("Unable to copy path ${initialPath} to ${destinationPath}")
        }
    }

    void move(Path initialRelativePath, Path destinationRelativePath) {
        Path initialPath = resolve(initialRelativePath)
        Path destinationPath = resolve(destinationRelativePath)

        if (this.exists(destinationRelativePath)) {
            throw new FilesystemException("The destination path ${destinationPath} already exists")
        }

        try {
            s3Client.copyObject(this.bucket, initialPath.toString(), this.bucket, destinationPath.toString())
            this.delete(initialPath)
        } catch (Exception e) {
            throw new FilesystemException("Unable to move path ${initialPath} to ${destinationPath}")
        }
    }

    void delete(Path relativePath) {
        Path path = resolve(relativePath)

        if (!this.exists(relativePath)) {
            throw new FilesystemException("The path ${path} doesn't exist")
        }

        try {
            s3Client.deleteObject(this.bucket, path.toString())
        } catch (Exception e) {
            throw new FilesystemException("Unable to delete path ${path}")
        }
    }

    URI getUri(Path relativePath) {
        Path path = resolve(relativePath)

        if (!this.exists(relativePath)) {
            throw new FilesystemException("The path ${path} doesn't exist")
        }

        try {
            return s3Client.getUrl(this.bucket, path.toString()).toURI()
        } catch (Exception e) {
            throw new FilesystemException("Unable to get url of path ${path}")
        }
    }

}
