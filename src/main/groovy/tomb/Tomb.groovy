package tomb

import java.nio.file.Path
import java.nio.file.Paths

import com.amazonaws.services.s3.AmazonS3Client

import tomb.utils.AmazonS3Utils
import tomb.filesystem.LocalFilesystem
import tomb.filesystem.AmazonS3Filesystem
import tomb.exception.FilesystemException

class Tomb {

    static LocalFilesystem getLocalFilesystem(Path basePath = Paths.get('/')) {
        File basePathFile = basePath.toFile()
        if (!basePathFile.exists()) {
            throw new FilesystemException("Path ${basePath} doesn't exist")
        }

        if (!basePathFile.isDirectory()) {
            throw new FilesystemException("Path ${basePath} is not a directory")
        }

        LocalFilesystem fs = new LocalFilesystem()
        fs.basePath = basePath

        return fs
    }

    static AmazonS3Filesystem getAmazonS3Filesystem(String key, String secret, String bucket, Path basePath = Paths.get('')) {
        AmazonS3Client s3Client = AmazonS3Utils.getClient(key, secret)
        AmazonS3Utils.checkBucket(s3Client, bucket)

        AmazonS3Filesystem fs = new AmazonS3Filesystem()
        fs.basePath = basePath
        fs.s3Client = s3Client
        fs.bucket = bucket

        return fs
    }

}
